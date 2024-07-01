package com.music_service.global.dto.response

import com.querydsl.core.annotations.QueryProjection

data class MusicDetailsQueryDTO @QueryProjection constructor(
    val id: Long,
    val userId: Long,
    val userNickname: String,
    val title: String
) {
//    lateinit var genres: List<String>
    lateinit var files: List<MusicFileQueryDTO>

    data class MusicFileQueryDTO @QueryProjection constructor(
        val id: Long,
        val originalFileName: String,
        val savedName: String,
        val fileUrl: String
    ) {
//        lateinit var fileTypeName: String
    }
}