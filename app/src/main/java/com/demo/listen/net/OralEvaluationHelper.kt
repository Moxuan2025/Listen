package com.demo.listen.net

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.tencent.cloud.soe.TAIOralController
import com.tencent.cloud.soe.entity.TAIConfig
import com.tencent.cloud.soe.listener.TAIListener
import com.tencent.cloud.soe.entity.OralEvaluationRequest
import com.tencent.cloud.soe.entity.ClientException
import com.tencent.cloud.soe.entity.ServerException
import org.json.JSONObject
import java.util.Locale
import java.util.Properties

object OralEvaluationHelper {

    private var APP_ID: String = ""
    private var SECRET_ID: String = ""
    private var SECRET_KEY: String = ""

    private var loaded = false

    private fun loadKeys(context: Context) {
        if (loaded) return
        try {
            val props = Properties()
            context.applicationContext.assets.open("tencent_tts.properties").use {
                props.load(it)
            }
            APP_ID = props.getProperty("APP_ID", "")
            SECRET_ID = props.getProperty("SECRET_ID", "")
            SECRET_KEY = props.getProperty("SECRET_KEY", "")
            loaded = true
            Log.d("OralEval", "密钥加载成功 APP_ID=$APP_ID")
        } catch (e: Exception) {
            Log.e("OralEval", "密钥加载失败", e)
        }
    }

    /**
     * 原有的自动 VAD 评测方法（保留不动，供其他场景使用）
     */
    fun startEvaluation(context: Context, refText: String, onResult: (String) -> Unit) {
        loadKeys(context)
        if (APP_ID.isEmpty()) {
            runOnUiThread(context) {
                Toast.makeText(context, "密钥未配置", Toast.LENGTH_SHORT).show()
                onResult("密钥未配置")
            }
            return
        }
        val apiParams = HashMap<String, Any>()
        apiParams["SERVER_ENGINE_TYPE"] = "16k_zh"
        apiParams["REF_TEXT"] = refText
        apiParams["EVAL_MODE"] = 1
        apiParams["TEXT_MODE"] = 0
        apiParams["SCORE_COEFF"] = 1.0
        apiParams["SENTENCE_INFO_ENABLED"] = 1

        val config = TAIConfig.Builder()
            .appID(APP_ID.toIntOrNull() ?: 0)
            .secretID(SECRET_ID)
            .secretKey(SECRET_KEY)
            .apiParams(apiParams)
            .enableVAD(true)
            .vadInterval(3000)
            .vadDBThreshold(20)
            .stopOnVadDetected(true)
            .dataSource(com.tencent.cloud.soe.audio.data.TAIDataSource(true))
            .connectTimeout(5000)
            .build()

        val controller = TAIOralController(config)
        controller.startOralEvaluation(object : TAIListener {
            override fun onMessage(msg: String?) {}
            override fun onVad() { Log.d("OralEval", "静音检测触发") }
            override fun onVolumeDb(volume: Float) {}

            override fun onFinish(msg: String?) {
                Log.d("OralEval", "评测完成: $msg")
                handleResult(msg, context, onResult)
                controller.release()
            }

            override fun onError(
                request: OralEvaluationRequest?,
                client: ClientException?,
                server: ServerException?,
                resp: String?
            ) {
                val errMsg = server?.message ?: client?.message ?: "未知错误"
                Log.e("OralEval", "评测错误: $errMsg, resp=$resp")
                runOnUiThread(context) {
                    Toast.makeText(context, "评测失败: $errMsg", Toast.LENGTH_SHORT).show()
                    onResult("评测失败")
                }
                controller.release()
            }
        }, null)
    }

