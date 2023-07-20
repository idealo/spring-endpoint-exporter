package de.idealo.security.endpointexporter.classreading.type

data class AnnotationMetadata(
    val name: String,
    val attributes: Map<String, Any>,
    private val annotations: List<AnnotationMetadata>
) : AnnotatedTypeMetadata {

    fun getString(attributeName: String): String? {
        return attributes[attributeName] as String?
    }

    fun getBoolean(attributeName: String): Boolean? {
        return attributes[attributeName] as Boolean?
    }

    @Suppress("UNCHECKED_CAST")
    fun getStringArray(attributeName: String): Array<String> {
        val attribute = attributes[attributeName]
        return when (attribute) {
            is Iterable<*> -> (attribute.toList() as List<String>).toTypedArray()
            is Array<*> -> attribute
            else -> emptyArray<String>()
        } as Array<String>
    }

    override fun getAnnotations(): List<AnnotationMetadata> = annotations
}
