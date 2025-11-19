package com.example.umaklabtrack.supabaseHandler
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

class SupabaseConnection {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://ratfzoobayyqmqegcfyu.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJhdGZ6b29iYXl5cW1xZWdjZnl1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjEzNjU3NjQsImV4cCI6MjA3Njk0MTc2NH0.vWWJzpvjz40UHq-l_V2EQjX3xwaJh17BLyfqSUycZdc"
    ) {
        install(Postgrest)
        install(Auth)
    }

}