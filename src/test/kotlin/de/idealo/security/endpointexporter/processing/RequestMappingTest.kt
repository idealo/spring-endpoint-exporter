package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.processing.RequestMapping.PathVariable
import de.idealo.security.endpointexporter.processing.RequestMapping.RequestParameter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.util.pattern.PathPatternParser

internal class RequestMappingTest {

    private val pathPatternParser = PathPatternParser()

    @Test
    internal fun `should combine RequestMappings`() {

        val classLevelRequestMapping = RequestMapping(
            urlPattern = pathPatternParser.parse("/test"),
            httpMethods = emptySet(),
            responseStatus = HttpStatus.OK,
            requestParameters = emptyList(),
            pathVariables = emptyList(),
            requestHeaders = emptyList(),
            consumes = listOf(MediaType.ALL_VALUE),
            produces = listOf(MediaType.ALL_VALUE),
            declaringClassName = "TestClass",
            methodName = null
        )

        val methodLevelRequestMapping = RequestMapping(
            urlPattern = pathPatternParser.parse("/{testId}"),
            httpMethods = setOf(HttpMethod.POST),
            responseStatus = HttpStatus.CREATED,
            requestParameters = listOf(
                RequestParameter(
                    name = "testQueryParam",
                    type = "java.lang.String",
                    required = true,
                    defaultValue = null
                )
            ),
            pathVariables = listOf(
                PathVariable(
                    name = "testId",
                    type = "java.lang.String",
                    required = true
                )
            ),
            requestHeaders = emptyList(),
            consumes = listOf(MediaType.APPLICATION_JSON_VALUE),
            produces = listOf(MediaType.ALL_VALUE),
            declaringClassName = "TestClass",
            methodName = "testMethod"
        )

        val requestMapping = classLevelRequestMapping.combine(methodLevelRequestMapping)

        assertThat(requestMapping.urlPattern.patternString).isEqualTo("/test/{testId}")
        assertThat(requestMapping.httpMethods).containsExactly(HttpMethod.POST)
        assertThat(requestMapping.responseStatus).isEqualTo(HttpStatus.CREATED)
        assertThat(requestMapping.requestParameters).hasSize(1)
        assertThat(requestMapping.requestParameters[0].name).isEqualTo("testQueryParam")
        assertThat(requestMapping.requestParameters[0].type).isEqualTo("java.lang.String")
        assertThat(requestMapping.requestParameters[0].required).isTrue
        assertThat(requestMapping.requestParameters[0].defaultValue).isNull()
        assertThat(requestMapping.pathVariables).hasSize(1)
        assertThat(requestMapping.pathVariables[0].name).isEqualTo("testId")
        assertThat(requestMapping.pathVariables[0].type).isEqualTo("java.lang.String")
        assertThat(requestMapping.pathVariables[0].required).isTrue
        assertThat(requestMapping.consumes).containsExactly(MediaType.APPLICATION_JSON_VALUE)
        assertThat(requestMapping.produces).containsExactly(MediaType.ALL_VALUE)
        assertThat(requestMapping.declaringClassName).isEqualTo("TestClass")
        assertThat(requestMapping.methodName).isEqualTo("testMethod")
    }

    @Test
    internal fun `should normalize RequestMapping without explicit httpMethods`() {

        val requestMapping = RequestMapping(
            urlPattern = pathPatternParser.parse("/test"),
            httpMethods = emptySet(),
            responseStatus = HttpStatus.OK,
            requestParameters = emptyList(),
            pathVariables = emptyList(),
            requestHeaders = emptyList(),
            consumes = listOf(MediaType.ALL_VALUE),
            produces = listOf(MediaType.ALL_VALUE),
            declaringClassName = "TestClass",
            methodName = "testMethod"
        )

        val normalizedRequestMapping = requestMapping.normalize()

        assertThat(normalizedRequestMapping.urlPattern.patternString).isEqualTo("/test")
        assertThat(normalizedRequestMapping.httpMethods).containsExactly(*HttpMethod.values())
        assertThat(normalizedRequestMapping.responseStatus).isEqualTo(HttpStatus.OK)
        assertThat(normalizedRequestMapping.requestParameters).isEmpty()
        assertThat(normalizedRequestMapping.pathVariables).isEmpty()
        assertThat(normalizedRequestMapping.consumes).containsExactly(MediaType.ALL_VALUE)
        assertThat(normalizedRequestMapping.produces).containsExactly(MediaType.ALL_VALUE)
        assertThat(normalizedRequestMapping.declaringClassName).isEqualTo("TestClass")
        assertThat(normalizedRequestMapping.methodName).isEqualTo("testMethod")
    }

    @Test
    internal fun `should normalize RequestMapping with explicit httpMethods`() {

        val requestMapping = RequestMapping(
            urlPattern = pathPatternParser.parse("/test"),
            httpMethods = setOf(
                HttpMethod.GET,
                HttpMethod.POST
            ),
            responseStatus = HttpStatus.OK,
            requestParameters = emptyList(),
            pathVariables = emptyList(),
            requestHeaders = emptyList(),
            consumes = listOf(MediaType.ALL_VALUE),
            produces = listOf(MediaType.ALL_VALUE),
            declaringClassName = "TestClass",
            methodName = "testMethod"
        )

        val normalizedRequestMapping = requestMapping.normalize()

        assertThat(normalizedRequestMapping.urlPattern.patternString).isEqualTo("/test")
        assertThat(normalizedRequestMapping.httpMethods).containsExactly(HttpMethod.GET, HttpMethod.POST)
        assertThat(normalizedRequestMapping.responseStatus).isEqualTo(HttpStatus.OK)
        assertThat(normalizedRequestMapping.requestParameters).isEmpty()
        assertThat(normalizedRequestMapping.pathVariables).isEmpty()
        assertThat(normalizedRequestMapping.consumes).containsExactly(MediaType.ALL_VALUE)
        assertThat(normalizedRequestMapping.produces).containsExactly(MediaType.ALL_VALUE)
        assertThat(normalizedRequestMapping.declaringClassName).isEqualTo("TestClass")
        assertThat(normalizedRequestMapping.methodName).isEqualTo("testMethod")
    }

    @Test
    internal fun `should combine media types correctly`() {

        val classLevelRequestMapping = RequestMapping(
            urlPattern = pathPatternParser.parse("/test"),
            httpMethods = emptySet(),
            responseStatus = HttpStatus.OK,
            requestParameters = emptyList(),
            pathVariables = emptyList(),
            requestHeaders = emptyList(),
            consumes = emptyList(),
            produces = listOf(MediaType.ALL_VALUE),
            declaringClassName = "TestClass",
            methodName = null
        )

        val methodLevelRequestMapping = RequestMapping(
            urlPattern = pathPatternParser.parse("/{testId}"),
            httpMethods = emptySet(),
            responseStatus = HttpStatus.OK,
            requestParameters = emptyList(),
            pathVariables = emptyList(),
            requestHeaders = emptyList(),
            consumes = listOf(MediaType.APPLICATION_JSON_VALUE),
            produces = emptyList(),
            declaringClassName = "TestClass",
            methodName = "testMethod"
        )

        val requestMapping = classLevelRequestMapping.combine(methodLevelRequestMapping)

        assertThat(requestMapping.consumes).containsExactly(MediaType.APPLICATION_JSON_VALUE)
        assertThat(requestMapping.produces).containsExactly(MediaType.ALL_VALUE)
    }
}
