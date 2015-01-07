package test;

import nodamushi.annotation.Clone;
import nodamushi.annotation.MustOverride;

/**
 * Cloneはクラスに、ForcedOverrideはメソッドに付加します
 * @author nodamushi
 *
 */
@Clone
public class A{

  @MustOverride
  public void a(final Integer i){

  }

  //cloneをオーバーライドしないと警告が出ます
  @Override
  protected Object clone(){
    return null;
  }

  class AA extends A{

    @Override
    public void a(final Integer i){
      // TODO 自動生成されたメソッド・スタブ
      super.a(i);
    }

    @Override
    protected Object clone(){
      // TODO 自動生成されたメソッド・スタブ
      return super.clone();
    }
  }

}
