package com.demo.listen.net

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import java.io.File

object TtsPlayer {

    fun playMp3Base64(context: Context, base64Audio: String, onComplete: () -> Unit) {
        try {
            Log.d("TTS_PLAYER", "开始解码 base64，长度：${base64Audio.length}")
            val bytes = Base64.decode(base64Audio, Base64.DEFAULT)
            Log.d("TTS_PLAYER", "解码成功，字节数：${bytes.size}")

            val file = File(context.cacheDir, "tts_${System.currentTimeMillis()}.mp3")
            file.writeBytes(bytes)
            Log.d("TTS_PLAYER", "临时文件写入：${file.absolutePath}，大小：${file.length()}")

            val mp = MediaPlayer()
            mp.setDataSource(file.absolutePath)

            mp.setOnPreparedListener {
                Log.d("TTS_PLAYER", "MediaPlayer 准备完成，开始播放")
                mp.start()
            }

            mp.setOnCompletionListener {
                Log.d("TTS_PLAYER", "播放完成")
                mp.release()
                file.delete()
                onComplete()
            }

            mp.setOnErrorListener { mediaPlayer, what, extra ->
                Log.e("TTS_PLAYER", "播放出错 what=$what extra=$extra")
                mediaPlayer.release()
                file.delete()
                onComplete()
                true
            }

            mp.prepareAsync()
            Log.d("TTS_PLAYER", "prepareAsync 已调用")
        } catch (e: Exception) {
            Log.e("TTS_PLAYER", "播放异常", e)
            onComplete()
        }
    }
}