package net.somethingdreadful.MAL

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.util.Log

import net.somethingdreadful.MAL.broadcasts.AutoSync
import net.somethingdreadful.MAL.cover.CoverFragment
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment

class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener, NumberPickerDialogFragment.onUpdateClickListener {
    private lateinit var settingsContext: Context
    private var alarmMgr: AlarmManager? = null

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        addPreferencesFromResource(R.xml.settings)
        findPreference("reset").onPreferenceClickListener = this
        findPreference("IGFcolumnsportrait").onPreferenceClickListener = this
        findPreference("IGFcolumnslandscape").onPreferenceClickListener = this

        settingsContext = activity.applicationContext
        alarmMgr = settingsContext!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        PreferenceManager.getDefaultSharedPreferences(settingsContext).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        try {
            // autosync
            val autosyncIntent = Intent(settingsContext, AutoSync::class.java)
            val autosyncalarmIntent = PendingIntent.getBroadcast(settingsContext, 0, autosyncIntent, 0)
            val intervalAutosync = PrefManager.getSyncTime() * 60 * 1000

            when (key) {
                "synchronisation_time" -> {
                    alarmMgr!!.cancel(autosyncalarmIntent)
                    alarmMgr!!.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, intervalAutosync.toLong(), intervalAutosync.toLong(), autosyncalarmIntent)
                }
                "synchronisation" -> if (PrefManager.getSyncEnabled())
                    alarmMgr!!.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, intervalAutosync.toLong(), intervalAutosync.toLong(), autosyncalarmIntent)
                else
                    alarmMgr!!.cancel(autosyncalarmIntent)
                "hideTabs", "locale" -> {
                    sharedPreferences.edit().commit()
                    startActivity(Intent(settingsContext, Home::class.java))
                    System.exit(0)
                }
                "darkTheme" -> {
                    PrefManager.setTextColor(true)
                    PrefManager.commitChanges()
                    sharedPreferences.edit().commit()
                    startActivity(Intent(settingsContext, Home::class.java))
                    System.exit(0)
                }
            }
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "SettingsFragment.onSharedPreferenceChanged(): " + e.message)
            AppLog.logException(e)
        }

    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            "IGFcolumnsportrait" -> makeNumberpicker(R.string.preference_list_columns_portrait, R.string.preference_list_columns_portrait, PrefManager.getIGFColumns(true), CoverFragment.getMaxColumns(true), 2)
            "IGFcolumnslandscape" -> makeNumberpicker(R.string.preference_list_columns_landscape, R.string.preference_list_columns_landscape, PrefManager.getIGFColumns(false), CoverFragment.getMaxColumns(false), 2)
            "reset" -> {
                PrefManager.clear()
                startActivity(Intent(settingsContext, Home::class.java))
                System.exit(0)
            }
        }
        return false
    }

    private fun makeNumberpicker(id: Int, title: Int, current: Int, max: Int, min: Int) {
        val bundle = Bundle()
        bundle.putInt("id", id)
        bundle.putString("title", getString(title))
        bundle.putInt("current", current)
        bundle.putInt("max", max)
        bundle.putInt("min", min)
        val numberPickerDialogFragment = NumberPickerDialogFragment().setOnSendClickListener(this)
        numberPickerDialogFragment.arguments = bundle
        numberPickerDialogFragment.show(activity.fragmentManager, "numberPickerDialogFragment")
    }

    override fun onUpdated(number: Int, id: Int) {
        when (id) {
            R.string.preference_list_columns_portrait -> {
                PrefManager.setIGFColumns(number, true)
                PrefManager.commitChanges()
                startActivity(Intent(settingsContext, Home::class.java))
                System.exit(0)
            }
            R.string.preference_list_columns_landscape -> {
                PrefManager.setIGFColumns(number, false)
                PrefManager.commitChanges()
                startActivity(Intent(settingsContext, Home::class.java))
                System.exit(0)
            }
        }
    }
}
