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
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.intranda.ugh.extension.MarcFileformat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class SubfieldGroupConfigurationItem {

    private String groupName;

    private String fieldMainTag;
    private String fieldInd1 = "any";
    private String fieldInd2 = "any";

    private List<MetadataConfigurationItem> metadataList = new LinkedList<>();
    private List<MetadataConfigurationItem> personList = new LinkedList<>();
    private List<MetadataConfigurationItem> corporationList = new LinkedList<>();

    public SubfieldGroupConfigurationItem(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {

                if (MarcFileformat.PREFS_MARC_INTERNAL_METADATA_NAME.equalsIgnoreCase(n.getNodeName())) {
                    groupName = MarcFileformat.readTextNode(n);
                } else if (MarcFileformat.PREFS_MARC_MAIN_TAG.equalsIgnoreCase(n.getNodeName())) {
                    fieldMainTag = MarcFileformat.readTextNode(n);
                } else if (MarcFileformat.PREFS_MARC_INDICATOR_1.equalsIgnoreCase(n.getNodeName())) {
                    fieldInd1 = MarcFileformat.readTextNode(n);
                } else if (MarcFileformat.PREFS_MARC_INDICATOR_2.equalsIgnoreCase(n.getNodeName())) {
                    fieldInd2 = MarcFileformat.readTextNode(n);
                } else if (MarcFileformat.PREFS_MARC_METADATA_NAME.equalsIgnoreCase(n.getNodeName())) {
                    MetadataConfigurationItem metadata = new MetadataConfigurationItem(n);
                    metadataList.add(metadata);
                } else if (MarcFileformat.PREFS_MARC_PERSON_NAME.equalsIgnoreCase(n.getNodeName())) {
                    MetadataConfigurationItem metadata = new MetadataConfigurationItem(n);
                    personList.add(metadata);
                } else if (MarcFileformat.PREFS_MARC_CORPORATE_NAME.equalsIgnoreCase(n.getNodeName())) {
                    MetadataConfigurationItem metadata = new MetadataConfigurationItem(n);
                    corporationList.add(metadata);
                }
            }
        }

        for (MetadataConfigurationItem item : metadataList) {
            for (MarcField field : item.getFieldList()) {
                updateField(field);
            }
        }
        for (MetadataConfigurationItem item : personList) {
            for (MarcField field : item.getFieldList()) {
                updateField(field);
            }
        }
        for (MetadataConfigurationItem item : corporationList) {
            for (MarcField field : item.getFieldList()) {
                updateField(field);
            }
        }

    }

    private void updateField(MarcField field) {
        field.setFieldMainTag(fieldMainTag);
        field.setFieldInd1(fieldInd1);
        field.setFieldInd2(fieldInd2);
    }

}
