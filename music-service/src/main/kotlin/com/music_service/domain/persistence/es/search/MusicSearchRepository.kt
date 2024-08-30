package com.music_service.domain.persistence.es.search

import com.music_service.domain.persistence.es.document.MusicDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface MusicSearchRepository: ElasticsearchRepository<MusicDocument, Long>