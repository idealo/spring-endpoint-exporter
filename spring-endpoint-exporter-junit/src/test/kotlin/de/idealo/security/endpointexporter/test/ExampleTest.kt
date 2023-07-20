package de.idealo.security.endpointexporter.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringEndpointExporterExtension::class)
class ExampleTest(
    @Autowired
    private var mockMvc: MockMvc
) {

    @Test
    fun `should extract request mappings`() {

        mockMvc.perform(get("/persons"))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
    }
}
