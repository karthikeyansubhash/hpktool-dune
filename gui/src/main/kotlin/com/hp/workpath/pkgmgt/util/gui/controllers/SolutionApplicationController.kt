package com.hp.workpath.pkgmgt.util.gui.controllers

import com.hp.workpath.pkgmgt.util.gui.models.ApplicationInfo
import com.hp.workpath.pkgmgt.util.gui.models.DataSource
import com.hp.workpath.pkgmgt.util.gui.utilities.ActionType
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import com.hp.workpath.pkgmgt.util.utilities.apk.ApkParser
import com.hp.workpath.pkgmgt.util.utilities.isValidUuid
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.File
import java.nio.file.Path
import java.util.*

class SolutionApplicationController : Controller() {
    @FXML
    private lateinit var vbBackground: VBox

    @FXML
    private lateinit var tfApplicationId: TextField

    @FXML
    private lateinit var tfApplicationName: TextField

    @FXML
    private lateinit var tfApplicationPath: TextField

    @FXML
    private lateinit var tfApplicationTitle: TextField
    private var applicationTitleMap = mutableMapOf<String, String>()
        set(value) {
            field = value
            tfApplicationTitle.text = field["en-US"]
            updateTextFieldEmpty(tfApplicationTitle)
        }

    @FXML
    private lateinit var lbTitleStatus: Label

    private var titleSource: DataSource = DataSource.DATA_NULL
        set(value) {
            lbTitleStatus.text = when (value) {
                DataSource.DATA_NULL -> MESSAGE.getString("label_empty")
                DataSource.DATA_FROM_APK -> MESSAGE.getString("label_title_source_apk")
                DataSource.DATA_FROM_USER -> MESSAGE.getString("label_title_source_user")
            }
            field = value
        }

    @FXML
    private lateinit var tfApplicationDescription: TextField
    private var applicationDescriptionMap = mutableMapOf<String, String>()
        set(value) {
            field = value
            tfApplicationDescription.text = field["en-US"]
            updateTextFieldEmpty(tfApplicationDescription)
        }

    @FXML
    private lateinit var lbDescriptionStatus: Label

    private var descriptionSource: DataSource = DataSource.DATA_NULL
        set(value) {
            lbDescriptionStatus.text = when (value) {
                DataSource.DATA_NULL -> MESSAGE.getString("label_empty")
                DataSource.DATA_FROM_APK -> MESSAGE.getString("label_description_source_apk")
                DataSource.DATA_FROM_USER -> MESSAGE.getString("label_description_source_user")
            }
            field = value
        }

    @FXML
    private lateinit var ivApplicationIcon: ImageView

    private var applicationIconFile: File? = null
        set(value) {
            field = value
            ivApplicationIcon.image = if (field != null) {
                Image(field!!.toURI().toString())
            } else {
                null
            }
        }

    @FXML
    private lateinit var lbIconStatus: Label

    private var iconSource: DataSource = DataSource.DATA_NULL
        set(value) {
            lbIconStatus.text = when (value) {
                DataSource.DATA_NULL -> MESSAGE.getString("label_empty")
                DataSource.DATA_FROM_APK -> MESSAGE.getString("label_icon_source_apk")
                DataSource.DATA_FROM_USER -> MESSAGE.getString("label_icon_source_user")
            }
            field = value
        }

    private var applicationIconFiles: MutableMap<String, File> = mutableMapOf()
        set(value) {
            field = value
            // When is empty one of iconSet
            if (field.isNotEmpty() && applicationIconFile == null) {
                applicationIconFile = field.values.first()
            }
        }

