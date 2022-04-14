package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import de.idealo.security.endpointexporter.classreading.type.MethodMetadata
import de.idealo.security.endpointexporter.classreading.type.ParameterMetadata
import de.idealo.security.endpointexporter.processing.RequestMapping.PathVariable
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable as PathVariableAnnotation

@Component
class PathVariableProcessor : MetadataProcessor<MethodMetadata, List<PathVariable>> {

    override fun process(metadata: MethodMetadata): List<PathVariable> {

        return metadata.parameters
            .filter { it.isAnnotated(PathVariableAnnotation::class.qualifiedName!!) }
            .map { parameterMetadata ->
                val parameterAnnotationAttributes = parameterMetadata.getAnnotation(PathVariableAnnotation::class.qualifiedName!!)!!

                PathVariable(
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
