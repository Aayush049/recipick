package com.aayush.recipick.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiErrorParserTest {

    @Test
    fun parse_503HighDemandError_returnsFriendlyBusyMessage() {
        val rawJson503 = """
            Unexpected Response:
            {
              "error": {
                "code": 503,
                "message": "This model is currently experiencing high demand. Spikes in demand are usually temporary. Please try again later.",
                "status": "UNAVAILABLE"
              }
            }
            kotlinx.serialization.MissingFieldException: Field 'details' is required for type with serial name 'com.google.ai.client.generativeai.common.server.GRpcError', but it was missing at path: $.error
        """.trimIndent()

        val parsed = GeminiErrorParser.parse(RuntimeException(rawJson503))
        assertEquals(
            "Chef Gemini is currently experiencing high demand. Spikes are usually brief—please tap Retry.",
            parsed
        )
        assertTrue(GeminiErrorParser.isTransient(RuntimeException(rawJson503)))
    }

    @Test
    fun parse_429RateLimitError_returnsRateLimitMessage() {
        val rawError = "Resource has been exhausted (e.g. check quota). 429 RESOURCE_EXHAUSTED"
        val parsed = GeminiErrorParser.parse(RuntimeException(rawError))
        assertEquals("Rate limit reached. Please wait a moment and tap Retry.", parsed)
    }

    @Test
    fun parse_timeoutError_returnsTimeoutMessage() {
        val rawError = "java.net.SocketTimeoutException: timeout"
        val parsed = GeminiErrorParser.parse(RuntimeException(rawError))
        assertEquals("Connection timed out or network unavailable. Please check your connection and tap Retry.", parsed)
        assertTrue(GeminiErrorParser.isTransient(RuntimeException(rawError)))
    }

    @Test
    fun parse_missingApiKey_returnsSettingsPrompt() {
        val rawError = "Gemini API Key is not configured. Please enter your key in Settings."
        val parsed = GeminiErrorParser.parse(IllegalStateException(rawError))
        assertEquals("Gemini API Key is not configured. Please enter your key in Settings.", parsed)
        assertFalse(GeminiErrorParser.isTransient(IllegalStateException(rawError)))
    }

    @Test
    fun parse_invalidApiKey_returnsInvalidKeyMessage() {
        val rawError = "API key not valid. Please pass a valid API key. (400)"
        val parsed = GeminiErrorParser.parse(RuntimeException(rawError))
        assertEquals("Your Gemini API key appears invalid or expired. Please check your key in Settings.", parsed)

        val rawCombo = "Gemini API key is invalid or rate limited."
        val parsedCombo = GeminiErrorParser.parse(RuntimeException(rawCombo))
        assertEquals("Your Gemini API key appears invalid or expired. Please check your key in Settings.", parsedCombo)
    }

    @Test
    fun parse_deserializationError_returnsFriendlyEmptyMessage() {
        val rawError = "Empty response from Gemini AI"
        val parsed = GeminiErrorParser.parse(IllegalStateException(rawError))
        assertEquals("Chef Gemini couldn't craft recipes with those ingredients right now. Tap Retry to try again.", parsed)
    }

    @Test
    fun parse_cleanSentence_preservesMessage() {
        val clean = "Please add at least one ingredient."
        val parsed = GeminiErrorParser.parse(RuntimeException(clean))
        assertEquals(clean, parsed)
    }
}
