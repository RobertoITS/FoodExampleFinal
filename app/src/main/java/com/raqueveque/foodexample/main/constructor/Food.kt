package com.raqueveque.foodexample.main.constructor

data class Food(var category: String? = null,
                var image: String? = null,
                var name: String? = null,
                var price: Long? = null,
                var id: String? = null,
                var check: Boolean = false)