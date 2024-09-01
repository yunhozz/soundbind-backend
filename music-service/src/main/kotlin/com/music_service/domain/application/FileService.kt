package com.music_service.domain.application

import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.dto.response.MusicDetailsDTO
import com.music_service.domain.application.file.FileHandler
import com.music_service.domain.application.listener.ElasticsearchListener
import com.music_service.domain.persistence.entity.FileEntity
import com.music_service.domain.persistence.entity.Music
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class FileService(private val fileHandler: FileHandler) {

    // TODO: 예외 보상 처리

    @Autowired
    private lateinit var elasticsearchListener: ElasticsearchListener

    private val log: Logger = LoggerFactory.getLogger(FileService::class.java)

    @Async
    fun upload(fileMap: Map<FileEntity, FileUploadResponseDTO>, music: Music) {
        log.info("=====File Upload Start=====")
        fileMap.values.forEach { dto ->
            fileHandler.upload(dto.file, dto.savedName)
        }
        elasticsearchListener.onMusicUpload(MusicDetailsDTO(music, fileMap.keys.toList()))
        log.info("=====File Upload End=====")
    }

    @Async
    fun update(fileUrl: String?, dto: FileUploadResponseDTO?, music: Music, files: List<FileEntity>) {
        log.info("=====File Update Start=====")
        dto?.let {
            fileHandler.delete(fileUrl!!)
            fileHandler.upload(it.file, it.savedName)
        }
        elasticsearchListener.onMusicUpload(MusicDetailsDTO(music, files))
        log.info("=====File Update End=====")
    }

    @Async
    fun delete(musicId: Long, files: List<FileEntity>) {
        log.info("=====File Delete Start=====")
        val fileIds = files.map {
            fileHandler.delete(it.fileUrl)
            it.id!!
        }
        elasticsearchListener.onMusicDelete(musicId, fileIds)
        log.info("=====File Delete End=====")
    }
}