package com.hp.workpath.pkgmgt.util.gui.utilities

import com.hp.workpath.pkgmgt.lib.LocalizedString
import com.hp.workpath.pkgmgt.util.gui.controllers.*
import com.hp.workpath.pkgmgt.util.utilities.DATE_FORMAT
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import com.hp.workpath.pkgmgt.util.utilities.TOOL_VERSION
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.MenuBar
import javafx.scene.image.Image
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.util.regex.Pattern

fun changeScreen(stage: Stage, cls: Class<Any>, actionType: ActionType) {
    var fxmlLoader: FXMLLoader = when (actionType) {
        ActionType.SOLUTION_NEW -> FXMLLoader(cls.getResource("/fxml/solution_controller.fxml"))
        ActionType.OPEN -> FXMLLoader(cls.getResource("/fxml/solution_controller.fxml"))
        ActionType.OPEN_HPK -> FXMLLoader(cls.getResource("/fxml/solution_controller.fxml"))
        ActionType.INSTALL -> FXMLLoader(cls.getResource("/fxml/install_controller.fxml"))
        ActionType.MANAGEMENT -> FXMLLoader(cls.getResource("/fxml/management_controller.fxml"))
        ActionType.CONFIGURATION -> FXMLLoader(cls.getResource("/fxml/configuration_controller.fxml"))
        ActionType.ATTESTATION -> FXMLLoader(cls.getResource("/fxml/attestation_controller.fxml"))
        ActionType.ABOUT -> throw IllegalAccessError()
    }
    fxmlLoader.resources = MESSAGE
    val root: Parent = fxmlLoader.load()
    when (actionType) {
        ActionType.SOLUTION_NEW -> (fxmlLoader.getController() as SolutionController).apply {
            setStage(stage, actionType)
        }

        ActionType.OPEN -> (fxmlLoader.getController() as SolutionController).apply {
            setStage(stage, actionType)
        }

        ActionType.OPEN_HPK -> (fxmlLoader.getController() as SolutionController).apply {
            setStage(stage, actionType)
        }

        ActionType.INSTALL -> (fxmlLoader.getController() as InstallController).apply {
            setStage(stage, actionType)
        }

        ActionType.MANAGEMENT -> (fxmlLoader.getController() as ManagementController).apply {
            setStage(stage, actionType)
        }

        ActionType.CONFIGURATION -> (fxmlLoader.getController() as ConfigurationController).apply {
            setStage(stage, actionType)
        }

        ActionType.ATTESTATION -> (fxmlLoader.getController() as AttestationController).apply {
            setStage(stage, actionType)
        }
        ActionType.ABOUT -> throw IllegalAccessError()
    }

    fxmlLoader = FXMLLoader(cls.getResource("/fxml/menu_controller.fxml"))
    fxmlLoader.resources = MESSAGE
    val menuBar: MenuBar = fxmlLoader.load()
    (fxmlLoader.getController() as MenuController).setStage(stage, actionType)

    val anchorPane = AnchorPane(root)
    AnchorPane.setTopAnchor(root, 0.0)
    AnchorPane.setBottomAnchor(root, 0.0)
    AnchorPane.setLeftAnchor(root, 0.0)
    AnchorPane.setRightAnchor(root, 0.0)
    val borderPane = BorderPane(anchorPane) //center
    borderPane.top = menuBar
    val scene = Scene(borderPane)
    stage.icons.add(Image("/images/hp.png"))
    stage.title = "${MESSAGE.getString("menu_tool_name")} (${MESSAGE.getString("menu_version")}: $TOOL_VERSION)"
    stage.scene = scene
    stage.height = stage.height // On linux, Size have to be reassigned. If not, the scene hang when click menu button.
    stage.width = stage.width
    stage.show()
    stage.minWidth = 600.0
    stage.minHeight = 600.0
}

fun isDecimalString(input: String): Boolean {
    val pattern = Pattern.compile("[0-9]+")
    return pattern.matcher(input).matches()
}

fun isDateFormatString(input: String): Boolean {
    return try {
        DATE_FORMAT.parse(input)
        true
    } catch (e: Exception) {
        false
    }
}

fun getLocalizedArrayFromMap(map: MutableMap<String, String>): ArrayList<LocalizedString> {
    val localizedString = ArrayList<LocalizedString>()
    map.forEach { (key, set) ->
        localizedString.add(LocalizedString().apply {
            code = key
            value = set
        })
    }
    return localizedString
}

fun getMapFromLocalizedArray(localizedArray: ArrayList<LocalizedString>): MutableMap<String, String> {
    val map = mutableMapOf<String, String>()
    localizedArray.forEach {
        map[it.code] = it.value
    }
    return map
}
