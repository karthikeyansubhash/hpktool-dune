package com.hp.workpath.pkgmgt.util.gui.services

import com.hp.workpath.pkgmgt.util.models.SolutionProject
import com.hp.workpath.pkgmgt.util.services.WorkpathSolutionProjectService
import com.hp.workpath.pkgmgt.util.utilities.TaskInterface
import javafx.concurrent.Service
import javafx.concurrent.Task

enum class WorkpathSolutionProjectServiceType {
    CREATE_BUNDLE, CREATE_HPK_BUNDLE,
}

class WorkpathSolutionProjectServiceGui(
    solutionProject: SolutionProject,
    taskInterface: TaskInterface,
    private val workpathSolutionProjectServiceType: WorkpathSolutionProjectServiceType,
) : Service<Boolean>() {
    private var workpathSolutionProjectService: WorkpathSolutionProjectService

    init {
        workpathSolutionProjectService = WorkpathSolutionProjectService(solutionProject, taskInterface)
    }

    fun execute() {
        when (workpathSolutionProjectServiceType) {
            WorkpathSolutionProjectServiceType.CREATE_BUNDLE -> workpathSolutionProjectService.createBundle()
            WorkpathSolutionProjectServiceType.CREATE_HPK_BUNDLE -> workpathSolutionProjectService.createHpkBundle()
        }
    }

    override fun createTask(): Task<Boolean> {
        return object : Task<Boolean>() {
            override fun call(): Boolean? {
                execute()
                return null
            }

        }
    }

}