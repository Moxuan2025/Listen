package com.demo.listen.net

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.tencent.cloud.realtime.tts.RealTimeSpeechSynthesizer
import com.tencent.cloud.realtime.tts.RealTimeSpeechSynthesizerListener
import com.tencent.cloud.realtime.tts.RealTimeSpeechSynthesizerRequest
import com.tencent.cloud.realtime.tts.SpeechSynthesizerResponse
import com.tencent.cloud.realtime.tts.core.ws.Credential
import com.tencent.cloud.realtime.tts.core.ws.SpeechClient
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.Properties
import java.util.UUID

object TencentSpeechHelper {

    // 密钥变量（不再硬编码）
    private var APP_ID: String = ""
    private var SECRET_ID: String = ""
    private var SECRET_KEY: String = ""

    // 是否已加载密钥
    private var keyLoaded = false

    /**
     * 从 assets 加载密钥（第一次调用时自动执行）
     */
    private fun ensureKeysLoaded(context: Context) {
        if (keyLoaded) return
        try {
            val props = Properties()
            context.applicationContext.assets.open("tencent_tts.properties").use { stream ->
                props.load(stream)
            }
            APP_ID = props.getProperty("APP_ID", "")
            SECRET_ID = props.getProperty("SECRET_ID", "")
            SECRET_KEY = props.getProperty("SECRET_KEY", "")
            keyLoaded = true
            Log.d("TTS", "密钥加载成功 APP_ID=$APP_ID")
        } catch (e: Exception) {
            Log.e("TTS", "密钥加载失败，请检查 assets/tencent_tts.properties", e)
            // 不抛出异常，避免崩溃，后续合成会因 APP_ID 为空而给出提示
        }
    }

    private val ttsProxy = SpeechClient()
    private var synthesizer: RealTimeSpeechSynthesizer? = null
    private var exoPlayer: ExoPlayer? = null

    fun synthesisAndPlay(text: String, context: Context, onComplete: () -> Unit) {
        // ---- 新增：保证密钥已加载 ----
        ensureKeysLoaded(context)
        if (APP_ID.isEmpty()) {
            runOnUiThread(context) {
                Toast.makeText(context, "密钥未配置", Toast.LENGTH_SHORT).show()
                onComplete()
            }
            return
        }
        if (text.isBlank()) {
            Toast.makeText(context, "朗读文本为空", Toast.LENGTH_SHORT).show()
            onComplete()
            return
        }

        // 启动线程，避免阻塞UI
        Thread {
            try {
                val credential = Credential(APP_ID, SECRET_ID, SECRET_KEY, null)
                val request = RealTimeSpeechSynthesizerRequest().apply {
                    this.text = text
                    volume = 0f
                    speed = 0f
                    codec = "mp3"
                    sampleRate = 16000
                    voiceType = 101021          // 智瑞 男声，可根据需要更换
                    enableSubtitle = false
                    emotionCategory = "neutral"
                    emotionIntensity = 100
                    sessionId = UUID.randomUUID().toString()
                }

                val listener = object : RealTimeSpeechSynthesizerListener() {
                    private var fullAudio = ByteArray(0)

                    override fun onSynthesisStart(response: SpeechSynthesizerResponse?) {
                        fullAudio = ByteArray(0)
                        Log.d("TTS", "合成开始: $text")
                    }

                    override fun onAudioResult(buffer: ByteBuffer?) {
                        buffer?.let {
                            val chunk = ByteArray(it.remaining())
                            it.get(chunk)
                            val newArray = ByteArray(fullAudio.size + chunk.size)
                            System.arraycopy(fullAudio, 0, newArray, 0, fullAudio.size)
                            System.arraycopy(chunk, 0, newArray, fullAudio.size, chunk.size)
                            fullAudio = newArray
                        }
                    }

                    override fun onSynthesisEnd(response: SpeechSynthesizerResponse?) {
                        Log.d("TTS", "合成结束，音频大小：${fullAudio.size}")
                        if (fullAudio.isNotEmpty()) {
                            playAudioBytes(fullAudio, context)
                        }
                        // 等待播放完成
                        Thread.sleep(3000)
                        synthesizer = null
                        runOnUiThread(context) { onComplete() }
                    }

                    override fun onSynthesisFail(response: SpeechSynthesizerResponse?) {
                        Log.e("TTS", "合成失败: ${response?.message}")
                        runOnUiThread(context) {
                            Toast.makeText(context, "朗读失败", Toast.LENGTH_SHORT).show()
                            onComplete()
                        }
                    }

                    override fun onSynthesisCancel() {
                        runOnUiThread(context) { onComplete() }
                    }

                    override fun onTextResult(response: SpeechSynthesizerResponse?) {}
                }

                synthesizer?.cancel()
                synthesizer = RealTimeSpeechSynthesizer(ttsProxy, credential, request, listener)
                synthesizer?.start()

            } catch (e: Exception) {
                Log.e("TTS", "合成异常", e)
                runOnUiThread(context) {
                    Toast.makeText(context, "朗读异常", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            }
        }.start()
    }

    /**
     * 播放音频字节数组
     */
    private fun playAudioBytes(bytes: ByteArray, context: Context) {
        try {
            val tempFile = File(context.cacheDir, "tts_${System.currentTimeMillis()}.mp3")
            FileOutputStream(tempFile).use { it.write(bytes) }

            runOnUiThread(context) {
                exoPlayer?.release()
                exoPlayer = ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(tempFile.toURI().toString()))
                    prepare()
                    play()
                }
            }
        } catch (e: Exception) {
            Log.e("TTS", "播放失败", e)
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        synthesizer?.cancel()
        exoPlayer?.release()
    }

    private fun runOnUiThread(context: Context, action: () -> Unit) {
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            action()
        } else {
            android.os.Handler(context.mainLooper).post { action() }
        }
    }
}