package com.indisparte.nfcreader.parser

import android.nfc.NdefRecord
import java.io.UnsupportedEncodingException
import java.util.Arrays

/**
 * Represents a text record parsed from an NDEF message.
 *
 * @property languageCode The ISO/IANA language code associated with this text element.
 * @property text The text content of the record.
 * @author Antonio Di Nuzzo
 */
class TextRecord(private val languageCode: String, private val text: String) : ParsedNdefRecord {

    /**
     * Get the content of the text record as a string.
     *
     * @return The text content of the record.
     */
    override fun str(): String {
        return text
    }

    companion object {
        /**
         * Parse an NDEF record and create a TextRecord if it's a valid text record.
         *
         * @param record The NDEF record to parse.
         * @return A TextRecord if the record is a valid text record, or null otherwise.
         */
        fun parse(record: NdefRecord): TextRecord? {
            return if (isText(record)) {
                try {
                    val payload = record.payload
                    val textEncoding = if (payload[0].toInt() and 128 == 0) Charsets.UTF_8 else Charsets.UTF_16
                    val languageCodeLength = payload[0].toInt() and 63
                    val languageCode = String(payload.copyOfRange(1, 1 + languageCodeLength), Charsets.US_ASCII)
                    val text = String(payload.copyOfRange(1 + languageCodeLength, payload.size), textEncoding)
                    TextRecord(languageCode, text)
                } catch (e: Exception) {
                    throw IllegalArgumentException("Failed to parse TextRecord", e)
                }
            } else {
                null
            }
        }

        /**
         * Check if an NDEF record is a valid text record.
         *
         * @param record The NDEF record to check.
         * @return True if the record is a valid text record, false otherwise.
         */
        fun isText(record: NdefRecord): Boolean {
            return record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                    Arrays.equals(record.type, NdefRecord.RTD_TEXT)
        }
    }
}

