package com.hp.workpath.pkgmgt.util.cli

import com.fasterxml.jackson.databind.ObjectMapper
import com.hp.ext.types.solutionManager.NotificationType
import com.hp.ext.types.solutionManager.SolutionManifest
import com.hp.workpath.pkgmgt.util.cli.utilities.getIconMapFromJsonString
import com.hp.workpath.pkgmgt.util.cli.utilities.getLocalizedArrayFromJsonString
import com.hp.workpath.pkgmgt.util.cli.utilities.getMapFromJsonString
import com.hp.workpath.pkgmgt.util.models.BaseModel
import com.hp.workpath.pkgmgt.util.models.ConnectionData
import com.hp.workpath.pkgmgt.util.models.SolutionProject
import com.hp.workpath.pkgmgt.util.models.application.*
import com.hp.workpath.pkgmgt.util.models.hpk.WebServiceEndPoint
import com.hp.workpath.pkgmgt.util.models.hpk.WorkpathPlatformVersion
import com.hp.workpath.pkgmgt.util.models.security.SecurityAgentModel
import com.hp.workpath.pkgmgt.util.models.solutionManager.SolutionDiagnosticsModel
import com.hp.workpath.pkgmgt.util.models.solutionManager.SolutionNotificationAgentModel
import com.hp.workpath.pkgmgt.util.models.usbAccessories.UsbRegistrationModel
import com.hp.workpath.pkgmgt.util.utilities.*
import com.hp.workpath.pkgmgt.util.utilities.apk.ApkParser
import joptsimple.BuiltinHelpFormatter
import joptsimple.OptionDescriptor
import joptsimple.OptionParser
import joptsimple.OptionSet
import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList
import kotlin.io.path.name

class HpkParser {
    private val defaultParser = OptionParser()
    private var apkParser: ApkParser? = null

    // These parsers are for help command
    private val commandsParser = OptionParser()
    private val createParser = OptionParser()
    private val jsonParser = OptionParser()
    private val installParser = OptionParser()
    private val uninstallParser = OptionParser()
    private val solutionListParser = OptionParser()
    private val solutionDetailParser = OptionParser()
    private val configGetParser = OptionParser()
    private val configUpdateParser = OptionParser()
    private val attestationUpdateParser = OptionParser()
    private val USER_INFORMATION = listOf(
        "EMAIL_ADDRESS",
        "USER_NAME",
        "USER_DOMAIN",
        "AUTH_TYPE",
        "FQ_USER_NAME",
        "DISPLAY_NAME",
        "AUTH_AGENT_ID",
        "HOME_FOLDER"
    )
    private val agentModels = BaseModel()

