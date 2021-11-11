package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import de.idealo.security.endpointexporter.classreading.type.ClassMetadata
import de.idealo.security.endpointexporter.classreading.type.MethodMetadata
import de.idealo.security.endpointexporter.classreading.type.ParameterMetadata
import de.idealo.security.endpointexporter.classreading.type.Visibility
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

internal class RequestMappingProcessorTest {

    private val requestMappingProcessor = RequestMappingProcessor()

    @Test
    internal fun shouldExtractRequestMappingFromClassMetadata() {

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
                            name = GetMapping::class.qualifiedName!!,
                            attributes = mapOf(
                                "value" to arrayOf("/test/{testParameter}")
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
        assertThat(requestMappings[0].pathVariables).hasSize(1)
        assertThat(requestMappings[0].pathVariables[0].name).isEqualTo("testParameter")
        assertThat(requestMappings[0].pathVariables[0].type).isEqualTo("java.lang.String")
        assertThat(requestMappings[0].pathVariables[0].required).isTrue
        assertThat(requestMappings[0].requestParameters).isEmpty()
    }
}
