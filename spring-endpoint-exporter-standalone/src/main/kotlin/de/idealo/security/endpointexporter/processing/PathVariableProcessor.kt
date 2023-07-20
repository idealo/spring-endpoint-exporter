package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import de.idealo.security.endpointexporter.classreading.type.MethodMetadata
import de.idealo.security.endpointexporter.classreading.type.ParameterMetadata
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable

@Component
class PathVariableProcessor : MetadataProcessor<MethodMetadata, List<RequestMapping.PathVariable>> {

    override fun process(metadata: MethodMetadata): List<RequestMapping.PathVariable> {

        return metadata.parameters
            .filter { it.isAnnotated(PathVariable::class.qualifiedName!!) }
            .map { parameterMetadata ->
                val parameterAnnotationAttributes = parameterMetadata.getAnnotation(PathVariable::class.qualifiedName!!)!!

                RequestMapping.PathVariable(
                    name = getParameterName(parameterMetadata, parameterAnnotationAttributes),
                    type = parameterMetadata.type,
                    required = parameterAnnotationAttributes.getBoolean("required") ?: true
                )
            }
    }

    private fun getParameterName(parameterMetadata: ParameterMetadata, annotationMetadata: AnnotationMetadata): String {
        return annotationMetadata.getString("name")
            ?: annotationMetadata.getString("value")
            ?: parameterMetadata.name
    }
}
