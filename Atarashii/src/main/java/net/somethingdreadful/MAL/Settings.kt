package net.somethingdreadful.MAL

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

class Settings : AppCompatActivity() {

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_settings)
        Theme.setTheme(this, 0, false)
        Theme.setActionBar(this)
        setTitle(R.string.title_activity_settings)

        NfcHelper.disableBeam(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}