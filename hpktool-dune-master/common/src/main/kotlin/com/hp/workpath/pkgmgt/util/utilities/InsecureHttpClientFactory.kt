package com.hp.workpath.pkgmgt.util.utilities

import java.net.http.HttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.Array
import kotlin.String
import kotlin.arrayOf

class InsecureHttpClientFactory : HttpClientFactory {
    override fun getHttpClient(): HttpClient {
        val properties = System.getProperties()
        properties.setProperty("jdk.internal.httpclient.disableHostnameVerification", true.toString())

        return HttpClient.newBuilder()
            .sslContext(insecureContext())
            .build()
    }

    private fun insecureContext(): SSLContext {
        return SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
                override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }), null)
        }
    }
}
