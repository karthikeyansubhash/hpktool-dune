package com.hp.workpath.pkgmgt.util.models

import java.nio.file.Path

class LocalContentModel {
    var path: String = ""
    var fileType: String = ""
    var fileName: String = ""
    var subFolder: String = ""
    var originalPath: Path? = null
}