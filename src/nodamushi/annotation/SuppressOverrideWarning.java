package nodamushi.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * メソッドの実装のオーバーライドを常に強制を解除する。<br>
 * 解除したいメソッド名を列挙する。（引数の指定は出来ない）
 * @author nodamushi
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface SuppressOverrideWarning{
  /**
   * オーバーライド指定を解除したいメソッド名
   * @return メソッド名の配列
   */
  String[] value();
}
