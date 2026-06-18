package com.hp.workpath.pkgmgt.util.gui.controllers

import com.hp.workpath.pkgmgt.util.gui.models.ApplicationInfo
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

class ManagementDetailApplicationController : Controller() {
    @FXML
    private lateinit var vbBackground: VBox

    @FXML
    private lateinit var tfApplicationId: TextField

    @FXML
    private lateinit var tfApplicationName: TextField

    @FXML
    private lateinit var tfApplicationPath: TextField

    @FXML
    private lateinit var tfApplicationTitle: TextField
    private var applicationTitleMap = mutableMapOf<String, String>()
        set(value) {
            field = value
            tfApplicationTitle.text = field["en-US"]
        }

    @FXML
    private lateinit var tfApplicationDescription: TextField
    private var applicationDescriptionMap = mutableMapOf<String, String>()
        set(value) {
            field = value
            tfApplicationDescription.text = field["en-US"]
        }

    /**   can not bring icon file from device */
//    @FXML
//    private lateinit var ivApplicationIcon: ImageView
//
//    private var applicationIconFile: File? = null
//        set(value) {
//            field = value
//            ivApplicationIcon.image = if (field != null) {
//                Image(field!!.toURI().toString())
//            } else {
//                null
//            }
//        }

    var applicationType = ApplicationInfo.ApplicationType.MAIN
        set(value) {
            field = value
            when (field) {
                ApplicationInfo.ApplicationType.MAIN -> {
                    setVisibility(hbApplicationType, true)
                    cbApplicationHome.isSelected = false
                    cbApplicationHomeDefault.isDisable = true
                    cbApplicationHomeDefault.isSelected = false
                }

                ApplicationInfo.ApplicationType.SUB -> {
                    setVisibility(hbApplicationType, false)
                }

                ApplicationInfo.ApplicationType.HOME -> {
                    setVisibility(hbApplicationType, true)
                    cbApplicationHome.isSelected = true
                    cbApplicationHomeDefault.isDisable = false
                    cbApplicationHomeDefault.isSelected = false
                }

                ApplicationInfo.ApplicationType.HOME_DEFAULT -> {
                    setVisibility(hbApplicationType, true)
                    cbApplicationHome.isSelected = true
                    cbApplicationHomeDefault.isDisable = false
                    cbApplicationHomeDefault.isSelected = true
                }
            }
        }

    @FXML
    private lateinit var hbApplicationType: HBox

    @FXML
    private lateinit var cbApplicationHome: CheckBox

    @FXML
    private lateinit var cbApplicationHomeDefault: CheckBox

    @FXML
    private lateinit var btClose: Button

    private lateinit var applicationInfo: ApplicationInfo

    fun setApplicationInfo(input: ApplicationInfo) {
        applicationInfo = input
        tfApplicationId.text = input.applicationId
        tfApplicationName.text = input.applicationName
        tfApplicationPath.text = input.applicationPath
        applicationTitleMap = input.applicationTitle
        applicationDescriptionMap = input.applicationDescription
//        input.applicationIconPath
        applicationType = input.applicationType
    }

    @FXML
    fun handleApplicationTitle(event: ActionEvent) {
        openLocalizationController(
            vbBackground.scene.window,
            MESSAGE.getString("label_application_title"),
            applicationTitleMap,
            true
        ) { }
    }

    @FXML
    fun handleApplicationDescription(event: ActionEvent) {
        openLocalizationController(
            vbBackground.scene.window,
            MESSAGE.getString("label_application_description"),
            applicationDescriptionMap,
            true
        ) { }
    }

    @FXML
    fun handleClose(event: ActionEvent) {
        closeSubController(btClose)
    }
}