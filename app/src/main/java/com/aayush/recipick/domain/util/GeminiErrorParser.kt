package com.aayush.recipick.domain.util

object GeminiErrorParser {

    fun isTransient(throwable: Throwable): Boolean {
        val msg = (throwable.message ?: throwable.localizedMessage.orEmpty()).lowercase()
        return msg.contains("503") ||
                msg.contains("high demand") ||
                msg.contains("unavailable") ||
                msg.contains("timeout") ||
                msg.contains("timed out")
    }

    fun parse(throwable: Throwable): String {
        val rawMessage = throwable.message ?: throwable.localizedMessage.orEmpty()
        val lower = rawMessage.lowercase()

        return when {
            // High demand / 503 Service Unavailable / Spikes in demand
            lower.contains("503") ||
            lower.contains("high demand") ||
            lower.contains("unavailable") ||
            lower.contains("spikes in demand") ||
            lower.contains("overloaded") ||
            lower.contains("server is busy") -> {
                "Chef Gemini is currently experiencing high demand. Spikes are usually brief—please tap Retry."
            }

            // Missing API Key
            lower.contains("api key is not configured") || lower.contains("key is not configured") -> {
                "Gemini API Key is not configured. Please enter your key in Settings."
            }

            // Invalid / Unauthorized API Key (400/403)
            lower.contains("api_key_invalid") ||
            lower.contains("api key not valid") ||
            lower.contains("permission_denied") ||
            lower.contains("invalid api key") ||
            (lower.contains("api key") && lower.contains("invalid")) ||
            (lower.contains("400") && lower.contains("key")) ||
            (lower.contains("403") && lower.contains("key")) -> {
                "Your Gemini API key appears invalid or expired. Please check your key in Settings."
            }

            // Quota / Rate limit (429)
            lower.contains("429") ||
            lower.contains("resource_exhausted") ||
            lower.contains("quota") ||
            lower.contains("rate limit") -> {
                "Rate limit reached. Please wait a moment and tap Retry."
            }

            // Network / Timeout
            lower.contains("timeout") ||
            lower.contains("timed out") ||
            lower.contains("sockettimeout") ||
            lower.contains("connectexception") ||
            lower.contains("unknownhost") ||
            lower.contains("no address associated") ||
            lower.contains("network") -> {
                "Connection timed out or network unavailable. Please check your connection and tap Retry."
            }

            // Missing field / Deserialization / Empty response
            lower.contains("empty response") ||
            lower.contains("missingfieldexception") ||
            lower.contains("serialization") ||
            lower.contains("decode") -> {
                "Chef Gemini couldn't craft recipes with those ingredients right now. Tap Retry to try again."
            }

            // If it's already a clean human-readable sentence without brackets or stack traces
            !rawMessage.contains("{") && !rawMessage.contains("Exception") && !rawMessage.contains("class") && rawMessage.length in 5..120 -> {
                rawMessage
            }

            else -> {
                "Chef Gemini is temporarily unavailable. Please tap Retry."
            }
        }
    }
}
