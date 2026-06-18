package com.hp.workpath.pkgmgt.util.models.hpk

class WebServiceEndPoint {
    var method: MethodType = MethodType.GET
    var category: String = ""
    var absolutePath: String = ""
    var authType: AuthType = AuthType.NONE

    fun setMethodType(methodType: String) {
        method = if (MethodType.GET.name.equals(methodType, ignoreCase = true)) {
            MethodType.GET
        } else if (MethodType.PUT.name.equals(methodType, ignoreCase = true)) {
            MethodType.PUT
        } else if (MethodType.POST.name.equals(methodType, ignoreCase = true)) {
            MethodType.POST
        } else if (MethodType.DELETE.name.equals(methodType, ignoreCase = true)) {
            MethodType.DELETE
        } else {
            throw IllegalArgumentException("Invalid method type: $methodType")
        }
    }

    fun setAuthType(authType: String) {
        this.authType = if (AuthType.NONE.value.equals(authType, ignoreCase = true)) {
            AuthType.NONE
        } else if (AuthType.XAUTH.value.equals(authType, ignoreCase = true)) {
            AuthType.XAUTH
        } else if (AuthType.ADMIN.value.equals(authType, ignoreCase = true)) {
            AuthType.ADMIN
        } else {
            throw IllegalArgumentException("Invalid auth type: $authType")
        }
    }

    enum class MethodType {
        GET, PUT, POST, DELETE;

        override fun toString(): String {
            return super.toString().lowercase()
        }
    }

    enum class AuthType(val value: String) {
        NONE("none"), XAUTH("x-auth"), ADMIN("admin");

        override fun toString(): String {
            return value
        }
    }
}