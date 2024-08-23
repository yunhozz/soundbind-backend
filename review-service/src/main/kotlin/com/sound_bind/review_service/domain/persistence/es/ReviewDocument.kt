package com.sound_bind.review_service.domain.persistence.es

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDateTime

@Document(indexName = "review")
data class ReviewDocument(
    @Id
    val id: Long?,
    val musicId: Long,
    val userId: Long,
    val userNickname: String,
    val userImageUrl: String?,
    val message: String,
    val score: Double,
    val commentNum: Int = 0,
    val likes: Int = 0,
    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second_millis])
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    val createdAt: LocalDateTime,
    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second_millis])
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    val updatedAt: LocalDateTime
)