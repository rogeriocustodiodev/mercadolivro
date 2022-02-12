package com.mercadolivro.validation

import com.mercadolivro.service.CustomerService
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class EmailAvailableValidator(var customerService: CustomerService): ConstraintValidator<EmailAvailable, String> {

    override fun isValid(email: String?, context: ConstraintValidatorContext?): Boolean {
        if (email.isNullOrEmpty()) {
            return false
        }
        return customerService.emailAvailable(email)
    }

}
