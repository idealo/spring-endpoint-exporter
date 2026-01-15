package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import de.idealo.security.endpointexporter.classreading.type.MethodMetadata
import de.idealo.security.endpointexporter.classreading.type.ParameterMetadata
import de.idealo.security.endpointexporter.classreading.type.Visibility
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.web.bind.annotation.RequestHeader

internal class RequestHeaderProcessorTest {

    private val requestHeaderProcessor = RequestHeaderProcessor()

    @Test
    internal fun `should extract RequestHeader with explicit header name by name attribute`() {

        val name = "header-name"
        val type = "java.lang.String"
        val methodMetadata = createTestMethod("name", type, name, null, null)

        val requestParameters = requestHeaderProcessor.process(methodMetadata)

        assertThat(requestParameters)
            .hasSize(1)
            .extracting(RequestMapping.RequestHeader::name, RequestMapping.RequestHeader::type)
            .containsExactly(tuple(name, type))
    }

    @Test
    internal fun `should extract RequestHeader with implicit header name by parameter name`() {

        val paramName = "parameterName"
        val type = "java.lang.Long"
        val methodMetadata = createTestMethod(paramName, type, null, null, null)

        val requestParameters = requestHeaderProcessor.process(methodMetadata)

        assertThat(requestParameters)
            .hasSize(1)
            .extracting(RequestMapping.RequestHeader::name, RequestMapping.RequestHeader::type)
            .containsExactly(tuple(paramName, type))
    }

    /**
     * with default value 'required' is whatever is set by the according attribute.
     */
    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    internal fun `should extract RequestHeader with explicit required attribute`(required: Boolean) {

        val name = "header-name"
        val methodMetadata = createTestMethod("name", "java.lang.String", name, required, "default")

        val requestParameters = requestHeaderProcessor.process(methodMetadata)

        assertThat(requestParameters)
            .hasSize(1)
            .extracting(RequestMapping.RequestHeader::name, RequestMapping.RequestHeader::required)
            .containsExactly(tuple(name, required))
    }

    /**
     * with default value 'required' is false if not set at all.
     */
    @Test
    internal fun `should extract RequestHeader with implicit required attribute set to false because of existing default value`() {

        val name = "header-name"
        val methodMetadata = createTestMethod("name", "java.lang.String", name, null, "default")

        val requestParameters = requestHeaderProcessor.process(methodMetadata)

        assertThat(requestParameters)
            .hasSize(1)
            .extracting(RequestMapping.RequestHeader::name, RequestMapping.RequestHeader::required)
            .containsExactly(tuple(name, false))
    }

    /**
     * without default value 'required' is always true.
     */
    @Test
    internal fun `should extract RequestHeader with implicit required attribute set to true because of missing default value`() {
        val name = "header-name"
        val methodMetadata = createTestMethod("name", "java.lang.String", name, null, null)

        val requestParameters = requestHeaderProcessor.process(methodMetadata)

        assertThat(requestParameters)
            .hasSize(1)
            .extracting(RequestMapping.RequestHeader::name, RequestMapping.RequestHeader::required)
            .containsExactly(tuple(name, true))
    }

    @Test
    internal fun `should extract multiple (2) RequestHeaders`() {

        val headerName1 = "stringHeader"
        val headerType1 = "java.lang.String"
        val headerName2 = "doubleHeader"
        val headerType2 = "java.lang.Double"

        val methodMetadata = MethodMetadata(
            name = "testMethod",
            visibility = Visibility.PUBLIC,
            parameters = listOf(
                ParameterMetadata(
                    name = headerName1,
                    type = headerType1,
                    annotations = listOf(
                        AnnotationMetadata(
                            name = RequestHeader::class.qualifiedName!!,
                            attributes = mapOf(
                                "name" to headerName1
                            ),
                            annotations = emptyList()
                        )
                    )
                ),
                ParameterMetadata(
                    name = headerName2,
                    type = headerType2,
                    annotations = listOf(
                        AnnotationMetadata(
                            name = RequestHeader::class.qualifiedName!!,
                            attributes = mapOf(
                                "name" to headerName2
                            ),
                            annotations = emptyList()
                        )
                    )
                )
            ),
            annotations = emptyList()
        )

        val requestParameters = requestHeaderProcessor.process(methodMetadata)

        assertThat(requestParameters)
            .hasSize(2)
            .extracting(RequestMapping.RequestHeader::name, RequestMapping.RequestHeader::type)
            .containsExactlyInAnyOrder(
                tuple(headerName1, headerType1),
                tuple(headerName2, headerType2)
            )
    }

    private fun createTestMethod(
        paramName: String,
        paramType: String,
        headerName: String?,
        headerRequired: Boolean?,
        headerDefaultValue: String?
    ): MethodMetadata {
        return MethodMetadata(
            name = "testMethod",
            visibility = Visibility.PUBLIC,
            parameters = listOf(
                ParameterMetadata(
                    name = paramName,
                    type = paramType,
                    annotations = listOf(
                        AnnotationMetadata(
                            name = RequestHeader::class.qualifiedName!!,
                            attributes = buildMap {
                                if (headerName != null) {
                                    put("name", headerName)
                                }
                                if (headerRequired != null) {
                                    put("required", headerRequired)
                                }
                                if (headerDefaultValue != null) {
                                    put("defaultValue", headerDefaultValue)
                                }
                            },
                            annotations = emptyList()
                        )
                    )
                )
            ),
            annotations = emptyList()
        )
    }
}
