package com.hp.workpath.pkgmgt.util.utilities

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.hp.workpath.pkgmgt.lib.HpkFile
import com.hp.workpath.pkgmgt.lib.LocalizedString
import com.hp.workpath.pkgmgt.util.models.SolutionProject
import com.hp.workpath.pkgmgt.util.models.application.ApplicationAgentModel
import com.hp.workpath.pkgmgt.util.models.application.ApplicationCategory
import com.hp.workpath.pkgmgt.util.models.application.ApplicationIconDetailModel
import com.hp.workpath.pkgmgt.util.models.hpk.WebServiceEndPoint
import com.hp.workpath.pkgmgt.util.models.hpk.WorkpathPlatformVersion.Companion.getEnumByValue
import com.hp.workpath.pkgmgt.util.models.usbAccessories.UsbRegistrationModel
import com.hp.workpath.pkgmgt.util.utilities.apk.ApkParser
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

class HpkConverter(hpkFile: HpkFile) {
    val apkParser: ApkParser
    val solutionProject: SolutionProject

    init {
        try {
            Path.of(Paths.get("").absolutePathString(), DIR_TEMP).toFile().mkdirs()
            /** set apkFile */
            val apkFile = Path.of(Paths.get("").absolutePathString(), DIR_TEMP, hpkFile.connector.installFile).toFile()
            apkFile.createNewFile()
            var fileOutputStream = FileOutputStream(apkFile)
            hpkFile.installFile.copyTo(fileOutputStream)
            fileOutputStream.close()
            apkParser = ApkParser(apkFile)
            /** set configFile */
            val configFile: File? =
                if (hpkFile.connector.defaultConfig.isNullOrEmpty()) {
                    null
                } else {
                    Path.of(Paths.get("").absolutePathString(), DIR_TEMP, hpkFile.connector.defaultConfig).toFile()
                }
            if (configFile != null) {
                configFile.createNewFile()
                fileOutputStream = FileOutputStream(configFile)
                hpkFile.defaultConfigFile.copyTo(fileOutputStream)
                fileOutputStream.close()
            }
            /** set solutionProject */
            solutionProject = SolutionProject()
            solutionProject.apply {
                apkPath = apkFile.toPath()
                if (configFile != null) {
                    configPath = configFile.toPath()
                }
                solutionManager.apply {
                    solutionDetails.apply {
                        solutionId = hpkFile.connector.uuid.toString()
                        solutionDescription.apply {
                            name = hpkFile.connector.name
                            vendor = hpkFile.connector.vendorName
                            date = hpkFile.connector.date
                        }
                    }
                    workpathPlatformPackage.apply {
                        workpathPackagePath = apkFile.name
                        platformVersion = getEnumByValue(hpkFile.connector.platformVersion)
                        installFile = hpkFile.connector.installFile
                    }
                    configuration.apply {
                        if (configFile != null) {
                            includeConfiguration = true
                            archiveDataPath = ASSETS_CONFIGS + configFile.name
                            description = ""
                            mimeType = DEFAULT_CONFIG_MIME
                        }
                    }
                }
                if (hpkFile.connector.providers != null) {
                    hpkFile.connector.providers.forEach { provider ->
                        when (provider.type) {
                            PROVIDER_TYPE_AUTHENTICATION -> {
                                authenticationService.authenticationAgent.apply {
                                    includeAuthenticationAgent = true
                                    agentId = provider.uuid
                                    title.apply {
                                        i18nAssetId = applicationService.defaultI18nAsset.i18nAssetId
                                        stringId = AUTHENTICATION_TITLE_STRING_ID
                                    }
                                    addLocalizedStringArrayListToDefaultI18nAsset(
                                        AUTHENTICATION_TITLE_STRING_ID,
                                        provider.title
                                    )
                                    description.apply {
                                        i18nAssetId = applicationService.defaultI18nAsset.i18nAssetId
                                        stringId = AUTHENTICATION_DESCRIPTION_STRING_ID
                                    }
                                    addLocalizedStringArrayListToDefaultI18nAsset(
                                        AUTHENTICATION_DESCRIPTION_STRING_ID,
                                        provider.description
                                    )
                                    name = applicationService.getLocalizedStringFromI18nAsset(
                                        title.i18nAssetId,
                                        title.stringId
                                    )[LANGUAGE_TAG_EN_US] ?: hpkFile.connector.name
                                    enablePrePrompt = "true" == provider.enablePrePromptCheck
                                    workpathPackage = provider.authenticationUrl
                                    enableSignoutNotification = true
                                }
                            }

                            PROVIDER_TYPE_HOME_SCREEN -> {
                                applicationService.applications.add(ApplicationAgentModel().apply {
                                    target.apply {
                                        isMainApplication = true
                                        workpathPackage = apkParser.getMainActivityPlatformPath()
                                    }
                                    details.apply {
                                        applicationId = UUID.randomUUID().toString()
                                        category = ApplicationCategory.HomeScreen
                                        isTitleFromUser = false
                                        title.apply {
                                            i18nAssetId = applicationService.defaultI18nAsset.i18nAssetId
                                            stringId = APPLICATION_TITLE_STRING_ID
                                        }
                                        apkParser.getTitle(target.workpathPackage)?.forEach { (tag, value) ->
                                            applicationService.addI18nStringToDefaultI18nAsset(
                                                tag,
                                                APPLICATION_TITLE_STRING_ID,
                                                value
                                            )
                                        }
                                        isDescriptionFromUser = false
                                        description.apply {
                                            i18nAssetId = applicationService.defaultI18nAsset.i18nAssetId
                                            stringId = APPLICATION_DESCRIPTION_STRING_ID
                                        }
                                        apkParser.getDescription(target.workpathPackage)
                                            ?.forEach { (tag, value) ->
                                                applicationService.addI18nStringToDefaultI18nAsset(
                                                    tag,
                                                    APPLICATION_DESCRIPTION_STRING_ID,
                                                    value
                                                )
                                            }
                                        name = applicationService.getLocalizedStringFromI18nAsset(
                                            title.i18nAssetId,
                                            title.stringId
                                        )[LANGUAGE_TAG_EN_US] ?: hpkFile.connector.name
                                        icon.apply {
                                            localIcon.apply {
                                                originalPath = apkParser.getIconPath(target.workpathPackage)
                                                fileType = getIconFileType(originalPath!!)
                                                path = ASSETS_ICONS + originalPath!!.name
                                            }
                                        }
                                        apkParser.getIconSetPath(target.workpathPackage)?.forEach { (name, value) ->
                                            iconSet.add(ApplicationIconDetailModel().apply {
                                                isInIconSet = true
                                                key = name
                                                localIcon.apply {
                                                    originalPath = value
                                                    fileType = getIconFileType(originalPath!!)
                                                    path = ASSETS_ICONS + key + '/' + originalPath!!.name
                                                }
                                            })
                                        }
                                        isIconFromUser = false
                                        setAsDefault = "true" == provider.configOnInstall
                                    }
                                })
                            }

                            PROVIDER_TYPE_ACCESSORIES -> {
                                accessoriesService.usbAccessoriesAgent.apply {
                                    if (includeUsbAccessoriesAgent.not()) {
                                        includeUsbAccessoriesAgent = true
                                        agentId = UUID.randomUUID().toString()
                                        name =
                                            solutionManager.solutionDetails.solutionDescription.name + ACCESSORIES_NAME
                                        title.apply {
                                            i18nAssetId = applicationService.defaultI18nAsset.i18nAssetId
                                            stringId = ACCESSORIES_TITLE_STRING_ID
                                        }
                                        applicationService.addI18nStringToDefaultI18nAsset(
                                            LANGUAGE_TAG_EN_US,
                                            ACCESSORIES_TITLE_STRING_ID,
                                            name
                                        )
                                        description.apply {
                                            i18nAssetId = applicationService.defaultI18nAsset.i18nAssetId
                                            stringId = ACCESSORIES_DESCRIPTION_STRING_ID
                                        }
                                        applicationService.addI18nStringToDefaultI18nAsset(
                                            LANGUAGE_TAG_EN_US,
                                            ACCESSORIES_DESCRIPTION_STRING_ID,
                                            name
                                        )
                                    }
                                    registrations.add(UsbRegistrationModel().apply {
                                        productId = provider.productId.toInt()
                                        setRegistrationType(provider.registrationType)
                                        serialNumber = provider.serialNumber?.trim()?.let { normalized ->
                                            if (normalized.equals("null", ignoreCase = true) || normalized.isBlank()) null else normalized
                                        }
                                        vendorId = provider.vendorId.toInt()
                                    })
                                }
                            }

                            PROVIDER_TYPE_WEBSERVICES -> {
                                webService.webServiceAgent.apply {
                                    includeWebServiceInfo = true
                                    uuid = UUID.randomUUID().toString()
                                    titles.addAll(provider.title)
                                    descriptions.addAll(provider.description)
                                    webServiceEndPoints.addAll(convertJsonToWebServiceEndPoints(provider.endPoints))
                                }
                            }

                            PROVIDER_TYPE_STATISTICS -> {
                                statisticsJobService.statisticsAgent.apply {
                                    includeStatisticsJobAgent = true
                                    agentId = UUID.randomUUID().toString()
                                    title.apply {
                                        i18nAssetId = applicationService.defaultI18nAsset.i18nAssetId
                                        stringId = STATISTICS_AGENT_TITLE_STRING_ID
                                    }
                                    addLocalizedStringArrayListTagToDefaultI18nAsset(
                                        STATISTICS_AGENT_TITLE_STRING_ID,
                                        provider.title
                                    )
                                    description.apply {
                                        i18nAssetId = applicationService.defaultI18nAsset.i18nAssetId
                                        stringId = STATISTICS_AGENT_DESCRIPTION_STRING_ID
                                    }

                                    name = applicationService.getLocalizedStringFromI18nAsset(
                                        title.i18nAssetId,
                                        title.stringId
                                    )[LANGUAGE_TAG_EN_US] ?: hpkFile.connector.name

                                    criticalSolution = provider.ackRequiredForDelete
                                }
                            }

                            else -> throw Exception(EXCEPTION_HPK_OPEN_FAIL)
                        }
                    }
                }

                scanJobService.scanJobAgent.apply {
                    includeScanJobAgent =  true
                }

                copyJobService.copyJobAgent.apply {
                    includeCopyJobAgent =  true

                }

                printJobService.printJobAgent.apply {
                    includePrintJobAgent = true
                }

                deviceUsageService.deviceUsageAgent.apply {
                    includeDeviceUsageAgent = true
                }

                suppliesService.suppliesAgent.apply {
                    includeSuppliesAgent = true
                }

                
                val hasMainActivity = apkParser.getMainActivityName().isNotEmpty()

                if (hasMainActivity && applicationService.applications.isEmpty()) {
                    applicationService.applications.add(ApplicationAgentModel().apply {
                        target.apply {
                            isMainApplication = true
                            workpathPackage = apkParser.getMainActivityPlatformPath()
                        }
                        details.apply {
                            applicationId = UUID.randomUUID().toString()
                            category = ApplicationCategory.Standard
                            isTitleFromUser = false
                            title.apply {
                                i18nAssetId = applicationService.defaultI18nAsset.i18nAssetId
                                stringId = APPLICATION_TITLE_STRING_ID
                            }
                            apkParser.getTitle(target.workpathPackage)?.forEach { (tag, value) ->
                                applicationService.addI18nStringToDefaultI18nAsset(
                                    tag,
                                    APPLICATION_TITLE_STRING_ID,
                                    value
                                )
                            }
                            isDescriptionFromUser = false
                            description.apply {
                                i18nAssetId = applicationService.defaultI18nAsset.i18nAssetId
                                stringId = APPLICATION_DESCRIPTION_STRING_ID
                            }
                            apkParser.getDescription(target.workpathPackage)?.forEach { (tag, value) ->
                                applicationService.addI18nStringToDefaultI18nAsset(
                                    tag,
                                    APPLICATION_DESCRIPTION_STRING_ID,
                                    value
                                )
                            }
                            name = applicationService.getLocalizedStringFromI18nAsset(
                                title.i18nAssetId,
                                title.stringId
                            )[LANGUAGE_TAG_EN_US] ?: hpkFile.connector.name
                            icon.apply {
                                localIcon.apply {
                                    originalPath = apkParser.getIconPath(target.workpathPackage)
                                    fileType = getIconFileType(originalPath!!)
                                    path = ASSETS_ICONS + originalPath!!.name
                                }
                            }
                            apkParser.getIconSetPath(target.workpathPackage)?.forEach { (name, value) ->
                                iconSet.add(ApplicationIconDetailModel().apply {
                                    isInIconSet = true
                                    key = name
                                    localIcon.apply {
                                        originalPath = value
                                        fileType = getIconFileType(originalPath!!)
                                        path = ASSETS_ICONS + key + '/' + originalPath!!.name
                                    }
                                })
                            }
                            isIconFromUser = false
                        }
                    })
                }
                if (hpkFile.connector.subApps != null) {
                    hpkFile.connector.subApps.forEachIndexed { index, subApp ->
                        applicationService.applications.add(ApplicationAgentModel().apply {
                            target.apply {
                                isMainApplication = false
                                workpathPackage = subApp.platformId
                            }
                            details.apply {
                                applicationId = subApp.uuid.toString()
                                category = ApplicationCategory.Standard
                                isTitleFromUser = false
                                title.apply {
                                    i18nAssetId = applicationService.defaultI18nAsset.i18nAssetId
                                    stringId = APPLICATION_TITLE_STRING_ID + index
                                }
                                apkParser.getTitle(target.workpathPackage)?.forEach { (tag, value) ->
                                    applicationService.addI18nStringToDefaultI18nAsset(
                                        tag,
                                        APPLICATION_TITLE_STRING_ID + index,
                                        value
                                    )
                                }
                                isDescriptionFromUser = false
                                description.apply {
                                    i18nAssetId = applicationService.defaultI18nAsset.i18nAssetId
                                    stringId = APPLICATION_DESCRIPTION_STRING_ID + index
                                }
                                apkParser.getDescription(target.workpathPackage)?.forEach { (tag, value) ->
                                    applicationService.addI18nStringToDefaultI18nAsset(
                                        tag,
                                        APPLICATION_DESCRIPTION_STRING_ID + index,
                                        value
                                    )
                                }
                                name = applicationService.getLocalizedStringFromI18nAsset(
                                    title.i18nAssetId,
                                    title.stringId
                                )[LANGUAGE_TAG_EN_US] ?: hpkFile.connector.name
                                icon.apply {
                                    localIcon.apply {
                                        originalPath = apkParser.getIconPath(target.workpathPackage)
                                        fileType = getIconFileType(originalPath!!)
                                        path = ASSETS_ICONS + originalPath!!.name
                                    }
                                }
                                apkParser.getIconSetPath(target.workpathPackage)?.forEach { (name, value) ->
                                    iconSet.add(ApplicationIconDetailModel().apply {
                                        isInIconSet = true
                                        key = name
                                        localIcon.apply {
                                            originalPath = value
                                            fileType = getIconFileType(originalPath!!)
                                            path = ASSETS_ICONS + key + '/' + originalPath!!.name
                                        }
                                    })
                                }
                                isIconFromUser = false
                            }
                        })
                    }
                }
//            Can't set below Services automatically
//            printJobService, scanJobService, copyJobService
            }
        } catch (exception: Exception) {
            throw exception
        }
    }

