package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.classreading.MetadataReader
import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import de.idealo.security.endpointexporter.classreading.type.ClassMetadata
import de.idealo.security.endpointexporter.classreading.type.MethodMetadata
import de.idealo.security.endpointexporter.classreading.type.ParameterMetadata
import de.idealo.security.endpointexporter.classreading.type.Visibility
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.core.io.PathResource
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.util.pattern.PathPatternParser
import java.nio.file.Path
import java.util.stream.Stream

internal class RequestMappingProcessorTest {

    /**
     * @see de.idealo.security.endpointexporter.test.PersonController
     */
    private val testControllerClass = Path.of("target/test-classes/de/idealo/security/endpointexporter/test/PersonController.class")

    private val requestMappingProcessor = RequestMappingProcessor(
        requestParameterProcessor = RequestParameterProcessor(),
        pathVariableProcessor = PathVariableProcessor(),
        requestHeaderProcessor = RequestHeaderProcessor(),
        responseStatusProcessor = ResponseStatusProcessor(),
        patternParser = PathPatternParser()
    )

    @Test
    internal fun `should extract RequestMapping from actual class file`() {

        val metadataReader = MetadataReader(PathResource(testControllerClass))

        val requestMappings = requestMappingProcessor.process(metadataReader.classMetadata)

        assertThat(requestMappings).hasSize(2)
        assertThat(requestMappings).allMatch { requestMapping ->
            requestMapping.declaringClassName == "de.idealo.security.endpointexporter.test.PersonController"
        }

        assertThat(requestMappings[0].methodName).isEqualTo("getPersons")
        assertThat(requestMappings[0].httpMethods).containsExactly(HttpMethod.GET)
        assertThat(requestMappings[0].urlPattern.patternString).isEqualTo("/persons")
        assertThat(requestMappings[0].consumes).containsExactly("*/*")
        assertThat(requestMappings[0].produces).containsExactly("*/*")
        assertThat(requestMappings[0].responseStatus).isEqualTo(HttpStatus.OK)

        assertThat(requestMappings[1].methodName).isEqualTo("getPersonByFirstName")
        assertThat(requestMappings[1].httpMethods).containsExactly(HttpMethod.GET)
        assertThat(requestMappings[1].urlPattern.patternString).isEqualTo("/persons/{firstName}")
        assertThat(requestMappings[1].consumes).containsExactly("*/*")
        assertThat(requestMappings[1].produces).containsExactly("*/*")
        assertThat(requestMappings[1].responseStatus).isEqualTo(HttpStatus.OK)
        assertThat(requestMappings[1].pathVariables).hasSize(1)
        assertThat(requestMappings[1].pathVariables[0].name).isEqualTo("firstName")
        assertThat(requestMappings[1].pathVariables[0].type).isEqualTo("java.lang.String")
        assertThat(requestMappings[1].pathVariables[0].required).isTrue
    }

    @Test
    internal fun `should extract RequestMapping for GetMapping with RequestParam`() {

        val classMetadata = ClassMetadata(
            name = "TestClass",
            annotations = listOf(
                AnnotationMetadata(
                    name = Controller::class.qualifiedName!!,
                    attributes = emptyMap(),
                    annotations = emptyList()
                ),
                AnnotationMetadata(
                    name = RequestMapping::class.qualifiedName!!,
                    attributes = mapOf(
                        "value" to arrayOf("/test")
                    ),
                    annotations = emptyList()
                )
            ),
            methods = listOf(
                MethodMetadata(
                    name = "testMethod",
                    visibility = Visibility.PUBLIC,
                    parameters = listOf(
                        ParameterMetadata(
                            name = "testParameter",
                            type = "java.lang.String",
                            annotations = listOf(
                                AnnotationMetadata(
                                    name = RequestParam::class.qualifiedName!!,
                                    attributes = emptyMap(),
                                    annotations = emptyList()
                                )
                            )
                        )
                    ),
                    annotations = listOf(
                        AnnotationMetadata(
                            name = RequestMapping::class.qualifiedName!!,
                            attributes = mapOf(
                                "value" to arrayOf("/{testParameter}"),
                                "method" to arrayOf(HttpMethod.GET.name)
                            ),
                            annotations = emptyList()
                        )
                    )
                )
            )
        )

        val requestMappings = requestMappingProcessor.process(classMetadata)

        assertThat(requestMappings).hasSize(1)
        assertThat(requestMappings[0].declaringClassName).isEqualTo("TestClass")
        assertThat(requestMappings[0].methodName).isEqualTo("testMethod")
        assertThat(requestMappings[0].httpMethods).containsExactly(HttpMethod.GET)
        assertThat(requestMappings[0].urlPattern.patternString).isEqualTo("/test/{testParameter}")
        assertThat(requestMappings[0].consumes).containsExactly("*/*")
        assertThat(requestMappings[0].produces).containsExactly("*/*")
        assertThat(requestMappings[0].responseStatus).isEqualTo(HttpStatus.OK)
        assertThat(requestMappings[0].requestParameters).hasSize(1)
        assertThat(requestMappings[0].requestParameters[0].name).isEqualTo("testParameter")
        assertThat(requestMappings[0].requestParameters[0].type).isEqualTo("java.lang.String")
        assertThat(requestMappings[0].requestParameters[0].required).isTrue
        assertThat(requestMappings[0].requestParameters[0].defaultValue).isNull()
        assertThat(requestMappings[0].pathVariables).isEmpty()
    }

