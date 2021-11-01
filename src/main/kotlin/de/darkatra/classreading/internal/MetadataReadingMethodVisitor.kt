package de.darkatra.classreading.internal

import de.darkatra.classreading.type.AnnotationMetadata
import de.darkatra.classreading.type.MethodMetadata
import de.darkatra.classreading.type.ParameterMetadata
import de.darkatra.classreading.type.Visibility
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.springframework.asm.Type

internal class MetadataReadingMethodVisitor(
    private val access: Int,
    private val name: String,
    private val descriptor: String,
    private val signature: String?,
    private val exceptions: Array<out String>?,
    private val callback: (MethodMetadata) -> Unit
) : MethodVisitor(Opcodes.ASM9) {

    private var parameters: MutableList<ParameterMetadata.Builder> = mutableListOf()
    private var annotations: MutableList<AnnotationMetadata> = mutableListOf()

    private var currentParameter = 0

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        return MetadataReadingAnnotationVisitor.get(
            descriptor = descriptor,
            visible = visible,
            callback = annotations::add
        )
    }

    override fun visitParameter(name: String?, access: Int) {
        parameters.add(ParameterMetadata.Builder(
            name = name ?: "arg$currentParameter",
            access = access,
            type = Type.getArgumentTypes(descriptor)[currentParameter].className
        ))
        currentParameter++
    }

    override fun visitParameterAnnotation(parameter: Int, descriptor: String, visible: Boolean): AnnotationVisitor? {
        return MetadataReadingAnnotationVisitor.get(
            descriptor = descriptor,
            visible = visible,
            callback = parameters[parameter]::addAnnotation
        )
    }

    override fun visitEnd() {
        callback(MethodMetadata(
            name = name,
            visibility = Visibility.of(access),
            parameters = parameters.map(ParameterMetadata.Builder::build),
            annotations = annotations
        ))
    }
}
