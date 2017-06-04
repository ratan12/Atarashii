package net.somethingdreadful.MAL.forum

import android.content.Context
import android.webkit.WebView
import net.somethingdreadful.MAL.AppLog
import net.somethingdreadful.MAL.DateTools
import net.somethingdreadful.MAL.R
import net.somethingdreadful.MAL.Theme
import net.somethingdreadful.MAL.account.AccountService
import net.somethingdreadful.MAL.api.BaseModels.Forum
import java.util.*

public class ForumHTMLUnit(private val context: Context, private val webView: WebView) {
    var forumMenuLayout: String
    private val forumMenuTiles: String
    private val forumListLayout: String
    private val forumListTiles: String
    private val forumCommentsLayout: String
    private val forumCommentsTiles: String
    private val spoilerStructure: String
    var id: Int = 0
    var page: String? = null
    var subBoard = false

    init {
        forumMenuLayout = getResString(R.raw.forum_menu)
        forumMenuTiles = getResString(R.raw.forum_menu_tiles)
        forumListLayout = getResString(R.raw.forum_list)
        forumListTiles = getResString(R.raw.forum_list_tiles)
        forumCommentsLayout = getResString(R.raw.forum_comment)
        forumCommentsTiles = getResString(R.raw.forum_comment_tiles)
        spoilerStructure = getResString(R.raw.forum_comment_spoiler_structure)
    }

    fun menuExists(): Boolean {
        return !forumMenuLayout.contains("<!-- insert here the tiles -->")
    }

    private fun loadWebview(html: String) {
        var html = html
        if (Theme.darkTheme) {
            html = html.replace("#f2f2f2;", "#212121;") // hover tags
            html = html.replace("#FFF;", "#313131;") // body
            html = html.replace("#EEE;", "#212121;") // body border
            html = html.replace("#022f70;", "#0078a0;") // selection tags
            html = html.replace("#3E454F;", "#818181;") // time ago
            html = html.replace("markdown {", "markdown {color:#E3E3E3;") // comment body color
        }
        html = html.replace("data:text/html,", "")
        webView.loadData(html, "text/html; charset=utf-8", "UTF-8")
    }

    fun setForumMenu(menu: ArrayList<Forum>?) {
        if (menu != null && menu.size > 0) {
            var forumArray = ""
            var tempTile: String
            var description: String
            for (item in menu) {
                tempTile = forumMenuTiles
                description = item.description

                if (item.children != null) {
                    tempTile = tempTile.replace("onClick=\"tileClick(<!-- id -->)\"", "")
                    description = description + " "

                    for (i in 0..item.children.size - 1) {
                        val child = item.children[i]
                        description = description + "<a onClick=\"subTileClick(" + child.id + ")\">" + child.name + "</a>" + if (i < item.children.size - 1) ", " else ""
                    }
                } else {
                    tempTile = tempTile.replace("<!-- id -->", item.id.toString())
                }

                tempTile = tempTile.replace("<!-- header -->", item.name)
                tempTile = tempTile.replace("<!-- description -->", description)
                tempTile = tempTile.replace("<!-- last reply -->", context.getString(R.string.dialog_message_last_post))
                forumArray = forumArray + tempTile
            }
            forumMenuLayout = forumMenuLayout.replace("<!-- insert here the tiles -->", forumArray)
            forumMenuLayout = forumMenuLayout.replace("<!-- title -->", "M 0") // M = menu, 0 = id
        }
        if (menuExists())
            loadWebview(forumMenuLayout)
    }

    fun setForumList(forumList: ArrayList<Forum>?) {
        if (forumList != null && forumList.size > 0) {
            var tempForumList: String
            var forumArray = ""
            var tempTile: String
            val maxPages = forumList[0].maxPages
            for (item in forumList) {
                tempTile = forumListTiles
                tempTile = tempTile.replace("<!-- id -->", item.id.toString())
                tempTile = tempTile.replace("<!-- title -->", item.name)
                if (item.reply != null) {
                    tempTile = tempTile.replace("<!-- username -->", item.reply.username)
                    tempTile = tempTile.replace("<!-- time -->", DateTools.parseDate(item.reply.time, true))
                }
                forumArray = forumArray + tempTile
            }
            tempForumList = forumListLayout.replace("<!-- insert here the tiles -->", forumArray)
            tempForumList = tempForumList.replace("<!-- title -->", (if (subBoard) "S " else "T ") + id + " " + maxPages) // T = Topics || S = subboard, id
            if (Integer.parseInt(page) == 1) {
                tempForumList = tempForumList.replace("class=\"previous\"", "class=\"previous\" style=\"visibility: hidden;\"")
            }
            if (Integer.parseInt(page) == maxPages) {
                tempForumList = tempForumList.replace("class=\"next\"", "class=\"next\" style=\"visibility: hidden;\"")
            }
            tempForumList = tempForumList.replace("Forum.prevTopicList(" + page!!, "Forum.prevTopicList(" + (Integer.parseInt(page) - 1))
            tempForumList = tempForumList.replace("Forum.nextTopicList(" + page!!, "Forum.nextTopicList(" + (Integer.parseInt(page) + 1))
            tempForumList = tempForumList.replace("<!-- page -->", page!!)
            tempForumList = tempForumList.replace("<!-- next -->", context.getString(R.string.next))
            tempForumList = tempForumList.replace("<!-- previous -->", context.getString(R.string.previous))
            loadWebview(tempForumList)
        }
    }

