package com.hp.workpath.pkgmgt.util.gui

import com.hp.workpath.pkgmgt.util.gui.utilities.ActionType
import com.hp.workpath.pkgmgt.util.gui.utilities.changeScreen
import com.hp.workpath.pkgmgt.util.utilities.LATEST_HPK_VERSION
import com.hp.workpath.pkgmgt.util.utilities.LATEST_PLATFORM_VERSION
import com.hp.workpath.pkgmgt.util.utilities.deleteTempDirectory
import javafx.application.Application
import javafx.stage.Stage

fun main() {
    Application.launch(HpkTool::class.java)
}

var currentHpkVersion = LATEST_HPK_VERSION
var currentPlatformVersion = LATEST_PLATFORM_VERSION

class HpkTool : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.width=650.0
        primaryStage.height=600.0
        changeScreen(primaryStage, javaClass, ActionType.SOLUTION_NEW)

        primaryStage.setOnCloseRequest {
            deleteTempDirectory()
        }
    }
}
