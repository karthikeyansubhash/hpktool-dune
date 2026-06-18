package com.hp.workpath.pkgmgt.util.models

class BaseModel {
    private val dataMap = mutableMapOf<String, Any>()

    fun add(key: String, value: Any) {
        dataMap[key] = value
    }

    fun get(key: String): Any? {
        return dataMap[key]
    }

    fun clear() {
        dataMap.clear()
    }
}