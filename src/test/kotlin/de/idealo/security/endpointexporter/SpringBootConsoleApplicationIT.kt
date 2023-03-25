package de.idealo.security.endpointexporter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.nio.charset.StandardCharsets
import kotlin.io.path.readText

@SpringBootTest(
    properties = [
        "exporter.scan-mode=FILE_SYSTEM",
        "exporter.input-path=target/test-classes/de/idealo/security/endpointexporter/test/",
        "exporter.output-path=target/out.json",
        "exporter.include-filters=de.idealo.*"
    ],
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class SpringBootConsoleApplicationIT(
    @Autowired
    private val exporterProperties: ExporterProperties
) {

    private val expectedOutput = javaClass.getResourceAsStream("/expected-output.json")!!.readAllBytes().toString(StandardCharsets.UTF_8)

    @Test
    fun `should correctly export endpoints in scan mode FILE_SYSTEM`() {

        val output = exporterProperties.outputPath.readText(StandardCharsets.UTF_8)

        assertThat(output).isEqualTo(expectedOutput)
    }
}
