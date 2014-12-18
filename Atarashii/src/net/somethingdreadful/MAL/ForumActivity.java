package net.somethingdreadful.MAL;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ViewFlipper;

import net.somethingdreadful.MAL.tasks.ForumJob;

public class ForumActivity extends ActionBarActivity {

    ForumsMain main;
    ForumsTopics topics;
    ForumsPosts posts;
    FragmentManager manager;
    ViewFlipper viewFlipper;
    ForumJob task;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        manager = getFragmentManager();
        main = (ForumsMain) manager.findFragmentById(R.id.main);
        topics = (ForumsTopics) manager.findFragmentById(R.id.topics);
        posts = (ForumsPosts) manager.findFragmentById(R.id.posts);

        viewFlipper.setDisplayedChild(savedInstanceState == null ? 0 : savedInstanceState.getInt("child"));
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putInt("child", viewFlipper.getDisplayedChild());
        super.onSaveInstanceState(state);
    }

    public void getTopics(int id) {
        viewFlipper.setDisplayedChild(1);
        setTask(topics.setId(id, ForumJob.TOPICS));
    }

    public void getSubBoard(int id) {
        viewFlipper.setDisplayedChild(1);
        setTask(topics.setId(id, ForumJob.SUBBOARD));
    }

    public void getPosts(int id) {
        viewFlipper.setDisplayedChild(2);
        setTask(posts.setId(id));
    }

    private void back() {
        if (viewFlipper.getDisplayedChild() - 1 != -1)
            viewFlipper.setDisplayedChild(viewFlipper.getDisplayedChild() - 1);
        else
            finish();
        if (viewFlipper.getDisplayedChild() == 0)
            setTask(ForumJob.BOARD);
    }

    public void setTask(ForumJob task) {
        this.task = task;
        menu.findItem(R.id.action_add).setVisible(task == ForumJob.POSTS);
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_forum, menu);
        this.menu = menu;
        setTask(ForumJob.BOARD);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                back();
                break;
            case R.id.action_ViewMALPage:
                startActivity(new Intent(Intent.ACTION_VIEW, getUri()));
                break;
            case R.id.action_add:
                if (task == ForumJob.POSTS)
                    posts.toggleComments();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public Uri getUri() {
        Uri url = Uri.EMPTY;
        switch (task) {
            case BOARD:
                url = Uri.parse("http://myanimelist.net/forum/");
                break;
            case SUBBOARD:
                url = Uri.parse("http://myanimelist.net/forum/?subboard=" + topics.id);
                break;
            case TOPICS:
                url = Uri.parse("http://myanimelist.net/forum/?board=" + topics.id);
                break;
            case POSTS:
                url = Uri.parse("http://myanimelist.net/forum/?topicid=" + posts.id);
                break;
        }
        return url;
    }
}
