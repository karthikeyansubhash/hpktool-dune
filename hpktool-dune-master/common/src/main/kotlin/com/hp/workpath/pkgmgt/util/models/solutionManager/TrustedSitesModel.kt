package com.hp.workpath.pkgmgt.util.models.solutionManager

import com.hp.ext.types.solutionManager.TrustedSitesContent
import com.hp.ext.types.solutionManager.TrustedSitesContent_Sites_Binding
import com.hp.ext.types.solutionManager.TrustedSitesContent_Sites_Value

class TrustedSitesModel {
    // TODO DUNE-195685
    var includeTrustedSites: Boolean = true
    var trustedSites: String = "*"

    fun from(fromType: TrustedSitesContent) {
        if (fromType.sites.isExplicit) {
            includeTrustedSites = true
            trustedSites = fromType.sites.explicit.explicitValue
        }
    }

    fun to(): TrustedSitesContent {
        return TrustedSitesContent().apply {
            sites = TrustedSitesContent_Sites_Binding().apply {
                explicit = TrustedSitesContent_Sites_Value().apply {
                    explicitValue = trustedSites
                }
            }
        }
    }
}