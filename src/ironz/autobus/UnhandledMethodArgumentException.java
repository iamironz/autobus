package ironz.autobus;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 04.09.2015, 19:27
 * aefremenkov@livemaster.ru
 */
public final class UnhandledMethodArgumentException extends RuntimeException {

    private static final long serialVersionUID = 5162710183343718792L;

    public UnhandledMethodArgumentException() {
        super();
    }

    public UnhandledMethodArgumentException(final String detailMessage) {
        super(detailMessage);
    }
}
