import ironz.autobus.Autobus;
import ironz.autobus.Subscribe;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 05.09.2015, 12:56
 * implimentz@gmail.com
 */
public class LifecycleHandler {

    public static final String FIRST_KEY = "first_key";
    public static final String SECOND_KEY = "second_key";
    private Autobus autobus = AutoBusHelper.getAutobus();

    public void init() {
        autobus.subscribe(this);
    }

    @Subscribe
    public void subscribeTest(StubObject object) {
        System.out.println(object);
    }

    @Subscribe
    private void subscribeTest2(Long aLong) {
        System.out.println(aLong);
    }

    @Subscribe(key = FIRST_KEY)
    private void subscribeTest3(StubObject object) {
        System.out.println(object);
    }

    @Subscribe(key = SECOND_KEY)
    private void subscribeTest4(Long aLong) {
        System.out.println(aLong);
    }

    public void destroy() {
        autobus.unsubscribe(this);
    }
}
