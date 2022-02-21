package com.mercadolivro.service

import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.enums.Role
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.CustomerRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*
import kotlin.random.Random.Default.nextInt

@ExtendWith(MockKExtension::class)
class CustomerServiceTest {

    @MockK
    private lateinit var customerRepository: CustomerRepository

    @MockK
    private lateinit var bookService: BookService

    @MockK
    private lateinit var bCrypt: BCryptPasswordEncoder

    @InjectMockKs
    private lateinit var customerService: CustomerService

    @Test
    fun `should return all customers`() {
        // arrange
        val fakeCustomers = PageImpl(listOf(buildCustomer(), buildCustomer()))
        every { customerRepository.findAll(any()) } returns fakeCustomers

        // act
        val customers = customerService.getAll(null, Pageable.ofSize(1))

        // assert
        assertEquals(fakeCustomers, customers)
        verify(exactly = 0) { customerRepository.findByNameContaining(any(), any())}
        verify(exactly = 1) { customerRepository.findAll(any()) }
    }

    @Test
    fun `should return customers when name is informed`() {
        // arrange
        val name = UUID.randomUUID().toString()
        val fakeCustomers = PageImpl(listOf(buildCustomer(), buildCustomer()))
        every { customerRepository.findByNameContaining(name, any()) } returns fakeCustomers

        // act
        val customers = customerService.getAll(name, Pageable.ofSize(1))

        // assert
        assertEquals(fakeCustomers, customers)
        verify(exactly = 0) { customerRepository.findAll(any()) }
        verify(exactly = 1) { customerRepository.findByNameContaining(name, any())}
    }

    @Test
    fun `should create customer and encrypt password`() {
        // Arrange
        val initialPassword = Random().nextInt().toString()
        val fakeCustomer = buildCustomer(password = initialPassword)
        val fakePassword = UUID.randomUUID().toString()
        val fakeCustomerWithEncryptedPassword = fakeCustomer.copy(password = fakePassword)
        every { bCrypt.encode(initialPassword) } returns fakePassword
        every { customerRepository.save(fakeCustomerWithEncryptedPassword) } returns fakeCustomer

        // Act
        customerService.create(fakeCustomer)

        // Assert
        verify(exactly = 1) { customerRepository.save(fakeCustomerWithEncryptedPassword) }
        verify(exactly = 1) { bCrypt.encode(initialPassword) }
    }

    @Test
    fun `should return customer by id`() {
        // Arrange
        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)
        every { customerRepository.findById(id) } returns Optional.of(fakeCustomer)

        // Act
        val customer = customerService.findById(id)

        // Assert
        assertEquals(fakeCustomer, customer)
        verify(exactly = 1) { customerRepository.findById(id) }
    }

    @Test
    fun `should throw exception when customer not found`() {
        // Arrange
        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)
        every { customerRepository.findById(id) } returns Optional.empty()

        // Act
        val error = assertThrows<NotFoundException> {
            customerService.findById(id)
        }

        // Assert
        assertEquals("Customer [${id}] not exists", error.message)
        assertEquals("ML-201", error.errorCode)
        verify(exactly = 1) { customerRepository.findById(id) }
    }

    private fun buildCustomer(
       id: Int? = null,
       name: String = "customer name",
       email: String = "${UUID.randomUUID()}@email.com",
       password: String = "password"
    ) = CustomerModel(
        id = id,
        name = name,
        email = email,
        status = CustomerStatus.ATIVO,
        password = password,
        roles = setOf(Role.CUSTOMER)
    )

}