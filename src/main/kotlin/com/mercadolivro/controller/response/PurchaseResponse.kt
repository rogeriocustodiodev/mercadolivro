package com.mercadolivro.controller.response

import com.fasterxml.jackson.annotation.JsonAlias
import com.mercadolivro.model.BookModel
import com.mercadolivro.model.CustomerModel
import java.math.BigDecimal
import java.time.LocalDateTime

data class PurchaseResponse (
    val id: Int? = null,
    val customer: CustomerModel,
    val books: MutableList<BookModel>,
    val nfe: String? = null,
    val price: BigDecimal,
    @JsonAlias("created_at")
    val createdAt: LocalDateTime
)