package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class ProjectModel(
    @BsonId
    val pid:String,
    val title:String,
    val desc:String,
    val studentList: HashMap<Int,String> =HashMap(),
    val tid:String,
    val cid:String,
    val status:String
)

