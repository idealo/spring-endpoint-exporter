package de.darkatra

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.core.type.filter.RegexPatternTypeFilter
import org.springframework.core.type.filter.TypeFilter
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ControllerAdvice
import java.nio.file.Path
import java.util.regex.Pattern

fun main(args: Array<String>) {
	runApplication<SpringBootConsoleApplication>(*args)
}

@SpringBootApplication
class SpringBootConsoleApplication : CommandLineRunner {

	override fun run(vararg args: String) {

		val includeFilters = listOf(
			CompositeTypeFilter.composeAnd(
				// TODO: the base package should be configurable
				RegexPatternTypeFilter(Pattern.compile("de.darkatra.*")),
				CompositeTypeFilter.composeOr(
					AnnotationTypeFilter(Controller::class.java),
					AnnotationTypeFilter(ControllerAdvice::class.java)
				)
			)
		)

		val fileSystemResult = scanFileSystem(includeFilters)
		val jarFileResult = scanJarFile(includeFilters)

		println("FileSystemResult: ${fileSystemResult.size}")
		println("JarFileResult: ${jarFileResult.size}")

		// TODO: extract RequestMapping Information, preferably without actually loading classes
	}

	private fun scanFileSystem(includeFilters: List<TypeFilter>): List<MetadataReader> {
		val scanner = FileSystemClassScanner(includeFilters)
		// TODO: read output directory
		return scanner.scan(Path.of("./target/classes"))
	}

	private fun scanJarFile(includeFilters: List<TypeFilter>): List<MetadataReader> {
		val scanner = JarClassScanner(includeFilters)
		// TODO: read output directory and final name from pom.xml
		return scanner.scan(Path.of("./target/spring-security-demo-1.0.0.jar"))
	}
}
