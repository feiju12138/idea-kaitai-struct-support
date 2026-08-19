package cn.fj.loli.kaitaistructsupport.highlighting;

import cn.fj.loli.kaitaistructsupport.KaitaiStructFileType;
import cn.fj.loli.kaitaistructsupport.KaitaiStructSupportBundle;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Map;

public final class KaitaiStructColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = {
            descriptor("color.key", KaitaiStructSyntaxHighlighter.KEY),
            descriptor("color.type", KaitaiStructSyntaxHighlighter.TYPE),
            descriptor("color.boolean", KaitaiStructSyntaxHighlighter.BOOLEAN),
            descriptor("color.number", KaitaiStructSyntaxHighlighter.NUMBER),
            descriptor("color.string", KaitaiStructSyntaxHighlighter.STRING),
            descriptor("color.comment", KaitaiStructSyntaxHighlighter.COMMENT),
            descriptor("color.anchor", KaitaiStructSyntaxHighlighter.ANCHOR),
            descriptor("color.operator", KaitaiStructSyntaxHighlighter.OPERATOR),
            descriptor("color.brackets", KaitaiStructSyntaxHighlighter.BRACKETS)
    };

    @Override public @Nullable Icon getIcon() { return KaitaiStructFileType.INSTANCE.getIcon(); }
    @Override public @NotNull SyntaxHighlighter getHighlighter() { return new KaitaiStructSyntaxHighlighter(); }
    @Override public AttributesDescriptor @NotNull [] getAttributeDescriptors() { return DESCRIPTORS; }
    @Override public ColorDescriptor @NotNull [] getColorDescriptors() { return ColorDescriptor.EMPTY_ARRAY; }
    @Override public @NotNull String getDisplayName() { return KaitaiStructSupportBundle.message("settings.colors.displayName"); }
    @Override public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() { return null; }

    @Override
    public @NotNull String getDemoText() {
        return """
                meta:
                  id: png
                  file-extension: png
                  endian: be
                seq:
                  - id: signature
                    contents: [0x89, 0x50, 0x4e, 0x47]
                  - id: length
                    type: u4
                  - id: chunk
                    type: chunk_body
                    if: length > 0
                types:
                  chunk_body:
                    seq:
                      - id: data
                        size: _parent.length
                """;
    }

    private static AttributesDescriptor descriptor(String key, TextAttributesKey attributesKey) {
        return new AttributesDescriptor(KaitaiStructSupportBundle.message(key), attributesKey);
    }
}

