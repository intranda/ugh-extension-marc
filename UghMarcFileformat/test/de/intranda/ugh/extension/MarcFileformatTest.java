package de.intranda.ugh.extension;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
     * @return
     */
    public List<Node> getDatafields(Document doc) {
        NodeList datafieldNodes = doc.getElementsByTagName("datafield");
        Assert.assertFalse(datafieldNodes.getLength() == 0);

        List<Node> datafields = new ArrayList<>(datafieldNodes.getLength());
        for (int i = 0; i < datafieldNodes.getLength(); i++) {
            Node n = datafieldNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equalsIgnoreCase("datafield")) {
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

        List<Node> datafields = getDatafields(doc);

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
     * @see MarcFileformat#parsePersons(List,List)
     * @verifies import person roles correctly
     */
    @Test
    public void parsePersons_shouldImportPersonRolesCorrectly() throws Exception {
        Document doc = loadMarcDocument("resources/test/34220059.xml");
        Assert.assertNotNull(doc);

        List<Node> datafields = getDatafields(doc);

        Prefs prefs = new Prefs();
        Assert.assertTrue(prefs.loadPrefs("resources/test/ruleset.xml"));
        MarcFileformat mfc = new MarcFileformat(prefs);

        List<Person> personList = mfc.parsePersons(datafields, mfc.personList);
        Assert.assertFalse(personList.isEmpty());
        for (Person p : personList) {
            if ("Author".equals(p.getType().getName())) {
                Assert.assertEquals("Theodor", p.getFirstname());
                Assert.assertEquals("Kutschmann", p.getLastname());
                Assert.assertEquals("115747876X", p.getAuthorityValue());
                return;
            }
        }

        Assert.fail("Metadata not created");
    }
}