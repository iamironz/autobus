/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 05.09.2015, 12:57
 * implimentz@gmail.com
 */
public class StubObject {

    private final int id;
    private final String title;

    public StubObject(int id, String title) {
        this.title = title;
        this.id = id;
    }

    @Override
    public String toString() {
        return id + ":" + title;
    }
}
