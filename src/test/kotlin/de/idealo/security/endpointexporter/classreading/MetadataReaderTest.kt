package de.idealo.security.endpointexporter.classreading

import de.idealo.security.endpointexporter.classreading.type.Visibility
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.PathResource
import java.nio.file.Path

internal class MetadataReaderTest {

    // compiled version of https://github.com/DarkAtra/spring-security-demo/blob/c46c40d9d016945213fa60af13fe0ba0862d1fd7/src/main/java/de/darkatra/springsecuritydemo/HelloController.java
    private val testControllerClass = Path.of("src/test/resources/classreading/HelloController.class")

    @Test
    internal fun shouldReadClassMetadata() {

        val metadataReader = MetadataReader(PathResource(testControllerClass))

        val classMetadata = metadataReader.classMetadata

        assertThat(classMetadata).isNotNull
        assertThat(classMetadata.name).isEqualTo("de.darkatra.springsecuritydemo.HelloController")
        assertThat(classMetadata.methods).hasSize(3)

        // find the first method that is not a constructor or static initialization block
        val methodMetadata = classMetadata.methods.first { it.name != "<init>" && it.name != "<cinit>" }
        assertThat(methodMetadata.name).isEqualTo("hello")
        assertThat(methodMetadata.visibility).isEqualTo(Visibility.PUBLIC)
        assertThat(methodMetadata.getAnnotations()).hasSize(2)
        assertThat(methodMetadata.parameters).hasSize(1)

        val firstMethodAnnotation = methodMetadata.getAnnotations()[0]
        assertThat(firstMethodAnnotation.name).isEqualTo("org.springframework.web.bind.annotation.GetMapping")
        assertThat(firstMethodAnnotation.attributes).hasSize(1)
        assertThat(firstMethodAnnotation.getStringArray("value")).containsExactly("/hello")

        val secondMethodAnnotation = methodMetadata.getAnnotations()[1]
        assertThat(secondMethodAnnotation.name).isEqualTo("org.springframework.security.access.prepost.PreAuthorize")
        assertThat(secondMethodAnnotation.attributes).hasSize(1)
        assertThat(secondMethodAnnotation.getString("value")).isEqualTo("hasAuthority('SCOPE_SECURITY_DEMO:GET_HELLO')")

        val parameterMetadata = methodMetadata.parameters[0]
        assertThat(parameterMetadata.name).isEqualTo("jwt")
        assertThat(parameterMetadata.type).isEqualTo("org.springframework.security.oauth2.jwt.Jwt")
        assertThat(parameterMetadata.getAnnotations()).hasSize(1)

        val parameterAnnotationMetadata = parameterMetadata.getAnnotations()[0]
        assertThat(parameterAnnotationMetadata.name).isEqualTo("org.springframework.security.core.annotation.AuthenticationPrincipal")
        assertThat(parameterAnnotationMetadata.attributes).isEmpty()
    }
}
