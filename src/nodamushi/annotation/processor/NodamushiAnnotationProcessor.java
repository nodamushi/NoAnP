package nodamushi.annotation.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
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
  private Kind level = Kind.WARNING;
  @Override
  public synchronized void init(final ProcessingEnvironment processingEnv){
    super.init(processingEnv);
    String l=processingEnv.getOptions().get(OPTION);
    if(l != null){
      l = l.toLowerCase();
      if("error".equals(l) || "e".equals(l)){
        level = Kind.ERROR;
      }else if("note".equals(l) || "n".equals(l)){
        level = Kind.NOTE;
      }
    }
    s = new AnnotationScan(processingEnv,level);
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations ,
      final RoundEnvironment roundEnv){
    for(final Element e:roundEnv.getRootElements()){
      s.scan(e);
    }

    return false;
  }

}
