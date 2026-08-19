package cn.fj.loli.kaitaistructsupport.runtime;

import cn.fj.loli.hexsupport.structure.BinarySnapshot;
import cn.fj.loli.hexsupport.structure.StructureAnalysisResult;
import cn.fj.loli.hexsupport.structure.StructureDiagnostic;
import cn.fj.loli.kaitaistructsupport.KaitaiStructSupportBundle;
import io.kaitai.struct.ByteBufferKaitaiStream;
import io.kaitai.struct.KaitaiStream;
import io.kaitai.struct.KaitaiStruct;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class KaitaiStructStructureEngine {
    private static final int READ_CHUNK_SIZE = 1024 * 1024;

    public StructureAnalysisResult analyze(Path template, BinarySnapshot snapshot, BooleanSupplier canceled) {
        try (KaitaiStructCompiler.CompiledSchema compiled = new KaitaiStructCompiler().compile(template, canceled)) {
            byte[] input = materialize(snapshot, canceled);
            KaitaiStruct root = instantiate(compiled.rootClass(), input);
            root.getClass().getMethod("_fetchInstances").invoke(root);
            GeneratedStructureMapper.MappingResult mapped = new GeneratedStructureMapper(canceled).map(root);
            List<String> output = new ArrayList<>();
            output.add(KaitaiStructSupportBundle.message("runtime.output.compiler"));
            compiled.compilerOutput().lines().filter(line -> !line.isBlank()).forEach(output::add);
            return new StructureAnalysisResult(template, snapshot.revision(), mapped.nodes(),
                    mapped.diagnostics(), output);
        } catch (KaitaiStructCompiler.KaitaiStructCanceledException failure) {
            return result(template, snapshot, StructureDiagnostic.Severity.INFO, failure.getMessage());
        } catch (InvocationTargetException failure) {
            Throwable cause = failure.getCause() == null ? failure : failure.getCause();
            return result(template, snapshot, StructureDiagnostic.Severity.ERROR,
                    KaitaiStructSupportBundle.message("runtime.error.instantiate", message(cause)));
        } catch (Throwable failure) {
            return result(template, snapshot, StructureDiagnostic.Severity.ERROR, message(failure));
        }
    }

    private static KaitaiStruct instantiate(Class<?> rootClass, byte[] input) throws ReflectiveOperationException {
        Object value = rootClass.getConstructor(KaitaiStream.class)
                .newInstance(new ByteBufferKaitaiStream(input));
        return (KaitaiStruct) value;
    }

    private static byte[] materialize(BinarySnapshot snapshot, BooleanSupplier canceled) {
        long length = snapshot.length();
        if (length > Integer.MAX_VALUE) throw new IllegalArgumentException(
                KaitaiStructSupportBundle.message("runtime.error.inputTooLarge", length));
        byte[] result = new byte[(int) length];
        int offset = 0;
        while (offset < result.length) {
            if (canceled.getAsBoolean()) throw new KaitaiStructCompiler.KaitaiStructCanceledException();
            int requested = Math.min(READ_CHUNK_SIZE, result.length - offset);
            byte[] chunk = snapshot.read(offset, requested);
            if (chunk.length != requested) throw new IllegalStateException(
                    KaitaiStructSupportBundle.message("runtime.error.snapshotRead", offset));
            System.arraycopy(chunk, 0, result, offset, requested);
            offset += requested;
        }
        return result;
    }

    private static StructureAnalysisResult result(Path template, BinarySnapshot snapshot,
                                                  StructureDiagnostic.Severity severity, String message) {
        return new StructureAnalysisResult(template, snapshot.revision(), List.of(),
                List.of(new StructureDiagnostic(severity, 0, 0, message)), List.of());
    }

    private static String message(Throwable failure) {
        String value = failure.getMessage();
        return value == null || value.isBlank() ? failure.getClass().getSimpleName() : value;
    }
}

