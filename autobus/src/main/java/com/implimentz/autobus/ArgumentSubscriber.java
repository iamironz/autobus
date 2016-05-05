package com.implimentz.autobus;

/**
 * Created by Alexander Efremenkov.
 * Date: 11.03.16, 20:34
 * In Intellij IDEA 15.0.4 Ultimate
 * email: implimentz@gmail.com
 * twitter: iamironz
 */
public interface ArgumentSubscriber<T> {
    void call(final T t);
    int hash();
}
