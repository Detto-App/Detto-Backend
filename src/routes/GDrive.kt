package com.dettoapp.routes

import com.dettoapp.data.GDriveModel
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import kotlin.concurrent.schedule

private val timer = Timer()
private var timerJob: TimerTask? = null
private var gDriveAccessToken: String? = null
private var refreshTokenCredential: GoogleCredential? = null


fun Route.gDrive() {
    route("/gDrive")
    {
        post {
            try {
                val model = call.receive<GDriveModel>()
                GlobalScope.launch(Dispatchers.IO)
                {
                    refreshTokenCredential =
                            GoogleCredential.Builder()
                                    .setJsonFactory(JacksonFactory.getDefaultInstance())
                                    .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                                    .setClientSecrets(model.clientID, model.clientSecret)
                                    .build()
                                    .setRefreshToken(model.refreshToken)
                    repeatFetchGDriveToken()
                }
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }

    route("/gDriveToken")
    {
        get {
            if (gDriveAccessToken == null)
                call.respond(HttpStatusCode.BadRequest)
            else
                call.respond(HttpStatusCode.OK, gDriveAccessToken!!)
        }
    }
}

fun repeatFetchGDriveToken() {
    if (timerJob != null) {
        timerJob!!.cancel()
    }
    timerJob = timer.schedule(0, 3000000) {
        fetchAccessToken()
    }
}

fun fetchAccessToken() {
    GlobalScope.launch(Dispatchers.IO) {
        refreshTokenCredential?.let {
            it.refreshToken()
            gDriveAccessToken = it.accessToken
            println(gDriveAccessToken)
        }
    }
}


//fun Long.toDateString(format:String = "dd/MM/yy hh:mm:ss a"): String {
//    val sdf = SimpleDateFormat(format)
//    //val df = DateFormat.getDateInstance(dateFormat, Locale.getDefault())
//    return sdf.format(this)
//}
//            refreshTokenCredential.refreshToken()
//            gDriveAccessToken = refreshTokenCredential.accessToken