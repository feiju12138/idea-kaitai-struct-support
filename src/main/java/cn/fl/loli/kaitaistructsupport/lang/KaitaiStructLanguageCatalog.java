package cn.fj.loli.kaitaistructsupport.lang;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class KaitaiStructLanguageCatalog {
    public static final List<String> ROOT_KEYS = List.of(
            "meta", "doc", "doc-ref", "to-string", "params", "seq", "types", "enums", "instances"
    );
    public static final List<String> META_KEYS = List.of(
            "id", "title", "application", "file-extension", "xref", "license", "ks-version",
            "ks-debug", "ks-opaque-types", "imports", "encoding", "endian", "bit-endian", "tags"
    );
    public static final List<String> FIELD_KEYS = List.of(
            "id", "type", "doc", "doc-ref", "contents", "size", "size-eos", "terminator",
            "include", "consume", "eos-error", "encoding", "pad-right", "process", "enum", "if",
            "repeat", "repeat-expr", "repeat-until", "valid"
    );
    public static final List<String> INSTANCE_KEYS = List.of(
            "id", "type", "doc", "doc-ref", "contents", "size", "size-eos", "terminator",
            "include", "consume", "eos-error", "encoding", "pad-right", "process", "enum", "if",
            "repeat", "repeat-expr", "repeat-until", "valid", "pos", "io", "value"
    );
    public static final List<String> PARAM_KEYS = List.of("id", "type", "enum", "doc", "doc-ref");
    public static final List<String> TYPE_KEYS = List.of(
            "meta", "doc", "doc-ref", "to-string", "params", "seq", "instances", "types", "enums"
    );
    public static final List<String> VALID_KEYS = List.of(
            "eq", "min", "max", "range", "any-of", "in-enum", "expr"
    );
    public static final List<String> XREF_KEYS = List.of(
            "forensicswiki", "iso", "justsolve", "loc", "mime", "pronom", "rfc", "wikidata"
    );
    public static final List<String> PRIMITIVE_TYPES = List.of(
            "u1", "u2", "u2le", "u2be", "u4", "u4le", "u4be", "u8", "u8le", "u8be",
            "s1", "s2", "s2le", "s2be", "s4", "s4le", "s4be", "s8", "s8le", "s8be",
            "f4", "f4le", "f4be", "f8", "f8le", "f8be", "b1", "b1le", "b1be", "str", "strz"
    );
    public static final List<String> PARAMETER_TYPES = List.of(
            "u1", "u2", "u4", "u8", "s1", "s2", "s4", "s8", "b1", "f4", "f8",
            "bytes", "str", "bool", "struct", "io", "any"
    );
    public static final List<String> EXPRESSION_SYMBOLS = List.of(
            "true", "false", "_io", "_root", "_parent", "_index", "_is_le"
    );

    private static final Set<String> BUILTIN_TYPE_SET = Set.copyOf(PRIMITIVE_TYPES);
    private static final Set<String> KNOWN_KEY_SET = keys();

    private KaitaiStructLanguageCatalog() {}

    public static boolean isBuiltinType(String value) {
        if (BUILTIN_TYPE_SET.contains(value)) return true;
        return value.matches("b\\d+(le|be)?");
    }

    public static boolean isKnownKey(String value) {
        return KNOWN_KEY_SET.contains(value);
    }

    private static Set<String> keys() {
        Set<String> result = new LinkedHashSet<>();
        for (List<String> values : List.of(ROOT_KEYS, META_KEYS, FIELD_KEYS, INSTANCE_KEYS,
                PARAM_KEYS, TYPE_KEYS, VALID_KEYS, XREF_KEYS)) {
            result.addAll(values);
        }
        return Set.copyOf(result);
    }
}
