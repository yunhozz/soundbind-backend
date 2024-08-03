package com.music_service.global.exception

sealed class MusicServiceException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {

    class MusicNotFoundException(override val message: String): MusicServiceException(ErrorCode.NOT_FOUND, message)
    class MusicFileNotExistException(override val message: String): MusicServiceException(ErrorCode.NOT_FOUND, message)
}