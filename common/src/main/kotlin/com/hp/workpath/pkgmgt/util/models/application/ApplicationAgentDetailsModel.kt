package com.hp.workpath.pkgmgt.util.models.application

import com.hp.workpath.pkgmgt.util.models.LocalizedStringReferenceModel

enum class ApplicationCategory {
    Standard,
    HomeScreen
}

class ApplicationAgentDetailsModel {
    var applicationId: String = ""
    var name: String = ""
    var platform: String = ""
    var category: ApplicationCategory = ApplicationCategory.Standard
    var isTitleFromUser: Boolean = false
    val title = LocalizedStringReferenceModel()
    var isDescriptionFromUser: Boolean = false
    val description = LocalizedStringReferenceModel()
    var isIconFromUser: Boolean = false
    var icon: ApplicationIconDetailModel = ApplicationIconDetailModel()
    var iconSet: MutableList<ApplicationIconDetailModel> = mutableListOf()
    var setAsDefault: Boolean = false
}