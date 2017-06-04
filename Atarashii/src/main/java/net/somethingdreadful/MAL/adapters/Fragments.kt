package net.somethingdreadful.MAL.adapters


import android.app.Activity
import android.app.Fragment
import java.util.*

internal class Fragments(private val activity: Activity) {
    private val fragments = ArrayList<FragmentHolder>()

    /**
     * Add fragments to the holder.

     * @param fragment The fragment which should be added
     * *
     * @param name     The fragment name res ID shown in the tabs
     */
    fun add(fragment: Fragment, name: Int) {
        add(fragment, activity.getString(name))
    }

    /**
     * Add fragments to the holder.

     * @param fragment The fragment which should be added
     * *
     * @param name     The fragment String name shown in the tabs
     */
    fun add(fragment: Fragment, name: String) {
        fragments.add(FragmentHolder(fragment, name))
    }

    /**
     * Get the amount of Fragments stored.

     * @return Int the amount
     */
    val size: Int
        get() = fragments.size

    /**
     * Get the fragment by the given position.

     * @param position The fragment position
     * *
     * @return Fragment The fragment which was stored
     */
    fun getFragment(position: Int): Fragment {
        return fragments[position].fragment
    }

    /**
     * Get the fragment name by the given position.

     * @param position The position of the fragment
     * *
     * @return String The name as shown in the tabs
     */
    fun getName(position: Int): String {
        return fragments[position].name
    }

    /**
     * Set the fragment name by the given position.

     * @param position The position of the fragment
     */
    fun setName(position: Int, name: String) {
        fragments[position].name = name
    }

    /**
     * Remove all fragments.
     */
    fun clear() {
        fragments.clear()
    }

    inner class FragmentHolder(internal val fragment: Fragment, internal var name: String)
}
