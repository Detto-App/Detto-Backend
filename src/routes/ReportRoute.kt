package com.dettoapp.routes

import com.dettoapp.Utility.MailHelper
import com.dettoapp.data.Classroom
import com.dettoapp.data.ProjectModel
import com.dettoapp.data.ProjectRubricsModel
import com.opencsv.CSVWriter
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.json
import java.io.FileOutputStream
import java.io.OutputStreamWriter


val mailHelper: MailHelper by lazy { MailHelper() }

fun Route.reportRoute() {
    route("/report/{email}")
    {
        post {
            val email = call.parameters["email"]
            val classroom = call.receive<Classroom>()
            if (mailHelper.ableToInitialize()) {
                createReport(classroom, email!!)
                call.respond(HttpStatusCode.OK)
            } else
                call.respond(HttpStatusCode.BadRequest)
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
fun createReport(classroom: Classroom, email: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {

            val allProjects = projectCollection.find(ProjectModel::cid eq classroom.classroomuid).toList()


            val outputStream = FileOutputStream("${classroom.classroomuid}.csv")
            outputStream.write(0xef)
            outputStream.write(0xbb)
            outputStream.write(0xbf)

            val writer = CSVWriter(OutputStreamWriter(outputStream),CSVWriter.DEFAULT_SEPARATOR,CSVWriter.DEFAULT_QUOTE_CHARACTER,CSVWriter.DEFAULT_ESCAPE_CHARACTER,CSVWriter.RFC4180_LINE_END)

            val headers = arrayOf("Sl No", "Title", "Description", "Students","Marks")
            val data: ArrayList<Array<String>> = ArrayList()
            data.add(headers)

            allProjects.forEachIndexed { index, model ->
                val title = model.title.capitalize()
                val description = model.desc.capitalize()
                var students = ""
                val pid=model.pid
                var marks=""


                for ((k, v) in model.projectStudentList) {
                    val rubricsList= projectRubricsCollection.findOne(and(ProjectRubricsModel::pid eq pid,ProjectRubricsModel::usn eq k))!!.rubrics.titleMarksList
                    var sum=0.0
                    for(i in rubricsList){
                        sum+=(i.marks*i.convertTo)/(i.maxMarks)
                    }
                    students += "${k.toUpperCase()} - ${v.capitalize()}${CSVWriter.RFC4180_LINE_END}"
                    marks +="${sum}${CSVWriter.RFC4180_LINE_END}"
                }
                data.add(arrayOf((index + 1).toString(), title, description, students,marks))
            }
            writer.writeAll(data)
            writer.flush()
            writer.close()
            mailHelper.sendMail("Report.csv",classroom,email)
        } catch (e: Exception) {
            println("ERROR"+e.localizedMessage)
        }
    }
}

//fun sendMail(classroom: Classroom, toEmail: String,dataInBytes:ByteArray) {
//    //val x = FileDataSource("${classroom.classroomuid}.csv")
//
//    val email = EmailBuilder.startingBlank()
//            .from("DettoApp", "dettoapp@gmail.com")
//            .to(classroom.teacher.name.capitalize(), toEmail)
//            .withSubject("Class Report")
//            .withAttachment("Report.csv", dataInBytes, MIME_CSV)
//            .withPlainText("Hello ${classroom.teacher.name.capitalize()} here is projectList of all students of " +
//                    "classroom ${classroom.sem} ${classroom.section}")
//            .buildEmail()
//
//    MailerBuilder
//            .withSMTPServer("smtp.gmail.com", 465, "dettoapp@gmail.com", "VLvbLwrQ7>-)qz(h")
//            .withTransportStrategy(TransportStrategy.SMTPS)
//            .buildMailer()
//            .sendMail(email)
//}


//
//MailerBuilder
//.withSMTPServer("smtp.gmail.com", 465, "dettoapp@gmail.com", "VLvbLwrQ7>-)qz(h")
//.withTransportStrategy(TransportStrategy.SMTPS)
//.buildMailer()
//.sendMail(email)


//val w2 = BufferedWriter(OutputStreamWriter(FileOutputStream("${classroom.classroomuid}.csv"), StandardCharsets.UTF_8))
//val w2 = Files.newBufferedWriter(Paths.get("${classroom.classroomuid}.csv"), StandardCharsets.UTF_8)

//            val baos = ByteArrayOutputStream()
//            val os = BufferedWriter(OutputStreamWriter(baos))