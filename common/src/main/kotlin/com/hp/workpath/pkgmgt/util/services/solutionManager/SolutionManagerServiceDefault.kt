package com.hp.workpath.pkgmgt.util.services.solutionManager

import com.fasterxml.jackson.core.JsonProcessingException
import com.hp.ext.clients.OXPdHttpRequestException
import com.hp.ext.clients.discovery.DiscoveryServiceClientImpl
import com.hp.ext.clients.solutionmanager.SolutionManagerServiceClientImpl
import com.hp.ext.service.solutionManager.*
import com.hp.ext.types.base.DeleteContent
import com.hp.workpath.pkgmgt.util.models.connection.AccessTokenType
import com.hp.workpath.pkgmgt.util.models.connection.BoundDevice
import com.hp.workpath.pkgmgt.util.services.BoundDeviceException
import com.hp.workpath.pkgmgt.util.services.deviceManagement.DeviceManagementService
import com.hp.workpath.pkgmgt.util.utilities.*
import java.io.IOException
import java.io.InputStream
import java.net.URISyntaxException
import java.net.http.HttpClient
import java.util.*
import java.util.concurrent.ExecutionException

class SolutionManagerServiceDefault(
    private val deviceManagementService: DeviceManagementService,
    private val httpClientFactory: HttpClientFactory,
) : SolutionManagerService {
    override val capabilities: Capabilities
        get() {
            val device: BoundDevice = deviceManagementService.getBoundDevice()
                ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
            val httpClient: HttpClient = httpClientFactory.getHttpClient()
            return try {
                // Get the discovery tree
                val discoveryTree =
                    DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
                // Construct the SolutionManagerServiceClient Execute the GET operation
                SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                    .capabilities().async.get()
            } catch (exception: URISyntaxException) {
                throw BoundDeviceException(EXCEPTION_INVALID_URI)
            } catch (executionException: ExecutionException) {
                if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                    throw RuntimeException(
                        "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_CAPABILITIES_GET_FAIL",
                        executionException
                    )
                }
                throw RuntimeException(EXCEPTION_CAPABILITIES_GET_FAIL, executionException)
            } catch (interruptedException: InterruptedException) {
                throw RuntimeException(EXCEPTION_CAPABILITIES_GET_FAIL, interruptedException)
            }
        }

    override fun getSolution(solutionId: String?): Solution {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires Administrator or Solution grant
        val accessToken: String = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient: HttpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Construct the SolutionManagerServiceClient Execute the GET operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).getAsync(accessToken).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_SOLUTION_GET_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_SOLUTION_GET_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_SOLUTION_GET_FAIL, interruptedException)
        }
    }

    override fun enumerateSolutions(includeMembers: Boolean, contentFilter: String?): Solutions {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires Administrator grant
        val accessToken: String = device.getToken(AccessTokenType.ADMIN)
        val httpClient: HttpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Encode the appropriate pieces of the query parameters.
            // This example is only demonstrating the includeMembers and contentFilter queryParams.
            var queryParams =
                "includeMembers=" + includeMembers.toString().lowercase(Locale.getDefault())
            if (contentFilter != null) {
                queryParams = "$queryParams&contentFilter=$contentFilter"
            }
            // Execute the GET operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getAsync(accessToken, queryParams).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_SOLUTIONS_GET_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_SOLUTIONS_GET_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_SOLUTIONS_GET_FAIL, interruptedException)
        }
    }

    override fun getSolutionContext(solutionId: String?): Context {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires Administrator or Solution grant
        val accessToken: String = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient: HttpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute the GET operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).context().getAsync(accessToken).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_CONTEXT_GET_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_CONTEXT_GET_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_CONTEXT_GET_FAIL, interruptedException)
        }
    }

    override fun modifySolutionContext(solutionId: String?, contextModify: Context_Modify?): Context {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires Administrator or Solution grant
        val accessToken: String = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient: HttpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute the modify operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).context().modifyAsync(accessToken, contextModify).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_CONTEXT_MODIFY_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_CONTEXT_MODIFY_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_CONTEXT_MODIFY_FAIL, interruptedException)
        } catch (jsonProcessingException: JsonProcessingException) {
            throw RuntimeException(EXCEPTION_CONTEXT_MODIFY_FAIL, jsonProcessingException)
        }
    }

    override fun replaceSolutionContext(solutionId: String?, contextReplace: Context_Replace?): Context {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires Administrator or Solution grant
        val accessToken: String = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient: HttpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute the replace operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).context().replaceAsync(accessToken, contextReplace).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_CONTEXT_REPLACE_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_CONTEXT_REPLACE_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_CONTEXT_REPLACE_FAIL, interruptedException)
        } catch (jsonProcessingException: JsonProcessingException) {
            throw RuntimeException(EXCEPTION_CONTEXT_REPLACE_FAIL, jsonProcessingException)
        }
    }

    override fun reissueInstallCode(solutionId: String?): Solution_ReissueInstallCode {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        val accessToken: String = device.getToken(AccessTokenType.ADMIN)
        val httpClient: HttpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Construct the SolutionManagerServiceClient
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).reissueInstallCode().executeAsync(accessToken).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_INSTALL_CODE_REISSUE_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_INSTALL_CODE_REISSUE_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_INSTALL_CODE_REISSUE_FAIL, interruptedException)
        } catch (ioException: IOException) {
            throw RuntimeException(EXCEPTION_INSTALL_CODE_REISSUE_FAIL, ioException)
        }
    }

    override val installer: Installer
        get() {
            val device: BoundDevice = deviceManagementService.getBoundDevice()
                ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
            // This operation requires Administrator or Solution grant
            val accessToken: String = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
            val httpClient: HttpClient = httpClientFactory.getHttpClient()
            return try {
                // Get the discovery tree
                val discoveryTree =
                    DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
                // Construct the SolutionManagerServiceClient
                SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                    .installer().getAsync(accessToken).get()
            } catch (exception: URISyntaxException) {
                throw BoundDeviceException(EXCEPTION_INVALID_URI)
            } catch (executionException: ExecutionException) {
                if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                    throw RuntimeException(
                        "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_INSTALLER_GET_FAIL",
                        executionException
                    )
                }
                throw RuntimeException(EXCEPTION_INSTALLER_GET_FAIL, executionException)
            } catch (interruptedException: InterruptedException) {
                throw RuntimeException(EXCEPTION_INSTALLER_GET_FAIL, interruptedException)
            } catch (exception: Exception) {
                throw RuntimeException(EXCEPTION_INSTALLER_GET_FAIL, exception)
            }
        }

    override fun installSolution(
        installRequest: InstallSolutionRequest?,
        solutionBundle: InputStream?,
        solutionBundleFilename: String?,
        context: Context_Replace?,
    ): Installer_InstallSolution {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires Administrator or Solution grant
        val accessToken: String = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient: HttpClient = httpClientFactory.getHttpClient()
        return try {
            // Fetch the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute install operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .installer().installSolution()
                .executeAsync(accessToken, installRequest, solutionBundle, solutionBundleFilename, context, null).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_INSTALL_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_INSTALL_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_INSTALL_FAIL, interruptedException)
        } catch (exception: Exception) {
            throw RuntimeException(EXCEPTION_INSTALL_FAIL, exception)
        }
    }

    override fun uninstallSolution(request: UninstallSolutionRequest?): Installer_UninstallSolution {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires Administrator or Solution grant
        val accessToken: String = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient: HttpClient = httpClientFactory.getHttpClient()
        return try {
            // Fetch the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute uninstall operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .installer().uninstallSolution().executeAsync(accessToken, request).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_UNINSTALL_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_UNINSTALL_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_UNINSTALL_FAIL, interruptedException)
        } catch (exception: Exception) {
            throw RuntimeException(EXCEPTION_UNINSTALL_FAIL, exception)
        }
    }

    override fun enumerateInstallerOperations(includeMembers: Boolean, contentFilter: String?): InstallerOperations {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires Administrator grant
        val accessToken: String = device.getToken(AccessTokenType.ADMIN)
        val httpClient: HttpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Encode the appropriate pieces of the query parameters.
            // This example is only demonstrating the includeMembers and contentFilter queryParams.
            var queryParams = "includeMembers=$includeMembers"
            if (contentFilter != null) {
                queryParams = "$queryParams&contentFilter=$contentFilter"
            }
            // Execute the GET operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .installer().installerOperations().getAsync(accessToken, queryParams).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_INSTALLER_OPERATIONS_GET_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_INSTALLER_OPERATIONS_GET_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_INSTALLER_OPERATIONS_GET_FAIL, interruptedException)
        }
    }

    override fun getInstallerOperation(operationId: String?): InstallerOperation {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires Administrator grant
        val accessToken: String = device.getToken(AccessTokenType.ADMIN)
        val httpClient: HttpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute the GET operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .installer().installerOperations().getMember(operationId).getAsync(accessToken).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_INSTALLER_OPERATION_GET_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_INSTALLER_OPERATION_GET_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_INSTALLER_OPERATION_GET_FAIL, interruptedException)
        }
    }

    override fun importCertificateAuthority(
        solutionId: String?,
        importRequest: CertificateAuthoritiesImportRequest?,
        certificate: InputStream?,
        certificateFilename: String?,
    ): CertificateAuthorities_Import? {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires Administrator grant
        val accessToken: String = device.getToken(AccessTokenType.ADMIN, AccessTokenType.SOLUTION)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute the POST operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).certificateAuthorities().importCertificateAuthorities()
                .executeAsync(accessToken, importRequest, certificate, certificateFilename).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_OPERATION_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, executionException)
        } catch (exception: Exception) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, exception)
        }
    }

    override fun enumerateCertificateAuthorities(
        solutionId: String?,
        includeMembers: Boolean,
    ): CertificateAuthorities? {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires administrator or solution-owner
        val accessToken: String = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Fetch the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Construct the SolutionManagerServiceClient
            var queryParams: String? = null
            if (includeMembers) {
                queryParams = "includeMembers=" + includeMembers.toString().lowercase(Locale.getDefault())
            }
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).certificateAuthorities()
                .getAsync(accessToken, queryParams).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_OPERATION_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, interruptedException)
        } catch (exception: Exception) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, exception)
        }
    }

    override fun exportCertificateAuthorities(solutionId: String?): Map<String, Any>? {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires administrator or solution-owner
        val accessToken = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Fetch the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Construct the SolutionManagerServiceClient
            val (key, value) = SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).certificateAuthorities()
                .exportCertificateAuthorities().executeAsync(accessToken, null).get()
            val result: MutableMap<String, Any> = HashMap()
            result[RESPONSE_KEY_STATUS] = key
            result[RESPONSE_KEY_DATA] = value
            result
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_OPERATION_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, interruptedException)
        } catch (exception: Exception) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, exception)
        }
    }

    override fun getCertificateAuthority(solutionId: String?, certificateId: String?): CertificateAuthority? {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires administrator or solution-owner
        val accessToken = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Fetch the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Construct the SolutionManagerServiceClient
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).certificateAuthorities()
                .getMember(certificateId).getAsync(accessToken, "").get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_OPERATION_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, interruptedException)
        } catch (exception: Exception) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, exception)
        }
    }

    override fun exportCertificateAuthority(solutionId: String?, certificateId: String?): Map<String, Any>? {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires administrator or solution-owner
        val accessToken = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Fetch the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Construct the SolutionManagerServiceClient
            val (key, value) = SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).certificateAuthorities()
                .getMember(certificateId).export().executeAsync(accessToken, null).get()
            val result: MutableMap<String, Any> = HashMap()
            result[RESPONSE_KEY_STATUS] = key
            result[RESPONSE_KEY_DATA] = value
            result
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_OPERATION_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, interruptedException)
        } catch (exception: Exception) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, exception)
        }
    }

    override fun deleteCertificateAuthority(solutionId: String?, certificateId: String?): DeleteContent? {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires administrator or solution-owner
        val accessToken = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Fetch the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Construct the SolutionManagerServiceClient
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).certificateAuthorities()
                .getMember(certificateId).deleteAsync(accessToken, accessToken).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_OPERATION_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, interruptedException)
        } catch (exception: Exception) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, exception)
        }
    }

