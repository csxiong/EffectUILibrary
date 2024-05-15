package me.csxiong.uiux.ui.video.render

import android.view.View
import me.csxiong.uiux.ui.video.AspectRatio

/**
 * @Desc : 渲染层测量
 * @Author : meitu - 2021/8/31
 */
class RenderMeasure {
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var mVideoSarNum = 0
    private var mVideoSarDen = 0
    var measureWidth = 0
        private set
    var measureHeight = 0
        private set

    @AspectRatio
    private var mCurrAspectRatio = AspectRatio.AspectRatio_FIT_PARENT
    private var mVideoRotationDegree = 0
    fun doMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //根据策略测算
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270) {
            val tempSpec = widthMeasureSpec
            widthMeasureSpec = heightMeasureSpec
            heightMeasureSpec = tempSpec
        }
        var width = View.getDefaultSize(mVideoWidth, widthMeasureSpec)
        var height = View.getDefaultSize(mVideoHeight, heightMeasureSpec)
        if (mCurrAspectRatio == AspectRatio.AspectRatio_MATCH_PARENT) {
            width = widthMeasureSpec
            height = heightMeasureSpec
        } else if (mVideoWidth > 0 && mVideoHeight > 0) {
            val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)
            if (widthSpecMode == View.MeasureSpec.AT_MOST && heightSpecMode == View.MeasureSpec.AT_MOST) {
                val specAspectRatio = widthSpecSize.toFloat() / heightSpecSize.toFloat()
                var displayAspectRatio: Float
                when (mCurrAspectRatio) {
                    AspectRatio.AspectRatio_16_9 -> {
                        displayAspectRatio = 16.0f / 9.0f
                        if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270) {
                            displayAspectRatio = 1.0f / displayAspectRatio
                        }
                    }
                    AspectRatio.AspectRatio_4_3 -> {
                        displayAspectRatio = 4.0f / 3.0f
                        if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270) {
                            displayAspectRatio = 1.0f / displayAspectRatio
                        }
                    }
                    AspectRatio.AspectRatio_FIT_PARENT, AspectRatio.AspectRatio_FILL_PARENT, AspectRatio.AspectRatio_ORIGIN, AspectRatio.AspectRatio_FILL_WIDTH, AspectRatio.AspectRatio_FILL_HEIGHT -> {
                        displayAspectRatio = mVideoWidth.toFloat() / mVideoHeight.toFloat()
                        if (mVideoSarNum > 0 && mVideoSarDen > 0) {
                            displayAspectRatio = displayAspectRatio * mVideoSarNum / mVideoSarDen
                        }
                    }
                    else -> {
                        displayAspectRatio = mVideoWidth.toFloat() / mVideoHeight.toFloat()
                        if (mVideoSarNum > 0 && mVideoSarDen > 0) {
                            displayAspectRatio = displayAspectRatio * mVideoSarNum / mVideoSarDen
                        }
                    }
                }
                val shouldBeWider = displayAspectRatio > specAspectRatio
                when (mCurrAspectRatio) {
                    AspectRatio.AspectRatio_FIT_PARENT, AspectRatio.AspectRatio_16_9, AspectRatio.AspectRatio_4_3 -> if (shouldBeWider) {
                        // too wide, fix width
                        width = widthSpecSize
                        height = (width / displayAspectRatio).toInt()
                    } else {
                        // too high, fix height
                        height = heightSpecSize
                        width = (height * displayAspectRatio).toInt()
                    }
                    AspectRatio.AspectRatio_FILL_PARENT -> if (shouldBeWider) {
                        // not high enough, fix height
                        height = heightSpecSize
                        width = (height * displayAspectRatio).toInt()
                    } else {
                        // not wide enough, fix width
                        width = widthSpecSize
                        height = (width / displayAspectRatio).toInt()
                    }
                    AspectRatio.AspectRatio_FILL_WIDTH -> {
                        width = widthSpecSize
                        height = widthSpecSize * mVideoHeight / mVideoWidth
                    }
                    AspectRatio.AspectRatio_FILL_HEIGHT -> {
                        height = heightSpecSize
                        width = heightSpecSize * mVideoWidth / mVideoHeight
                    }
                    AspectRatio.AspectRatio_ORIGIN -> if (shouldBeWider) {
                        // too wide, fix width
                        width = Math.min(mVideoWidth, widthSpecSize)
                        height = (width / displayAspectRatio).toInt()
                    } else {
                        // too high, fix height
                        height = Math.min(mVideoHeight, heightSpecSize)
                        width = (height * displayAspectRatio).toInt()
                    }
                    else -> if (shouldBeWider) {
                        width = Math.min(mVideoWidth, widthSpecSize)
                        height = (width / displayAspectRatio).toInt()
                    } else {
                        height = Math.min(mVideoHeight, heightSpecSize)
                        width = (height * displayAspectRatio).toInt()
                    }
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY && heightSpecMode == View.MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize
                if (mVideoWidth * height < width * mVideoHeight) {
                    width = height * mVideoWidth / mVideoHeight
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    height = width * mVideoHeight / mVideoWidth
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY) {
                width = widthSpecSize
                height = width * mVideoHeight / mVideoWidth
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    height = heightSpecSize
                }
            } else if (heightSpecMode == View.MeasureSpec.EXACTLY) {
                height = heightSpecSize
                width = height * mVideoWidth / mVideoHeight
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    width = widthSpecSize
                }
            } else {
                width = mVideoWidth
                height = mVideoHeight
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    height = heightSpecSize
                    width = height * mVideoWidth / mVideoHeight
                }
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    width = widthSpecSize
                    height = width * mVideoHeight / mVideoWidth
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        measureWidth = width
        measureHeight = height
    }

    fun setVideoSampleAspectRatio(videoSarNum: Int, videoSarDen: Int) {
        mVideoSarNum = videoSarNum
        mVideoSarDen = videoSarDen
    }

    fun setVideoSize(videoWidth: Int, videoHeight: Int) {
        mVideoWidth = videoWidth
        mVideoHeight = videoHeight
    }

    fun setVideoRotation(videoRotationDegree: Int) {
        mVideoRotationDegree = videoRotationDegree
    }

    fun setAspectRatio(@AspectRatio aspectRatio: Int) {
        mCurrAspectRatio = aspectRatio
    }
}