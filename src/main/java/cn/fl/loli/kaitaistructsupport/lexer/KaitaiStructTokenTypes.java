package cn.fj.loli.kaitaistructsupport.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public final class KaitaiStructTokenTypes {
    public static final IElementType WHITE_SPACE = TokenType.WHITE_SPACE;
    public static final IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;
    public static final IElementType KEY = new KaitaiStructTokenType("KEY");
    public static final IElementType TYPE = new KaitaiStructTokenType("TYPE");
    public static final IElementType BOOLEAN = new KaitaiStructTokenType("BOOLEAN");
    public static final IElementType NUMBER = new KaitaiStructTokenType("NUMBER");
    public static final IElementType STRING = new KaitaiStructTokenType("STRING");
    public static final IElementType IDENTIFIER = new KaitaiStructTokenType("IDENTIFIER");
    public static final IElementType COMMENT = new KaitaiStructTokenType("COMMENT");
    public static final IElementType ANCHOR = new KaitaiStructTokenType("ANCHOR");
    public static final IElementType COLON = new KaitaiStructTokenType("COLON");
    public static final IElementType DASH = new KaitaiStructTokenType("DASH");
    public static final IElementType COMMA = new KaitaiStructTokenType("COMMA");
    public static final IElementType OPERATOR = new KaitaiStructTokenType("OPERATOR");
    public static final IElementType LEFT_BRACKET = new KaitaiStructTokenType("LEFT_BRACKET");
    public static final IElementType RIGHT_BRACKET = new KaitaiStructTokenType("RIGHT_BRACKET");
    public static final IElementType LEFT_BRACE = new KaitaiStructTokenType("LEFT_BRACE");
    public static final IElementType RIGHT_BRACE = new KaitaiStructTokenType("RIGHT_BRACE");

    public static final TokenSet COMMENTS = TokenSet.create(COMMENT);
    public static final TokenSet STRINGS = TokenSet.create(STRING);

    private KaitaiStructTokenTypes() {}
}

