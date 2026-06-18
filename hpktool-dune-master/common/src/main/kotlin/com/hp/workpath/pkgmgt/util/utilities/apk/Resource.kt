package com.hp.workpath.pkgmgt.util.utilities.apk

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Root(name = "resource", strict = false)
class Resource {
    @field:ElementList(name = "string", required = false ,inline = true)
    var resourceString: MutableList<ResourceString> = mutableListOf()
}

@Root(name = "string", strict = false)
class ResourceString {
    @field:Text(required = false)
    var value: String = ""
    @field:Attribute(name = "name", required = true)
    var name: String = ""
}