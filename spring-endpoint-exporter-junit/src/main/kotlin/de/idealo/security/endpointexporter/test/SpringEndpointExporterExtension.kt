package de.idealo.security.endpointexporter.test

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import org.springframework.core.MethodParameter
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ValueConstants
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Constructor
import java.nio.file.Path
import kotlin.io.path.bufferedWriter

class SpringEndpointExporterExtension : InvocationInterceptor {

    companion object {
        private const val REQUEST_INFORMATION_CONTEXT_KEY = "idealo.spring-endpoint-exporter.request-information"

        // should use the `ExportService` from the standalone module
        private val objectMapper: ObjectMapper = ObjectMapper()
    }

    override fun <T> interceptTestClassConstructor(
        invocation: Invocation<T>,
        invocationContext: ReflectiveInvocationContext<Constructor<T>>,
        extensionContext: ExtensionContext
    ): T {

        val globalStore = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)

        // exit early if we already gathered request specific information in the global store
        if (globalStore.getOrDefault(REQUEST_INFORMATION_CONTEXT_KEY, MutableList::class.java, null) != null) {
            return super.interceptTestClassConstructor(invocation, invocationContext, extensionContext)
        }

        val testClass = extensionContext.requiredTestClass
        val requestMappingHandlerMapping = getRequestMappingHandlerMapping(extensionContext)

        val requestInformationList: MutableList<RequestInformation> = ArrayList()
        for ((mappingInfo, handlerMethod) in requestMappingHandlerMapping.handlerMethods.entries) {

            val urlPatterns = mappingInfo.patternValues
            val httpMethods = mappingInfo.methodsCondition.methods.map { it.name }.map(HttpMethod::valueOf).toSet()
            val requestParameters = getRequestParameters(handlerMethod.methodParameters)
            val pathVariables = getPathVariables(handlerMethod.methodParameters)

            for (urlPattern in urlPatterns) {
                requestInformationList.add(
                    RequestInformation(
                        urlPattern = urlPattern,
                        // according to the docs, if no httpMethods are specified, all methods are allowed
                        httpMethods = httpMethods.ifEmpty { HttpMethod.values().toSet() }.map { it.name() }.toSet(),
                        requestParameters = requestParameters,
                        pathVariables = pathVariables
                    )
                )
            }
        }

        globalStore.put(REQUEST_INFORMATION_CONTEXT_KEY, requestInformationList)

        // TODO: make the output path configurable
        val outputPath = Path.of("target", "idealo-spring-endpoint-exporter", testClass.name)
        if (outputPath.toFile().exists() || outputPath.toFile().mkdirs()) {
            val outputFile = outputPath.resolve("request-information.json")
            outputFile.bufferedWriter().use { writer ->
                objectMapper.writeValue(writer, requestInformationList)
            }
        } else {
            error("Could not export request-information for class '${testClass.name}'.")
        }

        return super.interceptTestClassConstructor(invocation, invocationContext, extensionContext)
    }

    private fun getRequestMappingHandlerMapping(extensionContext: ExtensionContext): RequestMappingHandlerMapping {
        val applicationContext = SpringExtension.getApplicationContext(extensionContext)
        return applicationContext.getBean(RequestMappingHandlerMapping::class.java)
    }

    private fun getRequestParameters(methodParameters: Array<MethodParameter>): List<RequestInformation.RequestParameter> {
        return methodParameters
            .filter { parameter -> parameter.hasParameterAnnotation(RequestParam::class.java) }
            .map { parameter ->
                val requestParam: RequestParam = parameter.getParameterAnnotation(RequestParam::class.java)!!
                RequestInformation.RequestParameter(
                    name = if ("" == requestParam.name) parameter.parameter.name else requestParam.name,
                    type = parameter.parameterType,
                    // the parameter is optional if a default value is set or the required flag is set to false explicitly
                    optional = !requestParam.required || ValueConstants.DEFAULT_NONE != requestParam.defaultValue,
                    // null is not allowed for annotations so the spring team uses an artificial arrangement of 16 unicode characters to simulate null
                    defaultValue = if (ValueConstants.DEFAULT_NONE == requestParam.defaultValue) null else requestParam.defaultValue
                )
            }
    }

    private fun getPathVariables(methodParameters: Array<MethodParameter>): List<RequestInformation.PathVariable> {
        return methodParameters
            .filter { parameter -> parameter.hasParameterAnnotation(PathVariable::class.java) }
            .map { parameter ->
                val pathVariable: PathVariable = parameter.getParameterAnnotation(PathVariable::class.java)!!
                RequestInformation.PathVariable(
                    name = if ("" == pathVariable.name) parameter.parameter.name else pathVariable.name,
                    type = parameter.parameterType
                )
            }
    }
}
