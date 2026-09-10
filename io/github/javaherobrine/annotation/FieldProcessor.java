package io.github.javaherobrine.annotation;
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.element.TypeElement;
import io.github.javaherobrine.net.*;
@SupportedAnnotationTypes("io.github.javaherobrine.net.NetworkField")
public class FieldProcessor extends AbstractProcessor{
	Messager messager;
	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment) {
		super.init(processingEnvironment);
		messager=processingEnvironment.getMessager();
	}
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		roundEnv.getElementsAnnotatedWith(NetworkField.class).forEach(e->{
			e.getEnclosingElement();
		});
		// TODO Auto-generated method stub
		return false;
	}
}
