package com.hp.workpath.pkgmgt.util.models

import com.hp.ext.types.localization.LocalizationStringId
import com.hp.ext.types.localization.LocalizedStringReference
import java.util.*

class LocalizedStringReferenceModel {
    var i18nAssetId: String = ""
    var stringId: String = ""

    fun from(fromType: LocalizedStringReference) {
        i18nAssetId = fromType.i18nAssetId.toString()
        stringId = fromType.stringId.toString()
    }

    fun to(): LocalizedStringReference {
        val localizedStringReference = LocalizedStringReference()
        localizedStringReference.i18nAssetId = UUID.fromString(i18nAssetId)
        localizedStringReference.stringId = LocalizationStringId(stringId)
        return localizedStringReference
    }
}