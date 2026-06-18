package com.hp.workpath.pkgmgt.lib;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "LocalizedString")
public class ExplicitLocalizedString {

    @Element
    public String languageTag;

    @Element
    public String value;

    public String getLanguageTag() {
        return languageTag;
    }

    public void setLanguageTag(String languageTag) {
        this.languageTag = languageTag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
