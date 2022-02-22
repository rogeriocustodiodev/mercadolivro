package com.mercadolivro.service

import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.helper.buildCustomer
import com.mercadolivro.repository.CustomerRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

@ExtendWith(MockKExtension::class)
class CustomerServiceTest {

    @MockK
    private lateinit var customerRepository: CustomerRepository

    @MockK
    private lateinit var bookService: BookService

    @MockK
    private lateinit var bCrypt: BCryptPasswordEncoder

    @InjectMockKs
    @SpyK
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

    @Test
    fun `should throw exception when customer to be updated not exists`() {
        // Arrange
        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)
        every { customerRepository.existsById(id) } returns false

        // Act
        val error = assertThrows<NotFoundException> {
            customerService.update(fakeCustomer)
        }

        // Assert
        assertEquals("Customer [${id}] not exists", error.message)
        assertEquals("ML-201", error.errorCode)
        verify(exactly = 1) { customerRepository.existsById(id) }
        verify(exactly = 0) { customerRepository.save(any()) }
    }

    @Test
    fun `should update customer`() {
        // Arrange
        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)
        every { customerRepository.existsById(id) } returns true
        every { customerRepository.save(fakeCustomer) } returns fakeCustomer

        // Act
        customerService.update(fakeCustomer)

        // Assert
        verify(exactly = 1) { customerRepository.existsById(id) }
        verify(exactly = 1) { customerRepository.save(fakeCustomer) }
    }

    @Test
    fun `should delete customer`() {
        // Arrange
        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)
        val expectedCustomer = fakeCustomer.copy(status = CustomerStatus.INATIVO)
        every { customerService.findById(id) } returns fakeCustomer
        every { bookService.deleteByCustomer(fakeCustomer) } just runs
        every { customerRepository.save(expectedCustomer)} returns expectedCustomer

        // Act
        customerService.delete(id)

        // Assert
        verify(exactly = 1) { customerService.findById(id) }
        verify(exactly = 1) { bookService.deleteByCustomer(fakeCustomer) }
        verify(exactly = 1) { customerRepository.save(expectedCustomer) }
    }

    @Test
    fun `should throw not found exception when delete customer`() {
        // Arrange
        val id = Random().nextInt()
        every { customerService.findById(id) } throws NotFoundException(Errors.ML201.message.format(id), Errors.ML201.code)

        // Act
        val error = assertThrows<NotFoundException> {
            customerService.delete(id)
        }

        // Assert
        assertEquals("Customer [${id}] not exists", error.message)
        assertEquals("ML-201", error.errorCode)
        verify(exactly = 1) { customerService.findById(id) }
        verify(exactly = 0) { bookService.deleteByCustomer(any()) }
        verify(exactly = 0) { customerRepository.save(any()) }
    }

    @Test
    fun `should return true when email available`() {
        // Arrange
        val email = "${Random().nextInt()}@email.com"
        every { customerRepository.existsByEmail(email) } returns false

        // Act
        val emailAvailable = customerService.emailAvailable(email)

        // Assert
        assertTrue(emailAvailable)
        verify(exactly = 1) { customerRepository.existsByEmail(email) }
    }

    @Test
    fun `should return false when email unavailable`() {
        // Arrange
        val email = "${Random().nextInt()}@email.com"
        every { customerRepository.existsByEmail(email) } returns true

        // Act
        val emailAvailable = customerService.emailAvailable(email)

        // Assert
        assertFalse(emailAvailable)
        verify(exactly = 1) { customerRepository.existsByEmail(email) }
    }

    @Test
    fun `should return true when customer is active`() {
        // Arrange
        val id = Math.random().toInt()
        val fakeCustomer = buildCustomer(id = id)
        every { customerService.findById(id) } returns fakeCustomer

        // Act
        val isCustomerActive = customerService.isCustomerActive(id)

        // Assert
        assertTrue(isCustomerActive)
        verify(exactly = 1) { customerService.isCustomerActive(id) }
    }

    @Test
    fun `should return false when customer is inactive`() {
        // Arrange
        val id = Math.random().toInt()
        val fakeCustomer = buildCustomer(id = id, status = CustomerStatus.INATIVO)
        every { customerService.findById(id) } returns fakeCustomer

        // Act
        val isCustomerActive = customerService.isCustomerActive(id)

        // Assert
        assertFalse(isCustomerActive)
        verify(exactly = 1) { customerService.isCustomerActive(id) }
    }

}