package com.mercadolivro.controller;

import com.mercadolivro.controller.request.PostPurchaseRequest
import com.mercadolivro.controller.response.CustomerResponse
import com.mercadolivro.controller.response.PurchaseResponse
import com.mercadolivro.extension.toResponse
import com.mercadolivro.mapper.PurchaseMapper
import com.mercadolivro.security.UserCanOnlyAccessTheirOwnResource
import com.mercadolivro.service.PurchaseService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid

@RestController
@RequestMapping("/purchase")
public class PurchaseController(
    private val purchaseService: PurchaseService,
    private val purchaseMapper: PurchaseMapper
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun purchase(@RequestBody @Valid request: PostPurchaseRequest) {
        purchaseService.create(purchaseMapper.toModel(request))
    }

    @GetMapping
    @UserCanOnlyAccessTheirOwnResource
    fun getAll(@PageableDefault(page = 0, size = 10) pageable: Pageable,
               @RequestParam("customer_id") customerId: Int?): Page<PurchaseResponse> { // o ? indica que o parâmetro não é obrigatório
        return purchaseService.getAll(customerId, pageable)
            .map{ purchaseMapper.toResponse(it) }
    }

}
