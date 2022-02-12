package com.mercadolivro.controller.request

import com.fasterxml.jackson.annotation.JsonAlias
import com.mercadolivro.validation.BookAvailableForSale
import com.mercadolivro.validation.CustomerActive
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

data class PostPurchaseRequest (

    @field:NotNull
    @field:Positive
    @JsonAlias("customer_id")
    @CustomerActive
    val customerId: Int,

    @field:NotNull
    @field:NotEmpty
    @JsonAlias("book_ids")
    @BookAvailableForSale
    val bookIds: Set<Int> // set não aceita números repetidos
)
