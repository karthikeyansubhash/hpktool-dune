package com.hp.workpath.pkgmgt.lib;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class LimitedStringConverter implements Converter<String> {
    // from xml to data
    public String read(InputNode node) {
        try {
            return toLimitedString(node.getValue());
        } catch (Exception e) {
            return null;
        }
    }
    // from data to xml
    public void write(OutputNode node, String input) {
        node.setValue(toLimitedString(input));
    }
    /* <xs:whiteSpace value="collapse"/>
     * All occurrences of #x9 (tab), #xA (line feed) and #xD (carriage return) are replaced with #x20 (space),
     * contiguous sequences of #x20s are collapsed to a single #x20, and initial and/or final #x20s are deleted.
     */
    private String toLimitedString(String input) {
        return input.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ')
                .trim().replaceAll("\\s+", " ");
    }
}