    private fun addLocalizedStringArrayListToDefaultI18nAsset(
        stringId: String,
        localizedStringArrayList: ArrayList<LocalizedString>,
    ) {
        localizedStringArrayList.forEach { localizedString ->
            solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                localizedString.code,
                stringId,
                localizedString.value
            )
        }
    }

    private fun addLocalizedStringArrayListTagToDefaultI18nAsset(
        stringId: String,
        localizedStringArrayList: ArrayList<LocalizedString>,
    ) {
        localizedStringArrayList.forEach { localizedString ->
            solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                localizedString.languageTag,
                stringId,
                localizedString.value
            )
        }
    }

    private fun convertJsonToWebServiceEndPoints(json: String): ArrayList<WebServiceEndPoint> {
        val webServiceEndPoints = arrayListOf<WebServiceEndPoint>()
        try {
            val arrayNode = ObjectMapper().readTree(json)
            if (arrayNode !is ArrayNode) {
                throw Exception(EXCEPTION_HPK_OPEN_FAIL)
            }
            arrayNode.forEach { array ->
                webServiceEndPoints.add(WebServiceEndPoint().apply {
                    setMethodType(array.get(WEBSERVICE_METHOD).asText())
                    category = array.get(WEBSERVICE_CATEGORY).asText()
                    absolutePath = array.get(WEBSERVICE_ABSOLUTE_PATH).asText()
                    setAuthType(
                        array.get(WEBSERVICE_AUTH_TYPE)?.asText() ?: WebServiceEndPoint.AuthType.NONE.toString()
                    )
                })
            }
        } catch (exception: Exception) {
            throw exception
        }
        return webServiceEndPoints
    }
}