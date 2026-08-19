package cn.fj.loli.kaitaistructsupport.completion;

import cn.fj.loli.kaitaistructsupport.lang.KaitaiStructLanguageCatalog;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KaitaiStructCompletionContext {
    private static final Pattern MAPPING = Pattern.compile("^([A-Za-z_][A-Za-z0-9_-]*)\\s*:(.*)$");
    private static final Set<String> BOOLEAN_KEYS = Set.of(
            "size-eos", "include", "consume", "eos-error", "ks-debug", "ks-opaque-types", "in-enum"
    );
    private static final Set<String> EXPRESSION_KEYS = Set.of(
            "size", "if", "repeat-expr", "repeat-until", "pos", "io", "value", "switch-on"
    );

    private KaitaiStructCompletionContext() {}

    public static CompletionItems at(CharSequence document, int offset) {
        String text = document.subSequence(0, Math.max(0, Math.min(offset, document.length()))).toString();
        int lineStart = Math.max(text.lastIndexOf('\n'), text.lastIndexOf('\r')) + 1;
        String currentLine = text.substring(lineStart);
        int indent = indentation(currentLine);
        String currentContent = content(currentLine);
        Matcher currentMapping = MAPPING.matcher(currentContent);
        if (currentMapping.matches()) {
            String key = currentMapping.group(1);
            Deque<Entry> path = pathBefore(text.substring(0, lineStart));
            return new CompletionItems(valuesFor(key, text, path), false);
        }

        Deque<Entry> path = pathBefore(text.substring(0, lineStart));
        List<String> keys = keysFor(indent, currentContent.startsWith("-"), path);
        return new CompletionItems(keys, true);
    }

    private static List<String> valuesFor(String key, String text, Deque<Entry> path) {
        if (key.equals("type")) {
            LinkedHashSet<String> values = new LinkedHashSet<>(inside(path, "params")
                    ? KaitaiStructLanguageCatalog.PARAMETER_TYPES : KaitaiStructLanguageCatalog.PRIMITIVE_TYPES);
            values.addAll(declarations(text, "types"));
            return List.copyOf(values);
        }
        if (key.equals("enum")) return declarations(text, "enums");
        if (key.equals("endian") || key.equals("bit-endian")) return List.of("le", "be");
        if (key.equals("repeat")) return List.of("expr", "until", "eos");
        if (key.equals("process")) return List.of("xor()", "rol()", "ror()", "zlib");
        if (key.equals("encoding")) return List.of("UTF-8", "ASCII", "ISO-8859-1", "UTF-16LE", "UTF-16BE");
        if (BOOLEAN_KEYS.contains(key)) return List.of("true", "false");
        if (EXPRESSION_KEYS.contains(key)) {
            LinkedHashSet<String> values = new LinkedHashSet<>(KaitaiStructLanguageCatalog.EXPRESSION_SYMBOLS);
            values.addAll(fieldIds(text));
            return List.copyOf(values);
        }
        return List.of();
    }

    private static boolean inside(Deque<Entry> path, String section) {
        return path.stream().anyMatch(entry -> entry.key.equals(section));
    }

    private static List<String> keysFor(int indent, boolean listItem, Deque<Entry> path) {
        if (indent == 0 && !listItem) return KaitaiStructLanguageCatalog.ROOT_KEYS;
        for (Entry entry : path.reversed()) {
            switch (entry.key) {
                case "valid" -> { return KaitaiStructLanguageCatalog.VALID_KEYS; }
                case "xref" -> { return KaitaiStructLanguageCatalog.XREF_KEYS; }
                case "seq" -> { return KaitaiStructLanguageCatalog.FIELD_KEYS; }
                case "instances" -> { return KaitaiStructLanguageCatalog.INSTANCE_KEYS; }
                case "params" -> { return KaitaiStructLanguageCatalog.PARAM_KEYS; }
                case "meta" -> { return KaitaiStructLanguageCatalog.META_KEYS; }
                case "types" -> { return KaitaiStructLanguageCatalog.TYPE_KEYS; }
                default -> { }
            }
        }
        return KaitaiStructLanguageCatalog.ROOT_KEYS;
    }

    private static Deque<Entry> pathBefore(String text) {
        Deque<Entry> path = new ArrayDeque<>();
        for (String line : text.split("\\R", -1)) {
            String withoutComment = stripComment(line);
            if (withoutComment.isBlank()) continue;
            int indent = indentation(withoutComment);
            String value = content(withoutComment);
            if (value.startsWith("-")) {
                value = value.substring(1).stripLeading();
                indent += 2;
            }
            Matcher matcher = MAPPING.matcher(value);
            if (!matcher.matches()) continue;
            while (!path.isEmpty() && path.peekLast().indent >= indent) path.removeLast();
            path.addLast(new Entry(indent, matcher.group(1)));
        }
        return path;
    }

    private static List<String> declarations(String text, String section) {
        List<String> result = new ArrayList<>();
        int sectionIndent = -1;
        for (String line : text.split("\\R", -1)) {
            if (line.isBlank()) continue;
            int indent = indentation(line);
            Matcher matcher = MAPPING.matcher(content(stripComment(line)));
            if (!matcher.matches()) continue;
            String key = matcher.group(1);
            if (indent == 0) sectionIndent = key.equals(section) ? 0 : -1;
            else if (sectionIndent >= 0 && indent == sectionIndent + 2) result.add(key);
        }
        return List.copyOf(new LinkedHashSet<>(result));
    }

    private static List<String> fieldIds(String text) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        Pattern id = Pattern.compile("^\\s*(?:-\\s*)?id\\s*:\\s*([A-Za-z_][A-Za-z0-9_]*)");
        for (String line : text.split("\\R", -1)) {
            Matcher matcher = id.matcher(line);
            if (matcher.find()) result.add(matcher.group(1));
        }
        return List.copyOf(result);
    }

    private static String stripComment(String value) {
        boolean single = false;
        boolean doubleQuoted = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\'' && !doubleQuoted) single = !single;
            else if (c == '"' && !single && (i == 0 || value.charAt(i - 1) != '\\')) doubleQuoted = !doubleQuoted;
            else if (c == '#' && !single && !doubleQuoted) return value.substring(0, i);
        }
        return value;
    }

    private static int indentation(String value) {
        int result = 0;
        while (result < value.length() && value.charAt(result) == ' ') result++;
        return result;
    }

    private static String content(String value) {
        String stripped = value.stripLeading();
        return stripped.startsWith("- ") ? stripped.substring(2).stripLeading() : stripped;
    }

    public record CompletionItems(List<String> values, boolean keys) {
        public CompletionItems {
            values = List.copyOf(values);
        }
    }

    private record Entry(int indent, String key) {}
}
