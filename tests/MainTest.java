import ironz.autobus.Autobus;
import org.junit.Test;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 05.09.2015, 12:55
 * aefremenkov@livemaster.ru
 */
public class MainTest {

    @Test
    public void testBaseSending() {
        AutobusMockClass mockClass = new AutobusMockClass();
        AutobusMockClass mockClass2 = new AutobusMockClass();

        mockClass.init();
        mockClass2.init();

        long start = System.currentTimeMillis();
        Autobus.get().post(new StubObject());
        System.out.println("taken: " + (System.currentTimeMillis() - start));

        mockClass.destroy();
        mockClass2.destroy();
    }

}
