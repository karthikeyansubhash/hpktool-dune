package com.hp.workpath.pkgmgt.util.models.hpk

import com.hp.workpath.pkgmgt.lib.LocalizedString

class WebServiceInfo {
    var includeWebServiceInfo: Boolean = false
    var uuid: String = ""
    val titles = arrayListOf<LocalizedString>()
    val descriptions = arrayListOf<LocalizedString>()
    val webServiceEndPoints = arrayListOf<WebServiceEndPoint>()
}