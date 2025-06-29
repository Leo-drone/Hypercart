package com.hypercart.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id: Long = 0,
    val name: String,
    @SerialName("created_by")
    val createdBy: String? = null
)

@Serializable
data class CreateStoreRequest(
    val name: String
)

@Serializable
data class Category(
    val id: Long = 0,
    val name: String
)

@Serializable
data class Product(
    val id: Long = 0,
    val name: String,
    @SerialName("category_id")
    val categoryId: Long = 1, // Catégorie par défaut
    @SerialName("store_id")
    val storeId: Long
)

@Serializable
data class CreateProductRequest(
    val name: String,
    @SerialName("category_id")
    val categoryId: Long,
    @SerialName("store_id")
    val storeId: Long
)

@Serializable
data class CartItem(
    val id: Long = 0,
    @SerialName("product_id")
    val productId: Long,
    val quantity: Int = 1,
    @SerialName("cart_id")
    val cartId: Long,
)

@Serializable
data class AddToCartRequest(
    @SerialName("product_id")
    val productId: Long,
    val quantity: Int = 1,
)

@Serializable
data class Cart(
    val id: Long = 0,
    @SerialName("store_id")
    val storeId: Long,
    @SerialName("owned_by")
    val ownedBy: String, // UUID du user
    val items: List<CartItem> = emptyList()
)

@Serializable
data class CreateCartRequest(
    @SerialName("store_id")
    val storeId: Long
)

@Serializable
data class InsertCartRequest(
    @SerialName("store_id")
    val storeId: Long,
    @SerialName("owned_by")
    val ownedBy: String
)

@Serializable
data class InsertCartItemRequest(
    @SerialName("product_id")
    val productId: Long,
    val quantity: Int,
    @SerialName("cart_id")
    val cartId: Long,
)

@Serializable
data class UpdateQuantityRequest(
    val quantity: Int
)

@Serializable
data class StoreMember(
    val id: Long = 0,
    @SerialName("store_id")
    val storeId: Long,
    @SerialName("user_id")
    val userId: String,
    val role: String = "member", // owner, admin, member
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class CreateStoreMemberRequest(
    @SerialName("store_id")
    val storeId: Long,
    @SerialName("user_id")
    val userId: String,
    val role: String = "member"
)