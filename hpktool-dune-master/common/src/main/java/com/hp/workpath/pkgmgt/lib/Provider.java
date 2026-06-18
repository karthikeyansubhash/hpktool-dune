package com.hp.workpath.pkgmgt.lib;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

@Root(name = "provider")
public class Provider {

    @Attribute
    private String type;

    @Element(required = false)
    private String uuid;

    @ElementList(required = false)
    private ArrayList<LocalizedString> title;

    @ElementList(required = false)
    private ArrayList<LocalizedString> description;

    @Element(required = false)
    private String authenticationUrl;

    @Element(required = false)
    private String enablePrePromptCheck;

    @Element(required = false)
    private String enableHomeScreenMode;

    @Element(required = false)
    private String configOnInstall;

    @Element(required = false)
    private String registrationType;

    @Element(required = false)
    private String vendorId;

    @Element(required = false)
    private String productId;

    @Element(required = false)
    private String serialNumber;

    @Element(required = false)
    private Boolean ackRequiredForDelete;

    @Element(required = false, data = true)
    private String endPoints;

    public String getAuthenticationUrl() {
        return authenticationUrl;
    }

    public void setAuthenticationUrl(String authenticationUrl) {
        this.authenticationUrl = authenticationUrl;
    }

    public String getEnablePrePromptCheck() {
        return enablePrePromptCheck;
    }

    public void setEnablePrePromptCheck(String enablePrePromptCheck) {
        this.enablePrePromptCheck = enablePrePromptCheck;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<LocalizedString> getTitle() {
        return title;
    }

    public void setTitle(ArrayList<LocalizedString> title) {
        this.title = title;
    }

    public ArrayList<LocalizedString> getDescription() {
        return description;
    }

    public void setDescription(ArrayList<LocalizedString> description) {
        this.description = description;
    }

    public String getEnableHomeScreenMode() {
        return enableHomeScreenMode;
    }

    public void setEnableHomeScreenMode(String enableHomeScreenMode) {
        this.enableHomeScreenMode = enableHomeScreenMode;
    }

    public String getConfigOnInstall() {
        return configOnInstall;
    }

    public void setConfigOnInstall(String configOnInstall) {
        this.configOnInstall = configOnInstall;
    }

    public String getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Boolean getAckRequiredForDelete() {
        return ackRequiredForDelete;
    }

    public void setAckRequiredForDelete(Boolean ackRequiredForDelete) {
        this.ackRequiredForDelete = ackRequiredForDelete;
    }

    public String getEndPoints() {
        return endPoints;
    }

    public void setEndPoints(String endPoints) {
        this.endPoints = endPoints;
    }
}
