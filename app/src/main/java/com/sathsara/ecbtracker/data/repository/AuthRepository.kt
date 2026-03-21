package com.sathsara.ecbtracker.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    val isUserLoggedIn: Flow<Boolean> = supabase.auth.sessionStatus.map {
        it is io.github.jan.supabase.auth.status.SessionStatus.Authenticated
    }

    suspend fun signIn(email: String, password: String) = Result.runCatching {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(email: String, password: String) = Result.runCatching {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() = Result.runCatching {
        supabase.auth.signOut()
    }

    suspend fun updatePassword(newPassword: String) = Result.runCatching {
        supabase.auth.updateUser {
            password = newPassword
        }
    }

    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }
}
