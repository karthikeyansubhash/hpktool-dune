package com.hp.workpath.pkgmgt.util.models.application

import com.hp.ext.service.application.I18nAssetRegistrationRecord
import com.hp.ext.types.agent.AssetId
import com.hp.ext.types.application.I18nAssetInnerContent

class I18nAssetModel {
    var i18nAssetId: String = ""
    val inlineAsset = I18nAssetInlineContentModel()

    fun from(fromType: I18nAssetRegistrationRecord) {
        i18nAssetId = fromType.assetId.toString()
        inlineAsset.from(fromType.assetContent.i18nAssetInlineContent)
    }

    fun to(): I18nAssetRegistrationRecord {
        val i18nAssetRegistrationRecord = I18nAssetRegistrationRecord()
        i18nAssetRegistrationRecord.assetId = AssetId.createAssetId(i18nAssetId)
        /**
         * workpath use inline content only.
         */
        i18nAssetRegistrationRecord.assetContent = I18nAssetInnerContent().apply {
            i18nAssetInlineContent = inlineAsset.to()
        }
        return i18nAssetRegistrationRecord
    }
}