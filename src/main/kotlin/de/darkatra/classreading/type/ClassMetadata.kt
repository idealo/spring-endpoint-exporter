package de.darkatra.classreading.type

data class ClassMetadata(
    val name: String,
    private val annotations: List<AnnotationMetadata>,
    val methods: List<MethodMetadata>
) : AnnotatedTypeMetadata {

    override fun getAnnotations(): List<AnnotationMetadata> = annotations

    fun getAnnotatedMethods(annotationName: String): List<MethodMetadata> {
        return methods.filter { methodMetadata ->
            methodMetadata.isAnnotated(annotationName)
        }
    }
}