    var applicationType = ApplicationInfo.ApplicationType.MAIN
        set(value) {
            field = value
            when (field) {
                ApplicationInfo.ApplicationType.MAIN -> {
                    setVisibility(hbApplicationType, true)
                    cbApplicationHome.isSelected = false
                    cbApplicationHomeDefault.isDisable = true
                    cbApplicationHomeDefault.isSelected = false
                }

                ApplicationInfo.ApplicationType.SUB -> {
                    setVisibility(hbApplicationType, false)
                }

                ApplicationInfo.ApplicationType.HOME -> {
                    setVisibility(hbApplicationType, true)
                    cbApplicationHome.isSelected = true
                    cbApplicationHomeDefault.isDisable = false
                    cbApplicationHomeDefault.isSelected = false
                }

                ApplicationInfo.ApplicationType.HOME_DEFAULT -> {
                    setVisibility(hbApplicationType, true)
                    cbApplicationHome.isSelected = true
                    cbApplicationHomeDefault.isDisable = false
                    cbApplicationHomeDefault.isSelected = true
                }
            }
        }

    @FXML
    private lateinit var hbApplicationType: HBox

    @FXML
    private lateinit var cbApplicationHome: CheckBox

    @FXML
    private lateinit var cbApplicationHomeDefault: CheckBox

    @FXML
    private lateinit var btAdd: Button

    @FXML
    private lateinit var btCancel: Button

    var mainWindow: SolutionController? = null
        set(value) {
            field = value
            initView()
        }

    private var applicationInfo: ApplicationInfo? = null

    fun setApplicationInfo(input: ApplicationInfo) {
        btAdd.text = MESSAGE.getString("btn_save")
        applicationInfo = input
        tfApplicationId.text = input.applicationId
        tfApplicationName.text = input.applicationName
        tfApplicationPath.text = input.applicationPath
        applicationTitleMap = input.applicationTitle
        titleSource = input.applicationTitleSource
        applicationDescriptionMap = input.applicationDescription
        descriptionSource = input.applicationDescriptionSource
        if (input.applicationIconPath != null) {
            applicationIconFile = input.applicationIconPath!!.toFile()
        }
        iconSource = input.applicationIconSource
        applicationIconFiles = input.applicationIconSet.mapValues { it.value.toFile() } as MutableMap<String, File>
        applicationType = input.applicationType

        updateEnableState()
    }

    private var apkParser: ApkParser? = null

    fun setApkParser(input: ApkParser?) {
        apkParser = input
    }

    private fun initView() {
        initListener()
        updateEnableState()
    }

    private fun initListener() {
        hasErrors.clear()
        hasErrors[tfApplicationId] = !isValidUuid(tfApplicationId.text)
        hasErrors[tfApplicationName] = tfApplicationName.text.isNullOrEmpty()
        hasErrors[tfApplicationPath] = tfApplicationPath.text.isNullOrEmpty()
        hasErrors[tfApplicationTitle] = tfApplicationTitle.text.isNullOrEmpty()
        hasErrors[tfApplicationDescription] = tfApplicationDescription.text.isNullOrEmpty()
        tfApplicationId.textProperty().addListener { _, _, newValue ->
            updateTextFieldError(tfApplicationId, !isValidUuid(newValue))
        }
        tfApplicationName.textProperty().addListener { _, _, _ ->
            updateTextFieldEmpty(tfApplicationName)
        }
        tfApplicationPath.textProperty().addListener { _, _, newValue ->
            updateTextFieldEmpty(tfApplicationPath)
            if (apkParser != null) {
                if (titleSource != DataSource.DATA_FROM_USER) {
                    val apkTitleMap = apkParser!!.getTitle(newValue)
                    if (apkTitleMap != null) {
                        applicationTitleMap = apkTitleMap
                        titleSource = DataSource.DATA_FROM_APK
                    }
                }
                if (descriptionSource != DataSource.DATA_FROM_USER) {
                    val apkDescriptionMap = apkParser!!.getDescription(newValue)
                    if (apkDescriptionMap != null) {
                        applicationDescriptionMap = apkDescriptionMap
                        descriptionSource = DataSource.DATA_FROM_APK
                    }
                }
                if (iconSource != DataSource.DATA_FROM_USER) {
                    val iconPath = apkParser!!.getIconPath(newValue)
                    if (iconPath != null) {
                        applicationIconFile = iconPath.toFile()
                        iconSource = DataSource.DATA_FROM_APK
                    }
                    val iconSet = apkParser!!.getIconSetPath(newValue)
                    if (iconSet != null) {
                        applicationIconFiles = iconSet.mapValues { it.value.toFile() } as MutableMap<String, File>
                        iconSource = DataSource.DATA_FROM_APK
                    }
                }
            }
        }
        tfApplicationTitle.textProperty().addListener { _, _, newValue ->
            updateTextFieldEmpty(tfApplicationTitle)
            applicationTitleMap["en-US"] = newValue
            titleSource = DataSource.DATA_FROM_USER
        }
        tfApplicationDescription.textProperty().addListener { _, _, newValue ->
            updateTextFieldEmpty(tfApplicationDescription)
            applicationDescriptionMap["en-US"] = newValue
            descriptionSource = DataSource.DATA_FROM_USER
        }
    }

