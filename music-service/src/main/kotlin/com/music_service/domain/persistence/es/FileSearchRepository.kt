package com.music_service.domain.persistence.es

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface FileSearchRepository: ElasticsearchRepository<FileDocument, Long>