package com.hp.workpath.pkgmgt.util.gui.controllers

import com.hp.ext.types.solutionManager.NotificationType
import com.hp.workpath.pkgmgt.lib.HpkFile
import com.hp.workpath.pkgmgt.util.gui.currentHpkVersion
import com.hp.workpath.pkgmgt.util.gui.currentPlatformVersion
import com.hp.workpath.pkgmgt.util.gui.models.ApplicationInfo
import com.hp.workpath.pkgmgt.util.gui.models.DataSource
import com.hp.workpath.pkgmgt.util.gui.services.WorkpathSolutionProjectServiceGui
import com.hp.workpath.pkgmgt.util.gui.services.WorkpathSolutionProjectServiceType
import com.hp.workpath.pkgmgt.util.gui.utilities.*
import com.hp.workpath.pkgmgt.util.models.BaseModel
import com.hp.workpath.pkgmgt.util.models.SolutionProject
import com.hp.workpath.pkgmgt.util.models.application.ApplicationAgentModel
import com.hp.workpath.pkgmgt.util.models.application.ApplicationCategory
import com.hp.workpath.pkgmgt.util.models.application.ApplicationIconDetailModel
import com.hp.workpath.pkgmgt.util.models.connection.TaskState
import com.hp.workpath.pkgmgt.util.models.copyJob.CopyJobAgentModel
import com.hp.workpath.pkgmgt.util.models.deviceUsage.DeviceUsageAgentModel
import com.hp.workpath.pkgmgt.util.models.hpk.WebServiceEndPoint
import com.hp.workpath.pkgmgt.util.models.hpk.WebServiceInfo
import com.hp.workpath.pkgmgt.util.models.hpk.WorkpathPlatformVersion
import com.hp.workpath.pkgmgt.util.models.messageCenter.MessageCenterAgentModel
import com.hp.workpath.pkgmgt.util.models.printJob.PrintJobAgentModel
import com.hp.workpath.pkgmgt.util.models.scanJob.ScanJobAgentModel
import com.hp.workpath.pkgmgt.util.models.security.SecurityAgentModel
import com.hp.workpath.pkgmgt.util.models.solutionManager.SolutionDiagnosticsModel
import com.hp.workpath.pkgmgt.util.models.solutionManager.SolutionNotificationAgentModel
import com.hp.workpath.pkgmgt.util.models.statisticsJob.StatisticsJobAgentModel
import com.hp.workpath.pkgmgt.util.models.supplies.SuppliesAgentModel
import com.hp.workpath.pkgmgt.util.models.usbAccessories.UsbAccessoriesAgentModel
import com.hp.workpath.pkgmgt.util.models.usbAccessories.UsbRegistrationModel
import com.hp.workpath.pkgmgt.util.utilities.*
import com.hp.workpath.pkgmgt.util.utilities.apk.ApkParser
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Callback
import org.w3c.dom.Text
import java.io.File
import java.util.*
import kotlin.io.path.name

class SolutionController : Controller() {
    @FXML
    private lateinit var spBackground: ScrollPane
    private var scrollLocation: Double = 0.0

    private fun saveScrollLocation() {
        scrollLocation = spBackground.vvalue
    }

    private fun restoreScrollLocation() {
        if (scrollLocation != 0.0) {
            spBackground.vvalue = scrollLocation
            scrollLocation = 0.0
        }
    }

    @FXML
    private lateinit var vbBackground: VBox

    @FXML
    private lateinit var cbComponentAuthentication: CheckBox

    @FXML
    private lateinit var cbComponentAccessory: CheckBox

    @FXML
    private lateinit var cbComponentCopyAgent: CheckBox

    @FXML
    private lateinit var cbComponentPrintAgent: CheckBox

    @FXML
    private lateinit var cbComponentScanAgent: CheckBox

    @FXML
    private lateinit var cbComponentDeviceUsage: CheckBox

    @FXML
    private lateinit var cbComponentStatistics: CheckBox

    @FXML
    private lateinit var cbComponentSupplies: CheckBox

    @FXML
    private lateinit var cbComponentWebService: CheckBox

    @FXML
    private lateinit var btSolutionTab: Button

    @FXML
    private lateinit var btAccessoryTab: Button

    @FXML
    private lateinit var btCopyAgentTab: Button

    @FXML
    private lateinit var btPrintAgentTab: Button

    @FXML
    private lateinit var btScanAgentTab: Button

    @FXML
    private lateinit var btDeviceUsageTab: Button

    @FXML
    private lateinit var btStatisticsTab: Button

    @FXML
    private lateinit var btSuppliesTab: Button

    @FXML
    private lateinit var btWebServiceTab: Button

    @FXML
    private lateinit var btBuildOptionTab: Button

    private var btCurrentTab: Button? = null

    @FXML
    private lateinit var vbSolutionBox: VBox

    @FXML
    private lateinit var vbApplicationBox: VBox

    @FXML
    private lateinit var vbAuthenticationBox: VBox

    @FXML
    private lateinit var vbAccessoryBox: VBox

    @FXML
    private lateinit var vbCopyAgentBox: VBox

    @FXML
    private lateinit var vbPrintAgentBox: VBox

    @FXML
    private lateinit var vbScanAgentBox: VBox

    @FXML
    private lateinit var vbDeviceUsageBox: VBox

    @FXML
    private lateinit var vbStatisticsBox: VBox

    @FXML
    private lateinit var vbSuppliesBox: VBox

    @FXML
    private lateinit var vbWebServiceBox: VBox

    @FXML
    private lateinit var vbBuildOptionBox: VBox

    @FXML
    private lateinit var tfSolutionApk: TextField
    private var solutionApkFile: File? = null
    private var apkParser: ApkParser? = null

    @FXML
    private lateinit var tfSolutionConfig: TextField
    private var defaultConfigFile: File? = null

    @FXML
    private lateinit var tfSolutionId: TextField

    @FXML
    private lateinit var tfSolutionName: TextField

    @FXML
    private lateinit var tfSolutionDescription: TextField

    @FXML
    private lateinit var tfSolutionVendor: TextField

    @FXML
    private lateinit var tfSolutionVersion: TextField

    @FXML
    private lateinit var tfSolutionDate: TextField

    @FXML
    private lateinit var cbSolutionPlatformVersion: ComboBox<WorkpathPlatformVersion>
    private var observablePlatformVersionList = FXCollections.observableArrayList<WorkpathPlatformVersion>()

    @FXML
    private lateinit var tfSolutionVersionNumber : TextField

    @FXML
    private lateinit var tfSolutionEmail: TextField

    @FXML
    private lateinit var tfSolutionPhone: TextField

    @FXML
    private lateinit var tfSolutionUrl: TextField

    @FXML
    private lateinit var btApplicationAdd: Button

    @FXML
    private lateinit var tvApplication: TableView<ApplicationInfo>
    var applicationList = mutableListOf<ApplicationInfo>()
    private var observableApplicationList = FXCollections.observableArrayList<ApplicationInfo>()

    inner class ApplicationDetailButtonCell : TableCell<ApplicationInfo, Boolean>() {
        private val cellButton: Button = Button().also {
            it.id = BUTTON_DETAIL
            it.styleClass.add(BUTTON_DETAIL_STYLE)
            it.onAction = EventHandler { _ ->
                openApplicationController(this.tableView.items[this.index])
            }
        }

        override fun updateIndex(i: Int) {
            super.updateIndex(i)
            graphic = if (i < observableApplicationList.size) {
                cellButton
            } else {
                null
            }
        }
    }

    inner class ApplicationDeleteButtonCell : TableCell<ApplicationInfo, Boolean>() {
        private val cellButton: Button = Button().also {
            it.id = BUTTON_DELETE
            it.styleClass.add(BUTTON_DELETE_STYLE)
            it.onAction = EventHandler { event ->
                val info: ApplicationInfo = this.tableView.items[this.index]
                if (info.applicationType != ApplicationInfo.ApplicationType.SUB && this.tableView.items.size > 1) {
                    showMessagePopup(
                        State.WARNING,
                        MESSAGE.getString("dialog_title_error"),
                        "",
                        MESSAGE.getString("dialog_content_solution_delete_sub_app_first")
                    )
                    return@EventHandler
                }
                val deleteButtonClickListener: ButtonClickListener = object : ButtonClickListener {
                    override fun ok() {
                        deleteApplication(info)
                    }

                    override fun cancel() {
                        event.consume()
                    }
                }
                showOkCancelPopup(
                    State.WARNING,
                    MESSAGE.getString("dialog_title_confirmation"),
                    MESSAGE.getString("dialog_header_solution_delete_check"),
                    "",
                    deleteButtonClickListener
                )
            }
        }

        override fun updateIndex(i: Int) {
            super.updateIndex(i)
            graphic = if (i < observableApplicationList.size) {
                cellButton
            } else {
                null
            }
        }
    }


    @FXML
    private lateinit var tcApplicationId: TableColumn<ApplicationInfo, String>

    @FXML
    private lateinit var tcApplicationName: TableColumn<ApplicationInfo, String>

    @FXML
    private lateinit var tcApplicationPath: TableColumn<ApplicationInfo, String>

    @FXML
    private lateinit var tcApplicationType: TableColumn<ApplicationInfo, String>

    @FXML
    private lateinit var tcApplicationDetail: TableColumn<ApplicationInfo, Boolean>

    @FXML
    private lateinit var tcApplicationDelete: TableColumn<ApplicationInfo, Boolean>

    @FXML
    private lateinit var tfAuthenticationId: TextField

    @FXML
    private lateinit var tfAuthenticationName: TextField

    @FXML
    private lateinit var tfAuthenticationPath: TextField

    @FXML
    private lateinit var cbAuthenticationPrePrompt: CheckBox

    @FXML
    private lateinit var tfAuthenticationTitle: TextField
    private var authenticationTitleMap = mutableMapOf<String, String>()

    @FXML
    private lateinit var tfAuthenticationDescription: TextField
    private var authenticationDescriptionMap = mutableMapOf<String, String>()

    @FXML
    private lateinit var btAccessoryAdd: Button

    @FXML
    private lateinit var tvAccessory: TableView<UsbRegistrationModel>
    var accessoryList = mutableListOf<UsbRegistrationModel>()
    private var observableAccessoryList = FXCollections.observableArrayList<UsbRegistrationModel>()

    private val agentModels = BaseModel()

