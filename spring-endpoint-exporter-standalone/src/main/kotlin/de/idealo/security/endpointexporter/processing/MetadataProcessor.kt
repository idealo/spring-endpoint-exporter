package de.idealo.security.endpointexporter.processing

import de.idealo.security.endpointexporter.classreading.type.Metadata

interface MetadataProcessor<I : Metadata, O> {

    fun process(metadata: I): O
}
