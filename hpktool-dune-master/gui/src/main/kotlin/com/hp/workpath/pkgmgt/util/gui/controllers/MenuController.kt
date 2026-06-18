package com.hp.workpath.pkgmgt.util.gui.controllers

import com.hp.workpath.pkgmgt.util.gui.utilities.ActionType
import com.hp.workpath.pkgmgt.util.gui.utilities.changeScreen
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Modality
import javafx.stage.Stage
import kotlin.system.exitProcess

class MenuController : Controller() {
    @FXML
    fun handleNewMenuAction(actionEvent: ActionEvent) {
        changeScreen(stage, javaClass, ActionType.SOLUTION_NEW)
    }

    @FXML
    fun handleOpenHPKAction(actionEvent: ActionEvent) {
        changeScreen(stage, javaClass, ActionType.OPEN_HPK)
    }

    @FXML
    fun handleOpenAction(actionEvent: ActionEvent) {
        changeScreen(stage, javaClass, ActionType.OPEN)
    }

    @FXML
    fun handleExitMenuAction(actionEvent: ActionEvent) {
        exitProcess(0)
    }

    @FXML
    fun handleInstallMenuAction(actionEvent: ActionEvent) {
        changeScreen(stage, javaClass, ActionType.INSTALL)
    }

    @FXML
    fun handleManagementMenuAction(actionEvent: ActionEvent) {
        changeScreen(stage, javaClass, ActionType.MANAGEMENT)
    }

    @FXML
    fun handleConfigurationMenuAction(actionEvent: ActionEvent) {
        changeScreen(stage, javaClass, ActionType.CONFIGURATION)
    }

    @FXML
    fun handleAttestationMenuAction(actionEvent: ActionEvent) {
        changeScreen(stage, javaClass, ActionType.ATTESTATION)
    }

    @FXML
    fun handleAboutMenuAction(actionEvent: ActionEvent) {
        openAboutController()
    }

    private fun openAboutController() {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/about_controller.fxml"))
            loader.resources = MESSAGE
            val newWindow: Parent = loader.load()
            val scene = Scene(newWindow)
            val stage: Stage = Stage().also {
                it.title = "About ${MESSAGE.getString("menu_tool_name")}" //TODO
                it.initModality(Modality.WINDOW_MODAL)
                it.initOwner(this.stage.scene.window)
                it.icons.add(Image(javaClass.getResourceAsStream("/images/hp.png")))
                it.scene = scene
                it.sizeToScene()
            }
            (loader.getController() as AboutController).setStage(stage, ActionType.ABOUT)
            stage.show()
            stage.minWidth = 550.0
            stage.minHeight = 100.0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}