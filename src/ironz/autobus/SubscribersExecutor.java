package ironz.autobus;

import java.util.List;

/**
 * Created by Alexander Efremenkov.
 * Date: 11.09.2015, 11:43.
 * In Intellij IDEA 14.1.4 Ultimate
 * aefremenkov@livamster.ru
 */
class SubscribersExecutor {

    protected final boolean post(final String key, final List<Subscription> subscriptions) {
        for (final Subscription subscription : subscriptions) {
            if (subscription.getKey().equals(key)) {
                try {
                    subscription.getMethod().invoke(subscription.getType());
                } catch (Exception e) {
                    return false;
                }
            }
        }

        return true;
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
            if (subscription.getKey().equals(key)) {
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
