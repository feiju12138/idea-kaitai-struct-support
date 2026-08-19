package cn.fj.loli.kaitaistructsupport;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class KaitaiStructSupportBundle extends DynamicBundle {
    private static final String BUNDLE = "cn.fj.loli.kaitaistructsupport.KaitaiStructSupportBundle";
    private static final KaitaiStructSupportBundle INSTANCE = new KaitaiStructSupportBundle();

    private KaitaiStructSupportBundle() {
        super(BUNDLE);
    }

    public static @Nls @NotNull String message(
            @NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
            Object @NotNull ... params
    ) {
        return INSTANCE.getMessage(key, params);
    }
}

