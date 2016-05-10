package com.implimentz.autobus.compiler;


import com.implimentz.autobus.ArgumentSubscriber;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static javax.lang.model.element.Modifier.*;

/**
 * Created by Alexander Efremenkov.
 * Date: 12.03.16, 13:59
 * In Intellij IDEA 15.0.4 Ultimate
 * email: implimentz@gmail.com
 * twitter: iamironz
 */
public final class AutobusClassGenerator {

    public static final String REMOVE_METHOD_NAME = "remove";
    private static final String LIST = "_LIST";
    private static final String PACKAGE_NAME = "ironz.autobus";
    private static final String CLASS_NAME = "Autobus";

    private static final String FACTORY_CLASS_NAME = PACKAGE_NAME + "." + CLASS_NAME;

    private static final String SUBSCRIBER = "subscriber";
    public static final String HASH_CODE = "hashCode";
    public static final String EVENT = "event";

    private FileWriter writer;
    private volatile boolean closed = false;

    public final void initSource(final Filer filer) throws Exception {
        final JavaFileObject sourceFile = filer.createSourceFile(FACTORY_CLASS_NAME);
        writer = new FileWriter(sourceFile.openWriter());
    }

    public final void generate(final List<ProcessMetaData> generated) throws Exception {

        if (closed) {
            return;
        }

        createImports(generated);

        createClassTitle();

        createArrayFields(generated);

        createGeneralMethods(generated);

        createRemoveExtractedMethod();

        writer.endType();

        writer.close();

        closed = true;
    }

    private void createImports(final List<ProcessMetaData> generated) throws Exception {

        writer.emitPackage(PACKAGE_NAME);

        writer.emitEmptyLine();

        writer.emitImports(CopyOnWriteArrayList.class);
        writer.emitImports(ArrayList.class);
        writer.emitImports(List.class);
        writer.emitImports(ArgumentSubscriber.class);

        writer.emitEmptyLine();

        final List<String> addedImports = new ArrayList<>();

        for (final ProcessMetaData data : generated) {

            final String path = data.getSubscriberPath();

            if (!addedImports.contains(path)) {
                writer.emitImports(path);
                addedImports.add(path);
            }

            final String typePath = data.getFirstArgument();

            if (!addedImports.contains(typePath)) {
                writer.emitImports(typePath);
                addedImports.add(typePath);
            }

            final String subscriberClassName = data.getGeneratedClassName();

            writer.emitImports(data.getPath() + "." + subscriberClassName);
        }

        writer.emitEmptyLine();

    }

    private void createClassTitle() throws Exception {

        writer.beginType(CLASS_NAME, "class", EnumSet.of(PUBLIC, FINAL));

        writer.emitEmptyLine();
    }

    private void createArrayFields(final List<ProcessMetaData> generated) throws Exception {

        final List<Integer> initializedArrays = new ArrayList<>();

        final String argument = getSubscriberClassSimpleName();

        for (final ProcessMetaData meta : generated) {

            final boolean subscriberKeyAlreadyInitialized = initializedArrays.contains(meta.toString().hashCode());

            if (subscriberKeyAlreadyInitialized) {
                continue;
            }

            final String parametrizedType = String.format("<%s<%s>>", argument, meta.getArgumentSimpleName());

            final String type = "List" + parametrizedType;
            final String name = (meta.getArgumentSimpleName() + LIST).toUpperCase();
            final String initialValue = String.format("new %s()", getArrayListSimpleName());

            writer.emitField(type, name, EnumSet.of(PRIVATE, STATIC, FINAL), initialValue);

            initializedArrays.add(meta.toString().hashCode());
        }

        writer.emitEmptyLine();

    }

    private void createGeneralMethods(final List<ProcessMetaData> generated) throws Exception {

        final List<String> createdSubscriberClassNames = new ArrayList<>();

        for (final ProcessMetaData meta : generated) {

            final List<ProcessMetaData> bySubscriber = new ArrayList<>();

            if (createdSubscriberClassNames.contains(meta.getSubscriberClassName())) {
                continue;
            }

            for (final ProcessMetaData processMetaData : generated) {

                if (processMetaData.getSubscriberClassName().equals(meta.getSubscriberClassName())) {
                    bySubscriber.add(processMetaData);
                }

            }

            final String subscriber = meta.getSubscriberClassName();

            createSubscribersMethods(bySubscriber, subscriber);

            createUnSubscribersMethods(bySubscriber, subscriber);

            createdSubscriberClassNames.add(subscriber);
        }

        createPostMethods(generated);
    }

