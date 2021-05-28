package com.dettoapp.data

data class EmailDetailsModel(
        val server: String,
        val serverport: Int,
        val senderemail:String,
        val username: String,
        val password: String,
)