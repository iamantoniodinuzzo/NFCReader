package com.indisparte.nfcreader

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.indisparte.nfcreader.databinding.ActivityMainBinding
import com.indisparte.nfcreader.util.Util.logMessage
import com.indisparte.nfcreader.util.Util.scrollDown


class MainActivity : AppCompatActivity()/*, NfcAdapter.ReaderCallback*/ {

    private var tagInRange: Boolean = false

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var binding: ActivityMainBinding
    private lateinit var nfcStatusText: TextView
    private lateinit var scrollView: ScrollView

    // NFC adapter for checking NFC state in the device
    private var nfcAdapter: NfcAdapter? = null

    // Pending intent for NFC intent foreground dispatch.
    // Used to read all NDEF tags while the app is running in the foreground.
    private var nfcPendingIntent: PendingIntent? = null

    // Optional: filter NDEF tags this app receives through the pending intent.
    //private var nfcIntentFilters: Array<IntentFilter>? = null


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


        // Read all tags when app is running and in the foreground
        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        nfcPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        // Optional: Setup an intent filter from code for a specific NDEF intent
        // Use this code if you are only interested in a specific intent and don't want to
        // interfere with other NFC tags.
        // In this example, the code is commented out so that we get all NDEF messages,
        // in order to analyze different NDEF-formatted NFC tag contents.
        //val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        //ndef.addCategory(Intent.CATEGORY_DEFAULT)
        //ndef.addDataScheme("https")
        //ndef.addDataAuthority("*.indisparte.com", null)
        //ndef.addDataPath("/", PatternMatcher.PATTERN_PREFIX)
        // More information: https://stackoverflow.com/questions/30642465/nfc-tag-is-not-discovered-for-action-ndef-discovered-action-even-if-it-contains
        //nfcIntentFilters = arrayOf(ndef)

        if (intent != null) {
            // Check if the app was started via an NDEF intent
            nfcStatusText.logMessage(
                "Found intent in onCreate", intent.action.toString(), scrollView
            )
            Log.i(TAG, "Found intent in onCreate: ${intent.action.toString()}")
            processIntent(intent)
        }

        // Make sure the text view is scrolled down so that the latest messages are visible
        scrollView.scrollDown()


    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: Enable foreground dispatch")
        // Get all NDEF discovered intents
        // Makes sure the app gets all discovered NDEF messages as long as it's in the foreground.
        nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
        // Alternative: only get specific HTTP NDEF intent
        //nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, nfcIntentFilters, null)
        /*
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
                }*/
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: Disable Foreground dispatch")
        // Disable foreground dispatch, as this activity is no longer in the foreground
        nfcAdapter?.disableForegroundDispatch(this)

        /*  //From https://stackoverflow.com/questions/64920307/how-to-write-ndef-records-to-nfc-tag/64921434#64921434
          if (nfcAdapter != null) nfcAdapter!!.disableReaderMode(this);*/
    }

    /**
     * For reading the NFC when the app is already launched
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        nfcStatusText.logMessage(
            "Found intent in onNewIntent", intent?.action.toString(), scrollView
        )
        Log.w(TAG, "Found intent in onNewIntent : ${intent?.action.toString()}")
        // If we got an intent while the app is running, also check if it's a new NDEF message
        // that was discovered
        if (intent != null) processIntent(intent)

    }

    /**
     * Check if the Intent has the action "ACTION_NDEF_DISCOVERED". If yes, handle it
     * accordingly and parse the NDEF messages.
     * @param checkIntent the intent to parse and handle if it's the right type
     */
    private fun processIntent(checkIntent: Intent) {
        // Check if intent has the action of a discovered NFC tag
        // with NDEF formatted contents
        if (checkIntent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            nfcStatusText.logMessage("New NDEF intent", checkIntent.toString(), scrollView)
            Log.d(TAG, "New NDEF intent $checkIntent")

            // Retrieve the raw NDEF message from the tag
            val rawMessages = checkIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            nfcStatusText.logMessage("Raw messages", rawMessages?.size.toString(), scrollView)
            Log.d(TAG, "Raw messages ${rawMessages?.size.toString()}")

            // Complete variant: parse NDEF messages
            if (rawMessages != null) {
                val messages =
                    arrayOfNulls<NdefMessage?>(rawMessages.size)// Array<NdefMessage>(rawMessages.size, {})
                for (i in rawMessages.indices) {
                    messages[i] = rawMessages[i] as NdefMessage;
                }
                // Process the messages array.
                processNdefMessages(messages)
            }

            // Simple variant: assume we have 1x URI record
            //if (rawMessages != null && rawMessages.isNotEmpty()) {
            //    val ndefMsg = rawMessages[0] as NdefMessage
            //    if (ndefMsg.records != null && ndefMsg.records.isNotEmpty()) {
            //        val ndefRecord = ndefMsg.records[0]
            //        if (ndefRecord.toUri() != null) {
            //            logMessage("URI detected", ndefRecord.toUri().toString())
            //        } else {
            //            // Other NFC Tags
            //            logMessage("Payload", ndefRecord.payload.contentToString())
            //        }
            //    }
            //}

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

                // Loop through all the records contained in the message
                for (curRecord in curMsg.records) {
                    if (curRecord.toUri() != null) {
                        // URI NDEF Tag
                        nfcStatusText.logMessage("- URI", curRecord.toUri().toString(), scrollView)
                        Log.i(TAG, "- URI ${curRecord.toUri()}")
                    } else {
                        // Other NDEF Tags - simply print the payload
                        nfcStatusText.logMessage(
                            "- Contents", curRecord.payload.contentToString(), scrollView
                        )
                        Log.i(TAG, "- Contents ${curRecord.payload.contentToString()}")
                    }
                }
            }
        }
    }


    /* // This method is run in another thread when a card is discovered
     // !!!! This method cannot  direct interact with the UI Thread
     // Use `runOnUiThread` method to change the UI from this method
     override fun onTagDiscovered(tag: Tag?) {
          Log.d(TAG, "Tag discovered!")
          // Read and or write to Tag here to the appropriate Tag Technology type class
          // in this example the card should be an Ndef Technology Type
          val mNdef = Ndef.get(tag)

          Log.d(TAG, "NDEF: $mNdef")

          // Check that it is an Ndef capable card
          if (mNdef != null) {

              // If we want to read
              // As we did not turn on the NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
              // We can get the cached Ndef message the system read for us.

              val mNdefMessage = mNdef.cachedNdefMessage

              Log.w(TAG, "onTagDiscovered message: $mNdefMessage")

          }

     }*/
}