    private void createSubscribersMethods(final List<ProcessMetaData> bySubscriber, final String subscriber) throws Exception {

        writer.beginMethod("void", "subscribe", EnumSet.of(PUBLIC, STATIC), "final " + subscriber, SUBSCRIBER);

        for (final ProcessMetaData meta : bySubscriber) {

            final String generatedClassName = meta.getGeneratedClassName();
            final String arrayName = (meta.getArgumentSimpleName() + LIST).toUpperCase();
            final String statement = String.format("%s.add(new %s(%s))", arrayName, generatedClassName, SUBSCRIBER);

            writer.emitStatement(statement);
        }

        writer.endMethod();

        writer.emitEmptyLine();

    }

    private void createUnSubscribersMethods(final List<ProcessMetaData> bySubscriber, final String subscriber) throws Exception {

        writer.beginMethod("void", "unsubscribe", EnumSet.of(PUBLIC, STATIC), "final " + subscriber, SUBSCRIBER);

        writer.emitStatement(String.format("final int %s = %s.hashCode()", HASH_CODE, SUBSCRIBER));

        for (final ProcessMetaData meta : bySubscriber) {

            final String postKey = (meta.getArgumentSimpleName() + LIST).toUpperCase();
            final String pattern = String.format("%s(%s, %s)", REMOVE_METHOD_NAME, postKey, HASH_CODE);

            writer.emitStatement(pattern);
        }

        writer.endMethod();

        writer.emitEmptyLine();

    }

    private void createPostMethods(final List<ProcessMetaData> generated) throws Exception {

        final List<ProcessMetaData> forGeneration = sortUniqueImports(generated);

        createPostMethod(forGeneration);
    }

    private List<ProcessMetaData> sortUniqueImports(final List<ProcessMetaData> generated) {

        final List<String> createdSubscriberClassNames = new ArrayList<>();
        final List<ProcessMetaData> forGeneration = new ArrayList<>();

        for (final ProcessMetaData meta : generated) {

            final String simpleName = meta.getArgumentSimpleName();

            if (createdSubscriberClassNames.contains(simpleName)) {
                continue;
            }

            forGeneration.add(meta);
            createdSubscriberClassNames.add(simpleName);
        }

        return forGeneration;
    }

    private void createPostMethod(final List<ProcessMetaData> generated) throws Exception {

        for (final ProcessMetaData it : generated) {

            writer.beginMethod("void", "post", EnumSet.of(PUBLIC, STATIC), "final " + it.getArgumentSimpleName(), EVENT);

            final String fieldName = (it.getArgumentSimpleName() + LIST).toUpperCase();

            final String simpleName = getSubscriberClassSimpleName();

            final String controlFlow = String.format("for (final %s<%s> it : %s)", simpleName, it.getArgumentSimpleName(), fieldName);

            writer.beginControlFlow(controlFlow);

            writer.emitStatement(String.format("it.call(%s)", EVENT));

            writer.endControlFlow();

            writer.endMethod();

            writer.emitEmptyLine();
        }
    }

    private void createRemoveExtractedMethod() throws Exception {

        final String simpleName = getSubscriberClassSimpleName();

        final String classType = String.format("final List<%s<T>>", simpleName);

        writer.beginMethod("<T> void", REMOVE_METHOD_NAME, EnumSet.of(PRIVATE, STATIC), classType, "list", "final int", HASH_CODE);

        writer.emitStatement(classType + " forDeleting = new ArrayList<>()");

        writer.beginControlFlow(String.format("for (final %s it : list)", simpleName));

        writer.beginControlFlow("if (" + HASH_CODE + " == it.hash())");
        writer.emitStatement("forDeleting.add(it)");
        writer.endControlFlow();

        writer.endControlFlow();

        writer.emitStatement("list.removeAll(forDeleting)");

        writer.endMethod();

        writer.emitEmptyLine();
    }

    private String getArrayListSimpleName() {
        return CopyOnWriteArrayList.class.getSimpleName();
    }

    private String getSubscriberClassSimpleName() {
        return ArgumentSubscriber.class.getSimpleName();
    }
}
