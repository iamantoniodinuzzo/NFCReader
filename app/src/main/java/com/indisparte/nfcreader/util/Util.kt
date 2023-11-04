package com.indisparte.nfcreader.util

import android.text.Html
import android.text.Spanned
import android.widget.ScrollView
import android.widget.TextView

/**
 * @author Antonio Di Nuzzo
 */
object Util {
    /**
     * Log a message to the debug text view.
     * @param header title text of the message, printed in bold
     * @param text optional parameter containing details about the message. Printed in plain text.
     */
    fun TextView.logMessage(header: String, text: String?, scrollView: ScrollView) {
        append(if (text.isNullOrBlank()) fromHtml("<b>$header</b><br>") else fromHtml("<b>$header</b>: $text<br>"))
        scrollView.scrollDown()
    }

    /**
     * Convert HTML formatted strings to spanned (styled) text, for inserting to the TextView.
     * Externalized into an own function as the fromHtml(html) method was deprecated with
     * Android N. This method chooses the right variant depending on the OS.
     * @param html HTML-formatted string to convert to a Spanned text.
     */
    private fun fromHtml(html: String): Spanned {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html)
        }
    }

    /**
     * Scroll the ScrollView to the bottom, so that the latest appended messages are visible.
     */
    fun ScrollView.scrollDown() {
        post { this.smoothScrollTo(0, bottom) }
    }
}