    inner class AccessoryDetailButtonCell : TableCell<UsbRegistrationModel, Boolean>() {
        private val cellButton: Button = Button().also {
            it.id = BUTTON_DETAIL
            it.styleClass.add(BUTTON_DETAIL_STYLE)
            it.onAction = EventHandler { _ ->
                openAccessoryController(this.tableView.items[this.index])
            }
        }

        override fun updateIndex(i: Int) {
            super.updateIndex(i)
            graphic = if (i < observableAccessoryList.size) {
                cellButton
            } else {
                null
            }
        }
    }

    inner class AccessoryDeleteButtonCell : TableCell<UsbRegistrationModel, Boolean>() {
        private val cellButton: Button = Button().also {
            it.id = BUTTON_DELETE
            it.styleClass.add(BUTTON_DELETE_STYLE)
            it.onAction = EventHandler { event ->
                val model: UsbRegistrationModel = this.tableView.items[this.index]
                val deleteButtonClickListener: ButtonClickListener = object : ButtonClickListener {
                    override fun ok() {
                        deleteAccessory(model)
                    }

                    override fun cancel() {
                        event.consume()
                    }
                }
                showOkCancelPopup(
                    State.WARNING,
                    MESSAGE.getString("dialog_title_confirmation"),
                    MESSAGE.getString("dialog_header_solution_delete_check"),
                    "",
                    deleteButtonClickListener
                )
            }
        }

        override fun updateIndex(i: Int) {
            super.updateIndex(i)
            graphic = if (i < observableAccessoryList.size) {
                cellButton
            } else {
                null
            }
        }
    }

    @FXML
    private lateinit var tcAccessoryType: TableColumn<UsbRegistrationModel, String>

    @FXML
    private lateinit var tcAccessoryVendorId: TableColumn<UsbRegistrationModel, String>

    @FXML
    private lateinit var tcAccessoryProductId: TableColumn<UsbRegistrationModel, String>

    @FXML
    private lateinit var tcAccessoryDetail: TableColumn<UsbRegistrationModel, Boolean>

    @FXML
    private lateinit var tcAccessoryDelete: TableColumn<UsbRegistrationModel, Boolean>

    @FXML
    private lateinit var cbEnableCopyAgent: CheckBox

    @FXML
    private lateinit var cbEnablePrintAgent: CheckBox

    @FXML
    private lateinit var cbEnableScanAgent: CheckBox

    @FXML
    private lateinit var cbEnableDeviceUsage: CheckBox

    @FXML
    private lateinit var cbEnableSupplies: CheckBox

    @FXML
    private lateinit var cbEnableStatistics: CheckBox

    @FXML
    private lateinit var cbEnableStatisticsCriticalSolution: CheckBox

    @FXML
    private lateinit var tfWebServiceTitle: TextField
    private var webServiceTitleMap = mutableMapOf<String, String>()

    @FXML
    private lateinit var tfWebServiceDescription: TextField
    private var webServiceDescriptionMap = mutableMapOf<String, String>()

    @FXML
    private lateinit var btWebServiceAdd: Button

    @FXML
    private lateinit var tvWebservice: TableView<WebServiceEndPoint>
    var webServiceList = mutableListOf<WebServiceEndPoint>()
    private var observableWebServiceList = FXCollections.observableArrayList<WebServiceEndPoint>()

    inner class WebServiceDetailButtonCell : TableCell<WebServiceEndPoint, Boolean>() {
        private val cellButton: Button = Button().also {
            it.id = BUTTON_DETAIL
            it.styleClass.add(BUTTON_DETAIL_STYLE)
            it.onAction = EventHandler { _ ->
                openWebServiceController(this.tableView.items[this.index])
            }
        }

        override fun updateIndex(i: Int) {
            super.updateIndex(i)
            graphic = if (i < observableWebServiceList.size) {
                cellButton
            } else {
                null
            }
        }
    }

    inner class WebServiceDeleteButtonCell : TableCell<WebServiceEndPoint, Boolean>() {
        private val cellButton: Button = Button().also {
            it.id = BUTTON_DELETE
            it.styleClass.add(BUTTON_DELETE_STYLE)
            it.onAction = EventHandler { event ->
                val model: WebServiceEndPoint = this.tableView.items[this.index]
                val deleteButtonClickListener: ButtonClickListener = object : ButtonClickListener {
                    override fun ok() {
                        deleteWebService(model)
                    }

                    override fun cancel() {
                        event.consume()
                    }
                }
                showOkCancelPopup(
                    State.WARNING,
                    MESSAGE.getString("dialog_title_confirmation"),
                    MESSAGE.getString("dialog_header_solution_delete_check"),
                    "",
                    deleteButtonClickListener
                )
            }
        }

        override fun updateIndex(i: Int) {
            super.updateIndex(i)
            graphic = if (i < observableWebServiceList.size) {
                cellButton
            } else {
                null
            }
        }
    }

    @FXML
    private lateinit var tcWebserviceMethod: TableColumn<WebServiceEndPoint, String>

    @FXML
    private lateinit var tcWebserviceCategory: TableColumn<WebServiceEndPoint, String>

    @FXML
    private lateinit var tcWebserviceAbsolutePath: TableColumn<WebServiceEndPoint, String>

    @FXML
    private lateinit var tcWebserviceAuth: TableColumn<WebServiceEndPoint, String>

    @FXML
    private lateinit var tcWebserviceDetail: TableColumn<WebServiceEndPoint, Boolean>

    @FXML
    private lateinit var tcWebserviceDelete: TableColumn<WebServiceEndPoint, Boolean>

    @FXML
    private lateinit var tfBundleSerial: TextField

    @FXML
    private lateinit var hbSignTar: HBox

    @FXML
    private lateinit var cbSignTar: CheckBox

    @FXML
    private lateinit var tfSigningKey: TextField
    private var signingKeyFile: File? = null

    @FXML
    private lateinit var smbCreate: SplitMenuButton

    @FXML
    private lateinit var miCreateHpk: MenuItem

    @FXML
    private lateinit var piProgress: ProgressIndicator

    @FXML
    private lateinit var lbSolutionBundleCreateResult: Label

    private val USER_INFORMATION = listOf(
        "EMAIL_ADDRESS",
        "USER_NAME",
        "USER_DOMAIN",
        "AUTH_TYPE",
        "FQ_USER_NAME",
        "DISPLAY_NAME",
        "AUTH_AGENT_ID",
        "HOME_FOLDER"
    )

    //lateinit val norificationType: NotificationType

    private fun setSolutionBundleCreateResultText(text: String, isError: Boolean) {
        lbSolutionBundleCreateResult.text = text
        lbSolutionBundleCreateResult.styleClass.remove(LABEL_ERROR)
        if (isError) {
            lbSolutionBundleCreateResult.styleClass.add(LABEL_ERROR)
        }
    }

    private var solutionProject = SolutionProject()

    override fun setStage(stage: Stage, actionType: ActionType) {
        super.setStage(stage, actionType)
        initView(actionType)
    }

    private fun initView(actionType: ActionType) {
        when (actionType) {
            ActionType.SOLUTION_NEW -> {
                initNewView()
            }

            ActionType.OPEN -> {
                val bdlFile = openFile(stage, FileExtension.BUNDLE, FileChooserMode.OPEN)
                if (bdlFile == null) {
                    initNewView()
                } else {
                    initOpen(bdlFile)
                }
            }

            ActionType.OPEN_HPK -> {
                val hpkFile = openFile(stage, FileExtension.HPK, FileChooserMode.OPEN)
                if (hpkFile == null) {
                    initNewView()
                } else {
                    initOpenHpk(hpkFile)
                }
            }

            else -> throw IllegalArgumentException(actionType.name)
        }
    }

    private fun initNewView() {
        initNodes()
    }

    private fun initOpen(input: File) {
        try {
            setSolutionBundleCreateResultText(MESSAGE.getString("task_loading"), false)
            setBackgroundProgress(true)
            Thread {
                try {
                    deleteTempDirectory()
                    val bundleExtractor = BundleExtractor(input)
                    apkParser = bundleExtractor.apkParser
                    solutionProject = bundleExtractor.solutionProject
                    solutionProject?.applicationService?.applications?.forEach { appModel ->
                        try {
                            if (appModel.details.platform.isNullOrEmpty()) {
                                // 만약 platform이 null이면, 기본값 설정
                                appModel.details.platform = "ApWorkpath"
                            }
                        } catch (e: Exception) {
                            // platform 처리 중 예외 발생해도 무시
                        }
                    }
                    Platform.runLater {
                        initNodes(solutionProject)
                        setSolutionBundleCreateResultText("", false)
                        setBackgroundProgress(false)
                    }
                } catch (exception: Exception) {
                    Platform.runLater {
                        handleBackgroundException(exception)
                        initNodes()
                    }
                }
            }.start()
        } catch (exception: Exception) {
            handleBackgroundException(exception)
            initNodes()
        }
    }

    private fun initOpenHpk(input: File) {
        try {
            setSolutionBundleCreateResultText(MESSAGE.getString("task_loading"), false)
            setBackgroundProgress(true)
            Thread {
                try {
                    deleteTempDirectory()
                    val hpkConverter = HpkConverter(HpkFile(input))
                    apkParser = hpkConverter.apkParser
                    solutionProject = hpkConverter.solutionProject

                    Platform.runLater {
                        initNodes(solutionProject)
                        setSolutionBundleCreateResultText("", false)
                        setBackgroundProgress(false)
                    }
                } catch (exception: Exception) {
                    Platform.runLater {
                        handleBackgroundException(exception)
                        initNodes()
                    }
                }
            }.start()
        } catch (exception: Exception) {
            handleBackgroundException(exception)
            initNodes()
        }
    }

    private fun handleBackgroundException(exception: Exception) {
        showMessagePopup(
            State.ERROR,
            MESSAGE.getString("dialog_title_error"),
            MESSAGE.getString("dialog_header_failed"),
            "${exception.message}"
        )
        setSolutionBundleCreateResultText("${MESSAGE.getString("dialog_header_failed")}${exception.message}", true)
        setBackgroundProgress(false)
        exception.printStackTrace()
    }

    private fun initVariables() {
        accessoryList.clear()
        agentModels.clear()
    }

    private fun initNodes() {
        observablePlatformVersionList.setAll(WorkpathPlatformVersion.entries)
        cbSolutionPlatformVersion.items = observablePlatformVersionList
        cbSolutionPlatformVersion.selectionModel.select(currentPlatformVersion)
        observableApplicationList.setAll(applicationList)
        observableAccessoryList.setAll(accessoryList)
        observableWebServiceList.setAll(webServiceList)
        initVariables()
        initListener()
        initTable()
        updateEnableState()
    }

