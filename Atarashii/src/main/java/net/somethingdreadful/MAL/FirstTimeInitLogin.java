package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.somethingdreadful.MAL.api.ALApi;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.dialog.ChooseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FirstTimeInitLogin extends Fragment implements ChooseDialogFragment.onClickListener {
    @BindView(R.id.input_username) public EditText input1;
    @BindView(R.id.input_password) public EditText input2;
    @BindView(R.id.webview) public WebView webview;
    @BindView(R.id.loginBlock) public RelativeLayout loginBlock;
    @BindView(R.id.footer) public RelativeLayout footer;
    @BindView(R.id.title) public TextView title;
    boolean webviewIsInit = false;
    private FirstTimeInit firstTimeInit;

    public static FirstTimeInitLogin newInstance(FirstTimeInit firstTimeInit) {
        FirstTimeInitLogin fragment = new FirstTimeInitLogin();
        fragment.firstTimeInit = firstTimeInit;
        return fragment;
    }

    @OnClick(R.id.register)
    public void register() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://myanimelist.net/register.php")));
    }

    @OnClick(R.id.socialmedia)
    public void socialMedia() {
        ChooseDialogFragment lcdf = new ChooseDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", getString(R.string.form_hint_socialmedia));
        bundle.putString("message", getString(R.string.dialog_message_socialmedia));
        bundle.putString("positive", getString(R.string.dialog_label_password));
        lcdf.setArguments(bundle);
        lcdf.setCallback(this);
        lcdf.show(getActivity().getFragmentManager(), "Dialog");
    }

    /**
     * Switch the layout design for the requested website.
     *
     * Note: should be called after website change
     */
    public void isMal() {
        if (firstTimeInit.isMAL) {
            title.setText(getString(R.string.init_hint_myanimelist));
            webview.setVisibility(View.GONE);
            loginBlock.setVisibility(View.VISIBLE);
            footer.setVisibility(View.VISIBLE);
            firstTimeInit.showDoneButton(true);
        } else {
            title.setText(getString(R.string.init_hint_anilist));
            firstTimeInit.showDoneButton(false);
            webview.setVisibility(View.VISIBLE);
            loginBlock.setVisibility(View.GONE);
            footer.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initWebview() {
        if (!webviewIsInit && APIHelper.isNetworkAvailable(getContext())) {
            webviewIsInit = true;
            webview.loadUrl(ALApi.getAnilistURL());
            CookieManager.getInstance().removeAllCookie();
            webview.getSettings().setJavaScriptEnabled(true);
            webview.getSettings().setDomStorageEnabled(true);
            webview.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    String code = ALApi.getCode(url);
                    if (code != null) {
                        input1.setText(code);
                        firstTimeInit.onDonePressed(getTargetFragment());
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.init_fragment_login, container, false);
        ButterKnife.bind(this, view);
        isMal();
        initWebview();
        return view;
    }

    @Override
    public void onPositiveButtonClicked() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://myanimelist.net/editprofile.php?go=myoptions")));
    }
}
