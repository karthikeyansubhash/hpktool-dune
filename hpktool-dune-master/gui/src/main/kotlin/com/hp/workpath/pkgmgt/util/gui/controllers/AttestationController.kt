package com.hp.workpath.pkgmgt.util.gui.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.hp.workpath.pkgmgt.util.gui.services.ConnectionServiceGui
import com.hp.workpath.pkgmgt.util.gui.services.ConnectionServiceType
import com.hp.workpath.pkgmgt.util.gui.utilities.*
import com.hp.workpath.pkgmgt.util.models.ConnectionData
import com.hp.workpath.pkgmgt.util.models.attestation.AttestationData
import com.hp.workpath.pkgmgt.util.models.connection.TaskState
import com.hp.workpath.pkgmgt.util.services.AttestationService
import com.hp.workpath.pkgmgt.util.utilities.*
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.io.File

class AttestationController : Controller() {
    @FXML
    private lateinit var vbBackground: VBox

    @FXML
    private lateinit var tfDeviceAddress: TextField

    @FXML
    private lateinit var pfDevicePassword: PasswordField

    @FXML
    private lateinit var tfUserName: TextField

    @FXML
    private lateinit var tfSolutionFile: TextField

    @FXML
    private lateinit var tfCommandFile: TextField

    @FXML
    private lateinit var tfSolutionId: TextField

    @FXML
    private lateinit var tfLdbServiceKey: TextField

    @FXML
    private lateinit var taAttestationData: TextArea

    @FXML
    private lateinit var btUpdate: Button

    @FXML
    private lateinit var piProgress: ProgressIndicator

    @FXML
    private lateinit var lbOperationResult: Label

