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

import de.intranda.ugh.extension.util.DocstructConfigurationItem;
import de.intranda.ugh.extension.util.GroupConfigurationItem;
import de.intranda.ugh.extension.util.MarcField;
import de.intranda.ugh.extension.util.MetadataConfigurationItem;
import ugh.dl.Corporate;
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
    public static final String PREFS_MARC_ABORT_AFTER_MATCH = "abortAfterFirstMatch";

    public static final String PREFS_MARC_LEADER_6 = "leader6";
    public static final String PREFS_MARC_LEADER_7 = "leader7";
    public static final String PREFS_MARC_LEADER_19 = "leader19";
    public static final String PREFS_MARC_CONTROLFIELD_007_0 = "field007_0";
    public static final String PREFS_MARC_CONTROLFIELD_007_1 = "field007_1";
    public static final String PREFS_MARC_CONTROLFIELD_008_21 = "field008_21";

    private Prefs prefs;
    private Perl5Util perlUtil;
    private DigitalDocument digDoc = new DigitalDocument();

    protected List<MetadataConfigurationItem> metadataList = new LinkedList<>();
    protected List<MetadataConfigurationItem> personList = new LinkedList<>();
    protected List<MetadataConfigurationItem> corporationList = new LinkedList<>();
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
                } else if (n.getNodeName().equalsIgnoreCase("Corporate")) {
                    MetadataConfigurationItem metadata = new MetadataConfigurationItem(n);
                    corporationList.add(metadata);
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
        }
        //        catch (MetadataTypeNotAllowedException e) {
        //            String message = "Can't add child to parent DocStruct! Child type must not be null";
        //            throw new ReadException(message, e);
        //        }
        return true;
    }

    private DocStruct parseMarcRecord(Node inNode) {

        DocStruct ds = null;

        // Get all subfields.
        NodeList nl = inNode.getChildNodes();
        List<Node> controlfields = new ArrayList<>();
        List<Node> datafields = new ArrayList<>();
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

        List<Corporate> allCorp = parseCorporations(datafields, corporationList);

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
        if (allCorp != null) {
            for (Corporate corp : allCorp) {
                try {
                    ds.addCorporate(corp);
                } catch (MetadataTypeNotAllowedException e) {
                    String message = "Ignoring MetadataTypeNotAllowedException at OPAC import!";
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

    /**
     * 
     * @param datafields
     * @return
     */
    private List<MetadataGroup> parseGroups(List<Node> datafields) {
        List<MetadataGroup> groups = new ArrayList<>(groupList.size());
        for (GroupConfigurationItem gci : groupList) {
            List<Metadata> mList = new ArrayList<>();
            List<Person> pList = new ArrayList<>();
            if (!gci.getMetadataList().isEmpty()) {
                mList = parseMetadata(datafields, gci.getMetadataList());
            }
            if (!gci.getPersonList().isEmpty()) {
                pList = parsePersons(datafields, gci.getPersonList());
            }
            if (mList.isEmpty() && pList.isEmpty()) {
                continue;
            }

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

                        if (StringUtils.isBlank(per.getFirstname()) && StringUtils.isBlank(per.getLastname())
                                && StringUtils.isBlank(per.getAuthorityValue())) {
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

        return groups;
    }

    List<Corporate> parseCorporations(List<Node> datafields, List<MetadataConfigurationItem> corporationList) {
        List<Corporate> corporations = new ArrayList<>();

        for (MetadataConfigurationItem mmi : corporationList) {
            String singleMainName = null;
            List<String> singleSubNames = new ArrayList<>();
            String singlePartName = null;
            String singleIdentifier = null;

            for (Node node : datafields) {
                NamedNodeMap nnm = node.getAttributes();
                Node tagNode = nnm.getNamedItem("tag");
                Node ind1Node = nnm.getNamedItem("ind1");
                Node ind2Node = nnm.getNamedItem("ind2");
                String ind1Value = ind1Node.getNodeValue().trim();
                String ind2Value = ind2Node.getNodeValue().trim();

                Boolean matches = null;

                // Match main tag in the config
                for (MarcField mf : mmi.getFieldList()) {
                    if (!mf.getFieldMainTag().equals(tagNode.getNodeValue())) {
                        continue;
                    }

                    boolean matchesInd1 = false;
                    boolean matchesInd2 = false;
                    if (mf.getFieldInd1().equals("any") || mf.getFieldInd1().trim().equals(ind1Value)) {
                        matchesInd1 = true;
                    }
                    if (mf.getFieldInd2().equals("any") || mf.getFieldInd2().trim().equals(ind2Value)) {
                        matchesInd2 = true;
                    }
                    if (!matchesInd1 || !matchesInd2) {
                        continue;
                    }

                    String currentIdentifier = "";
                    String currentMainName = "";
                    List<String> currentSubNames = new ArrayList<>();
                    String currentPartName = "";

                    NodeList subfieldList = node.getChildNodes();
                    for (int i = 0; i < subfieldList.getLength(); i++) {
                        Node subfield = subfieldList.item(i);
                        if (subfield.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        NamedNodeMap attributes = subfield.getAttributes();
                        Node code = attributes.getNamedItem("code");

                        // Skip values that don't match the field condition
                        if (StringUtils.isNotBlank(mmi.getConditionField()) && StringUtils.isNotBlank(mmi.getConditionValue())
                                && mmi.getConditionField().equals(code.getNodeValue())) {
                            String valueToCheck = readTextNode(subfield);
                            if (!perlUtil.match(mmi.getConditionValue(), valueToCheck)
                                    && !(mmi.getConditionValue().equals("/empty/") && StringUtils.isBlank(valueToCheck))) {
                                if (matches == null) {
                                    // Only set matches = false if not previously set to true by a matching subfield
                                    matches = false;
                                }
                                continue; // If condition field == value field, make sure non-matching values don't proceed
                            }
                            matches = true;
                        }

                        // Identifier
                        if (StringUtils.isNotBlank(mmi.getIdentifierField()) && mmi.getIdentifierField().equals(code.getNodeValue())) {
                            String localIdentifier = readTextNode(subfield);
                            if (StringUtils.isBlank(mmi.getIdentifierConditionField())
                                    || perlUtil.match(mmi.getIdentifierConditionField(), localIdentifier)) {

                                if (StringUtils.isNotBlank(mmi.getIdentifierReplacement())) {
                                    localIdentifier = perlUtil.substitute(mmi.getIdentifierReplacement(), localIdentifier);
                                }
                                currentIdentifier = localIdentifier;
                            }
                        }

                        if (!mf.getMainName().isEmpty()) {
                            for (String subfieldCode : mf.getMainName()) {
                                if (subfieldCode.equals(code.getNodeValue())) {
                                    if (StringUtils.isBlank(currentMainName)) {
                                        currentMainName = readTextNode(subfield);
                                    }
                                }
                            }
                        }
                        if (!mf.getSubName().isEmpty()) {
                            for (String subfieldCode : mf.getSubName()) {
                                if (subfieldCode.equals(code.getNodeValue())) {
                                    currentSubNames.add(readTextNode(subfield));
                                }

                            }
                        }
                        if (!mf.getPartName().isEmpty()) {
                            for (String subfieldCode : mf.getPartName()) {
                                if (subfieldCode.equals(code.getNodeValue())) {
                                    if (StringUtils.isBlank(currentPartName)) {
                                        currentPartName = readTextNode(subfield);
                                    } else {
                                        currentPartName = currentPartName + mmi.getSeparator() + readTextNode(subfield);
                                    }
                                }
                            }
                        }

                        // In case a non-empty condition is configured but no condition field was found, skip this value
                        if (StringUtils.isNotBlank(mmi.getConditionField()) && StringUtils.isNotBlank(mmi.getConditionValue())
                                && !mmi.getConditionValue().equals("/empty/") && matches == null) {
                            matches = false;
                        }

                        if (matches == null || matches) {
                            if (mmi.isSeparateEntries()) {
                                // Create separate entity
                                Corporate corporate = createCorporation(mmi, currentMainName, currentSubNames, currentPartName, currentIdentifier);
                                if (corporate != null) {
                                    corporations.add(corporate);
                                }
                            } else if (StringUtils.isNotBlank(currentMainName)) {
                                if (StringUtils.isNotBlank(singleMainName)) {
                                    singleMainName = singleMainName + mmi.getSeparator() + currentMainName;
                                } else {
                                    singleMainName = currentMainName;
                                }
                                if (!currentSubNames.isEmpty()) {
                                    singleSubNames.addAll(currentSubNames);
                                }
                                if (StringUtils.isNotBlank(currentPartName)) {
                                    if (StringUtils.isNotBlank(currentPartName)) {
                                        singlePartName = currentPartName + mmi.getSeparator() + currentPartName;
                                    } else {
                                        singlePartName = currentPartName;
                                    }
                                }
                                if (StringUtils.isNotBlank(currentIdentifier)) {
                                    singleIdentifier = currentIdentifier;
                                }
                            }

                        }
                    }
                }

                // Single entity for all occurrences
                if (!mmi.isSeparateEntries()) {
                    Corporate md = createCorporation(mmi, singleMainName, singleSubNames, singlePartName, singleIdentifier);

                    if (md != null) {
                        corporations.add(md);
                    }
                }

            }
        }

        return corporations;
    }

    /**
     * 
     * @param datafields
     * @param personList
     * @return
     * @should import person roles correctly
     * @should only import one role per person
     */
    List<Person> parsePersons(List<Node> datafields, List<MetadataConfigurationItem> personList) {
        List<Person> persons = new ArrayList<>();

        for (MetadataConfigurationItem mmo : personList) {
            String singleEntityLastName = "";
            String singleEntityFirstName = "";
            String singleEntityIdentifier = "";

            // For each node in the MARC document
            for (Node node : datafields) {
                NamedNodeMap nnm = node.getAttributes();
                Node tagNode = nnm.getNamedItem("tag");
                Node ind1Node = nnm.getNamedItem("ind1");
                Node ind2Node = nnm.getNamedItem("ind2");
                String ind1Value = ind1Node.getNodeValue().trim();
                String ind2Value = ind2Node.getNodeValue().trim();

                Boolean matches = null;
                String currentLastName = "";
                String currentFirstName = "";
                String currentIdentifier = "";

                // Match main tag in the config
                for (MarcField mf : mmo.getFieldList()) {
                    if (!mf.getFieldMainTag().equals(tagNode.getNodeValue())) {
                        continue;
                    }

                    boolean matchesInd1 = false;
                    boolean matchesInd2 = false;
                    if (mf.getFieldInd1().equals("any") || mf.getFieldInd1().trim().equals(ind1Value)) {
                        matchesInd1 = true;
                    }
                    if (mf.getFieldInd2().equals("any") || mf.getFieldInd2().trim().equals(ind2Value)) {
                        matchesInd2 = true;
                    }
                    if (!matchesInd1 || !matchesInd2) {
                        continue;
                    }

                    // Expansion
                    NodeList subfieldList = node.getChildNodes();
                    for (int i = 0; i < subfieldList.getLength(); i++) {
                        Node subfield = subfieldList.item(i);
                        if (subfield.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        NamedNodeMap attributes = subfield.getAttributes();
                        Node code = attributes.getNamedItem("code");

                        // Skip values that don't match the field condition
                        if (StringUtils.isNotBlank(mmo.getConditionField()) && StringUtils.isNotBlank(mmo.getConditionValue())
                                && mmo.getConditionField().equals(code.getNodeValue())) {
                            String valueToCheck = readTextNode(subfield);
                            if (!perlUtil.match(mmo.getConditionValue(), valueToCheck)
                                    && !(mmo.getConditionValue().equals("/empty/") && StringUtils.isBlank(valueToCheck))) {
                                if (matches == null) {
                                    // Only set matches = false if not previously set to true by a matching subfield
                                    matches = false;
                                }
                                continue; // If condition field == value field, make sure non-matching values don't proceed
                            }
                            matches = true;
                        }

                        // Identifier
                        if (StringUtils.isNotBlank(mmo.getIdentifierField()) && mmo.getIdentifierField().equals(code.getNodeValue())) {
                            String localIdentifier = readTextNode(subfield);
                            if (StringUtils.isBlank(mmo.getIdentifierConditionField())
                                    || perlUtil.match(mmo.getIdentifierConditionField(), localIdentifier)) {

                                if (StringUtils.isNotBlank(mmo.getIdentifierReplacement())) {
                                    localIdentifier = perlUtil.substitute(mmo.getIdentifierReplacement(), localIdentifier);
                                }
                                currentIdentifier = localIdentifier;
                            }
                        }

                        if (!mf.getExpansion().isEmpty() && mf.getExpansion().get(0).equals(code.getNodeValue())) {
                            String expansion = readTextNode(subfield);

                            switch (ind1Value) {
                                case "1":
                                    // lastname, firstname
                                    if (expansion.contains(",")) {
                                        currentLastName = expansion.substring(0, expansion.indexOf(",")).trim();
                                        currentFirstName = expansion.substring(expansion.indexOf(",") + 1).trim();
                                    } else {
                                        currentLastName = expansion;
                                    }
                                    break;
                                case "0":
                                    // firstname
                                    currentFirstName = expansion;
                                    break;
                                case "2": // hack for corporations configured as persons
                                case "3":
                                    // lastname
                                    currentLastName = expansion;
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    // Only check first/last name fields if no values have been found in expansion, or if it is configured
                    if ((currentFirstName.isEmpty() && currentLastName.isEmpty()) || !mmo.isAbortAfterFirstMatch()) {
                        for (int i = 0; i < subfieldList.getLength(); i++) {
                            Node subfield = subfieldList.item(i);
                            if (subfield.getNodeType() != Node.ELEMENT_NODE) {
                                continue;
                            }

                            NamedNodeMap attributes = subfield.getAttributes();
                            Node code = attributes.getNamedItem("code");

                            if (!mf.getFirstname().isEmpty()) {
                                for (String nodeName : mf.getFirstname()) {
                                    if (nodeName.equals(code.getNodeValue())) {
                                        if (StringUtils.isNotBlank(currentFirstName)) {
                                            currentFirstName += mmo.getSeparator();
                                        }
                                        currentFirstName += readTextNode(subfield);
                                        break;
                                    }
                                }
                            }
                            if (!mf.getLastname().isEmpty()) {
                                for (String nodeName : mf.getLastname()) {
                                    if (nodeName.equals(code.getNodeValue())) {
                                        if (StringUtils.isNotBlank(currentLastName)) {
                                            currentLastName += mmo.getSeparator();
                                        }
                                        currentLastName += readTextNode(subfield);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                // In case a non-empty condition is configured but no condition field was found, skip this value
                if (StringUtils.isNotBlank(mmo.getConditionField()) && StringUtils.isNotBlank(mmo.getConditionValue())
                        && !mmo.getConditionValue().equals("/empty/") && matches == null) {
                    matches = false;
                }

                if (matches == null || matches) {
                    if (mmo.isSeparateEntries()) {
                        // Create separate entity person
                        Person md = createPerson(mmo, currentFirstName, currentLastName, currentIdentifier);
                        if (md != null) {
                            persons.add(md);
                        }
                    } else if (!currentLastName.isEmpty() || !currentFirstName.isEmpty()) {
                        if (StringUtils.isNotBlank(singleEntityLastName)) {
                            singleEntityLastName = singleEntityLastName + mmo.getSeparator() + currentLastName;
                        } else {
                            singleEntityLastName = currentLastName;
                        }
                        if (StringUtils.isNotBlank(singleEntityFirstName)) {
                            singleEntityFirstName = singleEntityFirstName + mmo.getSeparator() + currentFirstName;
                        } else {
                            singleEntityFirstName = currentFirstName;
                        }
                        if (StringUtils.isNotBlank(currentIdentifier)) {
                            singleEntityIdentifier = currentIdentifier;
                        }
                    }
                }
            }

            // Single entity for all occurrences
            if (!mmo.isSeparateEntries()) {
                Person md = createPerson(mmo, singleEntityFirstName, singleEntityLastName, singleEntityIdentifier);
                if (md != null) {
                    persons.add(md);
                }
            }
        }

        return persons;
    }

    /**
     * 
     * @param datafields
     * @param metadataList
     * @return
     * @should import correct value for multiple subfields and condition on the same subfield
     * @should only import one role per corporation
     * @should import multiple values correctly
     */
    List<Metadata> parseMetadata(List<Node> datafields, List<MetadataConfigurationItem> metadataList) {
        List<Metadata> metadata = new ArrayList<>();

        for (MetadataConfigurationItem mmo : metadataList) {
            String singleEntityValue = "";
            String singleEntityIdentifier = "";
            List<String> matchedValueList = new ArrayList<>();

            // For each node in the MARC document
            for (Node node : datafields) {
                NamedNodeMap nnm = node.getAttributes();
                Node tagNode = nnm.getNamedItem("tag");
                Node ind1Node = nnm.getNamedItem("ind1");
                Node ind2Node = nnm.getNamedItem("ind2");
                logger.debug(tagNode.getNodeName());

                Boolean matches = null;

                // Match main tag in the config
                for (MarcField mf : mmo.getFieldList()) {
                    if (!mf.getFieldMainTag().equals(tagNode.getNodeValue())) {
                        continue;
                    }

                    boolean matchesInd1 = false;
                    boolean matchesInd2 = false;
                    if (mf.getFieldInd1().equals("any") || mf.getFieldInd1().trim().equals(ind1Node.getNodeValue().trim())) {
                        matchesInd1 = true;
                    }
                    if (mf.getFieldInd2().equals("any") || mf.getFieldInd2().trim().equals(ind2Node.getNodeValue().trim())) {
                        matchesInd2 = true;
                    }
                    if (!matchesInd1 || !matchesInd2) {
                        continue;
                    }

                    String currentIdentifier = "";
                    List<String> currentValue = new ArrayList<>();
                    // Subfields
                    NodeList subfieldList = node.getChildNodes();
                    for (int i = 0; i < subfieldList.getLength(); i++) {
                        Node subfield = subfieldList.item(i);
                        if (subfield.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        NamedNodeMap attributes = subfield.getAttributes();
                        Node code = attributes.getNamedItem("code");

                        // If a condition is configured, make sure at least one subfield value meets it
                        if (StringUtils.isNotBlank(mmo.getConditionField()) && StringUtils.isNotBlank(mmo.getConditionValue())
                                && mmo.getConditionField().equals(code.getNodeValue())) {
                            String valueToCheck = readTextNode(subfield);
                            if (!perlUtil.match(mmo.getConditionValue(), valueToCheck)
                                    && !(mmo.getConditionValue().equals("/empty/") && StringUtils.isBlank(valueToCheck))) {
                                if (matches == null) {
                                    // Only set matches = false if not previously set to true by a matching subfield
                                    matches = false;
                                }
                                continue; // If condition field == value field, make sure non-matching values don't proceed
                            }
                            matches = true;
                        }

                        if (mf.getFieldSubTag().equals(code.getNodeValue())) {
                            currentValue.add(readTextNode(subfield));
                        }

                        if (StringUtils.isNotBlank(mmo.getIdentifierField()) && mmo.getIdentifierField().equals(code.getNodeValue())) {
                            String localIdentifier = readTextNode(subfield);
                            if (StringUtils.isBlank(mmo.getIdentifierConditionField())
                                    || perlUtil.match(mmo.getIdentifierConditionField(), localIdentifier)) {
                                if (!StringUtils.isBlank(mmo.getIdentifierReplacement())) {
                                    localIdentifier = perlUtil.substitute(mmo.getIdentifierReplacement(), localIdentifier);
                                }
                                currentIdentifier = localIdentifier;
                            }
                        }

                        // In case a non-empty condition is configured but no condition field was found, skip this value
                        if (StringUtils.isNotBlank(mmo.getConditionField()) && StringUtils.isNotBlank(mmo.getConditionValue())
                                && !mmo.getConditionValue().equals("/empty/") && matches == null) {
                            matches = false;
                        }
                    }
                    if (matches == null || matches) {

                        if (mmo.isSeparateEntries()) {
                            // Separate metadata values
                            for (String val : currentValue) {
                                Metadata md = createMetadata(mmo, val, currentIdentifier);
                                if (md != null) {
                                    metadata.add(md);
                                }
                            }
                        } else {
                            matchedValueList.addAll(currentValue);
                            if (StringUtils.isNotBlank(currentIdentifier)) {
                                singleEntityIdentifier = currentIdentifier;
                            }
                        }
                    }

                }
            }

            // Single entity for all occurrences
            if (!mmo.isSeparateEntries()) {
                // Concatenated value
                StringBuilder sb = new StringBuilder();
                for (String val : matchedValueList) {
                    if (sb.length() > 0) {
                        sb.append(mmo.getSeparator());
                    }
                    sb.append(val);
                }
                singleEntityValue = sb.toString();

                Metadata md = createMetadata(mmo, singleEntityValue, singleEntityIdentifier);
                if (md != null) {
                    metadata.add(md);
                }
            }
        }

        return metadata;
    }

    private Metadata createMetadata(MetadataConfigurationItem mmo, String value, String identifier) {
        Metadata md = null;

        if (!value.isEmpty()) {
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
        return md;
    }

    /**
     * 
     * @param mmo
     * @param firstname
     * @param lastname
     * @param identifier
     * @return
     */
    private Person createPerson(MetadataConfigurationItem mmo, String firstname, String lastname, String identifier) {
        Person person = null;
        if (!firstname.isEmpty() || !lastname.isEmpty()) {
            try {
                MetadataType mdt = prefs.getMetadataTypeByName(mmo.getInternalMetadataName());
                person = new Person(mdt);
                person.setRole(mdt.getName());

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
        return person;
    }

    private Corporate createCorporation(MetadataConfigurationItem mmo, String mainName, List<String> subNames, String partName, String identifier) {
        Corporate corporate = null;
        if (StringUtils.isNotBlank(mainName)) {

            try {
                MetadataType mdt = prefs.getMetadataTypeByName(mmo.getInternalMetadataName());
                corporate = new Corporate(mdt);
                corporate.setRole(mdt.getName());

                corporate.setMainName(mainName);
                corporate.setSubNames(subNames);
                corporate.setPartName(partName);

                if (!identifier.isEmpty()) {
                    // TODO alternative zu gnd
                    corporate.setAutorityFile("gnd", "http://d-nb.info/gnd/", identifier);
                }

            } catch (MetadataTypeNotAllowedException e) {
                logger.error(e);
            }
        }
        return corporate;
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
    public void setDigitalDocument(DigitalDocument inDoc) {
        this.digDoc = inDoc;
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

    @Override
    public void setGoobiID(String goobiId) {

    }
}
