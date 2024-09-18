package com.music_service.domain.persistence.es.search

import com.music_service.domain.persistence.es.document.FileDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface FileSearchRepository: ElasticsearchRepository<FileDocument, Long>