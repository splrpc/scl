/**
 * 
 */
package ch.springcloud.lite.core.anno;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
/**
 * The mock method which will be invocked when QPSlimiting.
 * 
 * @author 恋骑士
 *
 */
public @interface LimitMock {

	/**
	 * default the service itself
	 * 
	 * @return
	 */
	String service() default "";

	String method() default "";

}
