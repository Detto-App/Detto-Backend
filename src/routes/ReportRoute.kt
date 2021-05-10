package com.dettoapp.routes

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.reportRoute()
{
    route("/report/{cid}")
    {
        get {
            
        }
    }
}