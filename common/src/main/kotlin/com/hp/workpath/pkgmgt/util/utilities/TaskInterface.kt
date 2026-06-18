package com.hp.workpath.pkgmgt.util.utilities

interface TaskInterface {
    fun updateMessage(status: TaskStatus)
    fun onSucceed(obj: Any?)
    fun onFailed(e: Exception)
}
