package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import de.idealo.security.endpointexporter.classreading.type.MethodMetadata
import de.idealo.security.endpointexporter.classreading.type.ParameterMetadata
import de.idealo.security.endpointexporter.classreading.type.Visibility
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.bind.annotation.RequestParam

internal class RequestParameterProcessorTest {

    private val requestParameterProcessor = RequestParameterProcessor()

    @Test
    internal fun `should extract RequestParameter with implicit name`() {

        val methodMetadata = MethodMetadata(
            name = "testMethod",
            visibility = Visibility.PUBLIC,
            parameters = listOf(
                ParameterMetadata(
                    name = "testParameter",
                    type = "java.lang.String",
                    annotations = listOf(
                        AnnotationMetadata(
                            name = RequestParam::class.qualifiedName!!,
                            attributes = mapOf(
                                "required" to false,
                                "defaultValue" to "testDefaultValue"
                            ),
                            annotations = emptyList()
                        )
                    )
                )
            ),
            annotations = emptyList()
        )

        val requestParameters = requestParameterProcessor.process(methodMetadata)

        assertThat(requestParameters).hasSize(1)
        assertThat(requestParameters[0].name).isEqualTo("testParameter")
        assertThat(requestParameters[0].type).isEqualTo("java.lang.String")
        assertThat(requestParameters[0].required).isFalse
        assertThat(requestParameters[0].defaultValue).isEqualTo("testDefaultValue")
    }

    @Test
    internal fun `should extract RequestParameter with explicit name via name attribute`() {

        val methodMetadata = MethodMetadata(
            name = "testMethod",
            visibility = Visibility.PUBLIC,
            parameters = listOf(
                ParameterMetadata(
                    name = "testParameter",
                    type = "java.lang.String",
                    annotations = listOf(
                        AnnotationMetadata(
                            name = RequestParam::class.qualifiedName!!,
                            attributes = mapOf(
                                "name" to "testParameterName"
                            ),
                            annotations = emptyList()
                        )
                    )
                )
            ),
            annotations = emptyList()
        )

        val requestParameters = requestParameterProcessor.process(methodMetadata)

        assertThat(requestParameters).hasSize(1)
        assertThat(requestParameters[0].name).isEqualTo("testParameterName")
        assertThat(requestParameters[0].type).isEqualTo("java.lang.String")
        assertThat(requestParameters[0].required).isTrue
        assertThat(requestParameters[0].defaultValue).isNull()
    }

    @Test
    internal fun `should extract RequestParameter with explicit name via value attribute`() {

        val methodMetadata = MethodMetadata(
            name = "testMethod",
            visibility = Visibility.PUBLIC,
            parameters = listOf(
                ParameterMetadata(
                    name = "testParameter",
                    type = "java.lang.String",
                    annotations = listOf(
                        AnnotationMetadata(
                            name = RequestParam::class.qualifiedName!!,
                            attributes = mapOf(
                                "value" to "testParameterName"
                            ),
                            annotations = emptyList()
                        )
                    )
                )
            ),
            annotations = emptyList()
        )

        val requestParameters = requestParameterProcessor.process(methodMetadata)

        assertThat(requestParameters).hasSize(1)
        assertThat(requestParameters[0].name).isEqualTo("testParameterName")
        assertThat(requestParameters[0].type).isEqualTo("java.lang.String")
        assertThat(requestParameters[0].required).isTrue
        assertThat(requestParameters[0].defaultValue).isNull()
    }
}
