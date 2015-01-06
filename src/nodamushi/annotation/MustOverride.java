package nodamushi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * オーバーライドを常に強制させるアノテーション
 * @author nodamushi
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface MustOverride{
  /**
   * 有効性を示す。
   * @return falseを返すとき、小クラスでオーバーライドを強制しない
   */
  boolean value() default true;
}
