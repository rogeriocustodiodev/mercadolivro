package com.mercadolivro.controller.request

import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

data class PutCustomerRequest (

    @field:NotEmpty(message = "Email deve ser válido")
    var name: String,

    @field:Email(message = "Nome deve ser informado")
    var email: String
)