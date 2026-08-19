package cn.fj.loli.kaitaistructsupport.runtime;

import cn.fj.loli.hexsupport.structure.StructureDiagnostic;
import cn.fj.loli.hexsupport.structure.StructureNode;
import cn.fj.loli.kaitaistructsupport.KaitaiStructSupportBundle;
import io.kaitai.struct.KaitaiStruct;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

final class GeneratedStructureMapper {
    private static final int MAX_NODES = 100_000;

    private final BooleanSupplier canceled;
    private final IdentityHashMap<KaitaiStruct, Boolean> visited = new IdentityHashMap<>();
    private final List<StructureDiagnostic> diagnostics = new ArrayList<>();
    private int nodeCount;
    private boolean limitReported;

    GeneratedStructureMapper(BooleanSupplier canceled) {
        this.canceled = canceled;
    }

    MappingResult map(KaitaiStruct root) throws ReflectiveOperationException {
        List<StructureNode> nodes = mapObject(root, 0);
        return new MappingResult(nodes, diagnostics);
    }

    private List<StructureNode> mapObject(KaitaiStruct object, long streamBase)
            throws ReflectiveOperationException {
        checkCanceled();
        if (visited.put(object, Boolean.TRUE) != null) return List.of();
        Map<String, Integer> starts = integerMap(object, "_attrStart");
        Map<String, Integer> ends = integerMap(object, "_attrEnd");
        Map<String, List<Integer>> arrayStarts = integerListMap(object, "_arrStart");
        Map<String, List<Integer>> arrayEnds = integerListMap(object, "_arrEnd");
        Set<String> names = fieldNames(object, starts);
        List<StructureNode> result = new ArrayList<>();
        for (String name : names) {
            if (!acceptNode()) break;
            checkCanceled();
            Method getter = getter(object.getClass(), name);
            if (getter == null) continue;
            Object value;
            try {
                value = getter.invoke(object);
            } catch (InvocationTargetException failure) {
                Throwable cause = failure.getCause();
                if (cause instanceof RuntimeException runtime) throw runtime;
                throw failure;
            }
            long start = streamBase + starts.getOrDefault(name, 0);
            long end = streamBase + ends.getOrDefault(name, starts.getOrDefault(name, 0));
            result.add(mapField(object, name, value, start, end, streamBase,
                    arrayStarts.get(name), arrayEnds.get(name)));
        }
        visited.remove(object);
        return List.copyOf(result);
    }

    private StructureNode mapField(KaitaiStruct owner, String generatedName, Object value,
                                   long start, long end, long streamBase,
                                   List<Integer> itemStarts, List<Integer> itemEnds)
            throws ReflectiveOperationException {
        String name = snakeCase(generatedName);
        long size = Math.max(0, end - start);
        if (value instanceof List<?> list) {
            List<StructureNode> children = new ArrayList<>();
            for (int index = 0; index < list.size(); index++) {
                if (!acceptNode()) break;
                long itemStart = itemStarts != null && index < itemStarts.size()
                        ? streamBase + itemStarts.get(index) : start;
                long itemEnd = itemEnds != null && index < itemEnds.size()
                        ? streamBase + itemEnds.get(index) : itemStart;
                children.add(mapValue(owner, "[" + index + "]", list.get(index), itemStart, itemEnd, streamBase));
            }
            return node(name, "array", list.size() + " entries", start, size, children);
        }
        return mapValue(owner, name, value, start, end, streamBase);
    }

    private StructureNode mapValue(KaitaiStruct owner, String name, Object value,
                                   long start, long end, long streamBase)
            throws ReflectiveOperationException {
        long size = Math.max(0, end - start);
        if (value instanceof KaitaiStruct nested) {
            boolean sameStream = nested._io() == owner._io();
            long nestedBase = sameStream ? streamBase : start;
            List<StructureNode> children = mapObject(nested, nestedBase);
            return node(name, nested.getClass().getSimpleName(), children.size() + " fields",
                    start, size, children);
        }
        return node(name, typeName(value), format(value), start, size, List.of());
    }

    private Set<String> fieldNames(KaitaiStruct object, Map<String, Integer> starts)
            throws ReflectiveOperationException {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        try {
            Field sequence = object.getClass().getField("_seqFields");
            names.addAll(Arrays.asList((String[]) sequence.get(null)));
        } catch (NoSuchFieldException ignored) { }
        names.addAll(starts.keySet());
        Arrays.stream(object.getClass().getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.getParameterCount() == 0 && method.getReturnType() != Void.TYPE)
                .map(Method::getName)
                .filter(name -> !name.startsWith("_"))
                .sorted()
                .forEach(names::add);
        return names;
    }

    private static Method getter(Class<?> type, String name) {
        try { return type.getMethod(name); }
        catch (NoSuchMethodException ignored) { return null; }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Integer> integerMap(Object object, String field)
            throws ReflectiveOperationException {
        try { return (Map<String, Integer>) object.getClass().getField(field).get(object); }
        catch (NoSuchFieldException ignored) { return Map.of(); }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<Integer>> integerListMap(Object object, String field)
            throws ReflectiveOperationException {
        try { return (Map<String, List<Integer>>) object.getClass().getField(field).get(object); }
        catch (NoSuchFieldException ignored) { return Map.of(); }
    }

    private boolean acceptNode() {
        if (nodeCount++ < MAX_NODES) return true;
        if (!limitReported) {
            diagnostics.add(new StructureDiagnostic(StructureDiagnostic.Severity.WARNING, 0, 0,
                    KaitaiStructSupportBundle.message("runtime.warning.nodeLimit", MAX_NODES)));
            limitReported = true;
        }
        return false;
    }

    private void checkCanceled() {
        if (canceled.getAsBoolean()) throw new KaitaiStructCompiler.KaitaiStructCanceledException();
    }

    private static StructureNode node(String name, String type, String value, long offset, long size,
                                      List<StructureNode> children) {
        return new StructureNode(name, type, value, offset, size, type, null, null, null, children);
    }

    private static String typeName(Object value) {
        if (value == null) return "null";
        if (value instanceof byte[]) return "bytes";
        if (value.getClass().isEnum()) return value.getClass().getSimpleName();
        return switch (value) {
            case Byte ignored -> "s1";
            case Short ignored -> "integer";
            case Integer ignored -> "integer";
            case Long ignored -> "integer";
            case Float ignored -> "f4";
            case Double ignored -> "f8";
            case Boolean ignored -> "bool";
            case String ignored -> "str";
            default -> value.getClass().getSimpleName();
        };
    }

    private static String format(Object value) {
        if (value == null) return "null";
        if (value instanceof byte[] bytes) {
            int shown = Math.min(bytes.length, 64);
            StringBuilder result = new StringBuilder(shown * 3);
            for (int index = 0; index < shown; index++) {
                if (index > 0) result.append(' ');
                result.append(String.format("%02X", bytes[index] & 0xff));
            }
            if (shown < bytes.length) result.append(" ...");
            return result.toString();
        }
        String result = String.valueOf(value);
        return result.length() <= 1024 ? result : result.substring(0, 1021) + "...";
    }

    private static String snakeCase(String value) {
        StringBuilder result = new StringBuilder(value.length() + 4);
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (Character.isUpperCase(character)) {
                if (index > 0) result.append('_');
                result.append(Character.toLowerCase(character));
            } else result.append(character);
        }
        return result.toString();
    }

    record MappingResult(List<StructureNode> nodes, List<StructureDiagnostic> diagnostics) {
        MappingResult {
            nodes = List.copyOf(nodes);
            diagnostics = List.copyOf(diagnostics);
        }
    }
}