    private fun initNodes(input: SolutionProject) {
        initNodes()
        solutionProject = input
        solutionProject.apply {
//            outputPathString
            solutionApkFile = apkPath.toFile()
            tfSolutionApk.text = solutionApkFile?.name
//            apkPath // set on upper method
//            configPath  // set on solutionProject.solutionManager.configuration
//            signingKeyPath
//            solutionBundleSerial
            solutionManager.apply {
                solutionDetails.apply {
                    tfSolutionId.text = solutionId
                    solutionDescription.apply {
                        tfSolutionDescription.text = description
                        tfSolutionName.text = name
                        tfSolutionEmail.text = supportEmail
                        tfSolutionPhone.text = supportPhone
                        tfSolutionUrl.text = supportUrl
                        tfSolutionVendor.text = vendor
                        tfSolutionVersion.text = version
                        tfSolutionDate.text = date
                        tfSolutionVersionNumber.text = versionNumber.toString()
                    }
                }
                workpathPlatformPackage.apply {
//                    workpathPackagePath
                    currentPlatformVersion = platformVersion
                    cbSolutionPlatformVersion.selectionModel.select(currentPlatformVersion)
//                    installFile
                }
                configuration.apply {
//                    archiveDataPath
//                    key
//                    mime
                    if (includeConfiguration) {
                        defaultConfigFile = configPath.toFile()
                        tfSolutionConfig.text = defaultConfigFile?.name
                    }
                }
            }
            applicationService.apply {
                i18nAssets.forEach {
//                    TODO()
                }
                applications.forEach {
                    applicationList.add(ApplicationInfo().apply {
                        applicationPath = it.target.workpathPackage
                        applicationType = if (it.target.isMainApplication) {
                            if (it.details.category == ApplicationCategory.HomeScreen) {
                                if (it.details.setAsDefault) {
                                    ApplicationInfo.ApplicationType.HOME_DEFAULT
                                } else {
                                    ApplicationInfo.ApplicationType.HOME
                                }
                            } else {
                                ApplicationInfo.ApplicationType.MAIN
                            }
                        } else {
                            ApplicationInfo.ApplicationType.SUB
                        }
                        applicationId = it.details.applicationId
                        applicationName = it.details.name
                        applicationTitle = applicationService.getLocalizedStringFromI18nAsset(
                            it.details.title.i18nAssetId,
                            it.details.title.stringId
                        )
                        applicationTitleSource =
                            if (it.details.isTitleFromUser) DataSource.DATA_FROM_USER else DataSource.DATA_FROM_APK
                        applicationDescription = applicationService.getLocalizedStringFromI18nAsset(
                            it.details.description.i18nAssetId,
                            it.details.description.stringId
                        )
                        applicationDescriptionSource =
                            if (it.details.isDescriptionFromUser) DataSource.DATA_FROM_USER else DataSource.DATA_FROM_APK
                        applicationIconPath = it.details.icon.localIcon.originalPath
                        applicationIconSource =
                            if (it.details.isIconFromUser) DataSource.DATA_FROM_USER else DataSource.DATA_FROM_APK
                        it.details.iconSet.forEach { icon ->
                            if (icon.localIcon.originalPath != null) {
                                applicationIconSet[icon.key] = icon.localIcon.originalPath!!
                            }
                        }
                    })
                }
                refreshApplicationTable()
//                i18nAssets
            }
            authenticationService.authenticationAgent.apply {
                if (includeAuthenticationAgent) {
                    cbComponentAuthentication.isSelected = true
                    enableAuthenticationAgent(true)
                    tfAuthenticationId.text = agentId
                    tfAuthenticationName.text = name
                    tfAuthenticationPath.text = workpathPackage
                    cbAuthenticationPrePrompt.isSelected = enablePrePrompt
                    authenticationTitleMap = applicationService.getLocalizedStringFromI18nAsset(
                        title.i18nAssetId,
                        title.stringId
                    )
                    tfAuthenticationTitle.text = authenticationTitleMap["en-US"]
                    authenticationDescriptionMap = applicationService.getLocalizedStringFromI18nAsset(
                        description.i18nAssetId,
                        description.stringId
                    )
                    tfAuthenticationDescription.text = authenticationDescriptionMap["en-US"]
                }
                agentModels.add(this.javaClass.name, this)
            }
            accessoriesService.usbAccessoriesAgent.apply {
                if (includeUsbAccessoriesAgent) {
                    cbComponentAccessory.isSelected = true
                    enableAgent(cbComponentAccessory, true)
//                    agentId
//                    name
//                    title
//                    description
//                    registrationTarget
                    accessoryList.addAll(registrations)
                    refreshAccessoryTable()
                }
                agentModels.add(this.javaClass.name, this)
            }
            copyJobService.copyJobAgent.apply {
                if (includeCopyJobAgent) {
                    cbComponentCopyAgent.isSelected = true
                    enableAgent(cbComponentCopyAgent, true)
                    cbEnableCopyAgent.isSelected = includeCopyJobAgent
//                    agentId
//                    name
//                    title
//                    description
                    // Auto-enable scan agent when copy agent is included (one-way dependency)
                    if (!cbComponentScanAgent.isSelected) {
                        cbComponentScanAgent.isSelected = true
                        enableAgent(cbComponentScanAgent, true)
                        cbEnableScanAgent.isSelected = true
                    }
                }
                agentModels.add(this.javaClass.name, this)
            }
            printJobService.printJobAgent.apply {
                if (includePrintJobAgent) {
                    cbComponentPrintAgent.isSelected = true
                    enableAgent(cbComponentPrintAgent, true)
                    cbEnablePrintAgent.isSelected = includePrintJobAgent
//                    agentId
//                    name
//                    title
//                    description
                }
                agentModels.add(this.javaClass.name, this)
            }
            scanJobService.scanJobAgent.apply {
                if (includeScanJobAgent) {
                    cbComponentScanAgent.isSelected = true
                    enableAgent(cbComponentScanAgent, true)
                    cbEnableScanAgent.isSelected = includeScanJobAgent
//                    agentId
//                    name
//                    title
//                    description
                }
                agentModels.add(this.javaClass.name, this)
            }
            deviceUsageService.deviceUsageAgent.apply {
                if (includeDeviceUsageAgent) {
                    cbComponentDeviceUsage.isSelected = true
                    enableAgent(cbComponentDeviceUsage, true)
                    cbEnableDeviceUsage.isSelected = includeDeviceUsageAgent
//                    agentId
//                    name
//                    title
//                    description
                }
                agentModels.add(this.javaClass.name, this)
            }
            statisticsJobService.statisticsAgent.apply {
                if(includeStatisticsJobAgent){
                    cbComponentStatistics.isSelected = true
                    enableAgent(cbComponentStatistics, true)
                    cbEnableStatistics.isSelected = includeStatisticsJobAgent
                    cbEnableStatisticsCriticalSolution.isSelected = criticalSolution
                }
                agentModels.add(this.javaClass.name, this)
            }
            suppliesService.suppliesAgent.apply {
                if (includeSuppliesAgent) {
                    cbComponentSupplies.isSelected = true
                    enableAgent(cbComponentSupplies, true)
                    cbEnableSupplies.isSelected = includeSuppliesAgent
//                    agentId
//                    name
//                    title
//                    description
                }
                agentModels.add(this.javaClass.name, this)
            }
            webService.webServiceAgent.apply {
                if (includeWebServiceInfo) {
                    cbComponentWebService.isSelected = true
                    enableAgent(cbComponentWebService, true)
//                    uuid
                    webServiceTitleMap = getMapFromLocalizedArray(titles)
                    if (webServiceTitleMap.containsKey(LANGUAGE_TAG_EN_US)) {
                        tfWebServiceTitle.text = webServiceTitleMap[LANGUAGE_TAG_EN_US]
                    }
                    webServiceDescriptionMap = getMapFromLocalizedArray(descriptions)
                    if (webServiceDescriptionMap.containsKey(LANGUAGE_TAG_EN_US)) {
                        tfWebServiceDescription.text = webServiceDescriptionMap[LANGUAGE_TAG_EN_US]
                    }
                    webServiceList.addAll(webServiceEndPoints)
                    refreshWebServiceTable()
                }
                agentModels.add(this.javaClass.name, this)
            }
            securityService.securityAgent.apply {
                agentModels.add(this.javaClass.name, this)
            }
            notificationService.notificationAgent.apply {
                agentModels.add(this.javaClass.name, this)
            }
            solutionDiagnosticsService.solutionDiagnosticsAgent.apply {
                agentModels.add(this.javaClass.name, this)
            }

            tfBundleSerial.text = solutionBundleSerial
        }
    }

    private fun initListener() {
        hasErrors.clear()
        hasErrors[tfSolutionApk] = tfSolutionApk.text.isNullOrEmpty()
        hasErrors[tfSolutionId] = !isValidUuid(tfSolutionId.text)
        hasErrors[tfSolutionName] = tfSolutionName.text.isNullOrEmpty()
        hasErrors[tfSolutionDescription] = tfSolutionDescription.text.isNullOrEmpty()
        hasErrors[tfSolutionVendor] = tfSolutionVendor.text.isNullOrEmpty()
        hasErrors[tfSolutionVersion] = tfSolutionVersion.text.isNullOrEmpty()
        hasErrors[tfSolutionDate] = !isDateFormatString(tfSolutionDate.text)
        spBackground.vvalueProperty().addListener { _, _, _ ->
            /**
             * When background process call ui enable method(function name) with Platform.runLater,
             * scroll location is changed to 0. So I store location data before enable ui and restore it on here.
             */
            restoreScrollLocation()
        }
        tfSolutionApk.textProperty().addListener { _, _, _ ->
            updateTextFieldEmpty(tfSolutionApk)
        }
        tfSolutionId.textProperty().addListener { _, _, newValue ->
            updateTextFieldError(tfSolutionId, !isValidUuid(newValue))
        }
        tfSolutionName.textProperty().addListener { _, _, _ ->
            updateTextFieldEmpty(tfSolutionName)
        }
        tfSolutionDescription.textProperty().addListener { _, _, _ ->
            updateTextFieldEmpty(tfSolutionDescription)
        }
        tfSolutionVendor.textProperty().addListener { _, _, _ ->
            updateTextFieldEmpty(tfSolutionVendor)
        }
        tfSolutionVersion.textProperty().addListener { _, _, _ ->
            updateTextFieldEmpty(tfSolutionVersion)
        }
        tfSolutionDate.textProperty().addListener { _, _, newValue ->
            updateTextFieldError(tfSolutionDate, !isDateFormatString(newValue))
        }
        if (!cbComponentAuthentication.isSelected) {
            setApplicationListener(true)
            setAuthenticationListener(false)
        } else {
            setApplicationListener(false)
            setAuthenticationListener(true)
        }
        setAccessoryListener(cbComponentAccessory.isSelected)
        setCopyAgentListener(cbComponentCopyAgent.isSelected)
        setPrintAgentListener(cbComponentPrintAgent.isSelected)
        setScanAgentListener(cbComponentScanAgent.isSelected)
        setDeviceUsageListener(cbComponentDeviceUsage.isSelected)
        setStatisticsListener(cbComponentStatistics.isSelected)
        setSuppliesListener(cbComponentSupplies.isSelected)
        setWebServiceListener(cbComponentWebService.isSelected)
        setSignTarListener(cbSignTar.isSelected)
    }

