package com.indisparte.nfcreader.parser

/**
 * An interface for parsed NDEF (NFC Data Exchange Format) records.
 * Implementing classes should provide a method to return the record content as a string.
 * @author Antonio Di Nuzzo
 */
interface ParsedNdefRecord {
    /**
     * Get the content of the NDEF record as a string.
     *
     * @return The content of the NDEF record as a string.
     */
    fun str(): String
}
