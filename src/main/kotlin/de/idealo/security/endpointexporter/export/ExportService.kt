package de.idealo.security.endpointexporter.export

import de.idealo.security.endpointexporter.classreading.type.ApplicationMetadata
import de.idealo.security.endpointexporter.processing.RequestMapping
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

    fun writeAsOpenAPIDefinitions(
        applicationMetadata: ApplicationMetadata?,
        classToRequestMappings: Map<String, List<RequestMapping>>,
        outputPath: Path
    ) {

        val paths = Paths()
        classToRequestMappings.values.flatten().forEach { requestMapping ->

            val operation = Operation()

            requestMapping.requestParameters.forEach { requestParameter ->
                operation.addParametersItem(
                    Parameter()
                        .name(requestParameter.name)
                        .required(requestParameter.required)
                        .`in`("query")
                        .schema(mapTypeToSchema(requestParameter.type))
                )
            }

            requestMapping.pathVariables.forEach { pathVariable ->
                operation.addParametersItem(
                    Parameter()
                        .name(pathVariable.name)
                        .required(pathVariable.required)
                        .`in`("path")
                        .schema(mapTypeToSchema(pathVariable.type))
                )
            }

            requestMapping.requestHeaders.forEach { requestHeader ->
                operation.addParametersItem(
                    Parameter()
                        .name(requestHeader.name)
                        .required(requestHeader.required)
                        .`in`("header")
                        .schema(mapTypeToSchema(requestHeader.type))
                )
            }

            val responseStatus = requestMapping.responseStatus
            operation.responses(
                ApiResponses()
                    .addApiResponse(
                        responseStatus.value().toString(),
                        ApiResponse()
                            .description(responseStatus.reasonPhrase)
                    )
            )

            operation.extensions(
                mapOf(
                    "x-consumes" to requestMapping.consumes.joinToString(", "),
                    "x-produces" to requestMapping.produces.joinToString(", "),
                    "x-declaring-class-name" to requestMapping.declaringClassName,
                    "x-method-name" to requestMapping.methodName
                )
            )

            requestMapping.httpMethods.forEach { httpMethod ->
                val pathString = requestMapping.urlPattern.patternString
                val pathItem = paths[pathString] ?: PathItem()
                mapHttpMethodToOperation(httpMethod).invoke(pathItem, operation)
                paths.addPathItem(pathString, pathItem)
            }
        }

        val openAPI = OpenAPI().paths(paths)
        if (applicationMetadata != null) {
            openAPI.info(
                Info()
                    .title(applicationMetadata.title)
                    .version(applicationMetadata.version)
            )
        }

        outputPath.bufferedWriter().use { writer ->
            Json.pretty().writeValue(writer, openAPI)
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
            "java.lang.Long" -> IntegerSchema().format("int64")
            "int" -> IntegerSchema()
            "long" -> IntegerSchema().format("int64")
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
            else -> throw IllegalStateException("Could not map unsupported HttpMethod '${httpMethod.name()}'.")
        }
    }
}
