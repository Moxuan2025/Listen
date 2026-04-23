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
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class CompleteInfo : AppCompatActivity() {

    private var chooseIdentityFragment = ChooseIdentityFragment()
   // private var infoChildFragment = InfoChildFragment()
  //  private var infoParentFragment = InfoParentFragment()

    private var curPage = "identity"

    private lateinit var userName: String
    private lateinit var userPasswd: String
    private lateinit var userIdentity: String
    private var childChoices: Array<String>? = null
   // private var parentChoices: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_complete_info)

        userName = intent.getStringExtra("uname") ?: "Null"
        userPasswd = intent.getStringExtra("passwd") ?: "null"

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

   /* private fun switchPage(next: Boolean) {
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
    }*/
   private fun switchPage(next: Boolean) {
       if (next) {
           when (curPage) {
               "identity" -> {
                   if (userIdentity == "child") {
                       // 孩子直接注册
                       submitRegister()
                   } else {
                       // 家长进入选孩子页面
                       showChildInputDialog()
//                       loadFragment(infoChildFragment)
//                       curPage = "infoChild"
                   }
               }
               "infoChild" -> {
                   // 家长选完孩子后直接注册
                   submitRegister()
               }
           }
       } else {
           when (curPage) {
               "identity" -> finish()
//               "infoChild" -> {
//                   loadFragment(chooseIdentityFragment)
//                   curPage = "identity"
//               }
           }
       }
   }
    private fun showChildInputDialog() {
        val editText = EditText(this)
        editText.hint = "请输入孩子用户名"
        AlertDialog.Builder(this)
            .setTitle("关联已有孩子")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val childName = editText.text.toString().trim()
                if (childName.isEmpty()) {
                    Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                childChoices = arrayOf(childName)  // 转为数组，复用原有提交逻辑
                submitRegister()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    private fun submitRegister() {
        lifecycleScope.launch {
            try {
                val role = when (userIdentity) {
                    "child" -> "child"
                    "parent" -> "guardian"
                    else -> "guardian"
                }

                val result = if (userIdentity == "child") {
                    // 孩子注册，无监护人
                    ServerApi.register(
                        username = userName,
                        password = userPasswd,
                        name = userName,
                        role = role,
                        guardian = null,
                        children = emptyList()
                    )
                } else {
                    // 家长注册，必须带一个孩子
                    val selectedChildren = childChoices?.toList() ?: emptyList()
                    if (selectedChildren.isEmpty()) {
                        Toast.makeText(this@CompleteInfo, "请选择一个孩子", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    ServerApi.register(
                        username = userName,
                        password = userPasswd,
                        name = userName,
                        role = role,
                        guardian = null,
                        children = selectedChildren
                    )
                }

                val finalRole = result.role ?: role
                SessionStore.save(this@CompleteInfo, userName, finalRole, result.token)

                startActivity(Intent(this@CompleteInfo, MainActivity::class.java).apply {
                    putExtra("uname", userName)
                    putExtra("token", result.token)
                    putExtra("role", finalRole)
                })
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@CompleteInfo, e.message ?: "注册失败", Toast.LENGTH_SHORT).show()
            }
        }
    }/*
    private fun submitRegister() {

        lifecycleScope.launch {
            try {
                val role = when (userIdentity) {
                    "child" -> "child"
                    "parent" -> "guardian"
                    "admin" -> "admin"
                    else -> "guardian"
                }

                val result = ServerApi.register(
                    username = userName,
                    password = userPasswd,
                    name = userName,
                    role = role,
                    identity = userIdentity,
                    choices = (childChoices?.toList() ?: emptyList()) + (parentChoices?.toList() ?: emptyList())
                )

                val finalRole = result.role ?: role  // 优先使用服务器返回的 role

                // 保存到 SessionStore
                SessionStore.save(this@CompleteInfo, userName, finalRole, result.token)

                // 跳转并传递 role
                startActivity(Intent(this@CompleteInfo, MainActivity::class.java).apply {
                    putExtra("uname", userName)
                    putExtra("token", result.token)
                    putExtra("role", userIdentity)
                })
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@CompleteInfo, e.message ?: "注册失败", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    /*private fun handleFragmentResult() {
        supportFragmentManager.setFragmentResultListener("identity", this) { _, bundle ->
            userIdentity = bundle.getString("choice") ?: "Null"
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
}*/
private fun handleFragmentResult() {
    supportFragmentManager.setFragmentResultListener("identity", this) { _, bundle ->
        userIdentity = bundle.getString("choice") ?: "Null"
        Toast.makeText(this, userIdentity, Toast.LENGTH_SHORT).show()
        switchPage(true)
    }

//    supportFragmentManager.setFragmentResultListener("infoChild", this) { _, bundle ->
//        childChoices = bundle.getStringArray("choices")
//        Toast.makeText(this, "已选择孩子", Toast.LENGTH_SHORT).show()
//        switchPage(true)
//    }
        }
}