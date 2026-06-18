package com.hp.workpath.pkgmgt.util.gui.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.hp.workpath.pkgmgt.util.gui.services.ConnectionServiceGui
import com.hp.workpath.pkgmgt.util.gui.services.ConnectionServiceType
import com.hp.workpath.pkgmgt.util.gui.utilities.*
import com.hp.workpath.pkgmgt.util.models.ConnectionData
import com.hp.workpath.pkgmgt.util.models.connection.TaskState
import com.hp.workpath.pkgmgt.util.utilities.*
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Stage

class ConfigurationController : Controller() {
    @FXML
    private lateinit var vbBackground: VBox

    @FXML
    private lateinit var tfDeviceAddress: TextField

    @FXML
    private lateinit var pfDevicePassword: PasswordField

    @FXML
    private lateinit var tfSolutionId: TextField

    @FXML
    private lateinit var taConfigData: TextArea

    @FXML
    private lateinit var btGet: Button

    @FXML
    private lateinit var btUpdate: Button

    @FXML
    private lateinit var piProgress: ProgressIndicator

    @FXML
    private lateinit var lbOperationResult: Label

    private fun setOperationResultText(text: String, isError: Boolean) {
        lbOperationResult.text = text
        lbOperationResult.styleClass.remove(LABEL_ERROR)
        if (isError) {
            lbOperationResult.styleClass.add(LABEL_ERROR)
        }
    }

    override fun setStage(stage: Stage, actionType: ActionType) {
        super.setStage(stage, actionType)
        initView()
    }

    private fun initView() {
        tfDeviceAddress.text = DEFAULT_HOST
        pfDevicePassword.text = DEFAULT_PASSWORD
        tfSolutionId.text = DEFAULT_UUID
        initNodes()
    }

    private fun initNodes() {
        initListener()
        updateEnableState()
    }

    private fun initListener() {
        hasErrors.clear()
        hasErrors[tfDeviceAddress] = tfDeviceAddress.text.isNullOrEmpty()
        tfDeviceAddress.textProperty().addListener { _, _, _ ->
            updateTextFieldEmpty(tfDeviceAddress)
        }
        tfSolutionId.textProperty().addListener { _, _, newValue ->
            tfSolutionId.styleClass.remove(TEXT_FIELD_ERROR)
            if (!isValidUuid(newValue)) {
                tfSolutionId.styleClass.add(TEXT_FIELD_ERROR)
            }
            updateEnableState()
        }
        taConfigData.textProperty().addListener { _, _, newValue ->
            taConfigData.styleClass.remove(TEXT_AREA_ERROR)
            if (!isValidJson(newValue)) {
                taConfigData.styleClass.add(TEXT_AREA_ERROR)
            }
            updateEnableState()
        }
    }
    override fun updateEnableState() {
        hasErrors.forEach { (_, value) ->
            if (value) {
                btGet.isDisable = true
                btUpdate.isDisable = true
                return
            }
        }
        btGet.isDisable = !isValidUuid(tfSolutionId.text)
        btUpdate.isDisable = !isValidUuid(tfSolutionId.text) || !isValidJson(taConfigData.text)
    }

    private fun setBackgroundProgress(isBackground: Boolean) {
        vbBackground.isDisable = isBackground
        if (isBackground) {
            btGet.isDisable = true
            btUpdate.isDisable = true
        } else {
            updateEnableState()
        }
        setVisibility(piProgress, isBackground)
    }

    @FXML
    fun handleConfigurationGet(event: ActionEvent) {
        try {
            DEFAULT_HOST = tfDeviceAddress.text
            DEFAULT_PASSWORD = pfDevicePassword.text
            DEFAULT_UUID = tfSolutionId.text
            val connectionData = ConnectionData().apply {
                networkAddress = tfDeviceAddress.text
                password = pfDevicePassword.text
                solutionId = tfSolutionId.text
            }
            val connectionServiceGui =
                ConnectionServiceGui(connectionData, taskInterface, ConnectionServiceType.CONFIG_GET)
            connectionServiceGui.start()
            setBackgroundProgress(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @FXML
    fun handleConfigurationUpdate(event: ActionEvent) {
        try {
            DEFAULT_HOST = tfDeviceAddress.text
            DEFAULT_PASSWORD = pfDevicePassword.text
            DEFAULT_UUID = tfSolutionId.text
            val connectionData = ConnectionData().apply {
                networkAddress = tfDeviceAddress.text
                password = pfDevicePassword.text
                solutionId = tfSolutionId.text
                configData = taConfigData.text
            }
            val connectionServiceGui =
                ConnectionServiceGui(connectionData, taskInterface, ConnectionServiceType.CONFIG_UPDATE)
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
                    TaskState.Connecting -> setOperationResultText(MESSAGE.getString("task_connecting"), false)
                    TaskState.Authorizing -> setOperationResultText(MESSAGE.getString("task_authorizing"), false)
                    TaskState.CheckingStatus -> setOperationResultText(MESSAGE.getString("task_checking_status"), false)
                    TaskState.Sending -> setOperationResultText(MESSAGE.getString("task_sending"), false)
                    TaskState.InProgress -> setOperationResultText(MESSAGE.getString("task_in_progress"), false)
                    TaskState.Sending_Broadcast -> setOperationResultText(MESSAGE.getString("task_send_broadcasting"), false)
                    TaskState.Completed -> onSucceed(null)
                    TaskState.Failed -> onFailed(Exception("${MESSAGE.getString("task_error_prefix")}${status.cause}"))
                }
            }
        }

        override fun onSucceed(obj: Any?) {
            Platform.runLater {
                if (obj != null) {
                    when (obj) {
                        is Map<*,*> -> {
                            val rawJson = String(obj[RESPONSE_KEY_DATA] as ByteArray)
                            taConfigData.text = try {
                                val mapper = ObjectMapper()
                                mapper.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(mapper.readTree(rawJson))
                            } catch (e: Exception) {
                                rawJson
                            }
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
                setOperationResultText(MESSAGE.getString("task_done"), false)
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
                setOperationResultText("${MESSAGE.getString("dialog_header_failed")}${e.message}", true)
                e.printStackTrace()
                setBackgroundProgress(false)
            }
        }
    }
}