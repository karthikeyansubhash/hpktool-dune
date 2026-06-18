package com.hp.workpath.pkgmgt.lib;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

@Root(name = "LocalizedString")
public class LocalizedString {

    @Element(required = false)
    @Convert(LimitedStringConverter.class)
    public String languageTag;

    @Element(required = false)
    @Convert(LimitedStringConverter.class)
    public String code;

    @Element
    public String value;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLanguageTag() {
        return languageTag;
    }

    public void setLanguageTag(String languageTag) {
        this.languageTag = languageTag;
    }
}
