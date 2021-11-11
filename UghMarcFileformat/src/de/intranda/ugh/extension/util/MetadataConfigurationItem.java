package de.intranda.ugh.extension.util;

/******************************************************************************
 * Copyright notice
 *
 * (c) 2016 intranda GmbH, Göttingen
 * http://www.intranda.com
 *
 * All rights reserved
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The GNU Lesser General Public License can be found at
 * http://www.gnu.org/licenses/lgpl-3.0
 *
 * This Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * This copyright notice MUST APPEAR in all copies of this file!
 ******************************************************************************/
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.intranda.ugh.extension.MarcFileformat;
import lombok.Data;

public @Data class MetadataConfigurationItem {

    private String internalMetadataName;

    private String separator = "; ";

    private List<MarcField> fieldList = new ArrayList<>();

    private String identifierField = "";
    private String identifierConditionField = "";
    private String identifierReplacement = "";

    private String conditionField = "";
    private String conditionValue = "";
    private String fieldReplacement = "";

    private boolean separateEntries = true;
    private Boolean separateSubfields = null;   //a null value means that the value of separateEntries should be used

    private boolean abortAfterFirstMatch = true;

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
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_SEPARATE_SUBFIELDS)) {
                    String value = MarcFileformat.readTextNode(n);
                    if (value != null && value.equalsIgnoreCase("true")) {
                        separateSubfields = true;
                    } else if (value != null && value.equalsIgnoreCase("false")) {
                        separateSubfields = false;
                    } else {
                        separateSubfields = null;
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
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_ABORT_AFTER_MATCH)) {
                    String value = MarcFileformat.readTextNode(n);
                    if (value != null && value.equalsIgnoreCase("true")) {
                        abortAfterFirstMatch = true;
                    } else {
                        abortAfterFirstMatch = false;
                    }
                }

            }
        }
    }
    
    public boolean isSeparateSubfields() {
        return Optional.ofNullable(separateSubfields).orElse(separateEntries);
    }
}
