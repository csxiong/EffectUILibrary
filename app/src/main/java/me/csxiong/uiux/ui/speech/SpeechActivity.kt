package me.csxiong.uiux.ui.speech

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.alibaba.android.arouter.facade.annotation.Route
import me.csxiong.ipermission.EnsureAllPermissionCallBack
import me.csxiong.ipermission.IPermission
import me.csxiong.library.base.APP
import me.csxiong.library.base.BaseActivity
import me.csxiong.uiux.R
import me.csxiong.uiux.databinding.ActivitiySpeechBinding
import java.util.Locale


/**
 * 语音Demo
 */
@Route(path = "/main/speech", name = "语音识别")
class SpeechActivity: BaseActivity<ActivitiySpeechBinding>() {

    private val recognizer by lazy { SpeechRecognizer.createSpeechRecognizer(APP.get()) }

    override fun getLayoutId(): Int {
        return R.layout.activitiy_speech
    }

    override fun initView() {
        IPermission(this)
            .request(Manifest.permission.RECORD_AUDIO)
            .excute(object: EnsureAllPermissionCallBack(){
                override fun onPreRequest(requestList: MutableList<String>?) {
                }

                override fun onAllPermissionEnable(isEnable: Boolean) {
                    if(isEnable){
                        recognizer.setRecognitionListener(object : RecognitionListener {
                            override fun onReadyForSpeech(params: Bundle) {}
                            override fun onBeginningOfSpeech() {
                                mViewBinding.text.text = "识别中..."
                            }
                            override fun onRmsChanged(rmsdB: Float) {}
                            override fun onBufferReceived(buffer: ByteArray) {}
                            override fun onEndOfSpeech() {}
                            override fun onError(error: Int) {}
                            override fun onResults(results: Bundle) {
                                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                if (matches != null && !matches.isEmpty()) {
                                    val text = matches[0] // 获取识别结果中的第一条
                                    // 使用识别到的文本
                                    mViewBinding.text.text = text
                                }
                            }

                            override fun onPartialResults(partialResults: Bundle) {}
                            override fun onEvent(eventType: Int, params: Bundle) {}
                        })
                    }
                }

            })

        mViewBinding.btn.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT,"请说话...")
            }
            recognizer.startListening(intent)
        }
    }

    override fun initData() {
    }

    override fun onDestroy() {
        super.onDestroy()
        recognizer.destroy()
    }
}