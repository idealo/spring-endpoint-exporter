package de.idealo.security.endpointexporter.classreading.type

import java.util.Properties

class ApplicationMetadata(private val properties: Properties) : Metadata {

    fun getApplicationTitle() = properties.getProperty("Implementation-Title") ?: "n/a";

    fun getApplicationVersion() = properties.getProperty("Implementation-Version") ?: "n/a";
}
