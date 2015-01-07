package nodamushi.annotation.processor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementScanner6;

/**
 * cloneメソッドが実装されているか調べる
 * @author nodamushi
 *
 */
class CloneMethodScan extends ElementScanner6<Boolean, Void>{

  public CloneMethodScan(){
    super(false);
  }

  @Override
  public Boolean visitExecutable(final ExecutableElement e ,final Void p){
    final String name = e.getSimpleName().toString();
    if(name.equals("clone") && e.getParameters().isEmpty()){
      return true;
    }
    return false;
  }

}