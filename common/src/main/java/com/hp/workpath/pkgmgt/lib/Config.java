package com.hp.workpath.pkgmgt.lib;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "config")
public class Config {

    @Attribute
    public String key;

    @Attribute
    public String schema;

    @Attribute(name = "content-type")
    public String contentType;

    @Element
    private String resourceFile;
}
