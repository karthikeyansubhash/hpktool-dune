package com.hp.workpath.pkgmgt.util.gui.controllers

import com.hp.workpath.pkgmgt.util.gui.utilities.BUTTON_WHITE
import com.hp.workpath.pkgmgt.util.gui.utilities.HBOX_LINE
import com.hp.workpath.pkgmgt.util.gui.utilities.LABEL_NAME
import com.hp.workpath.pkgmgt.util.gui.utilities.VBOX_IMAGE
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import com.hp.workpath.pkgmgt.util.utilities.getIconSizes
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import java.io.File

class SolutionIconSetController : Controller() {
    @FXML
    private lateinit var vbIconMapBox: VBox

    @FXML
    private lateinit var btOk: Button

    @FXML
    private lateinit var btCancel: Button

    private var mainWindow: Controller? = null

    private lateinit var ivDefault: ImageView
    private var icon: File? = null
        set(value) {
            field = value
            setImageViewImage()
        }
    private lateinit var btClearDefault: Button

    private var ivIconMap = mutableMapOf<String, ImageView>()
    private var iconMap = mutableMapOf<String, File>()
        set(value) {
            field = value
            setImageViewImage()
        }
    private lateinit var resultFunc: (File?, MutableMap<String, File>) -> Unit

    fun setValue(
        mainWindow: Controller,
        icon: File?,
        iconMap: MutableMap<String, File>,
        callback: (File?, MutableMap<String, File>) -> Unit,
    ) {
        createIconSetLayout()
        this.mainWindow = mainWindow
        this.icon = icon
        this.iconMap = iconMap.toMutableMap()
        resultFunc = callback
    }

    private fun createIconSetLayout() {
        vbIconMapBox.children.add(createDefaultImageViewBox())
        val iconSizes: List<String> = getIconSizes()
        iconSizes.forEach {
            vbIconMapBox.children.add(createImageViewBox(it))
        }
    }

    private fun createDefaultImageViewBox(): HBox {
        return HBox().also { hb ->
            hb.styleClass.add(HBOX_LINE)
            val label = Label().also { lb ->
                lb.styleClass.add(LABEL_NAME)
                lb.text = "Default"
            }
            HBox.setHgrow(label, Priority.NEVER)
            val vbox = VBox().also { vb ->
                vb.styleClass.add(VBOX_IMAGE)
                ivDefault = ImageView().also { iv ->
                    // iv.image is init on setIconMap
                    iv.fitWidth = 80.0
                    iv.fitHeight = 80.0
                    iv.isPreserveRatio = true
                }
                vb.children.addAll(ivDefault)
            }
            HBox.setHgrow(vbox, Priority.ALWAYS)
            btClearDefault = Button().also { clear ->
                clear.styleClass.add(BUTTON_WHITE)
                clear.text = MESSAGE.getString("btn_clear")
                clear.isMnemonicParsing = false
                clear.onAction = EventHandler { _ ->
                    icon = null
                }
            }
            HBox.setHgrow(btClearDefault, Priority.NEVER)
            val selectButton = Button().also { select ->
                select.text = MESSAGE.getString("btn_select_file")
                select.isMnemonicParsing = false
                select.onAction = EventHandler { _ ->
                    val selected = openFile(stage, FileExtension.IMAGE, FileChooserMode.OPEN)
                    if (selected != null) {
                        icon = selected
                    }
                }
            }
            HBox.setHgrow(selectButton, Priority.NEVER)
            hb.children.addAll(label, vbox, btClearDefault, selectButton)
        }
    }

    private fun createImageViewBox(input: String): HBox {
        return HBox().also { hb ->
            hb.styleClass.add(HBOX_LINE)
            val label = Label().also { lb ->
                lb.styleClass.add(LABEL_NAME)
                lb.text = input
            }
            HBox.setHgrow(label, Priority.NEVER)
            val vbox = VBox().also { vb ->
                vb.styleClass.add(VBOX_IMAGE)
                val imageView = ImageView().also { iv ->
                    // iv.image //init on setIconMap
                    iv.fitWidth = 80.0
                    iv.fitHeight = 80.0
                    iv.isPreserveRatio = true
                }
                vb.children.addAll(imageView)
                ivIconMap[input] = imageView
            }
            HBox.setHgrow(vbox, Priority.ALWAYS)
            val clearButton = Button().also { clear ->
                clear.styleClass.add(BUTTON_WHITE)
                clear.text = MESSAGE.getString("btn_clear")
                clear.isMnemonicParsing = false
                clear.onAction = EventHandler { _ ->
                    val iconMapTemp = iconMap
                    if (iconMapTemp.containsKey(input)) {
                        iconMapTemp.remove(input)
                        iconMap = iconMapTemp
                    }
                }
            }
            HBox.setHgrow(clearButton, Priority.NEVER)
            val selectButton = Button().also { select ->
                select.text = MESSAGE.getString("btn_select_file")
                select.isMnemonicParsing = false
                select.onAction = EventHandler { _ ->
                    val iconMapTemp = iconMap
                    val selected = openFile(stage, FileExtension.IMAGE, FileChooserMode.OPEN)
                    if (selected != null) {
                        iconMapTemp[input] = selected
                        iconMap = iconMapTemp
                    }
                }
            }
            HBox.setHgrow(selectButton, Priority.NEVER)
            hb.children.addAll(label, vbox, clearButton, selectButton)
        }
    }

    private fun setImageViewImage() {
        ivDefault.image =
            if (icon != null) {
                Image(icon!!.toURI().toString())
            } else {
                if (iconMap.isNotEmpty()) {
                    Image(iconMap.values.first().toURI().toString())
                } else {
                    null
                }
            }
        ivIconMap.forEach { (key, value) ->
            value.image =
                if (iconMap.containsKey(key)) {
                    Image(iconMap[key]!!.toURI().toString())
                } else {
                    null
                }
        }
        btClearDefault.isDisable = icon == null
    }

    @FXML
    fun handleOk(event: ActionEvent) {
        resultFunc(icon, iconMap)
        closeSubController(btOk)
    }

    @FXML
    fun handleCancel(event: ActionEvent) {
        closeSubController(btCancel)
    }
}