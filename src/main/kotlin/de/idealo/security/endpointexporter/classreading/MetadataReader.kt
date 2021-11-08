package de.idealo.security.endpointexporter.classreading

import de.idealo.security.endpointexporter.classreading.internal.MetadataReadingClassVisitor
import de.idealo.security.endpointexporter.classreading.type.ClassMetadata
import org.objectweb.asm.ClassReader
import org.springframework.core.io.Resource
import java.io.IOException

class MetadataReader(resource: Resource) {

    val classMetadata: ClassMetadata

    init {
        val visitor = MetadataReadingClassVisitor()
        getClassReader(resource).accept(visitor, ClassReader.SKIP_CODE or ClassReader.SKIP_FRAMES)
        classMetadata = visitor.getClassMetadata()
    }

    private fun getClassReader(resource: Resource): ClassReader {
        resource.inputStream.use {
            return try {
                ClassReader(it)
            } catch (e: IllegalArgumentException) {
                throw IOException(
                    "ASM ClassReader failed to parse class file - probably due to a new Java class file version that isn't supported yet: $resource",
                    e
                )
            }
        }
    }
}
