package com.indisparte.nfcreader

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.nfc.TagLostException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.indisparte.nfcreader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inizializzazione del NfcAdapter
        val nfcManager = getSystemService(NFC_SERVICE) as NfcManager
        nfcAdapter = nfcManager.defaultAdapter

        // Verifica se il dispositivo supporta l'NFC
        if (nfcAdapter == null) {
            // Il dispositivo non supporta l'NFC
            binding.nfcStatusText.text = "NFC non supportato su questo dispositivo"
        } else {
            // Imposta il click listener per il pulsante NFC
            binding.nfcButton.setOnClickListener {
                // Avvia l'attivazione dell'NFC
                enableNfc()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // Gestisci l'intent NFC quando un dispositivo NFC è rilevato
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent?.action) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)

            // Leggi i dati dal tag NFC
            try {
                // Qui puoi gestire la lettura dei dati dal tag NFC
                // Ad esempio, puoi ottenere gli ID o i dati dal tag e mostrarli all'utente
                // Esempio: val id = tag?.id
                // Mostra i dati all'utente
                binding.nfcStatusText.text = "Dati NFC rilevati: ${tag.toString()}"
            } catch (e: TagLostException) {
                // Il tag NFC è stato perso durante la lettura
                binding.nfcStatusText.text = "Tag NFC perso durante la lettura"
            }
        }
    }

    private fun enableNfc() {
        // Controlla se l'NFC è abilitato sul dispositivo
        if (!nfcAdapter?.isEnabled!!) {
            // Se l'NFC è disabilitato, mostra un messaggio all'utente per attivarlo
            binding.nfcStatusText.text = "Si prega di attivare l'NFC"
        } else {
            // L'NFC è già abilitato, attende il rilevamento del dispositivo NFC
            binding.nfcStatusText.text = "In attesa di un dispositivo NFC..."
        }
    }

    override fun onResume() {
        super.onResume()

        val intent = Intent(this, this.javaClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }


    override fun onPause() {
        super.onPause()
        // Disabilita il dispatch NFC in pausa
        nfcAdapter?.disableForegroundDispatch(this)
    }
}