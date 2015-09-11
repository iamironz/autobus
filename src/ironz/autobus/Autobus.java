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

    private final SubscribersFetcher fetcher = new SubscribersFetcher();
    private final SubscribersExecutor executor = new SubscribersExecutor();

    private final List<Subscription> subscriptions = new ArrayList<>();
    private final List<Subscription> subscriptionsWithKey = new ArrayList<>();
    private final List<Subscription> subscriptionsWithoutValue = new ArrayList<>();

    public final <T> void subscribe(final T t) {
        final List<Subscription> subscriptionList = fetcher.getSubscriptions(t);
        final List<Subscription> subscriptionsListWithKey = fetcher.getSubscriptionsWithKey(t);
        final List<Subscription> subscriptionsListWithoutValue = fetcher.getSubscriptionsListWithoutValue(t);
        subscriptions.addAll(subscriptionList);
        subscriptionsWithKey.addAll(subscriptionsListWithKey);
        subscriptionsWithoutValue.addAll(subscriptionsListWithoutValue);
    }

    public final <T> void unsubscribe(final T t) {
        final List<Subscription> subscriptionList = fetcher.getSubscriptionsByObject(t, subscriptions);
        final List<Subscription> subscriptionsListWithKey = fetcher.getSubscriptionsByObject(t, subscriptionsWithKey);
        final List<Subscription> subscriptionsListWithoutValue = fetcher.getSubscriptionsWithoutValue(t, subscriptionsWithoutValue);
        subscriptions.removeAll(subscriptionList);
        subscriptionsWithKey.removeAll(subscriptionsListWithKey);
        subscriptionsWithoutValue.removeAll(subscriptionsListWithoutValue);
    }

    public final <T> boolean post(final T t) {
        return executor.post(t, subscriptions);
    }

    public final <T> boolean post(final String key, final T t) {
        return executor.post(key, t, subscriptionsWithKey);
    }

    public final boolean post(final String key) {
        return executor.post(key, subscriptionsWithoutValue);
    }
}
