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
	private var source: Source? = null

	override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
		val clazz = Class.forName("org.springframework.core.type.classreading.MergedAnnotationReadingVisitor")
		return clazz
			.getDeclaredMethod("get", ClassLoader::class.java, Any::class.java, String::class.java, Boolean::class.java, Consumer::class.java)
			.also { it.trySetAccessible() }
			.invoke(clazz, null, getSource(), descriptor, visible, Consumer { e: MergedAnnotation<Annotation> -> annotations.add(e) })
			as AnnotationVisitor?
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

	override fun visitParameterAnnotation(parameter: Int, descriptor: String?, visible: Boolean): AnnotationVisitor? {
		val clazz = Class.forName("org.springframework.core.type.classreading.MergedAnnotationReadingVisitor")
		return clazz
			.getDeclaredMethod("get", ClassLoader::class.java, Any::class.java, String::class.java, Boolean::class.java, Consumer::class.java)
			.also { it.trySetAccessible() }
			.invoke(clazz, null, getSource(), descriptor, visible, Consumer { e: MergedAnnotation<Annotation> -> parameters[parameter].addAnnotation(e) })
			as AnnotationVisitor?
	}

	override fun visitEnd() {
		if (annotations.isNotEmpty()) {
			val returnTypeName = Type.getReturnType(descriptor).className
			val annotations = MergedAnnotations.of(annotations)
			val metadata =
				ParameterAwareMethodMetadata(methodName, access, declaringClassName, returnTypeName, getSource(), annotations, parameters.map { it.build() })
			consumer.accept(metadata)
		}
	}

	private fun getSource(): Source {
		if (source == null) {
			source = Source(declaringClassName, methodName, descriptor)
		}
		return source!!
	}

	class Source(
		private val declaringClassName: String,
		private val methodName: String,
		private val descriptor: String
	) {

		private var toStringValue: String? = null

		override fun hashCode(): Int {
			var result = 1
			result = 31 * result + declaringClassName.hashCode()
			result = 31 * result + methodName.hashCode()
			result = 31 * result + descriptor.hashCode()
			return result
		}

		override fun equals(other: Any?): Boolean {
			if (this === other) {
				return true
			}
			if (other == null || javaClass != other.javaClass) {
				return false
			}
			val otherSource = other as Source
			return declaringClassName == otherSource.declaringClassName && methodName == otherSource.methodName && descriptor == otherSource.descriptor
		}

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
