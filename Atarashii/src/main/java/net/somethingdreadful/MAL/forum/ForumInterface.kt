package net.somethingdreadful.MAL.forum

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.webkit.JavascriptInterface
import net.somethingdreadful.MAL.ForumActivity
import net.somethingdreadful.MAL.ProfileActivity
import net.somethingdreadful.MAL.R
import net.somethingdreadful.MAL.Theme
import net.somethingdreadful.MAL.account.AccountService
import net.somethingdreadful.MAL.dialog.ChooseDialogFragment
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment
import net.somethingdreadful.MAL.tasks.ForumJob
import net.somethingdreadful.MAL.tasks.ForumNetworkTask
import org.apache.commons.lang3.StringEscapeUtils

class ForumInterface(private val forum: ForumActivity) {

    /**
     * convert HTML to BBCode
     */
    @JavascriptInterface
    fun convertHTML(username: String, messageID: String, bbCode: String) {
        var bbCode = bbCode
        if (bbCode.contains("src=\"http://youtube.com/embed/")) {
            forum.runOnUiThread { Theme.Snackbar(forum, R.string.toast_info_disabled_youtube) }
        } else {
            if (AccountService.isMAL) {
                bbCode = convertMessageQuote(bbCode)
                bbCode = convertUserQuote(bbCode)
                bbCode = convertQuote(bbCode)
                bbCode = convertQuoteSpoiler(bbCode)
                bbCode = convertSpoiler(bbCode)
                bbCode = bbCode.replace(" target=\"_blank", "") //image trash
                bbCode = bbCode.replace("<b>((.|\\n)+?)</b>".toRegex(), "[b]$1[/b]") //Bold text
                bbCode = bbCode.replace("<i>((.|\\n)+?)</i>".toRegex(), "[i]$1[/i]") //Italics
                bbCode = bbCode.replace("<u>((.|\\n)+?)</u>".toRegex(), "[u]$1[/u]") //Underlined text
                bbCode = bbCode.replace("<ul>((.|\\n)+?)</ul>".toRegex(), "[list]$1[/list]") //list
                bbCode = bbCode.replace("<ol>((.|\\n)+?)</ol>".toRegex(), "[list=1]$1[/list]") //list
                bbCode = bbCode.replace("<li>((.|\\n)+?)</li>".toRegex(), "[*]$1") //list items
                bbCode = bbCode.replace("<pre>((.|\\n)+?)</pre>".toRegex(), "$1") //unknown
                bbCode = bbCode.replace("<a (.+?)>\\[b]@(.+?)\\[/b](.+?)</a>".toRegex(), "@$2") //@ mention
                bbCode = bbCode.replace("<span style=\"text-decoration:line-through;\">((.|\\n)+?)</span>".toRegex(), "[s]$1[/s]") //Strike-thru text
                bbCode = bbCode.replace("<span style=\"font-size: (\\d+?)%;\">((.|\\n)+?)</span>".toRegex(), "[size=$1]$2[/size]") //resized text
                bbCode = bbCode.replace("<span style=\"color: (\\w+?)\">((.|\\n)+?)</span>".toRegex(), "[color=$1]$2[/color]") //colored text
                bbCode = bbCode.replace("<div style=\"text-align: center;\">((.|\\n)+?)</div>".toRegex(), "[center]$1[/center]") //centered text
                bbCode = bbCode.replace("<div style=\"text-align: right;\">((.|\\n)+?)</div>".toRegex(), "[right]$1[/right]") //right text
                bbCode = bbCode.replace("<a href=\"(.+?)\" rel=\"nofollow\">((.|\\n)+?)</a>".toRegex(), "[url=$1]$2[/url]") //Text link
                bbCode = bbCode.replace("<img class=\"userimg\" src=\"(.+?)\">".toRegex(), "[img]$1[/img]") //image
                bbCode = bbCode.replace("<img class=\"userimg img-a-l\" src=\"(.+?)\">".toRegex(), "[img align=left]$1[/img]") //image left
                bbCode = bbCode.replace("<img class=\"userimg img-a-r\" src=\"(.+?)\">".toRegex(), "[img align=right]$1[/img]") //image right
                bbCode = bbCode.replace("<div class=\"codetext\">((.|\\n)+?)<div>".toRegex(), "[code]$[/code]") //code
            } else {
                bbCode = bbCode.replace("<font color=\"#022f70\"><b>@(\\w+)</b></font>".toRegex(), "@$1")
                bbCode = bbCode.replace("<div class=\"codetext\">((.|\\n)+?)</div>".toRegex(), "`$1`") //code
                bbCode = bbCode.replace("<b>((.|\\n)+?)</b>".toRegex(), "__$1__") //Bold text
                bbCode = bbCode.replace("<i>((.|\\n)+?)</i>".toRegex(), "_$1_") //Italics
                bbCode = bbCode.replace("<center>((.|\\n)+?)</center>".toRegex(), "~~~$1~~~") //centered text
                bbCode = bbCode.replace("<span style=\"text-decoration:line-through;\">((.|\\n)+?)</span>".toRegex(), "~~$1~~") //Strike-thru text
                bbCode = bbCode.replace("<input class=\"spoilerbutton\"(.+?)spoiler quotetext\">((.|\\n)+?)</div>".toRegex(), "~!$2!~") //spoiler
                bbCode = bbCode.replace("<a href=\"((.|\\n)+?)\" rel=\"nofollow\">((.|\\n)+?)</a>".toRegex(), "[$3]($1)") //Text link
                bbCode = bbCode.replace("<img width=\"(\\d.+?)\" src=\"(\\w.+?)\"></img>".toRegex(), "img$1($2)") //image
                bbCode = bbCode.replace("<h1>((.|\\n)+?)</h1>".toRegex(), "##$2") //header text
                bbCode = bbCode.replace("<div class=\"quotetext\">((.|\\n)+?)</div>".toRegex(), ">$1") //quote
            }

            bbCode = StringEscapeUtils.unescapeHtml4(bbCode) //clean the code
            val finalBbCode = bbCode.replace("<br>", "\\n") //new line
            if (finalBbCode.contains("<div")) {
                Theme.Snackbar(forum, R.string.toast_error_convert)
            } else {
                forum.runOnUiThread {
                    forum.webview.loadUrl("javascript:document.getElementById(\"textarea\").scrollIntoView();")
                    if (username.equals(AccountService.username!!, ignoreCase = true)) {
                        forum.webview.loadUrl("javascript:document.getElementById(\"textarea\").setAttribute(\"name\", \"$messageID\");")
                        forum.webview.loadUrl("javascript:updateTextarea(\"$finalBbCode\");")
                    } else {
                        forum.webview.loadUrl("javascript:document.getElementById(\"textarea\").setAttribute(\"name\", \"0\");")
                        if (AccountService.isMAL)
                            forum.webview.loadUrl("javascript:updateTextarea(\"[quote=$username message=$messageID]$finalBbCode[/quote]\");")
                        else
                            forum.webview.loadUrl("javascript:updateTextarea(\">$finalBbCode\");")
                    }
                }
            }
        }
    }

