package de.intranda.ugh.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ugh.dl.Corporate;
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
        Assert.assertNotEquals(datafieldNodes.getLength(), 0);

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
     * @verifies import correct value for multiple subfields and condition on the same subfield
     */
    @Test
    public void parseMetadata_hu() throws Exception {

        Document doc = loadMarcDocument("resources/test/hu/BV047509647.xml");
        Assert.assertNotNull(doc);

        List<Node> datafields = getDatafields(doc, null);

        Prefs prefs = new Prefs();
        Assert.assertTrue(prefs.loadPrefs("resources/test/hu/HU-monographie.xml"));
        MarcFileformat mfc = new MarcFileformat(prefs);

        List<Metadata> metadataList = mfc.parseMetadata(datafields, mfc.metadataList);
        Assert.assertFalse(metadataList.isEmpty());

        List<Metadata> subjects = metadataList.stream().filter(md -> "Subject".equals(md.getType().getName())).collect(Collectors.toList());
        Assert.assertEquals(2, subjects.size());
        Assert.assertEquals("Literatur und Sprachen#Deutsche Sprache und Literatur", subjects.get(0).getValue());
        Assert.assertEquals("Geschichte", subjects.get(1).getValue());

        //        List<Metadata> format = metadataList.stream().filter(md -> md.getType().getName().equals("FormatSourcePrint")).collect(Collectors.toList());
        //        Assert.assertEquals(1, format.size());
        //        Assert.assertEquals("96 Seiten", format.get(0).getValue());

        List<Metadata> lang = metadataList.stream().filter(md -> "DocLanguage".equals(md.getType().getName())).collect(Collectors.toList());
        Assert.assertEquals(1, lang.size());
        Assert.assertEquals("ger", lang.get(0).getValue());

        List<Person> personList = mfc.parsePersons(datafields, mfc.personList);
        Assert.assertEquals(1, personList.size());
        Assert.assertEquals("116144742", personList.get(0).getAuthorityValue());
        Assert.assertEquals("Bodo", personList.get(0).getFirstname());
        Assert.assertEquals("Wildberg", personList.get(0).getLastname());
    }

    @Test
    public void parseMetadata_hu_corporate() throws Exception {

        Document doc = loadMarcDocument("resources/test/hu/BV047511788.xml");
        Assert.assertNotNull(doc);

        List<Node> datafields = getDatafields(doc, null);

        Prefs prefs = new Prefs();
        Assert.assertTrue(prefs.loadPrefs("resources/test/hu/HU-monographie.xml"));
        MarcFileformat mfc = new MarcFileformat(prefs);

        List<Metadata> metadataList = mfc.parseMetadata(datafields, mfc.metadataList);
        Assert.assertFalse(metadataList.isEmpty());

        List<Corporate> corporateList = mfc.parseCorporations(datafields, mfc.corporationList);
        Assert.assertEquals(2, corporateList.size());
        Assert.assertEquals("5023117-0", corporateList.get(0).getAuthorityValue());
        Assert.assertEquals("Verband der Privat-Theater-Vereine Deutschlands", corporateList.get(0).getMainName());
        Assert.assertEquals("5023117-1", corporateList.get(1).getAuthorityValue());
        Assert.assertEquals("Verband 2", corporateList.get(1).getMainName());
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

    @Test
    public void parseMetadata_shouldImportCombinedTitleCorrectly() throws Exception {
        Document doc = loadMarcDocument("resources/test/1717559573.xml");
        Assert.assertNotNull(doc);

        List<Node> datafields = getDatafields(doc, "");

        Prefs prefs = new Prefs();
        Assert.assertTrue(prefs.loadPrefs("resources/test/ruleset.xml"));
        MarcFileformat mfc = new MarcFileformat(prefs);

        List<Metadata> metadataList = mfc.parseMetadata(datafields, mfc.metadataList);
        Assert.assertFalse(metadataList.isEmpty());
        Metadata title = null;
        for (Metadata md : metadataList) {
            if (md.getType().getName().contains("TitleDocMain")) {
                title = md;
            }
        }
        assertNotNull(title);
        Assert.assertEquals("Itt, Paul; Blatt 2", title.getValue());
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

    @Test
    public void parseCorporation() throws Exception {
        Document doc = loadMarcDocument("resources/test/corporation.xml");
        Assert.assertNotNull(doc);
        List<Node> datafields = getDatafields(doc, null);
        Prefs prefs = new Prefs();
        Assert.assertTrue(prefs.loadPrefs("resources/test/ruleset.xml"));
        MarcFileformat mfc = new MarcFileformat(prefs);

        List<Corporate> cl = mfc.parseCorporations(datafields, mfc.corporationList);
        Assert.assertEquals(2, cl.size());
        Corporate fixture = cl.get(0);
        assertEquals("Georg-August-Universität Göttingen", fixture.getMainName());
        assertEquals("2024315-7", fixture.getAuthorityValue());

        fixture = cl.get(1);
        assertEquals("Catholic Church.", fixture.getMainName());
        assertEquals("Province of Baltimore (Md.).", fixture.getSubNames().get(0).getValue());
        assertEquals("Provincial Council", fixture.getSubNames().get(1).getValue());
        assertEquals("1869; 10th", fixture.getPartName());
    }

}