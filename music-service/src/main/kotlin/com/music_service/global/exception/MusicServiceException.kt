package com.music_service.global.exception

import com.sound_bind.global.exception.ErrorCode

sealed class MusicServiceException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {

    class MusicNotFoundException(override val message: String): MusicServiceException(ErrorCode.NOT_FOUND, message)
    class MusicFileNotExistException(override val message: String): MusicServiceException(ErrorCode.NOT_FOUND, message)
    class MusicNotUpdatableException(override val message: String): MusicServiceException(ErrorCode.BAD_REQUEST, message)
    class MusicFileUploadFailException(override val message: String): MusicServiceException(ErrorCode.BAD_REQUEST, message)
    class NegativeValueException(override val message: String): MusicServiceException(ErrorCode.BAD_REQUEST, message)
}