package com.hp.workpath.pkgmgt.util.gui.controllers

import com.hp.workpath.pkgmgt.util.gui.services.ConnectionServiceGui
import com.hp.workpath.pkgmgt.util.gui.services.ConnectionServiceType
import com.hp.workpath.pkgmgt.util.gui.utilities.*
import com.hp.workpath.pkgmgt.util.models.ConnectionData
import com.hp.workpath.pkgmgt.util.models.connection.TaskState
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import com.hp.workpath.pkgmgt.util.utilities.TaskInterface
import com.hp.workpath.pkgmgt.util.utilities.TaskStatus
import com.hp.workpath.pkgmgt.util.utilities.isValidUuid
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.io.File

class InstallController : Controller() {
    @FXML
    private lateinit var vbBackground: VBox

    @FXML
    private lateinit var tfDeviceAddress: TextField

    @FXML
    private lateinit var pfDevicePassword: PasswordField

    @FXML
    private lateinit var tfSolutionBundle: TextField
    private var solutionBundleFile: File? = null

    @FXML
    private lateinit var tfSolutionId: TextField

    @FXML
    private lateinit var btInstall: Button

    @FXML
    private lateinit var btUninstall: Button

    @FXML
    private lateinit var piProgress: ProgressIndicator

    @FXML
    private lateinit var lbInstallResult: Label

    private fun setInstallResultText(text: String, isError: Boolean) {
        lbInstallResult.text = text
        lbInstallResult.styleClass.remove(LABEL_ERROR)
        if (isError) {
            lbInstallResult.styleClass.add(LABEL_ERROR)
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
        tfSolutionBundle.textProperty().addListener { _, _, newValue ->
            tfSolutionBundle.styleClass.remove(TEXT_FIELD_ERROR)
            if (newValue.isNullOrEmpty()) {
                tfSolutionBundle.styleClass.add(TEXT_FIELD_ERROR)
            }
            updateEnableState()
        }
        tfSolutionId.textProperty().addListener { _, _, newValue ->
            tfSolutionId.styleClass.remove(TEXT_FIELD_ERROR)
            if (!isValidUuid(newValue)) {
                tfSolutionId.styleClass.add(TEXT_FIELD_ERROR)
            }
            updateEnableState()
        }
    }

    override fun updateEnableState() {
        hasErrors.forEach { (_, value) ->
            if (value) {
                btInstall.isDisable = true
                btUninstall.isDisable = true
                return
            }
        }
        btInstall.isDisable = tfSolutionBundle.text.isNullOrEmpty()
        btUninstall.isDisable = !isValidUuid(tfSolutionId.text)
    }

    @FXML
    fun handleSolutionBundleSelect(event: ActionEvent) {
        solutionBundleFile = openFile(stage, FileExtension.BUNDLE, FileChooserMode.OPEN)
        if (solutionBundleFile != null) {
            tfSolutionBundle.text = solutionBundleFile!!.name
            tfSolutionId.text = ""
        }
    }

    private fun setBackgroundProgress(isBackground: Boolean) {
        vbBackground.isDisable = isBackground
        if (isBackground) {
            btInstall.isDisable = true
            btUninstall.isDisable = true
        } else {
            updateEnableState()
        }
        setVisibility(piProgress, isBackground)
    }

    @FXML
    fun handleSolutionInstall(event: ActionEvent) {
        try {
            DEFAULT_HOST = tfDeviceAddress.text
            DEFAULT_PASSWORD = pfDevicePassword.text
            val connectionData = ConnectionData().apply {
                networkAddress = tfDeviceAddress.text
                password = pfDevicePassword.text
                installFilePath = solutionBundleFile!!.toPath().toAbsolutePath()
            }
            val connectionServiceGui =
                ConnectionServiceGui(connectionData, taskInterface, ConnectionServiceType.INSTALL)
            connectionServiceGui.start()
            setBackgroundProgress(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @FXML
    fun handleSolutionUninstall(event: ActionEvent) {
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
                ConnectionServiceGui(connectionData, taskInterface, ConnectionServiceType.UNINSTALL)
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
                    TaskState.Connecting -> setInstallResultText(MESSAGE.getString("task_connecting"), false)
                    TaskState.Authorizing -> setInstallResultText(MESSAGE.getString("task_authorizing"), false)
                    TaskState.CheckingStatus -> setInstallResultText(MESSAGE.getString("task_checking_status"), false)
                    TaskState.Sending -> setInstallResultText(MESSAGE.getString("task_sending"), false)
                    TaskState.InProgress -> setInstallResultText(MESSAGE.getString("task_in_progress"), false)
                    TaskState.Sending_Broadcast -> setInstallResultText(MESSAGE.getString("task_sending_"), false)
                    TaskState.Completed -> onSucceed(null)
                    TaskState.Failed -> onFailed(Exception("${MESSAGE.getString("task_send_broadcasting")}${status.cause}"))
                }
            }
        }

        override fun onSucceed(obj: Any?) {
            Platform.runLater {
                if (obj != null) {
                    when (obj) {
                        is String -> {
                            tfSolutionId.text = obj
                            DEFAULT_UUID = tfSolutionId.text
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
                setInstallResultText(MESSAGE.getString("task_done"), false)
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
                setInstallResultText("${MESSAGE.getString("dialog_header_failed")}${e.message}", true)
                e.printStackTrace()
                setBackgroundProgress(false)
            }
        }
    }
}