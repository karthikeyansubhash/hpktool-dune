package com.hp.workpath.pkgmgt.util.gui.controllers

import com.hp.workpath.pkgmgt.util.gui.utilities.HBOX_LINE
import com.hp.workpath.pkgmgt.util.gui.utilities.LABEL_NAME
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import com.hp.workpath.pkgmgt.util.utilities.getLocalizations
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class SolutionLocalizationController : Controller() {
    @FXML
    private lateinit var vbLocalizationBox: VBox

    @FXML
    private lateinit var btOk: Button

    @FXML
    private lateinit var btCancel: Button

    private var mainWindow: Controller? = null
    private var localizationMap = mutableMapOf<String, String>()
    private lateinit var resultFunc: (MutableMap<String, String>) -> Unit
    private var readOnly = false

    fun setValue(
        mainWindow: Controller,
        value: MutableMap<String, String>,
        resultFunc: (MutableMap<String, String>) -> Unit,
    ) {
        createLocalizationLayout()
        this.mainWindow = mainWindow
        localizationMap = value
        setLocalizationText()
        this.resultFunc = resultFunc
    }

    fun setReadOnly(readOnly: Boolean) {
        this.readOnly = readOnly
        if (readOnly) {
            vbLocalizationBox.children.forEach { hb ->
                if (hb is HBox) {
                    hb.children.forEach { node ->
                        if (node is TextField) {
                            node.isEditable = false
                        }
                    }
                }
            }
        }
    }

    private fun createLocalizationLayout() {
        val locals: List<String> = getLocalizations()
        locals.forEach {
            val hBox = HBox().also { hb ->
                hb.styleClass.add(HBOX_LINE)
                val label = Label().also { lb ->
                    lb.styleClass.add(LABEL_NAME)
                    lb.text = it
                }
                HBox.setHgrow(label, Priority.NEVER)
                val textField = TextField().also { tf ->
                    tf.id = it
                    tf.promptText = "${MESSAGE.getString("hint_localized_string")}($it)"
                }
                HBox.setHgrow(textField, Priority.ALWAYS)
                hb.children.addAll(label, textField)
            }
            vbLocalizationBox.children.add(hBox)
        }
    }

    private fun setLocalizationText() {
        vbLocalizationBox.children.forEach { hbox ->
            (hbox as HBox).children.forEach { comp ->
                if (comp.id != null) {
                    localizationMap.forEach { (locale, value) ->
                        if (comp.id.equals(locale)) {
                            (comp as TextField).text = value
                            return@forEach
                        }
                    }
                }
            }
        }
    }

    private fun saveLocalizationText() {
        localizationMap = mutableMapOf<String, String>()
        vbLocalizationBox.children.forEach { hbox ->
            (hbox as HBox).children.forEach { comp ->
                if (comp.id != null) {
                    if ((comp as TextField).text.isNotEmpty()) {
                        localizationMap[comp.id] = comp.text
                    }
                }
            }
        }
    }

    @FXML
    fun handleOk(event: ActionEvent) {
        setResult()
        closeSubController(btOk)
    }

    private fun setResult() {
        saveLocalizationText()
        resultFunc(localizationMap)
    }

    @FXML
    fun handleCancel(event: ActionEvent) {
        closeSubController(btCancel)
    }
}