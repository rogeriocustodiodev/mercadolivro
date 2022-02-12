package com.mercadolivro.validation

import com.mercadolivro.service.CustomerService
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class CustomerActiveValidator (var customerService: CustomerService): ConstraintValidator<CustomerActive, Int> {

    override fun isValid(id: Int, context: ConstraintValidatorContext?): Boolean {
        return customerService.isCustomerActive(id)
    }

}
