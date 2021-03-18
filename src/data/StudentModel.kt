package com.dettoapp.data

data class StudentModel(val sName: String, val sEmail: String, val sUid: String, val sUSN: String) : User(sName, sEmail, sUid)