    fun setForumComments(forumList: ArrayList<Forum>?) {
        if (forumList != null && forumList.size > 0) {
            var tempForumList: String
            var rank: String
            var comment: String
            var forumArray = ""
            var tempTile: String
            val maxPages = forumList[0].maxPages
            for (item in forumList) {
                rank = item.profile.getSpecialAccesRank(item.username)
                comment = item.comment
                comment = convertComment(comment)

                tempTile = forumCommentsTiles
                if (item.username.equals(AccountService.username!!, ignoreCase = true))
                    tempTile = tempTile.replace("fa-quote-right fa-lg\"", "fa-pencil fa-lg\" id=\"edit\"")
                tempTile = tempTile.replace("<!-- username -->", item.username)
                tempTile = tempTile.replace("<!-- comment id -->", Integer.toString(item.id))
                tempTile = tempTile.replace("<!-- time -->", DateTools.parseDate(item.time, true))
                tempTile = tempTile.replace("<!-- comment -->", comment)
                if (item.profile.avatarUrl.contains("xmlhttp-loader"))
                    tempTile = tempTile.replace("<!-- profile image -->", "http://cdn.myanimelist.net/images/na.gif")
                else
                    tempTile = tempTile.replace("<!-- profile image -->", item.profile.avatarUrl)
                if (rank != "")
                    tempTile = tempTile.replace("<!-- access rank -->", rank)
                else
                    tempTile = tempTile.replace("<span class=\"forum__mod\"><!-- access rank --></span>", "")
                if (item.children != null) {
                    tempTile = tempTile.replace("<!-- child -->", getChildren(item.children))
                }
                forumArray = forumArray + tempTile
            }
            tempForumList = forumCommentsLayout.replace("<!-- insert here the tiles -->", forumArray)
            tempForumList = tempForumList.replace("<!-- title -->", "C $id $maxPages $page") // C = Comments, id, maxPages, page
            if (Integer.parseInt(page) == 1) {
                tempForumList = tempForumList.replace("class=\"previous\"", "class=\"previous\" style=\"visibility: hidden;\"")
            }
            if (Integer.parseInt(page) == maxPages) {
                tempForumList = tempForumList.replace("class=\"next\"", "class=\"next\" style=\"visibility: hidden;\"")
            }
            tempForumList = tempForumList.replace("Forum.prevCommentList(" + page!!, "Forum.prevCommentList(" + (Integer.parseInt(page) - 1))
            tempForumList = tempForumList.replace("Forum.nextCommentList(" + page!!, "Forum.nextCommentList(" + (Integer.parseInt(page) + 1))
            tempForumList = tempForumList.replace("<!-- page -->", page!!)
            tempForumList = tempForumList.replace("<!-- next -->", context.getString(R.string.next))
            tempForumList = tempForumList.replace("<!-- previous -->", context.getString(R.string.previous))
            if (!AccountService.isMAL) {
                tempForumList = tempForumList.replace("[b][/b]", "____")
                tempForumList = tempForumList.replace("[i][/i]", "__")
                tempForumList = tempForumList.replace("[s][/s]", "~~~~")
                tempForumList = tempForumList.replace("[spoiler][/spoiler]", "~!!~")
                tempForumList = tempForumList.replace("[url=][/url]", "[link](URL)")
                tempForumList = tempForumList.replace("[img][/img]", "img220(URL)")
                tempForumList = tempForumList.replace("[yt][/yt]", "youtube(ID)")
                tempForumList = tempForumList.replace("[list][/list]", "1.")
                tempForumList = tempForumList.replace("[size=][/size]", "##")
                tempForumList = tempForumList.replace("[center][/center]", "~~~~~~")
                tempForumList = tempForumList.replace("[quote][/quote]", ">")
                tempForumList = tempForumList.replace("webm(.+?),\"(.+?)\"\\);\\}".toRegex(), "webm$1,\"webm(URL)\"\\);}")
                tempForumList = tempForumList.replace("ulist(.+?),\"(.+?)\"\\);\\}".toRegex(), "ulist$1,\"- \"\\);}")
            }
            loadWebview(tempForumList)
        }
    }

