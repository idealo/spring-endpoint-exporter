package de.darkatra

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.core.type.filter.RegexPatternTypeFilter
import org.springframework.core.type.filter.TypeFilter
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.util.regex.Pattern

fun main(args: Array<String>) {
	runApplication<SpringBootConsoleApplication>(*args)
}

@SpringBootApplication
class SpringBootConsoleApplication : CommandLineRunner {

	override fun run(vararg args: String) {

		val includeFilters = listOf(
			RegexPatternTypeFilter(Pattern.compile("de.darkatra.*")),
			AnnotationTypeFilter(Component::class.java)
		)

		val fileSystemResult = scanFileSystem(includeFilters)
		val jarFileResult = scanJarFile(includeFilters)

		println("FileSystemResult: ${fileSystemResult.size}")
		println("JarFileResult: ${jarFileResult.size}")
	}

	private fun scanFileSystem(includeFilters: List<TypeFilter>): List<MetadataReader> {
		val scanner = FileSystemClassScanner(includeFilters)
		return scanner.scan(Path.of("./target/classes"))
	}

	private fun scanJarFile(includeFilters: List<TypeFilter>): List<MetadataReader> {
		val scanner = JarClassScanner(includeFilters)
		return scanner.scan(Path.of("./target/spring-security-demo-1.0.0.jar"))
	}
}
