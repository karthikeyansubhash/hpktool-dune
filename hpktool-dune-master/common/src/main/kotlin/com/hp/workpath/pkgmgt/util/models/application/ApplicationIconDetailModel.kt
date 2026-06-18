package com.hp.workpath.pkgmgt.util.models.application

import com.hp.ext.types.application.ApplicationImage
import com.hp.ext.types.application.LocalImage
import com.hp.ext.types.common.ArchivePath
import com.hp.ext.types.common.InternetMediaType
import com.hp.workpath.pkgmgt.util.models.LocalContentModel
import com.hp.workpath.pkgmgt.util.utilities.EXCEPTION_UNSUPPORTED_IMAGE_TYPE

enum class ApplicationIconFormat {
    None,
    Inline,
    Local,
    Reference
}

/**
 * Workpath currently use Local Icon only.
 */
class ApplicationIconDetailModel {
    var isInIconSet: Boolean = false
    var key: String = ""
    var iconFormat = ApplicationIconFormat.Local
    var localIcon = LocalContentModel()

    fun from(fromType: ApplicationImage?) {
        if (fromType == null) {
            return
        }
        if (fromType.isLocalImage) {
            iconFormat = ApplicationIconFormat.Local
            localIcon.fileType = fromType.localImage.fileType.toString()
            localIcon.path = fromType.localImage.path.toString()
        } else {
            // Do nothing, we only support LocalImage Type for now.
        }
    }

    fun to(): ApplicationImage? {
        if (iconFormat == ApplicationIconFormat.Local) {
            return if (localIcon.originalPath != null) {
                ApplicationImage().apply {
                    this.localImage = LocalImage().apply {
                        fileType = InternetMediaType(localIcon.fileType)
                        path = ArchivePath(localIcon.path)
                    }
                }
            } else {
                null
            }
        } else {
            // Do nothing, we only support LocalImage Type for now.
            throw Exception(EXCEPTION_UNSUPPORTED_IMAGE_TYPE)
        }
    }
}