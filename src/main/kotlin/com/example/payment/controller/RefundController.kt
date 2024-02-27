package com.example.payment.controller

import com.example.payment.service.RefundService
import com.example.payment.service.RefundServiceRequest
import com.example.payment.service.RefundServiceResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RequestMapping("/api/v1")
@RestController
class RefundController(
    private val refundService: RefundService
) {
    @PostMapping("/pay")
    fun pay(
        @Valid @RequestBody
        refundRequest: RefundRequest
    ): RefundResponse = RefundResponse.from(
        refundService.refund(
            refundRequest.toRefundServiceRequest()
        )
    )
}

class RefundResponse(
    val refundTransactionId: String,
    val refundAmount: Long,
    val refundedAt: LocalDateTime
) {
    companion object {
        fun from(response: RefundServiceResponse) =
            RefundResponse(
                refundTransactionId = response.refundTransactionId,
                refundAmount = response.refundAmount,
                refundedAt = response.refundedAt
            )
    }
}

data class RefundRequest(
    val transactionId: String,
    val refundId: String,
    val refundAmount: Long,
    val refundReason: String,
) {
    fun toRefundServiceRequest() = RefundServiceRequest(
        transactionId = transactionId,
        refundId = refundId,
        refundAmount = refundAmount,
        refundReason = refundReason
    )
}
