package cn.fj.loli.kaitaistructsupport;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public final class KaitaiStructFileType extends LanguageFileType {
    public static final KaitaiStructFileType INSTANCE = new KaitaiStructFileType();
    private static final Icon ICON = IconLoader.getIcon("/icons/ksyFile.svg", KaitaiStructFileType.class);

    private KaitaiStructFileType() {
        super(KaitaiStructLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "Kaitai Struct YAML";
    }

    @Override
    public @Nls @NotNull String getDescription() {
        return KaitaiStructSupportBundle.message("fileType.description");
    }

    @Override
    public @NonNls @NotNull String getDefaultExtension() {
        return "ksy";
    }

    @Override
    public @NotNull Icon getIcon() {
        return ICON;
    }
}

