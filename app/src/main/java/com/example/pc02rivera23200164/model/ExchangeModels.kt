package com.example.pc02rivera23200164.model

import com.google.firebase.Timestamp

data class ExchangeRateResponse(
    val result: String,
    val base_code: String,
    val conversion_rates: Map<String, Double>
)

data class ConversionRecord(
    val userId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val amount: Double = 0.0,
    val fromCurrency: String = "",
    val toCurrency: String = "",
    val result: Double = 0.0
)
