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

/**
 * The main activity for handling NFC (Near Field Communication) functionality.
 * This activity checks if NFC is supported and enabled on the device and uses the
 * NFC Reader Mode to detect NFC tags. It provides methods for processing NDEF (NFC Data Exchange Format)
 * messages and displaying the message content.
 *
 * @author Antonio Di Nuzzo
 */
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

        // Scroll the text view down so that the latest messages are visible
        scrollView.scrollDown()
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: Enable foreground reader mode")

        if (nfcAdapter != null) {
            val options = Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250) // Workaround for some NFC firmware issues
            nfcAdapter!!.enableReaderMode(this, this,
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V or
                        NfcAdapter.FLAG_READER_NFC_BARCODE or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                options
            )
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: Disable Foreground reader mode")

        if (nfcAdapter != null) {
            nfcAdapter!!.disableReaderMode(this)
        }
    }

    // This method is run in another thread when a card is discovered
    // !!! This method cannot directly interact with the UI Thread
    // Use `runOnUiThread` method to change the UI from this method
    override fun onTagDiscovered(tag: Tag?) {
        Log.d(TAG, "Tag discovered! ${tag.toString()}")

        // Read and write to Tag here, depending on the Tag Technology Type
        // In this example, the card should be an Ndef Technology Type
        val mNdef = Ndef.get(tag)

        Log.d(TAG, "NDEF: ${mNdef.cachedNdefMessage}")

        // Check if it's an Ndef capable card
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
     * Parse the NDEF message contents and print them to the on-screen log.
     */
    private fun processNdefMessages(ndefMessages: Array<NdefMessage?>) {
        for (curMsg in ndefMessages) {
            if (curMsg != null) {
                Log.i(TAG, "Message $curMsg")
                nfcStatusText.logMessage("Message", curMsg.toString(), scrollView)

                Log.i(TAG, "Records ${curMsg.records.size}")
                nfcStatusText.logMessage("Records", curMsg.records.size.toString(), scrollView)

                val builder = StringBuilder()
                val parsedNdefRecordList = NdefMessageParser.parse(curMsg)
                for (record in parsedNdefRecordList) {
                    val str = record.str()
                    builder.append(str).append("\n")
                }
                nfcStatusText.logMessage(
                    "- Parsed Content",
                    builder.toString(),
                    scrollView
                )
                Log.i(TAG, "- Parsed Contents ${builder.toString()}")
            }
        }
    }
}
