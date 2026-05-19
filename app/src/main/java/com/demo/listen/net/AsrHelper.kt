package com.demo.listen.net

import android.content.Context
import android.util.Log
import com.tencent.aai.AAIClient
import com.tencent.aai.auth.LocalCredentialProvider
import com.tencent.aai.exception.ClientException
import com.tencent.aai.exception.ServerException
import com.tencent.aai.listener.AudioRecognizeResultListener
import com.tencent.aai.listener.AudioRecognizeStateListener
import com.tencent.aai.model.AudioRecognizeConfiguration
import com.tencent.aai.model.AudioRecognizeRequest
import com.tencent.aai.model.AudioRecognizeResult
import com.tencent.aai.audio.data.AudioRecordDataSource
import java.util.Properties

object AsrHelper {

    private const val TAG = "ASR_HELPER"

    private var APP_ID: String = ""
    private var SECRET_ID: String = ""
    private var SECRET_KEY: String = ""

    private var keyLoaded = false
    private var aaiClient: AAIClient? = null

    private fun ensureKeysLoaded(context: Context) {
        if (keyLoaded) return
        try {
            val props = Properties()
            context.applicationContext.assets.open("asr.properties").use { stream ->
                props.load(stream)
            }
            APP_ID = props.getProperty("APP_ID", "")
            SECRET_ID = props.getProperty("SECRET_ID", "")
            SECRET_KEY = props.getProperty("SECRET_KEY", "")
            keyLoaded = true
            Log.d(TAG, "ASR密钥加载成功 APP_ID=$APP_ID")
        } catch (e: Exception) {
            Log.e(TAG, "ASR密钥加载失败，请检查 assets/asr.properties", e)
        }
    }

    fun startRecognize(
        context: Context,
        engineModelType: String = "16k_zh",
        onRecognizing: (text: String) -> Unit = {},
        onComplete: (text: String) -> Unit = {},
        onError: (code: Int, message: String) -> Unit = { _, _ -> }
    ) {
        ensureKeysLoaded(context)
        if (APP_ID.isEmpty() || SECRET_ID.isEmpty() || SECRET_KEY.isEmpty()) {
            onError(-1, "密钥未配置")
            return
        }

        stopRecognize()

        try {
            val credentialProvider = LocalCredentialProvider(SECRET_KEY)
            aaiClient = AAIClient(context.applicationContext, APP_ID.toInt(), 0, SECRET_ID, credentialProvider)

            val request = AudioRecognizeRequest.Builder()
                .pcmAudioDataSource(AudioRecordDataSource(false))
                .setEngineModelType(engineModelType)
                .build()

            var finalResultHandled = false

            val listener = object : AudioRecognizeResultListener {
                override fun onSliceSuccess(
                    request: AudioRecognizeRequest,
                    result: AudioRecognizeResult,
                    seq: Int
                ) {
                    val text = result.text ?: ""
                    Log.d(TAG, "实时识别: $text")
                    onRecognizing(text)
                }

                override fun onSegmentSuccess(
                    request: AudioRecognizeRequest,
                    result: AudioRecognizeResult,
                    seq: Int
                ) {
                    // 仅记录日志，不触发最终回调，避免与 onSuccess 重复
                    val text = result.text ?: ""
                    Log.d(TAG, "稳定片段结果: $text")
                }

                override fun onSuccess(request: AudioRecognizeRequest, result: String) {
                    if (finalResultHandled) return
                    finalResultHandled = true
                    Log.d(TAG, "识别全部完成: $result")
                    onComplete(result)
                }

                override fun onFailure(
                    request: AudioRecognizeRequest?,
                    clientException: ClientException?,
                    serverException: ServerException?,
                    response: String?
                ) {
                    if (finalResultHandled) return
                    finalResultHandled = true
                    val errorMsg = serverException?.message ?: clientException?.message ?: "识别失败"
                    Log.e(TAG, "识别失败: $errorMsg")
                    onError(-1, errorMsg)
                }
            }

            val stateListener = object : AudioRecognizeStateListener {
                override fun onStartRecord(request: AudioRecognizeRequest) { Log.d(TAG, "开始录音") }
                override fun onStopRecord(request: AudioRecognizeRequest) { Log.d(TAG, "停止录音") }
                override fun onVoiceVolume(request: AudioRecognizeRequest, volume: Int) {}
                override fun onVoiceDb(db: Float) {}
                override fun onNextAudioData(audioData: ShortArray?, dataLength: Int) {}
                override fun onSilentDetectTimeOut() {}
            }

            val config = AudioRecognizeConfiguration.Builder()
                .setSilentDetectTimeOut(false)
                .build()

            Thread {
                aaiClient?.startAudioRecognize(request, listener, stateListener, config)
            }.start()

        } catch (e: ClientException) {
            Log.e(TAG, "初始化异常", e)
            onError(-1, "初始化失败: ${e.message}")
        }
    }

    fun stopRecognize() {
        Thread { aaiClient?.stopAudioRecognize() }.start()
    }

    fun cancelRecognize() {
        Thread { aaiClient?.cancelAudioRecognize() }.start()
        aaiClient = null
    }

    fun destroy() {
        cancelRecognize()
        aaiClient = null
    }
}