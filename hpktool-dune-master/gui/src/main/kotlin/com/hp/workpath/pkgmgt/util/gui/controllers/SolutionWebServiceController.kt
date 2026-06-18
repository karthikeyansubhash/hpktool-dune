package com.hp.workpath.pkgmgt.util.gui.controllers

import com.hp.workpath.pkgmgt.util.models.hpk.WebServiceEndPoint
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.RadioButton
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import java.lang.IllegalArgumentException

class SolutionWebServiceController : Controller() {
    @FXML
    private lateinit var toggleWebServiceMethod: ToggleGroup

    @FXML
    private lateinit var rbGet: RadioButton

    @FXML
    private lateinit var rbPut: RadioButton

    @FXML
    private lateinit var rbPost: RadioButton

    @FXML
    private lateinit var rbDelete: RadioButton

    @FXML
    private lateinit var tfWebServiceCategory: TextField

    @FXML
    private lateinit var tfWebServiceAbsolutePath: TextField

    @FXML
    private lateinit var toggleWebServiceAuthType: ToggleGroup

    @FXML
    private lateinit var rbNone: RadioButton

    @FXML
    private lateinit var rbXAuth: RadioButton

    @FXML
    private lateinit var rbAdmin: RadioButton

    @FXML
    private lateinit var btAdd: Button

    @FXML
    private lateinit var btCancel: Button

    var mainWindow: SolutionController? = null
        set(value) {
            field = value
            initView()
        }

    private var endPoint: WebServiceEndPoint? = null

    fun setWebServiceEndPoint(input: WebServiceEndPoint) {
        btAdd.text = MESSAGE.getString("btn_save")
        endPoint = input
        when (input.method) {
            WebServiceEndPoint.MethodType.GET -> toggleWebServiceMethod.selectToggle(rbGet)
            WebServiceEndPoint.MethodType.PUT -> toggleWebServiceMethod.selectToggle(rbPut)
            WebServiceEndPoint.MethodType.POST -> toggleWebServiceMethod.selectToggle(rbPost)
            WebServiceEndPoint.MethodType.DELETE -> toggleWebServiceMethod.selectToggle(rbDelete)
        }
        tfWebServiceCategory.text = input.category
        tfWebServiceAbsolutePath.text = input.absolutePath
        when (input.authType) {
            WebServiceEndPoint.AuthType.NONE -> toggleWebServiceAuthType.selectToggle(rbNone)
            WebServiceEndPoint.AuthType.XAUTH -> toggleWebServiceAuthType.selectToggle(rbXAuth)
            WebServiceEndPoint.AuthType.ADMIN -> toggleWebServiceAuthType.selectToggle(rbAdmin)
        }

        updateEnableState()
    }

    private fun initView() {
        initListener()
        updateEnableState()
    }

    private fun initListener() {
        hasErrors.clear()
        hasErrors[tfWebServiceCategory] = tfWebServiceCategory.text.isNullOrEmpty()
        hasErrors[tfWebServiceAbsolutePath] = tfWebServiceAbsolutePath.text.isNullOrEmpty()
        tfWebServiceCategory.textProperty().addListener { _, _, _ ->
            updateTextFieldEmpty(tfWebServiceCategory)
        }
        tfWebServiceAbsolutePath.textProperty().addListener { _, _, _ ->
            updateTextFieldEmpty(tfWebServiceAbsolutePath)
        }
    }

    override fun updateEnableState() {
        hasErrors.forEach { (_, value) ->
            if (value) {
                btAdd.isDisable = true
                return
            }
        }
        btAdd.isDisable = false
    }

    @FXML
    fun handleAddAccessory(event: ActionEvent) {
        var isNew = false
        if (endPoint == null) {
            endPoint = WebServiceEndPoint()
            isNew = true
        }
        val endPoints: MutableList<WebServiceEndPoint> = mainWindow!!.webServiceList.toMutableList()
        if (!isNew) {
            endPoints.remove(endPoint)
        }
        endPoint!!.method = when (toggleWebServiceMethod.selectedToggle) {
            rbGet -> WebServiceEndPoint.MethodType.GET
            rbPut -> WebServiceEndPoint.MethodType.PUT
            rbPost -> WebServiceEndPoint.MethodType.POST
            rbDelete -> WebServiceEndPoint.MethodType.DELETE
            else -> throw IllegalArgumentException("Invalid method type: ${toggleWebServiceMethod.selectedToggle}")
        }
        endPoint!!.category = tfWebServiceCategory.text
        endPoint!!.absolutePath = tfWebServiceAbsolutePath.text
        endPoint!!.authType = when (toggleWebServiceAuthType.selectedToggle) {
            rbNone -> WebServiceEndPoint.AuthType.NONE
            rbXAuth -> WebServiceEndPoint.AuthType.XAUTH
            rbAdmin -> WebServiceEndPoint.AuthType.ADMIN
            else -> throw IllegalArgumentException("Invalid auth type: ${toggleWebServiceAuthType.selectedToggle}")
        }
        endPoints.forEach {
            if (it.method == endPoint!!.method
                && it.category == endPoint!!.category
                && it.absolutePath == endPoint!!.absolutePath
                && it.authType == endPoint!!.authType
            ) {
                endPoint = null
                showMessagePopup(
                    State.WARNING,
                    MESSAGE.getString("dialog_title_warning"),
                    MESSAGE.getString("dialog_header_webservice_duplicate"),
                    MESSAGE.getString("dialog_content_webservice_duplicate")
                )
                return
            }
        }

        mainWindow!!.setWebService(endPoint!!, isNew)
        closeSubController(btAdd)
    }

    @FXML
    fun handleCancelAccessory(event: ActionEvent) {
        closeSubController(btCancel)
    }
}