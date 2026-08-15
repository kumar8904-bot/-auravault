package com.ati.leadhunter.v54

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val homeUrl = "https://www.ati-gulf.com/ati-lead-hunt/backend/app/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        web = WebView(this)
        setContentView(web)
        web.setBackgroundColor(android.graphics.Color.rgb(7, 17, 31))

        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = false
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            mediaPlaybackRequiresUserGesture = false
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
        }

        web.webChromeClient = WebChromeClient()
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                return handleUri(uri)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url.isNullOrBlank()) return false
                return runCatching { handleUri(Uri.parse(url)) }.getOrDefault(false)
            }
        }

        if (savedInstanceState == null) web.loadUrl(homeUrl)
        else web.restoreState(savedInstanceState)
    }

    private fun handleUri(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase().orEmpty()

        if (scheme == "http" || scheme == "https") {
            val host = uri.host?.lowercase().orEmpty()
            return if (host == "ati-gulf.com" || host == "www.ati-gulf.com") {
                false
            } else {
                openExternal(Intent(Intent.ACTION_VIEW, uri))
                true
            }
        }

        if (scheme == "mailto") {
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            openExternal(Intent.createChooser(intent, "Send email with"))
            return true
        }

        if (scheme == "tel") {
            openExternal(Intent(Intent.ACTION_DIAL, uri))
            return true
        }

        if (scheme == "intent") {
            return try {
                val intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
                openExternal(intent)
                true
            } catch (_: Exception) {
                false
            }
        }

        return try {
            openExternal(Intent(Intent.ACTION_VIEW, uri))
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun openExternal(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No compatible app is installed for this action.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        web.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else confirmExit()
    }

    private fun confirmExit() {
        AlertDialog.Builder(this)
            .setTitle("Exit ATI Lead Hunter?")
            .setMessage("Lead Hunter server automation can continue after you close the app.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Exit") { _, _ -> finish() }
            .show()
    }
}
