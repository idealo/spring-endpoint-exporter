package de.darkatra

import org.springframework.http.HttpMethod
import org.springframework.web.util.pattern.PathPattern
import java.lang.reflect.Type

data class RequestMapping(
	val urlPattern: PathPattern,
	val httpMethods: Set<HttpMethod> = emptySet(),
	val requestParameters: List<RequestParameter> = emptyList(),
	val pathVariables: List<PathVariable> = emptyList()
) {

	fun combine(other: RequestMapping): RequestMapping {
		return RequestMapping(
			urlPattern = this.urlPattern.combine(other.urlPattern),
			httpMethods = this.httpMethods.plus(other.httpMethods),
			requestParameters = other.requestParameters,
			pathVariables = other.pathVariables
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
			requestParameters = this.requestParameters,
			pathVariables = this.pathVariables
		)
	}

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
