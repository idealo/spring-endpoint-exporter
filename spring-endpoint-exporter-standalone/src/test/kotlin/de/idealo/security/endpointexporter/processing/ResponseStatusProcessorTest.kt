package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import de.idealo.security.endpointexporter.classreading.type.MethodMetadata
import de.idealo.security.endpointexporter.classreading.type.Visibility
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

internal class ResponseStatusProcessorTest {

    private val responseStatusProcessor = ResponseStatusProcessor()

    @Test
    internal fun `should extract ResponseStatus with code attribute`() {

        val methodMetadata = MethodMetadata(
            name = "testMethod",
            visibility = Visibility.PUBLIC,
            parameters = emptyList(),
            annotations = listOf(
                AnnotationMetadata(
                    name = ResponseStatus::class.qualifiedName!!,
                    attributes = mapOf(
                        "code" to HttpStatus.CREATED.name
                    ),
                    annotations = emptyList()
                )
            )
        )

        val responseStatus = responseStatusProcessor.process(methodMetadata)

        assertThat(responseStatus).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    internal fun `should extract ResponseStatus with value attribute`() {

        val methodMetadata = MethodMetadata(
            name = "testMethod",
            visibility = Visibility.PUBLIC,
            parameters = emptyList(),
            annotations = listOf(
                AnnotationMetadata(
                    name = ResponseStatus::class.qualifiedName!!,
                    attributes = mapOf(
                        "value" to HttpStatus.CREATED.name
                    ),
                    annotations = emptyList()
                )
            )
        )

        val responseStatus = responseStatusProcessor.process(methodMetadata)

        assertThat(responseStatus).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    internal fun `should extract ResponseStatus without code and value attributes`() {

        val methodMetadata = MethodMetadata(
            name = "testMethod",
            visibility = Visibility.PUBLIC,
            parameters = emptyList(),
            annotations = listOf(
                AnnotationMetadata(
                    name = ResponseStatus::class.qualifiedName!!,
                    attributes = emptyMap(),
                    annotations = emptyList()
                )
            )
        )

        val responseStatus = responseStatusProcessor.process(methodMetadata)

        assertThat(responseStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @Test
    internal fun `should not extract ResponseStatus when method is not annotated`() {

        val methodMetadata = MethodMetadata(
            name = "testMethod",
            visibility = Visibility.PUBLIC,
            parameters = emptyList(),
            annotations = emptyList()
        )

        val responseStatus = responseStatusProcessor.process(methodMetadata)

        assertThat(responseStatus).isNull()
    }
}
