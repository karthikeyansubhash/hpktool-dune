package com.hp.workpath.pkgmgt.util.utilities.apk

import brut.androlib.Androlib
import brut.androlib.ApkDecoder
import brut.androlib.ApktoolProperties
import brut.androlib.res.AndrolibResources
import com.hp.workpath.pkgmgt.util.utilities.*
import org.simpleframework.xml.core.Persister
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.io.path.absolutePathString

import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

class ApkParser(apkFile: File) {
    companion object {
        private var isFrameworkDirectoryEmptied = false
    }

    private var androidPackageName: String = ""
    private var mainActivityPlatformPath: String = ""
    private var mainActivityName: String = ""
    private val activityIconMap: MutableMap<String, Path> = mutableMapOf()
    private val activityIconSetMap: MutableMap<String, MutableMap<String, Path>> = mutableMapOf()
    private val activityTitleMap: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
    private val activityDescriptionMap: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    init {
        /** empty framework directory only once.
         *  empty framework directory before decoding apk to copy and use latest android-framework file.*/
        if (!isFrameworkDirectoryEmptied) {
            Androlib().emptyFrameworkDirectory()
            isFrameworkDirectoryEmptied = true
        }
        /** 1. unpack apk */
        val outDir: File = Path.of(Paths.get("").absolutePathString(), DIR_TEMP_APK).toFile()
        val decoder = ApkDecoder()
        decoder.setDecodeSources(ApkDecoder.DECODE_SOURCES_NONE)
        decoder.setOutDir(outDir)
        decoder.setForceDelete(true)
        decoder.setApkFile(apkFile)
        // change library log level
        setOf<String>(
            Androlib::class.java.name,
            AndrolibResources::class.java.name,
            ApktoolProperties::class.java.name,
        ).forEach {
            Logger.getLogger(it).level = Level.SEVERE
        }
        try {
            decoder.decode()
        } catch (e: Exception) {
            throw e
        } finally {
            decoder.close()
        }

        /** 2. open and parse androidManifest.xml */
        val manifest: Manifest = Persister().read(
            Manifest::class.java,
            Path.of(Paths.get("").absolutePathString(), DIR_TEMP_APK, FILE_ANDROID_MANIFEST).toFile()
        )

        /** 3. set activity-resource maps */
        setMainActivityPlatformPath(manifest)
        setActivityIconMap(manifest)
        setActivityTitleMap(manifest)
        setActivityDescriptionMap(manifest)
    }

    private fun setMainActivityPlatformPath(manifest: Manifest) {
        val packageName = manifest.packageName
        androidPackageName = packageName

        val mainActivity = findMainActivityFromXml()

        var activityName = mainActivity ?: ""

        if (activityName.startsWith(".")) {
            activityName = packageName + activityName
        }

        mainActivityPlatformPath = "$packageName/$activityName"
        mainActivityName = activityName
    }

    private fun findMainActivityFromXml(): String? {
        return try {
            val tempDir = Path.of(Paths.get("").absolutePathString(), DIR_TEMP_APK)
            val manifestFile = tempDir.resolve("AndroidManifest.xml").toFile()

            if (!manifestFile.exists()) {
                return null
            }

            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(manifestFile)
            val activities = doc.getElementsByTagName("activity")

            for (i in 0 until activities.length) {
                val activity = activities.item(i) as Element
                val activityName = activity.getAttribute("android:name")

                val intentFilters = activity.getElementsByTagName("intent-filter")

                for (j in 0 until intentFilters.length) {
                    val intentFilter = intentFilters.item(j) as Element

                    val actions = extractActions(intentFilter)
                    val categories = extractCategories(intentFilter)

                    val hasMainAction = actions.contains("android.intent.action.MAIN")
                    val hasLauncherCategory = categories.contains("android.intent.category.LAUNCHER")

                    if (hasMainAction && hasLauncherCategory) {
                        return activityName
                    }
                }
            }

            null
        } catch (e: Exception) {
            e.message
            null
        }
    }

    private fun extractActions(intentFilter: Element): List<String> {
        val actions = mutableListOf<String>()
        val actionNodes = intentFilter.getElementsByTagName("action")

        for (i in 0 until actionNodes.length) {
            val action = actionNodes.item(i) as Element
            val actionName = action.getAttribute("android:name")
            if (actionName.isNotEmpty()) {
                actions.add(actionName)
            }
        }

        return actions
    }

    private fun extractCategories(intentFilter: Element): List<String> {
        val categories = mutableListOf<String>()
        val categoryNodes = intentFilter.getElementsByTagName("category")

        for (i in 0 until categoryNodes.length) {
            val category = categoryNodes.item(i) as Element
            val categoryName = category.getAttribute("android:name")
            if (categoryName.isNotEmpty()) {
                categories.add(categoryName)
            }
        }

        return categories
    }

