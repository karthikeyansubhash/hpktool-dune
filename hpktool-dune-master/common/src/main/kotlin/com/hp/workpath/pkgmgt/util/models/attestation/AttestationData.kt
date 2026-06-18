package com.hp.workpath.pkgmgt.util.models.attestation

data class AttestationData(
    var host: String = "",
    var password: String = "",
    var uuid: String = "",
    var userName: String = "",
    var key: String = "",
    var attestationData: String = "",
    var commandData: String = ""
)

