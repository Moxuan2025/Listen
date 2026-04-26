package com.demo.listen.net

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

// ========== 本地存储工具 ==========
object SessionStore {
    private const val PREF = "session"
    private const val KEY_NAME = "name"
    private const val KEY_ROLE = "role"
    private const val KEY_TOKEN = "token"

    fun save(context: Context, name: String, role: String, token: String = "") {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_ROLE, role)
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun name(context: Context): String {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_NAME, "") ?: ""
    }

    fun role(context: Context): String {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_ROLE, "") ?: ""
    }

    fun token(context: Context): String {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, "") ?: ""
    }

    fun clear(context: Context) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit()
            .remove(KEY_NAME)
            .remove(KEY_ROLE)
            .remove(KEY_TOKEN)
            .apply()
    }
}

// ========== 服务器 API ==========
object ServerApi {
    private const val BASE_URL = "http://q6f969d4.natappfree.cc"
    
    // [全局 Context] 用于获取 Token
    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    data class AuthResult(
        val token: String,
        val userName: String?,
        val role: String?
    )

    data class ChildInfo(
        val username: String,
        val name: String
    )

    // 开始评估会话
    suspend fun startAssessment(childUsername: String, level: Int): String {
        val body = JSONObject().apply {
            put("child_username", childUsername)
            put("level", level)
        }
        val resp = request("POST", "/api/v1/sessions/start", body)
        if (!resp.optBoolean("ok")) {
            error(resp.optString("message", "启动评估会话失败"))
        }
        return resp.getJSONObject("data").optString("id")
    }

    // 完成评估并上传分数
    suspend fun finishAssessment(
        sessionId: String,
        level: Int,
        levelScore: Double,
        avgReadScore: Double,
        expressionScore: Double,
        readingScore: Double,
        overallScore: Double,
        answers: Map<Int, String>
    ): Boolean {
        val answersJson = JSONObject()
        answers.forEach { (index, answer) -> answersJson.put(index.toString(), answer) }

        val summary = JSONObject().apply {
            put("level", level)
            put("level_score", levelScore)
            put("avg_read_score", avgReadScore)
            put("expression_score", expressionScore)
            put("reading_score", readingScore)
            put("overall_score", overallScore)
            put("answers", answersJson)
        }

        val body = JSONObject().apply {
            put("status", "finished")
            put("finished_at", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date()))
            put("summary", summary)
        }

