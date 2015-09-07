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

    private final SubscribersManager manager = new SubscribersManager();
    private final List<Subscription> subscriptions = new ArrayList<>();
    private final List<Subscription> subscriptionsWithKey = new ArrayList<>();

    public final <T> void subscribe(final T t) {
        final List<Subscription> subscriptionList = manager.getSubscriptions(t);
        final List<Subscription> subscriptionsListWithKey = manager.getSubscriptionsWithKey(t);
        subscriptions.addAll(subscriptionList);
        subscriptionsWithKey.addAll(subscriptionsListWithKey);
    }

    public final <T> void unsubscribe(final T t) {
        final List<Subscription> subscriptionList = manager.getSubscriptionsFromObject(t, subscriptions);
        final List<Subscription> subscriptionsListWithKey = manager.getSubscriptionsFromObject(t, subscriptionsWithKey);
        subscriptions.removeAll(subscriptionList);
        subscriptionsWithKey.removeAll(subscriptionsListWithKey);
    }

    public final <T> boolean post(final T t) {
        return manager.post(t, subscriptions);
    }

    public final <T> boolean post(final String key, final T t) {
        return manager.post(key, t, subscriptionsWithKey);
    }
}
