package com.hp.workpath.pkgmgt.lib;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import java.util.UUID;

public class UUIDConverter implements Converter<UUID> {

    public UUID read(InputNode node) {
        try {
            String uuid = node.getValue();

            return UUID.fromString(uuid);
        } catch (Exception e) {
            return null;
        }
    }

    public void write(OutputNode node, UUID uuid) {
        node.setValue(uuid.toString());
    }
}