package com.indisparte.nfcreader.parser

import android.nfc.NdefMessage
import android.nfc.NdefRecord

/**
 * @author Antonio Di Nuzzo
 */
object NdefMessageParser {
    fun parse(message: NdefMessage): List<ParsedNdefRecord> {
        return getRecords(message.records)
    }

    private fun getRecords(records: Array<NdefRecord>): List<ParsedNdefRecord> {
        val elements = ArrayList<ParsedNdefRecord>()

        for (record in records) {
            if (TextRecord.isText(record)) {
                TextRecord.parse(record)?.let { elements.add(it) }
            } else {
                elements.add(object : ParsedNdefRecord {
                    override fun str(): String {
                        return String(record.payload)
                    }
                })
            }
        }

        return elements
    }
}