package cn.fj.loli.kaitaistructsupport.runtime;

import cn.fj.loli.kaitaistructsupport.KaitaiStructSupportBundle;
import io.kaitai.struct.JavaMain;
import io.kaitai.struct.KaitaiStruct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

/** Compiles KSY through the bundled official KSC and loads the generated Java parser. */
public final class KaitaiStructCompiler {
    private static final String COMPILER_VERSION = "0.11";
    private static final Object COMPILER_LOCK = new Object();
    private static final List<String> COMPILER_CLASSES = List.of(
            "io.kaitai.struct.JavaMain",
            "scala.Option",
            "scopt.OptionParser",
            "fastparse.Parsed",
            "sourcecode.Name",
            "geny.Generator",
            "org.yaml.snakeyaml.Yaml"
    );

    public CompiledSchema compile(Path template, BooleanSupplier canceled) throws IOException {
        Path normalized = template.toAbsolutePath().normalize();
        SchemaIdentity identity = identity(normalized);
        String packageName = "cn.fj.loli.kaitaistructsupport.generated.h" + identity.hash.substring(0, 16);
        String className = packageName + "." + upperCamel(identity.rootId);
        Path cache = cacheRoot().resolve(identity.hash);
        Path sources = cache.resolve("sources");
        Path classes = cache.resolve("classes");
        Path complete = cache.resolve("complete");

        synchronized (COMPILER_LOCK) {
            if (!Files.isRegularFile(complete)) {
                Files.createDirectories(sources);
                Files.createDirectories(classes);
                String compilerOutput = runOfficialCompiler(normalized, sources, packageName, canceled);
                compileJava(sources, classes, canceled);
                Files.writeString(cache.resolve("compiler-output.txt"), compilerOutput, StandardCharsets.UTF_8);
                Files.writeString(complete, className, StandardCharsets.UTF_8);
            }
        }

        URLClassLoader loader = new URLClassLoader(new URL[]{classes.toUri().toURL()}, KaitaiStructCompiler.class.getClassLoader());
        try {
            Class<?> rootClass = Class.forName(className, true, loader);
            String output = Files.exists(cache.resolve("compiler-output.txt"))
                    ? Files.readString(cache.resolve("compiler-output.txt"), StandardCharsets.UTF_8) : "";
            return new CompiledSchema(rootClass, loader, identity.hash, output);
        } catch (ClassNotFoundException failure) {
            try { loader.close(); } catch (IOException ignored) { }
            throw new IOException("Generated root class was not found: " + className, failure);
        }
    }

    private static String runOfficialCompiler(Path template, Path output, String packageName,
                                              BooleanSupplier canceled) throws IOException {
        Path java = javaExecutable("java");
        List<String> command = new ArrayList<>();
        command.add(java.toString());
        command.add("-cp");
        command.add(compilerClasspath());
        command.add(JavaMain.class.getName());
        command.add("-t");
        command.add("java");
        command.add("--read-pos");
        command.add("--java-package");
        command.add(packageName);
        command.add("-I");
        command.add(template.getParent().toString());
        command.add("-d");
        command.add(output.toString());
        command.add(template.toString());

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        CompletableFuture<String> outputReader = CompletableFuture.supplyAsync(() -> {
            try {
                return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException failure) {
                return failure.getMessage();
            }
        });
        try {
            while (process.isAlive()) {
                if (canceled.getAsBoolean()) {
                    process.destroyForcibly();
                    throw new KaitaiStructCanceledException();
                }
                Thread.sleep(50);
            }
            String compilerOutput = outputReader.join().strip();
            if (process.exitValue() != 0) {
                throw new IOException(KaitaiStructSupportBundle.message("runtime.error.compile", compilerOutput));
            }
            return compilerOutput;
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new IOException("Kaitai Struct compilation was interrupted.", failure);
        }
    }

