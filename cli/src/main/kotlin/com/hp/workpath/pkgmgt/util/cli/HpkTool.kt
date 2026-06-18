package com.hp.workpath.pkgmgt.util.cli

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.hp.ext.service.solutionManager.Solution
import com.hp.ext.service.solutionManager.Solutions
import com.hp.workpath.pkgmgt.util.models.ConnectionData
import com.hp.workpath.pkgmgt.util.models.SolutionProject
import com.hp.workpath.pkgmgt.util.models.connection.TaskState
import com.hp.workpath.pkgmgt.util.services.ConnectionService
import com.hp.workpath.pkgmgt.util.services.WorkpathSolutionProjectService
import com.hp.workpath.pkgmgt.util.utilities.EXCEPTION_LOG_SAVE_FILE
import com.hp.workpath.pkgmgt.util.utilities.FAILED
import com.hp.workpath.pkgmgt.util.utilities.LOG_FILE_DATETIME_PATTERN
import com.hp.workpath.pkgmgt.util.utilities.LOG_FILE_DIRECTORY
import com.hp.workpath.pkgmgt.util.utilities.MESSAGE
import com.hp.workpath.pkgmgt.util.utilities.OPERATION_RESULT
import com.hp.workpath.pkgmgt.util.utilities.RESULT_SOLUTION_ID
import com.hp.workpath.pkgmgt.util.utilities.TaskInterface
import com.hp.workpath.pkgmgt.util.utilities.TaskStatus
import com.hp.workpath.pkgmgt.util.utilities.deleteTempDirectory
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        processCli(args)
    } catch (e: Throwable) {
        exitWithError(e, 1)
    } finally {
        deleteTempDirectory()
        exitProcess(0)
    }
}

private fun processCli(args: Array<String>) {
    val hpkParser = HpkParser()
    val cmd = hpkParser.parse(*args)
    when (hpkParser.getMainCommand(cmd)) {
        HpkParser.CliCommands.NULL -> hpkParser.printHelp()
        HpkParser.CliCommands.HELP -> hpkParser.printHelp()
        HpkParser.CliCommands.CREATE -> processCreateBundle(hpkParser.getSolutionProjectFromCommand(cmd))
        HpkParser.CliCommands.JSON -> processCreateWithJsonBundle(hpkParser.getJsonBundleFromCommand(cmd))
        HpkParser.CliCommands.INSTALL -> processInstallBundle(hpkParser.getConnectionDataFromCommand(cmd))
        HpkParser.CliCommands.UNINSTALL -> processUninstall(hpkParser.getConnectionDataFromCommand(cmd))
        HpkParser.CliCommands.SOLUTION_LIST -> processSolutionList(hpkParser.getConnectionDataFromCommand(cmd))
        HpkParser.CliCommands.SOLUTION_DETAIL -> processSolutionDetail(hpkParser.getConnectionDataFromCommand(cmd))
        HpkParser.CliCommands.CONFIG_GET -> processConfigurationGet(hpkParser.getConnectionDataFromCommand(cmd))
        HpkParser.CliCommands.CONFIG_UPDATE -> processConfigurationUpdate(hpkParser.getConnectionDataFromCommand(cmd))
        HpkParser.CliCommands.ATTESTATION_UPDATE -> processAttestationUpdate(hpkParser.getAttestationDataFromCommand(cmd))
        HpkParser.CliCommands.ERR -> throw IllegalArgumentException("Too many main commands")
    }
}

private fun exitWithError(e: Throwable, status: Int) {
    val message = e.message ?: FAILED
    println("${MESSAGE.getString("task_error_prefix")}$message")
    try {
        Files.createDirectories(Paths.get(".", LOG_FILE_DIRECTORY))
        val printStream = PrintStream(
            Paths.get(
                ".",
                LOG_FILE_DIRECTORY,
                "${LocalDateTime.now().format(DateTimeFormatter.ofPattern(LOG_FILE_DATETIME_PATTERN))}.log"
            ).toFile()
        )
        printStream.println(e.message)
        e.printStackTrace(printStream)
        printStream.close()
    } catch (exception: Exception) {
        println("$EXCEPTION_LOG_SAVE_FILE${e.message}")
        exception.printStackTrace()
    }
    exitProcess(status)
}

private fun processCreateBundle(solutionProject: SolutionProject) {
    WorkpathSolutionProjectService(solutionProject, taskInterface).run {
        createBundle()
        if (solutionProject.outputHpkPath != null) {
            createHpkBundle()
        }
    }
}

private fun processCreateWithJsonBundle(solutionProject: SolutionProject) {
    WorkpathSolutionProjectService(solutionProject, taskInterface).run {
        createBundleWithJson()
        if (solutionProject.outputHpkPath != null) {
            createHpkBundle()
        }
    }
}

private fun processInstallBundle(connectionData: ConnectionData) {
    ConnectionService(connectionData, taskInterface).installSolution()
}

private fun processUninstall(connectionData: ConnectionData) {
    ConnectionService(connectionData, taskInterface).uninstallSolution()
}

private fun processSolutionList(connectionData: ConnectionData) {
    ConnectionService(connectionData, taskInterface).enumerateSolutions()
}

private fun processSolutionDetail(connectionData: ConnectionData) {
    ConnectionService(connectionData, taskInterface).getSolution()
}

private fun processConfigurationGet(connectionData: ConnectionData) {
    ConnectionService(connectionData, taskInterface).getConfigurationData()
}

private fun processConfigurationUpdate(connectionData: ConnectionData) {
    ConnectionService(connectionData, taskInterface).updateConfigurationData()
}

private fun processAttestationUpdate(connectionData: ConnectionData) {
    ConnectionService(connectionData, taskInterface).updateAttestationData()
}

private val taskInterface = object : TaskInterface {
    override fun updateMessage(status: TaskStatus) {
        when (status.state) {
            TaskState.Connecting -> println(MESSAGE.getString("task_connecting"))
            TaskState.Authorizing -> println(MESSAGE.getString("task_authorizing"))
            TaskState.CheckingStatus -> println(MESSAGE.getString("task_checking_status"))
            TaskState.Sending -> println(MESSAGE.getString("task_sending"))
            TaskState.Sending_Broadcast -> println(MESSAGE.getString("task_send_broadcasting"))
            TaskState.InProgress -> println(MESSAGE.getString("task_in_progress"))
            TaskState.Completed -> onSucceed(null)
            TaskState.Failed -> println("${MESSAGE.getString("task_error_prefix")}${status.cause}")
            else -> println("${MESSAGE.getString("task_error_prefix")}${status.cause}")
        }
    }

    override fun onSucceed(obj: Any?) {
        if (obj != null) {
            when (obj) {
                is String -> {
                    println("$RESULT_SOLUTION_ID$obj")
                }

                is Solutions -> {
                    println(OPERATION_RESULT)
                    println(
                        ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                            .writerWithDefaultPrettyPrinter().writeValueAsString(obj.members)
                    )
                }

                is Solution -> {
                    println(OPERATION_RESULT)
                    println(
                        ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                            .writerWithDefaultPrettyPrinter().writeValueAsString(obj)
                    )
                }

                is Map<*, *> -> {
                    println(OPERATION_RESULT)
                    println(String(obj["item2"] as ByteArray))
                }
            }
        }
        println(MESSAGE.getString("task_done"))
    }

    override fun onFailed(e: Exception) {
        exitWithError(e, 2)
    }
}
