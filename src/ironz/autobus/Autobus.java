package ironz.autobus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 04.09.2015, 16:43
 * implimentz@gmail.com
 */
public final class Autobus {

    private static volatile Autobus autobus;

    private final SubscribersManager manager = new SubscribersManager();
    private final List<Subscription> subscriptions = new ArrayList<>();
    private final List<Subscription> subscriptionsWithKey = new ArrayList<>();

    private Autobus() {}

    public static Autobus get() {
        if (autobus == null) {
            synchronized (Autobus.class) {
                if (autobus == null) {
                    autobus = new Autobus();
                }
            }
        }
        return autobus;
    }

    public final <T> void subscribe(final T t) {
        final List<Subscription> subscriptions = manager.getSubscriptions(t);
        final List<Subscription> subscriptionsWithKey = manager.getSubscriptionsWithKey(t);
        this.subscriptions.addAll(subscriptions);
        this.subscriptionsWithKey.addAll(subscriptionsWithKey);
    }

    public final <T> void unsubscribe(final T t) {
        List<Subscription> subscriptionList = manager.getSubscriptionsFromObject(t, subscriptions);
        subscriptions.removeAll(subscriptionList);
        List<Subscription> subscriptionsListWithKey = manager.getSubscriptionsFromObject(t, subscriptionsWithKey);
        subscriptionsWithKey.removeAll(subscriptionsListWithKey);
    }

    public final <T> boolean post(final T t) {
        return manager.post(t, subscriptions);
    }

    public final <T> boolean post(final String key, final T t) {
        return manager.post(key, t, subscriptionsWithKey);
    }
}
