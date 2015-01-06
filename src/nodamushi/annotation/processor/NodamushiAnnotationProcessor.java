package nodamushi.annotation.processor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

import nodamushi.annotation.Clone;
import nodamushi.annotation.MustOverride;
import nodamushi.annotation.SuppressCloneWarning;
import nodamushi.annotation.SuppressOverrideWarning;
/**
 * Clone,ForcedOverride,SuppressClone,SuppressOverrideを処理します。<br>
 * <br>
 * "nodamushi.override.level"オプションにより、警告のレベルを変えることが出来ます。<br>
 * デフォルトでは警告です。<br>
 * "error"を指定するとエラーに、"note"を指定すると情報になります。
 * @author nodamushi
 *
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions(NodamushiAnnotationProcessor.OPTION)
public class NodamushiAnnotationProcessor extends AbstractProcessor{

  public static final String OPTION = "nodamushi.override.level";

  private AnnotationScan s;
  private Elements elements;
  private Kind level = Kind.WARNING;
  @Override
  public synchronized void init(final ProcessingEnvironment processingEnv){
    super.init(processingEnv);
    elements = processingEnv.getElementUtils();
    String l=processingEnv.getOptions().get(OPTION);
    if(l != null){
      l = l.toLowerCase();
      if("error".equals(l) || "e".equals(l)){
        level = Kind.ERROR;
      }else if("note".equals(l) || "n".equals(l)){
        level = Kind.NOTE;
      }
    }
    s = new AnnotationScan(processingEnv.getMessager(),elements,level);
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations ,
      final RoundEnvironment roundEnv){
    for(final Element e:roundEnv.getRootElements()){
      s.scan(e);
    }

    return false;
  }

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


  /**
   * このクラスだけでなく、クラスをさかのぼってCloneが付加されているか調べる
   * @author nodamushi
   *
   */
  private static class AnnotationScan extends ElementScanner6<Void, Void>{
    private Messager m;
    private CloneMethodScan s;
    private Elements elements;
    private Kind level;
    public AnnotationScan(final Messager m,final Elements elements,final Kind level){
      this.m = m;
      s = new CloneMethodScan();
      this.elements = elements;
      this.level = level;
    }

    @Override
    public Void visitType(final TypeElement e ,final Void p){
      if(isClass(e)){
        if(isAbstractClass(e)){
          return null;
        }
        checkClone(e);
        checkForcedMethod(e);
        return null;
      }

      return super.visitType(e, p);
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
                if(elements.overrides(l, scane, (TypeElement)l.getEnclosingElement())){
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

      final OverrideMethodScan s2 = new OverrideMethodScan(l, e, elements);
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


  }



  /**
   * cloneメソッドが実装されているか調べる
   * @author nodamushi
   *
   */
  private static class CloneMethodScan extends ElementScanner6<Boolean, Void>{

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



  private static class ForcedOverrideMethodScan extends ElementScanner6<ExecutableElement, Void>{
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



  private static class OverrideMethodScan extends ElementScanner6<Void, Void>{
    private List<ExecutableElement> list;
    private TypeElement classe;
    private Elements elements;
    public OverrideMethodScan(final List<ExecutableElement> list,final TypeElement classe,
        final Elements elements){
      this.list = list;
      this.elements = elements;
      this.classe = classe;
    }

    @Override
    public Void visitExecutable(final ExecutableElement e ,final Void p){
      if(e.getKind() != ElementKind.METHOD){
        return null;
      }
      final Iterator<ExecutableElement> i = list.iterator();
      while(i.hasNext()){
        if(elements.overrides(i.next(), e, classe)){
          i.remove();
          break;
        }
      }

      return null;
    }

  }



}
