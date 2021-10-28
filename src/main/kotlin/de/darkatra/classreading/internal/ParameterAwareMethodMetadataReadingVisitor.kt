package de.darkatra.classreading.internal

import de.darkatra.classreading.ParameterAwareMethodMetadata
import de.darkatra.classreading.ParameterMetadata
import org.springframework.asm.AnnotationVisitor
import org.springframework.asm.MethodVisitor
import org.springframework.asm.SpringAsmInfo
import org.springframework.asm.Type
import org.springframework.core.annotation.MergedAnnotation
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.core.type.MethodMetadata
import java.util.function.Consumer

/**
 * ASM method visitor that creates [ParameterAwareMethodMetadata].
 * Heavily inspired by [SimpleMethodMetadataReadingVisitor][org.springframework.core.type.classreading.SimpleMethodMetadataReadingVisitor].
 */
class ParameterAwareMethodMetadataReadingVisitor(
	private val declaringClassName: String,
	private val access: Int,
	private val methodName: String,
	private val descriptor: String,
	private val consumer: Consumer<MethodMetadata>
) : MethodVisitor(SpringAsmInfo.ASM_VERSION) {

	private val annotations: MutableList<MergedAnnotation<*>> = ArrayList(4)
	private var currentParameter = 0
	private val parameters: MutableList<ParameterMetadata.Builder> = ArrayList(4)
	private val source: Source by lazy { Source(declaringClassName, methodName, descriptor) }

	override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
		return MergedAnnotationReadingVisitorFactory.get(
			source, descriptor, visible
		) { e: MergedAnnotation<Annotation> -> annotations.add(e) }
	}

	override fun visitParameter(name: String?, access: Int) {
		super.visitParameter(name, access)
		parameters.add(ParameterMetadata.Builder(
			name = name ?: "arg$currentParameter",
			access = access,
			type = Type.getArgumentTypes(descriptor)[currentParameter].className
		))
		currentParameter++
	}

	override fun visitParameterAnnotation(parameter: Int, descriptor: String, visible: Boolean): AnnotationVisitor? {
		return MergedAnnotationReadingVisitorFactory.get(
			source, descriptor, visible
		) { e: MergedAnnotation<Annotation> -> parameters[parameter].addAnnotation(e) }
	}

	override fun visitEnd() {
		if (annotations.isNotEmpty()) {
			val returnTypeName = Type.getReturnType(descriptor).className
			val annotations = MergedAnnotations.of(annotations)
			val parameters = parameters.map { it.build() }
			val metadata = ParameterAwareMethodMetadata(methodName, access, declaringClassName, returnTypeName, source, annotations, parameters)
			consumer.accept(metadata)
		}
	}

	data class Source(
		private val declaringClassName: String,
		private val methodName: String,
		private val descriptor: String
	) {

		private var toStringValue: String? = null

		override fun toString(): String {
			if (toStringValue == null) {
				val builder = StringBuilder()
				builder.append(declaringClassName)
				builder.append('.')
				builder.append(methodName)
				val argumentTypes = Type.getArgumentTypes(descriptor)
				builder.append('(')
				for (i in argumentTypes.indices) {
					if (i != 0) {
						builder.append(',')
					}
					builder.append(argumentTypes[i].className)
				}
				builder.append(')')
				toStringValue = builder.toString()
			}
			return toStringValue!!
		}
	}
}
