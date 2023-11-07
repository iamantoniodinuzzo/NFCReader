package com.indisparte.nfcreader.parser

import android.nfc.NdefMessage
import android.nfc.NdefRecord


/**
 * Helper class for parsing NDEF (NFC Data Exchange Format) messages and extracting their contents.
 *
 * @author Antonio Di Nuzzo
 */
object NdefMessageParser {

    /**
     * Parses an NDEF message and returns a list of parsed NDEF records.
     *
     * @param message The NDEF message to parse.
     * @return A list of parsed NDEF records.
     */
    fun parse(message: NdefMessage): List<ParsedNdefRecord> {
        return getRecords(message.records)
    }

    /**
     * Extracts and parses NDEF records from an array of NDEF records.
     *
     * @param records The array of NDEF records to extract and parse.
     * @return A list of parsed NDEF records.
     */
    private fun getRecords(records: Array<NdefRecord>): List<ParsedNdefRecord> {
        return records.map { record ->
            val parsedRecord = TextRecord.parse(record) ?: createGenericParsedRecord(record)
            parsedRecord
        }
    }

    /**
     * Creates a generic parsed NDEF record.
     *
     * @param record The NDEF record to parse.
     * @return A generic parsed NDEF record.
     */
    private fun createGenericParsedRecord(record: NdefRecord): ParsedNdefRecord {
        return object : ParsedNdefRecord {
            override fun str(): String {
                return String(record.payload)
            }
        }
    }
}

