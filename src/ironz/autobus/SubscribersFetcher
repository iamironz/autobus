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
final class SubscribersFetcher {

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
                if(method.getParameterTypes().length > 0) {
                    final Class<?> parameter = method.getParameterTypes()[0];
                    list.add(new Subscription(t, parameter, method, method.getAnnotation(Subscribe.class).key()));
                }
            }
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    public <T> List<Subscription> getSubscriptionsListWithoutValue(final T t) {
        final List<Subscription> list = new ArrayList<>();

        for (final Method method : t.getClass().getDeclaredMethods()) {
            if(method.isAnnotationPresent(Subscribe.class) && !method.getAnnotation(Subscribe.class).key().isEmpty()) {
                method.setAccessible(true);
                if(method.getParameterTypes().length == 0) {
                    list.add(new Subscription(t, null, method, method.getAnnotation(Subscribe.class).key()));
                }
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

    public <T> List<Subscription> getSubscriptionsWithoutValue(final T t, final List<Subscription> subscriptions) {
        final List<Subscription> list = new ArrayList<>();

        for (final Subscription subscription : subscriptions) {
            if (subscription.getType() == t) {
                list.add(subscription);
            }
        }

        return list;
    }
}
