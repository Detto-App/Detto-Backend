package com.dettoapp.auth

import com.auth0.jwt.*
import com.auth0.jwt.algorithms.*
import com.dettoapp.Utility.Constants
import com.dettoapp.data.StudentModel
import com.dettoapp.data.TeacherModel
import com.dettoapp.data.User
import java.util.*

object JwtConfig {

    private const val secret = "zAP5MBA4B4Ijz0MZaS48"
    private const val issuer = Constants.ISSUER
    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT
            .require(algorithm)
            .withIssuer(issuer)
            .build()

    /**
     * Produce a token for this combination of User and Account
     */
    fun makeToken(studentModel: StudentModel): String = JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withClaim("id", studentModel.uid)
            .sign(algorithm)

    fun makeToken(teacherModel: TeacherModel): String = JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withClaim("id", teacherModel.uid)
            .sign(algorithm)
}

//    /**
//     * Calculate the expiration Date based on current time + the given validity
//     */
//    // private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
//    private const val validityInMs = 36_000_00 * 10 // 10 hours