package de.intranda.ugh.extension;

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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.oro.text.perl.Perl5Util;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.intranda.ugh.extension.util.GroupConfigurationItem;
import de.intranda.ugh.extension.util.MarcField;
import de.intranda.ugh.extension.util.DocstructConfigurationItem;
import de.intranda.ugh.extension.util.MetadataConfigurationItem;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.IncompletePersonObjectException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

public class MarcFileformat implements Fileformat {

    public static final String PREFS_MARC_METADATA_NAME = "Metadata";
    public static final String PREFS_MARC_DOCTSRUCT_NAME = "Docstruct";
    public static final String PREFS_MARC_PERSON_NAME = "Person";
    public static final String PREFS_MARC_GROUP_NAME = "Group";

    protected static final String MARC_PREFS_NODE_NAME_STRING = "Marc";
    protected static final String MARC_PREFS_NODE_COLLECTION_STRING = "collection";
    protected static final String MARC_PREFS_NODE_RECORDDATA_STRING = "recordData";
    protected static final String MARC_PREFS_NODE_RECORD_STRING = "record";

    public static final String PREFS_MARC_INTERNAL_METADATA_NAME = "Name";
    public static final String PREFS_MARC_FIELD_NAME = "field";

    public static final String PREFS_MARC_MAIN_TAG = "fieldMainTag";
    public static final String PREFS_MARC_SUB_TAG = "fieldSubTag";
    public static final String PREFS_MARC_INDICATOR_1 = "fieldInd1";
    public static final String PREFS_MARC_INDICATOR_2 = "fieldInd2";
    public static final String PREFS_MARC_SEPARATOR = "separator";
    public static final String PREFS_MARC_FIRSTNAME = "firstname";
    public static final String PREFS_MARC_LASTNAME = "lastname";
    public static final String PREFS_MARC_EXPANSION = "expansion";
    public static final String PREFS_MARC_IDENTIFIER = "identifierField";
    public static final String PREFS_MARC_IDENTIFIER_CONDITION = "identifierConditionField";
    public static final String PREFS_MARC_IDENTIFIER_REPLACEMENT = "identifierReplacement";
    public static final String PREFS_MARC_CONDITION_FIELD = "conditionField";
    public static final String PREFS_MARC_CONDITION_VALUE = "conditionValue";
    public static final String PREFS_MARC_VALUE_REPLACEMENT = "fieldReplacement";
    public static final String PREFS_MARC_SEPARATE_ENTRIES = "separateEntries";

    public static final String PREFS_MARC_LEADER_6 = "leader6";
    public static final String PREFS_MARC_LEADER_7 = "leader7";
    public static final String PREFS_MARC_LEADER_19 = "leader19";
    public static final String PREFS_MARC_CONTROLFIELD_007_0 = "field007_0";
    public static final String PREFS_MARC_CONTROLFIELD_007_1 = "field007_1";
    public static final String PREFS_MARC_CONTROLFIELD_008_21 = "field008_21";

    private Prefs prefs;
    private Perl5Util perlUtil;
    private DigitalDocument digDoc = new DigitalDocument();

    private List<MetadataConfigurationItem> metadataList = new LinkedList<>();
    private List<MetadataConfigurationItem> personList = new LinkedList<>();
    private List<DocstructConfigurationItem> docstructList = new LinkedList<>();

    private List<GroupConfigurationItem> groupList = new LinkedList<>();

    private static final Logger logger = Logger.getLogger(ugh.dl.DigitalDocument.class);

    public MarcFileformat(Prefs prefs) {
        this.prefs = prefs;
        perlUtil = new Perl5Util();
        Node marcNode = this.prefs.getPreferenceNode(MARC_PREFS_NODE_NAME_STRING);
        if (marcNode == null) {
            logger.error("Can't read preferences for marcxml fileformat! Node 'Marc' in XML-file not found!");
        } else {
            this.readPrefs(marcNode);
        }

    }

