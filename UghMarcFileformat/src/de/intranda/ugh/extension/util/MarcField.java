package de.intranda.ugh.extension.util;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.intranda.ugh.extension.MarcFileformat;
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
import lombok.Data;

public @Data class MarcField {

    private String fieldMainTag = "";
    private List<String> fieldSubTags = new ArrayList<>();

    private List<String> firstname = new ArrayList<>();
    private List<String> lastname = new ArrayList<>();
    private List<String> expansion = new ArrayList<>();

    private List<String> mainName = new ArrayList<>();
    private List<String> subName = new ArrayList<>();
    private List<String> partName = new ArrayList<>();

    private String fieldInd1 = "any";
    private String fieldInd2 = "any";

    public MarcField(Node node) {
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_MAIN_TAG)) {
                    fieldMainTag = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_SUB_TAG)) {
                    fieldSubTags.add(MarcFileformat.readTextNode(n));
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_INDICATOR_1)) {
                    fieldInd1 = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_INDICATOR_2)) {
                    fieldInd2 = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_FIRSTNAME)) {
                    firstname.add(MarcFileformat.readTextNode(n));
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_LASTNAME)) {
                    lastname.add(MarcFileformat.readTextNode(n));
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_EXPANSION)) {
                    expansion.add(MarcFileformat.readTextNode(n));
                }

                else if (n.getNodeName().equalsIgnoreCase("fieldMainName")) {
                    mainName.add(MarcFileformat.readTextNode(n));
                } else if (n.getNodeName().equalsIgnoreCase("fieldSubName")) {
                    subName.add(MarcFileformat.readTextNode(n));
                } else if (n.getNodeName().equalsIgnoreCase("fieldPartName")) {
                    partName.add(MarcFileformat.readTextNode(n));
                }
            }
        }

    }

}
