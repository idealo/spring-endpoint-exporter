package de.darkatra.classreading.type

data class AnnotationMetadata(
    val name: String,
    val attributes: Map<String, Any>,
    private val annotations: List<AnnotationMetadata>
) : AnnotatedTypeMetadata {

    fun getString(attributeName: String): String {
        return attributes[attributeName] as String
    }

    fun getBoolean(attributeName: String): Boolean {
        return attributes[attributeName] as Boolean
    }

    @Suppress("UNCHECKED_CAST")
    fun getStringArray(attributeName: String): Array<String> {
        return attributes[attributeName] as Array<String>
    }

    override fun getAnnotations(): List<AnnotationMetadata> = annotations
}
