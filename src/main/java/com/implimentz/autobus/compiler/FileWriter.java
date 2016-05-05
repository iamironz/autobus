// Copyright 2013 Square, Inc.
package com.implimentz.autobus.compiler;

import javax.lang.model.element.Modifier;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.lang.model.element.Modifier.ABSTRACT;

class FileWriter implements Closeable {
    private static final Pattern TYPE_TRAILER = Pattern.compile("(.*?)(\\.\\.\\.|(?:\\[\\])+)$");
    private static final Pattern TYPE_PATTERN = Pattern.compile("(?:[\\w$]+\\.)*([\\w\\.*$]+)");
    private static final int MAX_SINGLE_LINE_ATTRIBUTES = 3;
    private static final String INDENT = "  ";

    private final Map<String, String> importedTypes = new LinkedHashMap<>();

    private String packagePrefix;
    private final Deque<Scope> scopes = new ArrayDeque<>();
    private final Deque<String> types = new ArrayDeque<>();
    private final Writer out;
    private boolean isCompressingTypes = true;
    private String indent = INDENT;

    FileWriter(Writer out) {
        this.out = out;
    }

    public void setCompressingTypes(boolean isCompressingTypes) {
        this.isCompressingTypes = isCompressingTypes;
    }

    public boolean isCompressingTypes() {
        return isCompressingTypes;
    }

    public void setIndent(String indent) {
        this.indent = indent;
    }

    public String getIndent() {
        return indent;
    }

    public FileWriter emitPackage(String packageName) throws IOException {
        if (this.packagePrefix != null) {
            throw new IllegalStateException();
        }
        if (packageName.isEmpty()) {
            this.packagePrefix = "";
        } else {
            out.write("package ");
            out.write(packageName);
            out.write(";\n\n");
            this.packagePrefix = packageName + ".";
        }
        return this;
    }

    public FileWriter emitImports(String... types) throws IOException {
        return emitImports(Arrays.asList(types));
    }

    public FileWriter emitImports(Class<?>... types) throws IOException {
        List<String> classNames = new ArrayList<String>(types.length);
        for (Class<?> classToImport : types) {
            classNames.add(classToImport.getCanonicalName());
        }
        return emitImports(classNames);
    }

