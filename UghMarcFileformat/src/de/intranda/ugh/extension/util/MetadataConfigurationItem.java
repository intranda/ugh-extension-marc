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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetadataConfigurationItem {

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
    private Boolean separateSubfields = null; //a null value means that the value of separateEntries should be used
    private boolean separateMainfields = false;

    private String separationType = null;

    private boolean abortAfterFirstMatch = true;

    public MetadataConfigurationItem(Node node) {
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (MarcFileformat.PREFS_MARC_INTERNAL_METADATA_NAME.equalsIgnoreCase(n.getNodeName())) {
                    internalMetadataName = MarcFileformat.readTextNode(n);
                } else if (MarcFileformat.PREFS_MARC_SEPARATOR.equalsIgnoreCase(n.getNodeName())) {
                    separator = MarcFileformat.readTextNode(n);
                } else if (MarcFileformat.PREFS_MARC_FIELD_NAME.equalsIgnoreCase(n.getNodeName())) {
                    fieldList.add(new MarcField(n));
                } else if (MarcFileformat.PREFS_MARC_SEPARATE_ENTRIES.equalsIgnoreCase(n.getNodeName())) {
                    String value = MarcFileformat.readTextNode(n);
                    if (value != null && "true".equalsIgnoreCase(value)) {
                        separateEntries = true;
                    } else {
                        separateEntries = false;
                    }
                } else if (MarcFileformat.PREFS_MARC_SEPARATE_SUBFIELDS.equalsIgnoreCase(n.getNodeName())) {
                    String value = MarcFileformat.readTextNode(n);
                    if (value != null && "true".equalsIgnoreCase(value)) {
                        separateSubfields = true;
                    } else if (value != null && "false".equalsIgnoreCase(value)) {
                        separateSubfields = false;
                    } else {
                        separateSubfields = null;
                    }
                } else if (MarcFileformat.PREFS_MARC_IDENTIFIER.equalsIgnoreCase(n.getNodeName())) {
                    identifierField = MarcFileformat.readTextNode(n);
                } else if (MarcFileformat.PREFS_MARC_IDENTIFIER_CONDITION.equalsIgnoreCase(n.getNodeName())) {
                    identifierConditionField = MarcFileformat.readTextNode(n);
                } else if (MarcFileformat.PREFS_MARC_IDENTIFIER_REPLACEMENT.equalsIgnoreCase(n.getNodeName())) {
                    identifierReplacement = MarcFileformat.readTextNode(n);
                    if (identifierReplacement == null) {
                        identifierReplacement = "";
                    }
                } else if (MarcFileformat.PREFS_MARC_CONDITION_FIELD.equalsIgnoreCase(n.getNodeName())) {
                    conditionField = MarcFileformat.readTextNode(n);
                } else if (MarcFileformat.PREFS_MARC_CONDITION_VALUE.equalsIgnoreCase(n.getNodeName())) {
                    conditionValue = MarcFileformat.readTextNode(n);
                } else if (MarcFileformat.PREFS_MARC_VALUE_REPLACEMENT.equalsIgnoreCase(n.getNodeName())) {
                    fieldReplacement = MarcFileformat.readTextNode(n);
                    if (fieldReplacement == null) {
                        fieldReplacement = "";
                    }
                } else if (MarcFileformat.PREFS_MARC_ABORT_AFTER_MATCH.equalsIgnoreCase(n.getNodeName())) {
                    String value = MarcFileformat.readTextNode(n);
                    if (value != null && "true".equalsIgnoreCase(value)) {
                        abortAfterFirstMatch = true;
                    } else {
                        abortAfterFirstMatch = false;
                    }
                } else if ("separationType".equalsIgnoreCase(n.getNodeName())) {
                    separationType = MarcFileformat.readTextNode(n);
                }
            }
        }
        if (separationType != null) {
            if ("subfield".equalsIgnoreCase(separationType)) {
                separateEntries = false;
                separateSubfields = true;
                separateMainfields = false;
            } else if ("mainfield".equalsIgnoreCase(separationType)) {
                separateEntries = false;
                separateSubfields = false;
                separateMainfields = true;
            } else if ("none".equalsIgnoreCase(separationType)) {
                separateEntries = false;
                separateSubfields = false;
                separateMainfields = false;
            } else {
                separateEntries = true;
                separateSubfields = false;
                separateMainfields = false;
            }
        }
    }
    
    public boolean isSeparateSubfields() {
        return Optional.ofNullable(separateSubfields).orElse(separateEntries);
    }

    public String getSeparator() {
        return Optional.ofNullable(separator).orElse("");
    }
}
