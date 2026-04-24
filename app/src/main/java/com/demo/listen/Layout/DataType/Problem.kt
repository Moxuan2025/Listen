package com.demo.listen.Layout.DataType

import kotlin.random.Random

class BinaryChoice {        // 二选一
    private var _answer: String = ""
    private var _disturb: String = ""

    constructor(answer: String, disturb: String) {
        _answer = answer
        _disturb = disturb
    }

    fun optionList(): List<String> {
        return listOf(_answer, _disturb)
    }

    fun getAnswer(): String {
        return _answer
    }
}

class CompleteSentence {        // 选词补全句子
    private var _problem: String = ""
    private var _answer: String = ""
    private var _disturb: String = ""

    constructor(problem: String, answer: String, disturb: String) {
        _problem = problem
        _answer = answer
        _disturb = disturb
    }

    fun optionList(): List<String> {
        return listOf(_answer, _disturb)
    }

    fun getProblem(): String {
        return _problem
    }

    fun getAnswer(): String {
        return _answer
    }
}

class WordFormation {       // 选字组词
    private var _answer: String = ""
    private var _choices: List<String> = emptyList()

    constructor(answer: String, choices: List<String>) {
        _answer = answer
        _choices = choices
    }

    fun optionList(): List<String> {
        return _choices
    }

    fun getAnswer(): String {
        return _answer
    }
}
