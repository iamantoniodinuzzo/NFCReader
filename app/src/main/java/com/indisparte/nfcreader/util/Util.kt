package com.indisparte.nfcreader.util

import android.text.Html
import android.text.Spanned
import android.widget.ScrollView
import android.widget.TextView

object Util {
    /**
     * Log a message to the debug text view.
     *
     * @param header The title text of the message, printed in bold.
     * @param text Optional parameter containing details about the message, printed in plain text.
     * @param scrollView The ScrollView to scroll down after appending the message.
     */
    fun TextView.logMessage(header: String, text: String?, scrollView: ScrollView) {
        append(if (text.isNullOrBlank()) fromHtml("<b>$header</b><br>") else fromHtml("<b>$header</b>: $text<br>"))
        scrollView.scrollDown()
    }

    /**
     * Convert HTML formatted strings to spanned (styled) text, for inserting into a TextView.
     * The method chooses the right variant depending on the Android version.
     *
     * @param html The HTML-formatted string to convert to a Spanned text.
     * @return A Spanned text representation of the HTML string.
     */
    private fun fromHtml(html: String): Spanned {
        return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    }

    /**
     * Scroll the ScrollView to the bottom so that the latest appended messages are visible.
     */
    fun ScrollView.scrollDown() {
        post { this.smoothScrollTo(0, bottom) }
    }
}