    private fun setActivityIconMap(manifest: Manifest) {
        getManifestIconMap(manifest).forEach { (key, value) ->
            val path: Path? = findIconResourceFile(value.substringAfter(APK_PARSER_DRAWABLE_SLASH))
            if (path != null) {
                activityIconMap[key] = path
            }
            val iconSet = findIconSetResourceFile(value.substringAfter(APK_PARSER_DRAWABLE_SLASH))
            if (iconSet.isNotEmpty()) {
                activityIconSetMap[key] = iconSet
            }
        }
    }

    private fun getManifestIconMap(manifest: Manifest): Map<String, String> {
        val manifestIconMap = mutableMapOf<String, String>()
        if (manifest.application.icon.isNotEmpty()) {
            manifestIconMap[MANIFEST_APPLICATION] = manifest.application.icon
        }
        for (activity in manifest.application.activity) {
            if (activity.icon.isNotEmpty()) {
                manifestIconMap[activity.activityName] = activity.icon
            }
            for (meta in activity.metaData) {
                if (meta.metaDataName == MANIFEST_META_ICON && meta.resource.isNotEmpty()) {
                    manifestIconMap[activity.activityName] = meta.resource
                }
            }
        }
        return manifestIconMap
    }

    private fun findIconResourceFile(fileName: String): Path? {
        val resDirectory = getResourcePath().toFile()
        resDirectory.listFiles { _, name -> name.contains(DIR_DRAWABLE, true) }?.forEach { drawable ->
            drawable.listFiles { dir, name -> dir.isDirectory && name.contains(fileName, true) }?.forEach { file ->
                if (file.isFile && checkIconFileExtension(file)) {
                    return file.toPath().toAbsolutePath()
                }
            }
        }
        // Error: could not find resource file
        return null
    }

    private fun findIconSetResourceFile(fileName: String): MutableMap<String, Path> {
        val ret = mutableMapOf<String, Path>()
        val resDirectory = getResourcePath().toFile()
        resDirectory.listFiles { _, name -> name.contains(DIR_DRAWABLE, true) }?.forEach { drawable ->
            drawable.listFiles { dir, name -> dir.isDirectory && name.contains(fileName, true) }?.forEach { file ->
                if (file.isFile && checkIconFileExtension(file)) {
                    if (drawable.name.contains("md", true)) {
                        ret["90x90"] = file.toPath().toAbsolutePath()
                    } else if (drawable.name.contains("lg", true)) {
                        ret["140x140"] = file.toPath().toAbsolutePath()
                    } else if (drawable.name.contains("xl", true)) {
                        ret["179x179"] = file.toPath().toAbsolutePath()
                    }
                }
            }
        }
        return ret
    }

    private fun setActivityTitleMap(manifest: Manifest) {
        getManifestTitleMap(manifest).forEach { (key, value) ->
            val localizedStringMap: MutableMap<String, String> =
                if (value.startsWith(APK_PARSER_AT_STRING, true)) {
                    getLocalizedMap(value.substringAfter(APK_PARSER_STRING_SLASH)).ifEmpty {
                        mutableMapOf(Pair(LANGUAGE_TAG_EN_US, value))
                    }
                } else {
                    mutableMapOf(Pair(LANGUAGE_TAG_EN_US, value))
                }
            activityTitleMap[key] = localizedStringMap
        }
    }

    private fun getManifestTitleMap(manifest: Manifest): Map<String, String> {
        val manifestTitleMap = mutableMapOf<String, String>()
        if (manifest.application.label.isNotEmpty()) {
            manifestTitleMap[MANIFEST_APPLICATION] = manifest.application.label
        }
        for (activity in manifest.application.activity) {
            if (activity.label.isNotEmpty()) {
                manifestTitleMap[activity.activityName] = activity.label
            }
            for (meta in activity.metaData) {
                if (meta.metaDataName == MANIFEST_META_TITLE && meta.resource.isNotEmpty()) {
                    manifestTitleMap[activity.activityName] = meta.resource
                }
            }
        }
        return manifestTitleMap
    }

    private fun setActivityDescriptionMap(manifest: Manifest) {
        getManifestDescriptionMap(manifest).forEach { (key, value) ->
            val localizedStringMap: MutableMap<String, String> =
                if (value.startsWith(APK_PARSER_AT_STRING, true)) {
                    getLocalizedMap(value.substringAfter(APK_PARSER_STRING_SLASH)).ifEmpty {
                        mutableMapOf(Pair(LANGUAGE_TAG_EN_US, value))
                    }
                } else {
                    mutableMapOf(Pair(LANGUAGE_TAG_EN_US, value))
                }
            activityDescriptionMap[key] = localizedStringMap
        }
    }

