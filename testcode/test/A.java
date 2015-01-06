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

}