    public FileWriter emitImports(Collection<String> types) throws IOException {
        for (String type : new TreeSet<String>(types)) {
            Matcher matcher = TYPE_PATTERN.matcher(type);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(type);
            }
            if (importedTypes.put(type, matcher.group(1)) != null) {
                throw new IllegalArgumentException(type);
            }
            out.write("import ");
            out.write(type);
            out.write(";\n");
        }
        return this;
    }

    public FileWriter emitStaticImports(String... types) throws IOException {
        return emitStaticImports(Arrays.asList(types));
    }

    public FileWriter emitStaticImports(Collection<String> types) throws IOException {
        for (String type : new TreeSet<>(types)) {
            Matcher matcher = TYPE_PATTERN.matcher(type);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(type);
            }
            if (importedTypes.put(type, matcher.group(1)) != null) {
                throw new IllegalArgumentException(type);
            }
            out.write("import static ");
            out.write(type);
            out.write(";\n");
        }
        return this;
    }

    private FileWriter emitCompressedType(String type) throws IOException {
        if (isCompressingTypes) {
            out.write(compressType(type));
        } else {
            out.write(type);
        }
        return this;
    }

    public String compressType(String type) {
        Matcher trailer = TYPE_TRAILER.matcher(type);
        if (trailer.matches()) {
            type = trailer.group(1);
        }

        StringBuilder sb = new StringBuilder();
        if (this.packagePrefix == null) {
            throw new IllegalStateException();
        }

        Matcher m = TYPE_PATTERN.matcher(type);
        int pos = 0;
        while (true) {
            boolean found = m.find(pos);

            // Copy non-matching characters like "<".
            int typeStart = found ? m.start() : type.length();
            sb.append(type, pos, typeStart);

            if (!found) {
                break;
            }

            // Copy a single class name, shortening it if possible.
            String name = m.group(0);
            String imported = importedTypes.get(name);
            if (imported != null) {
                sb.append(imported);
            } else if (isClassInPackage(name, packagePrefix)) {
                String compressed = name.substring(packagePrefix.length());
                if (isAmbiguous(compressed)) {
                    sb.append(name);
                } else {
                    sb.append(compressed);
                }
            } else if (isClassInPackage(name, "java.lang.")) {
                sb.append(name.substring("java.lang.".length()));
            } else {
                sb.append(name);
            }
            pos = m.end();
        }

        if (trailer.matches()) {
            sb.append(trailer.group(2));
        }
        return sb.toString();
    }

    private static boolean isClassInPackage(String name, String packagePrefix) {
        if (name.startsWith(packagePrefix)) {
            if (name.indexOf('.', packagePrefix.length()) == -1) {
                return true;
            }
            // check to see if the part after the package looks like a class
            if (Character.isUpperCase(name.charAt(packagePrefix.length()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isAmbiguous(String compressed) {
        return importedTypes.values().contains(compressed);
    }

    public FileWriter beginInitializer(boolean isStatic) throws IOException {
        indent();
        if (isStatic) {
            out.write("static");
            out.write(" {\n");
        } else {
            out.write("{\n");
        }
        scopes.push(Scope.INITIALIZER);
        return this;
    }

    public FileWriter endInitializer() throws IOException {
        popScope(Scope.INITIALIZER);
        indent();
        out.write("}\n");
        return this;
    }

    public FileWriter beginType(String type, String kind) throws IOException {
        return beginType(type, kind, EnumSet.noneOf(Modifier.class), null);
    }

    public FileWriter beginType(String type, String kind, Set<Modifier> modifiers)
            throws IOException {
        return beginType(type, kind, modifiers, null);
    }

    public FileWriter beginType(String type, String kind, Set<Modifier> modifiers, String extendsType,
                                String... implementsTypes) throws IOException {
        indent();
        emitModifiers(modifiers);
        out.write(kind);
        out.write(" ");
        emitCompressedType(type);
        if (extendsType != null) {
            out.write(" extends ");
            emitCompressedType(extendsType);
        }
        if (implementsTypes.length > 0) {
            out.write("\n");
            indent();
            out.write("    implements ");
            for (int i = 0; i < implementsTypes.length; i++) {
                if (i != 0) {
                    out.write(", ");
                }
                emitCompressedType(implementsTypes[i]);
            }
        }
        out.write(" {\n");
        scopes.push("interface".equals(kind) ? Scope.INTERFACE_DECLARATION : Scope.TYPE_DECLARATION);
        types.push(type);
        return this;
    }

    public FileWriter endType() throws IOException {
        popScope(Scope.TYPE_DECLARATION, Scope.INTERFACE_DECLARATION);
        types.pop();
        indent();
        out.write("}\n");
        return this;
    }

    public FileWriter emitField(String type, String name) throws IOException {
        return emitField(type, name, EnumSet.noneOf(Modifier.class), null);
    }

    public FileWriter emitField(String type, String name, Set<Modifier> modifiers)
            throws IOException {
        return emitField(type, name, modifiers, null);
    }

    public FileWriter emitField(String type, String name, Set<Modifier> modifiers,
                                String initialValue) throws IOException {
        indent();
        emitModifiers(modifiers);
        emitCompressedType(type);
        out.write(" ");
        out.write(name);

        if (initialValue != null) {
            out.write(" =");
            if (!initialValue.startsWith("\n")) {
                out.write(" ");
            }

            String[] lines = initialValue.split("\n", -1);
            out.write(lines[0]);
            for (int i = 1; i < lines.length; i++) {
                out.write("\n");
                hangingIndent();
                out.write(lines[i]);
            }
        }
        out.write(";\n");
        return this;
    }

    public FileWriter beginMethod(String returnType, String name, Set<Modifier> modifiers,
                                  String... parameters) throws IOException {
        return beginMethod(returnType, name, modifiers, Arrays.asList(parameters), null);
    }

    public FileWriter beginMethod(String returnType, String name, Set<Modifier> modifiers,
                                  List<String> parameters, List<String> throwsTypes) throws IOException {
        indent();
        emitModifiers(modifiers);
        if (returnType != null) {
            emitCompressedType(returnType);
            out.write(" ");
            out.write(name);
        } else {
            emitCompressedType(name);
        }
        out.write("(");
        if (parameters != null) {
            for (int p = 0; p < parameters.size(); ) {
                if (p != 0) {
                    out.write(", ");
                }
                emitCompressedType(parameters.get(p++));
                out.write(" ");
                emitCompressedType(parameters.get(p++));
            }
        }
        out.write(")");
        if (throwsTypes != null && throwsTypes.size() > 0) {
            out.write("\n");
            indent();
            out.write("    throws ");
            for (int i = 0; i < throwsTypes.size(); i++) {
                if (i != 0) {
                    out.write(", ");
                }
                emitCompressedType(throwsTypes.get(i));
            }
        }
        if (modifiers.contains(ABSTRACT) || Scope.INTERFACE_DECLARATION.equals(scopes.peek())) {
            out.write(";\n");
            scopes.push(Scope.ABSTRACT_METHOD);
        } else {
            out.write(" {\n");
            scopes.push(returnType == null ? Scope.CONSTRUCTOR : Scope.NON_ABSTRACT_METHOD);
        }
        return this;
    }

    public FileWriter beginConstructor(Set<Modifier> modifiers, String... parameters)
            throws IOException {
        beginMethod(null, rawType(types.peekFirst()), modifiers, parameters);
        return this;
    }

    public FileWriter beginConstructor(Set<Modifier> modifiers,
                                       List<String> parameters, List<String> throwsTypes)
            throws IOException {
        beginMethod(null, rawType(types.peekFirst()), modifiers, parameters, throwsTypes);
        return this;
    }

    public FileWriter emitJavadoc(String javadoc, Object... params) throws IOException {
        String formatted = String.format(javadoc, params);

        indent();
        out.write("/**\n");
        for (String line : formatted.split("\n")) {
            indent();
            out.write(" *");
            if (!line.isEmpty()) {
                out.write(" ");
                out.write(line);
            }
            out.write("\n");
        }
        indent();
        out.write(" */\n");
        return this;
    }

    public FileWriter emitSingleLineComment(String comment, Object... args) throws IOException {
        indent();
        out.write("// ");
        out.write(String.format(comment, args));
        out.write("\n");
        return this;
    }

    public FileWriter emitEmptyLine() throws IOException {
        out.write("\n");
        return this;
    }

    public FileWriter emitEnumValue(String name) throws IOException {
        indent();
        out.write(name);
        out.write(",\n");
        return this;
    }

    public FileWriter emitEnumValue(String name, boolean isLast) throws IOException {
        return isLast ? emitLastEnumValue(name) : emitEnumValue(name);
    }

    private FileWriter emitLastEnumValue(String name) throws IOException {
        indent();
        out.write(name);
        out.write(";\n");
        return this;
    }

    public FileWriter emitEnumValues(Iterable<String> names) throws IOException {
        Iterator<String> iterator = names.iterator();

        while (iterator.hasNext()) {
            String name = iterator.next();
            if (iterator.hasNext()) {
                emitEnumValue(name);
            } else {
                emitLastEnumValue(name);
            }
        }

        return this;
    }

    public FileWriter emitAnnotation(String annotation) throws IOException {
        return emitAnnotation(annotation, Collections.<String, Object>emptyMap());
    }

    public FileWriter emitAnnotation(Class<? extends Annotation> annotationType) throws IOException {
        return emitAnnotation(type(annotationType), Collections.<String, Object>emptyMap());
    }

    public FileWriter emitAnnotation(Class<? extends Annotation> annotationType, Object value)
            throws IOException {
        return emitAnnotation(type(annotationType), value);
    }

    public FileWriter emitAnnotation(String annotation, Object value) throws IOException {
        indent();
        out.write("@");
        emitCompressedType(annotation);
        out.write("(");
        emitAnnotationValue(value);
        out.write(")");
        out.write("\n");
        return this;
    }

    public FileWriter emitAnnotation(Class<? extends Annotation> annotationType,
                                     Map<String, ?> attributes) throws IOException {
        return emitAnnotation(type(annotationType), attributes);
    }

    public FileWriter emitAnnotation(String annotation, Map<String, ?> attributes)
            throws IOException {
        indent();
        out.write("@");
        emitCompressedType(annotation);
        switch (attributes.size()) {
            case 0:
                break;
            case 1:
                Entry<String, ?> onlyEntry = attributes.entrySet().iterator().next();
                out.write("(");
                if (!"value".equals(onlyEntry.getKey())) {
                    out.write(onlyEntry.getKey());
                    out.write(" = ");
                }
                emitAnnotationValue(onlyEntry.getValue());
                out.write(")");
                break;
            default:
                boolean split = attributes.size() > MAX_SINGLE_LINE_ATTRIBUTES
                        || containsArray(attributes.values());
                out.write("(");
                scopes.push(Scope.ANNOTATION_ATTRIBUTE);
                String separator = split ? "\n" : "";
                for (Map.Entry<String, ?> entry : attributes.entrySet()) {
                    out.write(separator);
                    separator = split ? ",\n" : ", ";
                    if (split) {
                        indent();
                    }
                    out.write(entry.getKey());
                    out.write(" = ");
                    Object value = entry.getValue();
                    emitAnnotationValue(value);
                }
                popScope(Scope.ANNOTATION_ATTRIBUTE);
                if (split) {
                    out.write("\n");
                    indent();
                }
                out.write(")");
                break;
        }
        out.write("\n");
        return this;
    }

    private boolean containsArray(Collection<?> values) {
        for (Object value : values) {
            if (value instanceof Object[]) {
                return true;
            }
        }
        return false;
    }

    private FileWriter emitAnnotationValue(Object value) throws IOException {
        if (value instanceof Object[]) {
            out.write("{");
            boolean firstValue = true;
            scopes.push(Scope.ANNOTATION_ARRAY_VALUE);
            for (Object o : ((Object[]) value)) {
                if (firstValue) {
                    firstValue = false;
                    out.write("\n");
                } else {
                    out.write(",\n");
                }
                indent();
                out.write(o.toString());
            }
            popScope(Scope.ANNOTATION_ARRAY_VALUE);
            out.write("\n");
            indent();
            out.write("}");
        } else {
            out.write(value.toString());
        }
        return this;
    }

    public FileWriter emitStatement(String pattern, Object... args) throws IOException {
        checkInMethod();
        String[] lines = String.format(pattern, args).split("\n", -1);
        indent();
        out.write(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            out.write("\n");
            hangingIndent();
            out.write(lines[i]);
        }
        out.write(";\n");
        return this;
    }

    // NOTE: This method is for binary compatibility with previous versions.
    public FileWriter beginControlFlow(String controlFlow) throws IOException {
        return beginControlFlow(controlFlow, new Object[0]);
    }

    public FileWriter beginControlFlow(String controlFlow, Object... args) throws IOException {
        checkInMethod();
        indent();
        out.write(String.format(controlFlow, args));
        out.write(" {\n");
        scopes.push(Scope.CONTROL_FLOW);
        return this;
    }

    // NOTE: This method is for binary compatibility with previous versions.
    public FileWriter nextControlFlow(String controlFlow) throws IOException {
        return nextControlFlow(controlFlow, new Object[0]);
    }

    public FileWriter nextControlFlow(String controlFlow, Object... args) throws IOException {
        popScope(Scope.CONTROL_FLOW);
        indent();
        scopes.push(Scope.CONTROL_FLOW);
        out.write("} ");
        out.write(String.format(controlFlow, args));
        out.write(" {\n");
        return this;
    }

    public FileWriter endControlFlow() throws IOException {
        return endControlFlow(null);
    }

    // NOTE: This method is for binary compatibility with previous versions.
    public FileWriter endControlFlow(String controlFlow) throws IOException {
        return endControlFlow(controlFlow, new Object[0]);
    }

    public FileWriter endControlFlow(String controlFlow, Object... args) throws IOException {
        popScope(Scope.CONTROL_FLOW);
        indent();
        if (controlFlow != null) {
            out.write("} ");
            out.write(String.format(controlFlow, args));
            out.write(";\n");
        } else {
            out.write("}\n");
        }
        return this;
    }

    public FileWriter endMethod() throws IOException {
        Scope popped = scopes.pop();
        // support calling a constructor a "method" to support the legacy code
        if (popped == Scope.NON_ABSTRACT_METHOD || popped == Scope.CONSTRUCTOR) {
            indent();
            out.write("}\n");
        } else if (popped != Scope.ABSTRACT_METHOD) {
            throw new IllegalStateException();
        }
        return this;
    }

    public FileWriter endConstructor() throws IOException {
        popScope(Scope.CONSTRUCTOR);
        indent();
        out.write("}\n");
        return this;
    }

    public static String type(Class<?> raw, String... parameters) {
        if (parameters.length == 0) {
            return raw.getCanonicalName();
        }
        if (raw.getTypeParameters().length != parameters.length) {
            throw new IllegalArgumentException();
        }
        StringBuilder result = new StringBuilder();
        result.append(raw.getCanonicalName());
        result.append("<");
        result.append(parameters[0]);
        for (int i = 1; i < parameters.length; i++) {
            result.append(", ");
            result.append(parameters[i]);
        }
        result.append(">");
        return result.toString();
    }

    public static String rawType(String type) {
        int lessThanIndex = type.indexOf('<');
        if (lessThanIndex != -1) {
            return type.substring(0, lessThanIndex);
        }
        return type;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    private void emitModifiers(Set<Modifier> modifiers) throws IOException {
        if (modifiers.isEmpty()) {
            return;
        }
        // Use an EnumSet to ensure the proper ordering
        if (!(modifiers instanceof EnumSet)) {
            modifiers = EnumSet.copyOf(modifiers);
        }
        for (Modifier modifier : modifiers) {
            out.append(modifier.toString()).append(' ');
        }
    }

    private void indent() throws IOException {
        for (int i = 0, count = scopes.size(); i < count; i++) {
            out.write(indent);
        }
    }

    private void hangingIndent() throws IOException {
        for (int i = 0, count = scopes.size() + 2; i < count; i++) {
            out.write(indent);
        }
    }

    private static final EnumSet<Scope> METHOD_SCOPES = EnumSet.of(
            Scope.NON_ABSTRACT_METHOD, Scope.CONSTRUCTOR, Scope.CONTROL_FLOW, Scope.INITIALIZER);

    private void checkInMethod() {
        if (!METHOD_SCOPES.contains(scopes.peekFirst())) {
            throw new IllegalArgumentException();
        }
    }

    private void popScope(Scope... expected) {
        if (!EnumSet.copyOf(Arrays.asList(expected)).contains(scopes.pop())) {
            throw new IllegalStateException();
        }
    }

    private enum Scope {
        TYPE_DECLARATION,
        INTERFACE_DECLARATION,
        ABSTRACT_METHOD,
        NON_ABSTRACT_METHOD,
        CONSTRUCTOR,
        CONTROL_FLOW,
        ANNOTATION_ATTRIBUTE,
        ANNOTATION_ARRAY_VALUE,
        INITIALIZER
    }
}
