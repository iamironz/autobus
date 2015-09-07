package ironz.autobus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 04.09.2015, 17:10
 * implimentz@gmail.com
 */
final class SubscribersManager {

    @SuppressWarnings("unchecked")
    protected final <T> List<Subscription> getSubscriptions(final T t) {
        final List<Subscription> list = new ArrayList<>();

        for (final Method method : t.getClass().getDeclaredMethods()) {
            if(method.isAnnotationPresent(Subscribe.class) && method.getAnnotation(Subscribe.class).key().isEmpty()) {
                method.setAccessible(true);
                final Class<?> parameter = method.getParameterTypes()[0];
                list.add(new Subscription(t, parameter, method, method.getAnnotation(Subscribe.class).key()));
            }
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    protected final <T> List<Subscription> getSubscriptionsWithKey(final T t) {
        final List<Subscription> list = new ArrayList<>();

        for (final Method method : t.getClass().getDeclaredMethods()) {
            if(method.isAnnotationPresent(Subscribe.class) && !method.getAnnotation(Subscribe.class).key().isEmpty()) {
                method.setAccessible(true);
                final Class<?> parameter = method.getParameterTypes()[0];
                list.add(new Subscription(t, parameter, method, method.getAnnotation(Subscribe.class).key()));
            }
        }

        return list;
    }

    protected final <T> List<Subscription> getSubscriptionsByObject(final T t, final List<Subscription> subscriptions) {
        final List<Subscription> list = new ArrayList<>();

        for (final Subscription subscription : subscriptions) {
            if (subscription.getType() == t) {
                list.add(subscription);
            }
        }

        return list;
    }

    protected final <T> boolean post(final T t, final List<Subscription> subscriptions) {
        for (final Subscription subscription : subscriptions) {
            if (subscription.getParameter() == t.getClass()) {
                try {
                    subscription.getMethod().invoke(subscription.getType(), t);
                } catch (Exception e) {
                    return false;
                }
            }
        }

        return true;
    }

    protected final <T> boolean post(final String key, final T t, final List<Subscription> subscriptions) {
        for (final Subscription subscription : subscriptions) {
            if (subscription.getParameter() == t.getClass() && subscription.getKey().equals(key)) {
                try {
                    subscription.getMethod().invoke(subscription.getType(), t);
                } catch (Exception e) {
                    return false;
                }
            }
        }

        return true;
    }
}
