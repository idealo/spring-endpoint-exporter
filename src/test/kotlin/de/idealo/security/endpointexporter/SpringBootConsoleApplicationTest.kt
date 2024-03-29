package de.idealo.security.endpointexporter

import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.nio.charset.StandardCharsets
import java.nio.file.Path
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
class SpringBootConsoleApplicationTest(
    @Autowired
    private val exporterProperties: ExporterProperties
) {

    private val expectedOutput = Path.of("target/test-classes/expected-output.json").readText(StandardCharsets.UTF_8)

    @Test
    fun `should correctly export endpoints in scan mode FILE_SYSTEM`() {

        val output = exporterProperties.outputPath.readText(StandardCharsets.UTF_8)

        JSONAssert.assertEquals(output, expectedOutput, true)
    }
}
