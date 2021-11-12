package de.idealo.security.endpointexporter.classreading.type

data class ParameterMetadata(
    val name: String,
    val type: String,
    private val annotations: List<AnnotationMetadata>
) : AnnotatedTypeMetadata {

    class Builder(
        var name: String? = null,
        var type: String? = null,
        private val annotations: MutableList<AnnotationMetadata> = mutableListOf()
    ) {

        fun addAnnotation(annotation: AnnotationMetadata) {
            annotations.add(annotation)
        }

        fun build(): ParameterMetadata {
            return ParameterMetadata(
                name = name!!,
                type = type!!,
                annotations = annotations
            )
        }
    }

    override fun getAnnotations(): List<AnnotationMetadata> = annotations
}
