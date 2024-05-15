package me.csxiong.uiux.ui.video.render

import android.content.Context
import kotlin.jvm.JvmOverloads
import android.view.TextureView
import android.graphics.SurfaceTexture
import android.os.Build
import android.util.AttributeSet
import android.view.Surface
import android.view.View
import me.csxiong.uiux.ui.video.*
import me.csxiong.uiux.ui.video.decoder.IPlayer
import me.csxiong.uiux.ui.video.XPlayer
import me.csxiong.uiux.utils.print
import java.lang.ref.WeakReference

/**
 * 作为渲染的组件
 *
 * 使用TextureView时，需要开启硬件加速（系统默认是开启的）。
 * 如果硬件加速是关闭的，会造成[SurfaceTextureListener.onSurfaceTextureAvailable]不执行。
 */
class RenderTextureView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TextureView(context, attrs), IRender {

    val TAG = "RenderTextureView"

    private var mRenderCallback: IRenderCallback? = null

    private val mRenderMeasure: RenderMeasure

    var ownSurfaceTexture: SurfaceTexture? = null
        private set

    var isTakeOverSurfaceTexture = false

    private var isReleased = false

    /**
     * 测量并设置对应的尺寸
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mRenderMeasure.doMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mRenderMeasure.measureWidth, mRenderMeasure.measureHeight)
    }

    override fun setVideoSampleAspectRatio(videoSarNum: Int, videoSarDen: Int) {
        if (videoSarNum > 0 && videoSarDen > 0) {
            mRenderMeasure.setVideoSampleAspectRatio(videoSarNum, videoSarDen)
            requestLayout()
        }
    }

    override fun setRenderCallback(renderCallback: IRenderCallback?) {
        this.mRenderCallback = renderCallback
    }

    override fun setVideoRotation(degree: Int) {
        mRenderMeasure.setVideoRotation(degree)
        rotation = degree.toFloat()
    }

    override fun updateAspectRatio(@AspectRatio aspectRatio: Int) {
        mRenderMeasure.setAspectRatio(aspectRatio)
        requestLayout()
    }

    override fun updateVideoSize(videoWidth: Int, videoHeight: Int) {
        mRenderMeasure.setVideoSize(videoWidth, videoHeight)
        requestLayout()
    }

    override fun getRenderView(): View {
        return this
    }

    override fun prepare() {
        surfaceTextureListener = InternalSurfaceTextureListener()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        //fixed bug on before android 4.4
        //modify 2018/11/16
        //java.lang.RuntimeException: Error during detachFromGLContext (see logcat for details)
        //   at android.graphics.SurfaceTexture.detachFromGLContext(SurfaceTexture.java:215)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            release()
        }
    }

    override fun release() {
        ownSurfaceTexture?.release()
        ownSurfaceTexture = null
        surface?.release()
        surface = null
        surfaceTextureListener = null
        isReleased = true
    }

    override fun isReleased(): Boolean {
        return isReleased
    }

    private var surface: Surface? = null

    /**
     * 内部关于render如何绑定和回收对应surface的处理
     */
    private inner class InternalRenderHolder(
        textureView: RenderTextureView,
        surfaceTexture: SurfaceTexture?
    ) : IRenderHolder {

        var mSurfaceRefer: WeakReference<Surface>?

        var mTextureRefer: WeakReference<RenderTextureView>?

        val textureView: RenderTextureView?
            get() = mTextureRefer?.get()

        override fun bindPlayer(player: IPlayer) {
            val textureView = textureView
            if (mSurfaceRefer != null && textureView != null) {
                val surfaceTexture = textureView.ownSurfaceTexture
                val useTexture = textureView.surfaceTexture
                var isReleased = false
                //检查SurfaceTexture是否释放
                if (surfaceTexture != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    isReleased = surfaceTexture.isReleased
                }
                val available = surfaceTexture != null && !isReleased
                //When the user sets the takeover flag and SurfaceTexture is available.
                if (textureView.isTakeOverSurfaceTexture && available) {
                    //if SurfaceTexture not set or current is null, need set it.
                    if (surfaceTexture != useTexture) {
                        textureView.setSurfaceTexture(surfaceTexture!!)
                    } else {
                        val surface = textureView.surface
                        //release current Surface if not null.
                        surface?.release()
                        //create Surface use loginOut SurfaceTexture
                        val newSurface = Surface(surfaceTexture)
                        //set it for player
                        player.setSurface(newSurface)
                        //record the new Surface
                        textureView.surface = newSurface
                    }
                } else {
                    val surface = mSurfaceRefer?.get()
                    if (surface != null) {
                        player.setSurface(surface)
                        //record the Surface
                        textureView.surface = surface
                    }
                }
            }
        }

        init {
            mTextureRefer = WeakReference(textureView)
            mSurfaceRefer = WeakReference(Surface(surfaceTexture))
        }

    }

    private inner class InternalSurfaceTextureListener : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            mRenderCallback?.onSurfaceCreated(
                InternalRenderHolder(this@RenderTextureView, surface), width, height
            )
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            mRenderCallback?.onSurfaceChanged(
                InternalRenderHolder(this@RenderTextureView, surface), 0, width, height
            )
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            mRenderCallback?.onSurfaceDestroy(
                InternalRenderHolder(this@RenderTextureView, surface)
            )
            if (ownSurfaceTexture != surface) {
                "释放旧的surfaceTexture".print(XPlayer.TAG)
                ownSurfaceTexture?.release()
                ownSurfaceTexture = null
            }
            if (isTakeOverSurfaceTexture) {
                ownSurfaceTexture = surface
            }
            //fixed bug on before android 4.4
            //modify 2018/11/16
            //BugFix:java.lang.RuntimeException: Error during detachFromGLContext (see logcat for details)
            //   at android.graphics.SurfaceTexture.detachFromGLContext(SurfaceTexture.java:215)
            //all return false.
            return !isTakeOverSurfaceTexture
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    init {
        mRenderMeasure = RenderMeasure()
        surfaceTextureListener = InternalSurfaceTextureListener()
    }
}