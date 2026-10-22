package uz.demo.hambilite

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private var pendingCode: String? = null
    private val callPhoneRequest = 4102
    private val yellow = Color.rgb(255, 210, 31)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = yellow
        window.navigationBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        webView = WebView(this).apply {
            setBackgroundColor(Color.rgb(246, 247, 249))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url ?: return false
                    if (url.scheme == "file") return false
                    return try {
                        startActivity(Intent(Intent.ACTION_VIEW, url))
                        true
                    } catch (_: Exception) { false }
                }
            }
            addJavascriptInterface(UssdBridge(), "AndroidUssd")
            loadUrl("file:///android_asset/index.html")
        }
        setContentView(webView)
    }

    inner class UssdBridge {
        @JavascriptInterface
        fun request(code: String) {
            runOnUiThread {
                if (code !in setOf("*102#", "*103#", "*110*05#", "*106#", "*105#", "*111#")) {
                    reportUssd(code, "Xavfsizlik sababli faqat oldindan belgilangan USSD kodlariga ruxsat beriladi.", false)
                    return@runOnUiThread
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    reportUssd(code, "USSD javobini ilova ichida olish uchun Android 8.0 yoki yangiroq versiya kerak.", false)
                    return@runOnUiThread
                }
                if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    pendingCode = code
                    requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), callPhoneRequest)
                } else {
                    sendUssd(code)
                }
            }
        }
    }

    private fun sendUssd(code: String) {
        reportUssd(code, "Operatorga so‘rov yuborilmoqda. Iltimos, kuting…", false)
        try {
            val base = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            val subId = SubscriptionManager.getDefaultVoiceSubscriptionId()
            val manager = if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) base.createForSubscriptionId(subId) else base
            manager.sendUssdRequest(code, object : TelephonyManager.UssdResponseCallback() {
                override fun onReceiveUssdResponse(telephonyManager: TelephonyManager, request: String, response: CharSequence) {
                    runOnUiThread { reportUssd(request, response.toString(), true) }
                }
                override fun onReceiveUssdResponseFailed(telephonyManager: TelephonyManager, request: String, failureCode: Int) {
                    val reason = when (failureCode) {
                        TelephonyManager.USSD_ERROR_SERVICE_UNAVAIL -> "USSD xizmati hozir mavjud emas."
                        TelephonyManager.USSD_ERROR_RETURN_FAILURE -> "Operator so‘rovni bajara olmadi."
                        TelephonyManager.USSD_ERROR_NOT_SUPPORTED -> "Telefon yoki operator ilova ichidagi USSD javobini qo‘llamaydi."
                        TelephonyManager.USSD_ERROR_NOT_ALLOWED -> "Android yoki operator so‘rovga ruxsat bermadi."
                        else -> "USSD so‘rovi bajarilmadi (xato kodi: $failureCode)."
                    }
                    runOnUiThread { reportUssd(request, "$reason\nBeeline SIM standart qo‘ng‘iroq SIM’i ekanini tekshiring.", false) }
                }
            }, Handler(Looper.getMainLooper()))
        } catch (_: SecurityException) {
            reportUssd(code, "Telefon qo‘ng‘iroq ruxsati berilmadi. Sozlamalardan ruxsatni tekshiring.", false)
        } catch (e: Exception) {
            reportUssd(code, "USSD so‘rovini yuborib bo‘lmadi: ${e.localizedMessage ?: "noma’lum xato"}", false)
        }
    }

    private fun reportUssd(code: String, message: String, success: Boolean) {
        val c = JSONObject.quote(code)
        val m = JSONObject.quote(message)
        webView.post { webView.evaluateJavascript("window.onUssdResult($c,$m,${if (success) "true" else "false"});", null) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == callPhoneRequest) {
            val code = pendingCode
            pendingCode = null
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && code != null) sendUssd(code)
            else if (code != null) reportUssd(code, "USSD uchun telefon ruxsati kerak. Ruxsat berilmagani sababli so‘rov yuborilmadi.", false)
        }
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
