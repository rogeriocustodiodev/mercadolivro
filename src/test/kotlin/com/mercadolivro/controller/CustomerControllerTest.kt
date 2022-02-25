package com.mercadolivro.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mercadolivro.controller.request.PostCustomerRequest
import com.mercadolivro.controller.request.PutCustomerRequest
import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.helper.buildCustomer
import com.mercadolivro.repository.CustomerRepository
import com.mercadolivro.security.UserCustomDetails
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.random.Random

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@ActiveProfiles("test")
@WithMockUser(username = "test", password = "test", roles = ["CUSTOMER"])
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

    @Nested
    inner class `find all customers` {
        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `should return all customers when get all`() {
            val customer1 = customerRepository.save(buildCustomer())
            val customer2 = customerRepository.save(buildCustomer())

            mockMvc.perform(
                get("/customers")
            )
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
        @WithMockUser(roles = ["ADMIN"])
        fun `should filter by name all customers when get all`() {
            val customer1 = customerRepository.save(buildCustomer(name = "Rogerio"))
            customerRepository.save(buildCustomer(name = "Stefanny"))

            mockMvc.perform(
                get("/customers?name=Ro")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("items.length()").value(1))
                .andExpect(jsonPath("items[0].id").value(customer1.id))
                .andExpect(jsonPath("items[0].name").value(customer1.name))
                .andExpect(jsonPath("items[0].email").value(customer1.email))
                .andExpect(jsonPath("items[0].status").value(customer1.status.name))
        }

        @Test
        fun `should return forbidden when user try call get and he is not admin`() {
            mockMvc.perform(
                get("/customers"))
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.httpCode").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.internalCode").value("ML-000"))
        }
    }

    @Nested
    inner class `create customer` {
        @Test
        fun `should create customer`() {
            val request = PostCustomerRequest("fake name", "${Random.nextInt()}@fakeemail.com", "123456")

            mockMvc.perform(
                post("/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)

            val customers = customerRepository.findAll().toList()
            assertEquals(1, customers.size)
            assertEquals(request.name, customers[0].name)
            assertEquals(request.email, customers[0].email)
        }

        @Test
        fun `should throw error when create customer has invalid information`() {
            val request = PostCustomerRequest("", "${Random.nextInt()}@fakeemail.com", "123456")

            mockMvc.perform(
                post("/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnprocessableEntity)
                .andExpect(jsonPath("$.httpCode").value(422))
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.internalCode").value("ML-001"))
        }
    }

    @Nested
    inner class `find customer by id` {
        @Test
        fun `should get user by id when user has the same id`() {
            val customer = customerRepository.save(buildCustomer())

            mockMvc.perform(
                get("/customers/${customer.id}")
                    .with(user(UserCustomDetails(customer)))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(customer.id))
                .andExpect(jsonPath("$.name").value(customer.name))
                .andExpect(jsonPath("$.email").value(customer.email))
                .andExpect(jsonPath("$.status").value(customer.status.name))
        }

        @Test
        fun `should return forbidden when user has different id and is not admin`() {
            val customer = customerRepository.save(buildCustomer())

            mockMvc.perform(
                get("/customers/0")
                    .with(user(UserCustomDetails(customer))))
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.httpCode").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.internalCode").value("ML-000"))
        }

        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `should get user by id when is admin`() {
            val customer = customerRepository.save(buildCustomer())

            mockMvc.perform(
                get("/customers/${customer.id}")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(customer.id))
                .andExpect(jsonPath("$.name").value(customer.name))
                .andExpect(jsonPath("$.email").value(customer.email))
                .andExpect(jsonPath("$.status").value(customer.status.name))
        }
    }

    @Nested
    inner class `update customer` {
        @Test
        fun `should update customer when user has the same id`() {
            val customer = customerRepository.save(buildCustomer())
            val request = PutCustomerRequest("fake name updated", "emailupdated@fakeemail.com")

            mockMvc.perform(
                put("/customers/${customer.id}")
                    .with(user(UserCustomDetails(customer)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNoContent)

            val customers = customerRepository.findAll().toList()
            assertEquals(1, customers.size)
            assertEquals(request.name, customers[0].name)
            assertEquals(request.email, customers[0].email)
        }

        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `should return not found when update a customer that not exists`() {
            val id = Random.nextInt()
            val request = PutCustomerRequest("fake name updated", "emailupdated@fakeemail.com")

            mockMvc.perform(
                put("/customers/${id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Customer [${id}] not exists"))
                .andExpect(jsonPath("$.internalCode").value("ML-201"))
        }

        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `should update customer when user is admin`() {
            val customer = customerRepository.save(buildCustomer())
            val request = PutCustomerRequest("fake name updated", "emailupdated@fakeemail.com")

            mockMvc.perform(
                put("/customers/${customer.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNoContent)

            val customers = customerRepository.findAll().toList()
            assertEquals(1, customers.size)
            assertEquals(request.name, customers[0].name)
            assertEquals(request.email, customers[0].email)
        }

        @Test
        fun `should throw error when update customer has invalid information`() {
            val request = PutCustomerRequest("", "emailupdated@fakeemail.com")

            mockMvc.perform(
                put("/customers/${Random.nextInt()}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity)
                .andExpect(jsonPath("$.httpCode").value(422))
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.internalCode").value("ML-001"))
        }

        @Test
        fun `should return forbidden when user has different id and is not admin`() {
            val customer = customerRepository.save(buildCustomer())
            val request = PutCustomerRequest("fake customer", "emailupdated@fakeemail.com")

            mockMvc.perform(
                put("/customers/${Random.nextInt()}")
                    .with(user(UserCustomDetails(customer)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.httpCode").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.internalCode").value("ML-000"))
        }
    }

    @Nested
    inner class `delete customer` {
        @Test
        fun `should delete customer when user has the same id`() {
            val customer = customerRepository.save(buildCustomer())

            mockMvc.perform(
                delete("/customers/${customer.id}")
                    .with(user(UserCustomDetails(customer))))
                .andExpect(status().isNoContent)

            val customerDeleted = customerRepository.findById(customer.id!!)
            assertEquals(CustomerStatus.INATIVO, customerDeleted.get().status)
        }

        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `should delete customer when user is admin`() {
            val customer = customerRepository.save(buildCustomer())

            mockMvc.perform(
                delete("/customers/${customer.id}"))
                .andExpect(status().isNoContent)

            val customerDeleted = customerRepository.findById(customer.id!!)
            assertEquals(CustomerStatus.INATIVO, customerDeleted.get().status)
        }

        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `should return not found when delete a customer that not exists`() {
            val id = Random.nextInt()

            mockMvc.perform(
                delete("/customers/${id}"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Customer [${id}] not exists"))
                .andExpect(jsonPath("$.internalCode").value("ML-201"))
        }

        @Test
        fun `should return forbidden when user has different id and is not admin`() {
            val customer = customerRepository.save(buildCustomer())

            mockMvc.perform(
                delete("/customers/${Random.nextInt()}")
                    .with(user(UserCustomDetails(customer))))
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.httpCode").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.internalCode").value("ML-000"))
        }
    }

}