package com.mercadolivro.model

import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Role
import javax.persistence.*

@Entity(name = "customer")
data class CustomerModel (

    //var id: String? = null, // valor default
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false, unique = true)
    var email: String,
    @Column
    @Enumerated(EnumType.STRING)
    var status: CustomerStatus? = null,
    @Column
    val password: String,
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "customer_roles",
        joinColumns = [JoinColumn(name = "customer_id")]
    )
    @ElementCollection(
        targetClass = Role::class,
        fetch = FetchType.EAGER
    )
    var roles: Set<Role> = setOf()

)