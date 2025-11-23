package com.example.umaklabtrack.entityManagement
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import com.example.umaklabtrack.supabaseHandler.SupabaseConnection
import com.example.umaklabtrack.dataClasses.User
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.OtpType
import java.security.MessageDigest
import android.content.Context
import com.example.umaklabtrack.dataClasses.UserSession
import com.example.umaklabtrack.preferences.SessionPreferences
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CredentialsValidation {
    private val supabase = SupabaseConnection().supabase
    suspend fun isEmailDuplicate(user: User): Boolean {
        return try {
            val users = supabase
                .from("users")
                .select()
                .decodeList<User>()

            val emailExists = users.any { it.email.equals(user.email, ignoreCase = true) }

            emailExists
        } catch (e: Exception) {
            println("Error fetching users: ${e.message}")
            false
        }
    }

    suspend fun signInWithEmailOtp(user: User): Boolean {
        return try {
            val session = supabase.auth.signInWith(OTP) {
                email = user.email
            }
            println("OTP sent successfully! Session: $session")
            true
        } catch (e: Exception) {
            println("Error sending OTP: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun signInWithEmailOnly(email: String): Boolean {
        return try {
            val session = supabase.auth.signInWith(OTP) { this.email = email }
            println("OTP sent successfully! Session: $session")
            true
        } catch (e: Exception) {
            println("Error sending OTP: ${e.message}")
            e.printStackTrace()
            false
        }
    }



    suspend fun verifyEmailOtp(email: String, otpCode: String): Boolean {
        println("🔍 Verifying OTP for: $email")
        println("🔢 User-entered OTP: $otpCode")

        return try {
            supabase.auth.verifyEmailOtp(
                type = OtpType.Email.EMAIL,
                email = email,
                token = otpCode
            )
            println("✅ OTP verification successful!")
            true
        } catch (e: Exception) {
            println("OTP verification failed: ${e.message}")
            false
        }
    }
    fun getSavedEmail(context: Context): String {
        val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return prefs.getString("saved_email", "") ?: ""
    }

    fun getSavedPassword(context: Context): String {
        val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return prefs.getString("saved_password", "") ?: ""
    }

    fun isRememberMe(context: Context): Boolean {
        val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("remember_me", false)
    }

    fun clearLoginPrefs(context: Context) {
        val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()  // removes all saved values
    }

    suspend fun insertUser(newUser: User) {
        try {
            val result = supabase
                .from("users")
                .insert(newUser)
            println("User inserted successfully: $result")
        } catch (e: Exception) {
            println("Error inserting user: ${e.message}")
        }
    }

    suspend fun loginUser(
        email: String,
        hashedPassword: String,
        rememberMe: Boolean,
        context: Context,
        password: String,
    ): String? { // <-- return role as String or null
        return try {
            val params = buildJsonObject {
                put("user_email", email)
                put("user_password", hashedPassword)
            }

            println("🔹 RPC parameters: $params")
            val user = supabase.postgrest.rpc(
                function = "login_user",
                parameters = params
            ).decodeList<User>().firstOrNull() // safer than decodeSingle

            if (user != null) {
                UserSession.name= user.name
                UserSession.USER_ID = user.id
                println("User found with matching email and password")

                // Save remember me prefs
                val sharedPref = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    if (rememberMe) {
                        putString("saved_email", email)
                        putString("saved_password", password)
                        putBoolean("remember_me", rememberMe)
                    } else {
                        clear()
                    }
                    apply()
                }

                // Save session including role
                val sessionPrefs = SessionPreferences(context)
                sessionPrefs.saveSession(
                    userId = user.id ?: return null, // fail-safe
                    name = user.name ?: "",
                    email = user.email ?: "",
                    cNum = user.contact ?: "",
                    role = user.role ?: "user"
                )

                user.role ?: "user" // return the role
            } else {
                println("No user found with this email / password")
                null
            }

        } catch (e: Exception) {
            println("❌ Error calling RPC: ${e.message}")
            e.printStackTrace()
            null
        }
    }



    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }



}
