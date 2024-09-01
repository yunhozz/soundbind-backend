package com.sound_bind.api_gateway.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.api_gateway.handler.exception.BusinessException
import com.sound_bind.api_gateway.handler.exception.ErrorCode
import com.sound_bind.api_gateway.handler.exception.ErrorResponse
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.ResolvableType
import org.springframework.core.annotation.Order
import org.springframework.core.codec.Hints
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
@Order(-1)
class GlobalExceptionHandler: ErrorWebExceptionHandler {

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val response = exchange.response
        response.headers.contentType = MediaType.APPLICATION_JSON

        val errorResponse = when (ex) {
            is ResponseStatusException -> {
                val errorCode = ErrorCode.FRAME_WORK_INTERNAL_ERROR
                response.statusCode = HttpStatus.valueOf(errorCode.status)
                ErrorResponse.of(errorCode, ex.localizedMessage)
            }
            is BusinessException -> {
                response.statusCode = HttpStatus.valueOf(ex.errorCode.status)
                ErrorResponse.of(ex.errorCode, ex.localizedMessage)
            }
            else -> {
                val errorCode = ErrorCode.UNDEFINED_ERROR
                response.statusCode = HttpStatus.valueOf(errorCode.status)
                ErrorResponse.of(errorCode, ex.localizedMessage)
            }
        }

        return response.writeWith(
            Jackson2JsonEncoder(jacksonObjectMapper()).encode(
                Mono.just(errorResponse),
                response.bufferFactory(),
                ResolvableType.forInstance(errorResponse),
                MediaType.APPLICATION_JSON,
                Hints.from(Hints.LOG_PREFIX_HINT, exchange.logPrefix)
            )
        )
    }
}