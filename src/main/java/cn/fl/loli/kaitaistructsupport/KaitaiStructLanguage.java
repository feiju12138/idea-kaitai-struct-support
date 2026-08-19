package cn.fj.loli.kaitaistructsupport;

import com.intellij.lang.Language;

public final class KaitaiStructLanguage extends Language {
    public static final KaitaiStructLanguage INSTANCE = new KaitaiStructLanguage();

    private KaitaiStructLanguage() {
        super("KaitaiStruct");
    }
}

