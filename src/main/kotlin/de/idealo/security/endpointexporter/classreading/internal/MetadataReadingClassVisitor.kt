package de.idealo.security.endpointexporter.classreading.internal

import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import de.idealo.security.endpointexporter.classreading.type.ClassMetadata
import de.idealo.security.endpointexporter.classreading.type.MethodMetadata
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.springframework.util.ClassUtils

internal class MetadataReadingClassVisitor : ClassVisitor(Opcodes.ASM9) {

    private lateinit var className: String
    private val annotations: MutableList<AnnotationMetadata> = mutableListOf()
    private val methods: MutableList<MethodMetadata> = mutableListOf()

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
        className = ClassUtils.convertResourcePathToClassName(name)
    }

    override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
        return MetadataReadingAnnotationVisitor.get(
            descriptor = desc,
            visible = visible,
            callback = annotations::add
        )
    }

    override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        return MetadataReadingMethodVisitor(
            access = access,
            name = name,
            descriptor = desc,
            callback = methods::add
        )
    }

    fun getClassMetadata(): ClassMetadata {
        return ClassMetadata(
            name = className,
            annotations = annotations,
            methods = methods
        )
    }
}
