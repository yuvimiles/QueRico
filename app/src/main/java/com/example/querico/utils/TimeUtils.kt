package com.example.querico.utils

object TimeUtils {
    fun getRelativeTimeSpan(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - timestamp

        val seconds = timeDiff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "just now"
            minutes < 60 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            hours < 24 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            days < 7 -> "$days ${if (days == 1L) "day" else "days"} ago"
            weeks < 4 -> "$weeks ${if (weeks == 1L) "week" else "weeks"} ago"
            months < 12 -> "$months ${if (months == 1L) "month" else "months"} ago"
            else -> "$years ${if (years == 1L) "year" else "years"} ago"
        }
    }
}