    /**
     * 手动停止的评测方法，关闭 VAD，返回 controller 供外部调用 stop
     */
    fun startManualEvaluation(
        context: Context,
        refText: String,
        onResult: (String) -> Unit
    ): TAIOralController {
        // 确保密钥已加载
        loadKeys(context)
        if (APP_ID.isEmpty()) {
            runOnUiThread(context) {
                Toast.makeText(context, "密钥未配置", Toast.LENGTH_SHORT).show()
                onResult("密钥未配置")
            }
            // 返回一个空的 controller 防止空指针，但外部不会再调用 stop 了
            return TAIOralController(TAIConfig.Builder().appID(0).secretID("").secretKey("").apiParams(HashMap()).build())
        }

        val apiParams = HashMap<String, Any>()
        apiParams["SERVER_ENGINE_TYPE"] = "16k_zh"
        apiParams["REF_TEXT"] = refText
        apiParams["EVAL_MODE"] = 1
        apiParams["TEXT_MODE"] = 0
        apiParams["SCORE_COEFF"] = 1.0
        apiParams["SENTENCE_INFO_ENABLED"] = 1

        val config = TAIConfig.Builder()
            .appID(APP_ID.toIntOrNull() ?: 0)
            .secretID(SECRET_ID)
            .secretKey(SECRET_KEY)
            .apiParams(apiParams)
            .enableVAD(false)                // 关闭自动静音检测
            .dataSource(com.tencent.cloud.soe.audio.data.TAIDataSource(true))
            .connectTimeout(5000)
            .build()

        val controller = TAIOralController(config)
        controller.startOralEvaluation(object : TAIListener {
            override fun onMessage(msg: String?) {}
            override fun onVad() {}
            override fun onVolumeDb(volume: Float) {}

            override fun onFinish(msg: String?) {
                Log.d("OralEval", "手动评测完成: $msg")
                handleResult(msg, context, onResult)
                controller.release()
            }

            override fun onError(
                request: OralEvaluationRequest?,
                client: ClientException?,
                server: ServerException?,
                resp: String?
            ) {
                val errMsg = server?.message ?: client?.message ?: "未知错误"
                Log.e("OralEval", "手动评测错误: message=$errMsg")
                Log.e("OralEval", "完整响应: $resp")
                
                // 解析具体错误类型，提供友好提示
                var userFriendlyMsg = "评测失败: $errMsg"
                
                // 尝试从 resp 中解析错误码
                var errorCode = -1
                try {
                    if (resp != null) {
                        val json = JSONObject(resp)
                        errorCode = json.optInt("code", -1)
                    }
                } catch (e: Exception) {
                    Log.e("OralEval", "解析错误码失败: ${e.message}")
                }
                
                when (errorCode) {
                    4103 -> {
                        userFriendlyMsg = "参考文本包含无法识别的词汇，请更换常用字词"
                        Log.e("OralEval", "OOV错误：参考文本中有超出词库的字词")
                    }
                    else -> {
                        if (errorCode != -1) {
                            userFriendlyMsg = "评测失败: $errMsg (错误码: $errorCode)"
                        }
                    }
                }
                
                runOnUiThread(context) {
                    Toast.makeText(context, userFriendlyMsg, Toast.LENGTH_LONG).show()
                    onResult(userFriendlyMsg)
                }
                controller.release()
            }
        }, null)

        return controller
    }

    /**
     * 手动停止评估
     */
    fun stopManualEvaluation(controller: TAIOralController?) {
        controller?.stopOralEvaluation()
    }

    /**
     * 统一的评分结果解析
     */
    private fun handleResult(msg: String?, context: Context, onResult: (String) -> Unit) {
        try {
            if (msg == null) {
                runOnUiThread(context) { onResult("解析失败") }
                return
            }
            val json = JSONObject(msg)
            val respObj = json.optJSONObject("result")
            if (respObj == null) {
                // 可能返回错误信息
                val error = json.optJSONObject("Error")
                if (error != null) {
                    val code = error.optString("Code", "未知")
                    val message = error.optString("Message", "未知")
                    runOnUiThread(context) { onResult("评测失败: $code - $message") }
                    return
                }
                runOnUiThread(context) { onResult("解析失败") }
                return
            }
            val score = respObj.optDouble("SuggestedScore", respObj.optDouble("PronAccuracy", -1.0))
            val result = if (score >= 0) {
                String.format(Locale.US, "%.2f", score)
            } else {
                "评分失败"
            }
            runOnUiThread(context) { onResult(result) }
        } catch (e: Exception) {
            Log.e("OralEval", "解析异常", e)
            runOnUiThread(context) { onResult("解析失败") }
        }
    }

    private fun runOnUiThread(context: Context, action: () -> Unit) {
        val handler = android.os.Handler(context.mainLooper) ?: return
        handler.post { action() }
    }
}