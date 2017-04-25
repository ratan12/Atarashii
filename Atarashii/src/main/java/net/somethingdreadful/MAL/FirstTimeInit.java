package net.somethingdreadful.MAL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.IGFModel;
import net.somethingdreadful.MAL.tasks.AuthenticationCheckTask;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.TaskJob;

import java.util.ArrayList;

public class FirstTimeInit extends AppIntro implements AuthenticationCheckTask.AuthenticationCheckListener, NetworkTask.NetworkTaskListener {
    public boolean isMAL = true;
    public String username;
    public String password;
    private ProgressDialog dialog;
    private int loadedRecords = 0;
    public FirstTimeInitLogin firstTimeInitLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstTimeInitLogin = FirstTimeInitLogin.newInstance(this);

        addSlide(AppIntroFragment.newInstance(getString(R.string.app_name), getString(R.string.app_welcome), R.drawable.icon, getResources().getColor(R.color.primary)));
        addSlide(FirstTimeInitChoose.newInstance(this));
        addSlide(firstTimeInitLogin);

        setBarColor(getResources().getColor(R.color.accent));
        setSeparatorColor(getResources().getColor(android.R.color.black));
        showSkipButton(false);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        if (APIHelper.isNetworkAvailable(this)) {
            // Get username and password from the inputviews
            if (firstTimeInitLogin.input1 != null && firstTimeInitLogin.input2 != null) {
                username = firstTimeInitLogin.input1.getText().toString();
                password = firstTimeInitLogin.input2.getText().toString();

                // Create loading dialog
                dialog = new ProgressDialog(this);
                dialog.setIndeterminate(true);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setTitle(getString(R.string.dialog_title_Verifying));
                dialog.setMessage(getString(R.string.dialog_message_Verifying));
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                // Make auth check request
                if (isMAL)
                    new AuthenticationCheckTask(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username, password);
                else
                    new AuthenticationCheckTask(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username);
            } else {
                dialog.dismiss();
                Theme.Snackbar(this, R.string.toast_error_layout);
            }
        } else {
            dialog.dismiss();
            Theme.Snackbar(this, R.string.toast_error_noConnectivity);
        }
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }

    @Override
    public void onAuthenticationCheckFinished(boolean isValid) {
        if (isValid) {
            // Load anime and Manga records
            IGFModel.coverText = getResources().getStringArray(R.array.igf_strings);
            loadedRecords = 0;
            LoadRecords(true);
            LoadRecords(false);

            // Change dialog text
            dialog.setTitle(getString(R.string.dialog_title_records) + " (" + loadedRecords + "/2)");
            dialog.setMessage(getString(R.string.dialog_message_records));
        } else {
            dialog.dismiss();
            Theme.Snackbar(this, R.string.toast_error_VerifyProblem);
        }
    }

    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, boolean isAnime) {
        checkRecords();
    }

    @Override
    public void onNetworkTaskError(TaskJob job) {
        checkRecords();
    }

    /**
     * Check if both records are loadedRecords and launch the main activity
     */
    private void checkRecords() {
        loadedRecords++;
        dialog.setTitle(getString(R.string.dialog_title_records) + " (" + loadedRecords + "/2)");
        if (loadedRecords == 2) {
            dialog.dismiss();
            Intent goHome = new Intent(this, Home.class);
            startActivity(goHome);
            finish();
        }
    }

    /**
     * Create List loading request
     *
     * @param isAnime Boolean if the animelist or mangalist should be loadedRecords
     */
    private void LoadRecords(boolean isAnime) {
        ArrayList<String> args = new ArrayList<>();
        args.add(ContentManager.listSortFromInt(0, isAnime));
        args.add(String.valueOf(1));
        args.add(String.valueOf(false));

        NetworkTask networkTask = new NetworkTask(TaskJob.FORCESYNC, isAnime, this, new Bundle(), this);
        networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args.toArray(new String[args.size()]));
    }
}

