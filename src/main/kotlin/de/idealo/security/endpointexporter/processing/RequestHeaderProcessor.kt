package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import de.idealo.security.endpointexporter.classreading.type.MethodMetadata
import de.idealo.security.endpointexporter.classreading.type.ParameterMetadata
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestHeader

@Component
class RequestHeaderProcessor : MethodMetadataProcessor<RequestMapping.RequestHeader> {

    override fun process(metadata: MethodMetadata): List<RequestMapping.RequestHeader> {

        return metadata.parameters
            .filter { it.isAnnotated(RequestHeader::class.qualifiedName!!) }
            .map { parameterMetadata ->
                val parameterAnnotationAttributes = parameterMetadata.getAnnotation(RequestHeader::class.qualifiedName!!)!!
                val defaultValue = parameterAnnotationAttributes.getString("defaultValue")

                RequestMapping.RequestHeader(
                    name = getParameterName(parameterMetadata, parameterAnnotationAttributes),
                    type = parameterMetadata.type,
                    required = parameterAnnotationAttributes.getBoolean("required") ?: true || defaultValue == null
                )
            }
    }

    private fun getParameterName(parameterMetadata: ParameterMetadata, annotationMetadata: AnnotationMetadata): String {
        return annotationMetadata.getString("name")
            ?: annotationMetadata.getString("value")
            ?: parameterMetadata.name
    }
}
