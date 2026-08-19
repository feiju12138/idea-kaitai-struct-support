package cn.fj.loli.kaitaistructsupport.integration;

import cn.fj.loli.hexsupport.structure.BinarySnapshot;
import cn.fj.loli.hexsupport.structure.BinaryStructureProvider;
import cn.fj.loli.hexsupport.structure.StructureAnalysisResult;
import cn.fj.loli.kaitaistructsupport.KaitaiStructSupportBundle;
import cn.fj.loli.kaitaistructsupport.runtime.KaitaiStructStructureEngine;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class HexKaitaiStructStructureProvider implements BinaryStructureProvider {
    @Override
    public @NotNull String id() {
        return "kaitai-struct-yaml";
    }

    @Override
    public @NotNull String displayName() {
        return KaitaiStructSupportBundle.message("provider.displayName");
    }

    @Override
    public @NotNull Collection<String> templateExtensions() {
        return List.of("ksy");
    }

    @Override
    public @NotNull StructureAnalysisResult analyze(@NotNull Path template,
                                                    @NotNull BinarySnapshot input,
                                                    @NotNull BooleanSupplier canceled) {
        return new KaitaiStructStructureEngine().analyze(template, input, canceled);
    }
}