    private val applicationListListener = ListChangeListener<ApplicationInfo> { _ ->
        updateTableViewError(tvApplication as TableView<Any>, false)
    }

    private fun setApplicationListener(selected: Boolean) {
        if (selected) {
            observableApplicationList.addListener(applicationListListener)
        } else {
            hasErrors.remove(tvApplication)
            observableApplicationList.removeListener(applicationListListener)
        }
    }

    private fun setAuthenticationListener(selected: Boolean) {
        if (selected) {
            hasErrors[tfAuthenticationId] = !isValidUuid(tfAuthenticationId.text)
            hasErrors[tfAuthenticationName] = tfAuthenticationName.text.isNullOrEmpty()
            hasErrors[tfAuthenticationPath] = tfAuthenticationPath.text.isNullOrEmpty()
            hasErrors[tfAuthenticationTitle] = tfAuthenticationTitle.text.isNullOrEmpty()
            hasErrors[tfAuthenticationDescription] = tfAuthenticationDescription.text.isNullOrEmpty()
            tfAuthenticationId.textProperty().addListener { _, _, newValue ->
                updateTextFieldError(tfAuthenticationId, !isValidUuid(newValue))
            }
            tfAuthenticationName.textProperty().addListener { _, _, _ ->
                updateTextFieldEmpty(tfAuthenticationName)
            }
            tfAuthenticationPath.textProperty().addListener { _, _, _ ->
                updateTextFieldEmpty(tfAuthenticationPath)
            }
            tfAuthenticationTitle.textProperty().addListener { _, _, newValue ->
                updateTextFieldEmpty(tfAuthenticationTitle)
                authenticationTitleMap["en-US"] = newValue
            }
            tfAuthenticationDescription.textProperty().addListener { _, _, newValue ->
                updateTextFieldEmpty(tfAuthenticationDescription)
                authenticationDescriptionMap["en-US"] = newValue
            }
        } else {
            hasErrors.remove(tfAuthenticationId)
            hasErrors.remove(tfAuthenticationName)
            hasErrors.remove(tfAuthenticationPath)
            hasErrors.remove(tfAuthenticationTitle)
            hasErrors.remove(tfAuthenticationDescription)
            tfAuthenticationId.textProperty().removeListener { _, _, newValue ->
                updateTextFieldError(tfAuthenticationId, !isValidUuid(newValue))
            }
            tfAuthenticationName.textProperty().removeListener { _, _, _ ->
                updateTextFieldEmpty(tfAuthenticationName)
            }
            tfAuthenticationPath.textProperty().removeListener { _, _, _ ->
                updateTextFieldEmpty(tfAuthenticationPath)
            }
            tfAuthenticationTitle.textProperty().removeListener { _, _, newValue ->
                updateTextFieldEmpty(tfAuthenticationTitle)
                authenticationTitleMap["en-US"] = newValue
            }
            tfAuthenticationDescription.textProperty().removeListener { _, _, newValue ->
                updateTextFieldEmpty(tfAuthenticationDescription)
                authenticationDescriptionMap["en-US"] = newValue
            }
        }
    }

    private val accessoryListListener = ListChangeListener<UsbRegistrationModel> { _ ->
        updateTableViewError(tvAccessory as TableView<Any>, observableAccessoryList.isEmpty())
    }

    private fun setAccessoryListener(selected: Boolean) {
        if (selected) {
            hasErrors[tvAccessory] = observableAccessoryList.isEmpty()
            observableAccessoryList.addListener(accessoryListListener)
        } else {
            hasErrors.remove(tvAccessory)
            observableAccessoryList.removeListener(accessoryListListener)
        }
    }

    private fun setCopyAgentListener(selected: Boolean) {
        if (selected) {
            hasErrors[cbEnableCopyAgent] = cbEnableCopyAgent.isSelected.not()
            cbEnableCopyAgent.selectedProperty().addListener { _, _, newValue ->
                updateCheckBoxError(cbEnableCopyAgent, newValue.not())
            }
        } else {
            hasErrors.remove(cbEnableCopyAgent)
            cbEnableCopyAgent.selectedProperty().removeListener { _, _, newValue ->
                updateCheckBoxError(cbEnableCopyAgent, newValue.not())
            }
        }
    }

    private fun setPrintAgentListener(selected: Boolean) {
        if (selected) {
            hasErrors[cbEnablePrintAgent] = cbEnablePrintAgent.isSelected.not()
            cbEnablePrintAgent.selectedProperty().addListener { _, _, newValue ->
                updateCheckBoxError(cbEnablePrintAgent, newValue.not())
            }
        } else {
            hasErrors.remove(cbEnablePrintAgent)
            cbEnablePrintAgent.selectedProperty().removeListener { _, _, newValue ->
                updateCheckBoxError(cbEnablePrintAgent, newValue.not())
            }
        }
    }

    private fun setScanAgentListener(selected: Boolean) {
        if (selected) {
            hasErrors[cbEnableScanAgent] = cbEnableScanAgent.isSelected.not()
            cbEnableScanAgent.selectedProperty().addListener { _, _, newValue ->
                updateCheckBoxError(cbEnableScanAgent, newValue.not())
            }
        } else {
            hasErrors.remove(cbEnableScanAgent)
            cbEnableScanAgent.selectedProperty().removeListener { _, _, newValue ->
                updateCheckBoxError(cbEnableScanAgent, newValue.not())
            }
        }
    }

    private fun setDeviceUsageListener(selected: Boolean) {
        if (selected) {
            hasErrors[cbEnableDeviceUsage] = cbEnableDeviceUsage.isSelected.not()
            cbEnableDeviceUsage.selectedProperty().addListener { _, _, newValue ->
                updateCheckBoxError(cbEnableDeviceUsage, newValue.not())
            }
        } else {
            hasErrors.remove(cbEnableDeviceUsage)
            cbEnableDeviceUsage.selectedProperty().removeListener { _, _, newValue ->
                updateCheckBoxError(cbEnableDeviceUsage, newValue.not())
            }
        }
    }

    private fun setStatisticsListener(selected: Boolean) {
        if (selected) {
            hasErrors[cbEnableStatistics] = cbEnableStatistics.isSelected.not()
            cbEnableStatistics.selectedProperty().addListener { _, _, newValue ->
                updateCheckBoxError(cbEnableStatistics, newValue.not())
            }
        } else {
            hasErrors.remove(cbEnableStatistics)
            cbEnableStatistics.selectedProperty().removeListener { _, _, newValue ->
                updateCheckBoxError(cbEnableStatistics, newValue.not())
            }
        }
    }

    private fun setSuppliesListener(selected: Boolean) {
        if (selected) {
            hasErrors[cbEnableSupplies] = cbEnableSupplies.isSelected.not()
            cbEnableSupplies.selectedProperty().addListener { _, _, newValue ->
                updateCheckBoxError(cbEnableSupplies, newValue.not())
            }
        } else {
            hasErrors.remove(cbEnableSupplies)
            cbEnableSupplies.selectedProperty().removeListener { _, _, newValue ->
                updateCheckBoxError(cbEnableSupplies, newValue.not())
            }
        }
    }

    private val webServiceListListener = ListChangeListener<WebServiceEndPoint> { _ ->
        updateTableViewError(tvWebservice as TableView<Any>, observableWebServiceList.isEmpty())
    }

    private fun setWebServiceListener(selected: Boolean) {
        if (selected) {
            hasErrors[tfWebServiceTitle] = tfWebServiceTitle.text.isNullOrEmpty()
            hasErrors[tfWebServiceDescription] = tfWebServiceTitle.text.isNullOrEmpty()
            hasErrors[tvWebservice] = observableWebServiceList.isEmpty()
            tfWebServiceTitle.textProperty().addListener { _, _, newValue ->
                updateTextFieldEmpty(tfWebServiceTitle)
                webServiceTitleMap["en-US"] = newValue
            }
            tfWebServiceDescription.textProperty().addListener { _, _, newValue ->
                updateTextFieldEmpty(tfWebServiceDescription)
                webServiceDescriptionMap["en-US"] = newValue
            }
            observableWebServiceList.addListener(webServiceListListener)
        } else {
            hasErrors.remove(tfWebServiceTitle)
            hasErrors.remove(tfWebServiceDescription)
            hasErrors.remove(tvWebservice)
            tfWebServiceTitle.textProperty().removeListener { _, _, newValue ->
                updateTextFieldEmpty(tfWebServiceTitle)
                webServiceTitleMap["en-US"] = newValue
            }
            tfWebServiceDescription.textProperty().removeListener { _, _, newValue ->
                updateTextFieldEmpty(tfWebServiceDescription)
                webServiceDescriptionMap["en-US"] = newValue
            }
            observableWebServiceList.removeListener(webServiceListListener)
        }
    }

    private fun setSignTarListener(selected: Boolean) {
        if (selected) {
            hasErrors[tfSigningKey] = tfSigningKey.text.isNullOrEmpty()
            tfSigningKey.textProperty().addListener { _, _, _ ->
                updateTextFieldEmpty(tfSigningKey)
            }
        } else {
            hasErrors.remove(tfSigningKey)
            tfSigningKey.textProperty().removeListener { _, _, _ ->
                updateTextFieldEmpty(tfSigningKey)
            }
        }
        updateEnableState()
    }