        val resp = request("POST", "/api/v1/sessions/$sessionId/finish", body)
        return resp.optBoolean("ok")
    }

    // 提交单题答案（预留）
    suspend fun submitAnswer(sessionId: String, questionIndex: Int, answer: String) {
        // TODO: 实现逐题提交
    }

    // 语音合成
    suspend fun textToSpeech(text: String): String {
        val body = JSONObject().apply {
            put("text", text)
            put("voiceType", 1002)
            put("speed", 0)
            put("volume", 0)
        }
        val resp = request("POST", "/api/v1/tts/synthesize", body)
        if (!resp.optBoolean("ok")) {
            error(resp.optString("message", "TTS 失败"))
        }
        return resp.getJSONObject("data").optString("audio")
    }

    // 初始化口语评测
    suspend fun initOralEvaluation(refText: String, evalMode: String): String {
        val body = JSONObject().apply {
            put("refText", refText)
            put("evalMode", evalMode)
        }
        val resp = request("POST", "/api/v1/soe/init", body)
        if (!resp.optBoolean("ok")) {
            error(resp.optString("message", "初始化失败"))
        }
        return resp.getJSONObject("data").optString("sessionId")
    }

    // 提交评测音频
    suspend fun evaluateAudio(sessionId: String, audioBase64: String, isEnd: Boolean): JSONObject {
        val body = JSONObject().apply {
            put("sessionId", sessionId)
            put("userVoiceData", audioBase64)
            put("isEnd", if (isEnd) 1 else 0)
        }
        val resp = request("POST", "/api/v1/soe/evaluate", body)
        if (!resp.optBoolean("ok")) {
            error(resp.optString("message", "评测失败"))
        }
        return resp.getJSONObject("data")
    }

    // 检查用户名
    suspend fun checkUsername(username: String): Boolean {
        val path = "/api/v1/auth/check-username?username=" + URLEncoder.encode(username, "UTF-8")
        val resp = request("GET", path)
        return resp.optJSONObject("data")?.optBoolean("exists", false) ?: false
    }

    // 获取当前用户可见的孩子列表
    suspend fun getChildren(): List<ChildInfo> {
        Log.e("TRACE", "getChildren called")
        val resp = request("GET", "/api/v1/children")
        Log.e("TRACE", "getChildren response: ${resp.toString().take(100)}")
        
        if (!resp.optBoolean("ok")) {
            error(resp.optString("message", "获取孩子列表失败"))
        }
        val data = resp.optJSONArray("data")
        val children = mutableListOf<ChildInfo>()
        if (data != null) {
            for (i in 0 until data.length()) {
                val obj = data.getJSONObject(i)
                children.add(ChildInfo(
                    username = obj.getString("username"),
                    name = obj.optString("name", obj.getString("username"))
                ))
            }
        }
        return children
    }

    // 获取指定用户的详细信息（用于获取孩子的真实姓名）
    suspend fun getUserInfo(username: String): JSONObject {
        val path = "/api/v1/users/$username"
        val resp = request("GET", path)
        if (!resp.optBoolean("ok")) {
            error(resp.optString("message", "获取用户信息失败"))
        }
        return resp.getJSONObject("data")
    }

    // 获取孩子档案信息
    suspend fun getChildProfile(childUsername: String): JSONObject {
        val path = "/api/v1/children/$childUsername/profile"
        val resp = request("GET", path)
        if (!resp.optBoolean("ok")) {
            error(resp.optString("message", "获取档案失败"))
        }
        return resp.getJSONObject("data")
    }

    // 监护人关联孩子
    suspend fun addChild(childUsername: String): JSONObject {
        val body = JSONObject().put("childUsername", childUsername)
        return request("POST", "/api/v1/children/add", body)
    }

    // 登录
    suspend fun login(username: String, password: String): AuthResult {
        val body = JSONObject().apply {
            put("username", username)
            put("password", password)
        }
        val resp = request("POST", "/api/v1/auth/login", body)
        if (!resp.optBoolean("ok")) {
            error(resp.optString("message", "登录失败"))
        }
        val data = resp.getJSONObject("data")
        val user = data.optJSONObject("user")
        return AuthResult(
            token = data.optString("token"),
            userName = user?.optString("name"),
            role = user?.optString("role")
        )
    }

    // 注册
    suspend fun register(
        username: String,
        password: String,
        name: String,
        role: String,
        guardian: String?,
        children: List<String>
    ): AuthResult {
        val body = JSONObject().apply {
            put("username", username)
            put("password", password)
            put("name", name)
            put("role", role)
            put("guardian", guardian ?: "")
            put("children", JSONArray(children))
        }
        val resp = request("POST", "/api/v1/auth/register", body)
        if (!resp.optBoolean("ok")) {
            error(resp.optString("message", "注册失败"))
        }
        val data = resp.getJSONObject("data")
        val user = data.optJSONObject("user")
        return AuthResult(
            token = data.optString("token"),
            userName = user?.optString("name"),
            role = user?.optString("role")
        )
    }

    // 通用 HTTP 请求
    internal suspend fun request(
        method: String,
        path: String,
        body: JSONObject? = null
    ): JSONObject = withContext(Dispatchers.IO) {
        Log.e("TRACE", "request called: $method $path")
        
        val conn = (URL(BASE_URL + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            
            // [核心修复] 使用全局 appContext 获取 Token
            if (::appContext.isInitialized) {
                val token = SessionStore.token(appContext)
                Log.e("AUTH", "Token length=${token.length}, value=[${token.take(10)}...]")
                
                if (token.isNotEmpty()) {
                    setRequestProperty("Authorization", "Bearer $token")
                    Log.e("HTTP", "Authorization Header set to Bearer ${token.take(10)}...")
                } else {
                    Log.e("AUTH", "WARNING: Token is EMPTY!")
                }
            } else {
                Log.e("AUTH", "ERROR: appContext not initialized! Call ServerApi.init() first.")
            }

            doInput = true
            if (body != null) doOutput = true
        }

        if (body != null) {
            conn.outputStream.use { os ->
                os.write(body.toString().toByteArray(Charsets.UTF_8))
            }
        }

        // [步骤 3] 验证网络请求
        Log.e("HTTP", "URL=${BASE_URL + path}")
        val code = conn.responseCode
        Log.e("HTTP", "Response Code=$code")
        
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val resp = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

        if (resp.isBlank()) {
            JSONObject("""{"ok":false,"message":"empty response","data":null}""")
        } else {
            JSONObject(resp)
        }
    }
}
