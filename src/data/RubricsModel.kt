package com.dettoapp.data

import org.bson.codecs.pojo.annotations.BsonId

data class RubricsModel (
    @BsonId val rid:String?,
    val titleMarksList:ArrayList<MarksModel>,
    val cid:String
)