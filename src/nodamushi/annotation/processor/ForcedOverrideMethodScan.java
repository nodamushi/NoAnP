package nodamushi.annotation.processor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.ElementScanner6;

import nodamushi.annotation.MustOverride;

class ForcedOverrideMethodScan extends ElementScanner6<ExecutableElement, Void>{
  @Override
  public ExecutableElement visitExecutable(final ExecutableElement e ,final Void p){
    if(e.getAnnotation(MustOverride.class)!=null){
      for(final Modifier m:e.getModifiers()){
        switch(m){
          case FINAL:
          case PRIVATE:
          case NATIVE:
            return null;
          default:
            break;
        }
      }
      return e;
    }
    return null;
  }
}