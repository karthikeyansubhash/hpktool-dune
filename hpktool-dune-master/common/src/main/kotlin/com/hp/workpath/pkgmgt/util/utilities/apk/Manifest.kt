package com.hp.workpath.pkgmgt.util.utilities.apk

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

/**
 * reference web page:
 * https://developer.android.com/guide/topics/manifest/manifest-intro
 */

@Root(name = "manifest", strict = false)
@Namespace(reference = "http://schemas.android.com/apk/res/android", prefix = "android")
class Manifest {
    @field:Attribute(name = "package", required = true)
    var packageName: String = ""

    @field:Element(name = "application", required = true)
    var application: Application = Application()
}

@Root(name = "application", strict = false)
class Application {
    @field:Attribute(name = "icon", required = false)
    var icon: String = ""

    @field:Attribute(name = "label", required = false)
    var label: String = ""

    @field:ElementList(name = "activity", required = false, inline = true)
    var activity: MutableList<Activity> = mutableListOf()
}

@Root(name = "activity", strict = false)
class Activity {
    @field:Attribute(name = "name", required = true)
    var activityName: String = ""

    @field:Attribute(name = "icon", required = false)
    var icon: String = ""

    @field:Attribute(name = "label", required = false)
    var label: String = ""

    @field:ElementList(name = "meta-data", required = false, inline = true)
    var metaData: MutableList<Metadata> = mutableListOf()

    @field:ElementList(name = "intent-filter", required = false, inline = true)
    var intentFilter: MutableList<IntentFilter> = mutableListOf()
}

@Root(name = "meta-data", strict = false)
class Metadata {
    @field:Attribute(name = "name", required = true)
    var metaDataName: String = ""

    @field:Attribute(name = "resource", required = false)
    var resource: String = ""

    @field:Attribute(name = "value", required = false)
    var value: String = ""
}

@Root(name = "intent-filter", strict = false)
class IntentFilter {
    @field:ElementList(name = "action", required = false, inline = true)
    var action: MutableList<Action> = mutableListOf()

    @field:ElementList(name = "category", required = false, inline = true)
    var category: MutableList<Category> = mutableListOf()
}

@Root(name = "action", strict = false)
class Action {
    @field:Attribute(name = "name", required = true)
    var actionName: String = ""
}

@Root(name = "category", strict = false)
class Category {
    @field:Attribute(name = "name", required = true)
    var categoryName: String = ""
}