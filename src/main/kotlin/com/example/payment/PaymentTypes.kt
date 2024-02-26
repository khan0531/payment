package com.example.payment

//enum 클래스 묶어두기
enum class OrderStatus {
    CREATED,
    FAILED,
    PAID,
    CANCELED,
    PARTIALLY_REFUNDED,
    REFUNDED
}

enum class TransactionType {
    PAYMENT,
    REFUND,
    CANCEL
}

enum class TransactionStatus {
    RESERVE,
    SUCCESS,
    FAILURE
}