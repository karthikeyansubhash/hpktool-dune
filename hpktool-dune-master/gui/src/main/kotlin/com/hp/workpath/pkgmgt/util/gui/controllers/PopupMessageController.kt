package com.hp.workpath.pkgmgt.util.gui.controllers

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView

class PopupMessageController : Controller() {
    @FXML
    private lateinit var ivIcon: ImageView

    @FXML
    private lateinit var lbTitle: Label

    @FXML
    private lateinit var lbContext: Label

    @FXML
    private lateinit var btOk: Button

    @FXML
    private lateinit var btCancel: Button

    private var buttonClickListener: ButtonClickListener? = null

    fun initView(state: State, headerText: String, contentText: String, buttonClickListener: ButtonClickListener?) {
        val image = Image(
            javaClass.getResourceAsStream(
                when (state) {
                    //TODO change image files to new resources
                    State.INFORMATION -> "/images/information.png"
                    State.WARNING -> "/images/warning.png"
                    State.ERROR -> "/images/error.png"
                }
            )
        )
        ivIcon.image = image
        lbTitle.text = headerText
        if (headerText.isEmpty()) {
            setVisibility(lbTitle, false)
        }
        lbContext.text = contentText
        if (contentText.isEmpty()) {
            setVisibility(lbContext, false)
        }
        if (buttonClickListener == null) {
            setVisibility(btCancel, false)
        } else {
            setVisibility(btCancel, true)
            this.buttonClickListener = buttonClickListener
        }
    }

    @FXML
    fun handleOk() {
        buttonClickListener?.ok()
        closeSubController(btOk)
    }

    @FXML
    fun handleCancel() {
        buttonClickListener?.cancel()
        closeSubController(btCancel)
    }
}