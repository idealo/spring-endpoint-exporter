package de.darkatra.classreading.type

interface AnnotatedTypeMetadata : Metadata {

    fun getAnnotations(): List<AnnotationMetadata>

    fun isAnnotated(annotationName: String): Boolean {
        return getAnnotations().any { annotationMetadata ->
            annotationMetadata.name == annotationName
        }
    }

    fun getAnnotation(annotationName: String): AnnotationMetadata? {
        return getAnnotations().find { annotationMetadata ->
            annotationMetadata.name == annotationName
        }
    }
}
