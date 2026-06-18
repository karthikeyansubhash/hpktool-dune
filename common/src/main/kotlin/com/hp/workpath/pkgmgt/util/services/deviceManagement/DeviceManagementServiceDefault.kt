package com.hp.workpath.pkgmgt.util.services.deviceManagement

import com.fasterxml.jackson.core.JsonProcessingException
import com.hp.ext.clients.OXPdHttpRequestException
import com.hp.ext.clients.discovery.DiscoveryServiceClientImpl
import com.hp.ext.clients.oauth2.*
import com.hp.ext.types.common.ServicesDiscoveryImpl
import com.hp.workpath.pkgmgt.util.models.connection.AccessTokenInfo
import com.hp.workpath.pkgmgt.util.models.connection.BoundDevice
import com.hp.workpath.pkgmgt.util.models.connection.DeviceModel
import com.hp.workpath.pkgmgt.util.models.connection.TokenStatus
import com.hp.workpath.pkgmgt.util.services.BoundDeviceException
import com.hp.workpath.pkgmgt.util.utilities.*
import java.net.URI
import java.net.URISyntaxException
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class DeviceManagementServiceDefault(private val httpClientFactory: HttpClientFactory) : DeviceManagementService {
    private var boundDevice: BoundDevice? = null
    override fun getBoundDevice(): BoundDevice? {
        return boundDevice
    }

    override fun bindDevice(networkAddress: String?): DeviceModel? {
        boundDevice = BoundDevice(this).also {
            it.networkAddress = networkAddress
            it.boundStatus = "bound"
        }
        return boundDevice
    }

    override fun unbindDevice(): DeviceModel? {
        boundDevice = null
        return boundDevice
    }

    override fun passwordGrant(userName: String?, password: String?): Token? {
        val device = boundDevice ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        val httpClient = httpClientFactory.getHttpClient()

        return try {
            // Fetch the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute the PasswordGrant operation using the Token Resource Facade
            val token: Token = OAUTH2ServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .token()
                .passwordGrantAsync(PasswordGrantRequest().also { it.username = userName; it.password = password })
                .get()
            device.adminAccessTokenStatus = TokenStatus.Granted
            device.adminAccessToken = token
            token
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_LOGIN_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_LOGIN_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_LOGIN_FAIL, interruptedException)
        } catch (jsonProcessingException: JsonProcessingException) {
            throw RuntimeException(EXCEPTION_LOGIN_FAIL, jsonProcessingException)
        }
    }

    override fun getServicesDiscovery(): ServicesDiscoveryImpl? {
        val device = boundDevice ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Fetch the discovery tree
            DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_CONNECT_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_CONNECT_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_CONNECT_FAIL, interruptedException)
        }
    }

    override fun getTokens(): Iterable<AccessTokenInfo?> {
        return boundDevice?.tokens ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
    }

    override fun getDeviceInformation(): CompletableFuture<Any?>? {
        val httpClient = httpClientFactory.getHttpClient()
        val httpRequest: HttpRequest
        return try {
            httpRequest = HttpRequest.newBuilder()
                .uri(
                    URI(
                        "https://" + (boundDevice?.networkAddress
                            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)) + "/cdm/system/v1/identity"
                    )
                )
                .method("GET", BodyPublishers.noBody()).build()
            httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply { obj: HttpResponse<String?> -> obj.body() }
        } catch (exception: URISyntaxException) {
            throw RuntimeException(EXCEPTION_INVALID_URI, exception)
        }
    }

    override fun refreshGrant(refreshToken: String?): Token? {
        val device = boundDevice ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Fetch the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute the RefreshTokenGrantRequest operation using the Token Resource Facade
            val token: Token = OAUTH2ServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .token()
                .refreshTokenGrantAsync(RefreshTokenGrantRequest().also { it.refreshToken = refreshToken })
                .get()
            device.solutionAccessTokenTimeGranted = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            device.solutionAccessToken = token
            device.solutionAccessTokenStatus = TokenStatus.Granted
            token
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_REFRESH_GRANT_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_REFRESH_GRANT_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_REFRESH_GRANT_FAIL, interruptedException)
        } catch (jsonProcessingException: JsonProcessingException) {
            throw RuntimeException(EXCEPTION_REFRESH_GRANT_FAIL, jsonProcessingException)
        }
    }

    override fun authorizationCodeGrant(code: String?): Token? {
        val device = boundDevice ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Fetch the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute the AuthorizationCodeGrant operation using the Token Resource Facade
            val token = OAUTH2ServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .token()
                .authorizationCodeGrantAsync(AuthorizationCodeGrantRequest().also { it.code = code })
                .get()
            device.solutionAccessTokenTimeGranted = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            device.solutionAccessToken = token
            device.solutionAccessTokenStatus = TokenStatus.Granted
            token
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_AUTH_CODE_GRANT_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_AUTH_CODE_GRANT_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_AUTH_CODE_GRANT_FAIL, interruptedException)
        } catch (jsonProcessingException: JsonProcessingException) {
            throw RuntimeException(EXCEPTION_AUTH_CODE_GRANT_FAIL, jsonProcessingException)
        }
    }

    override fun setUiContextAccessToken(token: Token?) {
        if (boundDevice != null) {
            boundDevice!!.uiContextAccessToken = token
            boundDevice!!.uiContextAccessTokenStatus = if (token != null) TokenStatus.Granted else TokenStatus.None
        }
    }

    override fun setAuthContextAccessToken(token: Token?) {
        if (boundDevice != null) {
            boundDevice!!.authenticationContextAccessToken = token
            boundDevice!!.authenticationContextAccessTokenStatus =
                if (token != null) TokenStatus.Granted else TokenStatus.None
        }
    }
}
