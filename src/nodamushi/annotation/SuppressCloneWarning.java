package nodamushi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cloneの強制をこれが指定してあるクラスでは無視をする
 * @author nodamushi
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface SuppressCloneWarning{}
