package net.somethingdreadful.MAL.account

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.R
import net.somethingdreadful.MAL.Theme
import net.somethingdreadful.MAL.Theme.context
import org.apache.commons.lang3.text.WordUtils
import java.util.*

class AccountListAdapter(private val list: ArrayList<AccountService.userAccount>) : BaseAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var listview = view
        val record = list[position]
        val viewHolder: ViewHolder

        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            listview = inflater.inflate(R.layout.record_account, parent, false)

            viewHolder = ViewHolder()
            viewHolder.username = listview!!.findViewById(R.id.userName) as TextView
            viewHolder.accountType = listview.findViewById(R.id.accounttype) as TextView
            viewHolder.avatar = listview.findViewById(R.id.profileImg) as SimpleDraweeView

            if (Theme.darkTheme) {
                viewHolder.username!!.setTextColor(ContextCompat.getColor(context, R.color.white))
                viewHolder.accountType!!.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
                Theme.setBackground(context, listview)
            }

            listview.tag = viewHolder
        } else {
            viewHolder = listview?.tag as ViewHolder
        }

        try {
            viewHolder.username!!.text = WordUtils.capitalize(record.username)
            viewHolder.accountType!!.text = record.website.toString()
            viewHolder.avatar!!.setImageURI(record.imageUrl)
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "FriendsActivity.ListViewAdapter(): " + e.message)
            AppLog.logException(e)
        }

        return listview
    }

    internal class ViewHolder {
        var username: TextView? = null
        var accountType: TextView? = null
        var avatar: SimpleDraweeView? = null
    }
}