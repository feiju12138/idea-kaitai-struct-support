package cn.fj.loli.kaitaistructsupport.completion;

import cn.fj.loli.kaitaistructsupport.KaitaiStructLanguage;
import cn.fj.loli.kaitaistructsupport.KaitaiStructSupportBundle;
import cn.fj.loli.kaitaistructsupport.lang.KaitaiStructLanguageCatalog;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public final class KaitaiStructCompletionContributor extends CompletionContributor {
    public KaitaiStructCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().inFile(PlatformPatterns.psiFile().withLanguage(KaitaiStructLanguage.INSTANCE)),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        KaitaiStructCompletionContext.CompletionItems items = KaitaiStructCompletionContext.at(
                                parameters.getEditor().getDocument().getCharsSequence(), parameters.getOffset());
                        for (String value : items.values()) {
                            LookupElementBuilder builder = LookupElementBuilder.create(value);
                            double priority;
                            if (items.keys()) {
                                builder = builder.withPresentableText(value + ":")
                                        .withTypeText(KaitaiStructSupportBundle.message("completion.kind.key"), true)
                                        .withInsertHandler(KaitaiStructCompletionContributor::insertKeyColon);
                                priority = 100;
                            } else if (KaitaiStructLanguageCatalog.isBuiltinType(value)) {
                                builder = builder.withTypeText(KaitaiStructSupportBundle.message("completion.kind.type"), true);
                                priority = 95;
                            } else {
                                builder = builder.withTypeText(KaitaiStructSupportBundle.message("completion.kind.value"), true);
                                priority = 80;
                            }
                            result.addElement(PrioritizedLookupElement.withPriority(builder, priority));
                        }
                    }
                });
    }

    private static void insertKeyColon(@NotNull InsertionContext context, @NotNull LookupElement item) {
        int offset = context.getTailOffset();
        CharSequence chars = context.getDocument().getCharsSequence();
        if (offset < chars.length() && chars.charAt(offset) == ':') return;
        context.getDocument().insertString(offset, ": ");
        context.getEditor().getCaretModel().moveToOffset(offset + 2);
    }
}
