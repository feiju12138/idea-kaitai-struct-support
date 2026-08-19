package cn.fj.loli.kaitaistructsupport.editor;

import cn.fj.loli.kaitaistructsupport.lexer.KaitaiStructTokenTypes;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class KaitaiStructBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = {
            new BracePair(KaitaiStructTokenTypes.LEFT_BRACKET, KaitaiStructTokenTypes.RIGHT_BRACKET, false),
            new BracePair(KaitaiStructTokenTypes.LEFT_BRACE, KaitaiStructTokenTypes.RIGHT_BRACE, false)
    };

    @Override public BracePair @NotNull [] getPairs() { return PAIRS; }
    @Override public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType,
                                                             @Nullable IElementType contextType) { return true; }
    @Override public int getCodeConstructStart(PsiFile file, int openingBraceOffset) { return openingBraceOffset; }
}

