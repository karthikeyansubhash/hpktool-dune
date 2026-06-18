package com.hp.workpath.pkgmgt.lib;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

import java.util.UUID;

@Root(name = "subApp")
public class SubApp {
    @Element
    @Convert(UUIDConverter.class)
    private UUID uuid;

    @Element
    private String platformId;

    @Element(required = false)
    private String functionType;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubApp subApp = (SubApp) o;

        if (uuid != null ? !uuid.equals(subApp.uuid) : subApp.uuid != null) return false;
        return platformId != null ? platformId.equals(subApp.platformId) : subApp.platformId == null;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (platformId != null ? platformId.hashCode() : 0);
        return result;
    }
}
