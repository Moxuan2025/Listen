package com.demo.listen.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

import android.content.Context
import com.demo.listen.Layout.Assessment.AssessmentResult
import com.demo.listen.Layout.Assessment.AssessmentQuestion

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

    //fun getRole(context: Context): String? = prefs.getString("role", null)

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

object ServerApi {
  //配置url
    private const val BASE_URL = "http://b98bae58.natappfree.cc"

//    private const val BASE_URL = "127.0.0.1:8000"

    data class AuthResult(
        val token: String,
        val userName: String?,
        val role: String?
    )

    // 开始评估会话（返回 sessionId）
    suspend fun startAssessment(childUsername: String, level: Int): String {
        // TODO: 调用 POST /api/v1/assessments/start，返回 { sessionId: "xxx" }
        return "mock-session-id"
    }

    // 提交单题答案（可批量，也可每道题提交）
    suspend fun submitAnswer(sessionId: String, questionIndex: Int, answer: String) {
        // TODO: POST /api/v1/assessments/answer
    }

    // 完成评估，返回评估结果
    suspend fun finishAssessment(sessionId: String): AssessmentResult {
        // TODO: POST /api/v1/assessments/finish，返回评分
        return AssessmentResult(80f, 75f, 82f, 79f)
    }

    private suspend fun request(
        method: String,
        path: String,
        body: JSONObject? = null
    ): JSONObject = withContext(Dispatchers.IO) {
        val conn = (URL(BASE_URL + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            doInput = true
            if (body != null) doOutput = true
        }

        if (body != null) {
            conn.outputStream.use { os ->
                os.write(body.toString().toByteArray(Charsets.UTF_8))
            }
        }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val resp = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

        if (resp.isBlank()) {
            JSONObject("""{"ok":false,"message":"empty response","data":null}""")
        } else {
            JSONObject(resp)
        }
    }

    suspend fun checkUsername(username: String): Boolean {
        val path = "/api/v1/auth/check-username?username=" +
                URLEncoder.encode(username, "UTF-8")
        val resp = request("GET", path)
        return resp.optJSONObject("data")?.optBoolean("exists", false) ?: false
    }

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

//    suspend fun register(
//        username: String,
//        password: String,
//        name: String,
//        role: String,
//        guardian: String?,
//        children: List<String>
//    ): RegisterResult {
//        // 内部请求体的构建也要对应修改
//        val requestBody = mapOf(
//            "username" to username,
//            "password" to password,
//            "name" to name,
//            "role" to role,
//            "guardian" to (guardian ?: ""), // 或直接 null
//            "children" to children
//        )
//
//        val resp = request("POST", "/api/v1/auth/register", body)
//        if (!resp.optBoolean("ok")) {
//            error(resp.optString("message", "注册失败"))
//        }
//
//        val data = resp.getJSONObject("data")
//        val user = data.optJSONObject("user")
//        return AuthResult(
//            token = data.optString("token"),
//            userName = user?.optString("name"),
//            role = user?.optString("role")
//        )
//    }
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
}