package com.music_service.domain.application.file

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

object FileVariable {
    internal val ABSOLUTE_PATH: String = File("").absolutePath
    internal const val ROOT_DIRECTORY = "/music-service/src/main/resources/file/"
    internal val CURRENT_DATE: String = SimpleDateFormat("yy-MM-dd").format(Date())
    internal val FILE_DIRECTORY = ABSOLUTE_PATH + ROOT_DIRECTORY + CURRENT_DATE
}