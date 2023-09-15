package de.idealo.security.endpointexporter.test

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * This controller is only used in tests.
 */
@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class PersonController {

    private val persons = setOf(
        Person(
            firstName = "John",
            lastName = "Doe"
        ),
        Person(
            firstName = "Jane",
            lastName = "Doe"
        )
    )

    @GetMapping("/persons")
    fun getPersons(): Set<Person> {
        return persons
    }

    @GetMapping("/persons/{firstName}", produces = [MediaType.APPLICATION_XML_VALUE])
    fun getPersonByFirstName(@PathVariable firstName: String): Person? {
        return persons.find { person -> person.firstName == firstName }
    }
}
