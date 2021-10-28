package de.darkatra.classreading.internal

import org.springframework.asm.AnnotationVisitor
import org.springframework.core.annotation.MergedAnnotation
import java.util.function.Consumer

class MergedAnnotationReadingVisitorFactory {

	companion object {

		private val clazz = Class.forName("org.springframework.core.type.classreading.MergedAnnotationReadingVisitor")

		fun <A : Annotation> get(source: Any?, descriptor: String, visible: Boolean, consumer: Consumer<MergedAnnotation<A>>): AnnotationVisitor? {
			return clazz
				.getDeclaredMethod("get", ClassLoader::class.java, Any::class.java, String::class.java, Boolean::class.java, Consumer::class.java)
				.also { it.trySetAccessible() }
				.invoke(clazz, null, source, descriptor, visible, consumer)
				as AnnotationVisitor?
		}
	}
}
