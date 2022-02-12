package com.mercadolivro.controller;

import com.mercadolivro.controller.request.PostPurchaseRequest
import com.mercadolivro.mapper.PurchaseMapper
import com.mercadolivro.service.PurchaseService
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

}
