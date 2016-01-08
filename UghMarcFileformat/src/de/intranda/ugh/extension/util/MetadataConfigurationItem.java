package de.intranda.ugh.extension.util;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.intranda.ugh.extension.MarcFileformat;

public @Data class MetadataConfigurationItem {

    private String internalMetadataName;

    private String separator = "; ";

    private List<MarcField> fieldList = new ArrayList<MarcField>();
    
    private String identifierField = "";
    private String identifierConditionField = "";
    private String identifierReplacement = "";

    private String conditionField = "";
    private String conditionValue = "";
    private String fieldReplacement = "";
    
    private boolean separateEntries = true;

    
    
    public MetadataConfigurationItem(Node node) {
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_INTERNAL_METADATA_NAME)) {
                    internalMetadataName = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_SEPARATOR)) {
                    separator = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_FIELD_NAME)) {
                    fieldList.add(new MarcField(n));
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_SEPARATE_ENTRIES)) {
                    String value = MarcFileformat.readTextNode(n);
                    if (value != null && value.equalsIgnoreCase("true")) {
                        separateEntries = true;
                    } else {
                        separateEntries = false;
                    }
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_IDENTIFIER)) {
                    identifierField = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_IDENTIFIER_CONDITION)) {
                    identifierConditionField = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_IDENTIFIER_REPLACEMENT)) {
                    identifierReplacement = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_CONDITION_FIELD)) {
                    conditionField = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_CONDITION_VALUE)) {
                    conditionValue = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_VALUE_REPLACEMENT)) {
                    fieldReplacement = MarcFileformat.readTextNode(n);
                }

            }
        }
    }
}
