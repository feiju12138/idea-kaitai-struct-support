package cn.fj.loli.kaitaistructsupport.psi;

import cn.fj.loli.kaitaistructsupport.KaitaiStructLanguage;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import cn.fj.loli.kaitaistructsupport.KaitaiStructFileType;
import org.jetbrains.annotations.NotNull;

public final class KaitaiStructFile extends PsiFileBase {
    public KaitaiStructFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, KaitaiStructLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return KaitaiStructFileType.INSTANCE;
    }
}

