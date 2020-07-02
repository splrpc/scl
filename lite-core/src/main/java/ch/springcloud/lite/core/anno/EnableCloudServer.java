package ch.springcloud.lite.core.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

import ch.springcloud.lite.core.configuration.CloudConfiguration;
import ch.springcloud.lite.core.consts.SCLConsts;
import ch.springcloud.lite.core.selector.CloudServerConfigurationSelector;

/**
 * Enable Cloud Lite Server! <br>
 * The {@link EnableCloudClient} will also be imported!
 * 
 * @author 恋骑士
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Import({ CloudServerConfigurationSelector.class, CloudConfiguration.class })
public @interface EnableCloudServer {

	/**
	 * Wheather Spring {@link Service} exposed to the cloud. <br>
	 * Default TRUE
	 */
	boolean exposeSpringService() default true;

	String exposeMethod() default SCLConsts.SPRINGMVC;

	int priority() default 100;
	
	int timeout() default 30000;

}
