package com.sathsara.ecbtracker.data.repository

import com.sathsara.ecbtracker.data.model.Payment
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private val paymentsTable = supabase.postgrest["payments"]

    suspend fun getPayments(): Result<List<Payment>> = withContext(Dispatchers.IO) {
        Result.runCatching {
            paymentsTable.select {
                order("month", Order.DESCENDING)
            }.decodeList<Payment>()
        }
    }

    suspend fun getPaymentForMonth(yearMonth: String): Result<Payment?> = withContext(Dispatchers.IO) {
        Result.runCatching {
            paymentsTable.select {
                filter { eq("month", yearMonth) }
                limit(1)
            }.decodeSingleOrNull<Payment>()
        }
    }

    suspend fun insertOrUpdatePayment(payment: Payment): Result<Unit> = withContext(Dispatchers.IO) {
        Result.runCatching {
            paymentsTable.upsert(payment) {
                // Upsert handles both insert and update if the primary key exists
            }
        }
    }

    suspend fun getPaymentsForRange(startMonth: String, endMonth: String): Result<List<Payment>> = withContext(Dispatchers.IO) {
        Result.runCatching {
            paymentsTable.select {
                filter {
                    gte("month", startMonth)
                    lte("month", endMonth)
                }
                order("month", Order.DESCENDING)
            }.decodeList<Payment>()
        }
    }
}
