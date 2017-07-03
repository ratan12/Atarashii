package net.somethingdreadful.MAL.tasks

import android.app.Activity
import android.os.AsyncTask
import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.ContentManager
import net.somethingdreadful.MAL.api.APIHelper
import net.somethingdreadful.MAL.api.BaseModels.Forum
import java.util.*

class ForumNetworkTask(private val callback: ForumNetworkTask.ForumNetworkTaskListener?, private val activity: Activity, private val type: ForumJob, private val id: Int) : AsyncTask<String, Void, ArrayList<Forum>>() {

    override fun doInBackground(vararg params: String): ArrayList<Forum>? {
        var result: ArrayList<Forum>? = ArrayList()
        val cManager = ContentManager(activity)
        var error = false
        if (APIHelper.isNetworkAvailable(activity))
            error = cManager.verifyAuthentication()

        try {
            when (type) {
                ForumJob.MENU -> result = cManager.forumCategories // list with all categories
                ForumJob.CATEGORY -> result = cManager.getCategoryTopics(id, Integer.parseInt(params[0])) // list with all topics of a certain category
                ForumJob.SUBCATEGORY -> result = cManager.getSubCategory(id, Integer.parseInt(params[0]))
                ForumJob.TOPIC -> result = cManager.getTopic(id, Integer.parseInt(params[0])) // list with all comments of users
                ForumJob.SEARCH -> result = cManager.search(params[0])
                ForumJob.ADDCOMMENT -> {
                    if (!error) {
                        result = if (cManager.addComment(id, params[0])) ArrayList<Forum>() else null
                        if (result != null)
                            result = cManager.getTopic(id, Integer.parseInt(params[1]))
                    }
                }
                ForumJob.UPDATECOMMENT -> if (!error) result = if (cManager.updateComment(id, params[0])) ArrayList<Forum>() else null
            }
        } catch (e: Exception) {
            AppLog.logTaskCrash("ForumNetworkTask", "doInBackground(6): " + String.format("%s-task unknown API error on id %s", type.toString(), id), e)
        }

        return result
    }

    override fun onPostExecute(result: ArrayList<Forum>) {
        callback?.onForumNetworkTaskFinished(result, type)
    }

    interface ForumNetworkTaskListener {
        fun onForumNetworkTaskFinished(result: ArrayList<Forum>, task: ForumJob)
    }
}
