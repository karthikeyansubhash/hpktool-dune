package com.hp.workpath.pkgmgt.util.services.solutionManager

import com.hp.ext.service.solutionManager.*
import com.hp.ext.types.base.DeleteContent
import java.io.InputStream

// ref SolutionManagerService.java on oxpd2-examples
interface SolutionManagerService {
    val capabilities: Capabilities

    fun enumerateSolutions(includeMembers: Boolean, contentFilter: String?): Solutions?
    fun getSolution(solutionId: String?): Solution?
    fun getSolutionContext(solutionId: String?): Context?
    fun modifySolutionContext(solutionId: String?, contextModify: Context_Modify?): Context?
    fun replaceSolutionContext(solutionId: String?, contextReplace: Context_Replace?): Context?
    fun reissueInstallCode(solutionId: String?): Solution_ReissueInstallCode?
    val installer: Installer?
    fun installSolution(
        installRequest: InstallSolutionRequest?,
        solutionBundle: InputStream?,
        solutionBundleFilename: String?,
        context: Context_Replace?,
    ): Installer_InstallSolution?

    fun uninstallSolution(request: UninstallSolutionRequest?): Installer_UninstallSolution?
    fun enumerateInstallerOperations(includeMembers: Boolean, contentFilter: String?): InstallerOperations?
    fun getInstallerOperation(operationId: String?): InstallerOperation?

    fun importCertificateAuthority(
        solutionId: String?,
        importRequest: CertificateAuthoritiesImportRequest?,
        certificate: InputStream?,
        certificateFilename: String?,
    ): CertificateAuthorities_Import?

    fun exportCertificateAuthorities(solutionId: String?): Map<String, Any>?
    fun enumerateCertificateAuthorities(solutionId: String?, includeMembers: Boolean): CertificateAuthorities?
    fun getCertificateAuthority(solutionId: String?, certificateId: String?): CertificateAuthority?
    fun exportCertificateAuthority(solutionId: String?, certificateId: String?): Map<String, Any>?
    fun deleteCertificateAuthority(solutionId: String?, certificateId: String?): DeleteContent?
    //fun enumerateConfigurations(solutionId: String?, includeMembers: Boolean): Configurations?
/*    fun createConfiguration(
        solutionId: String?,
        createRequest: Configuration_Create?,
        data: InputStream?,
    ): Configuration?*/

    fun getConfiguration(solutionId: String?, key: String?): Configuration?
    //fun deleteConfiguration(solutionId: String?, key: String?): DeleteContent?
    fun getConfigurationData(solutionId: String?, key: String?): Map<String, Any>?
    fun replaceConfigurationData(solutionId: String?, key: String?, data: InputStream?, mime: String?): Data?
}
