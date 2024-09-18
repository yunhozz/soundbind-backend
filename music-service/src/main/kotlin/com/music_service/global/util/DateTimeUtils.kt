package com.music_service.global.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    fun convertLocalDateTimeToString(time: LocalDateTime): String =
        time.format(DateTimeFormatter.ofPattern(DATETIME_PATTERN))

    fun convertStringToLocalDateTime(time: String): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN)
        return LocalDateTime.parse(time, formatter)
    }

    private const val DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
}