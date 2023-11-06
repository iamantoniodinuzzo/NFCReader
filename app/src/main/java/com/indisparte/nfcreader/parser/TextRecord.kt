package com.indisparte.nfcreader.parser

import android.nfc.NdefRecord
import java.io.UnsupportedEncodingException
import java.util.Arrays

/**
 * Represents a text record parsed from an NDEF message.
 *
 * @property mLanguageCode The ISO/IANA language code associated with this text element.
 * @property mText The text content of the record.
 * @author Antonio Di Nuzzo
 */
class TextRecord(val languageCode: String, val text: String) : ParsedNdefRecord {

    override fun str(): String {
        return text
    }


    companion object {
        fun parse(record: NdefRecord): TextRecord? {
            return if (record.tnf == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(
                    record.type,
                    NdefRecord.RTD_TEXT
                )
            ) {
                try {
                    val payload = record.payload
                    val textEncoding =
                        if (payload[0].toInt() and 128 == 0) Charsets.UTF_8 else Charsets.UTF_16
                    val languageCodeLength = payload[0].toInt() and 63
                    val languageCode =
                        String(payload.copyOfRange(1, 1 + languageCodeLength), Charsets.US_ASCII)
                    val text = String(
                        payload.copyOfRange(1 + languageCodeLength, payload.size),
                        textEncoding
                    )
                    TextRecord(languageCode, text)
                } catch (e: UnsupportedEncodingException) {
                    throw IllegalArgumentException(e)
                }
            } else {
                null
            }
        }

        fun isText(record: NdefRecord): Boolean {
            return try {
                parse(record)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
