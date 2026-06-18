package com.hp.workpath.pkgmgt.util.models.usbAccessories

import com.hp.ext.service.usbAccessories.RegistrationKind
import com.hp.ext.service.usbAccessories.UsbRegistrationIdentification
import com.hp.ext.service.usbAccessories.UsbString
import com.hp.ext.types.protocol.Unsigned16

class UsbRegistrationModel {
    var productId: Int = 0
    var registration = RegistrationType.OWNED
    // Serial Number Null means Workpath system accept all accessories with the same vendorId and productId,
    // and empty string means Workpath system accept an accessory with the same vendorId, productId and empty serial number.
    var serialNumber: String? = null
    var vendorId: Int = 0

    fun setRegistrationType(registrationType: String) {
        registration = if (RegistrationType.OWNED.value.equals(registrationType, ignoreCase = true)) {
            RegistrationType.OWNED
        } else if (RegistrationType.SHARED.value.equals(registrationType, ignoreCase = true)) {
            RegistrationType.SHARED
        } else {
            throw IllegalArgumentException("Invalid registration type: $registrationType")
        }
    }

    enum class RegistrationType(val value: String, private val registrationKind: RegistrationKind) {
        OWNED("OWNED", RegistrationKind.RkOwned), SHARED("SHARED", RegistrationKind.RkShared);

        fun toRegistrationKind(): RegistrationKind {
            return registrationKind
        }
    }

    fun from(fromType: UsbRegistrationIdentification) {
        productId = fromType.productId.toString().toInt()
        vendorId = fromType.vendorId.toString().toInt()
        serialNumber = fromType.serialNumber?.toString()
        registration = when (fromType.registrationKind) {
            RegistrationKind.RkOwned -> RegistrationType.OWNED
            RegistrationKind.RkShared -> RegistrationType.SHARED
            else -> throw Exception("Invalid registration kind: ${fromType.registrationKind}")
        }
    }

    fun to(): UsbRegistrationIdentification {
        return UsbRegistrationIdentification().also {
            it.productId = Unsigned16(productId)
            it.vendorId = Unsigned16(vendorId)
            it.serialNumber = if (serialNumber != null) {
                UsbString(serialNumber)
            } else {
                null
            }
            it.registrationKind = registration.toRegistrationKind()
        }
    }
}