    override fun updateEnableState() {
        hasErrors.forEach { (_, value) ->
            if (value) {
                btAdd.isDisable = true
                return
            }
        }
        btAdd.isDisable = false
    }


    @FXML
    fun handleApplicationIdGenerate(event: ActionEvent) {
        tfApplicationId.text = UUID.randomUUID().toString()
        updateTextFieldError(tfApplicationId, !isValidUuid(tfApplicationId.text))
    }

    @FXML
    fun handleApplicationTitleClear(event: ActionEvent) {
        if (apkParser != null) {
            val apkTitleMap = apkParser!!.getTitle(tfApplicationPath.text)
            if (apkTitleMap != null) {
                applicationTitleMap = apkTitleMap
                titleSource = DataSource.DATA_FROM_APK
                return
            }
        }
        applicationTitleMap = mutableMapOf()
        titleSource = DataSource.DATA_NULL
    }

    @FXML
    fun handleApplicationTitle(event: ActionEvent) {
        openLocalizationController(
            vbBackground.scene.window,
            MESSAGE.getString("label_application_title"),
            applicationTitleMap
        ) { value ->
            if (applicationTitleMap != value) {
                applicationTitleMap = value
                titleSource = DataSource.DATA_FROM_USER
            }
        }
    }

    @FXML
    fun handleApplicationDescriptionClear(event: ActionEvent) {
        if (apkParser != null) {
            val apkDescriptionMap = apkParser!!.getDescription(tfApplicationPath.text)
            if (apkDescriptionMap != null) {
                applicationDescriptionMap = apkDescriptionMap
                descriptionSource = DataSource.DATA_FROM_APK
                return
            }
        }
        applicationDescriptionMap = mutableMapOf()
        descriptionSource = DataSource.DATA_NULL
    }

    @FXML
    fun handleApplicationDescription(event: ActionEvent) {
        openLocalizationController(
            vbBackground.scene.window,
            MESSAGE.getString("label_application_description"),
            applicationDescriptionMap
        ) { value ->
            if (applicationDescriptionMap != value) {
                applicationDescriptionMap = value
                descriptionSource = DataSource.DATA_FROM_USER
            }
        }
    }

    @FXML
    fun handleApplicationIconClear(event: ActionEvent) {
        applicationIconFile = null
        iconSource = DataSource.DATA_NULL
        if (apkParser != null) {
            val iconPath = apkParser!!.getIconPath(tfApplicationPath.text)
            if (iconPath != null) {
                applicationIconFile = iconPath.toFile()
                iconSource = DataSource.DATA_FROM_APK
            }
            val iconSet = apkParser!!.getIconSetPath(tfApplicationPath.text)
            if (iconSet != null) {
                applicationIconFiles = iconSet.mapValues { it.value.toFile() } as MutableMap<String, File>
                iconSource = DataSource.DATA_FROM_APK
            }
        }
    }

    @FXML
    fun handleApplicationIcon(event: ActionEvent) {
        openApplicationIconSetController()
    }

