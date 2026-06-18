package com.hp.workpath.pkgmgt.util.models

import com.fasterxml.jackson.databind.ObjectMapper
import com.hp.ext.service.application.ApplicationAgentRegistrationRecord
import com.hp.ext.service.application.I18nAssetRegistrationRecord
import com.hp.ext.service.application.MessageCenterAgentRegistrationRecord
import com.hp.ext.service.authentication.AuthenticationAgentRegistrationRecord
import com.hp.ext.service.copy.CopyAgentRegistrationRecord
import com.hp.ext.service.deviceUsage.DeviceUsageAgentRegistrationRecord
import com.hp.ext.service.jobStatistics.JobStatisticsAgentRegistrationRecord
import com.hp.ext.service.printJob.PrintJobAgentRegistrationRecord
import com.hp.ext.service.scanJob.ScanJobAgentRegistrationRecord
import com.hp.ext.service.security.SecurityAgentRegistrationRecord
import com.hp.ext.service.solutionDiagnostics.SolutionDiagnosticsAgentRegistrationRecord
import com.hp.ext.service.solutionManager.Solution
import com.hp.ext.service.solutionManager.SolutionNotificationAgentRegistrationRecord
import com.hp.ext.service.supplies.SuppliesAgentRegistrationRecord
import com.hp.ext.service.usbAccessories.UsbAccessoriesAgentRegistrationRecord
import com.hp.ext.types.agent.SolutionId
import com.hp.ext.types.common.TypedObject
import com.hp.ext.types.solutionManager.ContextProfile
import com.hp.ext.types.solutionManager.PlatformPackage
import com.hp.ext.types.solutionManager.RegistrationRecord
import com.hp.ext.types.solutionManager.SolutionManifest
import com.hp.workpath.pkgmgt.util.models.usbAccessories.UsbAccessoriesAgentModel
import com.hp.workpath.pkgmgt.util.models.application.ApplicationAgentModel
import com.hp.workpath.pkgmgt.util.models.application.I18nAssetLanguageModel
import com.hp.workpath.pkgmgt.util.models.application.I18nAssetModel
import com.hp.workpath.pkgmgt.util.models.application.I18nAssetStringModel
import com.hp.workpath.pkgmgt.util.models.authentication.AuthenticationAgentModel
import com.hp.workpath.pkgmgt.util.models.copyJob.CopyJobAgentModel
import com.hp.workpath.pkgmgt.util.models.deviceUsage.DeviceUsageAgentModel
import com.hp.workpath.pkgmgt.util.models.hpk.WebServiceInfo
import com.hp.workpath.pkgmgt.util.models.messageCenter.MessageCenterAgentModel
import com.hp.workpath.pkgmgt.util.models.printJob.PrintJobAgentModel
import com.hp.workpath.pkgmgt.util.models.scanJob.ScanJobAgentModel
import com.hp.workpath.pkgmgt.util.models.security.SecurityAgentModel
import com.hp.workpath.pkgmgt.util.models.solutionManager.*
import com.hp.workpath.pkgmgt.util.models.statisticsJob.StatisticsJobAgentModel
import com.hp.workpath.pkgmgt.util.models.supplies.SuppliesAgentModel
import com.hp.workpath.pkgmgt.util.utilities.MANIFEST_ARCHIVE_VERSION
import java.nio.file.Path
import java.util.*

class SolutionManagerData {
    val solutionDetails = SolutionDetailsModel()
    val workpathPlatformPackage = WorkpathPlatformPackageModel()
    //val defaultConfiguration = DefaultConfigurationModel() // TODO("Not yet implemented CDM, FW code: after beta8")
    val configuration = ConfigurationModel()
    val trustedSites = TrustedSitesModel()
}

class ApplicationServiceData {
    val applications = mutableListOf<ApplicationAgentModel>()
    val i18nAssets = mutableListOf<I18nAssetModel>()
    val defaultI18nAsset: I18nAssetModel
        get() {
            if (i18nAssets.isEmpty()) {
                i18nAssets.add(I18nAssetModel().also { i18nAsset ->
                    i18nAsset.i18nAssetId = UUID.randomUUID().toString()
                })
            }
            return i18nAssets[0]
        }

