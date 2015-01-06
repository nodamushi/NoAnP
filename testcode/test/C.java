package test;

import nodamushi.annotation.Clone;
import nodamushi.annotation.MustOverride;
/**
 *
 * abstractクラスではCloneなどは無視されます
 *
 * @author nodamushi
 *
 */

@Clone
public abstract class C{
  @MustOverride
  public String abc(final Integer i){
    return i.toString();
  }

}
