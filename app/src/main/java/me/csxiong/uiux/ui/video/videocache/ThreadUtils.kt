package me.csxiong.uiux.ui.video.videocache

import android.annotation.SuppressLint
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * @Desc : 线程工具
 * @Author : meitu - 2021/9/7
 */
class ThreadUtils {

    companion object {

        fun createSingleThreadExecutors(): ThreadPoolExecutor {
            return ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue<Runnable>())
        }

        @SuppressLint("ThreadNameRequired ")
        fun newFixedThreadPool(nThreads: Int): ThreadPoolExecutor {
            val executorService = Executors.newFixedThreadPool(nThreads) as ThreadPoolExecutor
            executorService.threadFactory = RenameThreadFactory()
            return executorService
        }

        private class RenameThreadFactory : ThreadFactory {
            private val threadNumber = AtomicInteger(1)
            override fun newThread(r: Runnable): Thread {
                val stringBuffer = StringBuffer()
                stringBuffer.append("videoCache-")
                stringBuffer.append(poolNumber.getAndIncrement().toString())
                stringBuffer.append("-thread-")
                stringBuffer.append(threadNumber.getAndIncrement().toString())
                val t = Thread(r, stringBuffer.toString())
                if (t.isDaemon) {
                    t.isDaemon = false
                }
                if (t.priority != Thread.NORM_PRIORITY) {
                    t.priority = Thread.NORM_PRIORITY
                }
                return t
            }

            companion object {
                private val poolNumber = AtomicInteger(1)
            }
        }
    }
}