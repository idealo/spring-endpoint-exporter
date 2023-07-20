package de.idealo.security.endpointexporter.classreading.type

import org.objectweb.asm.Opcodes

enum class Visibility {
    PUBLIC,
    PACKAGE_PRIVATE,
    PROTECTED,
    PRIVATE;

    companion object {
        fun of(access: Int): Visibility {
            return when {
                access and Opcodes.ACC_PUBLIC != 0 -> PUBLIC
                access and Opcodes.ACC_PROTECTED != 0 -> PROTECTED
                access and Opcodes.ACC_PRIVATE != 0 -> PRIVATE
                else -> PACKAGE_PRIVATE
            }
        }
    }
}
