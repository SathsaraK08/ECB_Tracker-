package com.sathsara.ecbtracker.data.repository

import com.sathsara.ecbtracker.data.model.Entry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntryRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private val entriesTable = supabase.postgrest["entries"]
    private val storageBucket = supabase.storage["meter_images"]

    suspend fun getEntries(
        limit: Long = 20,
        isVerified: Boolean? = null
    ): Result<List<Entry>> = withContext(Dispatchers.IO) {
        Result.runCatching {
            entriesTable.select {
                filter {
                    if (isVerified == true) {
                        neq("img_url", null)
                    } else if (isVerified == false) {
                        eq("img_url", null)
                    }
                }
                order("date", Order.DESCENDING)
                order("time", Order.DESCENDING)
                limit(limit)
            }.decodeList<Entry>()
        }
    }

    suspend fun getRecentEntries(limit: Long = 3): Result<List<Entry>> = withContext(Dispatchers.IO) {
        Result.runCatching {
            entriesTable.select {
                order("date", Order.DESCENDING)
                order("time", Order.DESCENDING)
                limit(limit)
            }.decodeList<Entry>()
        }
    }

    suspend fun getEntriesForMonth(yearMonth: String): Result<List<Entry>> = withContext(Dispatchers.IO) {
        Result.runCatching {
            // yearMonth format: "YYYY-MM"
            val startOfMonth = "$yearMonth-01"
            val endOfMonth = "$yearMonth-31" // Simple enough for greater/less comparisons
            
            entriesTable.select {
                filter {
                    gte("date", startOfMonth)
                    lte("date", endOfMonth)
                }
                order("date", Order.ASCENDING)
            }.decodeList<Entry>()
        }
    }
    
    suspend fun getEntriesForRange(startDate: String, endDate: String): Result<List<Entry>> = withContext(Dispatchers.IO) {
        Result.runCatching {
            entriesTable.select {
                filter {
                    gte("date", startDate)
                    lte("date", endDate)
                }
                order("date", Order.DESCENDING)
            }.decodeList<Entry>()
        }
    }

    suspend fun insertEntry(entry: Entry, photoFile: File? = null): Result<Unit> = withContext(Dispatchers.IO) {
        Result.runCatching {
            var imgUrl: String? = null
            
            if (photoFile != null && photoFile.exists()) {
                val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("User not logged in")
                val timestamp = System.currentTimeMillis()
                val path = "$userId/${entry.date}_$timestamp.jpg"
                
                storageBucket.upload(path, photoFile.readBytes())
                imgUrl = storageBucket.publicUrl(path)
            }
            
            val finalEntry = entry.copy(imgUrl = imgUrl)
            entriesTable.insert(finalEntry)
            Unit
        }
    }

    suspend fun deleteEntry(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        Result.runCatching {
            entriesTable.delete {
                filter { eq("id", id) }
            }
            Unit
        }
    }
    
    suspend fun getLatestEntryBefore(date: String): Result<Entry?> = withContext(Dispatchers.IO) {
        Result.runCatching {
            entriesTable.select {
                filter { lt("date", date) }
                order("date", Order.DESCENDING)
                limit(1)
            }.decodeSingleOrNull<Entry>()
        }
    }
}