    private fun initTable() {
        tvApplication.selectionModel = null
        tcApplicationId.cellValueFactory = PropertyValueFactory("applicationId")
        tcApplicationName.cellValueFactory = PropertyValueFactory("applicationName")
        tcApplicationPath.cellValueFactory = PropertyValueFactory("applicationPath")
        tcApplicationType.cellValueFactory = PropertyValueFactory("applicationType")
        tcApplicationDetail.cellFactory = Callback {
            ApplicationDetailButtonCell()
        }
        tcApplicationDelete.cellFactory = Callback {
            ApplicationDeleteButtonCell()
        }
        tvApplication.items = observableApplicationList

        tvAccessory.selectionModel = null
        tcAccessoryType.cellValueFactory = PropertyValueFactory("registration")
        tcAccessoryVendorId.cellValueFactory = PropertyValueFactory("vendorId")
        tcAccessoryProductId.cellValueFactory = PropertyValueFactory("productId")
        tcAccessoryDetail.cellFactory = Callback {
            AccessoryDetailButtonCell()
        }
        tcAccessoryDelete.cellFactory = Callback {
            AccessoryDeleteButtonCell()
        }
        tvAccessory.items = observableAccessoryList

        tvWebservice.selectionModel = null
        tcWebserviceMethod.cellValueFactory = PropertyValueFactory(WEBSERVICE_METHOD)
        tcWebserviceCategory.cellValueFactory = PropertyValueFactory(WEBSERVICE_CATEGORY)
        tcWebserviceAbsolutePath.cellValueFactory = PropertyValueFactory(WEBSERVICE_ABSOLUTE_PATH)
        tcWebserviceAuth.cellValueFactory = PropertyValueFactory(WEBSERVICE_AUTH_TYPE)
        tcWebserviceDetail.cellFactory = Callback {
            WebServiceDetailButtonCell()
        }
        tcWebserviceDelete.cellFactory = Callback {
            WebServiceDeleteButtonCell()
        }
        tvWebservice.items = observableWebServiceList
    }

    override fun updateEnableState() {
        hasErrors.forEach { (_, value) ->
            if (value) {
                smbCreate.isDisable = true
                miCreateHpk.isDisable = true
                return
            }
        }
        smbCreate.isDisable = false
        miCreateHpk.isDisable = false
    }

    @FXML
    fun handleComponentCheckBox(event: ActionEvent) {
        val selectedCheckBox: CheckBox = event.source as CheckBox
        enableAgent(selectedCheckBox, selectedCheckBox.isSelected)
    }

    private fun enableAgent(source: CheckBox, isEnable: Boolean) {
        val targetButton: Button = when (source) {
            cbComponentAccessory -> {
                setAccessoryListener(isEnable)
                btAccessoryTab
            }

            cbComponentPrintAgent -> {
                setPrintAgentListener(isEnable)
                btPrintAgentTab
            }

            cbComponentScanAgent -> {
                setScanAgentListener(isEnable)
                btScanAgentTab
            }

            cbComponentDeviceUsage -> {
                setDeviceUsageListener(isEnable)
                btDeviceUsageTab
            }
            cbComponentCopyAgent -> {
                setCopyAgentListener(isEnable)
                // When copy agent is enabled, automatically enable scan agent (one-way dependency)
                if (isEnable && !cbComponentScanAgent.isSelected) {
                    cbComponentScanAgent.isSelected = true
                    enableAgent(cbComponentScanAgent, true)
                }
                btCopyAgentTab
            }

            cbComponentStatistics -> {
                setStatisticsListener(isEnable)
                btStatisticsTab
            }

            cbComponentSupplies -> {
                setSuppliesListener(isEnable)
                btSuppliesTab
            }

            cbComponentWebService -> {
                setWebServiceListener(isEnable)
                btWebServiceTab
            }

            else -> throw IllegalArgumentException()
        }
        setVisibility(targetButton, isEnable)
        updateEnableState()
        if (targetButton == btCurrentTab) {
            enableTab(btSolutionTab)
        }
    }

    @FXML
    fun handleAuthenticationCheckBox(event: ActionEvent) {
        enableAuthenticationAgent(cbComponentAuthentication.isSelected)
    }

    private fun enableAuthenticationAgent(isEnable: Boolean) {
        setVisibility(vbAuthenticationBox, isEnable)
        setApplicationListener(isEnable.not())
        setAuthenticationListener(isEnable)
        updateEnableState()
    }

    @FXML
    fun handleComponentButton(event: ActionEvent) {
        enableTab(event.source as Button)
    }

    private fun enableTab(selected: Button) {
        btCurrentTab = selected
        setTabButtonStyle(btCurrentTab!!)
        when (btCurrentTab) {
            btSolutionTab -> setTabVisibility(vbSolutionBox)
            btAccessoryTab -> setTabVisibility(vbAccessoryBox)
            btCopyAgentTab -> setTabVisibility(vbCopyAgentBox)
            btPrintAgentTab -> setTabVisibility(vbPrintAgentBox)
            btScanAgentTab -> setTabVisibility(vbScanAgentBox)
            btDeviceUsageTab -> setTabVisibility(vbDeviceUsageBox)
            btStatisticsTab -> setTabVisibility(vbStatisticsBox)
            btSuppliesTab -> setTabVisibility(vbSuppliesBox)
            btWebServiceTab -> setTabVisibility(vbWebServiceBox)
            btBuildOptionTab -> setTabVisibility(vbBuildOptionBox)
        }
    }

    private fun setTabButtonStyle(selected: Button) {
        listOf<Button>(
            btSolutionTab,
            btAccessoryTab,
            btCopyAgentTab,
            btPrintAgentTab,
            btScanAgentTab,
            btDeviceUsageTab,
            btStatisticsTab,
            btSuppliesTab,
            btWebServiceTab,
            btBuildOptionTab
        ).forEach {
            it.styleClass.clear()
            if (selected == it) {
                it.styleClass.addAll(BUTTON, BUTTON_TAB_SELECTED)
            } else {
                it.styleClass.addAll(BUTTON, BUTTON_TAB)
            }
        }
    }

    private fun setTabVisibility(selected: VBox) {
        listOf<VBox>(
            vbSolutionBox,
            vbAccessoryBox,
            vbCopyAgentBox,
            vbPrintAgentBox,
            vbScanAgentBox,
            vbDeviceUsageBox,
            vbStatisticsBox,
            vbSuppliesBox,
            vbWebServiceBox,
            vbBuildOptionBox
        ).forEach {
            setVisibility(it, false)
        }
        setVisibility(selected, true)
    }

    @FXML
    fun handleSolutionApkSelect(event: ActionEvent) {
        solutionApkFile = openFile(stage, FileExtension.APK, FileChooserMode.OPEN)
        if (solutionApkFile != null) {
            setSolutionBundleCreateResultText(MESSAGE.getString("task_loading_apk"), false)
            setBackgroundProgress(true)
            tfSolutionApk.text = solutionApkFile!!.name
            Thread {
                try {
                    apkParser = ApkParser(solutionApkFile!!)
                    updateApplicationList(apkParser!!)
                    Platform.runLater {
                        setSolutionBundleCreateResultText("", false)
                        setBackgroundProgress(false)
                    }
                } catch (exception: Exception) {
                    Platform.runLater {
                        handleBackgroundException(exception)
                    }
                }
            }.start()
        }
    }

    @FXML
    fun handleSolutionConfigClear(event: ActionEvent) {
        defaultConfigFile = null
        tfSolutionConfig.text = null
    }

    @FXML
    fun handleSolutionConfigSelect(event: ActionEvent) {
        defaultConfigFile = openFile(stage, FileExtension.JSON, FileChooserMode.OPEN)
        if (defaultConfigFile != null) {
            tfSolutionConfig.text = defaultConfigFile!!.name
        }
    }

    @FXML
    fun handleSolutionIdGenerate(event: ActionEvent) {
        tfSolutionId.text = UUID.randomUUID().toString()
        updateTextFieldError(tfSolutionId, !isValidUuid(tfSolutionId.text))
    }

    @FXML
    fun handlePlatformVersionComboBox(event: ActionEvent) {
        currentPlatformVersion = cbSolutionPlatformVersion.selectionModel.selectedItem
        currentHpkVersion = currentPlatformVersion.hpkVersion
    }

    @FXML
    fun handleApplicationAdd(event: ActionEvent) {
        if (applicationList.size < 1 + SUB_APPLICATION_MAX) {
            if (applicationList.isNotEmpty() &&
                (applicationList[0].applicationType == ApplicationInfo.ApplicationType.HOME ||
                        applicationList[0].applicationType == ApplicationInfo.ApplicationType.HOME_DEFAULT)
            ) {
                return
            }
            openApplicationController(null)
        }
    }

    private fun openApplicationController(info: ApplicationInfo?) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/solution_application_controller.fxml"))
            loader.resources = MESSAGE
            val newWindow: Parent = loader.load()
            val scene = Scene(newWindow)
            val stage: Stage = Stage()
                .also {
                    it.title = "${MESSAGE.getString("label_add")} ${MESSAGE.getString("label_application")}"
                    it.initModality(Modality.WINDOW_MODAL)
                    it.initOwner(btApplicationAdd.scene.window)
                    it.icons.add(Image(javaClass.getResourceAsStream("/images/hp.png")))
                    it.scene = scene
                    it.sizeToScene()
                }
            (loader.getController() as SolutionApplicationController).let {
                it.setStage(stage, ActionType.SOLUTION_NEW)
                it.mainWindow = this
                it.applicationType = ApplicationInfo.ApplicationType.MAIN
                applicationList.forEach { applicationInfo ->
                    if (applicationInfo.applicationType == ApplicationInfo.ApplicationType.MAIN) {
                        it.applicationType = ApplicationInfo.ApplicationType.SUB
                        return@forEach
                    }
                }
                if (info != null) {
                    it.setApplicationInfo(info)
                }
                it.setApkParser(apkParser)
            }
            stage.show()
            stage.minWidth = 600.0
            stage.minHeight = 400.0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setApplication(applicationInfo: ApplicationInfo, isNew: Boolean) {
        if (isNew) {
            if (applicationList.size < SUB_APPLICATION_MAX + 1) {
                applicationList.add(applicationInfo)
            } else {
                showMessagePopup(
                    State.ERROR,
                    MESSAGE.getString("dialog_title_error"),
                    MESSAGE.getString("dialog_header_solution_user_cant_access"),
                    ""
                )
            }
        }
        refreshApplicationTable()
    }

    fun deleteApplication(applicationInfo: ApplicationInfo) {
        applicationList.forEach {
            if (it.applicationId == applicationInfo.applicationId
                && it.applicationName == applicationInfo.applicationName
                && it.applicationPath == applicationInfo.applicationPath
                && it.applicationType == applicationInfo.applicationType
            ) {
                applicationList.remove(it)
                refreshApplicationTable()
                return
            }
        }
    }

