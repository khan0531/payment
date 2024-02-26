package com.example.payment.exception

import mu.KotlinLogging
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException::class)
    fun handlePaymentException(
        e: PaymentException
    ): ErrorResponse {
        log.error(e) { "${e.errorCode} is occurred." }
        return ErrorResponse(e.errorCode)
    }

    //예상 못한 예외에 대해서 수정하기 위해서
    @ExceptionHandler(Exception::class)
    fun handleException(
        e: Exception
    ): ErrorResponse {
        log.error(e) { "Exception is occurred." }
        return ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR)
    }
}

class ErrorResponse(
    val errorCode: ErrorCode,
    val errorMessage: String = errorCode.errorMessage,
)
