package de.intranda.ugh.extension.util;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.intranda.ugh.extension.MarcFileformat;
import lombok.Data;

public @Data class DocstructConfigurationItem {

    private String internalName = "";
    private String leader6 = "";
    private String leader7 = "";
    private String field007_0 = "";
    private String field007_1 = "";
    private String field008_21 = "";

    public DocstructConfigurationItem(Node node) {
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_INTERNAL_METADATA_NAME)) {
                    internalName = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_LEADER_6)) {
                    leader6 = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_LEADER_7)) {
                    leader7 = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_CONTROLFIELD_007_0)) {
                    field007_0 = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_CONTROLFIELD_007_1)) {
                    field007_1 = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_CONTROLFIELD_008_21)) {
                    field008_21 = MarcFileformat.readTextNode(n);
                }
            }
        }
    }

}
