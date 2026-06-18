package com.hp.workpath.pkgmgt.util.gui.controllers

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.hp.ext.service.solutionManager.Solution
import com.hp.workpath.pkgmgt.util.gui.models.ApplicationInfo
import com.hp.workpath.pkgmgt.util.gui.services.ConnectionServiceGui
import com.hp.workpath.pkgmgt.util.gui.services.ConnectionServiceType
import com.hp.workpath.pkgmgt.util.gui.utilities.*
import com.hp.workpath.pkgmgt.util.models.ApplicationServiceData
import com.hp.workpath.pkgmgt.util.models.ConnectionData
import com.hp.workpath.pkgmgt.util.models.SolutionProject
import com.hp.workpath.pkgmgt.util.models.application.ApplicationCategory
import com.hp.workpath.pkgmgt.util.models.connection.TaskState
import com.hp.workpath.pkgmgt.util.models.hpk.WorkpathPlatformVersion
import com.hp.workpath.pkgmgt.util.models.usbAccessories.UsbRegistrationModel
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import com.hp.workpath.pkgmgt.util.utilities.TaskInterface
import com.hp.workpath.pkgmgt.util.utilities.TaskStatus
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Callback
import java.nio.file.Path

class ManagementDetailController : Controller() {
    @FXML
    private lateinit var spBackground: ScrollPane
    private var scrollLocation: Double = 0.0

    private fun saveScrollLocation() {
        scrollLocation = spBackground.vvalue
    }
    private fun restoreScrollLocation() {
        if(scrollLocation != 0.0){
            spBackground.vvalue = scrollLocation
            scrollLocation = 0.0
        }
    }

    @FXML
    private lateinit var vbBackground: VBox

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
    private lateinit var tvApplication: TableView<ApplicationInfo>
    private var applicationList = mutableListOf<ApplicationInfo>()
    private var observableApplicationList = FXCollections.observableArrayList<ApplicationInfo>()

