package cn.fj.loli.kaitaistructsupport.lexer;

import cn.fj.loli.kaitaistructsupport.lang.KaitaiStructLanguageCatalog;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class KaitaiStructLexer extends LexerBase {
    private CharSequence buffer = "";
    private int endOffset;
    private int tokenStart;
    private int tokenEnd;
    private IElementType tokenType;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.endOffset = endOffset;
        tokenStart = startOffset;
        locateToken();
    }

    @Override public int getState() { return 0; }
    @Override public @Nullable IElementType getTokenType() { return tokenType; }
    @Override public int getTokenStart() { return tokenStart; }
    @Override public int getTokenEnd() { return tokenEnd; }
    @Override public @NotNull CharSequence getBufferSequence() { return buffer; }
    @Override public int getBufferEnd() { return endOffset; }

    @Override
    public void advance() {
        tokenStart = tokenEnd;
        locateToken();
    }

    private void locateToken() {
        if (tokenStart >= endOffset) {
            tokenEnd = tokenStart;
            tokenType = null;
            return;
        }
        char current = charAt(tokenStart);
        if (Character.isWhitespace(current)) {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < endOffset && Character.isWhitespace(charAt(tokenEnd))) tokenEnd++;
            tokenType = KaitaiStructTokenTypes.WHITE_SPACE;
            return;
        }
        if (current == '#') {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < endOffset && charAt(tokenEnd) != '\n' && charAt(tokenEnd) != '\r') tokenEnd++;
            tokenType = KaitaiStructTokenTypes.COMMENT;
            return;
        }
        if (current == '\'' || current == '"') {
            tokenEnd = scanQuoted(tokenStart + 1, current);
            tokenType = KaitaiStructTokenTypes.STRING;
            return;
        }
        if ((current == '&' || current == '*') && isWordStart(peek(tokenStart + 1))) {
            tokenEnd = tokenStart + 2;
            while (tokenEnd < endOffset && isWordPart(charAt(tokenEnd))) tokenEnd++;
            tokenType = KaitaiStructTokenTypes.ANCHOR;
            return;
        }
        if (Character.isDigit(current) || ((current == '-' || current == '+') && Character.isDigit(peek(tokenStart + 1)))) {
            tokenEnd = scanNumber(tokenStart);
            tokenType = KaitaiStructTokenTypes.NUMBER;
            return;
        }
        if (isWordStart(current)) {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < endOffset && isWordPart(charAt(tokenEnd))) tokenEnd++;
            String word = buffer.subSequence(tokenStart, tokenEnd).toString();
            int following = tokenEnd;
            while (following < endOffset && (charAt(following) == ' ' || charAt(following) == '\t')) following++;
            if (following < endOffset && charAt(following) == ':') tokenType = KaitaiStructTokenTypes.KEY;
            else if (KaitaiStructLanguageCatalog.isBuiltinType(word)) tokenType = KaitaiStructTokenTypes.TYPE;
            else if (word.equals("true") || word.equals("false") || word.equals("null")) tokenType = KaitaiStructTokenTypes.BOOLEAN;
            else tokenType = KaitaiStructTokenTypes.IDENTIFIER;
            return;
        }

        tokenEnd = tokenStart + 1;
        tokenType = switch (current) {
            case ':' -> KaitaiStructTokenTypes.COLON;
            case '-' -> KaitaiStructTokenTypes.DASH;
            case ',' -> KaitaiStructTokenTypes.COMMA;
            case '[' -> KaitaiStructTokenTypes.LEFT_BRACKET;
            case ']' -> KaitaiStructTokenTypes.RIGHT_BRACKET;
            case '{' -> KaitaiStructTokenTypes.LEFT_BRACE;
            case '}' -> KaitaiStructTokenTypes.RIGHT_BRACE;
            case '|', '>', '?', '!', '%', '@', '`', '.', '+', '/', '=', '<', '(', ')' -> KaitaiStructTokenTypes.OPERATOR;
            default -> KaitaiStructTokenTypes.BAD_CHARACTER;
        };
    }

    private int scanQuoted(int from, char quote) {
        int index = from;
        boolean escaped = false;
        while (index < endOffset) {
            char value = charAt(index++);
            if (quote == '"' && !escaped && value == '\\') escaped = true;
            else if (!escaped && value == quote) break;
            else if (value == '\n' || value == '\r') break;
            else escaped = false;
        }
        return index;
    }

    private int scanNumber(int from) {
        int index = from;
        if (peek(index) == '+' || peek(index) == '-') index++;
        if (peek(index) == '0' && (peek(index + 1) == 'x' || peek(index + 1) == 'X'
                || peek(index + 1) == 'b' || peek(index + 1) == 'B'
                || peek(index + 1) == 'o' || peek(index + 1) == 'O')) index += 2;
        while (index < endOffset && (Character.isLetterOrDigit(charAt(index)) || charAt(index) == '_'
                || charAt(index) == '.')) index++;
        return index;
    }

    private char charAt(int offset) { return buffer.charAt(offset); }
    private char peek(int offset) { return offset >= 0 && offset < endOffset ? charAt(offset) : '\0'; }
    private static boolean isWordStart(char value) { return Character.isLetter(value) || value == '_'; }
    private static boolean isWordPart(char value) {
        return Character.isLetterOrDigit(value) || value == '_' || value == '-';
    }
}

