package com.example.webviewtiktokshop

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var urlInput: EditText
    private lateinit var uaInput: EditText
    private val defaultUrl = "https://seller-es-accounts.tiktok.com/account/register"
    private val defaultUA = "Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.7204.63 Mobile Safari/537.36"
    private lateinit var sharedPref: android.content.SharedPreferences
    private val PREF_UA = "user_agent_pref"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("MainActivity", "onCreate started")
        try {
            setContentView(R.layout.activity_main)
            android.util.Log.d("MainActivity", "setContentView completed")

            // Initialize SharedPreferences
            sharedPref = getSharedPreferences("WebViewPrefs", Context.MODE_PRIVATE)
            android.util.Log.d("MainActivity", "SharedPreferences initialized")

            // Initialize views
            webView = findViewById(R.id.webView)
            urlInput = findViewById(R.id.urlInput)
            uaInput = findViewById(R.id.uaInput)
            val goButton: ImageButton = findViewById(R.id.goButton)
            val refreshButton: ImageButton = findViewById(R.id.refreshButton)
            val backButton: ImageButton = findViewById(R.id.backButton)
            val forwardButton: ImageButton = findViewById(R.id.forwardButton)
            val clearButton: ImageButton = findViewById(R.id.clearButton)
            val menuButton: ImageButton = findViewById(R.id.menuButton)
            
            android.util.Log.d("MainActivity", "Views initialized")

            // Setup WebView
            setupWebView()

            // Set initial URL and User Agent
            urlInput.setText(defaultUrl)
            updateUaInput()

            // Configurar el EditText para responder al botón de búsqueda del teclado
            urlInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO) {
                    loadUrl()
                    hideKeyboard()
                    return@setOnEditorActionListener true
                }
                false
            }

            // Set click listeners
            goButton.setOnClickListener { 
                loadUrl()
                hideKeyboard()
            }
            refreshButton.setOnClickListener { 
                webView.reload()
                hideKeyboard()
            }
            backButton.setOnClickListener { 
                if (webView.canGoBack()) {
                    webView.goBack()
                }
                hideKeyboard()
            }
            forwardButton.setOnClickListener { 
                if (webView.canGoForward()) {
                    webView.goForward()
                }
                hideKeyboard()
            }
            clearButton.setOnClickListener { showClearMenu(clearButton) }
            menuButton.setOnClickListener { showUAMenu(menuButton) }

            // Load initial URL
            loadUrl()
            
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        android.util.Log.d("MainActivity", "setupWebView started")
        try {
            val currentUA = getUserAgent()
            android.util.Log.d("MainActivity", "Current User Agent: $currentUA")
            
            val webSettings = webView.settings
            android.util.Log.d("MainActivity", "WebSettings initialized")
            
            // Basic settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true
            webSettings.databaseEnabled = true
            webSettings.setSupportZoom(true)
            webSettings.builtInZoomControls = true
            webSettings.displayZoomControls = false
            webSettings.useWideViewPort = true
            webSettings.loadWithOverviewMode = true
            webSettings.cacheMode = WebSettings.LOAD_DEFAULT
            webSettings.allowFileAccess = false
            webSettings.allowContentAccess = true
            webSettings.userAgentString = currentUA
            android.util.Log.d("MainActivity", "WebSettings configured")
            
            // Enable mixed content for Android 5.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            // Cookie settings
            try {
                CookieManager.getInstance().apply {
                    setAcceptCookie(true)
                    setAcceptThirdPartyCookies(webView, true)
                }
            } catch (e: Exception) {
                android.util.Log.e("WebView", "Error setting up cookies", e)
            }
            
            // Set WebView clients
            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    android.util.Log.e("WebView", "Error loading page: ${error?.description}")
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    // Update URL in the input field
                    urlInput.setText(url)
                }
            }
            
            webView.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    android.util.Log.d("WebView", "${consoleMessage.message()} -- From line ${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}")
                    return true
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in setupWebView", e)
            throw e
        }

        // WebView clients
        webView.webViewClient = CustomWebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                // Ignore console messages
                return true
            }
        }
    }

    private fun loadUrl() {
        var url = urlInput.text.toString().trim()
        if (url.isNotEmpty()) {
            if (isUrlValid(url)) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }
                webView.loadUrl(url)
            } else {
                // Si no es una URL válida, hacer una búsqueda en Google
                val searchUrl = "https://www.google.com/search?q=${java.net.URLEncoder.encode(url, "UTF-8")}"
                webView.loadUrl(searchUrl)
            }
        }
    }

    private fun isUrlValid(url: String): Boolean {
        return try {
            // Intentar crear una URL válida
            java.net.URL(url).toURI()
            true
        } catch (e: Exception) {
            // Si no es una URL válida, asumimos que es una búsqueda
            false
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(webView.windowToken, 0)
    }

    private fun saveUserAgent(ua: String) {
        sharedPref.edit().putString(PREF_UA, ua).apply()
    }

    private fun getUserAgent(): String {
        return sharedPref.getString(PREF_UA, defaultUA) ?: defaultUA
    }

    private fun updateUaInput() {
        uaInput.setText(getUserAgent())
    }

    private fun showClearMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.clear_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.clear_cache -> {
                    clearCacheOnly()
                    true
                }
                R.id.clear_all -> {
                    clearAllData()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showUAMenu(anchor: View) {
        val popupView = layoutInflater.inflate(R.layout.ua_menu, null)
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        
        // Configurar el fondo del popup
        popupWindow.setBackgroundDrawable(
            resources.getDrawable(
                android.R.drawable.dialog_frame,
                theme
            )
        )
        
        // Configurar los botones
        val btnDesktop = popupView.findViewById<Button>(R.id.btnDesktopUA)
        val btnMobile = popupView.findViewById<Button>(R.id.btnMobileUA)
        val btnApply = popupView.findViewById<Button>(R.id.btnApplyUA)
        val etUA = popupView.findViewById<EditText>(R.id.etUA)
        
        // Cargar el UA actual
        etUA.setText(webView.settings.userAgentString)
        
        // Configurar los listeners
        btnDesktop.setOnClickListener {
            val desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
            etUA.setText(desktopUA)
        }
        
        btnMobile.setOnClickListener {
            val mobileUA = "Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.7204.63 Mobile Safari/537.36"
            etUA.setText(mobileUA)
        }
        
        btnApply.setOnClickListener {
            val newUA = etUA.text.toString()
            if (newUA.isNotEmpty()) {
                saveUserAgent(newUA)
                webView.settings.userAgentString = newUA
                webView.reload()
                Toast.makeText(this, "User Agent actualizado", Toast.LENGTH_SHORT).show()
                popupWindow.dismiss()
            } else {
                Toast.makeText(this, "Ingrese un User Agent válido", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Mostrar el popup
        popupWindow.showAsDropDown(anchor)
    }

    private fun clearCacheOnly() {
        // Limpiar solo la caché
        webView.clearCache(true)
        webView.clearFormData()
        
        // No limpiar cookies ni historial
        
        Toast.makeText(this, "Caché limpiada", Toast.LENGTH_SHORT).show()
        webView.reload()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun clearAllData() {
        // Clear WebView data
        webView.apply {
        
        // Limpiar cookies (esto cerrará la sesión)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        } else {
            val cookieSyncManager = CookieSyncManager.createInstance(this)
            cookieSyncManager.startSync()
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookie()
            cookieManager.removeSessionCookie()
            cookieSyncManager.stopSync()
            cookieSyncManager.sync()
        }
        
        // Limpiar bases de datos del WebView
        this.deleteDatabase("webview.db")
        this.deleteDatabase("webviewCache.db")
        
        Toast.makeText(this, "Todos los datos han sido eliminados (incluyendo sesión)", Toast.LENGTH_LONG).show()
        webView.reload()
    }

    inner class CustomWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            if (url.startsWith("http://") || url.startsWith("https://")) {
                view.loadUrl(url)
                return true
            }
            return false
        }

        override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
            super.onPageStarted(view, url, favicon)
            urlInput.setText(url)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            injectAntiDetectionScript(view)
        }
    }

    @SuppressLint("JavascriptInterface")
    private fun injectAntiDetectionScript(webView: WebView) {
        val antiDetectionScript = """
            // Anti-detection script
            Object.defineProperty(navigator, 'webdriver', { get: () => false });
            
            const originalQuery = window.navigator.permissions.query;
            window.navigator.permissions.query = (parameters) => (
                parameters.name === 'notifications' ? 
                Promise.resolve({ state: Notification.permission }) :
                originalQuery(parameters)
            );
            
            Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] });
            Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en'] });
            
            const originalGetUserMedia = navigator.mediaDevices.getUserMedia;
            navigator.mediaDevices.getUserMedia = (constraints) => {
                return new Promise((resolve, reject) => {
                    originalGetUserMedia.call(navigator.mediaDevices, constraints)
                        .then(stream => resolve(stream))
                        .catch(err => reject(err));
                });
            };
        """.trimIndent()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(antiDetectionScript, null)
        } else {
            webView.loadUrl("javascript:$antiDetectionScript")
        }
    }
}
