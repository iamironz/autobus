package com.implimentz.autobus.compiler;

final class ProcessingException extends Exception {

    ProcessingException(final String msg, final Object... args) {
        super(String.format(msg, args));
    }
}
