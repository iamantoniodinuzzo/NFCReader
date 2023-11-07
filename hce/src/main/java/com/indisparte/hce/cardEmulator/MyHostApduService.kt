package com.indisparte.hce.cardEmulator

import android.app.Service
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NdefRecord.createTextRecord
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.indisparte.hce.util.Constants.APDU_SELECT
import com.indisparte.hce.util.Constants.A_ERROR
import com.indisparte.hce.util.Constants.A_OKAY
import com.indisparte.hce.util.Constants.CAPABILITY_CONTAINER_OK
import com.indisparte.hce.util.Constants.HEX_CHARS
import com.indisparte.hce.util.Constants.NDEF_ID
import com.indisparte.hce.util.Constants.NDEF_READ_BINARY
import com.indisparte.hce.util.Constants.NDEF_READ_BINARY_NLEN
import com.indisparte.hce.util.Constants.NDEF_SELECT_OK
import com.indisparte.hce.util.Constants.NDEF_URI
import com.indisparte.hce.util.Constants.NDEF_URI_BYTES
import com.indisparte.hce.util.Constants.NDEF_URI_LEN
import com.indisparte.hce.util.Constants.READ_CAPABILITY_CONTAINER
import com.indisparte.hce.util.Constants.READ_CAPABILITY_CONTAINER_CHECK
import com.indisparte.hce.util.Constants.READ_CAPABILITY_CONTAINER_RESPONSE
import com.indisparte.hce.util.Util
import com.indisparte.hce.util.Util.fillByteArrayToFixedDimension
import com.indisparte.hce.util.toHex
import java.io.UnsupportedEncodingException
import java.math.BigInteger

/**
 * This is an Android HostApduService that simulates an NFC card for Host Card Emulation (HCE).
 * It responds to specific APDU (Application Protocol Data Unit) commands used for HCE communication.
 */
class MyHostApduService : HostApduService() {

    private val TAG = "HostApduService"



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Check if an intent with an "ndefMessage" extra was received.
        if (intent?.hasExtra("ndefMessage")!!) {
            // If so, update the NDEF_URI message to the new value.
            NDEF_URI =
                NdefMessage(Util.createTextRecord("en", intent.getStringExtra("ndefMessage")!!, NDEF_ID))

            NDEF_URI_BYTES = NDEF_URI.toByteArray()
            NDEF_URI_LEN = fillByteArrayToFixedDimension(
                BigInteger.valueOf(NDEF_URI_BYTES.size.toLong()).toByteArray(),
                2,
            )
        }

        Log.i(TAG, "onStartCommand() | NDEF$NDEF_URI")

        return Service.START_STICKY
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        //
        // The following flow is based on Appendix E "Example of Mapping Version 2.0 Command Flow"
        // in the NFC Forum specification
        //
        Log.i(TAG, "processCommandApdu() | incoming commandApdu: " + commandApdu.toHex())

        //
        // First command: NDEF Tag Application select (Section 5.5.2 in NFC Forum spec)
        //
        if (APDU_SELECT.contentEquals(commandApdu)) {
            Log.i(TAG, "APDU_SELECT triggered. Our Response: " + A_OKAY.toHex())
            return A_OKAY
        }

        //
        // Second command: Capability Container select (Section 5.5.3 in NFC Forum spec)
        //
        if (CAPABILITY_CONTAINER_OK.contentEquals(commandApdu)) {
            Log.i(TAG, "CAPABILITY_CONTAINER_OK triggered. Our Response: " + A_OKAY.toHex())
            return A_OKAY
        }

        //
        // Third command: ReadBinary data from CC file (Section 5.5.4 in NFC Forum spec)
        //
        if (READ_CAPABILITY_CONTAINER.contentEquals(commandApdu) && !READ_CAPABILITY_CONTAINER_CHECK
        ) {
            Log.i(
                TAG,
                "READ_CAPABILITY_CONTAINER triggered. Our Response: " + READ_CAPABILITY_CONTAINER_RESPONSE.toHex(),
            )
            READ_CAPABILITY_CONTAINER_CHECK = true
            return READ_CAPABILITY_CONTAINER_RESPONSE
        }

        //
        // Fourth command: NDEF Select command (Section 5.5.5 in NFC Forum spec)
        //
        if (NDEF_SELECT_OK.contentEquals(commandApdu)) {
            Log.i(TAG, "NDEF_SELECT_OK triggered. Our Response: " + A_OKAY.toHex())
            return A_OKAY
        }

        if (NDEF_READ_BINARY_NLEN.contentEquals(commandApdu)) {
            // Build our response
            val response = ByteArray(NDEF_URI_LEN.size + A_OKAY.size)
            System.arraycopy(NDEF_URI_LEN, 0, response, 0, NDEF_URI_LEN.size)
            System.arraycopy(A_OKAY, 0, response, NDEF_URI_LEN.size, A_OKAY.size)

            Log.i(TAG, "NDEF_READ_BINARY_NLEN triggered. Our Response: " + response.toHex())

            READ_CAPABILITY_CONTAINER_CHECK = false
            return response
        }

        if (commandApdu.sliceArray(0..1).contentEquals(NDEF_READ_BINARY)) {
            val offset = commandApdu.sliceArray(2..3).toHex().toInt(16)
            val length = commandApdu.sliceArray(4..4).toHex().toInt(16)

            val fullResponse = ByteArray(NDEF_URI_LEN.size + NDEF_URI_BYTES.size)
            System.arraycopy(NDEF_URI_LEN, 0, fullResponse, 0, NDEF_URI_LEN.size)
            System.arraycopy(
                NDEF_URI_BYTES,
                0,
                fullResponse,
                NDEF_URI_LEN.size,
                NDEF_URI_BYTES.size,
            )

            Log.i(TAG, "NDEF_READ_BINARY triggered. Full data: " + fullResponse.toHex())
            Log.i(TAG, "READ_BINARY - OFFSET: $offset - LEN: $length")

            val slicedResponse = fullResponse.sliceArray(offset until fullResponse.size)

            // Build our response
            val realLength = if (slicedResponse.size <= length) slicedResponse.size else length
            val response = ByteArray(realLength + A_OKAY.size)

            System.arraycopy(slicedResponse, 0, response, 0, realLength)
            System.arraycopy(A_OKAY, 0, response, realLength, A_OKAY.size)

            Log.i(TAG, "NDEF_READ_BINARY triggered. Our Response: " + response.toHex())

            READ_CAPABILITY_CONTAINER_CHECK = false
            return response
        }

        //
        // We're doing something outside our scope
        //
        Log.wtf(TAG, "processCommandApdu() | I don't know what's going on!!!")
        return A_ERROR
    }

    override fun onDeactivated(reason: Int) {
        Log.i(TAG, "onDeactivated() Fired! Reason: $reason")
    }






}


