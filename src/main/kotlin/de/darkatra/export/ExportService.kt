package de.darkatra.export

import com.fasterxml.jackson.databind.ObjectMapper
import de.darkatra.RequestMapping
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
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
			paths.addPathItem(
				requestMapping.urlPattern.patternString,
				PathItem().get(
					Operation()
						.summary("summary")
				)
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