    fun addI18nStringToDefaultI18nAsset(languageTag: String, stringId: String, value: String) {
        val i18nAsset = defaultI18nAsset
        var languageModel = i18nAsset.inlineAsset.getLanguageModelByLanguageTag(languageTag)
        if (languageModel == null) {
            languageModel = I18nAssetLanguageModel().also {
                it.languageTag = languageTag
            }
            i18nAsset.inlineAsset.languages.add(languageModel)
        }
        var stringModel = languageModel.getStringModelByStringId(stringId)
        if (stringModel == null) {
            stringModel = I18nAssetStringModel().also {
                it.stringId = stringId
                it.value = value
            }
            languageModel.strings.add(stringModel)
        } else {
            throw IllegalArgumentException("I18n asset is already exist: $languageTag, $stringId")
        }
    }

    /**
     * Get localizedString from i18nAssets.
     * @param i18nAssetId LocalizedStringAssetId that pointing the LocalizedString
     * @param stringId StringId that pointing the LocalizedString
     * @return The LocalizedString mutable map (languageTag, string)
     */
    fun getLocalizedStringFromI18nAsset(i18nAssetId: String, stringId: String): MutableMap<String, String> {
        val stringMap = mutableMapOf<String, String>()
        i18nAssets.forEach { i18nAsset ->
            if (i18nAsset.i18nAssetId.equals(i18nAssetId, true)) {
                i18nAsset.inlineAsset.languages.forEach { i18nAssetLanguageModel ->
                    i18nAssetLanguageModel.strings.forEach { i18nAssetStringModel ->
                        if (i18nAssetStringModel.stringId == stringId) {
                            stringMap[i18nAssetLanguageModel.languageTag] = i18nAssetStringModel.value
                        }
                    }
                }
                return@forEach
            }
        }
        return stringMap
    }
}

class AuthenticationServiceData {
    val authenticationAgent = AuthenticationAgentModel()
}

class UsbAccessoriesServiceData {
    val usbAccessoriesAgent = UsbAccessoriesAgentModel()
}

class CopyJobServiceData {
    val copyJobAgent = CopyJobAgentModel()
}

class PrintJobServiceData {
    val printJobAgent = PrintJobAgentModel()
}

class ScanJobServiceData {
    val scanJobAgent = ScanJobAgentModel()
}

class DeviceUsageServiceData {
    val deviceUsageAgent = DeviceUsageAgentModel()
}

class SuppliesServiceData {
    val suppliesAgent = SuppliesAgentModel()
}

class WebServiceDate {
    val webServiceAgent = WebServiceInfo()
}

class SecurityServiceDate {
    val securityAgent = SecurityAgentModel()
}

class NotificationServiceDate {
    val notificationAgent = SolutionNotificationAgentModel()
}

class SolutionDiagnosticsServiceData {
    val solutionDiagnosticsAgent = SolutionDiagnosticsModel()
}

class StatisticsJobServiceData {
    val statisticsAgent = StatisticsJobAgentModel()
}

class MessageCenterServiceData {
    val messageCenterAgent = MessageCenterAgentModel()
}

class SolutionProject {
    var outputPathString: String = ""
    var outputHpkPath: Path? = null
    var apkPath: Path = Path.of("")
    var configPath: Path = Path.of("")
    var signingKeyPath: Path? = null
    var solutionBundleSerial: String = ""
    val solutionManager = SolutionManagerData()
    val applicationService = ApplicationServiceData()
    val authenticationService = AuthenticationServiceData()
    val accessoriesService = UsbAccessoriesServiceData()
    val copyJobService = CopyJobServiceData()
    val printJobService = PrintJobServiceData()
    val scanJobService = ScanJobServiceData()
    val deviceUsageService = DeviceUsageServiceData()
    val suppliesService = SuppliesServiceData()
    val webService = WebServiceDate()
    val securityService = SecurityServiceDate()
    val notificationService  = NotificationServiceDate()
    val solutionDiagnosticsService = SolutionDiagnosticsServiceData()
    val statisticsJobService = StatisticsJobServiceData()
    val messageCenterService = MessageCenterServiceData()
    var jsonManifestPath: Path? = null

