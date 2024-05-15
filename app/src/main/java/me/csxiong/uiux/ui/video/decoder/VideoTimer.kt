package me.csxiong.uiux.ui.video.decoder

import android.os.Handler
import android.os.Looper
import android.os.Message

/**
 * @Desc : 视频timer
 * @Author : meitu - 2021/9/3
 */
abstract class VideoTimer(val millsInterval: Long) {

    private val COUNT_DOWN = 0x1

    val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                COUNT_DOWN -> {
                    onTimeUpdate()
                    loopNext()
                }
            }
        }
    }

    private fun loopNext() {
        cancel()
        handler.sendEmptyMessageDelayed(COUNT_DOWN, millsInterval)
    }

    fun start() {
        handler.removeMessages(COUNT_DOWN)
        handler.sendEmptyMessage(COUNT_DOWN)
    }

    fun cancel() {
        handler.removeMessages(COUNT_DOWN)
    }

    abstract fun onTimeUpdate()

}