    private fun getManifestDescriptionMap(manifest: Manifest): Map<String, String> {
        val manifestDescriptionMap = mutableMapOf<String, String>()
        if (manifest.application.label.isNotEmpty()) {
            manifestDescriptionMap[MANIFEST_APPLICATION] = manifest.application.label
        }
        for (activity in manifest.application.activity) {
            if (activity.label.isNotEmpty()) {
                manifestDescriptionMap[activity.activityName] = activity.label
            }
            for (meta in activity.metaData) {
                if (meta.metaDataName == MANIFEST_META_DESCRIPTION && meta.resource.isNotEmpty()) {
                    manifestDescriptionMap[activity.activityName] = meta.resource
                }
            }
        }
        return manifestDescriptionMap
    }

    private fun getLocalizedMap(resourceName: String): MutableMap<String, String> {
        val localizedStringMap: MutableMap<String, String> = mutableMapOf()
        val resDirectory = getResourcePath().toFile()
        resDirectory.listFiles { _, name -> name.contains(DIR_VALUES, true) }?.forEach { values ->
            values.listFiles { dir, name -> dir.isDirectory && name.contains(APK_PARSER_STRINGS_XML, true) }
                ?.forEach { file ->
                    if (file.isFile) {
                        val resourceXml: Resource = Persister().read(Resource::class.java, file)
                        resourceXml.resourceString.forEach { resourceString ->
                            if (resourceString.name.equals(resourceName, true)) {
                                if (values.name.equals(DIR_VALUES, true)) { // values directory for default en-US value.
                                    if (localizedStringMap.containsKey(LANGUAGE_TAG_EN_US).not()) {
                                        localizedStringMap[LANGUAGE_TAG_EN_US] = resourceString.value
                                    }
                                }
                                val tag = values.name.substringAfter(APK_PARSER_VALUES_BAR).replace("-r", "-")
                                if (getLocalizations().contains(tag)) {
                                    localizedStringMap[tag] = resourceString.value
                                }
                            }
                        }
                    }
                }
        }
        return localizedStringMap
    }

    fun getAndroidPackageName(): String {
        return androidPackageName
    }

    fun getMainActivityPlatformPath(): String {
        return mainActivityPlatformPath
    }

    fun getMainActivityName(): String {
        return mainActivityName
    }

    /**
     * Get absolute path of icon file by input applicationPath
     * @return absolute path of activity icon if input name contains activity name. Or path of application. Or null.
     */
    fun getIconPath(applicationPath: String): Path? {
        if (applicationPath.isNotEmpty()) {
            for (activityIconPath in activityIconMap) {
                if (applicationPath.substringAfterLast('.').contains(activityIconPath.key.substringAfterLast('.'))) {
                    return activityIconPath.value
                }
            }
        }
        if (activityIconMap.contains(MANIFEST_APPLICATION)) {
            return activityIconMap[MANIFEST_APPLICATION]
        }
        return null
    }

    fun getIconSetPath(applicationPath: String): MutableMap<String, Path>? {
        if (applicationPath.isNotEmpty()) {
            for (activityIconSet in activityIconSetMap) {
                if (applicationPath.substringAfterLast('.').contains(activityIconSet.key.substringAfterLast('.'))) {
                    return activityIconSet.value
                }
            }
        }
        if (activityIconSetMap.contains(MANIFEST_APPLICATION)) {
            return activityIconSetMap[MANIFEST_APPLICATION]
        }
        return null
    }

    fun getTitle(applicationPath: String): MutableMap<String, String>? {
        if (applicationPath.isNotEmpty()) {
            for (activityTitle in activityTitleMap) {
                if (applicationPath.substringAfterLast('.').contains(activityTitle.key.substringAfterLast('.'))) {
                    return activityTitle.value
                }
            }
        }
        if (activityTitleMap.contains(MANIFEST_APPLICATION)) {
            return activityTitleMap[MANIFEST_APPLICATION]
        }
        return null
    }

    fun getDescription(applicationPath: String): MutableMap<String, String>? {
        if (applicationPath.isNotEmpty()) {
            for (activityDescription in activityDescriptionMap) {
                if (applicationPath.substringAfterLast('.').contains(activityDescription.key.substringAfterLast('.'))) {
                    return activityDescription.value
                }
            }
        }
        if (activityDescriptionMap.contains(MANIFEST_APPLICATION)) {
            return activityDescriptionMap[MANIFEST_APPLICATION]
        }
        return null
    }

    private fun getResourcePath(): Path {
        return Path.of(Paths.get("").absolutePathString(), DIR_TEMP_APK, DIR_RES)
    }
}