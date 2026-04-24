package com.demo.listen.net

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import java.io.File

object AudioHelper {

    fun playBase64Mp3(context: Context, base64Audio: String, onComplete: () -> Unit) {
        try {
            Log.d("AudioHelper", "开始解码 base64，长度：${base64Audio.length}")
            val bytes = Base64.decode(base64Audio, Base64.DEFAULT)
            Log.d("AudioHelper", "解码后字节数：${bytes.size}")
            val file = File(context.cacheDir, "tts_${System.currentTimeMillis()}.mp3")
            file.writeBytes(bytes)
            Log.d("AudioHelper", "临时文件路径：${file.absolutePath}")

            MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener {
                    Log.d("AudioHelper", "MediaPlayer 准备完成，开始播放")
                    start()
                }
                setOnCompletionListener { mp ->
                    Log.d("AudioHelper", "播放结束")
                    mp.release()
                    file.delete()
                    onComplete()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("AudioHelper", "播放出错 what=$what extra=$extra")
                    mp.release()
                    file.delete()
                    onComplete()
                    true  // 返回 true 表示错误已处理，不会再回调 onCompletion
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioHelper", "播放异常", e)
            onComplete()
        }
    }
}