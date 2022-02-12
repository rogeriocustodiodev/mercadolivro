package com.mercadolivro.validation

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Constraint(validatedBy = [CustomerActiveValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CustomerActive(
    val message: String = "Cliente informado não está com status ATIVO",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
