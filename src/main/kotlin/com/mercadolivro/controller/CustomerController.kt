package com.mercadolivro.controller

import com.mercadolivro.controller.request.PostCustomerRequest
import com.mercadolivro.controller.request.PutCustomerRequest
import com.mercadolivro.controller.response.CustomerResponse
import com.mercadolivro.controller.response.PageResponse
import com.mercadolivro.extension.toCustomerModel
import com.mercadolivro.extension.toPageResponse
import com.mercadolivro.extension.toResponse
import com.mercadolivro.security.OnlyAdminCanAccess
import com.mercadolivro.security.UserCanOnlyAccessTheirOwnResource
import com.mercadolivro.service.CustomerService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("customers")
class CustomerController(
    private val customerService: CustomerService // como se fosse o autowired para o java
) {

    @GetMapping
    @OnlyAdminCanAccess
    fun getAll(@PageableDefault(page = 0, size = 10) pageable: Pageable, @RequestParam name: String?): PageResponse<CustomerResponse> { // o ? indica que o parâmetro não é obrigatório
        return customerService.getAll(name, pageable).map{ it.toResponse() }.toPageResponse()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun postCustomer(@RequestBody @Valid postCustomerRequest: PostCustomerRequest) {
        customerService.create(postCustomerRequest.toCustomerModel())
    }

    @GetMapping("/{id}")
    @UserCanOnlyAccessTheirOwnResource
    fun getCustomer(@PathVariable id: Int): CustomerResponse {
        return customerService.findById(id).toResponse()
    }

    @PutMapping("/{id}")
    @UserCanOnlyAccessTheirOwnResource
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateCustomer(@PathVariable id: Int,
                       @RequestBody @Valid putCustomerRequest: PutCustomerRequest) {
        val existingCustomer = customerService.findById(id)
        customerService.update(putCustomerRequest.toCustomerModel(existingCustomer))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @OnlyAdminCanAccess
    fun deleteCustomer(@PathVariable id: Int) {
        customerService.delete(id)
    }
}