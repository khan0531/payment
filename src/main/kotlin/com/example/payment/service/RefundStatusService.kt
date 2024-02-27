package com.example.payment.service

import com.example.payment.OrderStatus
import com.example.payment.OrderStatus.*
import com.example.payment.TransactionStatus.*
import com.example.payment.TransactionType.PAYMENT
import com.example.payment.TransactionType.REFUND
import com.example.payment.domain.Order
import com.example.payment.domain.OrderTransaction
import com.example.payment.exception.ErrorCode
import com.example.payment.exception.ErrorCode.*
import com.example.payment.exception.PaymentException
import com.example.payment.repository.OrderRepository
import com.example.payment.repository.OrderTransactionRepository
import com.example.payment.util.generateRefundTransactionId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 환불의 요청 저장, 성공, 실패 저장
 */
@Service
class RefundStatusService(
    private val orderRepository: OrderRepository,
    private val orderTransactionRepository: OrderTransactionRepository,
) {

    @Transactional
    fun saveRefundRequest(
        originalTransactionId: String,
        merchantRefundId: String,
        refundAmount: Long,
        refundReason: String
    ): Long {
        // 결제(orderTransaction) 확인
        // 환불이 가능한지 확인
        // 환불 트랜젝션(orderTransaction) 저장
        val originalTransaction = (orderTransactionRepository.findByTransactionId(originalTransactionId)
            ?: throw PaymentException(ORDER_NOT_FOUND))
        val order = originalTransaction.order

        validationRefund(order, refundAmount)

        return orderTransactionRepository.save(
            OrderTransaction(
                transactionId = generateRefundTransactionId(),
                order = order,
                transactionType = REFUND,
                transactionStatus = RESERVE,
                transactionAmount = refundAmount,
                merchantTransactionId = merchantRefundId,
                description = refundReason
            )
        ).id ?: throw PaymentException(INTERNAL_SERVER_ERROR)
    }

    private fun validationRefund(order: Order, refundAmount: Long) {
        if (order.orderStatus !in listOf(PAID, PARTIALLY_REFUNDED)) {
            throw PaymentException(CANNOT_REFUND)
        }
        if (order.refundedAmount + refundAmount > order.paidAmount) {
            throw PaymentException(EXCEED_REFUNDABLE_AMOUNT)
        }
    }

    @Transactional
    fun saveAsSuccess(
        refundTxId: Long, refundMethodTransactionId: String
    ): Pair<String, LocalDateTime> {
        val orderTransaction = orderTransactionRepository.findById(refundTxId)
            .orElseThrow { throw PaymentException(INTERNAL_SERVER_ERROR) }
            .apply {
                transactionStatus = SUCCESS
                this.payMethodTransactionId = refundMethodTransactionId
                transactedAt = LocalDateTime.now()
            }
        val order = orderTransaction.order
        val totalRefundedAmount = getTotalRefundedAmount(order)

        order.apply {
            orderStatus = getNewOrderStatus(this, totalRefundedAmount)
            refundedAmount = totalRefundedAmount
        }

        return Pair(
            orderTransaction.transactionId,
            orderTransaction.transactedAt ?: throw PaymentException(INTERNAL_SERVER_ERROR)
        )
    }

    private fun getNewOrderStatus(
        order: Order,
        totalRefundedAmount: Long
    ): OrderStatus = if (order.orderAmount == totalRefundedAmount) REFUNDED
    else PARTIALLY_REFUNDED

    private fun getTotalRefundedAmount(order: Order): Long =
        orderTransactionRepository.findByOrderAndTransactionType(order, REFUND)
            .filter { it.transactionStatus == SUCCESS }
            .sumOf { it.transactionAmount }

    fun saveAsFailure(refundTxId: Long, errorCode: ErrorCode) {
        val refundTransaction = orderTransactionRepository
            .findById(refundTxId)
            .orElseThrow { throw PaymentException(INTERNAL_SERVER_ERROR) }
            .apply {
                transactionStatus = FAILURE
                failureCode = errorCode.name
                description = errorCode.errorMessage
            }
    }

    private fun getOrderTransactionByOrder(order: Order) =
        orderTransactionRepository.findByOrderAndTransactionType(
            order = order,
            transactionType = PAYMENT
        ).first()

    private fun getOrderByOrderId(orderId: Long): Order = orderRepository.findById(orderId)
        .orElseThrow { throw PaymentException(ErrorCode.ORDER_NOT_FOUND) }
}