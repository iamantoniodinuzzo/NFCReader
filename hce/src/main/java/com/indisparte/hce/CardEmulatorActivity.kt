package com.indisparte.hce

import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.indisparte.hce.cardEmulator.MyHostApduService
import com.indisparte.hce.databinding.CardEmulatorMainBinding

class CardEmulatorActivity : AppCompatActivity() {

    private lateinit var binding: CardEmulatorMainBinding
    private var mNfcAdapter: NfcAdapter? = null
    private lateinit var button: Button
    private lateinit var editText: EditText
    private lateinit var textView: TextView
    private lateinit var mTurnNfcDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CardEmulatorMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        button = binding.button
        editText = binding.editText
        textView = binding.textView
        initNFCFunction()
    }

    private fun initNFCFunction() {
        if (supportNfcHceFeature()) {
            textView.visibility = View.GONE
            editText.visibility = View.VISIBLE
            button.visibility = View.VISIBLE
            initService()
        } else {
            textView.visibility = View.VISIBLE
            editText.visibility = View.GONE
            button.visibility = View.GONE
            // Prevent phone that doesn't support NFC to trigger dialog
            if (supportNfcHceFeature()) {
                showTurnOnNfcDialog()
            }
        }
    }

    private fun supportNfcHceFeature() =
        checkNFCEnable() && packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)

    private fun initService() {
        button.setOnClickListener {
            if (TextUtils.isEmpty(editText.text)) {
                Toast.makeText(
                    this@CardEmulatorActivity,
                    "Please write something in editText",
                    Toast.LENGTH_LONG,
                ).show()
            } else {
                val intent = Intent(this@CardEmulatorActivity, MyHostApduService::class.java)
                intent.putExtra("ndefMessage", editText.text.toString())
                startService(intent)
            }
        }
    }

    private fun checkNFCEnable(): Boolean {
        return if (mNfcAdapter == null) {
            textView.text = "There is no NFC module on this device"
            false
        } else {
            mNfcAdapter?.isEnabled == true
        }
    }

    private fun showTurnOnNfcDialog() {
        mTurnNfcDialog = MaterialAlertDialogBuilder(this)
            .setTitle("NFC is turned off.")
            .setMessage("You need turn on NFC module for scanning. Wish turn on it now?")
            .setPositiveButton(
                "Turn on",
            ) { _, _ ->
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            }.setNegativeButton("Dismiss") { _, _ ->
                onBackPressedDispatcher.onBackPressed()
            }
            .create()
        mTurnNfcDialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (mNfcAdapter?.isEnabled == true) {
            textView.visibility = View.GONE
            editText.visibility = View.VISIBLE
            button.visibility = View.VISIBLE
            initService()
        }
    }
}
