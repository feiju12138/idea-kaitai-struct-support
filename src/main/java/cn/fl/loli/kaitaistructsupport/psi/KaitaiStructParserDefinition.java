package cn.fj.loli.kaitaistructsupport.psi;

import cn.fj.loli.kaitaistructsupport.KaitaiStructLanguage;
import cn.fj.loli.kaitaistructsupport.lexer.KaitaiStructLexer;
import cn.fj.loli.kaitaistructsupport.lexer.KaitaiStructTokenTypes;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/** Flat, error-free PSI used to host lightweight KSY editor features. */
public final class KaitaiStructParserDefinition implements ParserDefinition {
    private static final IFileElementType FILE = new IFileElementType(KaitaiStructLanguage.INSTANCE);
    private static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);

    @Override public @NotNull Lexer createLexer(Project project) { return new KaitaiStructLexer(); }
    @Override public @NotNull IFileElementType getFileNodeType() { return FILE; }
    @Override public @NotNull TokenSet getWhitespaceTokens() { return WHITE_SPACES; }
    @Override public @NotNull TokenSet getCommentTokens() { return KaitaiStructTokenTypes.COMMENTS; }
    @Override public @NotNull TokenSet getStringLiteralElements() { return KaitaiStructTokenTypes.STRINGS; }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return (root, builder) -> {
            PsiBuilder.Marker file = builder.mark();
            while (!builder.eof()) builder.advanceLexer();
            file.done(root);
            return builder.getTreeBuilt();
        };
    }

    @Override public @NotNull PsiElement createElement(ASTNode node) { return new ASTWrapperPsiElement(node); }
    @Override public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new KaitaiStructFile(viewProvider);
    }
}
