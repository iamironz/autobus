package com.implimentz.autobus.compiler;

/**
 * Created by Alexander Efremenkov.
 * Date: 12.03.16, 17:02
 * In Intellij IDEA 15.0.4 Ultimate
 * email: implimentz@gmail.com
 * twitter: iamironz
 */
final class ProcessMetaData {

    private final String firstArgument;
    private final String subscriberClassName;
    private final String argumentSimpleName;
    private final String generatedClassName;
    private final String subscriberPath;
    private final String path;

    ProcessMetaData(final String firstArgument,
                    final String subscriberClassName,
                    final String argumentSimpleName,
                    final String generatedClassName,
                    final String subscriberPath,
                    final String path) {
        this.firstArgument = firstArgument;
        this.subscriberClassName = subscriberClassName;
        this.argumentSimpleName = argumentSimpleName;
        this.generatedClassName = generatedClassName;
        this.subscriberPath = subscriberPath;
        this.path = path;
    }

    public final String getFirstArgument() {
        return firstArgument;
    }
    final String getSubscriberClassName() {
        return subscriberClassName;
    }
    final String getArgumentSimpleName() {
        return argumentSimpleName;
    }
    final String getGeneratedClassName() {
        return generatedClassName;
    }
    final String getSubscriberPath() {
        return subscriberPath;
    }
    final String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ProcessMetaData{" +
                ", argumentSimpleName='" + argumentSimpleName + '\'' +
                '}';
    }
}
