package com.kimstik.social_mediatz_2

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.kimstik.social_mediatz_2.databinding.ActivityMainBinding

class MainActivity: AppCompatActivity() {

    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private val REQUEST_SELECT_FILE = 100

    private var bind: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        savedInstanceState?.let {bind?.wv?.restoreState(savedInstanceState)}
        setContentView(bind?.root)

        if(savedInstanceState == null) {
            showWebView("https://yandex.ru/")
        } else {
            bind?.wv?.let {onOption(it)}
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if(event.action === KeyEvent.ACTION_DOWN) {
            return when(keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if(bind?.wv != null) {
                        if(bind?.wv!!.canGoBack()) {
                            bind?.wv?.goBack()
                        } else {
                            finish()
                        }
                        true
                    } else {
                        false
                    }
                }
                else                  -> true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun onOption(webView: WebView) {
        webView.apply {
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            isFocusableInTouchMode = true
            isFocusable = true
            isSaveEnabled = true
            webViewClient = MyWebViewClient()
            webChromeClient = object: WebChromeClient() {

                override fun onShowFileChooser(mWebView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
                    if(uploadMessage != null) {
                        uploadMessage!!.onReceiveValue(null)
                        uploadMessage = null
                    }
                    uploadMessage = filePathCallback
                    val intent = fileChooserParams.createIntent()
                    try {
                        startActivityForResult(intent, REQUEST_SELECT_FILE)
                    } catch(e: ActivityNotFoundException) {
                        uploadMessage = null
                        return false
                    }
                    return true
                }
            }
            settings.apply {
                domStorageEnabled = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                defaultTextEncodingName = "utf-8"
                javaScriptCanOpenWindowsAutomatically = true
                loadWithOverviewMode = true
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                loadsImagesAutomatically = true
                cacheMode = WebSettings.LOAD_DEFAULT
                databaseEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                allowContentAccess = true
                useWideViewPort = true
                mixedContentMode = 0
                allowFileAccess = true;
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun showWebView(link: String) {
        bind?.wv?.apply {
            onOption(this)
            loadUrl(link)
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        bind?.wv?.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    inner class MyWebViewClient: WebViewClient() {

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            val weather = url?.contains("yandex.ru/pogoda", true)
            val map = url?.contains("yandex.ru/maps", true)
            var application: PackageInfo? = null
            when(true) {
                weather -> {
                    val packageName = "ru.yandex.weatherplugin"
                    try {
                        application = packageManager.getPackageInfo(packageName, 0)
                    } catch(ex: Exception) {
                        Log.wtf("ERROR", ex.toString())
                        return false
                    }

                    if(application != null) {
                        val intent = packageManager.getLaunchIntentForPackage(packageName)
                        startActivity(intent)
                    }
                    return true
                }
                map     -> {
                    val packageName = "ru.yandex.yandexmaps"
                    try {
                        application = packageManager.getPackageInfo(packageName, 0)
                    } catch(ex: Exception) {
                        Log.wtf("ERROR", ex.toString())
                        return false
                    }
                    if(application != null) {
                        val intent = packageManager.getLaunchIntentForPackage(packageName)
                        startActivity(intent)
                    }
                    return true
                }
                else    -> {
                    return false
                }
            }
        }
    }
}