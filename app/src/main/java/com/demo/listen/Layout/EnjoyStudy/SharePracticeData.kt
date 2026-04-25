package com.demo.listen.Layout.EnjoyStudy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharePracticeData: ViewModel() {

    // 发声动作内容
    private val _action = MutableLiveData<List<String>>(listOf())
    val action: LiveData<List<String>> = _action
    fun changeAction(input: List<String>) {
        _action.value = input
    }

    // 当前进度=========必须指定
    private val _index = MutableLiveData<Int>(0)
    val index: LiveData<Int> = _index
    fun changeIndex(input: Int) {
        _index.value = input
    }

    // 下一页
    private val _nextPage = MutableLiveData<String>("")
    val nextPage: LiveData<String> = _nextPage
    fun setNext(input: String) {
        _nextPage.value = input
    }

    // 学习/练习的对象，比如：bā、拔河
    private val _target = MutableLiveData<List<String>>(listOf())
    val target: LiveData<List<String>> = _target
    fun changeTarget(input: List<String>) {
        _target.value = input
    }

    // 类型：拼音 pinyin、词汇 word、词组 phrase、句子 sentence===========必须指定
    private val _type = MutableLiveData<String>("pinyin")
    val type: LiveData<String> = _type
    fun setType(input: String) {
        _type.value = input
    }


    // 一个 学习/练习 对象: 包含拼音和发声动作，可能包含 词汇/词组/句子
    private val _wordPinYin = MutableLiveData<List<WordPinYin>>(listOf())
    val wordPinYin: LiveData<List<WordPinYin>> = _wordPinYin
    fun setWordPinYin(input: List<WordPinYin>) {
        _wordPinYin.value = input
    }
}

data class WordPinYin(
    val pinyin: String = "",                // 词汇对应拼音
    val action: String = "",                // 发声动作
    val word: String = ""                   // 词汇
){
    constructor(pinyin: String, word: String) : this(
        pinyin, " ", word
    )
}