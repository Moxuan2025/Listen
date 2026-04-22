package com.demo.listen.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

import android.content.Context

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

    data class AuthResult(
        val token: String,
        val userName: String?,
        val role: String?
    )

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

    suspend fun register(
        username: String,
        password: String,
        name: String,
        role: String,
        identity: String? = null,
        choices: List<String>? = null
    ): AuthResult {
        val body = JSONObject().apply {
            put("username", username)
            put("password", password)
            put("name", name)
            put("role", role)
            if (identity != null) put("identity", identity)
            if (choices != null) put("choices", JSONArray(choices))
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