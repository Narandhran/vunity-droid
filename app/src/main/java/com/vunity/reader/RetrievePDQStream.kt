package com.vunity.reader

import android.os.AsyncTask
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class RetrievePDQStream : AsyncTask<String, Void, InputStream>() {
    override fun doInBackground(vararg strings: String): InputStream? {
        var inputStream: InputStream? = null
        try {
            val uri = URL(strings[0])
            val urlConnection =
                uri.openConnection() as HttpURLConnection
            if (urlConnection.responseCode == 200) {
                inputStream = BufferedInputStream(urlConnection.inputStream)
            }
        } catch (e: IOException) {
            return null
        }
        return inputStream
    }
}


