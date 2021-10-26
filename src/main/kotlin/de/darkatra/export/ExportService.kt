package de.darkatra.export

import com.fasterxml.jackson.databind.ObjectMapper
import de.darkatra.RequestMapping
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.parameters.Parameter
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.bufferedWriter

@Service
class ExportService(
	private val objectMapper: ObjectMapper
) {

	// TODO: finish implementation
	fun writeAsOpenAPIDefinitions(classToRequestMappings: Map<String, List<RequestMapping>>) {

		val outFile = Path.of("./open-api.json")

		val paths = Paths()
		classToRequestMappings.values.flatten().forEach { requestMapping ->

			val operation = Operation().summary("summary")

			// TODO: map type
			requestMapping.requestParameters.forEach { requestParameter ->
				operation.addParametersItem(Parameter()
					.name(requestParameter.name)
					.required(requestParameter.required)
					.`in`("query")
				)
			}

			// TODO: map type
			requestMapping.pathVariables.forEach { pathVariable ->
				operation.addParametersItem(Parameter()
					.name(pathVariable.name)
					.`in`("path")
				)
			}

			paths.addPathItem(
				requestMapping.urlPattern.patternString,
				PathItem().get(operation)
			)
		}

		outFile.bufferedWriter().use { writer ->
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer,
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
}
