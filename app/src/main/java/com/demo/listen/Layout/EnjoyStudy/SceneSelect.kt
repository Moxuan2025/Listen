package com.demo.listen.Layout.EnjoyStudy

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.listen.Layout.Scene
import com.demo.listen.Layout.Adapter.SceneAdapter
import com.demo.listen.R

class SceneSelect : AppCompatActivity() {

    private lateinit var schoolScene: TextView
    private lateinit var lifeScene: TextView

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SceneAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scene_select)

        mapWidget()
        initRecyclerView()
        initPage()
        handleClick()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mapWidget() {
        schoolScene = findViewById<TextView>(R.id.tv_scene_school)
        lifeScene = findViewById<TextView>(R.id.tv_scene_life)
    }

    private fun initPage() {
        // TODO: 模拟从网络或本地数据库获取数据
        loadSceneList(testLifeScenesList)
    }

    private fun handleClick() {
        schoolScene.setOnClickListener {
            resetBackground()
            schoolScene.setBackgroundResource(R.drawable.bg_frank_navi_green)
        }
        lifeScene.setOnClickListener {
            resetBackground()
            lifeScene.setBackgroundResource(R.drawable.bg_frank_navi_green)
        }
    }

    private fun resetBackground() {
        schoolScene.setBackgroundResource(R.drawable.bg_frank_navi_yellow)
        lifeScene.setBackgroundResource(R.drawable.bg_frank_navi_yellow)
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.rv_scenes)
        // 设置布局管理器
        recyclerView.layoutManager = LinearLayoutManager(this)
        // 创建适配器
        adapter = SceneAdapter(emptyList()) { scene ->
            onSceneSelected(scene)
        }
        recyclerView.adapter = adapter
    }

    private fun loadSceneList(scene: List<Scene>) {
        adapter.updateData(scene)
    }

    private fun onSceneSelected(scene: Scene) {
        // 跳转到智能对话页面，并传递场景信息
        val intent = Intent(this@SceneSelect, SceneChatActivity::class.java)
        intent.putExtra("scene_name", scene.name)
        startActivity(intent)
    }

    private val testLifeScenesList = listOf(
        Scene(1, "购物", 5),
        Scene(2, "日常游玩", 4),
        Scene(3, "生活服务", 4),
        Scene(4, "情绪表达", 3),
        Scene(5, "居家", 2),
        Scene(6, "兴趣爱好", 2)
    )

    private val testSchoolScenesList = listOf(
        Scene(1, "问路", 5),
        Scene(2, "借书", 4),
        Scene(3, "课堂交流", 4),
        Scene(4, "校园活动", 3),
        Scene(5, "同学互动", 2),
        Scene(6, "体育运动", 2)
    )

}