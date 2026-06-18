package com.hp.workpath.pkgmgt.util.models.application

import com.hp.ext.types.application.I18nAssetInlineContent
import com.hp.ext.types.application.I18nAssetLanguage

class I18nAssetInlineContentModel {
    val languages = mutableListOf<I18nAssetLanguageModel>()


    fun getLanguageModelByLanguageTag(languageTag: String) : I18nAssetLanguageModel? {
        var languageModel: I18nAssetLanguageModel? = null
        languages.forEach {
            if (it.languageTag == languageTag) {
                languageModel = it
                return@forEach
            }
        }
        return languageModel
    }

    fun from(fromType: I18nAssetInlineContent) {
        fromType.languages.forEach {
            languages.add(I18nAssetLanguageModel().apply { from(it) })
        }
    }

    fun to(): I18nAssetInlineContent {
        val i18nAssetInlineContent = I18nAssetInlineContent()
        i18nAssetInlineContent.languages = mutableListOf<I18nAssetLanguage>()
        for(languageModel in languages){
            i18nAssetInlineContent.languages.add(languageModel.to())
        }
        return i18nAssetInlineContent
    }
}