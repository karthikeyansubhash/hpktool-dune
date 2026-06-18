package com.hp.workpath.pkgmgt.lib;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "mod")
public class Mod {

    @Element
    private String resourceFile;
}
