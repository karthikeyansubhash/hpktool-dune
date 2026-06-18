package com.hp.workpath.pkgmgt.util.models.connection

enum class TaskState {
    Connecting,
    Authorizing,
    CheckingStatus,
    Sending,
    InProgress,
    Completed,
    Failed,
    Sending_Broadcast,
}
