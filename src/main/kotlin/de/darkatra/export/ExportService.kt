package de.darkatra.export

import de.darkatra.RequestMapping
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.bufferedWriter

@Service
class ExportService {

	// TODO: finish implementation
	fun writeAsOpenAPIDefinitions(classToRequestMappings: Map<String, List<RequestMapping>>) {

		val outFile = Path.of("./open-api.json")

		val paths = Paths()
		classToRequestMappings.values.flatten().forEach { requestMapping ->

			val operation = Operation().summary("summary")

			requestMapping.requestParameters.forEach { requestParameter ->
				operation.addParametersItem(Parameter()
					.name(requestParameter.name)
					.required(requestParameter.required)
					.`in`("query")
					.schema(mapTypeToSchema(requestParameter.type))
				)
			}

			requestMapping.pathVariables.forEach { pathVariable ->
				operation.addParametersItem(Parameter()
					.name(pathVariable.name)
					.required(pathVariable.required)
					.`in`("path")
					.schema(mapTypeToSchema(pathVariable.type))
				)
			}

			val responseStatus = requestMapping.responseStatus
			if (responseStatus != null) {
				operation.responses(
					ApiResponses()
						.addApiResponse(
							responseStatus.value().toString(),
							ApiResponse()
								.description(responseStatus.reasonPhrase)
						)
				)
			}

			operation.extensions(mapOf(
				"x-declaring-class-name" to requestMapping.declaringClassName,
				"x-method-name" to requestMapping.methodName
			))

			requestMapping.httpMethods.forEach { httpMethod ->
				paths.addPathItem(
					requestMapping.urlPattern.patternString,
					mapHttpMethodToOperation(httpMethod).invoke(PathItem(), operation)
				)
			}
		}

		outFile.bufferedWriter().use { writer ->
			Json.pretty().writeValue(writer,
				OpenAPI()
					.info(
						Info()
							.title("Export for <application>")
							.version("1.0.0")
					)
					.paths(paths)
			)
		}
	}

	// TODO: add type mappings for remaining primitives, arrays and date types
	private fun mapTypeToSchema(type: String): Schema<*> {
		return when (type) {
			"java.lang.String" -> StringSchema()
			"java.io.File" -> StringSchema()
			"java.util.Date" -> StringSchema()
			"java.lang.Float" -> NumberSchema()
			"java.lang.Double" -> NumberSchema()
			"float" -> NumberSchema()
			"double" -> NumberSchema()
			"java.lang.Integer" -> IntegerSchema()
			"java.lang.Long" -> IntegerSchema()
			"int" -> IntegerSchema()
			"long" -> IntegerSchema()
			"boolean" -> BooleanSchema()
			"java.lang.Boolean" -> BooleanSchema()
			"java.util.List" -> ArraySchema()
			else -> ObjectSchema()
		}
	}

	private fun mapHttpMethodToOperation(httpMethod: HttpMethod): (PathItem, Operation) -> PathItem {
		return when (httpMethod) {
			HttpMethod.GET -> PathItem::get
			HttpMethod.POST -> PathItem::post
			HttpMethod.PUT -> PathItem::put
			HttpMethod.PATCH -> PathItem::patch
			HttpMethod.DELETE -> PathItem::delete
			HttpMethod.HEAD -> PathItem::head
			HttpMethod.TRACE -> PathItem::trace
			HttpMethod.OPTIONS -> PathItem::options
		}
	}
}
