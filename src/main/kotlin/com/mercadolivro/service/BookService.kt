package com.mercadolivro.service

import com.mercadolivro.enums.BookStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.model.BookModel
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.BookRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BookService(
    private val bookRepository: BookRepository
) {

    fun create(book: BookModel) {
        bookRepository.save(book)
    }

    fun findAll(pageable: Pageable): Page<BookModel> {
        return bookRepository.findAll(pageable)
    }

    fun findActives(pageable: Pageable): Page<BookModel> {
        return bookRepository.findByStatus(BookStatus.ATIVO, pageable)
    }

    fun findById(id: Int): BookModel {
        return bookRepository.findById(id)
            .orElseThrow {
                NotFoundException(
                    Errors.ML101.message.format(id),
                    Errors.ML101.code
                )
            }
    }

    fun delete(id: Int) {
        val book = findById(id)
        book.status = BookStatus.CANCELADO
        update(book)
    }

    fun update(book: BookModel) {
        bookRepository.save(book)
    }

    fun deleteByCustomer(customer: CustomerModel) {
        val books = bookRepository.findByCustomer(customer)

        books.filter {
            it.status == BookStatus.ATIVO
        }.forEach {
            it.status = BookStatus.DELETADO
        }

        bookRepository.saveAll(books)
    }

    fun findAllByIds(bookIds: Set<Int>): MutableList<BookModel> {
        return bookRepository.findAllById(bookIds)
    }

    fun purchase(books: MutableList<BookModel>) {
        books.map {
            it.status = BookStatus.VENDIDO
        }
        bookRepository.saveAll(books)
    }

    fun isBooksAvailableForSale(ids: Set<Int>): Boolean {
        val books = findAllByIds(ids)

        books.forEach{
            if (it.status!! != BookStatus.ATIVO) {
                return false
            }
        }

        return true
    }

    fun findSold(customerId: Int?, pageable: Pageable): Page<BookModel> {
        customerId?.let {
            return bookRepository.findByStatusAndCustomerId(BookStatus.VENDIDO, customerId, pageable)
        }
        return bookRepository.findByStatus(BookStatus.VENDIDO, pageable)
    }

}
