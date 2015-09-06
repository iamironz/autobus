import ironz.autobus.Autobus;
import org.junit.Test;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 05.09.2015, 12:55
 * implimentz@gmail.com
 */
public class MainTest {

    @Test
    public void testBaseSending() {
        LifecycleHandler handler = new LifecycleHandler();
        LifecycleHandler handler1 = new LifecycleHandler();

        handler.init();
        handler1.init();

        Autobus.get().post(new StubObject(0, "title"));
        Autobus.get().post(256L);

        handler.destroy();
        handler1.destroy();
    }

}
