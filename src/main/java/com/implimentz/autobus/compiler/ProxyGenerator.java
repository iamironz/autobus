package com.implimentz.autobus.compiler;

import com.implimentz.autobus.ArgumentSubscriber;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.lang.model.element.Modifier.*;

/**
 * Created by Alexander Efremenkov.
 * Date: 12.03.16, 14:44
 * In Intellij IDEA 15.0.4 Ultimate
 * email: implimentz@gmail.com
 * twitter: iamironz
 */
final class ProxyGenerator {

    private static final String INCORRECT_METHOD_SIGNATURE = "Method \"%s\" contains incorrect signature (e.g. no event arg)";
    private static final String INCORRECT_SIGNATURE_ARGUMENT_TYPE = "Method \"%s\" in \"%s\" must contain \"*Event\" class name pattern in method argument";

    private static final Pattern METHOD_NAME_PATTERN = Pattern.compile("(.+)(\\()(.+)(\\))");
    private static final Pattern EVENT_TYPE_PATTERN = Pattern.compile("^(.+)(Event)$");

    private static final String SUBSCRIBER = "subscriber";
    private static final String HASH = "hash";
    private static final String EVENT = "event";

    private final Elements elementUtils;
    private final Filer filer;
    private final String packageName;

    ProxyGenerator(final Elements elementUtils, final Filer filer, final String packageName) {
        this.packageName = packageName;
        this.elementUtils = elementUtils;
        this.filer = filer;
    }

    @SuppressWarnings("UnusedParameters")
    final void generate(final String generatedClassName, final String subscriberPath, final String subscriberSimpleName,
                        final String methodName, final String firstArgument, final SubscriberProcessor processor)
            throws Exception {

        final String classNameForGeneration = String.format("%s.%s%sSubscriber", packageName, subscriberSimpleName, generatedClassName);

        final JavaFileObject sourceFile = filer.createSourceFile(classNameForGeneration);
        final FileWriter writer = new FileWriter(sourceFile.openWriter());

        createImports(subscriberPath, firstArgument, writer);

        createClassTitle(classNameForGeneration, writer, firstArgument);

        createFields(subscriberSimpleName, writer);

        createConstructor(subscriberSimpleName, writer);

        createCallMethod(subscriberSimpleName, methodName, firstArgument, writer);

        createHashCodeMethod(writer);

        writer.endType();

        writer.close();
    }

    private void createClassTitle(final String classNameForGeneration, final FileWriter writer, final String firstArgument)
            throws Exception {

        final EnumSet<Modifier> modifiers = EnumSet.of(PUBLIC, FINAL);
        final String className = String.format("%s<%s>", ArgumentSubscriber.class.getSimpleName(), firstArgument);

        writer.beginType(classNameForGeneration, "class", modifiers, null, className);
    }

    private void createImports(final String subscriberPath, final String firstArgument, final FileWriter writer)
            throws Exception {

        final TypeElement superClass = elementUtils.getTypeElement(subscriberPath);
        final PackageElement packageOf = elementUtils.getPackageOf(superClass);

        if (packageOf.isUnnamed()) {
            writer.emitPackage("");
        } else {
            final String packageName = packageOf.getQualifiedName().toString();
            writer.emitPackage(packageName);
        }

        writer.emitImports(ArgumentSubscriber.class);
        writer.emitImports(firstArgument);

        writer.emitEmptyLine();
    }

    private void createFields(final String subscriberSimpleName, final FileWriter writer) throws IOException {
        writer.emitEmptyLine();

        final EnumSet<Modifier> modifiers = EnumSet.of(PRIVATE, FINAL);

        writer.emitField(subscriberSimpleName, SUBSCRIBER, modifiers);
        writer.emitField(int.class.getSimpleName(), HASH, modifiers);

        writer.emitEmptyLine();
    }

    private void createConstructor(final String subscriberSimpleName, final FileWriter writer) throws IOException {
        writer.beginConstructor(EnumSet.of(PUBLIC), "final " + subscriberSimpleName, SUBSCRIBER);
        writer.emitStatement(String.format("this.%s = %s", SUBSCRIBER, SUBSCRIBER));
        writer.emitStatement(String.format("this.%s = %s.hashCode()", HASH, SUBSCRIBER));
        writer.endConstructor();
    }

    private void createCallMethod(final String subscriberSimpleName, final String methodSignature, final String firstArgument,
                                  final FileWriter writer) throws Exception {

        writer.emitEmptyLine();

        writer.emitAnnotation(Override.class);
        writer.beginMethod("void", "call", EnumSet.of(PUBLIC, FINAL), "final " + firstArgument, EVENT);

        final Matcher matcher = METHOD_NAME_PATTERN.matcher(methodSignature);

        if (!matcher.matches()) {
            throw new ProcessingException(INCORRECT_METHOD_SIGNATURE, methodSignature);
        }

        final String classPath = matcher.group(3);

        if (!EVENT_TYPE_PATTERN.matcher(classPath).matches()) {
            throw new ProcessingException(INCORRECT_SIGNATURE_ARGUMENT_TYPE, methodSignature, subscriberSimpleName);
        }

        final String methodName = matcher.group(1);
        final String callingStatement = String.format("%s.%s(%s)", SUBSCRIBER, methodName, EVENT);

        writer.emitStatement(callingStatement);
        writer.endMethod();

        writer.emitEmptyLine();
    }

    private void createHashCodeMethod(final FileWriter writer) throws Exception {
        writer.emitAnnotation(Override.class);
        writer.beginMethod("int", HASH, EnumSet.of(PUBLIC, FINAL));
        writer.emitStatement("return " + HASH);
        writer.endMethod();
    }
}
