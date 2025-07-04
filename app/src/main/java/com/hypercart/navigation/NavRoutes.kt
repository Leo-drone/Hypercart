package com.hypercart.navigation

sealed class NavRoutes(val route: String) {
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object Products : NavRoutes("products")
    object Home : NavRoutes("home")
    object StoreList : NavRoutes("store_list")
    object AddStore : NavRoutes("add_store")
    object StoreDetail : NavRoutes("store_detail/{storeId}") {
        fun createRoute(storeId: String) = "store_detail/$storeId"
    }
    object StoreProducts : NavRoutes("store_products/{storeId}") {
        fun createRoute(storeId: String) = "store_products/$storeId"
    }
    object Cart : NavRoutes("cart/{storeId}") {
        fun createRoute(storeId: String) = "cart/$storeId"
    }
    object StoreSettings : NavRoutes("store_settings/{storeId}") {
        fun createRoute(storeId: String) = "store_settings/$storeId"
    }
    object ResetPassword : NavRoutes("new-password")
    object NewPassword : NavRoutes("reset-password") {
        const val TOKEN_ARG = "token"
        const val EMAIL_ARG = "email"
        fun createRoute(token: String, email: String) = "reset-password?token=$token&email=$email"
    }
    
    companion object {
        fun fromRoute(route: String): NavRoutes {
            return when (route) {
                Login.route -> Login
                Register.route -> Register
                Products.route -> Products
                Home.route -> Home
                StoreList.route -> StoreList
                AddStore.route -> AddStore
                ResetPassword.route -> ResetPassword
                else -> Login
            }
        }
    }
} 