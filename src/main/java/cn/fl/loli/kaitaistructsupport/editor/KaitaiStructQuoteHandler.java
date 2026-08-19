package cn.fj.loli.kaitaistructsupport.editor;

import cn.fj.loli.kaitaistructsupport.lexer.KaitaiStructTokenTypes;
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;

public final class KaitaiStructQuoteHandler extends SimpleTokenSetQuoteHandler {
    public KaitaiStructQuoteHandler() {
        super(KaitaiStructTokenTypes.STRINGS);
    }
}
