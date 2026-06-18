package com.hp.workpath.pkgmgt.util.models.connection

import com.hp.ext.clients.oauth2.Token
import com.hp.workpath.pkgmgt.util.services.deviceManagement.DeviceManagementService
import java.util.*

class BoundDevice(private val deviceManagementService: DeviceManagementService) : DeviceModel() {
    var adminAccessToken: Token? = null
    var solutionAccessToken: Token? = null
    var uiContextAccessToken: Token? = null
    var authenticationContextAccessToken: Token? = null

    val tokens: Iterable<AccessTokenInfo>
        get() {
            return listOf(
                AccessTokenInfo(AccessTokenType.ADMIN, adminAccessToken, adminAccessTokenStatus),
                AccessTokenInfo(AccessTokenType.SOLUTION, solutionAccessToken, solutionAccessTokenStatus),
                AccessTokenInfo(AccessTokenType.UI_CONTEXT, uiContextAccessToken, uiContextAccessTokenStatus),
                AccessTokenInfo(
                    AccessTokenType.AUTHENTICATION_CONTEXT,
                    authenticationContextAccessToken,
                    authenticationContextAccessTokenStatus
                )
            )
        }

    fun getToken(vararg accessTokenTypes: AccessTokenType?): String {
        val grantedTokens = mutableListOf<AccessTokenInfo>().apply {
            tokens.forEach {
                if (it.status == TokenStatus.Granted)
                    add(it)
            }
        }
        for (accessTokenType in accessTokenTypes) {
            for (accessTokenInfo in grantedTokens) {
                if (accessTokenInfo.status == TokenStatus.Granted) {
                    if (accessTokenInfo.type == accessTokenType) {
                        return when (accessTokenType) {
                            AccessTokenType.UI_CONTEXT -> accessTokenInfo.token!!.accessToken
                            AccessTokenType.AUTHENTICATION_CONTEXT -> accessTokenInfo.token!!.accessToken
                            AccessTokenType.SOLUTION -> {
                                refreshTokenIfNeeded()
                                accessTokenInfo.token!!.accessToken
                            }

                            AccessTokenType.ADMIN -> accessTokenInfo.token!!.accessToken
                        }
                    }
                }
            }
        }
        return ""
    }

    private fun refreshTokenIfNeeded() {
        val expiryTime: Calendar = solutionAccessTokenTimeGranted!!
        expiryTime.add(Calendar.SECOND, solutionAccessToken!!.expiresIn)
        val currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        if (currentTime.compareTo(expiryTime) == 1) {
            deviceManagementService.refreshGrant(solutionAccessToken!!.refreshToken)
        }
    }
}
