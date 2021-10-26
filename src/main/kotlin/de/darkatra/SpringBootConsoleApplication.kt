package de.darkatra

import de.darkatra.classreading.CompositeTypeFilter
import de.darkatra.classreading.JarClassScanner
import de.darkatra.export.ExportService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.core.type.filter.RegexPatternTypeFilter
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ControllerAdvice
import java.nio.file.Path
import java.util.regex.Pattern

fun main(args: Array<String>) {
	runApplication<SpringBootConsoleApplication>(*args)
}

@SpringBootApplication
class SpringBootConsoleApplication(
	private val requestMappingProcessor: RequestMappingProcessor,
	private val exportService: ExportService
) : ApplicationRunner {

	override fun run(args: ApplicationArguments) {

		// eg. "./target/spring-security-demo-1.0.0.jar"
		val jarPath = (args.getOptionValues("jarPath") ?: emptyList()).firstOrNull()
			?: throw IllegalArgumentException("Required parameter 'jarPath' is not set.")
		// eg. "de.darkatra.*"
		val basePackage = (args.getOptionValues("basePackage") ?: emptyList()).firstOrNull()
			?: throw IllegalArgumentException("Required parameter 'basePackage' is not set.")

		val includeFilters = listOf(
			CompositeTypeFilter.composeAnd(
				RegexPatternTypeFilter(Pattern.compile(basePackage)),
				CompositeTypeFilter.composeOr(
					AnnotationTypeFilter(Controller::class.java),
					AnnotationTypeFilter(ControllerAdvice::class.java)
				)
			)
		)

		val scanner = JarClassScanner(includeFilters = includeFilters)
		val scanResult = scanner.scan(Path.of(jarPath))

		val classToRequestMappings = scanResult.associateBy({ it.classMetadata.className }) {
			requestMappingProcessor.process(it.annotationMetadata)
		}

		exportService.writeAsOpenAPIDefinitions(classToRequestMappings)
	}
}
