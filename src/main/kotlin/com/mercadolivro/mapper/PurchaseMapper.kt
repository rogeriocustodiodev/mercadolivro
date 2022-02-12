package com.mercadolivro.mapper

import com.mercadolivro.controller.request.PostPurchaseRequest
import com.mercadolivro.controller.response.PurchaseResponse
import com.mercadolivro.model.PurchaseModel
import com.mercadolivro.service.BookService
import com.mercadolivro.service.CustomerService
import org.springframework.stereotype.Component

@Component
class PurchaseMapper(
    private val bookService: BookService,
    private val customerService: CustomerService
) {

    fun toModel(request: PostPurchaseRequest): PurchaseModel {
        val customer = customerService.findById(request.customerId)
        val books = bookService.findAllByIds(request.bookIds)

        return PurchaseModel(
            customer = customer,
            books = books,
            price = books.sumOf { it.price }
        )
    }

    fun toResponse(purchaseModel: PurchaseModel?): PurchaseResponse {
        return PurchaseResponse(
            id = purchaseModel!!.id,
            customer = purchaseModel!!.customer,
            books = purchaseModel!!.books,
            nfe = purchaseModel!!.nfe,
            price = purchaseModel!!.price,
            createdAt = purchaseModel!!.createdAt
        )
    }

}