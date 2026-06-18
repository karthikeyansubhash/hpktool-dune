package com.hp.workpath.pkgmgt.util.models

import java.nio.file.Path

class ConnectionData {
    var networkAddress: String = ""
    var password: String = ""
    // install
    var installFilePath: Path = Path.of("")
    var installForce: Boolean = false
    // uninstall
    var solutionId: String = ""
    // config update
    var configData: String = ""
    // attestation update
    var attestationData: String = ""
    var key: String = ""
    var userName: String = ""
    var commandData: String = ""
}
