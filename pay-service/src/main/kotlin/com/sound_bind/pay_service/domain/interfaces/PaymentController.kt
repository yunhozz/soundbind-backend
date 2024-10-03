package com.sound_bind.pay_service.domain.interfaces

import com.sound_bind.pay_service.domain.application.PointManagementService
import com.sound_bind.pay_service.domain.application.PointSearchService
import com.sound_bind.pay_service.domain.application.dto.request.PointChargeRequestDTO
import com.sound_bind.pay_service.domain.application.dto.response.PointResponseDTO
import com.sound_bind.pay_service.domain.interfaces.dto.ApiResponse
import com.sound_bind.pay_service.domain.persistence.es.document.PointChargeDocument
import com.sound_bind.pay_service.global.annotation.HeaderSubject
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val pointManagementService: PointManagementService,
    private val pointSearchService: PointSearchService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun chargePoint(
        @HeaderSubject sub: String,
        @RequestBody dto: PointChargeRequestDTO
    ): ApiResponse<Long> {
        val chargePointId = pointManagementService.chargePoint(sub.toLong(), dto)
        return ApiResponse.of("Point Charge Success.", chargePointId)
    }

    @GetMapping("/point")
    @ResponseStatus(HttpStatus.OK)
    fun lookUpMyPoint(@HeaderSubject sub: String): ApiResponse<PointResponseDTO> {
        val pointResponseDTO = pointManagementService.lookUpMyPoint(sub.toLong())
        return ApiResponse.of("", pointResponseDTO)
    }

    @GetMapping("/charge-list")
    @ResponseStatus(HttpStatus.OK)
    fun lookUpChargeHistory(@HeaderSubject sub: String): ApiResponse<List<PointChargeDocument>> {
        val chargeHistory = pointSearchService.lookUpPointChargeHistory(sub.toLong())
        return ApiResponse.of("", chargeHistory)
    }
}