    private fun updateApplicationList(apkParser: ApkParser) {
        applicationList.forEach { applicationInfo ->
            if (applicationInfo.applicationTitleSource == DataSource.DATA_FROM_APK) {
                applicationInfo.applicationTitle =
                    apkParser.getTitle(applicationInfo.applicationPath) ?: mutableMapOf()
            }
            if (applicationInfo.applicationDescriptionSource == DataSource.DATA_FROM_APK) {
                applicationInfo.applicationDescription =
                    apkParser.getDescription(applicationInfo.applicationPath) ?: mutableMapOf()
            }
            if (applicationInfo.applicationIconSource == DataSource.DATA_FROM_APK) {
                applicationInfo.applicationIconPath =
                    apkParser.getIconPath(applicationInfo.applicationPath)?.toAbsolutePath()
            }
        }
        refreshApplicationTable()
    }

    private fun refreshApplicationTable() {
        saveScrollLocation()
        observableApplicationList.setAll(applicationList)
        btApplicationAdd.isDisable = applicationList.size >= SUB_APPLICATION_MAX + 1
                || (applicationList.size > 0
                && (applicationList[0].applicationType == ApplicationInfo.ApplicationType.HOME
                || applicationList[0].applicationType == ApplicationInfo.ApplicationType.HOME_DEFAULT))
    }

    @FXML
    fun handleAuthenticationIdGenerate(event: ActionEvent) {
        tfAuthenticationId.text = UUID.randomUUID().toString()
        updateTextFieldError(tfAuthenticationId, !isValidUuid(tfAuthenticationId.text))
    }

    @FXML
    fun handleAuthenticationTitle(event: ActionEvent) {
        openLocalizationController(
            vbBackground.scene.window,
            MESSAGE.getString("label_authentication_title"),
            authenticationTitleMap
        ) { value ->
            authenticationTitleMap = value
            tfAuthenticationTitle.text = authenticationTitleMap["en-US"]
            updateTextFieldEmpty(tfAuthenticationTitle)
        }
    }

    @FXML
    fun handleAuthenticationDescription(event: ActionEvent) {
        openLocalizationController(
            vbBackground.scene.window,
            MESSAGE.getString("label_authentication_description"),
            authenticationDescriptionMap
        ) { value ->
            authenticationDescriptionMap = value
            tfAuthenticationDescription.text = authenticationDescriptionMap["en-US"]
            updateTextFieldEmpty(tfAuthenticationDescription)
        }
    }

    @FXML
    fun handleAccessoryAdd(event: ActionEvent) {
        if (accessoryList.size < ACCESSORIES_MAX) {
            openAccessoryController(null)
        }
    }

    private fun openAccessoryController(model: UsbRegistrationModel?) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/solution_accessory_controller.fxml"))
            loader.resources = MESSAGE
            val newWindow: Parent = loader.load()
            val scene = Scene(newWindow)
            val stage: Stage = Stage()
                .also {
                    it.title = "${MESSAGE.getString("label_add")} ${MESSAGE.getString("label_accessory")}"
                    it.initModality(Modality.WINDOW_MODAL)
                    it.initOwner(btApplicationAdd.scene.window)
                    it.icons.add(Image(javaClass.getResourceAsStream("/images/hp.png")))
                    it.scene = scene
                    it.sizeToScene()
                }
            (loader.getController() as SolutionAccessoryController).let {
                it.setStage(stage, ActionType.SOLUTION_NEW)
                it.solutionControllerWindow = this
                if (model != null) {
                    it.setAccessoryModel(model)
                }
                it.initView()
            }
            stage.show()
            stage.minWidth = 600.0
            stage.minHeight = 200.0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setAccessory(usbRegistrationModel: UsbRegistrationModel, isNew: Boolean) {
        if (isNew) {
            if (accessoryList.size < ACCESSORIES_MAX) {
                accessoryList.add(usbRegistrationModel)
            } else {
                showMessagePopup(
                    State.ERROR,
                    MESSAGE.getString("dialog_title_error"),
                    MESSAGE.getString("dialog_header_solution_user_cant_access"),
                    ""
                )
            }
        }
        refreshAccessoryTable()
    }

    fun deleteAccessory(usbRegistrationModel: UsbRegistrationModel) {
        accessoryList.forEach {
            if (it.registration == usbRegistrationModel.registration
                && it.productId == usbRegistrationModel.productId
                && it.vendorId == usbRegistrationModel.vendorId
            ) {
                accessoryList.remove(it)
                refreshAccessoryTable()
                return
            }
        }
    }

    private fun refreshAccessoryTable() {
        saveScrollLocation()
        observableAccessoryList.setAll(accessoryList)
        btAccessoryAdd.isDisable = accessoryList.size >= ACCESSORIES_MAX
    }

    @FXML
    fun handleWebServiceTitle(event: ActionEvent) {
        openLocalizationController(
            vbBackground.scene.window,
            MESSAGE.getString("label_webservice_title"),
            webServiceTitleMap
        ) { value ->
            webServiceTitleMap = value
            tfWebServiceTitle.text = webServiceTitleMap["en-US"]
            updateTextFieldEmpty(tfWebServiceTitle)
        }
    }

    @FXML
    fun handleWebServiceDescription(event: ActionEvent) {
        openLocalizationController(
            vbBackground.scene.window,
            MESSAGE.getString("label_webservice_description"),
            webServiceDescriptionMap
        ) { value ->
            webServiceDescriptionMap = value
            tfWebServiceDescription.text = webServiceDescriptionMap["en-US"]
            updateTextFieldEmpty(tfWebServiceDescription)
        }
    }

    @FXML
    fun handleWebserviceAdd(event: ActionEvent) {
        // TODO webService는 무한하게 등록될 수 있는지?
        openWebServiceController(null)
    }

    private fun openWebServiceController(endPoint: WebServiceEndPoint?) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/solution_webservice_controller.fxml"))
            loader.resources = MESSAGE
            val newWindow: Parent = loader.load()
            val scene = Scene(newWindow)
            val stage: Stage = Stage()
                .also {
                    it.title = "${MESSAGE.getString("label_add")} ${MESSAGE.getString("label_webservice")}"
                    it.initModality(Modality.WINDOW_MODAL)
                    it.initOwner(btApplicationAdd.scene.window)
                    it.icons.add(Image(javaClass.getResourceAsStream("/images/hp.png")))
                    it.scene = scene
                    it.sizeToScene()
                }
            (loader.getController() as SolutionWebServiceController).let {
                it.setStage(stage, ActionType.SOLUTION_NEW)
                it.mainWindow = this
                if (endPoint != null) {
                    it.setWebServiceEndPoint(endPoint)
                }
            }
            stage.show()
            stage.minWidth = 600.0
            stage.minHeight = 200.0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setWebService(endPoint: WebServiceEndPoint, isNew: Boolean) {
        if (isNew) {
            webServiceList.add(endPoint)
        }
        refreshWebServiceTable()
    }

    fun deleteWebService(endPoint: WebServiceEndPoint) {
        webServiceList.forEach {
            if (it.method == endPoint.method
                && it.category == endPoint.category
                && it.absolutePath == endPoint.absolutePath
                && it.authType == endPoint.authType
            ) {
                webServiceList.remove(it)
                refreshWebServiceTable()
                return
            }
        }
    }

    private fun refreshWebServiceTable() {
        saveScrollLocation()
        observableWebServiceList.setAll(webServiceList)
        // TODO webService는 무한하게 등록될 수 있는지?
    }

    @FXML
    fun handleSignTarCheckBox(event: ActionEvent) {
        hbSignTar.isVisible = cbSignTar.isSelected
        setSignTarListener(cbSignTar.isSelected)
    }

    @FXML
    fun handleSigningKeySelect(event: ActionEvent) {
        signingKeyFile = openFile(stage, FileExtension.PEM, FileChooserMode.OPEN)
        if (signingKeyFile != null) {
            tfSigningKey.text = signingKeyFile!!.name
        }
    }

    private fun setBackgroundProgress(isBackground: Boolean) {
        vbBackground.isDisable = isBackground
        if (isBackground) {
            smbCreate.isDisable = true
        } else {
            saveScrollLocation()    // To restore scroll location.
            updateEnableState()
        }
        setVisibility(piProgress, isBackground)
    }

