package de.darkatra.classreading.internal

import org.springframework.asm.AnnotationVisitor
import org.springframework.asm.ClassVisitor
import org.springframework.asm.MethodVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.SpringAsmInfo
import org.springframework.core.annotation.MergedAnnotation
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.MethodMetadata
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils
import java.util.function.Consumer

class CustomAnnotationMetadataReadingClassVisitor : ClassVisitor(SpringAsmInfo.ASM_VERSION) {

	private var className = ""
	private var access = 0
	private var superClassName: String? = null
	private var interfaceNames = arrayOfNulls<String>(0)
	private var enclosingClassName: String? = null
	private var independentInnerClass = false
	private val memberClassNames: MutableSet<String> = LinkedHashSet(4)
	private val annotations: MutableList<MergedAnnotation<*>> = ArrayList()
	private val annotatedMethods: MutableList<MethodMetadata> = ArrayList()
	private var metadata: AnnotationMetadata? = null
	private var source: Source? = null

	override fun visit(version: Int, access: Int, name: String, signature: String?, supername: String?, interfaces: Array<String>?) {
		className = toClassName(name)
		this.access = access
		if (supername != null && !isInterface(access)) {
			superClassName = toClassName(supername)
		}
		interfaceNames = interfaces?.map { toClassName(it) }?.toTypedArray() ?: emptyArray()
	}

	override fun visitOuterClass(owner: String, name: String?, desc: String?) {
		enclosingClassName = toClassName(owner)
	}

	override fun visitInnerClass(name: String, outerName: String?, innerName: String?, access: Int) {
		if (outerName != null) {
			val className = toClassName(name)
			val outerClassName = toClassName(outerName)
			if (this.className == className) {
				enclosingClassName = outerClassName
				independentInnerClass = access and Opcodes.ACC_STATIC != 0
			} else if (this.className == outerClassName) {
				memberClassNames.add(className)
			}
		}
	}

	override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
		val clazz = Class.forName("org.springframework.core.type.classreading.MergedAnnotationReadingVisitor")
		return clazz
			.getDeclaredMethod("get", ClassLoader::class.java, Any::class.java, String::class.java, Boolean::class.java, Consumer::class.java)
			.also { it.trySetAccessible() }
			.invoke(clazz, null, getSource(), descriptor, visible, Consumer { e: MergedAnnotation<Annotation> -> annotations.add(e) })
			as AnnotationVisitor?
	}

	override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<String>?): MethodVisitor? {
		return when {
			isBridge(access) -> null
			else -> ParameterAwareMethodMetadataReadingVisitor(
				className, access, name, descriptor
			) { e: MethodMetadata -> annotatedMethods.add(e) }
		}
	}

	override fun visitEnd() {
		val memberClassNames = StringUtils.toStringArray(memberClassNames)
		val annotatedMethods = annotatedMethods.toTypedArray()
		val annotations = MergedAnnotations.of(annotations)

		metadata = Class.forName("org.springframework.core.type.classreading.SimpleAnnotationMetadata")
			.declaredConstructors.first()
			.also { it.trySetAccessible() }
			.newInstance(className, access, enclosingClassName, superClassName, independentInnerClass, interfaceNames, memberClassNames, annotatedMethods, annotations)
			as AnnotationMetadata
	}

	fun getMetadata(): AnnotationMetadata {
		Assert.state(metadata != null, "AnnotationMetadata not initialized")
		return metadata!!
	}

	private fun getSource(): Source {
		if (source == null) {
			source = Source(className)
		}
		return source!!
	}

	private fun toClassName(name: String): String {
		return ClassUtils.convertResourcePathToClassName(name)
	}

	private fun isBridge(access: Int): Boolean {
		return access and Opcodes.ACC_BRIDGE != 0
	}

	private fun isInterface(access: Int): Boolean {
		return access and Opcodes.ACC_INTERFACE != 0
	}

	private class Source(private val className: String) {

		override fun equals(other: Any?): Boolean {
			return when {
				this === other -> true
				other == null || javaClass != other.javaClass -> false
				else -> className == (other as Source).className
			}
		}

		override fun hashCode(): Int = className.hashCode()

		override fun toString(): String = className
	}
}