    private fun openApplicationIconSetController() {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/solution_icon_set_controller.fxml"))
            loader.resources = MESSAGE
            val newWindow: Parent = loader.load()
            val scene = Scene(newWindow)
            val stage: Stage = Stage().also {
                it.title = MESSAGE.getString("label_application_icon")
                it.initModality(Modality.WINDOW_MODAL)
                it.initOwner(vbBackground.scene.window)
                it.icons.add(Image(javaClass.getResourceAsStream("/images/hp.png")))
                it.scene = scene
                it.sizeToScene()
            }
            (loader.getController() as SolutionIconSetController).apply {
                setStage(stage, ActionType.SOLUTION_NEW)
                setValue(this, applicationIconFile, applicationIconFiles) { file, files ->
                    if (applicationIconFile != file) {
                        applicationIconFile = file
                        iconSource = DataSource.DATA_FROM_USER
                    }
                    if (applicationIconFiles != files) {
                        iconSource = DataSource.DATA_FROM_USER
                    }
                    applicationIconFiles = files
                }
                stage.show()
                stage.minWidth = 300.0
                stage.minHeight = 300.0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @FXML
    fun handleApplicationHomeCheckBox(event: ActionEvent) {
        applicationType = if (cbApplicationHome.isSelected) {
            ApplicationInfo.ApplicationType.HOME
        } else {
            ApplicationInfo.ApplicationType.MAIN
        }
    }

    @FXML
    fun handleApplicationHomeDefaultCheckBox(event: ActionEvent) {
        applicationType = if (cbApplicationHomeDefault.isSelected) {
            ApplicationInfo.ApplicationType.HOME_DEFAULT
        } else {
            ApplicationInfo.ApplicationType.HOME
        }
    }

    @FXML
    fun handleAddApplication(event: ActionEvent) {
        var isNew = false
        if (applicationInfo == null) {
            applicationInfo = ApplicationInfo()
            isNew = true
        }
        val applicationInfos: MutableList<ApplicationInfo> = mainWindow!!.applicationList.toMutableList()
        if ((applicationType == ApplicationInfo.ApplicationType.HOME || applicationType == ApplicationInfo.ApplicationType.HOME_DEFAULT) && applicationInfos.size > 1) {
            applicationInfo = null
            showMessagePopup(
                State.WARNING,
                MESSAGE.getString("dialog_title_warning"),
                MESSAGE.getString("dialog_header_application_home_sub"),
                MESSAGE.getString("dialog_content_application_home_sub")
            )
            return
        }
        if (!isNew) {
            applicationInfos.remove(applicationInfo)
        }
        applicationInfos.forEach {
            if (it.applicationId == tfApplicationId.text || it.applicationPath == tfApplicationPath.text) {
                applicationInfo = null
                showMessagePopup(
                    State.WARNING,
                    MESSAGE.getString("dialog_title_warning"),
                    MESSAGE.getString("dialog_header_application_duplicate"),
                    MESSAGE.getString("dialog_content_application_duplicate")
                )
                return
            }
        }
        applicationInfo!!.applicationId = tfApplicationId.text
        applicationInfo!!.applicationName = tfApplicationName.text
        applicationInfo!!.applicationPath = tfApplicationPath.text
        applicationInfo!!.applicationTitle = applicationTitleMap
        applicationInfo!!.applicationTitleSource = titleSource
        applicationInfo!!.applicationDescription = applicationDescriptionMap
        applicationInfo!!.applicationDescriptionSource = descriptionSource
        applicationInfo!!.applicationIconPath = applicationIconFile?.toPath()?.toAbsolutePath()
        applicationInfo!!.applicationIconSource = iconSource
        applicationInfo!!.applicationIconSet =
            applicationIconFiles.mapValues { it.value.toPath().toAbsolutePath() } as MutableMap<String, Path>
        applicationInfo!!.applicationType = applicationType

        mainWindow!!.setApplication(applicationInfo!!, isNew)
        closeSubController(btAdd)
    }

    @FXML
    fun handleCancelApplication(event: ActionEvent) {
        closeSubController(btCancel)
    }
}