package com.sound_bind.pay_service.domain.interfaces

import com.sound_bind.global.annotation.HeaderSubject
import com.sound_bind.global.dto.ApiResponse
import com.sound_bind.pay_service.domain.application.SponsorSearchService
import com.sound_bind.pay_service.domain.application.SponsorService
import com.sound_bind.pay_service.domain.application.dto.request.SponsorRequestDTO
import com.sound_bind.pay_service.domain.persistence.es.document.SponsorDocument
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sponsors")
class SponsorController(
    private val sponsorService: SponsorService,
    private val sponsorSearchService: SponsorSearchService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun sendSponsor(@HeaderSubject sub: String, @RequestBody dto: SponsorRequestDTO): ApiResponse<Long> {
        val sponsorId = sponsorService.sendSponsorAndSubtractPoint(sub.toLong(), dto)
        return ApiResponse.of("Send Sponsor Success!", sponsorId)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun lookUpSponsorHistory(@HeaderSubject sub: String): ApiResponse<List<SponsorDocument>> {
        val history = sponsorSearchService.lookUpSponsorHistory(sub.toLong())
        return ApiResponse.of("Look Up Sponsor History", history)
    }

    @PatchMapping("/{id}/receive")
    @ResponseStatus(HttpStatus.CREATED)
    fun receiveSponsor(@HeaderSubject sub: String, @PathVariable id: Long): ApiResponse<Unit> {
        sponsorService.receiveSponsorAndAddPoint(sub.toLong(), id)
        return ApiResponse.of("Receive Sponsor Success!")
    }
}