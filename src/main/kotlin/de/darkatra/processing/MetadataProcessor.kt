package de.darkatra.processing

import de.darkatra.classreading.type.Metadata

interface MetadataProcessor<I : Metadata, O> {

    fun process(metadata: I): List<O>
}
