package cn.fj.loli.kaitaistructsupport.lexer;

import cn.fj.loli.kaitaistructsupport.KaitaiStructLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class KaitaiStructTokenType extends IElementType {
    public KaitaiStructTokenType(@NonNls @NotNull String debugName) {
        super(debugName, KaitaiStructLanguage.INSTANCE);
    }
}

