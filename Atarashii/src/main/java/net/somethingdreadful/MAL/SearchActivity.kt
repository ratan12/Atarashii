package net.somethingdreadful.MAL

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.bindView
import com.lapism.searchview.SearchAdapter
import com.lapism.searchview.SearchItem
import com.lapism.searchview.SearchView
import net.somethingdreadful.MAL.adapters.IGFPagerAdapter
import net.somethingdreadful.MAL.api.BaseModels.IGFModel
import net.somethingdreadful.MAL.cover.CoverAction
import net.somethingdreadful.MAL.cover.CoverFragment
import net.somethingdreadful.MAL.database.DatabaseManager
import org.apache.commons.lang3.text.WordUtils

class SearchActivity : AppCompatActivity(), CoverFragment.CoverListener, SearchView.OnQueryTextListener {
    var query: String? = null
    private var af: CoverFragment? = null
    private var mf: CoverFragment? = null
    val searchView: SearchView by bindView(R.id.searchView)

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        Theme.setTheme(this, R.layout.activity_search, false)
        Theme.setActionBar(this, IGFPagerAdapter(fragmentManager))
        query = intent.getStringExtra("query")
        ButterKnife.bind(this)
        searchView.version = SearchView.VERSION_TOOLBAR
        searchView.setOnQueryTextListener(this)
        searchView.setQuery(query, false)
        searchView.setArrowOnly(true)

        val activity = this
        AsyncTask.execute {
            val dbManager = DatabaseManager(activity)
            val suggestion = dbManager.suggestions
            activity.runOnUiThread { initSuggestion(suggestion) }
        }
    }

    @OnClick(R.id.action_home)
    fun close(view: View) {
        finish()
    }

    override fun onCoverLoaded(coverFragment: CoverFragment) {
        if (coverFragment.isAnime)
            af = coverFragment
        else
            mf = coverFragment
        if (query != null && !TextUtils.isDigitsOnly(query))
        // there is already a search to do
            coverFragment.getSearchRecords(true, WordUtils.capitalize(query))
    }

    fun initSuggestion(suggestion: List<SearchItem>) {
        val searchAdapter = SearchAdapter(this, suggestion)
        searchAdapter.addOnItemClickListener { view, position ->
            val textView = view.findViewById(R.id.textView_item_text) as TextView
            query = textView.text.toString()
            if (af != null && mf != null) {
                af!!.getSearchRecords(true, WordUtils.capitalize(query))
                mf!!.getSearchRecords(true, WordUtils.capitalize(query))
                searchView.setQuery(query, false)
                searchView.close(true)
            }
        }
        searchView.adapter = searchAdapter
    }

    override fun onCoverRequest(isAnime: Boolean) {
        if (af != null && mf != null) {
            af!!.getSearchRecords(false, WordUtils.capitalize(query))
            mf!!.getSearchRecords(false, WordUtils.capitalize(query))
        }
    }

    override fun onCoverClicked(position: Int, actionId: Int, isAnime: Boolean, item: IGFModel.IGFItem) {
        if (actionId == 1)
            CoverAction(this, isAnime).addCoverItem(item)
        else if (actionId == 2)
            CoverAction(this, isAnime).addCoverItem(item)
        else if (actionId == 3)
            CoverAction(this, isAnime).addCoverItem(item)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        if (af != null && mf != null && this.query != query) {
            af!!.getSearchRecords(true, WordUtils.capitalize(query))
            mf!!.getSearchRecords(true, WordUtils.capitalize(query))
        }
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        return false
    }
}