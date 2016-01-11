package de.intranda.ugh.extension.util;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.intranda.ugh.extension.MarcFileformat;
import lombok.Data;

public @Data class GroupConfigurationItem {

    private String groupName;

    private List<MetadataConfigurationItem> metadataList = new LinkedList<MetadataConfigurationItem>();
    private List<MetadataConfigurationItem> personList = new LinkedList<MetadataConfigurationItem>();

    public GroupConfigurationItem(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {

                if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_INTERNAL_METADATA_NAME)) {
                    groupName = MarcFileformat.readTextNode(n);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_METADATA_NAME)) {
                    MetadataConfigurationItem metadata = new MetadataConfigurationItem(n);
                    metadataList.add(metadata);
                } else if (n.getNodeName().equalsIgnoreCase(MarcFileformat.PREFS_MARC_PERSON_NAME)) {
                    MetadataConfigurationItem metadata = new MetadataConfigurationItem(n);
                    personList.add(metadata);
                }

            }
        }
    }

}
