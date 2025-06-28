package com.hypercart.data

import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id: Long = 0,
    val name: String,
)

@Serializable
data class CreateStoreRequest(
    val name: String
)