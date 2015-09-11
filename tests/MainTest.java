import ironz.autobus.Autobus;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 05.09.2015, 12:55
 * implimentz@gmail.com
 */
public class MainTest {

    private final Autobus autobus = AutoBusHelper.getAutobus();

    private final LifecycleHandler handler = new LifecycleHandler();
    private final LifecycleHandler handler1 = new LifecycleHandler();

    @Before
    public void init() {
        handler.init();
        handler1.init();
    }

    @Test
    public void testBaseSending() {
        autobus.post(new StubObject(0, "title"));
        autobus.post(256L);

        handler.destroy();
        handler1.destroy();
    }

    @Test
    public void testSendingByKey() {
        autobus.post(LifecycleHandler.FIRST_KEY, Arrays.asList("One", "Two"));
        autobus.post(LifecycleHandler.SECOND_KEY, 0L);
        autobus.post(LifecycleHandler.SECOND_KEY);

        handler.destroy();
        handler1.destroy();
    }
}
