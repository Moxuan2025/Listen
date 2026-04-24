package com.demo.listen.Layout.DataType

import kotlin.random.Random

// 基类（抽象类）
abstract class Problem {
    protected lateinit var _answer: String

    abstract fun getAnswer(): String

    // 默认返回空列表，避免 null 问题
    open fun optionList(): List<String> = listOf("<None")

    open fun getProblem(): String = "<None>"
}

// 简单问题（只需要答案）
class SimpleProblem(answer: String) : Problem() {
    init {
        _answer = answer
    }

    override fun getAnswer(): String = _answer
}

// 二选一问题
class BinaryChoice(answer: String, private val _disturb: String) : Problem() {
    init {
        _answer = answer
    }

    override fun getAnswer(): String = _answer

    override fun optionList(): List<String> = listOf(_answer, _disturb)
}

// 选词补全句子
class CompleteSentence(
    private val _problem: String,
    answer: String,
    private val _disturb: String
) : Problem() {
    init {
        _answer = answer
    }

    override fun getAnswer(): String = _answer

    override fun getProblem(): String = _problem

    override fun optionList(): List<String> = listOf(_answer, _disturb)
}

// 选字组词
class WordFormation(answer: String, private val _choices: List<String>) : Problem() {
    init {
        _answer = answer
    }

    override fun getAnswer(): String = _answer

    override fun optionList(): List<String> = _choices
}