    private var selectedBdlFile: File? = null
    private var commandData: String = ""

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
        initNodes()
    }

    private fun initNodes() {
        initListener()
        setDefaultClientCredentials()
        updateEnableState()
    }

    private fun setDefaultClientCredentials() {
        val defaultClientInfo = mapOf(
            "client_id" to "",
            "client_secret" to ""
        )

        val objectMapper = ObjectMapper()
        try {
            val prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(defaultClientInfo)
            taAttestationData.text = prettyJson
        } catch (e: Exception) {
            taAttestationData.text = """{
                "client_id": "",
                "client_secret": ""
                }"""
        }
    }

    private fun initListener() {
        hasErrors.clear()
        hasErrors[tfDeviceAddress] = tfDeviceAddress.text.isNullOrEmpty()

        tfDeviceAddress.textProperty().addListener { _, _, _ ->
            updateTextFieldEmpty(tfDeviceAddress)
            updateEnableState()
        }
        pfDevicePassword.textProperty().addListener { _, _, _ ->
            updateEnableState()
        }
        tfUserName.textProperty().addListener { _, _, _ ->
            updateEnableState()
        }
        tfLdbServiceKey.textProperty().addListener { _, _, _ ->
            updateEnableState()
        }
        tfSolutionId.textProperty().addListener { _, _, newValue ->
            tfSolutionId.styleClass.remove(TEXT_FIELD_ERROR)
            if (!isValidUuid(newValue)) {
                tfSolutionId.styleClass.add(TEXT_FIELD_ERROR)
            }
            updateEnableState()
        }
        taAttestationData.textProperty().addListener { _, _, newValue ->
            taAttestationData.styleClass.remove(TEXT_AREA_ERROR)
            if (!isValidJson(newValue)) {
                taAttestationData.styleClass.add(TEXT_AREA_ERROR)
            }
            updateEnableState()
        }
    }

    override fun updateEnableState() {
        hasErrors.forEach { (_, value) ->
            if (value) {
                btUpdate.isDisable = true
                return
            }
        }

        val isHostEmpty = tfDeviceAddress.text.isNullOrBlank()
        val isPasswordEmpty = pfDevicePassword.text.isNullOrBlank()
        val isUserNameEmpty = tfUserName.text.isNullOrBlank()
        val isServiceKeyEmpty = tfLdbServiceKey.text.isNullOrBlank()
        val isSolutionIdInvalid = !isValidUuid(tfSolutionId.text)
        val isAttestationDataInvalid = !isValidJson(taAttestationData.text)

        btUpdate.isDisable = isHostEmpty ||
                isPasswordEmpty ||
                isUserNameEmpty ||
                isServiceKeyEmpty ||
                isSolutionIdInvalid ||
                isAttestationDataInvalid
    }

    private fun setBackgroundProgress(isBackground: Boolean) {
        vbBackground.isDisable = isBackground
        if (isBackground) {
            btUpdate.isDisable = true
        } else {
            updateEnableState()
        }
        setVisibility(piProgress, isBackground)
    }

    @FXML
    fun handleSolutionFileSelect(event: ActionEvent) {
        val bdlFile = openFile(stage, FileExtension.BUNDLE, FileChooserMode.OPEN)
        if (bdlFile != null) {
            loadBdlFile(bdlFile)
        }
    }

    @FXML
    fun handleSelectCommandLocation(event: ActionEvent) {
        val commandFile = openFile(stage, FileExtension.TXT, FileChooserMode.OPEN)
        if (commandFile != null) {
            loadCommandFile(commandFile)
        }
    }

    @FXML
    fun handleClearFile(event: ActionEvent) {
        tfCommandFile.text = ""
        taAttestationData.text = ""
        setOperationResultText("All attestation data and file selection cleared", false)
        updateEnableState()
    }

    private fun loadCommandFile(input: File){
        setOperationResultText("Loading...", false)
        setBackgroundProgress(true)
        tfSolutionFile.text = input.name
        selectedBdlFile = input

        Thread {
            try {
                deleteTempDirectory()
                val commandDContent = input.readText()

                Platform.runLater {
                    commandData = commandDContent
                    setOperationResultText("Command file loaded and displayed successfully", false)
                    setBackgroundProgress(false)
                }
            } catch (exception: Exception) {
                Platform.runLater {
                    setOperationResultText("Failed to load command: ${exception.message}", true)
                    setBackgroundProgress(false)
                }
                exception.printStackTrace()
            }
        }.start()
    }

    private fun loadBdlFile(input: File) {
        setOperationResultText("Loading...", false)
        setBackgroundProgress(true)
        tfSolutionFile.text = input.name
        selectedBdlFile = input

        Thread {
            try {
                deleteTempDirectory()
                val bundleExtractor = BundleExtractor(input)
                val solutionProject = bundleExtractor.solutionProject

                Platform.runLater {
                    tfSolutionId.text = solutionProject.solutionManager.solutionDetails.solutionId
                    setOperationResultText("Solution loaded successfully", false)
                    setBackgroundProgress(false)
                }
            } catch (exception: Exception) {
                Platform.runLater {
                    setOperationResultText("Failed to load BDL: ${exception.message}", true)
                    setBackgroundProgress(false)
                }
                exception.printStackTrace()
            }
        }.start()
    }

    @FXML
    fun handleAttestationUpdate(event: ActionEvent) {
        try {
            DEFAULT_HOST = tfDeviceAddress.text
            DEFAULT_PASSWORD = pfDevicePassword.text
            DEFAULT_UUID = tfSolutionId.text
            val connectionData = ConnectionData().apply {
                networkAddress = tfDeviceAddress.text
                password = pfDevicePassword.text
                solutionId = tfSolutionId.text
                userName = tfUserName.text
                key = tfLdbServiceKey.text
                attestationData = taAttestationData.text
                commandData = commandData
            }
            val connectionServiceGui =
                ConnectionServiceGui(connectionData, taskInterface, ConnectionServiceType.ATTESTATION_UPDATE)
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
                            taAttestationData.text = String(obj[RESPONSE_KEY_DATA] as ByteArray)
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