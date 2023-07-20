package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import de.idealo.security.endpointexporter.classreading.type.MethodMetadata
import de.idealo.security.endpointexporter.classreading.type.ParameterMetadata
import de.idealo.security.endpointexporter.classreading.type.Visibility
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.bind.annotation.PathVariable

internal class PathVariableProcessorTest {

    private val pathVariableProcessor = PathVariableProcessor()

    @Test
    internal fun `should extract PathVariables with implicit name`() {

        val methodMetadata = MethodMetadata(
            name = "testMethod",
            visibility = Visibility.PUBLIC,
            parameters = listOf(
                ParameterMetadata(
                    name = "testParameter",
                    type = "java.lang.String",
                    annotations = listOf(
                        AnnotationMetadata(
                            name = PathVariable::class.qualifiedName!!,
                            attributes = mapOf(
                                "required" to false
                            ),
                            annotations = emptyList()
                        )
                    )
                )
            ),
            annotations = emptyList()
        )

        val pathVariables = pathVariableProcessor.process(methodMetadata)

        assertThat(pathVariables).hasSize(1)
        assertThat(pathVariables[0].name).isEqualTo("testParameter")
        assertThat(pathVariables[0].type).isEqualTo("java.lang.String")
        assertThat(pathVariables[0].required).isFalse
    }

    @Test
    internal fun `should extract PathVariables with explicit name via name attribute`() {

        val methodMetadata = MethodMetadata(
            name = "testMethod",
            visibility = Visibility.PUBLIC,
            parameters = listOf(
                ParameterMetadata(
                    name = "testParameter",
                    type = "java.lang.String",
                    annotations = listOf(
                        AnnotationMetadata(
                            name = PathVariable::class.qualifiedName!!,
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

        val pathVariables = pathVariableProcessor.process(methodMetadata)

        assertThat(pathVariables).hasSize(1)
        assertThat(pathVariables[0].name).isEqualTo("testParameterName")
        assertThat(pathVariables[0].type).isEqualTo("java.lang.String")
        assertThat(pathVariables[0].required).isTrue
    }

    @Test
    internal fun `should extract PathVariable with explicit name via value attribute`() {

        val methodMetadata = MethodMetadata(
            name = "testMethod",
            visibility = Visibility.PUBLIC,
            parameters = listOf(
                ParameterMetadata(
                    name = "testParameter",
                    type = "java.lang.String",
                    annotations = listOf(
                        AnnotationMetadata(
                            name = PathVariable::class.qualifiedName!!,
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

        val pathVariables = pathVariableProcessor.process(methodMetadata)

        assertThat(pathVariables).hasSize(1)
        assertThat(pathVariables[0].name).isEqualTo("testParameterName")
        assertThat(pathVariables[0].type).isEqualTo("java.lang.String")
        assertThat(pathVariables[0].required).isTrue
    }
}