    private void readPrefs(Node marcNode) {

        NodeList children = marcNode.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node n = children.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equalsIgnoreCase(PREFS_MARC_METADATA_NAME)) {
                    MetadataConfigurationItem metadata = new MetadataConfigurationItem(n);
                    metadataList.add(metadata);
                } else if (n.getNodeName().equalsIgnoreCase(PREFS_MARC_PERSON_NAME)) {
                    MetadataConfigurationItem metadata = new MetadataConfigurationItem(n);
                    personList.add(metadata);
                } else if (n.getNodeName().equalsIgnoreCase(PREFS_MARC_DOCTSRUCT_NAME)) {
                    DocstructConfigurationItem docstruct = new DocstructConfigurationItem(n);
                    docstructList.add(docstruct);
                } else if (n.getNodeName().equalsIgnoreCase(PREFS_MARC_GROUP_NAME)) {
                    GroupConfigurationItem item = new GroupConfigurationItem(n);
                    groupList.add(item);
                }
            }
        }

    }

    public static String readTextNode(Node inNode) {

        NodeList nl = inNode.getChildNodes();
        if (nl.getLength() > 0) {
            Node n = nl.item(0);
            return n.getNodeValue();
        }

        return null;
    }

    @Override
    public DigitalDocument getDigitalDocument() throws PreferencesException {
        return digDoc;
    }

    public boolean read(Node inNode) throws ReadException {

        DocStruct ds = null;
        DocStruct dsOld = null;

        logger.info("Parsing marcxml record");

        // DOM tree is created already - parse the the tree and find
        // picaplusresults and picaplusrecord elements.
        try {
            // There should only be <picaplusresults> element nodes.
            Node ppr = inNode;
            if (ppr.getNodeType() == Node.ELEMENT_NODE) {
                String nodename = ppr.getNodeName();
                if (nodename.equals(MARC_PREFS_NODE_COLLECTION_STRING)) {

                    // Iterate over all results.
                    NodeList marcrecords = ppr.getChildNodes();
                    for (int x = 0; x < marcrecords.getLength(); x++) {
                        Node n = marcrecords.item(x);

                        if (n.getNodeType() == Node.ELEMENT_NODE) {
                            nodename = n.getNodeName();
                            if (nodename.equals(MARC_PREFS_NODE_RECORD_STRING)) {
                                // Parse a single picaplus record.
                                ds = parseMarcRecord(n);
                                // It's the first one, so this becomes the
                                // toplogical structural entity.
                                if (ds != null) {
                                    if (dsOld == null) {
                                        this.digDoc.setLogicalDocStruct(ds);
                                    } else {
                                        dsOld.addChild(ds);
                                    }
                                    dsOld = ds;
                                    ds = null;
                                }
                            }
                        }
                    }
                } else if (nodename.equals(MARC_PREFS_NODE_RECORD_STRING)) {
                    ds = parseMarcRecord(ppr);
                    if (ds != null) {
                        this.digDoc.setLogicalDocStruct(ds);
                    }
                }
            }

        } catch (TypeNotAllowedAsChildException e) {
            // Child DocStruct could not be added to father, because of ruleset.
            String message = "Can't add child to parent DocStruct! Child type '" + ds.getType().getName() + "' not allowed for parent type";
            throw new ReadException(message, e);
        } catch (MetadataTypeNotAllowedException e) {
            String message = "Can't add child to parent DocStruct! Child type must not be null";
            throw new ReadException(message, e);
        }
        return true;
    }

    private DocStruct parseMarcRecord(Node inNode) throws MetadataTypeNotAllowedException, ReadException {

        DocStruct ds = null;

        // Get all subfields.
        NodeList nl = inNode.getChildNodes();
        List<Node> controlfields = new ArrayList<Node>();
        List<Node> datafields = new ArrayList<Node>();
        Node leader = null;
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equalsIgnoreCase("leader")) {
                    leader = n;
                } else if (n.getNodeName().equalsIgnoreCase("controlfield")) {
                    controlfields.add(n);
                } else if (n.getNodeName().equalsIgnoreCase("datafield")) {
                    datafields.add(n);
                }
            }
        }
        ds = parseDocstruct(leader, controlfields);

        if (ds == null) {
            // No DocStruct found, this is a serious problem; as I do not know
            // to where I should attach the metadata.
            logger.error("Marcxml record read, but no DocStruct found!");
            return null;
        }

        List<Metadata> metadata = parseMetadata(datafields, metadataList);
        List<Person> allPer = parsePersons(datafields, personList);
        // Contains all metadata groups.
        List<MetadataGroup> allGroups = parseGroups(datafields);

        // Add metadata to DocStruct.
        if (metadata != null) {
            for (Metadata md2 : metadata) {
                try {
                    ds.addMetadata(md2);
                } catch (MetadataTypeNotAllowedException e) {
                    String message = "Ignoring MetadataTypeNotAllowedException at OPAC import!";
                    logger.warn(message, e);
                    continue;
                } catch (DocStructHasNoTypeException e) {
                    String message = "Ignoring DocStructHasNoTypeException at OPAC import!";
                    logger.warn(message, e);
                    continue;
                }
            }
        }

        // Add persons to DocStruct.
        if (allPer != null) {
            for (Person per2 : allPer) {
                try {
                    ds.addPerson(per2);
                } catch (MetadataTypeNotAllowedException e) {
                    String message = "Ignoring MetadataTypeNotAllowedException at OPAC import!";
                    logger.warn(message, e);
                } catch (IncompletePersonObjectException e) {
                    String message = "Ignoring IncompletePersonObjectException at OPAC import!";
                    logger.warn(message, e);
                }
            }
        }

        if (allGroups != null) {
            for (MetadataGroup mdg : allGroups) {
                try {
                    ds.addMetadataGroup(mdg);
                } catch (MetadataTypeNotAllowedException e) {
                    String message = "Ignoring MetadataTypeNotAllowedException at OPAC import!";
                    logger.warn(message, e);
                }
            }
        }

        return ds;
    }

    private List<MetadataGroup> parseGroups(List<Node> datafields) {
        List<MetadataGroup> groups = new ArrayList<>();
        for (GroupConfigurationItem gci : groupList) {
            List<Metadata> mList = new ArrayList<>();
            List<Person> pList = new ArrayList<>();
            if (!gci.getMetadataList().isEmpty()) {
                mList = parseMetadata(datafields, gci.getMetadataList());
            }
            if (!gci.getPersonList().isEmpty()) {
                pList = parsePersons(datafields, gci.getPersonList());
            }
            if (!mList.isEmpty() || !pList.isEmpty()) {
                try {
                    MetadataGroup mg = new MetadataGroup(prefs.getMetadataGroupTypeByName(gci.getGroupName()));
                    for (Metadata md : mList) {

                        List<Metadata> mdl = mg.getMetadataByType(md.getType().getName());
                        boolean added = false;
                        for (Metadata metadata : mdl) {
                            if (StringUtils.isBlank(metadata.getValue()) && StringUtils.isBlank(metadata.getAuthorityValue())) {
                                added = true;
                                metadata.setValue(md.getValue());
                                metadata.setAutorityFile(md.getAuthorityID(), md.getAuthorityURI(), md.getAuthorityValue());
                            }
                        }
                        if (!added) {
                            mg.addMetadata(md);
                        }
                    }
                    for (Person p : pList) {
                        List<Person> pl = mg.getPersonByType(p.getType().getName());
                        boolean added = false;
                        for (Person per : pl) {

                            if (StringUtils.isBlank(per.getFirstname()) && StringUtils.isBlank(per.getLastname()) && StringUtils.isBlank(per
                                    .getAuthorityValue())) {
                                added = true;
                                per.setFirstname(p.getFirstname());
                                per.setLastname(p.getLastname());
                                per.setAutorityFile(p.getAuthorityID(), p.getAuthorityURI(), p.getAuthorityValue());
                            }
                        }
                        if (!added) {
                            mg.addPerson(p);
                        }
                    }
                    groups.add(mg);
                } catch (MetadataTypeNotAllowedException e) {
                    logger.error(e);
                }
            }
        }

        return groups;
    }

    private List<Person> parsePersons(List<Node> datafields, List<MetadataConfigurationItem> personList) {
        List<Person> person = new ArrayList<>();

        for (MetadataConfigurationItem mmo : personList) {
            for (Node node : datafields) {
                String firstname = "";
                String lastname = "";
                String expansion = "";

                String identifier = "";
                String condition = "";
                NamedNodeMap nnm = node.getAttributes();
                Node tagNode = nnm.getNamedItem("tag");
                Node ind1Node = nnm.getNamedItem("ind1");
                Node ind2Node = nnm.getNamedItem("ind2");

                for (MarcField mf : mmo.getFieldList()) {
                    if (mf.getFieldMainTag().equals(tagNode.getNodeValue())) {
                        boolean matchesInd1 = false;
                        boolean matchesInd2 = false;
                        if (mf.getFieldInd1().equals("any") || mf.getFieldInd1().trim().equals(ind1Node.getNodeValue().trim())) {
                            matchesInd1 = true;
                        }
                        if (mf.getFieldInd2().equals("any") || mf.getFieldInd2().trim().equals(ind2Node.getNodeValue().trim())) {
                            matchesInd2 = true;
                        }
                        if (matchesInd1 && matchesInd2) {
                            NodeList subfieldList = node.getChildNodes();
                            for (int i = 0; i < subfieldList.getLength(); i++) {
                                Node subfield = subfieldList.item(i);
                                if (subfield.getNodeType() == Node.ELEMENT_NODE) {
                                    NamedNodeMap attributes = subfield.getAttributes();
                                    Node code = attributes.getNamedItem("code");

                                    if (!mf.getFirstname().isEmpty()) {
                                        for (String nodeName : mf.getFirstname()) {
                                            if (nodeName.equals(code.getNodeValue())) {
                                                if (firstname.isEmpty()) {
                                                    firstname = readTextNode(subfield);
                                                } else {
                                                    if (mmo.isSeparateEntries()) {
                                                        // create element 
                                                        Person md = createPerson(mmo, firstname, lastname, expansion, identifier, condition);
                                                        if (md != null) {
                                                            person.add(md);
                                                        }
                                                        firstname = readTextNode(subfield);
                                                    } else {
                                                        firstname = firstname + mmo.getSeparator() + readTextNode(subfield);
                                                    }
                                                }

                                            }
                                        }
                                    }
                                    if (!mf.getLastname().isEmpty()) {
                                        for (String nodeName : mf.getLastname()) {
                                            if (nodeName.equals(code.getNodeValue())) {
                                                if (lastname.isEmpty()) {
                                                    lastname = readTextNode(subfield);
                                                } else {
                                                    if (mmo.isSeparateEntries()) {
                                                        // create element 
                                                        Person md = createPerson(mmo, firstname, lastname, expansion, identifier, condition);
                                                        if (md != null) {
                                                            person.add(md);
                                                        }

                                                        lastname = readTextNode(subfield);
                                                    } else {
                                                        lastname = lastname + mmo.getSeparator() + readTextNode(subfield);
                                                    }
                                                }

                                            }
                                        }
                                    }

                                    if (StringUtils.isNotBlank(mmo.getIdentifierField()) && mmo.getIdentifierField().equals(code.getNodeValue())) {

                                        String currentIdentifier = readTextNode(subfield);
                                        if (StringUtils.isBlank(mmo.getIdentifierConditionField()) || perlUtil.match(mmo
                                                .getIdentifierConditionField(), currentIdentifier)) {

                                            if (StringUtils.isNotBlank(mmo.getIdentifierReplacement())) {
                                                currentIdentifier = perlUtil.substitute(mmo.getIdentifierReplacement(), currentIdentifier);
                                            }
                                            identifier = currentIdentifier;
                                        }
                                    }
                                    if (StringUtils.isNotBlank(mmo.getConditionField()) && mmo.getConditionField().equals(code.getNodeValue())) {
                                        condition = readTextNode(subfield);
                                    }
                                }
                            }

                            if (StringUtils.isBlank(lastname) && !mf.getExpansion().isEmpty()) {
                                for (int i = 0; i < subfieldList.getLength(); i++) {
                                    Node subfield = subfieldList.item(i);
                                    if (subfield.getNodeType() == Node.ELEMENT_NODE) {
                                        NamedNodeMap attributes = subfield.getAttributes();
                                        Node code = attributes.getNamedItem("code");

                                        for (String exp : mf.getExpansion()) {
                                            if (exp.equals(code.getNodeValue())) {

                                                //                                    if (StringUtils.isNotBlank(mf.getExpansion()) && mf.getExpansion().equals(code.getNodeValue())) {
                                                if (expansion.isEmpty()) {
                                                    expansion = readTextNode(subfield);
                                                } else {
                                                    if (mmo.isSeparateEntries()) {
                                                        // create element 
                                                        Person md = createPerson(mmo, firstname, lastname, expansion, identifier, condition);
                                                        if (md != null) {
                                                            person.add(md);
                                                        }

                                                        expansion = readTextNode(subfield);
                                                    } else {
                                                        expansion = expansion + mmo.getSeparator() + readTextNode(subfield);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Person md = createPerson(mmo, firstname, lastname, expansion, identifier, condition);
                if (md != null) {
                    person.add(md);
                }
            }
        }
        return person;

    }

    private List<Metadata> parseMetadata(List<Node> datafields, List<MetadataConfigurationItem> metadataList) {
        List<Metadata> metadata = new ArrayList<>();

        for (MetadataConfigurationItem mmo : metadataList) {

            String value = "";
            String identifier = "";
            String condition = "";

            for (Node node : datafields) {
                NamedNodeMap nnm = node.getAttributes();
                Node tagNode = nnm.getNamedItem("tag");
                Node ind1Node = nnm.getNamedItem("ind1");
                Node ind2Node = nnm.getNamedItem("ind2");

                for (MarcField mf : mmo.getFieldList()) {
                    if (mf.getFieldMainTag().equals(tagNode.getNodeValue())) {
                        boolean matchesInd1 = false;
                        boolean matchesInd2 = false;
                        if (mf.getFieldInd1().equals("any") || mf.getFieldInd1().trim().equals(ind1Node.getNodeValue().trim())) {
                            matchesInd1 = true;
                        }
                        if (mf.getFieldInd2().equals("any") || mf.getFieldInd2().trim().equals(ind2Node.getNodeValue().trim())) {
                            matchesInd2 = true;
                        }
                        if (matchesInd1 && matchesInd2) {
                            NodeList subfieldList = node.getChildNodes();
                            for (int i = 0; i < subfieldList.getLength(); i++) {
                                Node subfield = subfieldList.item(i);
                                if (subfield.getNodeType() == Node.ELEMENT_NODE) {
                                    NamedNodeMap attributes = subfield.getAttributes();
                                    Node code = attributes.getNamedItem("code");

                                    if (mf.getFieldSubTag().equals(code.getNodeValue())) {
                                        if (value.isEmpty()) {
                                            value = readTextNode(subfield);
                                        } else {
                                            if (mmo.isSeparateEntries()) {
                                                // create element 
                                                Metadata md = createMetadata(mmo, value, identifier, condition);
                                                if (md != null) {
                                                    metadata.add(md);
                                                }

                                                value = readTextNode(subfield);
                                            } else {
                                                value = value + mmo.getSeparator() + readTextNode(subfield);
                                            }
                                        }

                                    }

                                    if (StringUtils.isNotBlank(mmo.getIdentifierField()) && mmo.getIdentifierField().equals(code.getNodeValue())) {

                                        String currentIdentifier = readTextNode(subfield);
                                        if (StringUtils.isBlank(mmo.getIdentifierConditionField()) || perlUtil.match(mmo
                                                .getIdentifierConditionField(), currentIdentifier)) {

                                            if (!StringUtils.isBlank(mmo.getIdentifierReplacement())) {
                                                currentIdentifier = perlUtil.substitute(mmo.getIdentifierReplacement(), currentIdentifier);
                                            }
                                            identifier = currentIdentifier;
                                        }
                                    }
                                    if (StringUtils.isNotBlank(mmo.getConditionField()) && mmo.getConditionField().equals(code.getNodeValue())) {
                                        condition = readTextNode(subfield);
                                    }

                                }
                            }
                        }

                    }
                }

            }
            Metadata md = createMetadata(mmo, value, identifier, condition);
            if (md != null) {
                metadata.add(md);
            }
        }

        return metadata;
    }

    private Metadata createMetadata(MetadataConfigurationItem mmo, String value, String identifier, String condition) {
        Metadata md = null;

        if (!value.isEmpty()) {
            if (StringUtils.isBlank(mmo.getConditionValue()) || perlUtil.match(mmo.getConditionValue(), condition)) {
                try {
                    md = new Metadata(prefs.getMetadataTypeByName(mmo.getInternalMetadataName()));
                    if (StringUtils.isNotBlank(mmo.getFieldReplacement())) {
                        value = perlUtil.substitute(mmo.getFieldReplacement(), value);
                    }
                    md.setValue(value);
                    if (!identifier.isEmpty()) {
                        // TODO alternative zu gnd
                        md.setAutorityFile("gnd", "http://d-nb.info/gnd/", identifier);
                    }

                } catch (MetadataTypeNotAllowedException e) {
                    logger.error(e);
                }
            }
        }
        return md;
    }

    private Person createPerson(MetadataConfigurationItem mmo, String firstname, String lastname, String expansion, String identifier,
            String condition) {
        Person person = null;
        if (!expansion.isEmpty() || !firstname.isEmpty() || !lastname.isEmpty()) {
            if (StringUtils.isBlank(mmo.getConditionValue()) || perlUtil.match(mmo.getConditionValue(), condition)) {
                try {
                    MetadataType mdt = prefs.getMetadataTypeByName(mmo.getInternalMetadataName());
                    person = new Person(mdt);
                    person.setRole(mdt.getName());
                    if (!expansion.isEmpty()) {
                        if (expansion.contains(",")) {
                            lastname = expansion.substring(0, expansion.indexOf(",")).trim();
                            firstname = expansion.substring(expansion.indexOf(",") + 1).trim();
                        } else {
                            lastname = expansion;
                        }
                    }
                    person.setFirstname(firstname);
                    person.setLastname(lastname);

                    if (!identifier.isEmpty()) {
                        // TODO alternative zu gnd
                        person.setAutorityFile("gnd", "http://d-nb.info/gnd/", identifier);
                    }

                } catch (MetadataTypeNotAllowedException e) {
                    logger.error(e);
                }
            }
        }
        return person;
    }

    private DocStruct parseDocstruct(Node leader, List<Node> controlfields) {
        // fix for wrong leader in SWB
        String leaderValue = "";
        NodeList nl = leader.getChildNodes();
        if (nl.getLength() > 0) {
            Node n = nl.item(0);
            leaderValue = n.getNodeValue();
        }
        char[] leaderChars = leaderValue.toCharArray();
        char[] field007 = null;
        char[] field008 = null;

        for (Node node : controlfields) {
            NamedNodeMap nnm = node.getAttributes();
            Node tagNode = nnm.getNamedItem("tag");
            if (tagNode.getNodeValue().equals("007") && field007 == null) {
                field007 = readTextNode(node).toCharArray();
            } else if (tagNode.getNodeValue().equals("008") && field008 == null) {
                field008 = readTextNode(node).toCharArray();
            }
        }

        DocStruct ds = null;
        try {
            for (DocstructConfigurationItem dci : docstructList) {
                boolean match = true;
                //        field008_21
                if (StringUtils.isNotBlank(dci.getField008_21()) && (field008 == null || !(dci.getField008_21().toCharArray()[0] == field008[21]))) {
                    match = false;
                }
                //        field007_1
                if (StringUtils.isNotBlank(dci.getField007_1()) && (field007 == null || !(dci.getField007_1().toCharArray()[0] == field007[1]))) {
                    match = false;
                }
                //        field007_0

                if (StringUtils.isNotBlank(dci.getField007_0()) && (field007 == null || !(dci.getField007_0().toCharArray()[0] == field007[0]))) {
                    match = false;
                }
                // leadder 19
                if (StringUtils.isNotBlank(dci.getLeader19()) && !(dci.getLeader19().toCharArray()[0] == leaderChars[19])) {
                    match = false;
                }
                //        leaderChar6 and leaderChar7
                if (!(dci.getLeader6().toCharArray()[0] == leaderChars[6]) || !(dci.getLeader7().toCharArray()[0] == leaderChars[7])) {
                    match = false;
                }
                if (match) {
                    ds = digDoc.createDocStruct(prefs.getDocStrctTypeByName(dci.getInternalName()));
                    break;
                }

            }

        } catch (TypeNotAllowedForParentException e) {
            logger.error(e);
        }

        return ds;
    }

    @Override
    public boolean read(String filename) throws ReadException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Do not validate XML file.
        factory.setValidating(false);
        // Namespace does not matter.
        factory.setNamespaceAware(false);

        // Read file and parse it.

        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(filename));
            NodeList upperChildlist = document.getElementsByTagName(MARC_PREFS_NODE_RECORD_STRING);
            Node node = upperChildlist.item(0);

            return read(node);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error(e);
        }

        return false;
    }

    @Override
    public boolean write(String filename) throws WriteException, PreferencesException {
        return false;
    }

    @Override
    public boolean update(String filename) {
        return false;
    }

    @Override
    public boolean setDigitalDocument(DigitalDocument inDoc) {
        this.digDoc = inDoc;
        return true;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isExportable() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return "MARC";
    }

    @Override
    public void setPrefs(Prefs prefs) throws PreferencesException {
        this.prefs = prefs;

    }

    public void setGoobiID(String goobiId) {

    }
}
