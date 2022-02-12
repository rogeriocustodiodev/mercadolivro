package com.mercadolivro.repository

import com.mercadolivro.model.PurchaseModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PurchaseRepository : JpaRepository<PurchaseModel, Int> {

    fun findByCustomerId(customerId: Int, pageable: Pageable): Page<PurchaseModel>
}
