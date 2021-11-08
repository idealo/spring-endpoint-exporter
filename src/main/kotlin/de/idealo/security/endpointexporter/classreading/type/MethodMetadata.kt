package de.idealo.security.endpointexporter.classreading.type

data class MethodMetadata(
    val name: String,
    val visibility: Visibility,
    val parameters: List<ParameterMetadata>,
    private val annotations: List<AnnotationMetadata>
) : AnnotatedTypeMetadata {

    override fun getAnnotations(): List<AnnotationMetadata> = annotations
}
