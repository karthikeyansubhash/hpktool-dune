package com.hp.workpath.pkgmgt.util.gui.controllers

import com.hp.workpath.pkgmgt.util.gui.utilities.ActionType
import com.hp.workpath.pkgmgt.util.gui.utilities.CHECK_BOX_ERROR
import com.hp.workpath.pkgmgt.util.gui.utilities.TABLE_VIEW_ERROR
import com.hp.workpath.pkgmgt.util.gui.utilities.TEXT_FIELD_ERROR
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.CheckBox
import javafx.scene.control.Control
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import java.io.File

open class Controller {
    protected lateinit var stage: Stage

    open fun setStage(stage: Stage, actionType: ActionType) {
        this.stage = stage
        this.currentAction = actionType
    }

    private var currentAction: ActionType = ActionType.SOLUTION_NEW

    // If Node has error, true.
    protected var hasErrors = mutableMapOf<Node, Boolean>()

    protected fun updateCheckBoxError(checkBox: CheckBox, error: Boolean) {
        checkBox.styleClass.remove(CHECK_BOX_ERROR)
        if (error) {
            checkBox.styleClass.add(CHECK_BOX_ERROR)
        }
        hasErrors[checkBox] = error
        updateEnableState()
    }

    protected fun updateTableViewError(tableView: TableView<Any>, error: Boolean) {
        tableView.styleClass.remove(TABLE_VIEW_ERROR)
        if (error) {
            tableView.styleClass.add(TABLE_VIEW_ERROR)
        }
        hasErrors[tableView] = error
        updateEnableState()
    }

    protected fun updateTextFieldError(textField: TextField, error: Boolean) {
        textField.styleClass.remove(TEXT_FIELD_ERROR)
        if (error) {
            textField.styleClass.add(TEXT_FIELD_ERROR)
        }
        hasErrors[textField] = error
        updateEnableState()
    }

    protected fun updateTextFieldEmpty(textField: TextField) {
        updateTextFieldError(textField, textField.text.isNullOrEmpty())
    }

    open fun updateEnableState() {
        throw Exception("Not Implemented open method: updateEnableState")
    }

    private var initialPath: File? = null

    enum class FileExtension {
        APK, JSON, IMAGE, PEM, HPK, BUNDLE, TXT,
    }

    enum class State {
        INFORMATION, WARNING, ERROR
    }

    enum class FileChooserMode {
        OPEN, SAVE
    }

    interface ButtonClickListener {
        fun ok()
        fun cancel()
    }

    fun showOkCancelPopup(
        state: State,
        titleText: String,
        headerText: String,
        contentText: String,
        buttonClickListener: ButtonClickListener,
    ) {
        openPopupController(state, titleText, headerText, contentText, buttonClickListener)
    }

    fun showMessagePopup(
        state: State,
        titleText: String,
        headerText: String,
        contentText: String,
    ) {
        openPopupController(state, titleText, headerText, contentText, null)
    }

    private fun openPopupController(
        state: State,
        titleText: String,
        headerText: String,
        contentText: String,
        buttonClickListener: ButtonClickListener?,
    ) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/popup_message_controller.fxml"))
            loader.resources = MESSAGE
            val newWindow: Parent = loader.load()
            val scene = Scene(newWindow)
            val stage: Stage = Stage().also {
                it.title = titleText
                it.initModality(Modality.WINDOW_MODAL)
                it.initOwner(this.stage.scene.window)
                it.icons.add(Image(javaClass.getResourceAsStream("/images/hp.png")))
                it.scene = scene
                it.sizeToScene()
                it.isResizable = false
            }
            (loader.getController() as PopupMessageController).apply {
                setStage(stage, currentAction)
                initView(
                    state,
                    headerText,
                    contentText,
                    buttonClickListener
                )
            }
            stage.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun setVisibility(element: Node, value: Boolean) {
        element.apply {
            isManaged = value
            isVisible = value
        }
    }

    protected fun closeSubController(control: Control) {
        val stage: Stage = control.scene.window as Stage
        stage.close()
    }

