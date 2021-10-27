package de.darkatra.classreading

import de.darkatra.classreading.internal.ParameterAwareMethodMetadataReadingVisitor
import org.springframework.asm.Opcodes
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.core.type.MethodMetadata

/**
 * [MethodMetadata] created from a [ParameterAwareMethodMetadataReadingVisitor].
 * Heavily inspired by [SimpleMethodMetadata][org.springframework.core.type.classreading.SimpleMethodMetadata].
 */
class ParameterAwareMethodMetadata(
	private val methodName: String,
	private val access: Int,
	private val declaringClassName: String,
	private val returnTypeName: String,
	private val source: ParameterAwareMethodMetadataReadingVisitor.Source,
	private val annotations: MergedAnnotations,
	private val parameters: List<ParameterMetadata>
) : MethodMetadata {

	private val isPrivate: Boolean
		get() = access and Opcodes.ACC_PRIVATE != 0

	fun getParameters(): List<ParameterMetadata> = parameters

	override fun getMethodName(): String = methodName

	override fun getDeclaringClassName(): String = declaringClassName

	override fun getReturnTypeName(): String = returnTypeName

	override fun isAbstract(): Boolean = access and Opcodes.ACC_ABSTRACT != 0

	override fun isStatic(): Boolean = access and Opcodes.ACC_STATIC != 0

	override fun isFinal(): Boolean = access and Opcodes.ACC_FINAL != 0

	override fun isOverridable(): Boolean = !isStatic && !isFinal && !isPrivate

	override fun getAnnotations(): MergedAnnotations = annotations

	override fun equals(other: Any?): Boolean = this === other || other is ParameterAwareMethodMetadata && source == other.source

	override fun hashCode(): Int = source.hashCode()

	override fun toString(): String = source.toString()
}
