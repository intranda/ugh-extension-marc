package de.intranda.ugh.extension;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ugh.dl.Metadata;
import ugh.dl.Person;
import ugh.dl.Prefs;

public class MarcFileformatTest {

    /**
     * 
     * @param marcFilePath
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    private static Document loadMarcDocument(String marcFilePath) throws SAXException, IOException, ParserConfigurationException {
        if (marcFilePath == null) {
            throw new IllegalArgumentException("marcFilePath may not be null");
        }
        File marcFile = new File(marcFilePath);
        Assert.assertTrue(marcFile.isFile());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        return dBuilder.parse(marcFile);

    }

    /**
     * 
     * @param doc
     * @param namespacePrefix
     * @return
     */
    public List<Node> getDatafields(Document doc, String namespacePrefix) {
        String field = (StringUtils.isNotEmpty(namespacePrefix) ? namespacePrefix + ":" : "") + "datafield";
        NodeList datafieldNodes = doc.getElementsByTagName(field);
        Assert.assertFalse(datafieldNodes.getLength() == 0);

        List<Node> datafields = new ArrayList<>(datafieldNodes.getLength());
        for (int i = 0; i < datafieldNodes.getLength(); i++) {
            Node n = datafieldNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equalsIgnoreCase(field)) {
                    datafields.add(n);
                }
            }
        }
        Assert.assertEquals(datafieldNodes.getLength(), datafields.size());

        return datafields;
    }

    /**
     * @see MarcFileformat#parseMetadata(List,List)
     * @verifies import correct value for multiple subfields and condition on the same subfield
     */
    @Test
    public void parseMetadata_shouldImportCorrectValueForMultipleSubfieldsAndConditionOnTheSameSubfield() throws Exception {

        Document doc = loadMarcDocument("resources/test/34220059.xml");
        Assert.assertNotNull(doc);

        List<Node> datafields = getDatafields(doc, null);

        Prefs prefs = new Prefs();
        Assert.assertTrue(prefs.loadPrefs("resources/test/ruleset.xml"));
        MarcFileformat mfc = new MarcFileformat(prefs);

        List<Metadata> metadataList = mfc.parseMetadata(datafields, mfc.metadataList);
        Assert.assertFalse(metadataList.isEmpty());
        for (Metadata md : metadataList) {
            if ("CatalogIDSource".equals(md.getType().getName())) {
                Assert.assertEquals("12234599", md.getValue());
                return;
            }
        }

        Assert.fail("Metadata not created");
    }

    /**
     * @see MarcFileformat#parseMetadata(List,List)
     * @verifies only import one role per corporation
     */
    @Test
    public void parseMetadata_shouldOnlyImportOneRolePerCorporation() throws Exception {
        Document doc = loadMarcDocument("resources/test/000348732.xml");
        Assert.assertNotNull(doc);

        List<Node> datafields = getDatafields(doc, "marc");

        Prefs prefs = new Prefs();
        Assert.assertTrue(prefs.loadPrefs("resources/test/ruleset.xml"));
        MarcFileformat mfc = new MarcFileformat(prefs);

        List<Metadata> metadataList = mfc.parseMetadata(datafields, mfc.metadataList);
        Assert.assertFalse(metadataList.isEmpty());
        int count = 0;
        Metadata corporation = null;
        for (Metadata md : metadataList) {
            if (md.getType().getName().contains("Corporat")) {
                count++;
                corporation = md;
            }
        }
        Assert.assertEquals(1, count);
        Assert.assertEquals("Massachusetts Institute of Technology.", corporation.getValue());
    }

    /**
     * @see MarcFileformat#parseMetadata(List,List)
     * @verifies import multiple values correctly
     */
    @Test
    public void parseMetadata_shouldImportMultipleValuesCorrectly() throws Exception {
        Document doc = loadMarcDocument("resources/test/211482064.xml");
        Assert.assertNotNull(doc);

        List<Node> datafields = getDatafields(doc, "");

        Prefs prefs = new Prefs();
        Assert.assertTrue(prefs.loadPrefs("resources/test/ruleset.xml"));
        MarcFileformat mfc = new MarcFileformat(prefs);

        List<Metadata> metadataList = mfc.parseMetadata(datafields, mfc.metadataList);
        Assert.assertFalse(metadataList.isEmpty());
        int count = 0;
        List<String> values = new ArrayList<>(2);
        for (Metadata md : metadataList) {
            if (md.getType().getName().contains("PlaceOfPublication")) {
                count++;
                values.add(md.getValue());
            }
        }
        Assert.assertEquals(2, count);
        Assert.assertEquals("Stuttgart", values.get(0));
        Assert.assertEquals("Tübingen", values.get(1));
    }

    /**
     * @see MarcFileformat#parsePersons(List,List)
     * @verifies import person roles correctly
     */
    @Test
    public void parsePersons_shouldImportPersonRolesCorrectly() throws Exception {
        Document doc = loadMarcDocument("resources/test/34220059.xml");
        Assert.assertNotNull(doc);

        List<Node> datafields = getDatafields(doc, null);

        Prefs prefs = new Prefs();
        Assert.assertTrue(prefs.loadPrefs("resources/test/ruleset.xml"));
        MarcFileformat mfc = new MarcFileformat(prefs);

        List<Person> personList = mfc.parsePersons(datafields, mfc.personList);
        Assert.assertEquals(1, personList.size());
        Person p = personList.get(0);
        Assert.assertEquals("Author", p.getType().getName());
        Assert.assertEquals("Theodor", p.getFirstname());
        Assert.assertEquals("Kutschmann", p.getLastname());
        Assert.assertEquals("115747876X", p.getAuthorityValue());
    }

    /**
     * @see MarcFileformat#parsePersons(List,List)
     * @verifies only import one role per person
     */
    @Test
    public void parsePersons_shouldOnlyImportOneRolePerPerson() throws Exception {
        Document doc = loadMarcDocument("resources/test/000348732.xml");
        Assert.assertNotNull(doc);

        List<Node> datafields = getDatafields(doc, "marc");

        Prefs prefs = new Prefs();
        Assert.assertTrue(prefs.loadPrefs("resources/test/ruleset.xml"));
        MarcFileformat mfc = new MarcFileformat(prefs);

        List<Person> personList = mfc.parsePersons(datafields, mfc.personList);
        Assert.assertEquals(1, personList.size());
        Person p = personList.get(0);
        Assert.assertEquals("Creator", p.getType().getName());
        Assert.assertEquals("Jeffrey C.", p.getFirstname());
        Assert.assertEquals("Livas", p.getLastname());
    }

    /**
     * @see MarcFileformat#parsePersons(List,List,boolean)
     * @verifies concatenate names within one person correctly
     */
    @Test
    public void parsePersons_shouldConcatenateNamesWithinOnePersonCorrectly() throws Exception {
        Document doc = loadMarcDocument("resources/test/3592722050.xml");
        Assert.assertNotNull(doc);

        List<Node> datafields = getDatafields(doc, null);

        Prefs prefs = new Prefs();
        Assert.assertTrue(prefs.loadPrefs("resources/test/ruleset.xml"));
        MarcFileformat mfc = new MarcFileformat(prefs);

        List<Person> personList = mfc.parsePersons(datafields, mfc.personList);
        Assert.assertEquals(3, personList.size());
        Person p = personList.get(0);
        Assert.assertEquals("Contributor", p.getType().getName());
        Assert.assertEquals("Anna Amalia, Sachsen-Weimar-Eisenach, Herzogin", p.getFirstname());
    }
}