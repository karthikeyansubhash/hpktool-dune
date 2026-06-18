package com.hp.workpath.pkgmgt.lib;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
public class Param {

    @Attribute
    public String name;

    @Attribute
    public String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
