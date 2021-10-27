package de.darkatra

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.util.pattern.PathPattern

data class RequestMapping(
	val urlPattern: PathPattern,
	val httpMethods: Set<HttpMethod> = emptySet(),
	val responseStatus: HttpStatus? = null,
	val requestParameters: List<RequestParameter> = emptyList(),
	val pathVariables: List<PathVariable> = emptyList(),
	val declaringClassName: String? = null,
	val methodName: String? = null
) {

	fun combine(other: RequestMapping): RequestMapping {
		return RequestMapping(
			urlPattern = this.urlPattern.combine(other.urlPattern),
			httpMethods = this.httpMethods.plus(other.httpMethods),
			responseStatus = other.responseStatus,
			requestParameters = other.requestParameters,
			pathVariables = other.pathVariables,
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
			declaringClassName = this.declaringClassName,
			methodName = this.methodName
		)
	}

	data class RequestParameter(
		val name: String,
		val type: String,
		val required: Boolean = true,
		val defaultValue: String? = null
	)

	data class PathVariable(
		val name: String,
		val type: String,
		val required: Boolean = true
	)
}