    inner class ApplicationDetailButtonCell : TableCell<ApplicationInfo, Boolean>() {
        private val cellButton: Button = Button().also {
            it.id = BUTTON_DETAIL
            it.styleClass.add(BUTTON_DETAIL_STYLE)
            it.onAction = EventHandler { _ ->
                openApplicationDetailController(this.tableView.items[this.index])
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
    private lateinit var tvAccessory: TableView<UsbRegistrationModel>
    private var accessoryList = mutableListOf<UsbRegistrationModel>()
    private var observableAccessoryList = FXCollections.observableArrayList<UsbRegistrationModel>()

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

    @FXML
    private lateinit var tcAccessoryType: TableColumn<UsbRegistrationModel, String>

    @FXML
    private lateinit var tcAccessoryVendorId: TableColumn<UsbRegistrationModel, String>

    @FXML
    private lateinit var tcAccessoryProductId: TableColumn<UsbRegistrationModel, String>

    @FXML
    private lateinit var tcAccessoryDetail: TableColumn<UsbRegistrationModel, Boolean>

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
    private lateinit var cbEnableStatisticsAgent: CheckBox

    @FXML
    private lateinit var btClose: Button

    @FXML
    private lateinit var btRawData: Button

    @FXML
    private lateinit var piProgress: ProgressIndicator

    @FXML
    private lateinit var lbResult: Label

    private fun setResultText(text: String, isError: Boolean) {
        lbResult.text = text
        lbResult.styleClass.remove(LABEL_ERROR)
        if (isError) {
            lbResult.styleClass.add(LABEL_ERROR)
        }
    }

    private var mainWindow: Controller? = null
    private var solution: Solution? = null

    fun setValue(mainWindow: Controller, value: Solution, host: String, pw: String) {
        this.mainWindow = mainWindow
        solution = value
        initListener()
        initTable()
        setSolutionDetailLayout()
        getSolutionDetail(host, pw)
    }

    private fun initListener() {
        spBackground.vvalueProperty().addListener { _, _, _ ->
            /**
             * When background process call ui enable method(function name) with Platform.runLater,
             * scroll location is changed to 0. So I store location data before enable ui and restore it on here.
             */
            restoreScrollLocation()
        }
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
        tvApplication.items = observableApplicationList

        tvAccessory.selectionModel = null
        tcAccessoryType.cellValueFactory = PropertyValueFactory("registration")
        tcAccessoryVendorId.cellValueFactory = PropertyValueFactory("vendorId")
        tcAccessoryProductId.cellValueFactory = PropertyValueFactory("productId")
        tcAccessoryDetail.cellFactory = Callback {
            AccessoryDetailButtonCell()
        }
        tvAccessory.items = observableAccessoryList
    }

    private fun setBackgroundProgress(isBackground: Boolean) {
        if (!isBackground) {
            saveScrollLocation()    // To restore scroll location.
        }
        vbBackground.isDisable = isBackground
        btRawData.isDisable = isBackground
        setVisibility(piProgress, isBackground)
    }

    private fun setSolutionDetailLayout() {
        val solutionProject = SolutionProject().apply {
            from(solution ?: throw Exception("Solution data is null."))
        }
        tfSolutionId.text = solutionProject.solutionManager.solutionDetails.solutionId
        solutionProject.solutionManager.solutionDetails.solutionDescription.also {
            tfSolutionName.text = it.name
            tfSolutionDescription.text = it.description
            tfSolutionVendor.text = it.vendor
            tfSolutionVersion.text = it.version
            tfSolutionDate.text = it.date
            tfSolutionEmail.text = it.supportEmail
            tfSolutionPhone.text = it.supportPhone
            tfSolutionUrl.text = it.supportUrl
            tfSolutionVersionNumber.text = it.versionNumber.toString()
        }
        cbSolutionPlatformVersion.items = observablePlatformVersionList
        cbSolutionPlatformVersion.selectionModel.select(solutionProject.solutionManager.workpathPlatformPackage.platformVersion)
        if (solutionProject.authenticationService.authenticationAgent.includeAuthenticationAgent.not()) {
            setVisibility(vbApplicationBox, true)
            setVisibility(vbAuthenticationBox, false)
            solutionProject.applicationService.applications.forEach {
                applicationList.add(ApplicationInfo().apply {
                    applicationId = it.details.applicationId
                    applicationName = it.details.name
                    applicationPath = it.target.workpathPackage
                    applicationTitle = getLocalizationStringFromI18nAsset(
                        solutionProject.applicationService,
                        it.details.title.i18nAssetId,
                        it.details.title.stringId
                    )
                    applicationDescription = getLocalizationStringFromI18nAsset(
                        solutionProject.applicationService,
                        it.details.description.i18nAssetId,
                        it.details.description.stringId
                    )
                    applicationIconPath = Path.of(it.details.icon.localIcon.path)
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
                })
            }
            observableApplicationList.setAll(applicationList)
        } else {
            setVisibility(vbApplicationBox, false)
            setVisibility(vbAuthenticationBox, true)
            solutionProject.authenticationService.authenticationAgent.also {
                tfAuthenticationId.text = it.agentId
                tfAuthenticationName.text = it.name
                tfAuthenticationPath.text = it.workpathPackage
                cbAuthenticationPrePrompt.isSelected = it.enablePrePrompt
                authenticationTitleMap = getLocalizationStringFromI18nAsset(
                    solutionProject.applicationService,
                    it.title.i18nAssetId,
                    it.title.stringId
                )
                tfAuthenticationTitle.text = authenticationTitleMap["en-US"]
                authenticationDescriptionMap = getLocalizationStringFromI18nAsset(
                    solutionProject.applicationService,
                    it.description.i18nAssetId,
                    it.description.stringId
                )
                tfAuthenticationDescription.text = authenticationDescriptionMap["en-US"]
            }
        }

        if (solutionProject.accessoriesService.usbAccessoriesAgent.includeUsbAccessoriesAgent) {
            setVisibility(btAccessoryTab, true)
            accessoryList.addAll(solutionProject.accessoriesService.usbAccessoriesAgent.registrations)
            observableAccessoryList.setAll(accessoryList)
        }
        if (solutionProject.copyJobService.copyJobAgent.includeCopyJobAgent) {
            setVisibility(btCopyAgentTab, true)
            cbEnableCopyAgent.isSelected = solutionProject.copyJobService.copyJobAgent.includeCopyJobAgent
        }
        if (solutionProject.printJobService.printJobAgent.includePrintJobAgent) {
            setVisibility(btPrintAgentTab, true)
            cbEnablePrintAgent.isSelected = solutionProject.printJobService.printJobAgent.includePrintJobAgent
        }
        if (solutionProject.scanJobService.scanJobAgent.includeScanJobAgent) {
            setVisibility(btScanAgentTab, true)
            cbEnableScanAgent.isSelected = solutionProject.scanJobService.scanJobAgent.includeScanJobAgent
        }
        if (solutionProject.deviceUsageService.deviceUsageAgent.includeDeviceUsageAgent) {
            setVisibility(btDeviceUsageTab, true)
            cbEnableDeviceUsage.isSelected = solutionProject.deviceUsageService.deviceUsageAgent.includeDeviceUsageAgent
        }
        if (solutionProject.suppliesService.suppliesAgent.includeSuppliesAgent) {
            setVisibility(btSuppliesTab, true)
            cbEnableSupplies.isSelected = solutionProject.suppliesService.suppliesAgent.includeSuppliesAgent
        }
        if (solutionProject.statisticsJobService.statisticsAgent.includeStatisticsJobAgent) {
            setVisibility(btStatisticsTab, true)
            cbEnableStatisticsAgent.isSelected = solutionProject.statisticsJobService.statisticsAgent.includeStatisticsJobAgent
        }
    }

    private fun getLocalizationStringFromI18nAsset(
        applicationServiceData: ApplicationServiceData,
        i18nAssetId: String,
        stringId: String,
    ): MutableMap<String, String> {
        val stringMap = mutableMapOf<String, String>()
        applicationServiceData.i18nAssets.forEach { i18nAsset ->
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

    private fun openApplicationDetailController(info: ApplicationInfo) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/management_detail_application_controller.fxml"))
            loader.resources = MESSAGE
            val newWindow: Parent = loader.load()
            val scene = Scene(newWindow)
            val stage: Stage = Stage()
                .also {
                    it.title = "${MESSAGE.getString("label_application")}: ${info.applicationId}"
                    it.initModality(Modality.WINDOW_MODAL)
                    it.initOwner(vbBackground.scene.window)
                    it.icons.add(Image(javaClass.getResourceAsStream("/images/hp.png")))
                    it.scene = scene
                    it.sizeToScene()
                }
            (loader.getController() as ManagementDetailApplicationController).let {
                it.setStage(stage, ActionType.MANAGEMENT)
                it.applicationType = info.applicationType
                it.setApplicationInfo(info)
            }
            stage.show()
            stage.minWidth = 600.0
            stage.minHeight = 300.0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openAccessoryController(model: UsbRegistrationModel) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/solution_accessory_controller.fxml"))
            loader.resources = MESSAGE
            val newWindow: Parent = loader.load()
            val scene = Scene(newWindow)
            val stage: Stage = Stage()
                .also {
                    it.title = "${MESSAGE.getString("label_accessory")} ${MESSAGE.getString("label_detail")}"
                    it.initModality(Modality.WINDOW_MODAL)
                    it.initOwner(vbBackground.scene.window)
                    it.icons.add(Image(javaClass.getResourceAsStream("/images/hp.png")))
                    it.scene = scene
                    it.sizeToScene()
                }
            (loader.getController() as SolutionAccessoryController).let {
                it.setStage(stage, ActionType.MANAGEMENT)
                it.setAccessoryModel(model)
                it.setReadOnly()
                it.initView()
            }
            stage.show()
            stage.minWidth = 600.0
            stage.minHeight = 200.0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSolutionDetail(host: String, pw: String) {
        try {
            val connectionData = ConnectionData().apply {
                networkAddress = host
                password = pw
                solutionId = solution!!.solutionId.toString()
            }
            val connectionServiceGui =
                ConnectionServiceGui(connectionData, taskInterface, ConnectionServiceType.GET_SOLUTION)
            connectionServiceGui.start()
            setBackgroundProgress(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val taskInterface = object : TaskInterface {
        override fun updateMessage(status: TaskStatus) {
            Platform.runLater {
                when (status.state) {
                    TaskState.Connecting -> setResultText(MESSAGE.getString("task_connecting"), false)
                    TaskState.Authorizing -> setResultText(MESSAGE.getString("task_authorizing"), false)
                    TaskState.CheckingStatus -> setResultText(MESSAGE.getString("task_checking_status"), false)
                    TaskState.Sending -> setResultText(MESSAGE.getString("task_sending"), false)
                    TaskState.InProgress -> setResultText(MESSAGE.getString("task_in_progress"), false)
                    TaskState.Sending_Broadcast -> setResultText(MESSAGE.getString("task_sending_broadcast"), false)
                    TaskState.Completed -> onSucceed(null)
                    TaskState.Failed -> onFailed(Exception("${MESSAGE.getString("task_error_prefix")}${status.cause}"))
                }
            }
        }

        override fun onSucceed(obj: Any?) {
            Platform.runLater {
                if (obj != null) {
                    when (obj) {
                        is Solution -> {
                            solution = obj
                            setSolutionDetailLayout()
                        }

                        else -> {
                            showMessagePopup(
                                State.ERROR,
                                MESSAGE.getString("dialog_title_error"),
                                MESSAGE.getString("dialog_header_solution_user_cant_access"),
                                ""
                            )
                        }
                    }
                }
                setResultText(MESSAGE.getString("task_done"), false)
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
                setResultText("${MESSAGE.getString("dialog_header_failed")}${e.message}", true)
                e.printStackTrace()
                setBackgroundProgress(false)
            }
        }
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
            btStatisticsTab -> setTabVisibility(vbStatisticsBox)
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
        ).forEach {
            setVisibility(it, false)
        }
        setVisibility(selected, true)
    }

    @FXML
    fun handleAuthenticationTitle(event: ActionEvent) {
        openLocalizationController(
            vbBackground.scene.window,
            MESSAGE.getString("label_authentication_title"),
            authenticationTitleMap,
            true
        ) { }
    }

    @FXML
    fun handleAuthenticationDescription(event: ActionEvent) {
        openLocalizationController(
            vbBackground.scene.window,
            MESSAGE.getString("label_authentication_description"),
            authenticationDescriptionMap,
            true
        ) { }
    }

    @FXML
    fun handleClose(event: ActionEvent) {
        closeSubController(btClose)
    }

    @FXML
    fun handleRawData(event: ActionEvent) {
        openRawDataController(
            MESSAGE.getString("title_detail_raw"),
            ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .writerWithDefaultPrettyPrinter().writeValueAsString(solution)
        )
    }
}