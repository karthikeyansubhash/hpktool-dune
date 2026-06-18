package com.hp.workpath.pkgmgt.util.models.application

import com.hp.ext.types.application.I18nAssetLanguage
import com.hp.ext.types.application.I18nAssetString
import com.hp.ext.types.localization.LanguageTag

class I18nAssetLanguageModel {
    var languageTag: String = ""
    val strings = mutableListOf<I18nAssetStringModel>()

    fun getStringModelByStringId(stringId: String) :I18nAssetStringModel? {
        var stringModel: I18nAssetStringModel? = null
        strings.forEach {
            if (it.stringId == stringId) {
                stringModel = it
                return@forEach
            }
        }
        return stringModel
    }

    fun from(fromType: I18nAssetLanguage) {
        languageTag = fromType.languageTag.toString()
        fromType.strings.forEach {
            strings.add(I18nAssetStringModel().apply { from(it) })
        }
    }

    fun to(): I18nAssetLanguage {
        val i18nAssetLanguage = I18nAssetLanguage()
        i18nAssetLanguage.languageTag = LanguageTag(languageTag)
        i18nAssetLanguage.strings = mutableListOf<I18nAssetString>()
        for(stringModel in strings) {
            i18nAssetLanguage.strings.add(stringModel.to())
        }
        return i18nAssetLanguage
    }
}