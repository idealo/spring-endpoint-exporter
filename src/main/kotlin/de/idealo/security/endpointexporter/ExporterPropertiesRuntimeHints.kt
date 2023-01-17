package de.idealo.security.endpointexporter

import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar

class ExporterPropertiesRuntimeHints : RuntimeHintsRegistrar {

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        hints.reflection().registerType(ExporterProperties::class.java, MemberCategory.DECLARED_FIELDS)
    }
}
