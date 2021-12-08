package de.idealo.security.endpointexporter.classreading.type

import java.util.Properties

class ApplicationMetadata(private val properties: Properties) : Metadata {
    val applicationTitle: String = properties.getProperty("Implementation-Title") ?: "n/a"
    val applicationVersion = properties.getProperty("Implementation-Version") ?: "n/a";
}
