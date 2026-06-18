package com.hp.workpath.pkgmgt.util.services.deviceManagement

import com.hp.ext.clients.oauth2.Token
import com.hp.ext.types.common.ServicesDiscoveryImpl
import com.hp.workpath.pkgmgt.util.models.connection.AccessTokenInfo
import com.hp.workpath.pkgmgt.util.models.connection.BoundDevice
import com.hp.workpath.pkgmgt.util.models.connection.DeviceModel
import java.util.concurrent.CompletableFuture

interface DeviceManagementService {
    fun getBoundDevice(): BoundDevice?
    fun bindDevice(networkAddress: String?): DeviceModel?
    fun unbindDevice(): DeviceModel?
    fun passwordGrant(userName: String?, password: String?): Token?
    fun getServicesDiscovery(): ServicesDiscoveryImpl?
    fun getTokens(): Iterable<AccessTokenInfo?>?
    fun getDeviceInformation(): CompletableFuture<Any?>?
    fun refreshGrant(refreshToken: String?): Token?
    fun authorizationCodeGrant(code: String?): Token?
    fun setUiContextAccessToken(token: Token?)
    fun setAuthContextAccessToken(token: Token?)
}