    private var convertMessageQuote = 0

    private fun convertMessageQuote(HTML: String): String {
        var HTML = HTML
        convertMessageQuote += 1
        if (convertMessageQuote <= 8) {
            HTML = HTML.replace("<div class=\"quotetext\"><strong><a href=\"/forum/message/(.+?)\\?goto=topic\">(.+?) said:</a></strong>((.|\\n)+?)</div>".toRegex(), "[quote=$2 message=$1]$3[/quote]") //real quote
            if (HTML.contains("<div class=\"quotetext\"><strong><a href=\"/forum/message/"))
                return convertMessageQuote(HTML)
            convertMessageQuote = 0
        }
        return HTML
    }

    private var convertUserQuote = 0

    private fun convertUserQuote(HTML: String): String {
        var HTML = HTML
        convertUserQuote += 1
        if (convertUserQuote <= 8) {
            HTML = HTML.replace("<div class=\"quotetext\"><strong>(.+?) said:</strong>((.|\\n)+?)</div>".toRegex(), "[quote=$1]$2[/quote]") //real quote
            if (HTML.contains("<div class=\"quotetext\"><strong>"))
                return convertUserQuote(HTML)
            convertUserQuote = 0
        }
        return HTML
    }

    private var convertQuote = 0

    private fun convertQuote(HTML: String): String {
        var HTML = HTML
        convertQuote += 1
        if (convertQuote <= 8) {
            HTML = HTML.replace("<div class=\"quotetext\">((.|\\n)+?)<div>".toRegex(), "[quote]$1[/quote]") //quote
            if (HTML.contains("<div class=\"quotetext\">"))
                return convertQuote(HTML)
            convertQuote = 0
        }
        return HTML
    }

    private var convertQuoteSpoiler = 0

    private fun convertQuoteSpoiler(HTML: String): String {
        var HTML = HTML
        convertQuoteSpoiler += 1
        if (convertQuoteSpoiler <= 8) {
            HTML = HTML.replace("<input class=\"spoilerbutton\"(.+?)spoiler quotetext\"><strong>(.+?) said:</strong>((.|\\n)+?)</div>".toRegex(), "[spoiler][quote=$2]$3[/quote][/spoiler]") //quote
            if (HTML.contains("spoiler quotetext\"><strong>"))
                return convertQuoteSpoiler(HTML)
            convertQuoteSpoiler = 0
        }
        return HTML
    }

    private var convertSpoiler = 0

    private fun convertSpoiler(HTML: String): String {
        var HTML = HTML
        convertSpoiler += 1
        if (convertSpoiler <= 8) {
            HTML = HTML.replace("<input class=\"spoilerbutton\"(.+?)spoiler quotetext\">((.|\\n)+?)</div>".toRegex(), "[spoiler]$2[/spoiler]") //quote
            if (HTML.contains("spoiler quotetext\">"))
                return convertSpoiler(HTML)
            convertSpoiler = 0
        }
        return HTML
    }