    protected fun openFile(stage: Stage, extensionType: FileExtension, mode: FileChooserMode): File? {
        return startFileChooser(stage, extensionType, "Open ${extensionType.name.lowercase()} file", mode)
    }

    private fun startFileChooser(
        stage: Stage,
        extensionType: FileExtension,
        title: String,
        mode: FileChooserMode,
    ): File? {
        val extension: String = when (extensionType) {
            FileExtension.APK -> "apk"
            FileExtension.JSON -> "json"
            FileExtension.IMAGE -> "image"
            FileExtension.PEM -> "pem"
            FileExtension.HPK -> "hpk"
            FileExtension.BUNDLE -> "bdl"
            FileExtension.TXT -> "txt"
        }
        val fileChooser = FileChooser()
        fileChooser.title = title
        if (initialPath != null && initialPath!!.exists()) {
            fileChooser.initialDirectory = initialPath
        }
        if (extensionType == FileExtension.IMAGE) {
            fileChooser.extensionFilters.add(
                FileChooser.ExtensionFilter(
                    "$extension file",
                    "*.png", "*.jpeg", "*.jpg"
                )
            )
        } else if (extensionType == FileExtension.BUNDLE) {
            fileChooser.extensionFilters.add(
                FileChooser.ExtensionFilter(
                    "Bundle file (*.bdl, *.hpk2)",
                    "*.bdl", "*.hpk2"
                )
            )
        } else {
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("$extension file", "*.$extension"))
        }
        val currentPath: File? = when (mode) {
            FileChooserMode.OPEN -> fileChooser.showOpenDialog(stage)
            FileChooserMode.SAVE -> fileChooser.showSaveDialog(stage)
        }
        if (currentPath != null) {
            initialPath = currentPath.parentFile
        }

        return currentPath
    }

    protected fun startDirectoryChooser(stage: Stage, title: String): File? {
        val directoryChooser = DirectoryChooser()
        directoryChooser.title = title
        if (initialPath != null && initialPath!!.exists()) {
            directoryChooser.initialDirectory = initialPath
        }
        val selectedDirectory: File? = directoryChooser.showDialog(stage)
        if (selectedDirectory != null) {
            initialPath = selectedDirectory.parentFile
        }
        return selectedDirectory
    }

    protected fun openRawDataController(title: String, data: String) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/raw_data_controller.fxml"))
            loader.resources = MESSAGE
            val newWindow: Parent = loader.load()
            val scene = Scene(newWindow)
            val stage: Stage = Stage().also {
                it.title = title
                it.initModality(Modality.WINDOW_MODAL)
                it.initOwner(this.stage.scene.window)
                it.icons.add(Image(javaClass.getResourceAsStream("/images/hp.png")))
                it.scene = scene
                it.width = 600.0
                it.height = 500.0
            }
            (loader.getController() as RawDataController).apply {
                setStage(stage, currentAction)
                setValue(data)
            }
            stage.show()
            stage.minWidth = 600.0
            stage.minHeight = 400.0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Open LocalizationController
     * @param parent current window
     * @param title localization controller title
     * @param localizedMap current localizaed string
     * @param readOnly set localization controller as readonly (default: false)
     * @param callback function called when localization controller closed with ok button
     */
    protected fun openLocalizationController(
        parent: Window,
        title: String,
        localizedMap: MutableMap<String, String>,
        readOnly: Boolean = false,
        callback: (MutableMap<String, String>) -> Unit,
    ) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/solution_localization_controller.fxml"))
            loader.resources = MESSAGE
            val newWindow: Parent = loader.load()
            val scene = Scene(newWindow)
            val stage: Stage = Stage().also {
                it.title = title
                it.initModality(Modality.WINDOW_MODAL)
                it.initOwner(parent)
                it.icons.add(Image(javaClass.getResourceAsStream("/images/hp.png")))
                it.scene = scene
                it.width = 600.0
                it.height = 500.0
            }
            (loader.getController() as SolutionLocalizationController).apply {
                setStage(stage, ActionType.SOLUTION_NEW)
                setValue(this, localizedMap, callback)
                setReadOnly(readOnly)
            }
            stage.show()
            stage.minWidth = 600.0
            stage.minHeight = 400.0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}