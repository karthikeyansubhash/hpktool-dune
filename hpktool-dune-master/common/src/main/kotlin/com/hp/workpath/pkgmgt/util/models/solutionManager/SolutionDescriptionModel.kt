package com.hp.workpath.pkgmgt.util.models.solutionManager

import com.hp.ext.types.solutionManager.SolutionDescription

class SolutionDescriptionModel {
    var description: String = ""
    var name: String = ""
    var supportEmail: String? = null
    var supportPhone: String? = null
    var supportUrl: String? = null
    var vendor: String = ""
    var version: String = ""
    var date: String = ""
    var versionNumber : Long? = 1

    fun from(fromType: SolutionDescription) {
        this.description = fromType.description
        this.name = fromType.name
        this.supportEmail = fromType.supportEmail
        this.supportPhone = fromType.supportPhone
        this.supportUrl = fromType.supportUrl
        this.vendor = fromType.vendor
        this.version = fromType.version
        this.date = fromType.date ?: "" // TODO(after beta 7)
        this.versionNumber = fromType.versionNumber
    }

    fun to(): SolutionDescription {
        val solutionDescription = SolutionDescription()
        solutionDescription.description = this.description
        solutionDescription.name = this.name
        solutionDescription.supportEmail = this.supportEmail
        solutionDescription.supportPhone = this.supportPhone
        solutionDescription.supportUrl = this.supportUrl
        solutionDescription.vendor = this.vendor
        solutionDescription.version = this.version
        solutionDescription.date = this.date
        solutionDescription.versionNumber = this.versionNumber
        return solutionDescription
    }
}