package de.darkatra.processing

import de.darkatra.classreading.type.AnnotationMetadata
import de.darkatra.classreading.type.MethodMetadata
import de.darkatra.classreading.type.ParameterMetadata
import de.darkatra.processing.RequestMapping.PathVariable
import org.springframework.web.bind.annotation.PathVariable as PathVariableAnnotation

class PathVariableProcessor : MetadataProcessor<MethodMetadata, PathVariable> {

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
