package com.demo.listen.Layout.LoginRegister

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.demo.listen.MainActivity
import com.demo.listen.R
import com.demo.listen.net.ServerApi
import com.demo.listen.net.SessionStore
import kotlinx.coroutines.launch
import com.demo.listen.Layout.guardian.GuardianActivity

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    fun login(view: View) {
        val unameEt = findViewById<EditText>(R.id.uname)
        val passwdEt = findViewById<EditText>(R.id.passwd)
        val registerBox = findViewById<CheckBox>(R.id.newRegister)

        val uname = unameEt.text.toString().trim()
        val passwd = passwdEt.text.toString()
        val isRegister = registerBox.isChecked

        if (uname.isEmpty()) {
            Toast.makeText(this, "名字不能为空", Toast.LENGTH_SHORT).show()
            return
        } else if (passwd.isEmpty()) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                if (isRegister) {
                    val exists = ServerApi.checkUsername(uname)
                    if (exists) {
                        Toast.makeText(this@Login, "用户名已存在", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    Toast.makeText(this@Login, "用户名可用，继续完善信息", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@Login, CompleteInfo::class.java).apply {
                        putExtra("uname", uname)
                        putExtra("passwd", passwd)
                    })
                } else {
//                    val result = ServerApi.login(uname, passwd)
//                    SessionStore.save(this@Login, uname, result.role ?: "child", result.token)
//
//                    Toast.makeText(this@Login, "登录成功", Toast.LENGTH_SHORT).show()
//                    startActivity(Intent(this@Login, MainActivity::class.java).apply {
//                        putExtra("uname", uname)
//                        putExtra("token", result.token)
//                        putExtra("role", result.role ?: "child")
//                    })
//                    finish()
                    val result = ServerApi.login(uname, passwd)
                    SessionStore.save(this@Login, uname, result.role ?: "child", result.token)

                    Toast.makeText(this@Login, "登录成功", Toast.LENGTH_SHORT).show()

                    // ✅ 根据角色跳转不同页面
                    when (result.role) {
                        "guardian" -> {
                            startActivity(Intent(this@Login, GuardianActivity::class.java).apply {
                                putExtra("uname", uname)
                                putExtra("token", result.token)
                                putExtra("role", result.role)
                            })
                        }
                        else -> {
                            startActivity(Intent(this@Login, MainActivity::class.java).apply {
                                putExtra("uname", uname)
                                putExtra("token", result.token)
                                putExtra("role", result.role ?: "child")
                            })
                        }
                    }
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Login, e.message ?: "网络异常", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun findPasswd() {
        // TODO: 找回密码
    }
}