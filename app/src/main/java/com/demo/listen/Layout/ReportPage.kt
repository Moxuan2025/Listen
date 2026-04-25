package com.demo.listen.Layout

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.R
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ReportPage : AppCompatActivity() {

    private lateinit var radarChart: RadarChart
    private lateinit var score: TextView
    private lateinit var btBack: TextView
    private lateinit var tvListening: TextView
    private lateinit var tvListeningLevel: TextView
    private lateinit var tvExpression: TextView
    private lateinit var tvComprehension: TextView
    private lateinit var tvOverallDetail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_page)

        mapWidget()
        
        // 1. 获取传递的分数
        val listening = intent.getFloatExtra("listening_score", 0f)
        val listeningLevel = intent.getFloatExtra("listening_level_score", 0f)
        val expression = intent.getFloatExtra("expression_score", 0f)
        val comprehension = intent.getFloatExtra("comprehension_score", 0f)
        val overall = intent.getFloatExtra("overall_score", 0f)

        // 更新综合分数显示
        score.text = "综合评分: ${overall.toInt()}"
        
        // 更新详细维度分数
        tvListening.text = "听力能力: ${listening.toInt()} 分"
        tvListeningLevel.text = "听力等级: ${listeningLevel.toInt()} 分"
        tvExpression.text = "表达能力: ${expression.toInt()} 分"
        tvComprehension.text = "理解能力: ${comprehension.toInt()} 分"
        tvOverallDetail.text = "综合评分: ${overall.toInt()} 分"

        // 2. 配置雷达图
        setupRadarChart(listening, listeningLevel, expression, comprehension, overall)

        handleClick()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mapWidget() {
        radarChart = findViewById(R.id.report_radar_chart)
        score = findViewById(R.id.report_score)
        btBack = findViewById(R.id.report_back)
        tvListening = findViewById(R.id.report_listening)
        tvListeningLevel = findViewById(R.id.report_listening_level)
        tvExpression = findViewById(R.id.report_expression)
        tvComprehension = findViewById(R.id.report_comprehension)
        tvOverallDetail = findViewById(R.id.report_overall_detail)
    }

    private fun setupRadarChart(
        listening: Float,
        listeningLevel: Float,
        expression: Float,
        comprehension: Float,
        overall: Float
    ) {
        // 背景透明
        radarChart.setBackgroundColor(Color.TRANSPARENT)

        // 禁用描述文字
        radarChart.description.isEnabled = false

        // 启用旋转、设置动画
        radarChart.isRotationEnabled = true
        radarChart.animateXY(1000, 1000)

        // X轴（顶点标签）
        val labels = listOf("听力能力", "听力等级", "表达能力", "理解能力", "综合能力")
        val xAxis = radarChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 14f
        xAxis.setDrawGridLines(false)
        xAxis.setCenterAxisLabels(true) // 将标签显示在网格线的中心位置

        // Y轴（数值）
        val yAxis = radarChart.yAxis
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 100f
        yAxis.setDrawLabels(true)
        yAxis.textColor = Color.BLACK
        yAxis.setLabelCount(5, true) // 显示 0,25,50,75,100 刻度
        yAxis.setDrawGridLines(true)

        // 图例
        val legend = radarChart.legend
        legend.isEnabled = false // 单数据集时隐藏图例

        // 数据
        val entries = listOf(
            RadarEntry(listening),
            RadarEntry(listeningLevel),
            RadarEntry(expression),
            RadarEntry(comprehension),
            RadarEntry(overall)
        )

        val dataSet = RadarDataSet(entries, "")
        dataSet.color = Color.rgb(103, 110, 129) // 线条颜色
        dataSet.fillColor = Color.argb(100, 103, 110, 129) // 半透明填充
        dataSet.setDrawFilled(true)
        dataSet.lineWidth = 2f
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.isDrawHighlightCircleEnabled = true

        val radarData = RadarData(dataSet)
        radarChart.data = radarData
        radarChart.invalidate()
    }

    private fun handleClick() {
        btBack.setOnClickListener {
            finish()
        }
    }
}