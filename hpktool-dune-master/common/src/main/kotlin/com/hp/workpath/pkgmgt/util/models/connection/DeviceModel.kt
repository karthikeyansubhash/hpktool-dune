package com.hp.workpath.pkgmgt.util.models.connection

import java.util.*

open class DeviceModel {
    var networkAddress: String? = null
    var boundStatus: String? = null
    var adminAccessTokenStatus: TokenStatus = TokenStatus.None
    var solutionAccessTokenStatus: TokenStatus = TokenStatus.None
    var solutionAccessTokenTimeGranted: Calendar? = null
    var uiContextAccessTokenStatus: TokenStatus = TokenStatus.None
    var authenticationContextAccessTokenStatus: TokenStatus = TokenStatus.None
}
