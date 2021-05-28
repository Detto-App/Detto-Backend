package com.dettoapp.Utility

import com.dettoapp.data.Classroom
import com.dettoapp.data.EmailDetailsModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.activation.FileDataSource


@Suppress("PrivatePropertyName")
class MailHelper {
    private var emailList: ArrayList<EmailDetailsModel> = arrayListOf()
    private val MAIL_HELPER_FILE = "emailHelper.json"
    private var initialised = false

    init {
        initializeEmail()
    }

    private fun initializeEmail() {
        var br: BufferedReader? = null
        try {
            br = BufferedReader(
                    FileReader("emailHelper.json"))
            val typeToken = object : TypeToken<ArrayList<EmailDetailsModel>>() {}.type
            emailList = Gson().fromJson(br, typeToken)
            initialised = true
            br.close()
        } catch (e: Exception) {
        } finally {
            br?.close()
        }
    }

    fun ableToInitialize() = File(MAIL_HELPER_FILE).exists()

    fun sendMail(fileName: String, classroom: Classroom, toEmail: String) {

        val preferredEmailModel = emailList[0]
        val x = FileDataSource("${classroom.classroomuid}.csv")

        val email = EmailBuilder.startingBlank()
                .from("DettoApp", preferredEmailModel.senderemail)
                .to(classroom.teacher.name.capitalize(), toEmail)
                .withSubject("Class Report")
                .withAttachment(fileName, x)
                .withPlainText("Hello ${classroom.teacher.name.capitalize()} here is projectList of all students of " +
                        "classroom ${classroom.sem} ${classroom.section}")
                .buildEmail()

        MailerBuilder
                .withSMTPServer(preferredEmailModel.server, preferredEmailModel.serverport, preferredEmailModel.username, preferredEmailModel.password)
                .withTransportStrategy(TransportStrategy.SMTPS)
                .buildMailer()
                .sendMail(email)

        x.file.delete()
    }
}