    fun from(solutionManifest: SolutionManifest, solutionStagePath: Path) {
        solutionManifest.apply {
            if (archiveVersion != MANIFEST_ARCHIVE_VERSION) {
                throw Exception("Invalid archive version: $archiveVersion")
            }
            if(configuration != null) {
                solutionManager.configuration.from(solutionManifest.configuration)
                solutionManager.configuration.includeConfiguration = true
                configPath = Path.of(
                    solutionStagePath.toString(),
                    solutionManager.configuration.archiveDataPath
                ).toAbsolutePath()
            }
            if (trustedSites.sites.isExplicit) {
                // TODO DUNE-195685
                solutionManager.trustedSites.from(trustedSites)
            }
            solutionManager.solutionDetails.solutionDescription.from(description)
            if (packages.size != 1) {
                throw Exception("Invalid number of packages: ${packages.size}")
            }
            packages.forEach {
                if (it.isWorkpathPlatformPackage) {
                    solutionManager.workpathPlatformPackage.from(it.workpathPlatformPackage)
                } else {
                    throw Exception("Invalid package type: ${it.typeName}")
                }
            }
            registrations.forEach {
                when (it.record.typeGUN) {
                    I18nAssetRegistrationRecord().typeGUN -> {
                        if (applicationService.i18nAssets.size > 1) {
                            throw Exception("Invalid number of i18nAssets: ${applicationService.i18nAssets.size}")
                        }
                        applicationService.defaultI18nAsset.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), I18nAssetRegistrationRecord::class.java
                            )
                        )
                    }

                    ApplicationAgentRegistrationRecord().typeGUN -> {
                        applicationService.applications.add(
                            ApplicationAgentModel().apply {
                                from(
                                    ObjectMapper().readValue(
                                        ObjectMapper().writeValueAsString(it.record.value),
                                        ApplicationAgentRegistrationRecord::class.java
                                    )
                                )
                                details.icon.localIcon.originalPath =
                                    Path.of(solutionStagePath.toString(), details.icon.localIcon.path).toAbsolutePath()
                                details.iconSet.forEach { icon ->
                                    icon.localIcon.originalPath =
                                        Path.of(solutionStagePath.toString(), icon.localIcon.path).toAbsolutePath()
                                }
                            }
                        )
                    }

                    AuthenticationAgentRegistrationRecord().typeGUN -> {
                        authenticationService.authenticationAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(it.record.value),
                                AuthenticationAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    CopyAgentRegistrationRecord().typeGUN -> {
                        copyJobService.copyJobAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), CopyAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    PrintJobAgentRegistrationRecord().typeGUN -> {
                        printJobService.printJobAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), PrintJobAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    ScanJobAgentRegistrationRecord().typeGUN -> {
                        scanJobService.scanJobAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), ScanJobAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    DeviceUsageAgentRegistrationRecord().typeGUN -> {
                        deviceUsageService.deviceUsageAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), DeviceUsageAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    SuppliesAgentRegistrationRecord().typeGUN -> {
                        suppliesService.suppliesAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), SuppliesAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    UsbAccessoriesAgentRegistrationRecord().typeGUN -> {
                        accessoriesService.usbAccessoriesAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(it.record.value),
                                UsbAccessoriesAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    SecurityAgentRegistrationRecord().typeGUN -> {
                        securityService.securityAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), SecurityAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    SolutionNotificationAgentRegistrationRecord().typeGUN -> {
                        notificationService.notificationAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), SolutionNotificationAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    SolutionDiagnosticsAgentRegistrationRecord().typeGUN -> {
                        solutionDiagnosticsService.solutionDiagnosticsAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), SolutionDiagnosticsAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    JobStatisticsAgentRegistrationRecord().typeGUN -> {
                        statisticsJobService.statisticsAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), JobStatisticsAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    MessageCenterAgentRegistrationRecord().typeGUN -> {
                        messageCenterService.messageCenterAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), MessageCenterAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    else -> {
                        throw Exception("Invalid registration type: ${it.typeGUN}")
                    }
                }
            }
            solutionManager.solutionDetails.solutionId = solutionId.toString()
        }
    }

    fun from(solution: Solution) {
        solutionManager.solutionDetails.solutionId = solution.solutionId.toString()
        solutionManager.solutionDetails.solutionDescription.from(solution.description)
        // TODO DUNE-195685
        solution.trustedSites?.let {
            solutionManager.trustedSites.from(it)
        }
        if (solution.registrations.isNullOrEmpty()) {
            // Nothing
        } else {
            solution.registrations.forEach {
                when (it.record.typeGUN) {
                    ApplicationAgentRegistrationRecord().typeGUN -> {
                        applicationService.applications.add(
                            ApplicationAgentModel().apply {
                                from(
                                    ObjectMapper().readValue(
                                        ObjectMapper().writeValueAsString(
                                            it.record.value
                                        ), ApplicationAgentRegistrationRecord::class.java
                                    )
                                )
                            }
                        )
                    }

                    AuthenticationAgentRegistrationRecord().typeGUN -> {
                        authenticationService.authenticationAgent
                            .from(
                                ObjectMapper().readValue(
                                    ObjectMapper().writeValueAsString(it.record.value),
                                    AuthenticationAgentRegistrationRecord::class.java
                                )
                            )
                    }

                    UsbAccessoriesAgentRegistrationRecord().typeGUN -> {
                        accessoriesService.usbAccessoriesAgent
                            .from(
                                ObjectMapper().readValue(
                                    ObjectMapper().writeValueAsString(it.record.value),
                                    UsbAccessoriesAgentRegistrationRecord::class.java
                                )
                            )
                    }

                    CopyAgentRegistrationRecord().typeGUN -> {
                        copyJobService.copyJobAgent
                            .from(
                                ObjectMapper().readValue(
                                    ObjectMapper().writeValueAsString(it.record.value),
                                    CopyAgentRegistrationRecord::class.java
                                )
                            )
                    }

                    PrintJobAgentRegistrationRecord().typeGUN -> {
                        printJobService.printJobAgent
                            .from(
                                ObjectMapper().readValue(
                                    ObjectMapper().writeValueAsString(it.record.value),
                                    PrintJobAgentRegistrationRecord::class.java
                                )
                            )
                    }

                    ScanJobAgentRegistrationRecord().typeGUN -> {
                        scanJobService.scanJobAgent
                            .from(
                                ObjectMapper().readValue(
                                    ObjectMapper().writeValueAsString(it.record.value),
                                    ScanJobAgentRegistrationRecord::class.java
                                )
                            )
                    }

                    I18nAssetRegistrationRecord().typeGUN -> {
                        applicationService.i18nAssets.add(
                            I18nAssetModel().apply {
                                from(
                                    ObjectMapper().readValue(
                                        ObjectMapper().writeValueAsString(
                                            it.record.value
                                        ), I18nAssetRegistrationRecord::class.java
                                    )
                                )
                            }
                        )
                    }

                    DeviceUsageAgentRegistrationRecord().typeGUN -> {
                        deviceUsageService.deviceUsageAgent
                            .from(
                                ObjectMapper().readValue(
                                    ObjectMapper().writeValueAsString(it.record.value),
                                    DeviceUsageAgentRegistrationRecord::class.java
                                )
                            )
                    }

                    SuppliesAgentRegistrationRecord().typeGUN -> {
                        suppliesService.suppliesAgent
                            .from(
                                ObjectMapper().readValue(
                                    ObjectMapper().writeValueAsString(it.record.value),
                                    SuppliesAgentRegistrationRecord::class.java
                                )
                            )
                    }

                    SecurityAgentRegistrationRecord().typeGUN -> {
                        securityService.securityAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), SecurityAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    SolutionNotificationAgentRegistrationRecord().typeGUN -> {
                        notificationService.notificationAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), SolutionNotificationAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    SolutionDiagnosticsAgentRegistrationRecord().typeGUN -> {
                        solutionDiagnosticsService.solutionDiagnosticsAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), SolutionDiagnosticsAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    JobStatisticsAgentRegistrationRecord().typeGUN -> {
                        statisticsJobService.statisticsAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), JobStatisticsAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    MessageCenterAgentRegistrationRecord().typeGUN -> {
                        messageCenterService.messageCenterAgent.from(
                            ObjectMapper().readValue(
                                ObjectMapper().writeValueAsString(
                                    it.record.value
                                ), MessageCenterAgentRegistrationRecord::class.java
                            )
                        )
                    }

                    else -> {
                        // TODO other components: statistics, web service, ...
                        println("Unknown typeGUN: ${it.record.typeGUN}")
                    }
                }
            }
        }
    }

    fun to(): SolutionManifest {
        val solutionManifest = SolutionManifest()

        // solution manager
        solutionManifest.archiveVersion = MANIFEST_ARCHIVE_VERSION
        //solutionManifest.configuration = solutionManager.configuration.to()
        solutionManifest.solutionId = SolutionId.createSolutionId(solutionManager.solutionDetails.solutionId)
        solutionManifest.description = solutionManager.solutionDetails.solutionDescription.to()
        solutionManifest.packages = listOf(PlatformPackage().apply {
            workpathPlatformPackage = solutionManager.workpathPlatformPackage.to()
        })
        if (solutionManager.configuration.includeConfiguration) {
            solutionManifest.configuration = solutionManager.configuration.to()
        }
        solutionManifest.contextProfile = ContextProfile()
        // include empty contextDefinitions
        solutionManifest.contextProfile.contextDefinitions = mutableListOf()
        solutionManifest.registrations = mutableListOf()

        // TODO DUNE-195685
        solutionManifest.trustedSites = solutionManager.trustedSites.to()
        //application
        applicationService.i18nAssets.forEach { i18nAssetModel ->
            solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                rr.record = TypedObject().also {
                    val i18nAssetRecord = i18nAssetModel.to()
                    it.value = i18nAssetRecord
                    it.typeGUN = i18nAssetRecord.typeGUN
                }
            })
        }
        applicationService.applications.forEach { applicationAgentModel ->
            solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                rr.record = TypedObject().also {
                    val appAgentRecord = applicationAgentModel.to()
                    it.value = appAgentRecord
                    it.typeGUN = appAgentRecord.typeGUN
                }
            })
        }
        //authentication
        authenticationService.authenticationAgent.also { authenticationAgentModel ->
            if (authenticationAgentModel.includeAuthenticationAgent) {
                solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                    rr.record = TypedObject().also {
                        val authenticationRecord = authenticationAgentModel.to()
                        it.value = authenticationRecord
                        it.typeGUN = authenticationRecord.typeGUN
                    }
                })
            }
        }
        //accessories
        accessoriesService.usbAccessoriesAgent.also { usbAccessoriesAgentModel ->
            if (usbAccessoriesAgentModel.includeUsbAccessoriesAgent) {
                solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                    rr.record = TypedObject().also {
                        val usbAccessoriesRecord = usbAccessoriesAgentModel.to()
                        it.value = usbAccessoriesRecord
                        it.typeGUN = usbAccessoriesRecord.typeGUN
                    }
                })
            }
        }
        //copy job
        copyJobService.copyJobAgent.also { copyJobAgentModel ->
            if (copyJobAgentModel.includeCopyJobAgent) {
                solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                    rr.record = TypedObject().also {
                        val copyAgentRecord = copyJobAgentModel.to()
                        it.value = copyAgentRecord
                        it.typeGUN = copyAgentRecord.typeGUN
                    }
                })
            }
        }
        //print job
        printJobService.printJobAgent.also { printJobAgentModel ->
            if (printJobAgentModel.includePrintJobAgent) {
                solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                    rr.record = TypedObject().also {
                        val printAgentRecord = printJobAgentModel.to()
                        it.value = printAgentRecord
                        it.typeGUN = printAgentRecord.typeGUN
                    }
                })
            }
        }
        //scan job
        scanJobService.scanJobAgent.also { scanJobAgentModel ->
            if (scanJobAgentModel.includeScanJobAgent) {
                solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                    rr.record = TypedObject().also {
                        val scanAgentRecord = scanJobAgentModel.to()
                        it.value = scanAgentRecord
                        it.typeGUN = scanAgentRecord.typeGUN
                    }
                })
            }
        }
        //device usage
        deviceUsageService.deviceUsageAgent.also { deviceUsageAgentModel ->
            if (deviceUsageAgentModel.includeDeviceUsageAgent) {
                solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                    rr.record = TypedObject().also {
                        val deviceUsageAgentRecord = deviceUsageAgentModel.to()
                        it.value = deviceUsageAgentRecord
                        it.typeGUN = deviceUsageAgentRecord.typeGUN
                    }
                })
            }
        }
        //supplies
        suppliesService.suppliesAgent.also { suppliesAgentModel ->
            if (suppliesAgentModel.includeSuppliesAgent) {
                solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                    rr.record = TypedObject().also {
                        val suppliesAgentRecord = suppliesAgentModel.to()
                        it.value = suppliesAgentRecord
                        it.typeGUN = suppliesAgentRecord.typeGUN
                    }
                })
            }
        }

        //statistics job
        statisticsJobService.statisticsAgent.also { statisticsJobAgentModel ->
            if (statisticsJobAgentModel.includeStatisticsJobAgent) {
                solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                    rr.record = TypedObject().also {
                        val statisticsAgentRegistrationRecord = statisticsJobAgentModel.to()
                        it.value = statisticsAgentRegistrationRecord
                        it.typeGUN = statisticsAgentRegistrationRecord.typeGUN
                    }
                })
            }
        }

        //security
        securityService.securityAgent.also { securityAgentModel ->
            if (securityAgentModel.includeSecurityAgent) {
                solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                    rr.record = TypedObject().also {
                        val securityAgentRecord = securityAgentModel.to()
                        it.value = securityAgentRecord
                        it.typeGUN = securityAgentRecord.typeGUN
                    }
                })
            }
        }

        //solution notification
        notificationService.notificationAgent.also {  notificationAgentModel ->
            if (notificationAgentModel.includeNotificationAgent) {
                solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                    rr.record = TypedObject().also {
                        val notificationAgentRecord = notificationAgentModel.to()
                        it.value = notificationAgentRecord
                        it.typeGUN = notificationAgentRecord.typeGUN
                    }
                })
            }
        }

        //solution diagnostics
        solutionDiagnosticsService.solutionDiagnosticsAgent.also {  solutionDiagnosticsAgentModel ->
            if (solutionDiagnosticsAgentModel.includeSolutionDiagnosticsAgent) {
                solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                    rr.record = TypedObject().also {
                        val solutionDiagnosticsAgentRecord = solutionDiagnosticsAgentModel.to()
                        it.value = solutionDiagnosticsAgentRecord
                        it.typeGUN = solutionDiagnosticsAgentRecord.typeGUN
                    }
                })
            }
        }

        //message center
        messageCenterService.messageCenterAgent.also { messageCenterAgentModel ->
            if (messageCenterAgentModel.includeMessageCenterAgent) {
                solutionManifest.registrations.add(RegistrationRecord().also { rr ->
                    rr.record = TypedObject().also {
                        val messageCenterAgentRecord = messageCenterAgentModel.to()
                        it.value = messageCenterAgentRecord
                        it.typeGUN = messageCenterAgentRecord.typeGUN
                    }
                })
            }
        }
        return solutionManifest
    }
}
