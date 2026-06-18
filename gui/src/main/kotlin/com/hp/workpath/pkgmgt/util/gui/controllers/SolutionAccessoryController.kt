package com.hp.workpath.pkgmgt.util.gui.controllers

import com.hp.workpath.pkgmgt.util.gui.utilities.isDecimalString
import com.hp.workpath.pkgmgt.util.models.usbAccessories.UsbRegistrationModel
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.RadioButton
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup

class SolutionAccessoryController : Controller() {
    @FXML
    private lateinit var toggleAccessoryType: ToggleGroup

    @FXML
    private lateinit var rbOwned: RadioButton

    @FXML
    private lateinit var rbShared: RadioButton

    @FXML
    private lateinit var tfAccessoryVendorId: TextField

    @FXML
    private lateinit var tfAccessoryProductId: TextField

    @FXML
    private lateinit var tfAccessorySerialNumber: TextField

    @FXML
    private lateinit var cbAccessorySerialNumberNull: CheckBox

    @FXML
    private lateinit var btAdd: Button

    @FXML
    private lateinit var btCancel: Button

    lateinit var solutionControllerWindow: SolutionController

    private lateinit var accessoryModel: UsbRegistrationModel
    private var isNew: Boolean = false

    fun setAccessoryModel(input: UsbRegistrationModel) {
        btAdd.text = MESSAGE.getString("btn_save")
        accessoryModel = input
        isNew = false
        when (input.registration) {
            UsbRegistrationModel.RegistrationType.OWNED -> toggleAccessoryType.selectToggle(rbOwned)
            UsbRegistrationModel.RegistrationType.SHARED -> toggleAccessoryType.selectToggle(rbShared)
        }
        tfAccessoryVendorId.text = input.vendorId.toString()
        tfAccessoryProductId.text = input.productId.toString()
        if (input.serialNumber == null) {
            cbAccessorySerialNumberNull.isSelected = true
            tfAccessorySerialNumber.isDisable = true
        } else {
            tfAccessorySerialNumber.text = input.serialNumber
        }
        updateEnableState()
    }

    fun setReadOnly() {
        listOf(
            rbOwned,
            rbShared,
            cbAccessorySerialNumberNull
        ).forEach {
            it.isDisable = true
        }
        listOf(
            tfAccessoryVendorId,
            tfAccessoryProductId,
            tfAccessorySerialNumber
        ).forEach {
            it.isEditable = false
            it.promptText = null
        }
        btAdd.isVisible = false
        btAdd.isManaged = false
        btCancel.text = MESSAGE.getString("btn_close")
    }

    fun initView() {
        initListener()
        updateEnableState()
    }

    private fun initListener() {
        hasErrors.clear()
        hasErrors[tfAccessoryVendorId] = !isDecimalString(tfAccessoryVendorId.text)
        hasErrors[tfAccessoryProductId] = !isDecimalString(tfAccessoryProductId.text)
        tfAccessoryVendorId.textProperty().addListener { _, _, newValue ->
            updateTextFieldError(tfAccessoryVendorId, !isDecimalString(newValue))
        }
        tfAccessoryProductId.textProperty().addListener { _, _, newValue ->
            updateTextFieldError(tfAccessoryProductId, !isDecimalString(newValue))
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
    fun handleAccessorySerialNumberNull(event: ActionEvent) {
        tfAccessorySerialNumber.isDisable = cbAccessorySerialNumberNull.isSelected
    }

    @FXML
    fun handleAddAccessory(event: ActionEvent) {
        if (::solutionControllerWindow.isInitialized.not()) {
            // exception
            return
        }
        if (::accessoryModel.isInitialized.not()) {
            accessoryModel = UsbRegistrationModel()
            isNew = true
        }
        val accessoryModels: MutableList<UsbRegistrationModel> = solutionControllerWindow.accessoryList.toMutableList()
        if (!isNew) {
            accessoryModels.remove(accessoryModel)
        }
        accessoryModels.forEach {
            if (it.vendorId == tfAccessoryVendorId.text.toInt() || it.productId == tfAccessoryProductId.text.toInt()) {
                showMessagePopup(
                    State.WARNING,
                    MESSAGE.getString("dialog_title_warning"),
                    MESSAGE.getString("dialog_header_accessory_duplicate"),
                    MESSAGE.getString("dialog_content_accessory_duplicate")
                )
                return
            }
        }
        accessoryModel.registration = when (toggleAccessoryType.selectedToggle) {
            rbOwned -> UsbRegistrationModel.RegistrationType.OWNED
            rbShared -> UsbRegistrationModel.RegistrationType.SHARED
            else -> throw Exception("Invalid registration type: ${toggleAccessoryType.selectedToggle}")
        }
        accessoryModel.vendorId = tfAccessoryVendorId.text.toInt()
        accessoryModel.productId = tfAccessoryProductId.text.toInt()
        accessoryModel.serialNumber = if (cbAccessorySerialNumberNull.isSelected) {
            null
        } else {
            val normalized = if (::tfAccessorySerialNumber.isInitialized) {
                tfAccessorySerialNumber.text?.trim() ?: ""
            } else {
                ""
            }
            if (normalized.equals("null", ignoreCase = true) || normalized.isBlank()) null else normalized
        }

        solutionControllerWindow.setAccessory(accessoryModel, isNew)
        closeSubController(btAdd)
    }

    @FXML
    fun handleCancelAccessory(event: ActionEvent) {
        closeSubController(btCancel)
    }
}