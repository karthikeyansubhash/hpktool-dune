package com.hp.workpath.pkgmgt.util.gui.controllers

import com.hp.ext.service.solutionManager.Solution
import com.hp.ext.service.solutionManager.Solutions
import com.hp.workpath.pkgmgt.util.gui.services.ConnectionServiceGui
import com.hp.workpath.pkgmgt.util.gui.services.ConnectionServiceType
import com.hp.workpath.pkgmgt.util.gui.utilities.*
import com.hp.workpath.pkgmgt.util.models.ConnectionData
import com.hp.workpath.pkgmgt.util.models.connection.TaskState
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import com.hp.workpath.pkgmgt.util.utilities.TaskInterface
import com.hp.workpath.pkgmgt.util.utilities.TaskStatus
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
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

class ManagementController : Controller() {
    @FXML
    private lateinit var vbBackground: VBox

    @FXML
    private lateinit var tfDeviceAddress: TextField

    @FXML
    private lateinit var pfDevicePassword: PasswordField

    @FXML
    private lateinit var tvSolution: TableView<Solution>
    private var solutionList = mutableListOf<Solution>()
    private var observableSolutionList = FXCollections.observableArrayList<Solution>()

    inner class SolutionDetailButtonCell : TableCell<Solution, Boolean>() {
        private val cellButton: Button = Button().also {
            it.id = BUTTON_DETAIL
            it.styleClass.add(BUTTON_DETAIL_STYLE)
            it.onAction = EventHandler { _ ->
                openSolutionDetailController(this.tableView.items[this.index])
            }
        }

        override fun updateIndex(i: Int) {
            super.updateIndex(i)
            graphic = if (i < observableSolutionList.size) {
                cellButton
            } else {
                null
            }
        }
    }

    inner class SolutionDeleteButtonCell : TableCell<Solution, Boolean>() {
        private val cellButton: Button = Button().also {
            it.id = BUTTON_DELETE
            it.styleClass.add(BUTTON_DELETE_STYLE)
            it.onAction = EventHandler { event ->
                val solution: Solution = this.tableView.items[this.index]
                val deleteButtonClickListener: ButtonClickListener = object : ButtonClickListener {
                    override fun ok() {
                        deleteSolution(solution)
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
            graphic = if (i < observableSolutionList.size) {
                cellButton
            } else {
                null
            }
        }
    }

    @FXML
    private lateinit var tcSolutionId: TableColumn<Solution, String>

    @FXML
    private lateinit var tcSolutionName: TableColumn<Solution, String>

    @FXML
    private lateinit var tcSolutionVendor: TableColumn<Solution, String>

    @FXML
    private lateinit var tcSolutionDetail: TableColumn<Solution, Boolean>

    @FXML
    private lateinit var tcSolutionDelete: TableColumn<Solution, Boolean>

    @FXML
    private lateinit var btEnumerate: Button

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

    override fun setStage(stage: Stage, actionType: ActionType) {
        super.setStage(stage, actionType)
        initView()
    }

    private fun initView() {
        tfDeviceAddress.text = DEFAULT_HOST
        pfDevicePassword.text = DEFAULT_PASSWORD
        initNodes()
    }

    private fun initNodes() {
        observableSolutionList.setAll(solutionList)
        initListener()
        initTable()
        updateEnableState()
    }

    private fun initListener() {
        hasErrors.clear()
        hasErrors[tfDeviceAddress] = tfDeviceAddress.text.isNullOrEmpty()
        tfDeviceAddress.textProperty().addListener { _, _, _ ->
            updateTextFieldEmpty(tfDeviceAddress)
        }
    }

    private fun initTable() {
        tvSolution.selectionModel = null
        tcSolutionId.cellValueFactory = PropertyValueFactory("solutionId")
        tcSolutionName.cellValueFactory = Callback {
            SimpleStringProperty(it.value.description.name)
        }
        tcSolutionVendor.cellValueFactory = Callback {
            SimpleStringProperty(it.value.description.vendor)
        }
        tcSolutionDetail.cellFactory = Callback {
            SolutionDetailButtonCell()
        }
        tcSolutionDelete.cellFactory = Callback {
            SolutionDeleteButtonCell()
        }
        tvSolution.items = observableSolutionList
    }

    override fun updateEnableState() {
        hasErrors.forEach { (_, value) ->
            if (value) {
                btEnumerate.isDisable = true
                return
            }
        }
        btEnumerate.isDisable = false
    }

    private fun setBackgroundProgress(isBackground: Boolean) {
        vbBackground.isDisable = isBackground
        if (isBackground) {
            btEnumerate.isDisable = true
        } else {
            updateEnableState()
        }
        setVisibility(piProgress, isBackground)
    }

    private fun openSolutionDetailController(solution: Solution) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/management_detail_controller.fxml"))
            loader.resources = MESSAGE
            val newWindow: Parent = loader.load()
            val scene = Scene(newWindow)
            val stage: Stage = Stage().also {
                it.title = "Solution detail: ${solution.solutionId}"
                it.initModality(Modality.WINDOW_MODAL)
                it.initOwner(vbBackground.scene.window)
                it.icons.add(Image(javaClass.getResourceAsStream("/images/hp.png")))
                it.scene = scene
                it.width = 600.0
                it.height = 500.0
            }
            (loader.getController() as ManagementDetailController).apply {
                setStage(stage, ActionType.MANAGEMENT)
                setValue(
                    this,
                    solution,
                    DEFAULT_HOST,
                    DEFAULT_PASSWORD
                )
            }
            stage.show()
            stage.minWidth = 600.0
            stage.minHeight = 400.0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteSolution(solution: Solution) {
        try {
            DEFAULT_HOST = tfDeviceAddress.text
            DEFAULT_PASSWORD = pfDevicePassword.text
            val connectionData = ConnectionData().apply {
                networkAddress = tfDeviceAddress.text
                password = pfDevicePassword.text
                solutionId = solution.solutionId.toString()
            }
            val connectionServiceGui =
                ConnectionServiceGui(connectionData, taskInterface, ConnectionServiceType.UNINSTALL)
            connectionServiceGui.start()
            setBackgroundProgress(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun refreshSolutionTable() {
        observableSolutionList.setAll(solutionList)
    }

    @FXML
    fun handleSolutionEnumerate(event: ActionEvent) {
        try {
            DEFAULT_HOST = tfDeviceAddress.text
            DEFAULT_PASSWORD = pfDevicePassword.text
            val connectionData = ConnectionData().apply {
                networkAddress = tfDeviceAddress.text
                password = pfDevicePassword.text
            }
            val connectionServiceGui =
                ConnectionServiceGui(connectionData, taskInterface, ConnectionServiceType.ENUMERATE_SOLUTIONS)
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
                    TaskState.Sending_Broadcast -> setResultText(MESSAGE.getString("task_sending_"), false)
                    TaskState.Completed -> onSucceed(null)
                    TaskState.Failed -> onFailed(Exception("${MESSAGE.getString("task_error_prefix")}${status.cause}"))
                }
            }
        }

        override fun onSucceed(obj: Any?) {
            Platform.runLater {
                if (obj != null) {
                    when (obj) {
                        is String -> {
                            // remove
                            var deletedIndex = 0
                            solutionList.forEachIndexed { index, solution ->
                                if(solution.solutionId.toString().equals(obj, true)) {
                                    deletedIndex = index
                                    return@forEachIndexed
                                }
                            }
                            solutionList.removeAt(deletedIndex)
                            refreshSolutionTable()
                        }

                        is Solutions -> {
                            solutionList = obj.members
                            refreshSolutionTable()
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
}