package com.hp.workpath.pkgmgt.util.gui.controllers

import com.hp.workpath.pkgmgt.util.gui.utilities.ActionType
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import com.hp.workpath.pkgmgt.util.utilities.TOOL_BUILD_DATE
import com.hp.workpath.pkgmgt.util.utilities.TOOL_VERSION
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Stage

class AboutController : Controller() {
    @FXML
    private lateinit var ivIcon: ImageView

    @FXML
    private lateinit var lbText: Label

    @FXML
    private lateinit var btOk: Button

    override fun setStage(stage: Stage, actionType: ActionType) {
        super.setStage(stage, actionType)
        initView()
    }

    private fun initView() {
        ivIcon.image = Image(javaClass.getResourceAsStream("/images/information.png"))
        lbText.text = "${MESSAGE.getString("menu_tool_name")}\n" +
                "${MESSAGE.getString("menu_version")} $TOOL_VERSION (${TOOL_BUILD_DATE})\n" +
                MESSAGE.getString("copy_right")
    }

    @FXML
    fun handleOk(event: ActionEvent) {
        closeSubController(btOk)
    }

    @FXML
    fun handleLicense(event: ActionEvent) {
        openRawDataController(
            "License",
            javaClass.getResource("/LICENSE")?.readText() ?: "error: LICENSE file is empty."
        )
    }
}