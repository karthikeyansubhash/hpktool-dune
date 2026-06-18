package com.hp.workpath.pkgmgt.lib;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

//platformVersion from hpk_2.4 (SDK1.3)
@Root(name = "hpk")
public class Connector {
    public static final String XML_FILENAME = "hpk.xml";

    @Attribute(name = "schemaLocation")
    @Namespace(reference = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    private String schemaLocation;

    @Attribute(name = "xmlns", required = false)
    private String namespace;

    @Element
    @Convert(UUIDConverter.class)
    private UUID uuid;

    @Element
    @Convert(LimitedStringConverter.class)
    private String name;

    @Element
    @Convert(LimitedStringConverter.class)
    private String vendorName;

    @Element
    private String date;

    @Element
    private PlatformType platformType;

    /**
     * platformVersion is required in HPK1.3
     * but for backward compatibility, temporary set required = false
     * and platformVersion will check in the code when create HPK file.
     */
    @Element(required = false)
    private String platformVersion;

    @Element
    private String installFile;

    @ElementList(required = false)
    private ArrayList<SubApp> subApps;

    @Element(required = false)
    private String defaultConfig;

    @ElementList(required = false)
    private ArrayList<Config> configs;

    @ElementList(required = false)
    private ArrayList<Mod> mods;

    @ElementList(required = false)
    private ArrayList<Provider> providers;

    public ArrayList<Provider> getProviders() {
        return providers;
    }

    public void setProviders(ArrayList<Provider> providers) {
        this.providers = providers;
    }

    public Connector() {
        namespace = "http://www.hp.com/schemas/jetadvantage/link/hpk/v2.5";
        schemaLocation = "http://www.hp.com/schemas/jetadvantage/link/hpk/v2.5 hpk.xsd";
    }

    public Connector(String version) {
        if(version == null) {
            version = "v2.5";
        }
        namespace = "http://www.hp.com/schemas/jetadvantage/link/hpk/" + version;
        schemaLocation = "http://www.hp.com/schemas/jetadvantage/link/hpk/"+ version +" hpk.xsd";
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(String defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public PlatformType getPlatformType() {
        return platformType;
    }

    public void setPlatformType(PlatformType platformType) {
        this.platformType = platformType;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getInstallFile() {
        return installFile;
    }

    public void setInstallFile(String installFile) {
        this.installFile = installFile;
    }

    public ArrayList<SubApp> getSubAppList() {
        return subApps;
    }

    public void setSubAppList(ArrayList<SubApp> subApps) {
        this.subApps = subApps;
    }

    public ArrayList<SubApp> getSubApps() {
        return subApps;
    }

    public void setSubApps(ArrayList<SubApp> subApps) {
        this.subApps = subApps;
    }

    public ArrayList<Config> getConfigs() {
        return configs;
    }

    public void setConfigs(ArrayList<Config> configs) {
        this.configs = configs;
    }

    public ArrayList<Mod> getMods() {
        return mods;
    }

    public void setMods(ArrayList<Mod> mods) {
        this.mods = mods;
    }

    public void writeTo(final OutputStream os) throws Exception {
        HpkFile.SERIALIZER.write(this, os);
    }

    public String asString() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeTo(bos);
        return bos.toString("utf-8");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Connector connector = (Connector) o;

        if (schemaLocation != null ? !schemaLocation.equals(connector.schemaLocation) : connector.schemaLocation != null)
            return false;
        if (!uuid.equals(connector.uuid)) return false;
        if (!name.equals(connector.name)) return false;
        if (!vendorName.equals(connector.vendorName)) return false;
        if (!date.equals(connector.date)) return false;
        if (platformType != connector.platformType) return false;
        if (!platformVersion.equals(connector.platformVersion)) return false;
        if (!installFile.equals(connector.installFile)) return false;
        if (subApps != null ? !subApps.equals(connector.subApps) : connector.subApps != null)
            return false;
        if (defaultConfig != null ? !defaultConfig.equals(connector.defaultConfig) : connector.defaultConfig != null)
            return false;
        if (configs != null ? !configs.equals(connector.configs) : connector.configs != null)
            return false;
        if (mods != null ? !mods.equals(connector.mods) : connector.mods != null) return false;
        return providers != null ? providers.equals(connector.providers) : connector.providers == null;
    }

    @Override
    public int hashCode() {
        int result = schemaLocation != null ? schemaLocation.hashCode() : 0;
        result = 31 * result + uuid.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + vendorName.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + platformType.hashCode();
        result = 31 * result + platformVersion.hashCode();
        result = 31 * result + installFile.hashCode();
        result = 31 * result + (subApps != null ? subApps.hashCode() : 0);
        result = 31 * result + (defaultConfig != null ? defaultConfig.hashCode() : 0);
        result = 31 * result + (configs != null ? configs.hashCode() : 0);
        result = 31 * result + (mods != null ? mods.hashCode() : 0);
        result = 31 * result + (providers != null ? providers.hashCode() : 0);
        return result;
    }
}
