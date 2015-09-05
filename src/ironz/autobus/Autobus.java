package ironz.autobus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 04.09.2015, 16:43
 * aefremenkov@livemaster.ru
 */
public final class Autobus {

    private static volatile Autobus autobus;

    private final SubscribersManager manager = new SubscribersManager();
    private final List<Subscription> subscriptions = new ArrayList<>();

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
        final List<Subscription> list = manager.getSubscriptions(t);
        subscriptions.addAll(list);
    }

    public final <T> void unsubscribe(final T t) {
        final List<Subscription> list = manager.getSubscriptionsByType(t, subscriptions);
        subscriptions.removeAll(list);
    }

    public final <T> boolean post(final T t) {
        return manager.post(t, subscriptions);
    }
}