    private fun getChildren(forumList: ArrayList<Forum>?): String {
        if (forumList != null && forumList.size > 0) {
            var rank: String
            var comment: String
            var forumArray = ""
            var tempTile: String
            for (item in forumList) {
                rank = item.profile.getSpecialAccesRank(item.username)
                comment = if (item.comment == null) "" else item.comment
                comment = convertComment(comment)

                tempTile = forumCommentsTiles
                tempTile = tempTile.replace("<div class=\"comment\">", "<div class=\"subComment\">")
                if (item.username.equals(AccountService.username!!, ignoreCase = true))
                    tempTile = tempTile.replace("fa-quote-right fa-lg\"", "fa-pencil fa-lg\" id=\"edit\"")
                tempTile = tempTile.replace("<!-- username -->", item.username)
                tempTile = tempTile.replace("<!-- comment id -->", Integer.toString(item.id))
                tempTile = tempTile.replace("<!-- time -->", DateTools.parseDate(item.time, true))
                tempTile = tempTile.replace("<!-- comment -->", comment)
                if (item.profile.avatarUrl.contains("xmlhttp-loader"))
                    tempTile = tempTile.replace("<!-- profile image -->", "http://cdn.myanimelist.net/images/na.gif")
                else
                    tempTile = tempTile.replace("<!-- profile image -->", item.profile.avatarUrl)
                if (rank != "")
                    tempTile = tempTile.replace("<!-- access rank -->", rank)
                else
                    tempTile = tempTile.replace("<span class=\"forum__mod\"><!-- access rank --></span>", "")
                if (item.children != null) {
                    tempTile = tempTile.replace("<!-- child -->", getChildren(item.children))
                }
                forumArray = forumArray + tempTile
            }
            return forumArray
        }
        return ""
    }

    fun convertComment(comment: String): String {
        var comment = comment
        if (AccountService.isMAL) {
            comment = comment.replace("<div class=\"spoiler\">((.|\\n)+?)<br>((.|\\n)+?)</span>((.|\\n)+?)</div>".toRegex(), "$spoilerStructure$3</div></input>")
            comment = comment.replace("<div class=\"hide_button\">((.|\\n)+?)class=\"quotetext\">((.|\\n)+?)</div>".toRegex(), "$spoilerStructure$3</input>")
            comment = comment.replace("@(\\w+)".toRegex(), "<font color=\"#022f70\"><b>@$1</b></font>")
        } else {
            comment = comment.replace("(.*)>(.*)".toRegex(), "<div class=\"quotetext\">$2</div>")
            comment = comment.replace("`((.|\\n)+?)`".toRegex(), "<div class=\"codetext\">$1</div>")
            comment = comment.replace("__((.|\\n)+?)__".toRegex(), "<b>$1</b>")
            comment = comment.replace("~~~((.|\\n)+?)~~~".toRegex(), "<center>$1</center>")
            comment = comment.replace("~~((.|\\n)+?)~~".toRegex(), "<span style=\"text-decoration:line-through;\">$1</span>")
            comment = comment.replace("~!((.|\\n)+?)!~".toRegex(), "$spoilerStructure$1</div></input>")
            comment = comment.replace("\\[((.|\\n)+?)\\]\\(((.|\\n)+?)\\)".toRegex(), "<a href=\"$3\" rel=\"nofollow\"><b>$1</b></a>")
            comment = comment.replace("href=\"(.+?)(#)(.+?)\"".toRegex(), "href=\"$1%23$3\"") // Replace to avoid conflicts.
            comment = comment.replace("href=\"(.+?)(_)(.+?)\"".toRegex(), "href=\"$1%5F$3\"") // Replace to avoid conflicts.
            comment = comment.replace("img(\\d.+?)\\((\\w.+?)\\)".toRegex(), "<img width=\"$1\" src=\"$2\">")
            comment = comment.replace("img\\((\\w.+?)\\)".toRegex(), "<img src=\"$1\">")
            comment = comment.replace("src=\"(.+?)(_)(.+?)\">".toRegex(), "src=\"$1%5F$3\">") // Replace to avoid conflicts.
            comment = comment.replace("_((.|\\n)+?)_".toRegex(), "<i>$1</i>") // This must be after the images else it will replace image urls!
            comment = comment.replace("(.*)##(.*)".toRegex(), "<h1>$2</h1>")
            comment = comment.replace("(.*)#(.*)".toRegex(), "<h1>$2</h1>")
            comment = comment.replace("\n", "<br>")
            comment = comment.replace("@(\\w+)".toRegex(), "<font color=\"#022f70\"><b>@$1</b></font>")
        }
        return comment
    }

    /**
     * Get the string of the given resource file.

     * @param resource The resource of which string we need
     * *
     * @return String the wanted string
     */
    private fun getResString(resource: Int): String {
        try {
            val inputStream = context.resources.openRawResource(resource)
            val buffer = ByteArray(inputStream.available())
            while (inputStream.read(buffer) != -1)
                return String(buffer)
        } catch (e: Exception) {
            AppLog.logTaskCrash("ForumHTMLUnit", "getResString(): " + resource, e)
        }

        return ""
    }
}