package com.example.payment.util

import java.util.*

fun generateOrderId() = "PO" + generteUUID()
fun generateTransactionId() = "PT" + generteUUID()
fun generateRefundTransactionId() = "RT" + generteUUID()

private fun generteUUID() = UUID.randomUUID().toString().replace("-", "")

