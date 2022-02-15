package com.mercadolivro.repository

import com.mercadolivro.model.CustomerModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository

interface CustomerRepository : CrudRepository<CustomerModel, Int> {

    fun findAll(pageable: Pageable): Page<CustomerModel>
    fun findByNameContaining(name: String, pageable: Pageable): Page<CustomerModel>
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): CustomerModel?
}