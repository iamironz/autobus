package ironz.autobus;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Ironz.
 * In Intellij IDEA 14 Ultimate.
 * Date: 04.09.2015, 16:44
 * aefremenkov@livemaster.ru
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Subscribe {
}