    private static void compileJava(Path sources, Path classes, BooleanSupplier canceled) throws IOException {
        checkCanceled(canceled);
        List<Path> javaSources;
        try (Stream<Path> files = Files.walk(sources)) {
            javaSources = files.filter(path -> path.getFileName().toString().endsWith(".java"))
                    .sorted().toList();
        }
        if (javaSources.isEmpty()) throw new IOException("Kaitai Struct Compiler generated no Java sources.");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            compileWithJavac(javaSources, classes, canceled);
            return;
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, LocaleHolder.ROOT, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromPaths(javaSources);
            List<String> options = List.of(
                    "-proc:none", "--release", "17", "-encoding", "UTF-8",
                    "-classpath", runtimeClasspath(), "-d", classes.toString()
            );
            boolean success = Boolean.TRUE.equals(compiler.getTask(null, manager, diagnostics, options, null, units).call());
            checkCanceled(canceled);
            if (!success) throw new IOException(KaitaiStructSupportBundle.message(
                    "runtime.error.javaCompilation", formatDiagnostics(diagnostics.getDiagnostics())));
        }
    }

    private static void compileWithJavac(List<Path> sources, Path classes,
                                         BooleanSupplier canceled) throws IOException {
        Path javac = javaExecutable("javac");
        Path arguments = classes.getParent().resolve("javac.args");
        List<String> lines = new ArrayList<>(List.of(
                "-proc:none", "--release", "17", "-encoding", "UTF-8",
                "-classpath", quoteArgument(runtimeClasspath()), "-d", quoteArgument(classes.toString())
        ));
        for (Path source : sources) lines.add(quoteArgument(source.toString()));
        Files.write(arguments, lines, StandardCharsets.UTF_8);
        Process process = new ProcessBuilder(javac.toString(), "@" + arguments).redirectErrorStream(true).start();
        CompletableFuture<String> output = CompletableFuture.supplyAsync(() -> {
            try { return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8); }
            catch (IOException failure) { return failure.getMessage(); }
        });
        try {
            while (process.isAlive()) {
                if (canceled.getAsBoolean()) {
                    process.destroyForcibly();
                    throw new KaitaiStructCanceledException();
                }
                Thread.sleep(50);
            }
            if (process.exitValue() != 0) throw new IOException(KaitaiStructSupportBundle.message(
                    "runtime.error.javaCompilation", System.lineSeparator() + output.join().strip()));
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new IOException("Generated Java compilation was interrupted.", failure);
        }
    }

    private static String compilerClasspath() throws IOException {
        Set<String> entries = new LinkedHashSet<>();
        for (String className : COMPILER_CLASSES) {
            try {
                entries.add(classLocation(Class.forName(className)).toString());
            } catch (ClassNotFoundException failure) {
                throw new IOException("Cannot locate compiler dependency " + className, failure);
            }
        }
        return String.join(System.getProperty("path.separator"), entries);
    }

    private static String runtimeClasspath() throws IOException {
        return classLocation(KaitaiStruct.class).toString();
    }

    private static Path classLocation(Class<?> type) throws IOException {
        CodeSource source = type.getProtectionDomain().getCodeSource();
        if (source != null && source.getLocation() != null) {
            try {
                return Path.of(source.getLocation().toURI());
            } catch (java.net.URISyntaxException failure) {
                throw new IOException("Cannot locate dependency " + type.getName(), failure);
            }
        }

        String resourceName = "/" + type.getName().replace('.', '/') + ".class";
        URL resource = type.getResource(resourceName);
        if (resource == null) throw new IOException("Cannot locate dependency " + type.getName());
        try {
            if (resource.openConnection() instanceof JarURLConnection jar) {
                return Path.of(jar.getJarFileURL().toURI());
            }
            if (resource.getProtocol().equals("jar")) {
                String location = resource.toExternalForm();
                int separator = location.indexOf("!/");
                if (separator > 4) return Path.of(URI.create(location.substring(4, separator)));
            }
            if (resource.getProtocol().equals("file")) {
                Path classFile = Path.of(resource.toURI());
                int segments = type.getName().split("\\.").length;
                Path root = classFile;
                for (int index = 0; index < segments; index++) root = root.getParent();
                return root;
            }
        } catch (java.net.URISyntaxException failure) {
            throw new IOException("Cannot locate dependency " + type.getName(), failure);
        }
        throw new IOException("Unsupported dependency location for " + type.getName() + ": " + resource);
    }

    private static Path javaExecutable(String name) throws IOException {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        Path executable = Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : ""));
        if (!Files.isRegularFile(executable)) {
            throw new IOException(name.equals("javac")
                    ? KaitaiStructSupportBundle.message("runtime.error.javaCompiler")
                    : "Cannot locate the Java executable.");
        }
        return executable;
    }

    private static SchemaIdentity identity(Path template) throws IOException {
        MessageDigest digest;
        try { digest = MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
        Set<Path> visited = new LinkedHashSet<>();
        String rootId = hashSchema(template, digest, visited, true);
        digest.update(COMPILER_VERSION.getBytes(StandardCharsets.UTF_8));
        return new SchemaIdentity(HexFormat.of().formatHex(digest.digest()), rootId);
    }

    private static String hashSchema(Path schema, MessageDigest digest, Set<Path> visited,
                                     boolean root) throws IOException {
        Path normalized = schema.toAbsolutePath().normalize();
        if (!visited.add(normalized)) return null;
        byte[] source = Files.readAllBytes(normalized);
        digest.update(normalized.toString().getBytes(StandardCharsets.UTF_8));
        digest.update(source);
        Map<?, ?> document = loadYaml(source);
        Map<?, ?> meta = document.get("meta") instanceof Map<?, ?> value ? value : Map.of();
        String rootId = root ? String.valueOf(meta.get("id")) : null;
        Object imports = meta.get("imports");
        List<?> names = imports instanceof List<?> list ? list : imports == null ? List.of() : List.of(imports);
        List<Path> imported = new ArrayList<>();
        for (Object item : names) {
            String name = String.valueOf(item);
            Path path = Path.of(name);
            if (!path.isAbsolute()) path = normalized.getParent().resolve(path);
            if (!path.getFileName().toString().endsWith(".ksy")) path = Path.of(path + ".ksy");
            if (Files.isRegularFile(path)) imported.add(path.toAbsolutePath().normalize());
        }
        imported.sort(Comparator.comparing(Path::toString));
        for (Path path : imported) hashSchema(path, digest, visited, false);
        if (root && (rootId == null || rootId.equals("null") || rootId.isBlank()))
            throw new IOException("KSY meta.id is required.");
        return rootId;
    }

    private static Map<?, ?> loadYaml(byte[] source) throws IOException {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(50);
        options.setCodePointLimit(5_000_000);
        Object value;
        try {
            value = new Yaml(new SafeConstructor(options)).load(new String(source, StandardCharsets.UTF_8));
        } catch (RuntimeException failure) {
            throw new IOException(KaitaiStructSupportBundle.message("runtime.error.compile", failure.getMessage()), failure);
        }
        if (!(value instanceof Map<?, ?> map)) throw new IOException("The KSY document root must be a mapping.");
        return map;
    }

    private static String upperCamel(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean upper = true;
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '_') upper = true;
            else {
                result.append(upper ? Character.toUpperCase(character) : character);
                upper = false;
            }
        }
        return result.toString();
    }

    private static String formatDiagnostics(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        StringBuilder result = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
            result.append(System.lineSeparator());
            if (diagnostic.getSource() != null) {
                URI uri = diagnostic.getSource().toUri();
                result.append(Path.of(uri).getFileName()).append(':').append(diagnostic.getLineNumber()).append(": ");
            }
            result.append(diagnostic.getMessage(LocaleHolder.ROOT));
        }
        return result.toString();
    }

    private static String quoteArgument(String value) {
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }

    private static Path cacheRoot() {
        return Path.of(System.getProperty("java.io.tmpdir"), "idea-kaitai-struct-support",
                "ksc-" + COMPILER_VERSION);
    }

    private static void checkCanceled(BooleanSupplier canceled) {
        if (canceled.getAsBoolean()) throw new KaitaiStructCanceledException();
    }

    public record CompiledSchema(Class<?> rootClass, URLClassLoader classLoader, String fingerprint,
                                 String compilerOutput) implements AutoCloseable {
        @Override public void close() throws IOException { classLoader.close(); }
    }

    private record SchemaIdentity(String hash, String rootId) {}
    private static final class LocaleHolder { private static final java.util.Locale ROOT = java.util.Locale.ROOT; }

    public static final class KaitaiStructCanceledException extends RuntimeException {
        public KaitaiStructCanceledException() { super(KaitaiStructSupportBundle.message("runtime.error.canceled")); }
    }
}
