package test;

import nodamushi.annotation.Clone;
import nodamushi.annotation.ForcedOverride;
/**
 *
 * abstractクラスではCloneなどは無視されます
 *
 * @author nodamushi
 *
 */

@Clone
public abstract class C{
  @ForcedOverride
  public String abc(final Integer i){
    return i.toString();
  }

}
