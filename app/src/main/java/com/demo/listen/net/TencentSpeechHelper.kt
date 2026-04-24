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
import java.util.UUID

object TencentSpeechHelper {

    // ⚠️ 请替换为你的真实密钥

    private const val APP_ID = "XXX"
    private const val SECRET_ID = "XXX"
    private const val SECRET_KEY = "XXX"


    private val ttsProxy = SpeechClient()
    private var synthesizer: RealTimeSpeechSynthesizer? = null
    private var exoPlayer: ExoPlayer? = null

    /**
     * 合成语音并播放
     * @param text 要朗读的文本
     * @param context Android Context
     * @param onComplete 播放完成后的回调（不管成功失败都会调用）
     */
    fun synthesisAndPlay(text: String, context: Context, onComplete: () -> Unit) {
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