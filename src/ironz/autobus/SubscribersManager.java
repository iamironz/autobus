package ironz.autobus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 04.09.2015, 17:10
 * aefremenkov@livemaster.ru
 */
final class SubscribersManager {

    @SuppressWarnings("unchecked")
    protected final <T> List<Subscription> getSubscriptions(final T t) {
        final List<Subscription> list = new ArrayList<>();
        final Class<?> clazz = t.getClass();

        for (final Method method : clazz.getDeclaredMethods()) {
            if(isSubscriber(method)) {
                final Class<?> parameter = method.getParameterTypes()[0];
                method.setAccessible(true);
                list.add(new Subscription(t, parameter, method));
            }
        }

        return list;
    }

    private boolean isSubscriber(final Method method) {
        return method.isAnnotationPresent(Subscribe.class);
    }

    protected final <T> List<Subscription> getSubscriptionsByType(final T t, final List<Subscription> subscriptions) {
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
            if (subscription.getParameter().equals(t.getClass())) {
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
