package com.dettoapp.routes

import com.dettoapp.data.EmailDetailsModel
import com.dettoapp.data.GDriveModel
import com.dettoapp.data.ProjectModel
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileWriter
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

private val timer = Timer()
private var timerJob: TimerTask? = null
private var gDriveAccessToken: String? = null
private var refreshTokenCredential: GoogleCredential? = null


fun Route.initializeData() {
    route("/gDriveToken")
    {
        get {
            gDriveAccessToken?.let {
                call.respond(HttpStatusCode.OK, it)
            } ?: call.respond(HttpStatusCode.BadRequest)
        }

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

    route("/initializeEmail")
    {
        post {
            try {
                val listOfEmail = call.receive<ArrayList<EmailDetailsModel>>()
                storeEmailInformation(listOfEmail)

                //Do Not Remove this Code it is used to auto initalisze email data
                mailHelper

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
fun storeEmailInformation(list : ArrayList<EmailDetailsModel>)
{
    CoroutineScope(Dispatchers.IO).launch{
        val fileWriter = FileWriter("emailHelper.json",false)
        fileWriter.write(Gson().toJson(list))
        fileWriter.flush()
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