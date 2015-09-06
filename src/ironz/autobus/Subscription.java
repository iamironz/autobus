package ironz.autobus;

import java.lang.reflect.Method;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 04.09.2015, 17:25
 * implimentz@gmail.com
 */
final class Subscription<T> {

    private final T t;
    private final Class<?> parameter;
    private final Method method;

    protected Subscription(final T t, final Class<?> parameter, final Method method) {
        this.t = t;
        this.parameter = parameter;
        this.method = method;
    }

    protected final T getType() {
        return t;
    }
    protected final Class<?> getParameter() {
        return parameter;
    }
    protected final Method getMethod() {
        return method;
    }
}
