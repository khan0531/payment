package com.example.payment.adapter

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AccountAdapterTest @Autowired constructor(
    private val accountAdapter: AccountAdapter
) {
    @Test
    fun useAccount() {
        val useBalanceRequest = UseBalanceRequest(
            userId = 1,
            accountNumber = "1000000000",
            amount = 1000
        )
        val useBalanceResponse = accountAdapter.useAccount(
            useBalanceRequest
        )
        println(useBalanceResponse)
    }
}