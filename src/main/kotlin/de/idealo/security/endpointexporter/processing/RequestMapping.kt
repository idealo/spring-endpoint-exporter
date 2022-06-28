package de.idealo.security.endpointexporter.processing

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.util.pattern.PathPattern

data class RequestMapping(
    val urlPattern: PathPattern,
    val httpMethods: Set<HttpMethod>,
    val responseStatus: HttpStatus,
    val requestParameters: List<RequestParameter>,
    val pathVariables: List<PathVariable>,
    val requestHeaders: List<RequestHeader>,
    val consumes: List<String>,
    val produces: List<String>,
    val declaringClassName: String? = null,
    val methodName: String? = null
) {

    data class RequestParameter(
        val name: String,
        val type: String,
        val required: Boolean,
        val defaultValue: String? = null
    )

    data class PathVariable(
        val name: String,
        val type: String,
        val required: Boolean
    )

    data class RequestHeader(
        val name: String,
        val type: String,
        val required: Boolean
    )

    fun combine(other: RequestMapping): RequestMapping {
        return RequestMapping(
            urlPattern = this.urlPattern.combine(other.urlPattern),
            httpMethods = this.httpMethods.plus(other.httpMethods),
            responseStatus = other.responseStatus,
            requestParameters = other.requestParameters,
            pathVariables = other.pathVariables,
            requestHeaders = this.requestHeaders,
            consumes = other.consumes,
            produces = other.produces,
            declaringClassName = other.declaringClassName,
            methodName = other.methodName
        )
    }

    fun normalize(): RequestMapping {
        return RequestMapping(
            urlPattern = this.urlPattern,
            // according to the docs, if no httpMethods are specified, all methods are allowed
            httpMethods = when {
                this.httpMethods.isEmpty() -> HttpMethod.values().toSet()
                else -> this.httpMethods
            },
            responseStatus = this.responseStatus,
            requestParameters = this.requestParameters,
            pathVariables = this.pathVariables,
            requestHeaders = this.requestHeaders,
            consumes = normalizeMediaTypes(this.consumes),
            produces = normalizeMediaTypes(this.produces),
            declaringClassName = this.declaringClassName,
            methodName = this.methodName
        )
    }

    private fun normalizeMediaTypes(mediaTypes: List<String>): List<String> {
        return when {
            mediaTypes.isEmpty() -> listOf(MediaType.ALL_VALUE)
            else -> mediaTypes
        }
    }
}
