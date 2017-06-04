package net.somethingdreadful.MAL

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.widget.TextView

class AboutActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        Theme.setTheme(this, R.layout.activity_about, true)
        Theme.setActionBar(this)
        setTitle(R.string.title_activity_about)

        Card.fastInit(this, R.id.atarashii_card, R.layout.card_about_atarashii)
        Card.fastInit(this, R.id.contributors_card, R.layout.card_about_contributors)
        Card.fastInit(this, R.id.community_card, R.layout.card_about_community)
        Card.fastInit(this, R.id.translations_card, R.layout.card_about_translations)
        Card.fastInit(this, R.id.acknowledgements_card, R.layout.card_about_acknowledgements)
        createLinks(findViewById(R.id.contributor_anima_name) as TextView)
        createLinks(findViewById(R.id.contributor_motokochan_name) as TextView)
        createLinks(findViewById(R.id.contributor_apkawa_name) as TextView)
        createLinks(findViewById(R.id.contributor_dsko_name) as TextView)
        createLinks(findViewById(R.id.contributor_ratan12_name) as TextView)
        createLinks(findViewById(R.id.acknowledgements_card_content) as TextView)
        createLinks(findViewById(R.id.community_card_content) as TextView)
        createLinks(findViewById(R.id.translations_card_content) as TextView)
        createLinks(findViewById(R.id.notlisted_content) as TextView)

        val atarashii = findViewById(R.id.atarashii_card) as Card

        try {
            atarashii.Header.text = getString(R.string.app_name) + " " + packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        NfcHelper.disableBeam(this)
    }

    private fun createLinks(textView: TextView) {
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}
