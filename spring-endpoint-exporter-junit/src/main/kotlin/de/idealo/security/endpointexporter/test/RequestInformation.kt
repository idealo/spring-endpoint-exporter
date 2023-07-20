package de.idealo.security.endpointexporter.test

import java.lang.reflect.Type

// TODO: should be replaced by `RequestMapping` type from the standalone version
data class RequestInformation(
    val urlPattern: String,
    val httpMethods: Set<String>,
    val requestParameters: List<RequestParameter>,
    val pathVariables: List<PathVariable>
) {

    data class RequestParameter(
        val name: String,
        val type: Type,
        val optional: Boolean = false,
        val defaultValue: String? = null
    )

    data class PathVariable(
        val name: String,
        val type: Type
    )
}
