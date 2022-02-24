package com.mercadolivro.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mercadolivro.controller.request.PostCustomerRequest
import com.mercadolivro.helper.buildCustomer
import com.mercadolivro.repository.CustomerRepository
import com.mercadolivro.security.UserCustomDetails
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.random.Random

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@ActiveProfiles("test")
@WithMockUser(username = "test", password = "test", roles = ["ADMIN"])
class CustomerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() = customerRepository.deleteAll()

    @AfterEach
    fun tearDown() = customerRepository.deleteAll()

    @Test
    fun `should return all customers when get all`() {
        val customer1 = customerRepository.save(buildCustomer())
        val customer2 = customerRepository.save(buildCustomer())

        mockMvc.perform(
            get("/customers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("items.length()").value(2))
            .andExpect(jsonPath("items[0].id").value(customer1.id))
            .andExpect(jsonPath("items[0].name").value(customer1.name))
            .andExpect(jsonPath("items[0].email").value(customer1.email))
            .andExpect(jsonPath("items[0].status").value(customer1.status.name))
            .andExpect(jsonPath("items[1].id").value(customer2.id))
            .andExpect(jsonPath("items[1].name").value(customer2.name))
            .andExpect(jsonPath("items[1].email").value(customer2.email))
            .andExpect(jsonPath("items[1].status").value(customer2.status.name))
    }

    @Test
    fun `should filter by name all customers when get all`() {
        val customer1 = customerRepository.save(buildCustomer(name = "Rogerio"))
        customerRepository.save(buildCustomer(name = "Stefanny"))

        mockMvc.perform(
            get("/customers?name=Ro"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("items.length()").value(1))
            .andExpect(jsonPath("items[0].id").value(customer1.id))
            .andExpect(jsonPath("items[0].name").value(customer1.name))
            .andExpect(jsonPath("items[0].email").value(customer1.email))
            .andExpect(jsonPath("items[0].status").value(customer1.status.name))
    }

    @Test
    fun `should create customer`() {
        val request = PostCustomerRequest(
            "fake name",
            "${Random.nextInt()}@fakeemail.com",
            "123456"
        )

        mockMvc.perform(
            post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)

        val customers = customerRepository.findAll().toList()
        assertEquals(1, customers.size)
        assertEquals(request.name, customers[0].name)
        assertEquals(request.email, customers[0].email)
    }

    @Test
    fun `should get user by id when user has the same id`() {
        val customer = customerRepository.save(buildCustomer())

        mockMvc.perform(
            get("/customers/${customer.id}")
                .with(user(UserCustomDetails(customer))))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(customer.id))
            .andExpect(jsonPath("$.name").value(customer.name))
            .andExpect(jsonPath("$.email").value(customer.email))
            .andExpect(jsonPath("$.status").value(customer.status.name))
    }

    @Test
    fun `should return forbidden when user has different id`() {
        val customer = customerRepository.save(buildCustomer())

        mockMvc.perform(
            get("/customers/0")
                .with(user(UserCustomDetails(customer))))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.httpCode").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.internalCode").value("ML-000"))
    }

}