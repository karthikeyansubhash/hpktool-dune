package com.hp.workpath.pkgmgt.util.cli.utilities

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.hp.workpath.pkgmgt.lib.LocalizedString
import com.hp.workpath.pkgmgt.util.utilities.*
import java.io.File
import java.util.regex.Pattern

fun getIconMapFromJsonString(jsonString: String): Map<String, File> {
    val stringAnyMap = ObjectMapper().readValue(sanitizeJsonString(jsonString), object : TypeReference<Map<String, Any>>() {})
    val stringFileMap = mutableMapOf<String, File>()
    stringAnyMap.forEach { (key, value) ->
        val file = File(value.toString())
        if (!checkKeyFormat(key) || !checkIconFileExtension(file)) {
            throw IllegalArgumentException("Not supported Image type: $key, $value")
        }
        stringFileMap[key] = file
    }
    return stringFileMap
}

private fun checkKeyFormat(key: String): Boolean {
    val pattern = Pattern
        .compile("(?i)^[0-9]+x[0-9]+$")
    return pattern.matcher(key).matches()
}

fun getLocalizedArrayFromJsonString(jsonString: String): ArrayList<LocalizedString> {
    val localizedString = ArrayList<LocalizedString>()
    getMapFromJsonString(jsonString).forEach { (key, set) ->
        localizedString.add(LocalizedString().apply {
            code = key
            value = set.toString()
        })
    }
    return localizedString
}

fun getMapFromJsonString(jsonString: String): Map<String, Any> {
    val stringAnyMap = ObjectMapper().readValue(sanitizeJsonString(jsonString), object : TypeReference<Map<String, Any>>() {})
    stringAnyMap.forEach { (tag, _) ->
        if (!getLocalizations().contains(tag)) {
            throw IllegalArgumentException("Not supported language tag: $tag")
        }
    }
    return stringAnyMap
}