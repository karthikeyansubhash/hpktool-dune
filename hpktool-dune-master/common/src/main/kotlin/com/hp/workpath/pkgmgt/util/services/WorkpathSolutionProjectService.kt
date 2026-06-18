package com.hp.workpath.pkgmgt.util.services

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.hp.ext.types.solutionManager.SolutionManifest
import com.hp.workpath.pkgmgt.lib.*
import com.hp.workpath.pkgmgt.util.models.SolutionProject
import com.hp.workpath.pkgmgt.util.models.connection.TaskState
import com.hp.workpath.pkgmgt.util.models.hpk.HPKVersion
import com.hp.workpath.pkgmgt.util.utilities.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class WorkpathSolutionProjectService(
    private val solutionProject: SolutionProject,
    private val taskInterface: TaskInterface,
) {
    fun createBundle() {
        try {
            taskInterface.updateMessage(TaskStatus(TaskState.InProgress, ""))
            cleanOutputDirectory()
            copySolutionFiles()
            exportSolutionManifest()
            packageSolution()
            cleanTemporaryDirectories()
            taskInterface.onSucceed(null)
        } catch (exception: Exception) {
            taskInterface.onFailed(exception)
        }
    }

    fun createBundleWithJson() {
        try {
            taskInterface.updateMessage(TaskStatus(TaskState.InProgress, ""))
            cleanOutputDirectory()
            copySolutionCommandFiles()
            copySolutionManifestFile()
            packageSolution()
            cleanTemporaryDirectories()
            taskInterface.onSucceed(null)
        } catch (exception: Exception) {
            taskInterface.onFailed(exception)
        }
    }

    fun createHpkBundle() {
        try {
            packageHpk(generateHpkConnector())
            taskInterface.onSucceed(null)
        } catch (exception: Exception) {
            taskInterface.onFailed(exception)
        }
    }

    private fun cleanOutputDirectory() {
        //delete stage, squash, tar, pak dirs and [filename].hpk2
        listOf<Path>(
            Path.of(solutionProject.outputPathString, DIRECTORY_STAGE),
            Path.of(solutionProject.outputPathString, DIRECTORY_SQUASH),
            Path.of(solutionProject.outputPathString, DIRECTORY_TAR),
            Path.of(solutionProject.outputPathString, DIRECTORY_PAK),
            Path.of(
                solutionProject.outputPathString,
                solutionProject.apkPath.nameWithoutExtension + HPK2_EXTENSION_WITH_DOT
            ),
        ).map { it.toFile() }.forEach {
            if (it.exists()) {
                if (it.isDirectory) {
                    it.deleteRecursively()
                } else {
                    it.delete()
                }
            }
        }
    }

    private fun cleanTemporaryDirectories() {
        //delete stage, squash, tar, pak dirs and [filename].bdl
        listOf<Path>(
            Path.of(solutionProject.outputPathString, DIRECTORY_STAGE),
            Path.of(solutionProject.outputPathString, DIRECTORY_SQUASH),
            Path.of(solutionProject.outputPathString, DIRECTORY_TAR),
            Path.of(solutionProject.outputPathString, DIRECTORY_PAK),
        ).map { it.toFile() }.forEach {
            if (it.exists()) {
                if (it.isDirectory) {
                    it.deleteRecursively()
                } else {
                    it.delete()
                }
            }
        }
    }

    private fun exportSolutionManifest() {
        writeSolutionManifestFile(solutionProject.to())
    }

    private fun writeSolutionManifestFile(manifest: SolutionManifest) {
        Files.createDirectories(Paths.get(solutionProject.outputPathString, DIRECTORY_STAGE))
        val fileOutputStream =
            FileOutputStream(Paths.get(solutionProject.outputPathString, DIRECTORY_STAGE, MANIFEST_FILE_NAME).toFile())
        ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()
            .writeValue(fileOutputStream, manifest)
        fileOutputStream.close()
    }

    private fun copySolutionManifestFile() {
        Files.createDirectories(Paths.get(solutionProject.outputPathString, DIRECTORY_STAGE))
        solutionProject.jsonManifestPath!!.toFile().copyTo(
            Path.of(solutionProject.outputPathString, DIRECTORY_STAGE, MANIFEST_FILE_NAME).toFile(),
            overwrite = true
        )
    }

    private fun generateHpkConnector(): Connector {
        val connector = Connector()
        connector.platformType = PlatformType.LinkForDevice
        connector.vendorName = solutionProject.solutionManager.solutionDetails.solutionDescription.vendor
        connector.date = solutionProject.solutionManager.solutionDetails.solutionDescription.date
        connector.installFile = solutionProject.solutionManager.workpathPlatformPackage.installFile
        connector.platformVersion = solutionProject.solutionManager.workpathPlatformPackage.platformVersion.toString()
        if (solutionProject.solutionManager.configuration.includeConfiguration) {
            connector.defaultConfig = Path.of(solutionProject.solutionManager.configuration.archiveDataPath).name
        }
        val subApps = arrayListOf<SubApp>()
        solutionProject.applicationService.applications.forEach { applicationAgentModel ->
            if (applicationAgentModel.target.isMainApplication) {
                connector.uuid = UUID.fromString(applicationAgentModel.details.applicationId)
                connector.name = applicationAgentModel.details.name
            } else {
                subApps.add(SubApp().apply {
                    uuid = UUID.fromString(applicationAgentModel.details.applicationId)
                    platformId = applicationAgentModel.target.workpathPackage
                })
            }
        }
        if (subApps.isNotEmpty()) {
            connector.subApps = subApps
        }
        val providers = arrayListOf<Provider>()
        solutionProject.authenticationService.authenticationAgent.let { authentication ->
            if (authentication.includeAuthenticationAgent) {
                if (solutionProject.solutionManager.workpathPlatformPackage.platformVersion.hpkVersion.level < HPKVersion.HPK_1_1.level) {
                    throw IllegalArgumentException("$OPT_SOLUTION_PLATFORM_VERSION :${solutionProject.solutionManager.workpathPlatformPackage.platformVersion}$EXCEPTION_IS_NOT_SUPPORT$OPT_AUTHENTICATION_ID")
                }
                connector.uuid = UUID.fromString(authentication.agentId)
                connector.name = authentication.name
                providers.add(Provider().apply {
                    type = PROVIDER_TYPE_AUTHENTICATION
                    uuid = UUID.randomUUID().toString()
                    title = getLocalizedStringsFromI18nAssets(
                        authentication.title.i18nAssetId,
                        authentication.title.stringId
                    )
                    description = getLocalizedStringsFromI18nAssets(
                        authentication.description.i18nAssetId,
                        authentication.description.stringId
                    )
                    authenticationUrl = authentication.workpathPackage
                    enablePrePromptCheck = authentication.enablePrePrompt.toString()
                })
            }
        }
        /**
         * TODO HomeScreen
         */
        solutionProject.accessoriesService.usbAccessoriesAgent.let { usbAccessories ->
            if (solutionProject.solutionManager.workpathPlatformPackage.platformVersion.hpkVersion.level < HPKVersion.HPK_1_2.level) {
                throw IllegalArgumentException("$OPT_SOLUTION_PLATFORM_VERSION :${solutionProject.solutionManager.workpathPlatformPackage.platformVersion}$EXCEPTION_IS_NOT_SUPPORT$OPT_ACCESSORIES")
            }
            usbAccessories.registrations.forEach {
                providers.add(Provider().apply {
                    type = PROVIDER_TYPE_ACCESSORIES
                    registrationType = it.registration.value
                    vendorId = it.vendorId.toString()
                    productId = it.productId.toString()
                    serialNumber = it.serialNumber
                })
            }
        }
        solutionProject.webService.webServiceAgent.let { webService ->
            if (webService.includeWebServiceInfo) {
                if (solutionProject.solutionManager.workpathPlatformPackage.platformVersion.hpkVersion.level < HPKVersion.HPK_1_4.level) {
                    throw IllegalArgumentException("$OPT_SOLUTION_PLATFORM_VERSION :${solutionProject.solutionManager.workpathPlatformPackage.platformVersion}$EXCEPTION_IS_NOT_SUPPORT$OPT_WEBSERVICE_AGENT")
                }
                providers.add(Provider().apply {
                    type = PROVIDER_TYPE_WEBSERVICES
                    uuid = webService.uuid
                    title = webService.titles
                    description = webService.descriptions
                    endPoints = ObjectMapper().enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                        .writeValueAsString(webService.webServiceEndPoints)
                })
            }
        }
        solutionProject.statisticsJobService.statisticsAgent.let { statisticsJobAgentModel ->
            if (statisticsJobAgentModel.includeStatisticsJobAgent) {
                if (solutionProject.solutionManager.workpathPlatformPackage.platformVersion.hpkVersion.level < HPKVersion.HPK_1_4.level) {
                    throw IllegalArgumentException("$OPT_SOLUTION_PLATFORM_VERSION :${solutionProject.solutionManager.workpathPlatformPackage.platformVersion}$EXCEPTION_IS_NOT_SUPPORT$OPT_ACCESSORIES")
                }
                providers.add(Provider().apply {
                    type = PROVIDER_TYPE_STATISTICS
                    uuid = UUID.randomUUID().toString()
                    ackRequiredForDelete = statisticsJobAgentModel.criticalSolution
                })
            }

        }
        if (providers.isNotEmpty()) {
            connector.providers = providers
        }
        connector.schemaLocation =
            HPK_NAMESPACE + solutionProject.solutionManager.workpathPlatformPackage.platformVersion.hpkVersion.toString() + HPK_XSD
        connector.namespace =
            HPK_NAMESPACE + solutionProject.solutionManager.workpathPlatformPackage.platformVersion.hpkVersion.toString()

        return connector
    }

    private fun getLocalizedStringsFromI18nAssets(i18nAssetId: String, stringId: String): ArrayList<LocalizedString> {
        val localizedStrings = ArrayList<LocalizedString>()
        for (i18nAsset in solutionProject.applicationService.i18nAssets) {
            if (i18nAsset.i18nAssetId == i18nAssetId) {
                for (language in i18nAsset.inlineAsset.languages) {
                    for (string in language.strings) {
                        if (string.stringId == stringId) {
                            localizedStrings.add(LocalizedString().apply {
                                code = language.languageTag
                                value = string.value
                            })
                        }
                    }
                }
            }
        }
        return localizedStrings
    }

    private fun copySolutionFiles() {
        solutionProject.apkPath.toFile().copyTo(
            Path.of(
                solutionProject.outputPathString,
                DIRECTORY_SQUASH,
                solutionProject.apkPath.name
            ).toFile(), overwrite = true
        )
        if (solutionProject.solutionManager.configuration.includeConfiguration) {
            solutionProject.configPath.toFile().copyTo(
                Path.of(
                    solutionProject.outputPathString,
                    DIRECTORY_STAGE,
                    solutionProject.solutionManager.configuration.archiveDataPath
                ).toFile(), overwrite = true
            )
        }
        for (application in solutionProject.applicationService.applications) {
            application.details.icon.localIcon.apply {
                if (originalPath != null) {
                    originalPath!!.toFile()
                        .copyTo(Path.of(solutionProject.outputPathString, DIRECTORY_STAGE, path).toFile(), true)
                }
            }
            application.details.iconSet.forEach {
                if (it.localIcon.originalPath != null) {
                    it.localIcon.originalPath!!.toFile().copyTo(
                        Path.of(solutionProject.outputPathString, DIRECTORY_STAGE, it.localIcon.path).toFile(),
                        true
                    )
                }
            }
        }
    }

    private fun copySolutionCommandFiles() {
        solutionProject.apkPath.toFile().copyTo(
            Path.of(
                solutionProject.outputPathString,
                DIRECTORY_SQUASH,
                solutionProject.apkPath.name
            ).toFile(), overwrite = true
        )
        if (solutionProject.solutionManager.configuration.includeConfiguration) {
            solutionProject.configPath.toFile().copyTo(
                Path.of(
                    solutionProject.outputPathString,
                    DIRECTORY_STAGE,
                    solutionProject.solutionManager.configuration.archiveDataPath
                ).toFile(), overwrite = true
            )
        }
        for (application in solutionProject.applicationService.applications) {
            application.details.icon.localIcon.apply {
                if (originalPath != null) {
                    val targetPath = Path.of(solutionProject.outputPathString, DIRECTORY_STAGE, path)
                    Files.createDirectories(targetPath.parent)
                    originalPath!!.toFile().copyTo(targetPath.toFile(), true)
                }
            }
            application.details.iconSet.forEach {
                if (it.localIcon.originalPath != null) {
                    val targetPath = Path.of(solutionProject.outputPathString, DIRECTORY_STAGE, it.localIcon.path)
                    Files.createDirectories(targetPath.parent)
                    it.localIcon.originalPath!!.toFile().copyTo(targetPath.toFile(), true)
                }
            }
        }
    }


    private fun packageSolution() {
        SpuExecutor(solutionProject.outputPathString).apply {
            executeSpuSquash()
            // executeSpuVerity() // TODO after beta7
            executeSpuTar()
            if (solutionProject.signingKeyPath != null) {
                executeSpuSignTar(solutionProject.signingKeyPath!!)
            }
            executeSpuFimPak(
                solutionProject.solutionManager.solutionDetails.solutionDescription.vendor,
                solutionProject.solutionManager.solutionDetails.solutionDescription.name,
                solutionProject.solutionManager.solutionDetails.solutionDescription.description,
                solutionProject.solutionManager.solutionDetails.solutionDescription.version
            )
            executeSpuFimBdl(
                solutionProject.apkPath.nameWithoutExtension,
                solutionProject.solutionManager.solutionDetails.solutionDescription.vendor,
                solutionProject.solutionManager.solutionDetails.solutionDescription.name,
                solutionProject.solutionManager.solutionDetails.solutionDescription.description,
                solutionProject.solutionManager.solutionDetails.solutionDescription.version,
                solutionProject.solutionManager.solutionDetails.solutionId,
                solutionProject.solutionBundleSerial,
                solutionProject.solutionManager.solutionDetails.solutionDescription.supportEmail,
                solutionProject.solutionManager.solutionDetails.solutionDescription.supportPhone,
                solutionProject.solutionManager.solutionDetails.solutionDescription.supportUrl
            )
        }
    }

    private fun packageHpk(connector: Connector) {
        val outputFile: File = solutionProject.outputHpkPath?.toFile()
            ?: throw KotlinNullPointerException("output hpk file path is empty.")
        ZipOutputStream(outputFile.outputStream().buffered()).use { zipOutputStream ->
            /**
             * TODO faster-xml make Exception : should we change to other xml provider?? for now, I just add VM options to avoid this.
             * Unable to make field private final long java.util.UUID.mostSigBits accessible: module java.base does not "opens java.util" to unnamed module @378fd1ac
             * java.lang.reflect.InaccessibleObjectException: Unable to make field private final long java.util.UUID.mostSigBits accessible: module java.base does not "opens java.util" to unnamed module @378fd1ac
             * to fix:
             * --add-opens java.base/java.util=ALL-UNNAMED
             */
            zipOutputStream.putNextEntry(ZipEntry(Connector.XML_FILENAME))
            connector.writeTo(zipOutputStream)
            zipOutputStream.closeEntry()

            zipOutputStream.putNextEntry(ZipEntry(solutionProject.apkPath.toFile().name))
            FileInputStream(solutionProject.apkPath.toFile()).copyTo(zipOutputStream)
            zipOutputStream.closeEntry()

            if (solutionProject.solutionManager.configuration.includeConfiguration) {
                zipOutputStream.putNextEntry(ZipEntry(solutionProject.configPath.toFile().name))
                FileInputStream(solutionProject.configPath.toFile()).copyTo(zipOutputStream)
                zipOutputStream.closeEntry()
            }
            zipOutputStream.close()
        }
    }
}