/*    override fun enumerateConfigurations(solutionId: String?, includeMembers: Boolean): Configurations? {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires administrator or solution-owner
        val accessToken = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Construct the SolutionManagerServiceClient
            var queryParams: String? = null
            if (includeMembers) {
                queryParams = "includeMembers=" + includeMembers.toString().lowercase(Locale.getDefault())
            }
            // Execute the GET operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).configurations().getAsync(accessToken, queryParams).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_OPERATION_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, interruptedException)
        }
    }*/

/*    override fun createConfiguration(
        solutionId: String?,
        createRequest: Configuration_Create?,
        data: InputStream?,
    ): Configuration? {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires administrator or solution-owner
        val accessToken = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute the POST operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).configurations()
                .createAsync(accessToken, createRequest, data).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_OPERATION_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, interruptedException)
        } catch (ioException: IOException) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, ioException)
        }
    }*/

    override fun getConfiguration(solutionId: String?, key: String?): Configuration? {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires administrator or solution-owner
        val accessToken = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute the GET operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).configuration().getAsync(accessToken)
                .get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_OPERATION_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, interruptedException)
        }
    }

/*    override fun deleteConfiguration(solutionId: String?, key: String?): DeleteContent? {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires administrator or solution-owner
        val accessToken = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute the DELETE operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).configuration().modifyAsync(accessToken)
                .get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_OPERATION_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_OPERATION_FAIL, interruptedException)
        }
    }*/

    override fun getConfigurationData(solutionId: String?, key: String?): Map<String, Any>? {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires Administrator or Solution grant
        val accessToken = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute the GET operation
            val (key1, value) = SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).configuration().dataResource()
                .getAsync(accessToken).get()
            // Data Type casting required for front end side
            val response: MutableMap<String, Any> = HashMap()
            response[RESPONSE_KEY_STATUS] = key1
            response[RESPONSE_KEY_DATA] = value
            response
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_CONFIG_GET_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_CONFIG_GET_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_CONFIG_GET_FAIL, interruptedException)
        }
    }

    override fun replaceConfigurationData(solutionId: String?, key: String?, data: InputStream?, mime: String?): Data? {
        val device: BoundDevice = deviceManagementService.getBoundDevice()
            ?: throw BoundDeviceException(EXCEPTION_NO_BOUND_DEVICE)
        // This operation requires administrator or solution-owner
        val accessToken = device.getToken(AccessTokenType.SOLUTION, AccessTokenType.ADMIN)
        val httpClient = httpClientFactory.getHttpClient()
        return try {
            // Get the discovery tree
            val discoveryTree =
                DiscoveryServiceClientImpl(httpClient, device.networkAddress).servicesDiscovery().async.get()
            // Execute the PUT operation
            SolutionManagerServiceClientImpl(httpClient, device.networkAddress, discoveryTree)
                .solutions().getMember(solutionId).configuration().dataResource()
                .replaceAsync(accessToken, data, mime).get()
        } catch (exception: URISyntaxException) {
            throw BoundDeviceException(EXCEPTION_INVALID_URI)
        } catch (executionException: ExecutionException) {
            if (executionException.cause!!.javaClass == OXPdHttpRequestException::class.java) {
                throw RuntimeException(
                    "${(executionException.cause as OXPdHttpRequestException).statusCode} - $EXCEPTION_CONFIG_UPDATE_FAIL",
                    executionException
                )
            }
            throw RuntimeException(EXCEPTION_CONFIG_UPDATE_FAIL, executionException)
        } catch (interruptedException: InterruptedException) {
            throw RuntimeException(EXCEPTION_CONFIG_UPDATE_FAIL, interruptedException)
        } catch (ioException: IOException) {
            throw RuntimeException(EXCEPTION_CONFIG_UPDATE_FAIL, ioException)
        }
    }
}
