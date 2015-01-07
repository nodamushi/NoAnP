package test;

import nodamushi.annotation.MustOverride;
import nodamushi.annotation.SuppressCloneWarning;

/**
 * 警告を出さないためにはaとcloneをオーバーライドする必要があります。
 * @author nodamushi
 *
 */
public class B extends A{
  public static void test(){
  }
  @Override
  @MustOverride//同じメソッド名に再びMustOverrideを付けても良い
  public void a(final Integer i){
  }

  @Override
  protected Object clone(){
    return null;
  }

}

@SuppressCloneWarning
class BB extends B{
  @Override
  @MustOverride(false)
  public void a(final Integer i){
    super.a(i);
  }
  @MustOverride
  public static void bb(final String str){}

}
//@MustOverride(false)が指定されると、
//それ以降のサブクラスは実装を強制されない
@SuppressCloneWarning
class BBB extends BB{
  public static void bb(final String str){}
}