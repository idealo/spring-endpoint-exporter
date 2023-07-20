package de.idealo.security.endpointexporter

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.companionObject

abstract class LoggingAware {

    protected val log: Logger = LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass))

    private fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
        return ofClass.enclosingClass?.takeIf { ofClass.enclosingClass.kotlin.companionObject?.java == ofClass } ?: ofClass
    }
}
