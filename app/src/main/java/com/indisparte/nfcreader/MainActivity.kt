package com.indisparte.nfcreader

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.indisparte.nfcreader.databinding.ActivityMainBinding
import com.indisparte.nfcreader.parser.NdefMessageParser

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var binding: ActivityMainBinding
    private lateinit var nfcStatusText: TextView
    private var nfcAdapter: NfcAdapter? = null
    private var mPendingIntent: PendingIntent? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nfcStatusText = binding.nfcStatusText

        // Inizializzazione del NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)


        if (checkNFCEnable()) {
            mPendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            nfcStatusText.text = getString(R.string.tv_noNfc)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: Enable foreground dispatch")
        nfcAdapter?.enableForegroundDispatch(this, mPendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: Disable Foreground dispatch")
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val action = intent.action
        when (action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                    ?.also { rawMessages ->
                        val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                        // Process the messages array.
                        parserNDEFMessage(messages)
                    }
            }

            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_TAG)?.also { rawMessages ->
                    val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                    // Process the messages array.
//                    parserNDEFMessage(messages)
                    Log.w(TAG, "onNewIntent: $messages")
                }
            }

            Intent.ACTION_MAIN -> {
                Log.i(TAG, "onNewIntent: Main intent")
            }
        }

    }

    private fun parserNDEFMessage(messages: List<NdefMessage>) {
        val builder = StringBuilder()
        val records = NdefMessageParser.parse(messages[0])
        val size = records.size

        for (i in 0 until size) {
            val record = records[i]
            val str = record.str()
            builder.append(str).append("\n")
        }
        Log.d(TAG, "parserNDEFMessage: $builder")
        nfcStatusText.text = builder.toString()

    }

    private fun checkNFCEnable(): Boolean {
        return if (nfcAdapter == null) {
            nfcStatusText.text = getString(R.string.tv_noNfc)
            false
        } else {
            nfcAdapter?.isEnabled == true
        }
    }
}