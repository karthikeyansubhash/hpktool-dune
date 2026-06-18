package com.hp.workpath.pkgmgt.util.models.application

import com.hp.ext.types.application.I18nAssetString

class I18nAssetStringModel {
    var stringId: String = ""
    var value: String = ""

    fun from(fromType: I18nAssetString) {
        stringId = fromType.stringId
        value = fromType.value
    }

    fun to(): I18nAssetString {
        val i18nAssetString = I18nAssetString()
        i18nAssetString.stringId = stringId
        i18nAssetString.value = value
        return i18nAssetString
    }
}