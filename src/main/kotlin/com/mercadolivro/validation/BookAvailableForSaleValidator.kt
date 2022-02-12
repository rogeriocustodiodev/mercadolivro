package com.mercadolivro.validation

import com.mercadolivro.service.BookService
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class BookAvailableForSaleValidator(var bookService: BookService): ConstraintValidator<BookAvailableForSale, Set<Int>> {

    override fun isValid(ids: Set<Int>, context: ConstraintValidatorContext?): Boolean {
        return bookService.isBooksAvailableForSale(ids)
    }

}
