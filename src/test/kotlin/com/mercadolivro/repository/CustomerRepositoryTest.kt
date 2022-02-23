package com.mercadolivro.repository

import com.mercadolivro.helper.buildCustomer
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class CustomerRepositoryTest {

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Test
    fun `should return name containing`() {
        val rogerio = customerRepository.save(buildCustomer(name = "Rogerio"))
        val roberto = customerRepository.save(buildCustomer(name = "Roberto"))
        val stefanny = customerRepository.save(buildCustomer(name = "Stefanny"))
        val paola = customerRepository.save(buildCustomer(name = "Paola"))

        val customers = customerRepository.findByNameContaining("Ro", Pageable.ofSize(10))

        assertEquals(PageImpl(listOf(rogerio, roberto)).content, customers.content)
    }

}