    /**
     * Get the topics from a certain category.
     */
    @JavascriptInterface
    fun tileClick(id: String) {
        forum.runOnUiThread { forum.getRecords(ForumJob.CATEGORY, Integer.parseInt(id), "1") }
    }

    /**
     * Get more pages certain category.
     */
    @JavascriptInterface
    fun topicList(page: String) {
        forum.runOnUiThread {
            val details = forum.webview.title.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            forum.getRecords(ForumJob.CATEGORY, Integer.parseInt(details[1]), page)
        }
    }

    /**
     * Get the topics from a certain category.
     */
    @JavascriptInterface
    fun subTileClick(id: String) {
        forum.runOnUiThread { forum.getRecords(ForumJob.SUBCATEGORY, Integer.parseInt(id), "1") }
    }

    /**
     * Get the posts from a certain topic.
     */
    @JavascriptInterface
    fun topicClick(id: String) {
        forum.runOnUiThread {
            try {
                forum.getRecords(ForumJob.TOPIC, Integer.parseInt(id), "1")
            } catch (e: Exception) {
                Theme.Snackbar(forum, R.string.toast_error_Records)
            }
        }
    }

    /**
     * Get next topic page.
     */
    @JavascriptInterface
    fun nextTopicList(page: String) {
        forum.runOnUiThread {
            val details = forum.webview.title.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            forum.getRecords(ForumJob.CATEGORY, Integer.parseInt(details[1]), page)
        }
    }

    /**
     * Get topic comment page.
     */
    @JavascriptInterface
    fun prevTopicList(page: String) {
        forum.runOnUiThread {
            val details = forum.webview.title.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            forum.getRecords(ForumJob.CATEGORY, Integer.parseInt(details[1]), page)
        }
    }

    /**
     * Send a comment.
     */
    @JavascriptInterface
    fun sendComment(comment: String, messageID: String) {
        forum.runOnUiThread {
            if (comment.length > 16) {
                forum.setLoading(true)
                val details = forum.webview.title.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()


                val lcdf = ChooseDialogFragment()
                val bundle = Bundle()
                if (messageID == "0") {
                    bundle.putString("title", forum.getString(R.string.dialog_title_add_comment))
                    bundle.putString("message", forum.getString(R.string.dialog_message_add_comment))
                } else {
                    bundle.putString("title", forum.getString(R.string.dialog_title_edit_comment))
                    bundle.putString("message", forum.getString(R.string.dialog_message_edit_comment))
                }
                bundle.putString("positive", forum.getString(android.R.string.yes))
                lcdf.arguments = bundle
                lcdf.setCallback(object : ChooseDialogFragment.onClickListener {
                    override fun onPositiveButtonClicked() {
                        if (messageID == "0")
                            ForumNetworkTask(forum, forum, ForumJob.ADDCOMMENT, Integer.parseInt(details[1])).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, comment, details[3])
                        else
                            ForumNetworkTask(forum, forum, ForumJob.UPDATECOMMENT, Integer.parseInt(messageID)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, comment, details[3])
                    }
                })
                lcdf.show(forum.fragmentManager, "fragment_sendComment")
            } else {
                Theme.Snackbar(forum, R.string.toast_info_comment)
            }
        }
    }

    /**
     * Get next comment page.
     */
    @JavascriptInterface
    fun nextCommentList(page: String) {
        forum.runOnUiThread {
            val details = forum.webview.title.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            forum.getRecords(ForumJob.TOPIC, Integer.parseInt(details[1]), page)
        }
    }

    /**
     * Get comment page.
     */
    @JavascriptInterface
    fun pagePicker(page: String) {
        forum.runOnUiThread {
            val details = forum.webview.title.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val bundle = Bundle()
            bundle.putInt("id", Integer.parseInt(details[1]))
            bundle.putString("title", forum.getString(R.string.Page_number))
            bundle.putInt("current", Integer.parseInt(page))
            bundle.putInt("max", Integer.parseInt(details[2]))
            bundle.putInt("min", 1)
            val fm = forum.fragmentManager
            val dialogFragment = NumberPickerDialogFragment().setOnSendClickListener(forum)
            dialogFragment.arguments = bundle
            dialogFragment.show(fm, "fragment_page")
        }
    }

    /**
     * Get previous comment page.
     */
    @JavascriptInterface
    fun prevCommentList(page: String) {
        forum.runOnUiThread {
            val details = forum.webview.title.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            forum.getRecords(ForumJob.TOPIC, Integer.parseInt(details[1]), page)
        }
    }

    /**
     * Open the userprofile.
     */
    @JavascriptInterface
    fun profileClick(username: String) {
        forum.runOnUiThread {
            val Profile = Intent(forum, ProfileActivity::class.java)
            Profile.putExtra("username", username)
            forum.startActivity(Profile)
        }
    }
}