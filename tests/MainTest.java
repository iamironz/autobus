import ironz.autobus.Autobus;
import org.junit.Test;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 05.09.2015, 12:55
 * implimentz@gmail.com
 */
public class MainTest {

    private final Autobus autobus = AutoBusHelper.getAutobus();

    @Test
    public void testBaseSending() {
        final LifecycleHandler handler = new LifecycleHandler();
        final LifecycleHandler handler1 = new LifecycleHandler();

        handler.init();
        handler1.init();

        autobus.post(new StubObject(0, "title"));
        autobus.post(256L);
        autobus.post(LifecycleHandler.FIRST_KEY, new StubObject(1, "title 2"));
        autobus.post(LifecycleHandler.SECOND_KEY, 0L);

        handler.destroy();
        handler1.destroy();
    }



}
