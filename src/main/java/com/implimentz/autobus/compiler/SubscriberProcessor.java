package com.implimentz.autobus.compiler;

import com.google.auto.service.AutoService;
import com.implimentz.autobus.Subscribe;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

@AutoService(Processor.class)
public final class SubscriberProcessor extends AbstractProcessor {

    private static final String DUPLICATED_SUBSCRIBERS = "Class \"%s\" already contains subscription with \"%s\" key";
    private static final String INCORRECT_METHOD_SIGNATURE = "Method \"%s\" in \"%s\" class must be only as \"public\" and \"final\"";
    private static final String INCORRECT_METHOD_SIGNATURE_COUNT = "Method \"%s\" in \"%s\" class must contain only one argument";

    private static final Pattern METHOD_ARGS_PATTERN = Pattern.compile(".+\\((.+)\\)");

    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    private final List<ProcessMetaData> processClassesMetaDataList = new ArrayList<>();

    private AutobusClassGenerator autobusClassGenerator;

    @Override
    public synchronized void init(final ProcessingEnvironment env) {
        super.init(env);
        try {
            elementUtils = env.getElementUtils();
            filer = env.getFiler();
            messager = env.getMessager();
            autobusClassGenerator = new AutobusClassGenerator();
            autobusClassGenerator.initSource(filer);
        } catch (Exception e) {
            error(null, e);
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Subscribe.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment env) {

        try {

            for (final Element e : env.getElementsAnnotatedWith(Subscribe.class)) {

                final String packageName = processingEnv.getElementUtils().getPackageOf(e).toString();
                final String subscriberPath = e.getEnclosingElement().toString();
                final String subscriberSimpleName = e.getEnclosingElement().getSimpleName().toString();
                final String methodName = e.toString();

                print("packageName: " + packageName);
                print("subscriberPath: " + subscriberPath);
                print("subscriberSimpleName: " + subscriberSimpleName);
                print("methodName: " + methodName);

                final Set<Modifier> modifiers = e.getModifiers();

                if (!modifiers.contains(PUBLIC) || !modifiers.contains(FINAL) || modifiers.contains(STATIC)) {
                    throw new ProcessingException(INCORRECT_METHOD_SIGNATURE, methodName, subscriberSimpleName);
                }

                final Matcher matcher = METHOD_ARGS_PATTERN.matcher(methodName);

                if (!matcher.find()) {
                    throw new ProcessingException(INCORRECT_METHOD_SIGNATURE_COUNT, methodName, subscriberSimpleName);
                }

                final String group = matcher.group(1);
                final String[] arguments = group.split(",");

                if (arguments.length > 1) {
                    throw new ProcessingException(INCORRECT_METHOD_SIGNATURE_COUNT, methodName, subscriberSimpleName);
                }

                final String firstArgument = arguments[0];

                final String[] split = firstArgument.split("\\.");

                final String argumentSimpleName = split[split.length - 1];

                final String generateClassName = subscriberSimpleName + argumentSimpleName + "Subscriber";

                print("generateClassName: " + generateClassName);

                for (final ProcessMetaData it : processClassesMetaDataList) {
                    if (it.getGeneratedClassName().equals(generateClassName)) {
                        throw new ProcessingException(DUPLICATED_SUBSCRIBERS, subscriberSimpleName, firstArgument);
                    }
                }

                final ProcessMetaData metaData =
                        new ProcessMetaData(firstArgument, subscriberSimpleName, argumentSimpleName, generateClassName, subscriberPath, packageName);

                processClassesMetaDataList.add(metaData);

                final ProxyGenerator generator = new ProxyGenerator(elementUtils, filer, packageName);
                generator.generate(argumentSimpleName, subscriberPath, subscriberSimpleName, methodName, firstArgument, this);

            }

            if (processClassesMetaDataList.isEmpty()) {
                return true;
            }

            autobusClassGenerator.generate(processClassesMetaDataList);

        } catch (Exception e) {
            error(null, e);
        }

        return true;
    }

    public final void print(String msg) {
        messager.printMessage(NOTE, msg);
    }

    public final void error(Element e, Exception ex) {
        messager.printMessage(ERROR, ex.toString(), e);
        for (final StackTraceElement element : ex.getStackTrace()) {
            messager.printMessage(ERROR, element.toString(), e);
        }
    }
}