    init {
        // TODO create from hpk
        defaultParser.accepts(CMD_CREATE, MESSAGE.getString("cmd_create"))
        defaultParser.accepts(CMD_JSON, MESSAGE.getString("cmd_json"))
        defaultParser.accepts(CMD_HELP, MESSAGE.getString("cmd_help"))
        defaultParser.accepts(CMD_INSTALL, MESSAGE.getString("cmd_install"))
        defaultParser.accepts(CMD_UNINSTALL, MESSAGE.getString("cmd_uninstall"))
        defaultParser.accepts(CMD_SOLUTION_LIST, MESSAGE.getString("cmd_solution_list"))
        defaultParser.accepts(CMD_SOLUTION_DETAIL, MESSAGE.getString("cmd_solution_detail"))
        defaultParser.accepts(CMD_CONFIG_GET, MESSAGE.getString("cmd_config_get"))
        defaultParser.accepts(CMD_CONFIG_UPDATE, MESSAGE.getString("cmd_config_update"))
        defaultParser.accepts(CMD_ATTESTATION_UPDATE, MESSAGE.getString("cmd_attestation_update"))

        commandsParser.accepts(CMD_CREATE, MESSAGE.getString("cmd_create"))
        commandsParser.accepts(CMD_JSON, MESSAGE.getString("cmd_json"))
        commandsParser.accepts(CMD_HELP, MESSAGE.getString("cmd_help"))
        commandsParser.accepts(CMD_INSTALL, MESSAGE.getString("cmd_install"))
        commandsParser.accepts(CMD_UNINSTALL, MESSAGE.getString("cmd_uninstall"))
        commandsParser.accepts(CMD_SOLUTION_LIST, MESSAGE.getString("cmd_solution_list"))
        commandsParser.accepts(CMD_SOLUTION_DETAIL, MESSAGE.getString("cmd_solution_detail"))
        commandsParser.accepts(CMD_CONFIG_GET, MESSAGE.getString("cmd_config_get"))
        commandsParser.accepts(CMD_CONFIG_UPDATE, MESSAGE.getString("cmd_config_update"))
        commandsParser.accepts(CMD_ATTESTATION_UPDATE, MESSAGE.getString("cmd_attestation_update"))

        /**
         * SOLUTION:
         * OPT_OUTPUT, OPT_SOLUTION_HPK
         * OPT_SOLUTION_ID, OPT_SOLUTION_NAME, OPT_SOLUTION_DESCRIPTION, OPT_SOLUTION_EMAIL, OPT_SOLUTION_PHONE, OPT_SOLUTION_URL
         * , OPT_SOLUTION_VENDOR, OPT_SOLUTION_VERSION
         * OPT_SOLUTION_DATE
         * OPT_SOLUTION_APK
         * OPT_SOLUTION_PLATFORM_VERSION
         * OPT_SOLUTION_DEFAULT_CONFIG_FILE
         * OPT_SOLUTION_BUNDLE_SERIAL
         * OPT_SOLUTION_SIGNING
         */
        defaultParser.accepts(OPT_OUTPUT, MESSAGE.getString("opt_output"))
            .requiredIf(
                CMD_CREATE,
                CMD_JSON
            ).withRequiredArg()
            .describedAs(DESC_OPT_OUTPUT)
        defaultParser.accepts(OPT_CONNECTOR, MESSAGE.getString("opt_connector")).withRequiredArg()
            .describedAs(DESC_OPT_CONNECTOR)
        defaultParser.accepts(OPT_BUILD_HPK, MESSAGE.getString("opt_build_hpk")).withRequiredArg()
            .describedAs(DESC_OPT_BUILD_HPK)
        defaultParser.accepts(OPT_SOLUTION_ID, MESSAGE.getString("opt_solution_id"))
            .requiredIf(CMD_CREATE, CMD_UNINSTALL, CMD_SOLUTION_DETAIL, CMD_CONFIG_GET, CMD_CONFIG_UPDATE)
            .withRequiredArg().describedAs(DESC_OPT_UUID)
        defaultParser.accepts(OPT_SOLUTION_NAME, MESSAGE.getString("opt_solution_name")).requiredIf(CMD_CREATE)
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_NAME)
        defaultParser.accepts(OPT_SOLUTION_DESCRIPTION, MESSAGE.getString("opt_solution_description"))
            .requiredIf(CMD_CREATE).withRequiredArg().describedAs(DESC_OPT_SOLUTION_DESCRIPTION)
        defaultParser.accepts(OPT_SOLUTION_EMAIL, MESSAGE.getString("opt_solution_email")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_EMAIL)
        defaultParser.accepts(OPT_SOLUTION_PHONE, MESSAGE.getString("opt_solution_phone")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_PHONE)
        defaultParser.accepts(OPT_SOLUTION_URL, MESSAGE.getString("opt_solution_url")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_URL)
        defaultParser.accepts(OPT_SOLUTION_VENDOR, MESSAGE.getString("opt_solution_vendor")).requiredIf(CMD_CREATE)
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_VENDOR)
        defaultParser.accepts(OPT_SOLUTION_VERSION, MESSAGE.getString("opt_solution_version")).requiredIf(CMD_CREATE)
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_VERSION)
        defaultParser.accepts(OPT_SOLUTION_VERSION_NUMBER, MESSAGE.getString("opt_solution_version_number")).requiredIf(CMD_CREATE)
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_VERSION_NUMBER)
        defaultParser.accepts(OPT_SOLUTION_DATE, MESSAGE.getString("opt_solution_date")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_DATE)
        defaultParser.accepts(OPT_SOLUTION_APK, MESSAGE.getString("opt_solution_apk"))
            .requiredIf(
                CMD_CREATE,
                CMD_JSON
            )
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_APK)
        defaultParser.accepts(OPT_SOLUTION_PLATFORM_VERSION, MESSAGE.getString("opt_solution_platform_version"))
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_PLATFORM_VERSION)
        defaultParser.accepts(OPT_SOLUTION_DEFAULT_CONFIG_FILE, MESSAGE.getString("opt_solution_default_config_file"))
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_DEFAULT_CONFIG_FILE)
        defaultParser.accepts(OPT_SOLUTION_BUNDLE_SERIAL, MESSAGE.getString("opt_solution_bundle_serial"))
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_BUNDLE_SERIAL)
        defaultParser.accepts(OPT_SOLUTION_SIGNING, MESSAGE.getString("opt_solution_signing"))
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_SIGNING)
        /**
         * APPLICATION FROM MAIN and SUB1 to SUB5 :
         * OPT_APPLICATION_ID
         * OPT_APPLICATION_NAME
         * OPT_APPLICATION_TARGET_PACKAGE
         * OPT_APPLICATION_TITLE
         * OPT_APPLICATION_DESCRIPTION
         * OPT_APPLICATION_ICON // get icon as optional input, if empty extract icon from apk file.
         * OPT_APPLICATION_ICON_SET
         * OPT_APPLICATION_HOME_SCREEN  only for main application
         * OPT_APPLICATION_HOME_SCREEN_DEFAULT TODO (after beta6) HOME SCREEN DEFAULT handle this
         */
        defaultParser.accepts(OPT_APPLICATION_ID + OPT_MAIN, MESSAGE.getString("opt_application_id"))
            .withRequiredArg().describedAs(DESC_OPT_UUID)
        defaultParser.accepts(OPT_APPLICATION_NAME + OPT_MAIN, MESSAGE.getString("opt_application_name"))
            .requiredIf(OPT_APPLICATION_ID + OPT_MAIN).withRequiredArg().describedAs(DESC_OPT_APPLICATION_NAME)
        defaultParser.accepts(
            OPT_APPLICATION_TARGET_PACKAGE + OPT_MAIN,
            MESSAGE.getString("opt_application_target_package")
        ).requiredIf(OPT_APPLICATION_ID + OPT_MAIN).withRequiredArg().describedAs(DESC_OPT_APPLICATION_TARGET_PACKAGE)
        defaultParser.accepts(OPT_APPLICATION_TITLE + OPT_MAIN, MESSAGE.getString("opt_application_title"))
            .withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
        defaultParser.accepts(OPT_APPLICATION_DESCRIPTION + OPT_MAIN, MESSAGE.getString("opt_application_description"))
            .withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
        defaultParser.accepts(OPT_APPLICATION_ICON + OPT_MAIN, MESSAGE.getString("opt_application_icon"))
            .withRequiredArg().describedAs(DESC_OPT_APPLICATION_ICON)
        defaultParser.accepts(OPT_APPLICATION_ICON_SET + OPT_MAIN, MESSAGE.getString("opt_application_icon_set"))
            .withRequiredArg().describedAs(DESC_OPT_APPLICATION_ICON_SET)
        defaultParser.accepts(OPT_APPLICATION_HOME_SCREEN, MESSAGE.getString("opt_application_home_screen"))
        defaultParser.accepts(
            OPT_APPLICATION_HOME_SCREEN_DEFAULT,
            MESSAGE.getString("opt_application_home_screen_default")
        )
        for (i in 1..SUB_APPLICATION_MAX) {
            defaultParser.accepts(OPT_APPLICATION_ID + OPT_BAR + i.toString(), MESSAGE.getString("opt_application_id"))
                .withRequiredArg().describedAs(DESC_OPT_UUID)
            defaultParser.accepts(
                OPT_APPLICATION_NAME + OPT_BAR + i.toString(),
                MESSAGE.getString("opt_application_name")
            )
                .requiredIf(OPT_APPLICATION_ID + OPT_BAR + i.toString()).withRequiredArg()
                .describedAs(DESC_OPT_APPLICATION_NAME)
            defaultParser.accepts(
                OPT_APPLICATION_TARGET_PACKAGE + OPT_BAR + i.toString(),
                MESSAGE.getString("opt_application_target_package")
            ).requiredIf(OPT_APPLICATION_ID + OPT_BAR + i.toString()).withRequiredArg()
                .describedAs(DESC_OPT_APPLICATION_TARGET_PACKAGE)
            defaultParser.accepts(
                OPT_APPLICATION_TITLE + OPT_BAR + i.toString(),
                MESSAGE.getString("opt_application_title")
            )
                .withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
            defaultParser.accepts(
                OPT_APPLICATION_DESCRIPTION + OPT_BAR + i.toString(),
                MESSAGE.getString("opt_application_description")
            )
                .withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
            defaultParser.accepts(
                OPT_APPLICATION_ICON + OPT_BAR + i.toString(),
                MESSAGE.getString("opt_application_icon")
            )
                .withRequiredArg().describedAs(DESC_OPT_APPLICATION_ICON)
            defaultParser.accepts(
                OPT_APPLICATION_ICON_SET + OPT_BAR + i.toString(),
                MESSAGE.getString("opt_application_icon_set")
            )
                .withRequiredArg().describedAs(DESC_OPT_APPLICATION_ICON_SET)
        }
        /**
         * AUTH AGENT :
         * OPT_AUTHENTICATION_ID
         * OPT_AUTHENTICATION_NAME
         * OPT_AUTHENTICATION_TITLE
         * OPT_AUTHENTICATION_DESCRIPTION
         * OPT_AUTHENTICATION_TARGET_PACKAGE
         * OPT_AUTHENTICATION_PRE_PROMPT
         */
        defaultParser.accepts(OPT_AUTHENTICATION_ID, MESSAGE.getString("opt_authentication_id"))
            .withRequiredArg().describedAs(DESC_OPT_UUID)
        defaultParser.accepts(OPT_AUTHENTICATION_NAME, MESSAGE.getString("opt_authentication_name"))
            .requiredIf(OPT_AUTHENTICATION_ID).withRequiredArg().describedAs(DESC_OPT_AUTHENTICATION_NAME)
        defaultParser.accepts(OPT_AUTHENTICATION_TITLE, MESSAGE.getString("opt_authentication_title"))
            .requiredIf(OPT_AUTHENTICATION_ID).withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
        defaultParser.accepts(OPT_AUTHENTICATION_DESCRIPTION, MESSAGE.getString("opt_authentication_description"))
            .requiredIf(OPT_AUTHENTICATION_ID).withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
        defaultParser.accepts(OPT_AUTHENTICATION_TARGET_PACKAGE, MESSAGE.getString("opt_authentication_target_package"))
            .requiredIf(OPT_AUTHENTICATION_ID).withRequiredArg().describedAs(DESC_OPT_AUTHENTICATION_TARGET_PACKAGE)
        defaultParser.accepts(OPT_AUTHENTICATION_PRE_PROMPT, MESSAGE.getString("opt_authentication_pre_prompt"))
        /**
         * USB ACCESSORIES AGENT :
         * OPT_ACCESSORIES_N
         */
        for (i in 1..ACCESSORIES_MAX) {
            defaultParser.accepts(OPT_ACCESSORIES + OPT_BAR + i.toString(), MESSAGE.getString("opt_accessories"))
                .withRequiredArg().describedAs(DESC_OPT_ACCESSORIES)
        }
        /**
         * COPY JOB AGENT :
         * OPT_COPY_JOB_AGENT
         */
        defaultParser.accepts(OPT_COPY_JOB_AGENT, MESSAGE.getString("opt_copy_job_agent"))
        /**
         * PRINT JOB AGENT :
         * OPT_PRINT_JOB_AGENT
         */
        defaultParser.accepts(OPT_PRINT_JOB_AGENT, MESSAGE.getString("opt_print_job_agent"))
        /**
         * SCAN JOB AGENT :
         * OPT_SCAN_JOB_AGENT
         */
        defaultParser.accepts(OPT_SCAN_JOB_AGENT, MESSAGE.getString("opt_scan_job_agent"))
        /**
         * DEVICE USAGE AGENT :
         * OPT_DEVICE_USAGE_AGENT
         */
        defaultParser.accepts(OPT_DEVICE_USAGE, MESSAGE.getString("opt_device_usage_agent"))
        /**
         * STATISTICS AGENT : TODO
         */
        /**
         * SUPPLIRES AGENT :
         * OPT_SUPPLIES
         */
        defaultParser.accepts(OPT_SUPPLIES_AGENT, MESSAGE.getString("opt_supplies_agent"))
        /**
         * WEBSERVICE :
         * OPT_WEBSERVICE_AGENT
         * OPT_WEBSERVICE_TITLE as LocalizedString
         * OPT_WEBSERVICE_DESCRIPTION as LocalizedString
         * OPT_WEBSERVICE_ENDPOINTS
         */
        defaultParser.accepts(OPT_WEBSERVICE_AGENT, MESSAGE.getString("opt_webservice_agent"))
        defaultParser.accepts(OPT_WEBSERVICE_TITLE, MESSAGE.getString("opt_webservice_title"))
            .requiredIf(OPT_WEBSERVICE_AGENT).withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
        defaultParser.accepts(OPT_WEBSERVICE_DESCRIPTION, MESSAGE.getString("opt_webservice_description"))
            .requiredIf(OPT_WEBSERVICE_AGENT).withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
        defaultParser.accepts(OPT_WEBSERVICE_ENDPOINT, MESSAGE.getString("opt_webservice_endpoint"))
            .requiredIf(OPT_WEBSERVICE_AGENT).withRequiredArg().describedAs(DESC_OPT_WEBSERVICE_ENDPOINT)

        /**
         * Install:
         * OPT_INSTALL_FILE
         * OPT_HOST
         * OPT_PASSWORD
         * OPT_FORCE_INSTALL TODO (is force and soft update different?)
         */
        defaultParser.accepts(OPT_INSTALL_FILE, MESSAGE.getString("opt_install_file")).requiredIf(CMD_INSTALL)
            .withRequiredArg().describedAs(DESC_OPT_INSTALL_FILE)
        defaultParser.accepts(OPT_HOST, MESSAGE.getString("opt_host"))
            .requiredIf(
                CMD_INSTALL,
                CMD_UNINSTALL,
                CMD_SOLUTION_LIST,
                CMD_SOLUTION_DETAIL,
                CMD_CONFIG_GET,
                CMD_CONFIG_UPDATE,
                CMD_ATTESTATION_UPDATE
            )
            .withRequiredArg().describedAs(DESC_OPT_HOST)
        defaultParser.accepts(OPT_PASSWORD, MESSAGE.getString("opt_password"))
            .withRequiredArg().describedAs(DESC_OPT_PASSWORD)

        /**
         * Uninstall:
         * OPT_SOLUTION_ID : SOLUTION
         * OPT_HOST : Install
         * OPT_PASSWORD : Install
         */

        /**
         * Solution List:
         * OPT_HOST : Install
         * OPT_PASSWORD : Install
         */

        /**
         * Solution Detail:
         * OPT_SOLUTION_ID : SOLUTION
         * OPT_HOST : Install
         * OPT_PASSWORD : install
         */

        /**
         * Config Get:
         * OPT_SOLUTION_ID : SOLUTION
         * OPT_HOST : Install
         * OPT_PASSWORD : install
         * OPT_CONFIG_DATA
         */
        defaultParser.accepts(OPT_CONFIG_DATA, MESSAGE.getString("opt_config_data"))
            .requiredIf(CMD_CONFIG_UPDATE).withRequiredArg().describedAs(DESC_OPT_CONFIG_DATA)

        /**
         * Attestation Update:
         * OPT_SOLUTION_ID : SOLUTION
         * OPT_HOST : Install
         * OPT_PASSWORD : install
         * OPT_ATTESTATION_DATA
         */
        defaultParser.accepts(OPT_ATTESTATION_NAME, MESSAGE.getString("opt_attestation_name"))
            .requiredIf(CMD_ATTESTATION_UPDATE).withRequiredArg().describedAs(DESC_OPT_ATTESTATION_NAME)
        defaultParser.accepts(OPT_ATTESTATION_LDB_KEY, MESSAGE.getString("opt_attestation_ldb_key"))
            .requiredIf(CMD_ATTESTATION_UPDATE).withRequiredArg().describedAs(DESC_ATTESTATION_LDB_KEY)
        defaultParser.accepts(OPT_ATTESTATION_DATA, MESSAGE.getString("opt_attestation_data"))
            .availableIf(CMD_ATTESTATION_UPDATE).withRequiredArg().describedAs(DESC_OPT_ATTESTATION_DATA)

        /**
         * From here is Parsers for help command
         */
        createParser.accepts(OPT_OUTPUT, MESSAGE.getString("opt_output")).withRequiredArg()
            .describedAs(DESC_OPT_OUTPUT).required()
        createParser.accepts(OPT_BUILD_HPK, MESSAGE.getString("opt_build_hpk")).withRequiredArg()
            .describedAs(DESC_OPT_BUILD_HPK)
        createParser.accepts(OPT_SOLUTION_ID, MESSAGE.getString("opt_solution_id")).withRequiredArg()
            .describedAs(DESC_OPT_UUID).required()
        createParser.accepts(OPT_SOLUTION_NAME, MESSAGE.getString("opt_solution_name")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_NAME).required()
        createParser.accepts(OPT_SOLUTION_DESCRIPTION, MESSAGE.getString("opt_solution_description")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_DESCRIPTION).required()
        createParser.accepts(OPT_SOLUTION_EMAIL, MESSAGE.getString("opt_solution_email")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_EMAIL)
        createParser.accepts(OPT_SOLUTION_PHONE, MESSAGE.getString("opt_solution_phone")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_PHONE)
        createParser.accepts(OPT_SOLUTION_URL, MESSAGE.getString("opt_solution_url")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_URL)
        createParser.accepts(OPT_SOLUTION_VENDOR, MESSAGE.getString("opt_solution_vendor")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_VENDOR).required()
        createParser.accepts(OPT_SOLUTION_VERSION, MESSAGE.getString("opt_solution_version")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_VERSION).required()
        createParser.accepts(OPT_SOLUTION_VERSION_NUMBER, MESSAGE.getString("opt_solution_version_number")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_VERSION_NUMBER).required()
        createParser.accepts(OPT_SOLUTION_DATE, MESSAGE.getString("opt_solution_date")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_DATE)
        createParser.accepts(OPT_SOLUTION_APK, MESSAGE.getString("opt_solution_apk")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_APK).required()
        createParser.accepts(OPT_SOLUTION_PLATFORM_VERSION, MESSAGE.getString("opt_solution_platform_version"))
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_PLATFORM_VERSION).required()
        createParser.accepts(OPT_SOLUTION_DEFAULT_CONFIG_FILE, MESSAGE.getString("opt_solution_default_config_file"))
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_DEFAULT_CONFIG_FILE)
        createParser.accepts(OPT_SOLUTION_BUNDLE_SERIAL, MESSAGE.getString("opt_solution_bundle_serial"))
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_BUNDLE_SERIAL)
        createParser.accepts(OPT_SOLUTION_SIGNING, MESSAGE.getString("opt_solution_signing"))
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_SIGNING)
        createParser.accepts(OPT_APPLICATION_ID + OPT_MAIN, MESSAGE.getString("opt_application_id"))
            .withRequiredArg().describedAs(DESC_OPT_UUID)
        createParser.accepts(OPT_APPLICATION_NAME + OPT_MAIN, MESSAGE.getString("opt_application_name"))
            .requiredIf(OPT_APPLICATION_ID + OPT_MAIN).withRequiredArg().describedAs(DESC_OPT_APPLICATION_NAME)
        createParser.accepts(
            OPT_APPLICATION_TARGET_PACKAGE + OPT_MAIN,
            MESSAGE.getString("opt_application_target_package")
        )
            .requiredIf(OPT_APPLICATION_ID + OPT_MAIN).withRequiredArg()
            .describedAs(DESC_OPT_APPLICATION_TARGET_PACKAGE)
        createParser.accepts(OPT_APPLICATION_TITLE + OPT_MAIN, MESSAGE.getString("opt_application_title"))
            .withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
        createParser.accepts(OPT_APPLICATION_DESCRIPTION + OPT_MAIN, MESSAGE.getString("opt_application_description"))
            .withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
        createParser.accepts(OPT_APPLICATION_ICON + OPT_MAIN, MESSAGE.getString("opt_application_icon"))
            .withRequiredArg().describedAs(DESC_OPT_APPLICATION_ICON)
        createParser.accepts(OPT_APPLICATION_ICON_SET + OPT_MAIN, MESSAGE.getString("opt_application_icon_set"))
            .withRequiredArg().describedAs(DESC_OPT_APPLICATION_ICON_SET)
        createParser.accepts(OPT_APPLICATION_HOME_SCREEN, MESSAGE.getString("opt_application_home_screen"))
        createParser.accepts(
            OPT_APPLICATION_HOME_SCREEN_DEFAULT,
            MESSAGE.getString("opt_application_home_screen_default")
        )
        for (i in 1..SUB_APPLICATION_MAX) {
            createParser.accepts(OPT_APPLICATION_ID + OPT_BAR + i.toString(), MESSAGE.getString("opt_application_id"))
                .withRequiredArg().describedAs(DESC_OPT_UUID)
            createParser.accepts(
                OPT_APPLICATION_NAME + OPT_BAR + i.toString(),
                MESSAGE.getString("opt_application_name")
            )
                .requiredIf(OPT_APPLICATION_ID + OPT_BAR + i.toString()).withRequiredArg()
                .describedAs(DESC_OPT_APPLICATION_NAME)
            createParser.accepts(
                OPT_APPLICATION_TARGET_PACKAGE + OPT_BAR + i.toString(),
                MESSAGE.getString("opt_application_target_package")
            )
                .requiredIf(OPT_APPLICATION_ID + OPT_BAR + i.toString()).withRequiredArg()
                .describedAs(DESC_OPT_APPLICATION_TARGET_PACKAGE)
            createParser.accepts(
                OPT_APPLICATION_TITLE + OPT_BAR + i.toString(),
                MESSAGE.getString("opt_application_title")
            )
                .withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
            createParser.accepts(
                OPT_APPLICATION_DESCRIPTION + OPT_BAR + i.toString(),
                MESSAGE.getString("opt_application_description")
            )
                .withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
            createParser.accepts(
                OPT_APPLICATION_ICON + OPT_BAR + i.toString(),
                MESSAGE.getString("opt_application_icon")
            )
                .withRequiredArg().describedAs(DESC_OPT_APPLICATION_ICON)
            createParser.accepts(
                OPT_APPLICATION_ICON_SET + OPT_BAR + i.toString(),
                MESSAGE.getString("opt_application_icon_set")
            )
                .withRequiredArg().describedAs(DESC_OPT_APPLICATION_ICON_SET)
        }
        createParser.accepts(OPT_AUTHENTICATION_ID, MESSAGE.getString("opt_authentication_id"))
            .withRequiredArg().describedAs(DESC_OPT_UUID)
        createParser.accepts(OPT_AUTHENTICATION_NAME, MESSAGE.getString("opt_authentication_name"))
            .requiredIf(OPT_AUTHENTICATION_ID).withRequiredArg().describedAs(DESC_OPT_AUTHENTICATION_NAME)
        createParser.accepts(OPT_AUTHENTICATION_TITLE, MESSAGE.getString("opt_authentication_title"))
            .requiredIf(OPT_AUTHENTICATION_ID).withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
        createParser.accepts(OPT_AUTHENTICATION_DESCRIPTION, MESSAGE.getString("opt_authentication_description"))
            .requiredIf(OPT_AUTHENTICATION_ID).withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
        createParser.accepts(OPT_AUTHENTICATION_TARGET_PACKAGE, MESSAGE.getString("opt_authentication_target_package"))
            .requiredIf(OPT_AUTHENTICATION_ID).withRequiredArg().describedAs(DESC_OPT_AUTHENTICATION_TARGET_PACKAGE)
        createParser.accepts(OPT_AUTHENTICATION_PRE_PROMPT, MESSAGE.getString("opt_authentication_pre_prompt"))
        for (i in 1..ACCESSORIES_MAX) {
            createParser.accepts(OPT_ACCESSORIES + OPT_BAR + i.toString(), MESSAGE.getString("opt_accessories"))
                .withRequiredArg().describedAs(DESC_OPT_ACCESSORIES)
        }
        createParser.accepts(OPT_COPY_JOB_AGENT, MESSAGE.getString("opt_copy_job_agent"))
        createParser.accepts(OPT_PRINT_JOB_AGENT, MESSAGE.getString("opt_print_job_agent"))
        createParser.accepts(OPT_SCAN_JOB_AGENT, MESSAGE.getString("opt_scan_job_agent"))
        createParser.accepts(OPT_DEVICE_USAGE, MESSAGE.getString("opt_device_usage_agent"))
        createParser.accepts(OPT_SUPPLIES_AGENT, MESSAGE.getString("opt_supplies_agent"))
        createParser.accepts(OPT_WEBSERVICE_AGENT, MESSAGE.getString("opt_webservice_agent"))
        createParser.accepts(OPT_WEBSERVICE_TITLE, MESSAGE.getString("opt_webservice_title"))
            .requiredIf(OPT_WEBSERVICE_AGENT).withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
        createParser.accepts(OPT_WEBSERVICE_DESCRIPTION, MESSAGE.getString("opt_webservice_description"))
            .requiredIf(OPT_WEBSERVICE_AGENT).withRequiredArg().describedAs(DESC_OPT_LOCALIZED_LANGUAGE)
        createParser.accepts(OPT_WEBSERVICE_ENDPOINT, MESSAGE.getString("opt_webservice_endpoint"))
            .requiredIf(OPT_WEBSERVICE_AGENT).withRequiredArg().describedAs(DESC_OPT_WEBSERVICE_ENDPOINT)


        jsonParser.accepts(OPT_OUTPUT, MESSAGE.getString("opt_output")).withRequiredArg()
            .describedAs(DESC_OPT_OUTPUT).required()
        jsonParser.accepts(OPT_SOLUTION_APK, MESSAGE.getString("opt_solution_apk")).withRequiredArg()
            .describedAs(DESC_OPT_SOLUTION_APK).required()
        jsonParser.accepts(OPT_CONNECTOR, MESSAGE.getString("opt_connector")).withRequiredArg()
            .describedAs(DESC_OPT_CONNECTOR).required()
        jsonParser.accepts(OPT_SOLUTION_DEFAULT_CONFIG_FILE, MESSAGE.getString("opt_solution_default_config_file"))
            .withRequiredArg().describedAs(DESC_OPT_SOLUTION_DEFAULT_CONFIG_FILE)


        installParser.accepts(OPT_INSTALL_FILE, MESSAGE.getString("opt_install_file"))
            .withRequiredArg().describedAs(DESC_OPT_INSTALL_FILE).required()
        installParser.accepts(OPT_HOST, MESSAGE.getString("opt_host"))
            .withRequiredArg().describedAs(DESC_OPT_HOST).required()
        installParser.accepts(OPT_PASSWORD, MESSAGE.getString("opt_password"))
            .withRequiredArg().describedAs(DESC_OPT_PASSWORD)

        uninstallParser.accepts(OPT_SOLUTION_ID, MESSAGE.getString("opt_solution_id"))
            .withRequiredArg().describedAs(DESC_OPT_UUID).required()
        uninstallParser.accepts(OPT_HOST, MESSAGE.getString("opt_host"))
            .withRequiredArg().describedAs(DESC_OPT_HOST).required()
        uninstallParser.accepts(OPT_PASSWORD, MESSAGE.getString("opt_password"))
            .withRequiredArg().describedAs(DESC_OPT_PASSWORD)

        solutionListParser.accepts(OPT_HOST, MESSAGE.getString("opt_host"))
            .withRequiredArg().describedAs(DESC_OPT_HOST).required()
        solutionListParser.accepts(OPT_PASSWORD, MESSAGE.getString("opt_password"))
            .withRequiredArg().describedAs(DESC_OPT_PASSWORD)

        solutionDetailParser.accepts(OPT_SOLUTION_ID, MESSAGE.getString("opt_solution_id"))
            .withRequiredArg().describedAs(DESC_OPT_UUID).required()
        solutionDetailParser.accepts(OPT_HOST, MESSAGE.getString("opt_host"))
            .withRequiredArg().describedAs(DESC_OPT_HOST).required()
        solutionDetailParser.accepts(OPT_PASSWORD, MESSAGE.getString("opt_password"))
            .withRequiredArg().describedAs(DESC_OPT_PASSWORD)

        configGetParser.accepts(OPT_SOLUTION_ID, MESSAGE.getString("opt_solution_id"))
            .withRequiredArg().describedAs(DESC_OPT_UUID).required()
        configGetParser.accepts(OPT_HOST, MESSAGE.getString("opt_host"))
            .withRequiredArg().describedAs(DESC_OPT_HOST).required()
        configGetParser.accepts(OPT_PASSWORD, MESSAGE.getString("opt_password"))
            .withRequiredArg().describedAs(DESC_OPT_PASSWORD)

        configUpdateParser.accepts(OPT_SOLUTION_ID, MESSAGE.getString("opt_solution_id"))
            .withRequiredArg().describedAs(DESC_OPT_UUID).required()
        configUpdateParser.accepts(OPT_HOST, MESSAGE.getString("opt_host"))
            .withRequiredArg().describedAs(DESC_OPT_HOST).required()
        configUpdateParser.accepts(OPT_PASSWORD, MESSAGE.getString("opt_password"))
            .withRequiredArg().describedAs(DESC_OPT_PASSWORD)
        configUpdateParser.accepts(OPT_CONFIG_DATA, MESSAGE.getString("opt_config_data"))
            .withRequiredArg().describedAs(DESC_OPT_CONFIG_DATA).required()

        attestationUpdateParser.accepts(OPT_SOLUTION_ID, MESSAGE.getString("opt_solution_id"))
            .withRequiredArg().describedAs(DESC_OPT_UUID).required()
        attestationUpdateParser.accepts(OPT_HOST, MESSAGE.getString("opt_host"))
            .withRequiredArg().describedAs(DESC_OPT_HOST).required()
        attestationUpdateParser.accepts(OPT_PASSWORD, MESSAGE.getString("opt_password"))
            .withRequiredArg().describedAs(DESC_OPT_PASSWORD).required()
        attestationUpdateParser.accepts(OPT_ATTESTATION_NAME, MESSAGE.getString("opt_attestation_name"))
            .withRequiredArg().describedAs(DESC_OPT_ATTESTATION_NAME).required()
        attestationUpdateParser.accepts(OPT_ATTESTATION_LDB_KEY, MESSAGE.getString("opt_attestation_ldb_key"))
            .withRequiredArg().describedAs(DESC_ATTESTATION_LDB_KEY).required()
        attestationUpdateParser.accepts(OPT_ATTESTATION_DATA, MESSAGE.getString("opt_attestation_data"))
            .withRequiredArg().describedAs(DESC_OPT_ATTESTATION_DATA).required()
        attestationUpdateParser.accepts(OPT_ATTESTATION_CMD_LOC, MESSAGE.getString("opt_attestation_cmd_loc"))
            .withRequiredArg().describedAs(DESC_ATTESTATION_CMD_LOC)
    }

    fun parse(vararg args: String): OptionSet {
        return defaultParser.parse(*args)
    }

    enum class CliCommands {
        NULL, ERR, HELP, CREATE, JSON, INSTALL, UNINSTALL, SOLUTION_LIST, SOLUTION_DETAIL, CONFIG_GET, CONFIG_UPDATE, ATTESTATION_UPDATE;
    }

    fun getMainCommand(cmd: OptionSet): CliCommands {
        var command = CliCommands.NULL
        if (cmd.has(CMD_HELP)) {
            command = CliCommands.HELP
        }
        if (cmd.has(CMD_CREATE)) {
            command = if (command == CliCommands.NULL) CliCommands.CREATE else CliCommands.ERR
        }
        if (cmd.has(CMD_JSON)) {
            command = if (command == CliCommands.NULL) CliCommands.JSON else CliCommands.ERR
        }
        if (cmd.has(CMD_INSTALL)) {
            command = if (command == CliCommands.NULL) CliCommands.INSTALL else CliCommands.ERR
        }
        if (cmd.has(CMD_UNINSTALL)) {
            command = if (command == CliCommands.NULL) CliCommands.UNINSTALL else CliCommands.ERR
        }
        if (cmd.has(CMD_SOLUTION_LIST)) {
            command = if (command == CliCommands.NULL) CliCommands.SOLUTION_LIST else CliCommands.ERR
        }
        if (cmd.has(CMD_SOLUTION_DETAIL)) {
            command = if (command == CliCommands.NULL) CliCommands.SOLUTION_DETAIL else CliCommands.ERR
        }
        if (cmd.has(CMD_CONFIG_GET)) {
            command = if (command == CliCommands.NULL) CliCommands.CONFIG_GET else CliCommands.ERR
        }
        if (cmd.has(CMD_CONFIG_UPDATE)) {
            command = if (command == CliCommands.NULL) CliCommands.CONFIG_UPDATE else CliCommands.ERR
        }
        if (cmd.has(CMD_ATTESTATION_UPDATE)) {
            command = if (command == CliCommands.NULL) CliCommands.ATTESTATION_UPDATE else CliCommands.ERR
        }
        return command
    }

    class HelpFormatter internal constructor() : BuiltinHelpFormatter(255, 2) {
        override fun format(options: Map<String, OptionDescriptor?>): String {
            addRows(LinkedHashSet(options.values))
            return formattedHelpOutput()
        }

        override fun addHeaders(options: Collection<OptionDescriptor?>) {}
    }

    fun printHelp() {
        println(MESSAGE.getString("menu_tool_name") + " " + MESSAGE.getString("menu_version") + " " + TOOL_VERSION + "(" + TOOL_BUILD_DATE + ")")
        println(MESSAGE.getString("prefix_usage") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("prefix_example"))
        println(MESSAGE.getString("tool_name") + " -> win: " + MESSAGE.getString("tool_name_win") + " / linux: " + MESSAGE.getString("tool_name_linux"))
        println()

        println(MESSAGE.getString("prefix_commands"))
        commandsParser.formatHelpWith(HelpFormatter())
        commandsParser.printHelpOn(System.out)
        println()

        println(MESSAGE.getString("usage_create"))
        println(MESSAGE.getString("prefix_options_for") + CMD_CREATE)
        createParser.formatHelpWith(HelpFormatter())
        createParser.printHelpOn(System.out)
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_create1_without_application"))
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_create2_with_config"))
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_create3_with_application"))
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_create4_accessory"))
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_create5_agent"))
        println()

        println(MESSAGE.getString("usage_json"))
        println(MESSAGE.getString("prefix_options_for") + CMD_JSON)
        jsonParser.formatHelpWith(HelpFormatter())
        jsonParser.printHelpOn(System.out)
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_json1"))
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_json2_with_config"))

        println()

        println(MESSAGE.getString("usage_install"))
        println(MESSAGE.getString("prefix_options_for") + CMD_INSTALL)
        installParser.formatHelpWith(HelpFormatter())
        installParser.printHelpOn(System.out)
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_install"))
        println()

        println(MESSAGE.getString("usage_uninstall"))
        println(MESSAGE.getString("prefix_options_for") + CMD_UNINSTALL)
        uninstallParser.formatHelpWith(HelpFormatter())
        uninstallParser.printHelpOn(System.out)
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_uninstall"))
        println()

        println(MESSAGE.getString("usage_solution_list"))
        println(MESSAGE.getString("prefix_options_for") + CMD_SOLUTION_LIST)
        solutionListParser.formatHelpWith(HelpFormatter())
        solutionListParser.printHelpOn(System.out)
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_solution_list"))
        println()

        println(MESSAGE.getString("usage_solution_detail"))
        println(MESSAGE.getString("prefix_options_for") + CMD_SOLUTION_DETAIL)
        solutionDetailParser.formatHelpWith(HelpFormatter())
        solutionDetailParser.printHelpOn(System.out)
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_detailed_solution"))
        println()

        println(MESSAGE.getString("usage_config_get"))
        println(MESSAGE.getString("prefix_options_for") + CMD_CONFIG_GET)
        configGetParser.formatHelpWith(HelpFormatter())
        configGetParser.printHelpOn(System.out)
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_get_config"))
        println()

        println(MESSAGE.getString("usage_config_update"))
        println(MESSAGE.getString("prefix_options_for") + CMD_CONFIG_UPDATE)
        configUpdateParser.formatHelpWith(HelpFormatter())
        configUpdateParser.printHelpOn(System.out)
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_update_config"))
        println()

        println(MESSAGE.getString("usage_attestation_update"))
        println(MESSAGE.getString("prefix_options_for") + CMD_ATTESTATION_UPDATE)
        attestationUpdateParser.formatHelpWith(HelpFormatter())
        attestationUpdateParser.printHelpOn(System.out)
        println(MESSAGE.getString("example") + " " + MESSAGE.getString("tool_name") + " " + MESSAGE.getString("example_attestation_update"))
        println()
    }

    fun getSolutionProjectFromCommand(cmd: OptionSet): SolutionProject {
        println(MESSAGE.getString("task_loading"))
        val solutionProject = SolutionProject()
        // Solution
        cmd.valueOf(OPT_OUTPUT)?.let {
            solutionProject.outputPathString = Path.of(it.toString()).toAbsolutePath().toString()
        }
        cmd.valueOf(OPT_BUILD_HPK)?.let {
            val hpkFile = File(it.toString())
            if (!hpkFile.extension.equals(HPK_EXTENSION, true)) {
                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_BUILD_HPK: $it")
            }
            solutionProject.outputHpkPath = hpkFile.toPath().toAbsolutePath()
        }
        cmd.valueOf(OPT_SOLUTION_APK)?.let {
            val apkFile = File(it.toString())
            if (!apkFile.exists() || !apkFile.extension.equals(APK_EXTENSION, true)) {
                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_SOLUTION_APK: $it")
            }
            solutionProject.solutionManager.workpathPlatformPackage.workpathPackagePath = apkFile.name
            solutionProject.solutionManager.workpathPlatformPackage.installFile = apkFile.name
            solutionProject.apkPath = apkFile.toPath().toAbsolutePath()
        }
        cmd.valueOf(OPT_SOLUTION_DEFAULT_CONFIG_FILE)?.let {
            val configFile = File(it.toString())
            if (!configFile.exists()) {
                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_SOLUTION_DEFAULT_CONFIG_FILE: $it")
            }
            solutionProject.solutionManager.configuration.apply {
                includeConfiguration = true
                archiveDataPath = ASSETS_CONFIGS + configFile.name
                description = ""
                mimeType = DEFAULT_CONFIG_MIME
            }
            solutionProject.configPath = Path.of(it.toString()).toAbsolutePath()
        } ?: run {
            val defaultConfig = ConfigUtils.getDefaultConfigFile()
            if (defaultConfig != null) {
                solutionProject.solutionManager.configuration.apply {
                    includeConfiguration = true
                    archiveDataPath = ASSETS_CONFIGS + "default.json"
                    description = "Default configuration"
                    mimeType = DEFAULT_CONFIG_MIME
                }
                solutionProject.configPath = defaultConfig.toPath().toAbsolutePath()
            }
        }

        cmd.valueOf(OPT_SOLUTION_ID)?.let {
            solutionProject.solutionManager.solutionDetails.apply {
                solutionId = it.toString()
                if (!isValidUuid(solutionId)) {
                    throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_SOLUTION_ID: $it")
                }
            }
        }
        with(solutionProject.solutionManager.solutionDetails.solutionDescription) {
            cmd.valueOf(OPT_SOLUTION_DESCRIPTION)?.let {
                description = it.toString()
            }
            cmd.valueOf(OPT_SOLUTION_NAME)?.let {
                name = it.toString()
            }
            cmd.valueOf(OPT_SOLUTION_EMAIL)?.let {
                supportEmail = it.toString()
            }
            cmd.valueOf(OPT_SOLUTION_PHONE)?.let {
                supportPhone = it.toString()
            }
            cmd.valueOf(OPT_SOLUTION_URL)?.let {
                supportUrl = it.toString()
            }
            cmd.valueOf(OPT_SOLUTION_VENDOR)?.let {
                vendor = it.toString()
            }
            cmd.valueOf(OPT_SOLUTION_VERSION)?.let {
                version = it.toString()
            }
            cmd.valueOf(OPT_SOLUTION_VERSION_NUMBER)?.let {
                versionNumber = it.toString().toLong()
            }
            if (cmd.has(OPT_SOLUTION_DATE)) {
                cmd.valueOf(OPT_SOLUTION_DATE)?.let {
                    date = it.toString()
                    try {
                        DATE_FORMAT.parse(date)
                    } catch (exception: Exception) {
                        throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_SOLUTION_DATE: $it")
                    }
                }
            } else {
                date = DATE_FORMAT.format(Date())
            }
        }
        cmd.valueOf(OPT_SOLUTION_PLATFORM_VERSION)?.let {
            try {
                solutionProject.solutionManager.workpathPlatformPackage.platformVersion =
                    WorkpathPlatformVersion.getEnumByValue(it.toString())
            } catch (exception: Exception) {
                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_SOLUTION_PLATFORM_VERSION: $it")
            }
        }
        cmd.valueOf(OPT_SOLUTION_BUNDLE_SERIAL)?.let {
            solutionProject.solutionBundleSerial = it.toString()
        }
        cmd.valueOf(OPT_SOLUTION_SIGNING)?.let {
            val signingKey = File(it.toString())
            if (!signingKey.exists() && !signingKey.extension.equals(PEM_EXTENSION, true)) {
                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_SOLUTION_SIGNING: $it")
            }
            solutionProject.signingKeyPath = Path.of(it.toString()).toAbsolutePath()
        }
        // Application //
        // Application.I18n is added on other agent
        // Application.Application
        cmd.valueOf(OPT_APPLICATION_ID + OPT_MAIN)?.let { applicationId ->
            solutionProject.applicationService.applications.add(
                ApplicationAgentModel().apply {
                    details.applicationId = applicationId.toString()
                    if (!isValidUuid(details.applicationId)) {
                        throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_APPLICATION_ID$OPT_MAIN: $applicationId")
                    }
                    cmd.valueOf(OPT_APPLICATION_NAME + OPT_MAIN)?.let {
                        details.name = it.toString()
                    }
                    if (cmd.has(OPT_APPLICATION_HOME_SCREEN)) {
                        details.category = ApplicationCategory.HomeScreen
                    }
                    if (cmd.has(OPT_APPLICATION_HOME_SCREEN_DEFAULT)) {
                        // TODO (after beta6) handle default home screen option input
                        details.setAsDefault = true
                    }
                    details.title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                    details.description.i18nAssetId = details.title.i18nAssetId
                    details.title.stringId = APPLICATION_TITLE_STRING_ID
                    details.description.stringId = APPLICATION_DESCRIPTION_STRING_ID
                    cmd.valueOf(OPT_APPLICATION_TITLE + OPT_MAIN)?.let {
                        getMapFromJsonString(it.toString()).forEach { (tag, value) ->
                            solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                                tag,
                                APPLICATION_TITLE_STRING_ID,
                                value.toString()
                            )
                        }
                        details.isTitleFromUser = true
                    }
                    cmd.valueOf(OPT_APPLICATION_DESCRIPTION + OPT_MAIN)?.let {
                        getMapFromJsonString(it.toString()).forEach { (tag, value) ->
                            solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                                tag,
                                APPLICATION_DESCRIPTION_STRING_ID,
                                value.toString()
                            )
                        }
                        details.isDescriptionFromUser = true
                    }
                    cmd.valueOf(OPT_APPLICATION_ICON + OPT_MAIN)?.let {
                        val iconFile = File(it.toString())
                        if (!checkIconFileExtension(iconFile)) {
                            throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${OPT_APPLICATION_ICON + OPT_MAIN}: $it")
                        }
                        details.icon.localIcon.originalPath = iconFile.toPath().toAbsolutePath()
                        details.isIconFromUser = true
                    }
                    cmd.valueOf(OPT_APPLICATION_ICON_SET + OPT_MAIN)?.let {
                        getIconMapFromJsonString(it.toString()).forEach { (name, value) ->
                            details.iconSet.add(ApplicationIconDetailModel().apply {
                                isInIconSet = true
                                key = name
                                localIcon.originalPath = value.toPath().toAbsolutePath()
                            })
                        }
                        details.isIconFromUser = true
                    }
                    target.isMainApplication = true
                    cmd.valueOf(OPT_APPLICATION_TARGET_PACKAGE + OPT_MAIN)?.let {
                        target.workpathPackage = it.toString()
                    }
                })
        }
        for (i in 1..SUB_APPLICATION_MAX) {
            cmd.valueOf(OPT_APPLICATION_ID + OPT_BAR + i.toString())?.let { applicationId ->
                if (cmd.has(OPT_APPLICATION_HOME_SCREEN)) {
                    throw IllegalArgumentException("Home screen application can't have sub application")
                }
                solutionProject.applicationService.applications.add(
                    ApplicationAgentModel().apply {
                        details.applicationId = applicationId.toString()
                        if (!isValidUuid(details.applicationId)) {
                            throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_APPLICATION_ID$OPT_BAR$i: $applicationId")
                        }
                        cmd.valueOf(OPT_APPLICATION_NAME + OPT_BAR + i.toString())?.let {
                            details.name = it.toString()
                        }
                        details.title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                        details.description.i18nAssetId = details.title.i18nAssetId
                        details.title.stringId = APPLICATION_TITLE_STRING_ID + i.toString()
                        details.description.stringId = APPLICATION_DESCRIPTION_STRING_ID + i.toString()
                        cmd.valueOf(OPT_APPLICATION_TITLE + OPT_BAR + i.toString())?.let {
                            getMapFromJsonString(it.toString()).forEach { (tag, value) ->
                                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                                    tag,
                                    APPLICATION_TITLE_STRING_ID + i.toString(),
                                    value.toString()
                                )
                            }
                            details.isTitleFromUser = true
                        }
                        cmd.valueOf(OPT_APPLICATION_DESCRIPTION + OPT_BAR + i.toString())?.let {
                            getMapFromJsonString(it.toString()).forEach { (tag, value) ->
                                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                                    tag,
                                    APPLICATION_DESCRIPTION_STRING_ID + i.toString(),
                                    value.toString()
                                )
                            }
                            details.isDescriptionFromUser = true
                        }
                        cmd.valueOf(OPT_APPLICATION_ICON + OPT_BAR + i.toString())?.let {
                            val iconFile = File(it.toString())
                            if (!checkIconFileExtension(iconFile)) {
                                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE${OPT_APPLICATION_ICON + OPT_BAR + i.toString()}: $it")
                            }
                            details.icon.localIcon.originalPath = iconFile.toPath().toAbsolutePath()
                        }
                        cmd.valueOf(OPT_APPLICATION_ICON_SET + OPT_BAR + i.toString())?.let {
                            getIconMapFromJsonString(it.toString()).forEach { (name, value) ->
                                details.iconSet.add(ApplicationIconDetailModel().apply {
                                    isInIconSet = true
                                    key = name
                                    localIcon.originalPath = value.toPath().toAbsolutePath()
                                })
                            }
                        }
                        cmd.valueOf(OPT_APPLICATION_TARGET_PACKAGE + OPT_BAR + i.toString())?.let {
                            target.workpathPackage = it.toString()
                        }
                    })
            }
        }
        // Auth agent
        cmd.valueOf(OPT_AUTHENTICATION_ID)?.let { authenticationId ->
            solutionProject.authenticationService.authenticationAgent.apply {
                includeAuthenticationAgent = true
                agentId = authenticationId.toString()
                enableSignoutNotification = true
                if (!isValidUuid(agentId)) {
                    throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_AUTHENTICATION_ID: $authenticationId")
                }
                cmd.valueOf(OPT_AUTHENTICATION_NAME)?.let {
                    name = it.toString()
                }
                title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                description.i18nAssetId = title.i18nAssetId
                title.stringId = AUTHENTICATION_TITLE_STRING_ID
                description.stringId = AUTHENTICATION_DESCRIPTION_STRING_ID
                cmd.valueOf(OPT_AUTHENTICATION_TITLE)?.let {
                    getMapFromJsonString(it.toString()).forEach { (tag, value) ->
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            tag,
                            AUTHENTICATION_TITLE_STRING_ID,
                            value.toString()
                        )
                    }
                }
                cmd.valueOf(OPT_AUTHENTICATION_DESCRIPTION)?.let {
                    getMapFromJsonString(it.toString()).forEach { (tag, value) ->
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            tag,
                            AUTHENTICATION_DESCRIPTION_STRING_ID,
                            value.toString()
                        )
                    }
                }
                cmd.valueOf(OPT_AUTHENTICATION_TARGET_PACKAGE)?.let {
                    workpathPackage = it.toString()
                }
                if (cmd.has(OPT_AUTHENTICATION_PRE_PROMPT)) {
                    enablePrePrompt = true
                }
            }
        }

        // usb accessories
        for (i in 1..ACCESSORIES_MAX) {
            cmd.valueOf(OPT_ACCESSORIES + OPT_BAR + i.toString())?.let {
                solutionProject.accessoriesService.usbAccessoriesAgent.apply {
                    if (!includeUsbAccessoriesAgent) {
                        includeUsbAccessoriesAgent = true
                        agentId = UUID.randomUUID().toString()
                        name =
                            solutionProject.solutionManager.solutionDetails.solutionDescription.name + ACCESSORIES_NAME
                        title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                        description.i18nAssetId = title.i18nAssetId
                        title.stringId = ACCESSORIES_TITLE_STRING_ID
                        description.stringId = ACCESSORIES_DESCRIPTION_STRING_ID
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            LANGUAGE_TAG_EN_US,
                            ACCESSORIES_TITLE_STRING_ID,
                            name
                        )
                        solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                            LANGUAGE_TAG_EN_US,
                            ACCESSORIES_DESCRIPTION_STRING_ID,
                            name
                        )
                    }
                    registrations.add(parseUsbRegistration(it.toString()))
                }
            }
        }
        // Copy
        if (cmd.has(OPT_COPY_JOB_AGENT)) {
            solutionProject.copyJobService.copyJobAgent.apply {
                includeCopyJobAgent = true
                agentId = UUID.randomUUID().toString()
                name = solutionProject.solutionManager.solutionDetails.solutionDescription.name + COPY_AGENT_NAME
                title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                description.i18nAssetId = title.i18nAssetId
                title.stringId = COPY_AGENT_TITLE_STRING_ID
                description.stringId = COPY_AGENT_DESCRIPTION_STRING_ID
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    COPY_AGENT_TITLE_STRING_ID,
                    name
                )
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    COPY_AGENT_DESCRIPTION_STRING_ID,
                    name
                )
            }
        }
        // Print Job
        if (cmd.has(OPT_PRINT_JOB_AGENT)) {
            solutionProject.printJobService.printJobAgent.apply {
                includePrintJobAgent = true
                agentId = UUID.randomUUID().toString()
                name = solutionProject.solutionManager.solutionDetails.solutionDescription.name + PRINT_AGENT_NAME
                title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                description.i18nAssetId = title.i18nAssetId
                title.stringId = PRINT_AGENT_TITLE_STRING_ID
                description.stringId = PRINT_AGENT_DESCRIPTION_STRING_ID
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    PRINT_AGENT_TITLE_STRING_ID,
                    name
                )
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    PRINT_AGENT_DESCRIPTION_STRING_ID,
                    name
                )
            }
        }
        // Scan (also included automatically when copy agent is specified)
        if (cmd.has(OPT_SCAN_JOB_AGENT) || cmd.has(OPT_COPY_JOB_AGENT)) {
            solutionProject.scanJobService.scanJobAgent.apply {
                includeScanJobAgent = true
                agentId = UUID.randomUUID().toString()
                name = solutionProject.solutionManager.solutionDetails.solutionDescription.name + SCAN_AGENT_NAME
                title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                description.i18nAssetId = title.i18nAssetId
                title.stringId = SCAN_AGENT_TITLE_STRING_ID
                description.stringId = SCAN_AGENT_DESCRIPTION_STRING_ID
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    SCAN_AGENT_TITLE_STRING_ID,
                    name
                )
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    SCAN_AGENT_DESCRIPTION_STRING_ID,
                    name
                )
            }
        }
        // Device Usage
        if (cmd.has(OPT_DEVICE_USAGE)) {
            solutionProject.deviceUsageService.deviceUsageAgent.apply {
                includeDeviceUsageAgent = true
                agentId = UUID.randomUUID().toString()
                name =
                    solutionProject.solutionManager.solutionDetails.solutionDescription.name + DEVICE_USAGE_AGENT_NAME
                title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                description.i18nAssetId = title.i18nAssetId
                title.stringId = DEVICE_USAGE_AGENT_TITLE_STRING_ID
                description.stringId = DEVICE_USAGE_AGENT_DESCRIPTION_STRING_ID
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    DEVICE_USAGE_AGENT_TITLE_STRING_ID,
                    name
                )
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    DEVICE_USAGE_AGENT_DESCRIPTION_STRING_ID,
                    name
                )
            }
        }
        // TODO Statistics Agent
        // Supplies
        if (cmd.has(OPT_SUPPLIES_AGENT)) {
            solutionProject.suppliesService.suppliesAgent.apply {
                includeSuppliesAgent = true
                agentId = UUID.randomUUID().toString()
                name = solutionProject.solutionManager.solutionDetails.solutionDescription.name + SUPPLIES_AGENT_NAME
                title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
                description.i18nAssetId = title.i18nAssetId
                title.stringId = SUPPLIES_AGENT_TITLE_STRING_ID
                description.stringId = SUPPLIES_AGENT_DESCRIPTION_STRING_ID
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    SUPPLIES_AGENT_TITLE_STRING_ID,
                    name
                )
                solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    SUPPLIES_AGENT_DESCRIPTION_STRING_ID,
                    name
                )
            }
        }
        // WebService Agent
        if (cmd.has(OPT_WEBSERVICE_AGENT)) {
            solutionProject.webService.webServiceAgent.apply {
                includeWebServiceInfo = true
                uuid = UUID.randomUUID().toString()
                cmd.valueOf(OPT_WEBSERVICE_TITLE)?.let {
                    titles.addAll(getLocalizedArrayFromJsonString(it.toString()))
                }
                cmd.valueOf(OPT_WEBSERVICE_DESCRIPTION)?.let {
                    descriptions.addAll(getLocalizedArrayFromJsonString(it.toString()))
                }
                cmd.valueOf(OPT_WEBSERVICE_ENDPOINT)?.let {
                    webServiceEndPoints.addAll(parseWebServiceEndPoint(it.toString()))
                }
            }
        }
        // Security Agent
        solutionProject.securityService.securityAgent.apply {
            includeSecurityAgent = true
            securityContextExpressionsEnabled = true
            declaredExpressionOperators.addAll(USER_INFORMATION)
            agentId = (agentModels.get(this.javaClass.name) as? SecurityAgentModel)
                ?.agentId
                .takeIf { !it.isNullOrEmpty() }
                ?: UUID.randomUUID().toString()
            name =
                solutionProject.solutionManager.solutionDetails.solutionDescription.name + SECURITY_AGENT_NAME
            title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
            description.i18nAssetId = title.i18nAssetId
            title.stringId = SECURITY_AGENT_TITLE_STRING_ID
            description.stringId = SECURITY_AGENT_DESCRIPTION_STRING_ID
            solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                LANGUAGE_TAG_EN_US,
                SECURITY_AGENT_TITLE_STRING_ID,
                name
            )
            solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                LANGUAGE_TAG_EN_US,
                SECURITY_AGENT_DESCRIPTION_STRING_ID,
                name
            )
        }
        // extract icon, i18n from apk file
        val apkParser = ApkParser(solutionProject.apkPath.toFile())

        // Solution Notification
        solutionProject.notificationService.notificationAgent.apply {
            includeNotificationAgent = true
            notificationsToReceiveModel.explict.explicitValue.add(NotificationType.NtConfigurationModified)
            agentId = (agentModels.get(this.javaClass.name) as? SolutionNotificationAgentModel)
                ?.agentId
                .takeIf { !it.isNullOrEmpty() }
                ?: UUID.randomUUID().toString()
            // androidPackageName is set on apkParser
            notificationTargetModel.workpathPlatformClientTargetModel.androidPackageName =
                apkParser.getAndroidPackageName()
        }
        // Solution Diagnostics
        solutionProject.solutionDiagnosticsService.solutionDiagnosticsAgent.apply {
            includeSolutionDiagnosticsAgent = true
            agentId = (agentModels.get(this.javaClass.name) as? SolutionDiagnosticsModel)
                ?.agentId
                .takeIf { !it.isNullOrEmpty() }
                ?: UUID.randomUUID().toString()
            name =
                solutionProject.solutionManager.solutionDetails.solutionDescription.name + SOLUTION_DIAGNOSTICS_AGENT_NAME
            title.i18nAssetId = solutionProject.applicationService.defaultI18nAsset.i18nAssetId
            description.i18nAssetId = title.i18nAssetId
            title.stringId = SOLUTION_DIAGNOSTICS_AGENT_TITLE_STRING_ID
            description.stringId = SOLUTION_DIAGNOSTICS_AGENT_DESCRIPTION_STRING_ID
            solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                LANGUAGE_TAG_EN_US,
                SOLUTION_DIAGNOSTICS_AGENT_TITLE_STRING_ID,
                name
            )
            solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                LANGUAGE_TAG_EN_US,
                SOLUTION_DIAGNOSTICS_AGENT_DESCRIPTION_STRING_ID,
                name
            )
        }


        for (application in solutionProject.applicationService.applications) {
            application.details.icon.localIcon.apply {
                if (originalPath == null) { // extract icon
                    originalPath = apkParser.getIconPath(application.target.workpathPackage)
                }
                if (originalPath != null) { // set icon path (both user, apk)
                    fileType = getIconFileType(originalPath!!)
                    path = ASSETS_ICONS + originalPath!!.name
                }
            }
            if (application.details.iconSet.isEmpty()) { //isNotNull when user data exist
                val iconSet = apkParser.getIconSetPath(application.target.workpathPackage)
                iconSet?.forEach { (name, value) ->
                    application.details.iconSet.add(ApplicationIconDetailModel().apply {
                        isInIconSet = true
                        key = name
                        localIcon.originalPath = value
                    })
                }
            }
            application.details.iconSet.forEach { // set icon path (both user, apk)
                if (it.localIcon.originalPath != null) {
                    it.localIcon.fileType = getIconFileType(it.localIcon.originalPath!!)
                    it.localIcon.path = ASSETS_ICONS + it.key + '/' + it.localIcon.originalPath!!.name
                }
            }
            if (!application.details.isTitleFromUser) {
                apkParser.getTitle(application.target.workpathPackage)?.forEach { (tag, value) ->
                    solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                        tag,
                        application.details.title.stringId,
                        value
                    )
                } ?: solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    application.details.title.stringId,
                    "no_title"
                )
            }
            if (!application.details.isDescriptionFromUser) {
                apkParser.getDescription(application.target.workpathPackage)?.forEach { (tag, value) ->
                    solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                        tag,
                        application.details.description.stringId,
                        value
                    )
                } ?: solutionProject.applicationService.addI18nStringToDefaultI18nAsset(
                    LANGUAGE_TAG_EN_US,
                    application.details.description.stringId,
                    "no_description"
                )
            }
        }
        // set workpathPlatformClientTargetModels
        //USBAccessory
        solutionProject.accessoriesService.usbAccessoriesAgent.registrationTarget.workpathPlatformClientTargetModel.androidPackageName =
            apkParser.getAndroidPackageName()
        //Copy
        solutionProject.copyJobService.copyJobAgent.copyNotificationTargetModel.workpathPlatformClientTargetModel.androidPackageName =
            apkParser.getAndroidPackageName()
        //Scan
        solutionProject.scanJobService.scanJobAgent.scanNotificationTargetModel.workpathPlatformClientTargetModel.androidPackageName =
            apkParser.getAndroidPackageName()
        return solutionProject
    }

    fun getJsonBundleFromCommand(cmd: OptionSet): SolutionProject {
        println(MESSAGE.getString("task_loading"))
        val solutionProject = SolutionProject()
        cmd.valueOf(OPT_OUTPUT)?.let {
            solutionProject.outputPathString = Path.of(it.toString()).toAbsolutePath().toString()
        }
        cmd.valueOf(OPT_SOLUTION_APK)?.let {
            val apkFile = File(it.toString())
            apkParser = ApkParser(apkFile)
            if (!apkFile.exists() || !apkFile.extension.equals(APK_EXTENSION, true)) {
                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_SOLUTION_APK: $it")
            }
            solutionProject.solutionManager.workpathPlatformPackage.workpathPackagePath = apkFile.name
            solutionProject.solutionManager.workpathPlatformPackage.installFile = apkFile.name
            solutionProject.apkPath = apkFile.toPath().toAbsolutePath()

            val mainApplication = ApplicationAgentModel().apply {
                details.applicationId = UUID.randomUUID().toString()
                details.name = apkParser!!.getAndroidPackageName()
                target.workpathPackage = ""
                target.isMainApplication = true
            }

            solutionProject.applicationService.applications.add(mainApplication)

            for (application in solutionProject.applicationService.applications) {
                application.details.icon.localIcon.apply {
                    if (originalPath == null) { // extract icon
                        originalPath = apkParser!!.getIconPath(application.target.workpathPackage)
                    }
                    if (originalPath != null) { // set icon path (both user, apk)
                        fileType = getIconFileType(originalPath!!)
                        path = ASSETS_ICONS + originalPath!!.name
                    }
                }
                if (application.details.iconSet.isEmpty()) {
                    val iconSet = apkParser!!.getIconSetPath(application.target.workpathPackage)
                    iconSet?.forEach { (name, value) ->
                        application.details.iconSet.add(ApplicationIconDetailModel().apply {
                            isInIconSet = true
                            key = name
                            localIcon.originalPath = value
                        })
                    }
                }
                application.details.iconSet.forEach {
                    if (it.localIcon.originalPath != null) {
                        it.localIcon.fileType = getIconFileType(it.localIcon.originalPath!!)
                        it.localIcon.path = ASSETS_ICONS + it.key + '/' + it.localIcon.originalPath!!.name
                    }
                }
            }

        }
        cmd.valueOf(OPT_CONNECTOR)?.let {
            val manifestFile = File(it.toString())
            val solutionManifest = ObjectMapper().readValue(manifestFile, SolutionManifest::class.java)
            if (!manifestFile.exists() || !manifestFile.extension.equals(JSON_EXTENSION, true)) {
                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_CONNECTOR: $it")
            }
            solutionProject.jsonManifestPath = manifestFile.toPath().toAbsolutePath()

            solutionProject.solutionManager.solutionDetails.solutionDescription.apply {
                vendor = solutionManifest.description.vendor
                name = solutionManifest.description.name
                description = solutionManifest.description.description
                version = solutionManifest.description.version
                date = solutionManifest.description.date
                supportEmail = solutionManifest.description.supportEmail
                supportPhone = solutionManifest.description.supportPhone
                supportUrl = solutionManifest.description.supportUrl
            }
            solutionProject.solutionManager.solutionDetails.solutionId = solutionManifest.solutionId.toString()
        }

        cmd.valueOf(OPT_SOLUTION_DEFAULT_CONFIG_FILE)?.let { configPath ->
            val configFile = File(configPath.toString())
            if (!configFile.exists()) {
                throw IllegalArgumentException("Config file not found: $configPath")
            }
            solutionProject.solutionManager.configuration.apply {
                includeConfiguration = true
                archiveDataPath = ASSETS_CONFIGS + configFile.name
                description = ""
                mimeType = DEFAULT_CONFIG_MIME
            }
            solutionProject.configPath = Path.of(configPath.toString()).toAbsolutePath()
        } ?: run {
            val defaultConfig = ConfigUtils.getDefaultConfigFile()
            if (defaultConfig != null) {
                solutionProject.solutionManager.configuration.apply {
                    includeConfiguration = true
                    archiveDataPath = ASSETS_CONFIGS + "default.json"
                    description = "Default configuration"
                    mimeType = DEFAULT_CONFIG_MIME
                }
                solutionProject.configPath = defaultConfig.toPath().toAbsolutePath()
            }
        }
        return solutionProject
    }

    private fun parseUsbRegistration(value: String): UsbRegistrationModel {
        val registration = value.split(",".toRegex()).toTypedArray()
        return UsbRegistrationModel().apply {
            setRegistrationType(registration[0])
            vendorId = registration[1].toInt()
            productId = registration[2].toInt()
            if (registration.size > 3) {
                serialNumber = registration[3]
            }
        }
    }

    private fun parseWebServiceEndPoint(value: String): ArrayList<WebServiceEndPoint> {
        val webServiceEndpoints = arrayListOf<WebServiceEndPoint>()
        val endPoints = value.split(":".toRegex()).toTypedArray()
        endPoints.forEach { endPoint ->
            val endPointInfo = endPoint.split(",".toRegex()).toTypedArray()
            webServiceEndpoints.add(WebServiceEndPoint().apply {
                setMethodType(endPointInfo[0])
                category = endPointInfo[1]
                absolutePath = endPointInfo[2]
                if (endPointInfo.size > 3) {
                    setAuthType(endPointInfo[3])
                } else {
                    authType = WebServiceEndPoint.AuthType.NONE
                }
            })
        }
        return webServiceEndpoints
    }

    fun getConnectionDataFromCommand(cmd: OptionSet): ConnectionData {
        val connectionData = ConnectionData()
        // Install, Uninstall, Solution list, Solution detail, Config get, Config Update
        cmd.valueOf(OPT_HOST)?.let {
            connectionData.networkAddress = it.toString()
        }
        cmd.valueOf(OPT_PASSWORD)?.let {
            connectionData.password = it.toString()
        }
        // Install
        cmd.valueOf(OPT_INSTALL_FILE)?.let {
            val hpkFile = File(it.toString())
            if (!listOf(BDL_EXTENSION, HPK2_EXTENSION).any { ext -> hpkFile.extension.equals(ext, true) }) {
                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_INSTALL_FILE: $it")
            }
            connectionData.installFilePath = hpkFile.toPath().toAbsolutePath()
        }
        if (cmd.has(OPT_FORCE_INSTALL)) {
            connectionData.installForce = true
        }
        // Uninstall, Solution detail, Config get, Config Update
        cmd.valueOf(OPT_SOLUTION_ID)?.let {
            connectionData.solutionId = it.toString()
            if (!isValidUuid(connectionData.solutionId)) {
                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_SOLUTION_ID: $it")
            }
        }
        // Config Update
        cmd.valueOf(OPT_CONFIG_DATA)?.let {
            try {
                connectionData.configData = sanitizeJsonString(it.toString())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("$EXCEPTION_INVALID_JSON$OPT_CONFIG_DATA: $it")
            }
        }
        return connectionData
    }

    fun getAttestationDataFromCommand(cmd: OptionSet): ConnectionData {
        val connectionData = ConnectionData()
        cmd.valueOf(OPT_HOST)?.let {
            connectionData.networkAddress = it.toString()
        }
        cmd.valueOf(OPT_PASSWORD)?.let {
            connectionData.password = it.toString()
        }
        cmd.valueOf(OPT_SOLUTION_ID)?.let {
            connectionData.solutionId = it.toString()
            if (!isValidUuid(connectionData.solutionId)) {
                throw IllegalArgumentException("$EXCEPTION_WRONG_VALUE$OPT_SOLUTION_ID: $it")
            }
        }
        cmd.valueOf(OPT_ATTESTATION_NAME)?.let {
            connectionData.userName = it.toString()
        }
        cmd.valueOf(OPT_ATTESTATION_LDB_KEY)?.let {
            connectionData.key = it.toString()
        }
        cmd.valueOf(OPT_ATTESTATION_DATA)?.let {
            try {
                connectionData.attestationData = sanitizeJsonString(it.toString())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("$EXCEPTION_INVALID_JSON$OPT_ATTESTATION_DATA: $it")
            }
        }
        cmd.valueOf(OPT_ATTESTATION_CMD_LOC)?.let { filePath ->
            try {
                val commandFile = File(filePath.toString())
                if (!commandFile.exists()) {
                    throw IllegalArgumentException("Attestation file not found: $filePath")
                }
                if (!commandFile.canRead()) {
                    throw IllegalArgumentException("Cannot read attestation file: $filePath")
                }

                val commandContent = commandFile.readText()
                connectionData.commandData = commandContent

            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to process attestation file: ${e.message}")
            }
        }
        return connectionData
    }
}