    @Test
    internal fun `should extract RequestMapping for PostMapping with explicit ResponseStatus and PathVariable`() {

        val classMetadata = ClassMetadata(
            name = "TestClass",
            annotations = listOf(
                AnnotationMetadata(
                    name = Controller::class.qualifiedName!!,
                    attributes = emptyMap(),
                    annotations = emptyList()
                )
            ),
            methods = listOf(
                MethodMetadata(
                    name = "testMethod",
                    visibility = Visibility.PUBLIC,
                    parameters = listOf(
                        ParameterMetadata(
                            name = "testParameter",
                            type = "java.lang.String",
                            annotations = listOf(
                                AnnotationMetadata(
                                    name = PathVariable::class.qualifiedName!!,
                                    attributes = emptyMap(),
                                    annotations = emptyList()
                                )
                            )
                        )
                    ),
                    annotations = listOf(
                        AnnotationMetadata(
                            name = PostMapping::class.qualifiedName!!,
                            attributes = mapOf(
                                "value" to arrayOf("/test/{testParameter}")
                            ),
                            annotations = emptyList()
                        ),
                        AnnotationMetadata(
                            name = ResponseStatus::class.qualifiedName!!,
                            attributes = mapOf(
                                "code" to HttpStatus.CREATED.name
                            ),
                            annotations = emptyList()
                        )
                    )
                )
            )
        )

        val requestMappings = requestMappingProcessor.process(classMetadata)

        assertThat(requestMappings).hasSize(1)
        assertThat(requestMappings[0].declaringClassName).isEqualTo("TestClass")
        assertThat(requestMappings[0].methodName).isEqualTo("testMethod")
        assertThat(requestMappings[0].httpMethods).containsExactly(HttpMethod.POST)
        assertThat(requestMappings[0].urlPattern.patternString).isEqualTo("/test/{testParameter}")
        assertThat(requestMappings[0].consumes).containsExactly("*/*")
        assertThat(requestMappings[0].produces).containsExactly("*/*")
        assertThat(requestMappings[0].responseStatus).isEqualTo(HttpStatus.CREATED)
        assertThat(requestMappings[0].pathVariables).hasSize(1)
        assertThat(requestMappings[0].pathVariables[0].name).isEqualTo("testParameter")
        assertThat(requestMappings[0].pathVariables[0].type).isEqualTo("java.lang.String")
        assertThat(requestMappings[0].pathVariables[0].required).isTrue
        assertThat(requestMappings[0].requestParameters).isEmpty()
    }

    @Test
    internal fun `should extract RequestMapping without explicit urlPattern but with explicit consumes and produces attributes`() {

        val classMetadata = ClassMetadata(
            name = "TestClass",
            annotations = listOf(
                AnnotationMetadata(
                    name = Controller::class.qualifiedName!!,
                    attributes = emptyMap(),
                    annotations = emptyList()
                )
            ),
            methods = listOf(
                MethodMetadata(
                    name = "testMethod",
                    visibility = Visibility.PUBLIC,
                    parameters = emptyList(),
                    annotations = listOf(
                        AnnotationMetadata(
                            name = PutMapping::class.qualifiedName!!,
                            attributes = mapOf(
                                "consumes" to arrayOf(MediaType.APPLICATION_JSON_VALUE),
                                "produces" to arrayOf(MediaType.TEXT_PLAIN_VALUE)
                            ),
                            annotations = emptyList()
                        )
                    )
                )
            )
        )

        val requestMappings = requestMappingProcessor.process(classMetadata)

        assertThat(requestMappings).hasSize(1)
        assertThat(requestMappings[0].urlPattern.patternString).isEqualTo("/")
        assertThat(requestMappings[0].consumes).containsExactly(MediaType.APPLICATION_JSON_VALUE)
        assertThat(requestMappings[0].produces).containsExactly(MediaType.TEXT_PLAIN_VALUE)
    }

    @ParameterizedTest
    @MethodSource("requestMappingToHttpStatus")
    internal fun `should extract RequestMapping for all HttpMethods`(requestMappingAnnotationName: String, expectedHttpMethod: HttpMethod) {

        val classMetadata = ClassMetadata(
            name = "TestClass",
            annotations = listOf(
                AnnotationMetadata(
                    name = Controller::class.qualifiedName!!,
                    attributes = emptyMap(),
                    annotations = emptyList()
                )
            ),
            methods = listOf(
                MethodMetadata(
                    name = "testMethod",
                    visibility = Visibility.PUBLIC,
                    parameters = emptyList(),
                    annotations = listOf(
                        AnnotationMetadata(
                            name = requestMappingAnnotationName,
                            attributes = emptyMap(),
                            annotations = emptyList()
                        )
                    )
                )
            )
        )

        val requestMappings = requestMappingProcessor.process(classMetadata)

        assertThat(requestMappings).hasSize(1)
        assertThat(requestMappings[0].httpMethods).containsExactly(expectedHttpMethod)
    }

    companion object {
        @JvmStatic
        fun requestMappingToHttpStatus(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("org.springframework.web.bind.annotation.GetMapping", HttpMethod.GET),
                Arguments.of("org.springframework.web.bind.annotation.PostMapping", HttpMethod.POST),
                Arguments.of("org.springframework.web.bind.annotation.PutMapping", HttpMethod.PUT),
                Arguments.of("org.springframework.web.bind.annotation.PatchMapping", HttpMethod.PATCH),
                Arguments.of("org.springframework.web.bind.annotation.DeleteMapping", HttpMethod.DELETE)
            )
        }
    }
}
