package com.hp.workpath.pkgmgt.util.models.connection

import com.hp.ext.clients.oauth2.Token

data class AccessTokenInfo(val type: AccessTokenType, val token: Token?, val status: TokenStatus)
