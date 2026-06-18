package com.hp.workpath.pkgmgt.util.utilities

import com.hp.workpath.pkgmgt.util.models.connection.TaskState

data class TaskStatus(val state: TaskState, val cause: String)
