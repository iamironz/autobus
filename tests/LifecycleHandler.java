import ironz.autobus.Autobus;
import ironz.autobus.Subscribe;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 05.09.2015, 12:56
 * implimentz@gmail.com
 */
public class LifecycleHandler {

    public void init() {
        Autobus.get().subscribe(this);
    }

    @Subscribe
    public void subscribeTest(StubObject stubObject) {
        System.out.println(stubObject);
    }

    @Subscribe
    public void subscribeTest2(Long aLong) {
        System.out.println(aLong);
    }

    public void destroy() {
        Autobus.get().unsubscribe(this);
    }
}
