package com.shelldocs.core.data.supabase

/** Raised by [SupabasePostgrestApi] when a table request fails. */
class SupabasePostgrestException(message: String) : Exception(message)