    @FXML
    fun handleSolutionBundleCreate(event: ActionEvent) {
        try {
            val targetDir: File =
                startDirectoryChooser(stage, MESSAGE.getString("dialog_title_select_directory"))
                    ?: throw Exception("Bundle Directory not selected.")
            val workpathSolutionProjectServiceGui =
                WorkpathSolutionProjectServiceGui(
                    getSolutionProject(targetDir),
                    taskInterface,
                    WorkpathSolutionProjectServiceType.CREATE_BUNDLE
                )
            workpathSolutionProjectServiceGui.start()
            setBackgroundProgress(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @FXML
    fun handleHpkBundleCreate(event: ActionEvent) {
        try {
            val hpkFile: File = openFile(stage, FileExtension.HPK, FileChooserMode.SAVE)
                ?: throw Exception("Hpk file not selected.")
            val workpathSolutionProjectServiceGui =
                WorkpathSolutionProjectServiceGui(
                    getSolutionProjectForHpk(hpkFile),
                    taskInterface,
                    WorkpathSolutionProjectServiceType.CREATE_HPK_BUNDLE
                )
            workpathSolutionProjectServiceGui.start()
            setBackgroundProgress(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSolutionProject(targetDir: File): SolutionProject {
        val solutionProject = getSolutionProject()
        solutionProject.outputPathString = targetDir.absolutePath.toString()
        return solutionProject
    }

    private fun getSolutionProjectForHpk(hpkFile: File): SolutionProject {
        val solutionProject = getSolutionProject()
        solutionProject.outputHpkPath = hpkFile.toPath().toAbsolutePath()
        return solutionProject
    }

    private fun getSolutionProject(): SolutionProject {
        val solutionProject = SolutionProject()
        // Preserve i18nAssetId from the loaded solution project if it exists
        if (this.solutionProject.applicationService.i18nAssets.isNotEmpty()) {
            val originalI18nAssetId = this.solutionProject.applicationService.i18nAssets[0].i18nAssetId
            if (originalI18nAssetId.isNotEmpty()) {
                solutionProject.applicationService.defaultI18nAsset.i18nAssetId = originalI18nAssetId
            }
        }
        try {
            // Solution
            if (solutionApkFile != null) {
                if (!solutionApkFile!!.extension.equals(APK_EXTENSION, true)) {
                    throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_solution_apk")}")
                }
                solutionProject.solutionManager.workpathPlatformPackage.workpathPackagePath = solutionApkFile!!.name
                solutionProject.solutionManager.workpathPlatformPackage.installFile = solutionApkFile!!.name
                solutionProject.apkPath = solutionApkFile!!.toPath().toAbsolutePath()
            } else {
                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_solution_apk")}")
            }
/*            if (defaultConfigFile != null) {
                solutionProject.solutionManager.configuration.apply {
                    archiveDataPath = ASSETS_CONFIGS + defaultConfigFile!!.name
                }
                solutionProject.configPath = defaultConfigFile!!.toPath().toAbsolutePath()
            }*/
            defaultConfigFile?.let { configFile ->
                solutionProject.solutionManager.configuration.apply {
                    includeConfiguration = true
                    archiveDataPath = ASSETS_CONFIGS + configFile.name
                    description = ""
                    mimeType = DEFAULT_CONFIG_MIME
                }
                solutionProject.configPath = configFile.toPath().toAbsolutePath()
            } ?: run {
                val defaultConfig = ConfigUtils.getDefaultConfigFile()
                if (defaultConfig != null) {
                    solutionProject.solutionManager.configuration.apply {
                        includeConfiguration = true
                        archiveDataPath = ASSETS_CONFIGS + "default.json"
                        description = "Default configuration"
                        mimeType = DEFAULT_CONFIG_MIME
                    }
                    solutionProject.configPath = defaultConfig.toPath().toAbsolutePath()
                }
            }
            solutionProject.solutionManager.solutionDetails.apply {
                solutionId = tfSolutionId.text
                try {
                    if (!isValidUuid(solutionId)) {
                        throw IllegalArgumentException()
                    }
                } catch (exception: IllegalArgumentException) {
                    throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_solution_id")}")
                }
            }
            with(solutionProject.solutionManager.solutionDetails.solutionDescription) {
                description = tfSolutionDescription.text.ifEmpty {
                    throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_solution_description")}")
                }
                name = tfSolutionName.text.ifEmpty {
                    throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_solution_name")}")
                }
                vendor = tfSolutionVendor.text.ifEmpty {
                    throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_solution_vendor")}")
                }
                version = tfSolutionVersion.text.ifEmpty {
                    throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_solution_version")}")
                }
                supportEmail = if (tfSolutionEmail.text.isNullOrEmpty()) null else tfSolutionEmail.text
                supportPhone = if (tfSolutionPhone.text.isNullOrEmpty()) null else tfSolutionPhone.text
                supportUrl = if (tfSolutionUrl.text.isNullOrEmpty()) null else tfSolutionUrl.text
                versionNumber = tfSolutionVersionNumber.text.ifEmpty {
                    throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_solution_versionNumber")}")
                }.toLong()
                date = tfSolutionDate.text
                try {
                    DATE_FORMAT.parse(date)
                } catch (exception: Exception) {
                    throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_solution_date")}")
                }
            }
            solutionProject.solutionManager.workpathPlatformPackage.platformVersion =
                cbSolutionPlatformVersion.selectionModel.selectedItem
            // Application && Authentication
            if (applicationList.isNotEmpty()) {
                // Application
                applicationList.forEachIndexed { index, applicationInfo ->
                    solutionProject.applicationService.applications.add(
                        ApplicationAgentModel().apply {
                            details.applicationId = applicationInfo.applicationId
                            if (!isValidUuid(details.applicationId)) {
                                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_applications")}")
                            }
                            details.name = applicationInfo.applicationName.ifEmpty {
                                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_applications")}")
                            }
                            target.workpathPackage = applicationInfo.applicationPath.ifEmpty {
                                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_applications")}")
                            }
                            when (applicationInfo.applicationType) {
                                ApplicationInfo.ApplicationType.MAIN -> target.isMainApplication = true
                                ApplicationInfo.ApplicationType.SUB -> {}
                                ApplicationInfo.ApplicationType.HOME -> {
                                    target.isMainApplication = true
                                    details.category = ApplicationCategory.HomeScreen
                                }

                                ApplicationInfo.ApplicationType.HOME_DEFAULT -> {
                                    target.isMainApplication = true
                                    details.category = ApplicationCategory.HomeScreen
                                    details.setAsDefault = true
                                }
                            }
                            details.title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                            details.description.i18nAssetId = details.title.i18nAssetId
                            details.title.stringId = APPLICATION_TITLE_STRING_ID + index.toString()
                            details.description.stringId = APPLICATION_DESCRIPTION_STRING_ID + index.toString()
                            applicationInfo.applicationTitle.forEach { (tag, value) ->
                                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                                    tag,
                                    APPLICATION_TITLE_STRING_ID + index.toString(),
                                    value
                                )
                            }
                            applicationInfo.applicationDescription.forEach { (tag, value) ->
                                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                                    tag,
                                    APPLICATION_DESCRIPTION_STRING_ID + index.toString(),
                                    value
                                )
                            }
                            details.icon.localIcon.originalPath =
                                applicationInfo.applicationIconPath ?: apkParser!!.getIconPath("")
                            if (details.icon.localIcon.originalPath != null) {
                                details.icon.localIcon.fileType = getIconFileType(details.icon.localIcon.originalPath!!)
                                details.icon.localIcon.path = ASSETS_ICONS + details.icon.localIcon.originalPath!!.name
                            }
                            applicationInfo.applicationIconSet.forEach { (key, value) ->
                                details.iconSet.add(ApplicationIconDetailModel().apply {
                                    isInIconSet = true
                                    this.key = key
                                    localIcon.originalPath = value
                                    localIcon.fileType = getIconFileType(localIcon.originalPath!!)
                                    localIcon.path = ASSETS_ICONS + key + '/' + localIcon.originalPath!!.name
                                })
                            }
                        }
                    )
                }
            }

            if (cbComponentAuthentication.isSelected) {
                // Authentication
                solutionProject.authenticationService.authenticationAgent.apply {
                    includeAuthenticationAgent = true
                    agentId = tfAuthenticationId.text
                    if (!isValidUuid(agentId)) {
                        throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_AUTHENTICATION_ID: $agentId")
                    }
                    name = tfAuthenticationName.text.ifEmpty {
                        throw throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_authentication_name")}")
                    }
                    workpathPackage = tfAuthenticationPath.text.ifEmpty {
                        throw throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_authentication_path")}")
                    }
                    if (cbAuthenticationPrePrompt.isSelected) {
                        enablePrePrompt = true
                    }
                    enableSignoutNotification = true
                    title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                    description.i18nAssetId = title.i18nAssetId
                    title.stringId = AUTHENTICATION_TITLE_STRING_ID
                    description.stringId = AUTHENTICATION_DESCRIPTION_STRING_ID
                    authenticationTitleMap.forEach { (tag, value) ->
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            tag,
                            AUTHENTICATION_TITLE_STRING_ID,
                            value
                        )
                    }
                    authenticationDescriptionMap.forEach { (tag, value) ->
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            tag,
                            AUTHENTICATION_DESCRIPTION_STRING_ID,
                            value
                        )
                    }
                }
            }
            // Accessory
            if (cbComponentAccessory.isSelected) {
                accessoryList.ifEmpty {
                    throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_accessories")}")
                }
                solutionProject.accessoriesService.usbAccessoriesAgent.apply {
                    includeUsbAccessoriesAgent = true
                    agentId = (agentModels.get(this.javaClass.name) as? UsbAccessoriesAgentModel)
                        ?.agentId
                        .takeIf { !it.isNullOrEmpty() }
                        ?: UUID.randomUUID().toString()
                    name =
                        solutionProject.solutionManager.solutionDetails.solutionDescription.name + ACCESSORIES_NAME
                    title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                    description.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                    title.stringId = ACCESSORIES_TITLE_STRING_ID
                    description.stringId = ACCESSORIES_DESCRIPTION_STRING_ID
                    solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                        LANGUAGE_TAG_EN_US,
                        ACCESSORIES_TITLE_STRING_ID,
                        name
                    )
                    solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                        LANGUAGE_TAG_EN_US,
                        ACCESSORIES_DESCRIPTION_STRING_ID,
                        name
                    )
                    // androidPackageName is set on apkParser
                    registrationTarget.workpathPlatformClientTargetModel.androidPackageName =
                        apkParser!!.getAndroidPackageName()
                    registrations.addAll(accessoryList)
                }
            }
            // CopyAgent
            if (cbComponentCopyAgent.isSelected) {
                if (cbEnableCopyAgent.isSelected) {
                    solutionProject.copyJobService.copyJobAgent.apply {
                        includeCopyJobAgent = true
                        agentId = (agentModels.get(this.javaClass.name) as? CopyJobAgentModel)
                            ?.agentId
                            .takeIf { !it.isNullOrEmpty() }
                            ?: UUID.randomUUID().toString()
                        name =
                            solutionProject.solutionManager.solutionDetails.solutionDescription.name + COPY_AGENT_NAME
                        title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                        description.i18nAssetId = title.i18nAssetId
                        title.stringId = COPY_AGENT_TITLE_STRING_ID
                        description.stringId = COPY_AGENT_DESCRIPTION_STRING_ID
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            LANGUAGE_TAG_EN_US,
                            COPY_AGENT_TITLE_STRING_ID,
                            name
                        )
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            LANGUAGE_TAG_EN_US,
                            COPY_AGENT_DESCRIPTION_STRING_ID,
                            name
                        )
                        // androidPackageName is set on apkParser
                        copyNotificationTargetModel.workpathPlatformClientTargetModel.androidPackageName =
                            apkParser!!.getAndroidPackageName()
                    }
                }
            }
            // PrintAgent
            if (cbComponentPrintAgent.isSelected) {
                if (cbEnablePrintAgent.isSelected) {
                    solutionProject.printJobService.printJobAgent.apply {
                        includePrintJobAgent = true
                        agentId = (agentModels.get(this.javaClass.name) as? PrintJobAgentModel)
                            ?.agentId
                            .takeIf { !it.isNullOrEmpty() }
                            ?: UUID.randomUUID().toString()
                        name =
                            solutionProject.solutionManager.solutionDetails.solutionDescription.name + PRINT_AGENT_NAME
                        title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                        description.i18nAssetId = title.i18nAssetId
                        title.stringId = PRINT_AGENT_TITLE_STRING_ID
                        description.stringId = PRINT_AGENT_DESCRIPTION_STRING_ID
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            LANGUAGE_TAG_EN_US,
                            PRINT_AGENT_TITLE_STRING_ID,
                            name
                        )
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            LANGUAGE_TAG_EN_US,
                            PRINT_AGENT_DESCRIPTION_STRING_ID,
                            name
                        )
                    }
                }
            }
            // ScanAgent
            if (cbComponentScanAgent.isSelected) {
                if (cbEnableScanAgent.isSelected) {
                    solutionProject.scanJobService.scanJobAgent.apply {
                        includeScanJobAgent = true
                        agentId = (agentModels.get(this.javaClass.name) as? ScanJobAgentModel)
                            ?.agentId
                            .takeIf { !it.isNullOrEmpty() }
                            ?: UUID.randomUUID().toString()
                        name =
                            solutionProject.solutionManager.solutionDetails.solutionDescription.name + SCAN_AGENT_NAME
                        title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                        description.i18nAssetId = title.i18nAssetId
                        title.stringId = SCAN_AGENT_TITLE_STRING_ID
                        description.stringId = SCAN_AGENT_DESCRIPTION_STRING_ID
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            LANGUAGE_TAG_EN_US,
                            SCAN_AGENT_TITLE_STRING_ID,
                            name
                        )
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            LANGUAGE_TAG_EN_US,
                            SCAN_AGENT_DESCRIPTION_STRING_ID,
                            name
                        )
                        // androidPackageName is set on apkParser
                        scanNotificationTargetModel.workpathPlatformClientTargetModel.androidPackageName =
                            apkParser!!.getAndroidPackageName()
                    }
                }
            }
            // Device Usage
            if (cbComponentDeviceUsage.isSelected) {
                if (cbEnableDeviceUsage.isSelected) {
                    solutionProject.deviceUsageService.deviceUsageAgent.apply {
                        includeDeviceUsageAgent = true
                        agentId = (agentModels.get(this.javaClass.name) as? DeviceUsageAgentModel)
                            ?.agentId
                            .takeIf { !it.isNullOrEmpty() }
                            ?: UUID.randomUUID().toString()
                        name =
                            solutionProject.solutionManager.solutionDetails.solutionDescription.name + DEVICE_USAGE_AGENT_NAME
                        title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                        description.i18nAssetId = title.i18nAssetId
                        title.stringId = DEVICE_USAGE_AGENT_TITLE_STRING_ID
                        description.stringId = DEVICE_USAGE_AGENT_DESCRIPTION_STRING_ID
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            LANGUAGE_TAG_EN_US,
                            DEVICE_USAGE_AGENT_TITLE_STRING_ID,
                            name
                        )
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            LANGUAGE_TAG_EN_US,
                            DEVICE_USAGE_AGENT_DESCRIPTION_STRING_ID,
                            name
                        )
                    }
                }
            }
            // Statistics
            if (cbComponentStatistics.isSelected) {
                if (cbEnableStatistics.isSelected) {
                    solutionProject.statisticsJobService.statisticsAgent.apply {
                        includeStatisticsJobAgent = true
                        if (cbEnableStatisticsCriticalSolution.isSelected) {
                            criticalSolution = true
                        } else {
                            criticalSolution = false
                        }
                        agentId = (agentModels.get(this.javaClass.name) as? StatisticsJobAgentModel)
                            ?.agentId
                            .takeIf { !it.isNullOrEmpty() }
                            ?: UUID.randomUUID().toString()
                        name =
                            solutionProject.solutionManager.solutionDetails.solutionDescription.name + STATISTICS_AGENT_NAME
                        title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                        description.i18nAssetId = title.i18nAssetId
                        title.stringId = STATISTICS_AGENT_TITLE_STRING_ID
                        description.stringId = STATISTICS_AGENT_DESCRIPTION_STRING_ID
                        // androidPackageName is set on apkParser
                        statisticsNotificationTargetModel.workpathPlatformClientTargetModel.androidPackageName =
                            apkParser!!.getAndroidPackageName()
                    }
                }
            }
            // Supplies
            if (cbComponentSupplies.isSelected) {
                if (cbEnableSupplies.isSelected) {
                    solutionProject.suppliesService.suppliesAgent.apply {
                        includeSuppliesAgent = true
                        agentId = (agentModels.get(this.javaClass.name) as? SuppliesAgentModel)
                            ?.agentId
                            .takeIf { !it.isNullOrEmpty() }
                            ?: UUID.randomUUID().toString()
                        name =
                            solutionProject.solutionManager.solutionDetails.solutionDescription.name + SUPPLIES_AGENT_NAME
                        title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                        description.i18nAssetId = title.i18nAssetId
                        title.stringId = SUPPLIES_AGENT_TITLE_STRING_ID
                        description.stringId = SUPPLIES_AGENT_DESCRIPTION_STRING_ID
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            LANGUAGE_TAG_EN_US,
                            SUPPLIES_AGENT_TITLE_STRING_ID,
                            name
                        )
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            LANGUAGE_TAG_EN_US,
                            SUPPLIES_AGENT_DESCRIPTION_STRING_ID,
                            name
                        )
                    }
                }
            }
            // WebService
            if (cbComponentWebService.isSelected) {
                webServiceList.ifEmpty {
                    throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${MESSAGE.getString("label_webservice")}")
                }
                solutionProject.webService.webServiceAgent.apply {
                    includeWebServiceInfo = true
                    uuid = (agentModels.get(this.javaClass.name) as? WebServiceInfo)
                        ?.uuid
                        .takeIf { !it.isNullOrEmpty() }
                        ?: UUID.randomUUID().toString()
                    titles.addAll(getLocalizedArrayFromMap(webServiceTitleMap))
                    descriptions.addAll(getLocalizedArrayFromMap(webServiceDescriptionMap))
                    webServiceEndPoints.addAll(webServiceList)
                }
            }
            // Security Agent
            solutionProject.securityService.securityAgent.apply {
                includeSecurityAgent = true
                securityContextExpressionsEnabled = true
                declaredExpressionOperators.addAll(USER_INFORMATION)
                agentId = (agentModels.get(this.javaClass.name) as? SecurityAgentModel)
                    ?.agentId
                    .takeIf { !it.isNullOrEmpty() }
                    ?: UUID.randomUUID().toString()
                name =
                    solutionProject.solutionManager.solutionDetails.solutionDescription.name + SECURITY_AGENT_NAME
                title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                description.i18nAssetId = title.i18nAssetId
                title.stringId = SECURITY_AGENT_TITLE_STRING_ID
                description.stringId = SECURITY_AGENT_DESCRIPTION_STRING_ID
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    SECURITY_AGENT_TITLE_STRING_ID,
                    name
                )
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    SECURITY_AGENT_DESCRIPTION_STRING_ID,
                    name
                )
            }
            // Solution Notification
            solutionProject.notificationService.notificationAgent.apply {
                includeNotificationAgent = true
                notificationsToReceiveModel.explict.explicitValue.add(NotificationType.NtConfigurationModified)
                agentId = (agentModels.get(this.javaClass.name) as? SolutionNotificationAgentModel)
                    ?.agentId
                    .takeIf { !it.isNullOrEmpty() }
                    ?: UUID.randomUUID().toString()
                // androidPackageName is set on apkParser
                notificationTargetModel.workpathPlatformClientTargetModel.androidPackageName =
                    apkParser!!.getAndroidPackageName()
            }
            // Solution Diagnostics
            solutionProject.solutionDiagnosticsService.solutionDiagnosticsAgent.apply {
                includeSolutionDiagnosticsAgent = true
                agentId = (agentModels.get(this.javaClass.name) as? SolutionDiagnosticsModel)
                    ?.agentId
                    .takeIf { !it.isNullOrEmpty() }
                    ?: UUID.randomUUID().toString()
                name =
                    solutionProject.solutionManager.solutionDetails.solutionDescription.name + SOLUTION_DIAGNOSTICS_AGENT_NAME
                title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                description.i18nAssetId = title.i18nAssetId
                title.stringId = SOLUTION_DIAGNOSTICS_AGENT_TITLE_STRING_ID
                description.stringId = SOLUTION_DIAGNOSTICS_AGENT_DESCRIPTION_STRING_ID
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    SOLUTION_DIAGNOSTICS_AGENT_TITLE_STRING_ID,
                    name
                )
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    SOLUTION_DIAGNOSTICS_AGENT_DESCRIPTION_STRING_ID,
                    name
                )
            }
            // Message Center
            solutionProject.messageCenterService.messageCenterAgent.apply {
                includeMessageCenterAgent = true
                agentId = (agentModels.get(this.javaClass.name) as? MessageCenterAgentModel)
                    ?.agentId
                    .takeIf { !it.isNullOrEmpty() }
                    ?: UUID.randomUUID().toString()
                name = solutionProject.solutionManager.solutionDetails.solutionDescription.name + MESSAGE_CENTER_AGENT_NAME
                title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                description.i18nAssetId = title.i18nAssetId
                title.stringId = MESSAGE_CENTER_AGENT_TITLE_STRING_ID
                description.stringId = MESSAGE_CENTER_AGENT_DESCRIPTION_STRING_ID
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    MESSAGE_CENTER_AGENT_TITLE_STRING_ID,
                    name
                )
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    MESSAGE_CENTER_AGENT_DESCRIPTION_STRING_ID,
                    name
                )
            }

            // Build Options
            if (tfBundleSerial.text.isNotEmpty()) {
                solutionProject.solutionBundleSerial = tfBundleSerial.text
            }
            if (cbSignTar.isSelected) {
                solutionProject.signingKeyPath = signingKeyFile?.toPath()?.toAbsolutePath()
            }
        } catch (e: Exception) {
            setSolutionBundleCreateResultText("${MESSAGE.getString("task_error_prefix")}${e.message}", true)
            throw e
        }
        return solutionProject
    }

    private val taskInterface = object : TaskInterface {
        override fun updateMessage(status: TaskStatus) {
            Platform.runLater {
                when (status.state) {
                    TaskState.InProgress -> {
                        setSolutionBundleCreateResultText(MESSAGE.getString("task_in_progress"), false)
                    }

                    TaskState.Completed -> onSucceed(null)
                    TaskState.Failed -> {
                        onFailed(Exception("${MESSAGE.getString("task_error_prefix")}${status.cause}"))
                    }

                    else -> {
                        onFailed(IllegalStateException("${MESSAGE.getString("task_error_unexpected_state")}${status.state.name}"))
                    }
                }
            }
        }

        override fun onSucceed(obj: Any?) {
            Platform.runLater {
                setSolutionBundleCreateResultText(MESSAGE.getString("task_done"), false)
                setBackgroundProgress(false)
            }
        }

        override fun onFailed(e: Exception) {
            Platform.runLater {
                showMessagePopup(
                    State.ERROR,
                    MESSAGE.getString("dialog_title_error"),
                    MESSAGE.getString("dialog_header_failed"),
                    "${e.message}"
                )
                setSolutionBundleCreateResultText("${MESSAGE.getString("dialog_header_failed")}${e.message}", true)
                e.printStackTrace()// TODO change to print to file
                setBackgroundProgress(false)
            }
        }

    }
}