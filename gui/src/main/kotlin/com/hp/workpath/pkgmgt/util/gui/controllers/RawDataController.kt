package com.hp.workpath.pkgmgt.util.gui.controllers

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextArea

class RawDataController : Controller() {
    @FXML
    private lateinit var taData: TextArea

    @FXML
    private lateinit var btClose: Button

    private var data: String = ""

    fun setValue(value: String) {
        data = value
        initView()
    }

    private fun initView() {
        taData.text = data
    }

    @FXML
    fun handleClose(event: ActionEvent) {
        closeSubController(btClose)
    }
}