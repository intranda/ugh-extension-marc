package de.intranda.ugh.extension.util;

import lombok.Data;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.intranda.ugh.extension.MarcFileformat;

public @Data class MarcField {

    private String fieldMainTag = "";
    private String fieldSubTag = "";

    private String firstname = "";
    private String lastname = "";
    private String expansion = "";

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
                    fieldSubTag = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_INDICATOR_1)) {
                    fieldInd1 = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_INDICATOR_2)) {
                    fieldInd2 = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_FIRSTNAME)) {
                    firstname = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_LASTNAME)) {
                    lastname = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_EXPANSION)) {
                    expansion = MarcFileformat.readTextNode(n);
               
                } 
            }
        }

    }

}
