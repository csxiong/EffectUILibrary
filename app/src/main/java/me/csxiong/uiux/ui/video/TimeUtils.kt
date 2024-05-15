package me.csxiong.uiux.ui.video

import android.text.TextUtils

/**
 * @Desc : 事件工具
 * @Author : meitu - 2021/9/3
 */
object TimeUtils {
    private const val SECONDS_ONE_HOUR = (60 * 60).toLong()
    const val TIME_FORMAT_01 = "%02d:%02d"
    const val TIME_FORMAT_02 = "%02d:%02d:%02d"
    fun getTimeFormat1(timeMs: Long): String {
        return getTime(TIME_FORMAT_01, timeMs)
    }

    fun getTimeFormat2(timeMs: Long): String {
        return getTime(TIME_FORMAT_02, timeMs)
    }

    fun getTimeSmartFormat(timeMs: Long): String {
        val totalSeconds = (timeMs / 1000).toInt()
        return if (totalSeconds >= SECONDS_ONE_HOUR) {
            getTimeFormat2(timeMs)
        } else {
            getTimeFormat1(timeMs)
        }
    }

    fun getFormat(maxTimeMs: Long): String {
        val totalSeconds = (maxTimeMs / 1000).toInt()
        return if (totalSeconds >= SECONDS_ONE_HOUR) {
            TIME_FORMAT_02
        } else TIME_FORMAT_01
    }

    fun getTime(format: String, time: Long): String {
        var format = format
        var time = time
        if (time <= 0) {
            time = 0
        }
        val totalSeconds = (time / 1000).toInt()
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        if (TIME_FORMAT_01 == format) {
            return String.format(format, minutes, seconds)
        } else if (TIME_FORMAT_02 == format) {
            return String.format(format, hours, minutes, seconds)
        }
        if (TextUtils.isEmpty(format)) {
            format = TIME_FORMAT_02
        }
        return String.format(format, hours, minutes, seconds)
    }
}