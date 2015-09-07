import ironz.autobus.Autobus;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 07.09.2015, 10:20
 * implimentz@gmail.com
 */
public class AutoBusHelper {

    private static final Autobus autobus;

    static {
        autobus = new Autobus();
    }

    public static Autobus getAutobus() {
        return autobus;
    }
}
