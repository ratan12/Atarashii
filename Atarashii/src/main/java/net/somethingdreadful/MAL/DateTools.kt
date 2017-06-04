package net.somethingdreadful.MAL

import android.text.format.DateUtils
import android.util.Log
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateTools {

    /**
     * Return the current date.

     * @return String yyyy-mm-dd
     */
    val currentDate: String
        get() {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH) + 1
            val day = c.get(Calendar.DAY_OF_MONTH)
            return year.toString() + "-" + month + "-" + day
        }

    /**
     * Parse a date with an ISO8601 string.

     * @param ISO8601  The ISO8601 String
     * *
     * @param withTime Use true when you want also the time (hours & minutes)
     * *
     * @return String The readable string.
     */
    fun parseDate(ISO8601: String?, withTime: Boolean): String {
        val result: String?
        if (ISO8601 == null)
            return "?"
        else
            result = getDateString(parseISO8601(ISO8601), withTime)

        return result ?: ISO8601
    }

    /**
     * Get the day of an week.

     * @param ISO8601 The ISO8601 String
     * *
     * @return String The readable string.
     */
    fun getDayOfWeek(ISO8601: String): Int {
        val c = Calendar.getInstance()
        c.time = parseISO8601(ISO8601)
        return c.get(Calendar.DAY_OF_WEEK)
    }

    /**
     * Parse a date with miliseconds.

     * @param time The time in miliseconds
     * *
     * @return String The readable string.
     */
    fun parseDate(time: Long?): String? {
        val calander = Calendar.getInstance()
        calander.timeInMillis = time!!
        return getDateString(calander.time, true)
    }

    private fun parseISO8601(ISO8601: String): Date? {
        when (ISO8601.length) {
            4 -> return getDate("yyyy", ISO8601)                            // 2015-05
            7 -> return getDate("yyyy-MM", ISO8601)                         // 2015-05
            10 -> return getDate("yyyy-MM-dd", ISO8601)                     // 2015-05-10
            16 -> return getDate("yyyy-MM-dd HH:mmZ", ISO8601 + "+0900")    // 2015-05-10 16:23:20      // AniList
            18 -> return getDate("yyyy-MM-dd'T'HHZ", ISO8601)               // 2015-05-10T16+0100
            19 -> return getDate("yyyy-MM-dd HH:mm:ssZ", ISO8601 + "+0900") // 2015-05-10 16:23:20      // AniList
            21 -> return getDate("yyyy-MM-dd'T'HH:mmZ", ISO8601)            // 2015-05-10T16:23+0100
            22 -> return getDate("yyyy-MM-dd'T'HH:mmZZZZZ", ISO8601)        // 2015-05-10T16:23+01:00   // AniList
            24 -> return getDate("yyyy-MM-dd'T'HH:mm:ssZ", ISO8601)         // 2015-05-10T16:23:20+0100
            25 -> return getDate("yyyy-MM-dd'T'HH:mm:ssZZZZZ", ISO8601)     // 2015-05-10T16:23:20+0100 // AniList
            else -> return Date()
        }
    }

    private fun getDateString(date: Date?, withTime: Boolean): String? {
        if (date == null)
            return ""

        try {
            if (withTime) { // only do "nice" formatting if time
                var diffHoursToNow = Date().time - date.time
                val MINUTE = 60000
                val HOUR = MINUTE * 60
                val DAY = HOUR * 24

                if (diffHoursToNow < 0)
                    diffHoursToNow = date.time - Date().time

                if (diffHoursToNow < HOUR)
                    return getRelativeTime(date, DateUtils.MINUTE_IN_MILLIS)

                if (diffHoursToNow < DAY)
                    return getRelativeTime(date, DateUtils.HOUR_IN_MILLIS)

                if (diffHoursToNow < DAY * 2)
                    return getRelativeTime(date, DateUtils.DAY_IN_MILLIS)

                if (isSameYear(date))
                    return DateUtils.formatDateTime(Theme.context, date.time, DateUtils.FORMAT_NO_YEAR) + " " + getRelativeTimePreposition(date)
                else
                    return DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(date) + " " + getRelativeTimePreposition(date)

            } else {
                val dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
                return dateFormatter.format(date)
            }
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DateTools.getDate(): " + date.toString() + ": " + e.message)
            AppLog.logException(e)
        }

        return null
    }

    private fun getRelativeTimePreposition(date: Date): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-M-dd HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dateTime = Calendar.getInstance()
        dateTime.timeInMillis = date.time
        try {
            val dateString = calendar.get(Calendar.YEAR).toString() + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH)
            val time = simpleDateFormat.parse(dateString + " " + dateTime.get(Calendar.HOUR_OF_DAY) + ":" + dateTime.get(Calendar.MINUTE))
            return DateUtils.getRelativeTimeSpanString(Theme.context, time.time, true).toString()
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }

    }

    private fun isSameYear(date: Date): Boolean {
        val dateTime = Calendar.getInstance()
        dateTime.timeInMillis = date.time
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == dateTime.get(Calendar.YEAR)
    }

    private fun getDate(formatter: String, date: String): Date? {
        try {
            return SimpleDateFormat(formatter, Locale.getDefault()).parse(date)
        } catch (e: Exception) {
            AppLog.log(Log.ERROR, "Atarashii", "DateTools.getDate(): " + formatter + ": " + e.message)
            AppLog.logException(e)
        }

        return null
    }

    private fun getRelativeTime(date: Date, minResolution: Long?): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return DateUtils.getRelativeTimeSpanString(calendar.timeInMillis, System.currentTimeMillis(), minResolution!!).toString()
    }
}
