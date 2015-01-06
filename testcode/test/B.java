package test;

/**
 * 警告を出さないためにはaとcloneをオーバーライドする必要があります。
 * @author nodamushi
 *
 */
public class B extends A{
  public static void test(){

  }

  @Override
  public void a(final Integer i){
  }

  @Override
  protected Object clone(){
    return null;
  }

}
