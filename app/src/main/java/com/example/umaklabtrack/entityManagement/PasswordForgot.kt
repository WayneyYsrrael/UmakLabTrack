package com.example.umaklabtrack.entityManagement

import com.example.umaklabtrack.dataClasses.User
import com.example.umaklabtrack.supabaseHandler.SupabaseConnection
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonObject

import io.github.jan.supabase.postgrest.postgrest

private val crdval= CredentialsValidation()

class PasswordForgot {
    private val supabase = SupabaseConnection().supabase

    suspend fun getEmailByPhone(contact: String): String? {
        return try {
            val users = supabase
                .from("users")
                .select()
                .decodeList<User>()

            val user = users.find { it.contact == contact }
            user?.email
        } catch (e: Exception) {
            println("Error fetching user by phone: ${e.message}")
            null
        }
    }

    suspend fun signUpWithEmailOtp(email: String): Boolean {
        return try {
            val session = supabase.auth.signUpWith(OTP) {
                this.email = email
            }
            println("OTP sent successfully to $email! Session: $session")
            true
        } catch (e: Exception) {
            println("Error sending OTP: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun getUsersByName(nameToFind: String): List<User> {
        return try {
            val users = supabase
                .from("users")
                .select {
                    filter {
                        // case-insensitive match
                        User::contact ilike "%$nameToFind%"
                    }
                }
                .decodeList<User>() // decode the result directly into your data class

            users.forEach { user ->
                println("Found user: ${user.name}, email: ${user.email}, contact: ${user.contact}")
            }

            users
        } catch (e: Exception) {
            println("Error fetching users: ${e.message}")
            emptyList()
        }
    }


    suspend fun updatePassword(identifier: String, newPassword: String): Boolean {
        val hashedPassword = crdval.hashPassword(newPassword)

        val params: JsonObject = buildJsonObject {
            put("identifier", identifier)
            put("new_password", hashedPassword)
        }

        println("🔹 RPC parameters: $params")

        return try {
            supabase.postgrest.rpc(
                function = "update_password",
                parameters = params
            )
            println("✅ RPC executed successfully for identifier: $identifier")
            true
        } catch (e: Exception) {
            println("❌ RPC failed for identifier: $identifier")
            e.printStackTrace()
            false
        }
    }










}