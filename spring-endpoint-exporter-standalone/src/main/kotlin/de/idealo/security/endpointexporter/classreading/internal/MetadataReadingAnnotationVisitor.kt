package de.idealo.security.endpointexporter.classreading.internal

import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class MetadataReadingAnnotationVisitor private constructor(
    private val name: String,
    private val callback: (AnnotationMetadata) -> Unit
) : AnnotationVisitor(Opcodes.ASM9) {

    private val annotations: MutableList<AnnotationMetadata> = mutableListOf()

    companion object {
        fun get(descriptor: String, visible: Boolean, callback: (AnnotationMetadata) -> Unit): MetadataReadingAnnotationVisitor? {
            return when (visible) {
                false -> null
                true -> MetadataReadingAnnotationVisitor(
                    name = Type.getType(descriptor).className,
                    callback = callback
                )
            }
        }
    }

    private val attributes: MutableMap<String, Any> = mutableMapOf()

    override fun visit(name: String, value: Any) {
        if (value is Type) {
            attributes[name] = value.className
        } else {
            attributes[name] = value
        }
    }

    override fun visitAnnotation(name: String, descriptor: String): AnnotationVisitor? {
        return get(
            descriptor = descriptor,
            visible = true,
            callback = { attributes[name] = it }
        )
    }

    override fun visitArray(name: String): AnnotationVisitor {
        return MetadataReadingAnnotationArrayVisitor { attributes[name] = it }
    }

    override fun visitEnum(name: String, descriptor: String, value: String) {
        attributes[name] = value
    }

    override fun visitEnd() {
        callback(
            AnnotationMetadata(
                name = name,
                attributes = attributes,
                annotations = annotations
            )
        )
    }

    private class MetadataReadingAnnotationArrayVisitor(
        private val callback: (List<Any>) -> Unit
    ) : AnnotationVisitor(Opcodes.ASM9) {

        private val values: MutableList<Any> = mutableListOf()

        override fun visit(name: String?, value: Any) {
            if (value is Type) {
                values.add(value.className)
            } else {
                values.add(value)
            }
        }

        override fun visitEnd() {
            callback(values)
        }
    }
}
