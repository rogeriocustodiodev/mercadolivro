package com.mercadolivro.service

import com.mercadolivro.events.PurchaseEvent
import com.mercadolivro.helper.buildPurchase
import com.mercadolivro.repository.PurchaseRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

@ExtendWith(MockKExtension::class)
class PurchaseServiceTest {

    @MockK
    private lateinit var purchaseRepository: PurchaseRepository

    @MockK
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @InjectMockKs
    private lateinit var purchaseService: PurchaseService

    val purchaseEventSlot = slot<PurchaseEvent>()

    @Test
    fun `should create purchase and publish event`() {
        // Arrahge
        val purchase = buildPurchase()
        every { purchaseRepository.save(purchase) } returns purchase
        every { applicationEventPublisher.publishEvent(any()) } just runs

        // Act
        purchaseService.create(purchase)

        // Assert
        verify(exactly = 1) { purchaseRepository.save(purchase) }
        verify(exactly = 1) { applicationEventPublisher.publishEvent(capture(purchaseEventSlot)) }
        assertEquals(purchase, purchaseEventSlot.captured.purchaseModel)
    }

    @Test
    fun `should update purchase`() {
        // Arrange
        val purchase = buildPurchase()
        every { purchaseRepository.save(purchase) } returns purchase

        // Act
        purchaseService.update(purchase)

        // Assert
        verify(exactly = 1) { purchaseRepository.save(purchase) }
    }

    @Test
    fun `should return all purchases by customer`() {
        // Arrange
        val customerId = Math.random().toInt()
        val purchasesExpected = PageImpl(listOf(buildPurchase(), buildPurchase()))
        every { purchaseRepository.findByCustomerId(customerId, any()) } returns purchasesExpected

        // Act
        val purchasesReturned = purchaseService.getAll(customerId, Pageable.ofSize(1))

        // Assert
        assertEquals(purchasesExpected, purchasesReturned)
        verify(exactly = 1) { purchaseRepository.findByCustomerId(customerId, any()) }
        verify(exactly = 0) { purchaseRepository.findAll(Pageable.ofSize(1)) }
    }

    @Test
    fun `should return all purchases`() {
        // Arrange
        val purchasesExpected = PageImpl(listOf(buildPurchase(), buildPurchase()))
        every { purchaseRepository.findAll(Pageable.ofSize(1)) } returns purchasesExpected

        // Act
        val purchasesReturned = purchaseService.getAll(null, Pageable.ofSize(1))

        // Assert
        assertEquals(purchasesExpected, purchasesReturned)
        verify(exactly = 1) { purchaseRepository.findAll(Pageable.ofSize(1)) }
        verify(exactly = 0) { purchaseRepository.findByCustomerId(any(), any()) }
    }

}