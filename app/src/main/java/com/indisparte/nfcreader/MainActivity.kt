package com.indisparte.nfcreader

import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.indisparte.nfcreader.databinding.ActivityMainBinding
import com.indisparte.nfcreader.parser.NdefMessageParser
import com.indisparte.nfcreader.util.Util.logMessage
import com.indisparte.nfcreader.util.Util.scrollDown


class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var binding: ActivityMainBinding
    private lateinit var nfcStatusText: TextView
    private lateinit var scrollView: ScrollView

    // NFC adapter for checking NFC state in the device
    private var nfcAdapter: NfcAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nfcStatusText = binding.tvMessages
        scrollView = binding.svMessages

        // Check if NFC is supported and enabled
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcStatusText.logMessage("NFC supported", (nfcAdapter != null).toString(), scrollView)
        nfcStatusText.logMessage("NFC enabled", (nfcAdapter?.isEnabled).toString(), scrollView)


        // Make sure the text view is scrolled down so that the latest messages are visible
        scrollView.scrollDown()


    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: Enable foreground reader mode")

        //From https://stackoverflow.com/questions/64920307/how-to-write-ndef-records-to-nfc-tag/64921434#64921434
        if (nfcAdapter != null) {
            val options = Bundle()
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

            // Enable ReaderMode for all types of card and disable platform sounds
            nfcAdapter!!.enableReaderMode(
                this,
                this,
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V or NfcAdapter.FLAG_READER_NFC_BARCODE or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                options
            )
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: Disable Foreground reader mode")

        //From https://stackoverflow.com/questions/64920307/how-to-write-ndef-records-to-nfc-tag/64921434#64921434
        if (nfcAdapter != null) nfcAdapter!!.disableReaderMode(this);
    }


    // This method is run in another thread when a card is discovered
    // !!!! This method cannot  direct interact with the UI Thread
    // Use `runOnUiThread` method to change the UI from this method
    override fun onTagDiscovered(tag: Tag?) {
        Log.d(TAG, "Tag discovered! ${tag.toString()}")
        
        // Read and or write to Tag here to the appropriate Tag Technology type class
        // in this example the card should be an Ndef Technology Type
        val mNdef = Ndef.get(tag)

        Log.d(TAG, "NDEF: $mNdef")

        // Check that it is an Ndef capable card
        if (mNdef != null) {

            mNdef.connect()
            val mNdefMessage = mNdef.cachedNdefMessage

            Log.w(TAG, "onTagDiscovered message: $mNdefMessage")

            runOnUiThread {
                processNdefMessages(arrayOf(mNdefMessage))
            }

        }

    }


    /**
     * Parse the NDEF message contents and print these to the on-screen log.
     */
    private fun processNdefMessages(ndefMessages: Array<NdefMessage?>) {
        // Go through all NDEF messages found on the NFC tag
        for (curMsg in ndefMessages) {
            if (curMsg != null) {
                // Print generic information about the NDEF message
                Log.i(TAG, "Message $curMsg")
                nfcStatusText.logMessage("Message", curMsg.toString(), scrollView)

                // The NDEF message usually contains 1+ records - print the number of records
                Log.i(TAG, "Records ${curMsg.records.size}")
                nfcStatusText.logMessage("Records", curMsg.records.size.toString(), scrollView)


                val builder = StringBuilder()
                val parsedNdefRecordList = NdefMessageParser.parse(curMsg)
                // Loop through all the records contained in the message
                for (record in parsedNdefRecordList) {
                    val str = record.str()
                    builder.append(str).append("\n")
                }
                nfcStatusText.logMessage(
                    "- Parsed Content", builder.toString(), scrollView
                )
                Log.i(TAG, "- Parsed Contents ${builder.toString()}")

            }
        }
    }


}