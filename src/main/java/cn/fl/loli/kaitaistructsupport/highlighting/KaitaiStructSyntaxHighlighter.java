package cn.fj.loli.kaitaistructsupport.highlighting;

import cn.fj.loli.kaitaistructsupport.lexer.KaitaiStructLexer;
import cn.fj.loli.kaitaistructsupport.lexer.KaitaiStructTokenTypes;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public final class KaitaiStructSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey KEY = key("KSY_KEY", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey TYPE = key("KSY_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey BOOLEAN = key("KSY_BOOLEAN", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey NUMBER = key("KSY_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING = key("KSY_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey COMMENT = key("KSY_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey ANCHOR = key("KSY_ANCHOR", DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey OPERATOR = key("KSY_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey BRACKETS = key("KSY_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey BAD_CHARACTER = key("KSY_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    private static final TextAttributesKey[] EMPTY = TextAttributesKey.EMPTY_ARRAY;

    @Override public @NotNull Lexer getHighlightingLexer() { return new KaitaiStructLexer(); }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType == KaitaiStructTokenTypes.KEY) return pack(KEY);
        if (tokenType == KaitaiStructTokenTypes.TYPE) return pack(TYPE);
        if (tokenType == KaitaiStructTokenTypes.BOOLEAN) return pack(BOOLEAN);
        if (tokenType == KaitaiStructTokenTypes.NUMBER) return pack(NUMBER);
        if (tokenType == KaitaiStructTokenTypes.STRING) return pack(STRING);
        if (tokenType == KaitaiStructTokenTypes.COMMENT) return pack(COMMENT);
        if (tokenType == KaitaiStructTokenTypes.ANCHOR) return pack(ANCHOR);
        if (tokenType == KaitaiStructTokenTypes.LEFT_BRACKET || tokenType == KaitaiStructTokenTypes.RIGHT_BRACKET
                || tokenType == KaitaiStructTokenTypes.LEFT_BRACE || tokenType == KaitaiStructTokenTypes.RIGHT_BRACE) return pack(BRACKETS);
        if (tokenType == KaitaiStructTokenTypes.COLON || tokenType == KaitaiStructTokenTypes.DASH
                || tokenType == KaitaiStructTokenTypes.COMMA || tokenType == KaitaiStructTokenTypes.OPERATOR) return pack(OPERATOR);
        if (tokenType == KaitaiStructTokenTypes.BAD_CHARACTER) return pack(BAD_CHARACTER);
        return EMPTY;
    }

    private static TextAttributesKey key(String externalName, TextAttributesKey fallback) {
        return TextAttributesKey.createTextAttributesKey(externalName, fallback);
    }
}

