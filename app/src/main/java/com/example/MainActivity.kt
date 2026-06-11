package com.example

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val results = WebChromeClient.FileChooserParams.parseResult(result.resultCode, data)
            filePathCallback?.onReceiveValue(results)
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable premium edge-to-edge displays
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F0F12))
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F0F12))
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        MockupGeneratorWebView(
                            url = "file:///android_asset/index.html",
                            onShowFileChooser = { callback, params ->
                                this@MainActivity.filePathCallback = callback
                                val intent = params.createIntent()
                                try {
                                    fileChooserLauncher.launch(intent)
                                    true
                                } catch (e: Exception) {
                                    this@MainActivity.filePathCallback?.onReceiveValue(null)
                                    this@MainActivity.filePathCallback = null
                                    false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MockupGeneratorWebView(
    url: String,
    onShowFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // Configure high-performance standard WebView properties for 3D operation
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.databaseEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.mediaPlaybackRequiresUserGesture = false

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        return false // keep loading in current webview viewport
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        if (filePathCallback != null && fileChooserParams != null) {
                            return onShowFileChooser(filePathCallback, fileChooserParams)
                        }
                        return false
                    }
                }

                loadUrl(url)
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
