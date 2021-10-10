package de.darkatra

import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.classreading.MetadataReaderFactory
import org.springframework.core.type.filter.TypeFilter

/**
 * Allows to easily compose complex TypeFilters.
 */
fun interface CompositeTypeFilter : TypeFilter {

	fun andThen(typeFilter: TypeFilter): CompositeTypeFilter {
		return CompositeTypeFilter { metadataReader, metadataReaderFactory ->
			match(metadataReader, metadataReaderFactory) && typeFilter.match(metadataReader, metadataReaderFactory)
		}
	}

	fun orThen(typeFilter: TypeFilter): CompositeTypeFilter {
		return CompositeTypeFilter { metadataReader, metadataReaderFactory ->
			match(metadataReader, metadataReaderFactory) || typeFilter.match(metadataReader, metadataReaderFactory)
		}
	}

	fun negate(): CompositeTypeFilter {
		return CompositeTypeFilter { metadataReader, metadataReaderFactory ->
			!match(metadataReader, metadataReaderFactory)
		}
	}

	companion object {

		fun composeAnd(vararg array: TypeFilter): CompositeTypeFilter = composeAnd(array.asIterable())

		fun composeAnd(iterable: Iterable<TypeFilter>): CompositeTypeFilter {
			return of(iterable.reduce { result, current -> of(current).andThen(result) })
		}

		fun composeOr(vararg array: TypeFilter): CompositeTypeFilter = composeOr(array.asIterable())

		fun composeOr(iterable: Iterable<TypeFilter>): CompositeTypeFilter {
			return of(iterable.reduce { result, current -> of(current).orThen(result) })
		}

		fun of(typeFilter: TypeFilter): CompositeTypeFilter {
			return when (typeFilter) {
				is CompositeTypeFilter -> typeFilter
				else -> CompositeTypeFilter { metadataReader: MetadataReader, metadataReaderFactory: MetadataReaderFactory ->
					typeFilter.match(metadataReader, metadataReaderFactory)
				}
			}
		}
	}
}
