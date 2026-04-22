package com.demo.listen.Layout.LoginRegister

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.demo.listen.MainActivity
import com.demo.listen.R

class CompleteInfo : AppCompatActivity() {

    private var chooseIdentityFragment = ChooseIdentityFragment()
    private var infoChildFragment = InfoChildFragment()
    private var infoParentFragment = InfoParentFragment()

    private var curPage = "identity"

    private var userName: String? = null
    private var userIdentity: String? = null
    private var userChoices: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_complete_info)

        userName = intent.getStringExtra("uname")   // 用户昵称
        if (savedInstanceState == null)
            loadFragment(chooseIdentityFragment)

        findViewById<ImageButton>(R.id.bt_complete_back).setOnClickListener {
            switchPage(false)
        }
        handleFragmentResult() //处理fragment的返回信息

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
        if (next)
            when(curPage) {
                "identity" -> {
                    loadFragment(infoChildFragment)
                    curPage = "infoChild"
                }
                "infoChild" -> {
                    loadFragment(infoParentFragment)
                    curPage = "infoParent"
                }
                "infoParent" -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        putExtra("uname", userName)
                    })
                    finish()
                }
            }
        else
            when(curPage) {
                "identity" -> {
                    finish()    // 回到登录界面
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

    private fun handleFragmentResult() {
        supportFragmentManager.setFragmentResultListener("identity", this) {
                _, bundle ->
            userIdentity = bundle.getString("choice")
            Toast.makeText(this, userIdentity, Toast.LENGTH_SHORT).show()
            switchPage(true)
        }
        supportFragmentManager.setFragmentResultListener("infoChild", this) {
                _, bundle ->
            userChoices = bundle.getStringArray("choices")
            Toast.makeText(this, "info child", Toast.LENGTH_SHORT).show()
            switchPage(true)
        }
        supportFragmentManager.setFragmentResultListener("infoParent", this) {
                _, bundle ->
            userChoices = bundle.getStringArray("choices")
            Toast.makeText(this, "info parent", Toast.LENGTH_SHORT).show()
            switchPage(true)
        }

    }
}