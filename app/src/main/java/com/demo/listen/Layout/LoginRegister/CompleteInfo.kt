package com.demo.listen.Layout.LoginRegister

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.demo.listen.MainActivity
import com.demo.listen.R
import com.demo.listen.net.ServerApi
import com.demo.listen.net.SessionStore
import kotlinx.coroutines.launch

class CompleteInfo : AppCompatActivity() {

    private var chooseIdentityFragment = ChooseIdentityFragment()
    private var infoChildFragment = InfoChildFragment()
    private var infoParentFragment = InfoParentFragment()

    private var curPage = "identity"

    private var userName: String? = null
    private var userPasswd: String? = null
    private var userIdentity: String? = null
    private var childChoices: Array<String>? = null
    private var parentChoices: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_complete_info)

        userName = intent.getStringExtra("uname")
        userPasswd = intent.getStringExtra("passwd")

        if (savedInstanceState == null) {
            loadFragment(chooseIdentityFragment)
        }

        findViewById<ImageButton>(R.id.bt_complete_back).setOnClickListener {
            switchPage(false)
        }

        handleFragmentResult()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.infoContent, fragment)
            .commit()
    }

    private fun switchPage(next: Boolean) {
        if (next) {
            when (curPage) {
                "identity" -> {
                    loadFragment(infoChildFragment)
                    curPage = "infoChild"
                }

                "infoChild" -> {
                    loadFragment(infoParentFragment)
                    curPage = "infoParent"
                }

                "infoParent" -> {
                    submitRegister()
                }
            }
        } else {
            when (curPage) {
                "identity" -> {
                    finish()
                }

                "infoChild" -> {
                    loadFragment(chooseIdentityFragment)
                    curPage = "identity"
                }

                "infoParent" -> {
                    loadFragment(infoChildFragment)
                    curPage = "infoChild"
                }
            }
        }
    }

    private fun submitRegister() {
        val uname = userName.orEmpty()
        val passwd = userPasswd.orEmpty()

        if (uname.isEmpty() || passwd.isEmpty()) {
            Toast.makeText(this, "注册信息不完整", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val role = when (userIdentity) {
                    "child" -> "child"
                    "parent", "家长", "监护人" -> "guardian"
                    "admin", "管理员" -> "admin"
                    else -> "guardian"
                }

                val result = ServerApi.register(
                    username = uname,
                    password = passwd,
                    name = uname,
                    role = role,
                    identity = userIdentity,
                    choices = (childChoices?.toList() ?: emptyList()) + (parentChoices?.toList() ?: emptyList())
                )

                val finalRole = result.role ?: role  // 优先使用服务器返回的 role
                Toast.makeText(this@CompleteInfo, "注册成功", Toast.LENGTH_SHORT).show()

                // 保存到 SessionStore
                SessionStore.save(this@CompleteInfo, uname, finalRole, result.token)

                // 跳转并传递 role
                startActivity(Intent(this@CompleteInfo, MainActivity::class.java).apply {
                    putExtra("uname", uname)
                    putExtra("token", result.token)
                    putExtra("role", finalRole)
                })
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@CompleteInfo, e.message ?: "注册失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleFragmentResult() {
        supportFragmentManager.setFragmentResultListener("identity", this) { _, bundle ->
            userIdentity = bundle.getString("choice")
            Toast.makeText(this, userIdentity, Toast.LENGTH_SHORT).show()
            switchPage(true)
        }

        supportFragmentManager.setFragmentResultListener("infoChild", this) { _, bundle ->
            childChoices = bundle.getStringArray("choices")
            Toast.makeText(this, "info child", Toast.LENGTH_SHORT).show()
            switchPage(true)
        }

        supportFragmentManager.setFragmentResultListener("infoParent", this) { _, bundle ->
            parentChoices = bundle.getStringArray("choices")
            Toast.makeText(this, "info parent", Toast.LENGTH_SHORT).show()
            switchPage(true)
        }
    }
}