package nodamushi.annotation.processor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import nodamushi.annotation.Clone;
import nodamushi.annotation.MustOverride;
import nodamushi.annotation.SuppressCloneWarning;
import nodamushi.annotation.SuppressOverrideWarning;


class AnnotationScan extends ElementScanner6<Void, Void>{

  private static boolean isClass(final TypeElement e){
    return e.getKind() == ElementKind.CLASS;
  }

  private static boolean isAbstractClass(final TypeElement e){
    for(final Modifier m:e.getModifiers()){
      if(m == Modifier.ABSTRACT) {
        return true;
      }
    }
    return false;
  }


  private Messager m;
  private CloneMethodScan s;
  private Elements elements;
  private Types types;
  private Kind level;
  public AnnotationScan(final ProcessingEnvironment processingEnv,final Kind level){
    m = processingEnv.getMessager();
    s = new CloneMethodScan();
    elements = processingEnv.getElementUtils();
    types = processingEnv.getTypeUtils();
    this.level = level;
  }

  @Override
  public Void visitType(final TypeElement e ,final Void p){
    check(e);
    return super.visitType(e, p);
  }

  public void check(final TypeElement e ){
    if(isClass(e)){
      if(isAbstractClass(e)){
        return;
      }
      checkClone(e);
      checkForcedMethod(e);
      return;
    }
  }


  //クラスをさかのぼってCloneが付加されているか調べる
  private static boolean findAnotation(TypeElement e,
      final Class<? extends Annotation> c){
    while(true){
      if(isClass(e)){
        if(e.getAnnotation(c)!=null){
          return true;
        }
        final TypeMirror sp = e.getSuperclass();
        if(sp instanceof DeclaredType){
          e = (TypeElement)((DeclaredType)sp).asElement();
        }else{
          return false;
        }
      }else{
        return false;
      }
    }
  }



  private void checkClone(final TypeElement e){
    if(e.getAnnotation(SuppressCloneWarning.class)!=null) {
      return;
    }
    if(findAnotation(e, Clone.class)){
      final CloneMethodScan s = this.s;
      for(final Element ee:e.getEnclosedElements()){
        if(s.scan(ee)){
          return;
        }
      }
      m.printMessage(level, "'clone' isn't overrided.",e);
    }
  }

  private boolean isStatic(final Element e){
    for(final Modifier m:e.getModifiers()){
      if(m == Modifier.STATIC) {
        return true;
      }
    }
    return false;
  }

  private boolean isOverrides(final ExecutableElement e1,final ExecutableElement e2,final TypeElement te){
    if(elements.overrides(e1, e2, te)) {
      return true;
    }
    if(e1.getSimpleName().equals(e2.getSimpleName())&&
        isStatic(e1) && isStatic(e2)){
      if((e1.asType() instanceof ExecutableType)&&
          (e2.asType() instanceof ExecutableType)){
        final ExecutableType
        t1 = (ExecutableType)e1.asType(),
        t2 = (ExecutableType)e2.asType();

        if(!types.isSubsignature(t1, t2)) {
          return false;
        }
//        return types.isSubtype(t1.getReturnType(), t2.getReturnType());
        return true;

      }
    }
    return false;
  }


  private List<ExecutableElement> findAnotationMethod(TypeElement e,
      final Class<? extends Annotation> c,final List<String> ignore){
    final List<ExecutableElement> list = new ArrayList<ExecutableElement>();
    final ForcedOverrideMethodScan s = new ForcedOverrideMethodScan();

    {
      final TypeMirror sp = e.getSuperclass();
      if(sp instanceof DeclaredType){
        e = (TypeElement)((DeclaredType)sp).asElement();
      }else{
        return list;
      }
    }

    while(true){
      if(isClass(e)){
        for(final Element ee:e.getEnclosedElements()){
          final ExecutableElement scane = s.scan(ee);
          if(scane!=null && !ignore.contains(ee.getSimpleName().toString())){
            boolean overrided = false;
            for(final ExecutableElement l:list){
              if(isOverrides(l, scane, (TypeElement)l.getEnclosingElement())){
                overrided = true;
                break;
              }
            }
            if(!overrided){
              list.add(scane);
            }
          }
        }
        final TypeMirror sp = e.getSuperclass();
        if(sp instanceof DeclaredType){
          e = (TypeElement)((DeclaredType)sp).asElement();
        }else{
          return list;
        }

      } else {
        return list;
      }
    }
  }

  private void checkForcedMethod(final TypeElement e){
    final SuppressOverrideWarning so = e.getAnnotation(SuppressOverrideWarning.class);
    final List<String> ignoreNames = so == null?Collections.EMPTY_LIST:
      Arrays.asList(so.value());
    final List<ExecutableElement> l =findAnotationMethod(e, MustOverride.class,ignoreNames);
    if(l.isEmpty()) {
      return;
    }

    final OverrideMethodScan s2 = new OverrideMethodScan(l, e);
    for(final Element ee:e.getEnclosedElements()){
      s2.scan(ee);
    }

    if(!l.isEmpty()){
      for(final ExecutableElement ee:l){
        if(ee.getAnnotation(MustOverride.class).value()){
          m.printMessage(level, "'"+ee.getSimpleName()+"' isn't overrided.", e);
        }
      }
    }
  }


  private class OverrideMethodScan extends ElementScanner6<Void, Void>{
    private List<ExecutableElement> list;
    private TypeElement classe;
    public OverrideMethodScan(final List<ExecutableElement> list,final TypeElement classe){
      this.list = list;
      this.classe = classe;
    }

    @Override
    public Void visitExecutable(final ExecutableElement e ,final Void p){
      if(e.getKind() != ElementKind.METHOD){
        return null;
      }
      final Iterator<ExecutableElement> i = list.iterator();
      while(i.hasNext()){
        if(isOverrides(i.next(), e, classe)){
          i.remove();
          break;
        }
      }

      return null;
    }

  }

}