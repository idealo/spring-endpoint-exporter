package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.classreading.type.AnnotationMetadata
import de.idealo.security.endpointexporter.classreading.type.MethodMetadata
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus

@Component
class ResponseStatusProcessor : MetadataProcessor<MethodMetadata, HttpStatus?> {

    override fun process(metadata: MethodMetadata): HttpStatus? {

        return metadata.getAnnotation(ResponseStatus::class.qualifiedName!!)
            ?.let { getResponseStatus(it) }
    }

    private fun getResponseStatus(annotationMetadata: AnnotationMetadata): HttpStatus {
        return (annotationMetadata.getString("code") ?: annotationMetadata.getString("value"))
            ?.let { HttpStatus.valueOf(it) }
            ?: HttpStatus.INTERNAL_SERVER_ERROR
    }
}
