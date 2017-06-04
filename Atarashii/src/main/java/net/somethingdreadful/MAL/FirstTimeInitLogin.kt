package net.somethingdreadful.MAL

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.bindView
import net.somethingdreadful.MAL.api.ALApi
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.dialog.ChooseDialogFragment

class FirstTimeInitLogin : Fragment(), ChooseDialogFragment.onClickListener {
    val input1: EditText by bindView(R.id.input_username)
    val input2: EditText by bindView(R.id.input_password)
    val webview: WebView by bindView(R.id.webview)
    val loginBlock: RelativeLayout by bindView(R.id.loginBlock)
    val footer: RelativeLayout by bindView(R.id.footer)
    val title: TextView by bindView(R.id.title)
    internal var webviewIsInit = false
    private var firstTimeInit: FirstTimeInit? = null

    @OnClick(R.id.register)
    fun register() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://myanimelist.net/register.php")))
    }

    @OnClick(R.id.socialmedia)
    fun socialMedia() {
        val lcdf = ChooseDialogFragment()
        val bundle = Bundle()
        bundle.putString("title", getString(R.string.form_hint_socialmedia))
        bundle.putString("message", getString(R.string.dialog_message_socialmedia))
        bundle.putString("positive", getString(R.string.dialog_label_password))
        lcdf.arguments = bundle
        lcdf.setCallback(this)
        lcdf.show(activity.fragmentManager, "Dialog")
    }

    /**
     * Switch the layout design for the requested website.

     * Note: should be called after website change
     */
    fun isMal() {
        if (firstTimeInit!!.isMAL) {
            title.text = getString(R.string.init_hint_myanimelist)
            webview.visibility = View.GONE
            loginBlock.visibility = View.VISIBLE
            footer.visibility = View.VISIBLE
            firstTimeInit!!.showDoneButton(true)
        } else {
            title.text = getString(R.string.init_hint_anilist)
            firstTimeInit!!.showDoneButton(false)
            webview.visibility = View.VISIBLE
            loginBlock.visibility = View.GONE
            footer.visibility = View.GONE
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun initWebview() {
        if (!webviewIsInit && APIHelper.isNetworkAvailable(context)) {
            webviewIsInit = true
            webview.loadUrl(ALApi.getAnilistURL())
            CookieManager.getInstance().removeAllCookie()
            webview.settings.javaScriptEnabled = true
            webview.settings.domStorageEnabled = true
            webview.setWebViewClient(object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    val code = ALApi.getCode(url)
                    if (code != null) {
                        input1!!.setText(code)
                        firstTimeInit!!.onDonePressed(targetFragment)
                        return true
                    } else {
                        return false
                    }
                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.init_fragment_login, container, false)
        ButterKnife.bind(this, view)
        isMal()
        initWebview()
        return view
    }

    override fun onPositiveButtonClicked() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://myanimelist.net/editprofile.php?go=myoptions")))
    }

    companion object {

        fun newInstance(firstTimeInit: FirstTimeInit): FirstTimeInitLogin {
            val fragment = FirstTimeInitLogin()
            fragment.firstTimeInit = firstTimeInit
            return fragment
        }
    }
}
