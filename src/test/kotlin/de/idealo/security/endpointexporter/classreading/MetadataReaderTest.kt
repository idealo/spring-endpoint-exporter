package de.idealo.security.endpointexporter.classreading

import de.idealo.security.endpointexporter.classreading.type.Visibility
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.FileSystemResource
import java.nio.file.Path

internal class MetadataReaderTest {

    /**
     * @see de.idealo.security.endpointexporter.test.PersonController
     */
    private val testControllerClass = Path.of("target/test-classes/de/idealo/security/endpointexporter/test/PersonController.class")

    @Test
    internal fun `should read class metadata`() {

        val metadataReader = MetadataReader(FileSystemResource(testControllerClass))

        val classMetadata = metadataReader.classMetadata

        assertThat(classMetadata).isNotNull
        assertThat(classMetadata.name).isEqualTo("de.idealo.security.endpointexporter.test.PersonController")
        assertThat(classMetadata.methods).hasSize(4)

        // find all methods that are not constructors or static initialization blocks
        val methods = classMetadata.methods.filter { it.name != "<init>" && it.name != "<cinit>" }

        val getPersonsMethod = methods.first()
        assertThat(getPersonsMethod.name).isEqualTo("getPersons")
        assertThat(getPersonsMethod.visibility).isEqualTo(Visibility.PUBLIC)
        assertThat(getPersonsMethod.getAnnotations()).hasSize(1)
        assertThat(getPersonsMethod.parameters).isEmpty()

        val getPersonsGetMappingAnnotation = getPersonsMethod.getAnnotations()[0]
        assertThat(getPersonsGetMappingAnnotation.name).isEqualTo("org.springframework.web.bind.annotation.GetMapping")
        assertThat(getPersonsGetMappingAnnotation.attributes).hasSize(1)
        assertThat(getPersonsGetMappingAnnotation.getStringArray("value")).containsExactly("/persons")

        val getPersonByFirstNameMethod = methods.drop(1).first()
        assertThat(getPersonByFirstNameMethod.name).isEqualTo("getPersonByFirstName")
        assertThat(getPersonByFirstNameMethod.visibility).isEqualTo(Visibility.PUBLIC)
        assertThat(getPersonByFirstNameMethod.getAnnotations()).hasSize(1)
        assertThat(getPersonByFirstNameMethod.parameters).hasSize(1)

        val getPersonByFirstNameParameter = getPersonByFirstNameMethod.parameters[0]
        assertThat(getPersonByFirstNameParameter.name).isEqualTo("firstName")
        assertThat(getPersonByFirstNameParameter.type).isEqualTo("java.lang.String")
        assertThat(getPersonByFirstNameParameter.getAnnotations()).hasSize(1)

        val getPersonByFirstNameParameterAnnotation = getPersonByFirstNameParameter.getAnnotations()[0]
        assertThat(getPersonByFirstNameParameterAnnotation.name).isEqualTo("org.springframework.web.bind.annotation.PathVariable")
        assertThat(getPersonByFirstNameParameterAnnotation.attributes).isEmpty()

        val doSomethingWithPersonsMethod = methods.last()
        assertThat(doSomethingWithPersonsMethod.name).isEqualTo("doSomethingWithPersons")
        assertThat(doSomethingWithPersonsMethod.visibility).isEqualTo(Visibility.PUBLIC)
        assertThat(doSomethingWithPersonsMethod.getAnnotations()).hasSize(1)
        assertThat(doSomethingWithPersonsMethod.parameters).isEmpty()

        val doSomethingWithPersonsMethodPostMappingAnnotation = doSomethingWithPersonsMethod.getAnnotations()[0]
        assertThat(doSomethingWithPersonsMethodPostMappingAnnotation.name).isEqualTo("org.springframework.web.bind.annotation.PostMapping")
        assertThat(doSomethingWithPersonsMethodPostMappingAnnotation.attributes).hasSize(1)
        assertThat(doSomethingWithPersonsMethodPostMappingAnnotation.getStringArray("value")).containsExactly("/persons")
    }
}
