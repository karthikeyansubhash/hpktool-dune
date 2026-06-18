package com.hp.workpath.pkgmgt.util.utilities

import java.net.http.HttpClient

interface HttpClientFactory {
    fun getHttpClient(): HttpClient
}
