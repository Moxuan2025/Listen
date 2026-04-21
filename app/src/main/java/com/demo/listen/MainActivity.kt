package com.demo.listen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.demo.listen.Layout.DoTestFragment
import com.demo.listen.Layout.LoginRegister.Login
import com.demo.listen.Layout.RankFragment
import com.demo.listen.Layout.SettingFragment
import com.demo.listen.Layout.StudyFragment
import com.demo.listen.Layout.UserFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bmv: BottomNavigationView  // 导航栏
    // 主要界面
    private val studyFragment = StudyFragment()
    private val userFragment = UserFragment()
    private val rankFragment = RankFragment()

    private var loged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        bmv = findViewById<BottomNavigationView>(R.id.navigator)
        if (savedInstanceState == null)
            loadFragment(studyFragment)
        bmv.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.study_pg -> {
                    loadFragment(studyFragment)
                    true
                }
                R.id.user_pg -> {
                    loadFragment(userFragment)
                    true
                }
                R.id.rank_pg -> {
                    loadFragment(rankFragment)
                    true
                }
                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // 封装加载 Fragment 的方法
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, fragment)
            .commit()
    }
}