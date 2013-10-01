/*
   Copyright (C) 2005-2012, by the President and Fellows of Harvard College.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   Dataverse Network - A web application to share, preserve and analyze research data.
   Developed at the Institute for Quantitative Social Science, Harvard University.
   Version 3.0.
*/
/*
 * DDI20ServiceBean.java
 *
 * Created on November 9, 2006, 4:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.dublinCore;

import edu.harvard.iq.dvn.core.study.Metadata;
import edu.harvard.iq.dvn.core.study.Study;
import edu.harvard.iq.dvn.core.study.StudyAbstract;
import edu.harvard.iq.dvn.core.study.StudyAuthor;
import edu.harvard.iq.dvn.core.study.StudyGeoBounding;
import edu.harvard.iq.dvn.core.study.StudyKeyword;
import edu.harvard.iq.dvn.core.study.StudyOtherRef;
import edu.harvard.iq.dvn.core.study.StudyProducer;
import edu.harvard.iq.dvn.core.study.StudyRelMaterial;
import edu.harvard.iq.dvn.core.study.StudyRelPublication;
import edu.harvard.iq.dvn.core.study.StudyRelStudy;
import edu.harvard.iq.dvn.core.study.StudyTopicClass;
import edu.harvard.iq.dvn.core.util.StringUtil;
import java.io.IOException;
import java.io.OutputStream;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author ekraffmiller
 */
@Stateless
public class DCServiceBean implements DCServiceLocal {

    private static final Logger logger = Logger.getLogger("edu.harvard.iq.dvn.core.dublinCore.DCServiceBean");

    /** Creates a new instance of DDI20ServiceBean */
    public DCServiceBean() {
    }

    public boolean isXmlFormat() {
        return true;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void exportStudy(Study study, OutputStream out) throws IOException {
        XMLStreamWriter xmlw = null;
        try {
            javax.xml.stream.XMLOutputFactory xmlof = javax.xml.stream.XMLOutputFactory.newInstance();
            //xmlof.setProperty("javax.xml.stream.isPrefixDefaulting", java.lang.Boolean.TRUE);
            xmlw = xmlof.createXMLStreamWriter(out);

            xmlw.writeStartDocument();
            createDC(xmlw, study);
            //  createCodeBook(xmlw,s);
            xmlw.writeEndDocument();
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new EJBException("ERROR occurred in exportStudy.", ex);
        } finally {
            try {
                if (xmlw != null) {
                    xmlw.close();
                }
            } catch (XMLStreamException ex) {
            }
        }
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void exportStudy(Study s, OutputStream os, String xpathExclude, String xpathInclude) throws IOException {
        throw new IllegalArgumentException("Partial export not supported for Dublin Core.");
    }

    /**
     *
     * @param xmlw - stream to write DublinCore XML for the study
     * @param study - get metadata from released version of the study.
     * @throws XMLStreamException
     * @throws IllegalArgumentException - if study does not have a released version.
     */
    public void createDC(XMLStreamWriter xmlw, Study study) throws XMLStreamException {
        if (study.getReleasedVersion() == null) {
            throw new IllegalArgumentException("Study does not have released version, study.id = " + study.getId());
        }
        Metadata metadata = study.getReleasedVersion().getMetadata();

        xmlw.writeStartElement("oai_dc:dc");
        xmlw.writeAttribute("xmlns:oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        xmlw.writeAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
        xmlw.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        xmlw.writeAttribute("xsi:schemaLocation", "http://www.openarchives.org/OAI/2.0/oai_dc/    http://www.openarchives.org/OAI/2.0/oai_dc.xsd");

        // Title
        xmlw.writeStartElement("dc:title");
        xmlw.writeCharacters(metadata.getTitle());
        xmlw.writeEndElement();

        // Identifier
        xmlw.writeStartElement("dc:identifier");
        xmlw.writeCharacters(study.getPersistentURL());
        xmlw.writeEndElement();

        //Creator
        for (StudyAuthor author : metadata.getStudyAuthors()) {
            xmlw.writeStartElement("dc:creator");
            xmlw.writeCharacters(author.getName());
            xmlw.writeEndElement();
        }

        //Publisher
        for (StudyProducer producer : metadata.getStudyProducers()) {
            xmlw.writeStartElement("dc:publisher");
            xmlw.writeCharacters(producer.getName());
            xmlw.writeEndElement();
        }

        // Date
        if (!StringUtil.isEmpty(metadata.getProductionDate())) {
            xmlw.writeStartElement("dc:date");
            xmlw.writeCharacters(metadata.getProductionDate());
            xmlw.writeEndElement();

        }

        //Relation              
        for (StudyRelPublication rp : metadata.getStudyRelPublications()) {
            xmlw.writeStartElement("dc:relation");
            xmlw.writeCharacters(rp.getText());
            xmlw.writeEndElement();
        }
        for (StudyRelMaterial rm : metadata.getStudyRelMaterials()) {
            xmlw.writeStartElement("dc:relation");
            xmlw.writeCharacters(rm.getText());
            xmlw.writeEndElement();
        }
        for (StudyRelStudy rs : metadata.getStudyRelStudies()) {
            xmlw.writeStartElement("dc:relation");
            xmlw.writeCharacters(rs.getText());
            xmlw.writeEndElement();
        }
        for (StudyOtherRef or : metadata.getStudyOtherRefs()) {
            xmlw.writeStartElement("dc:relation");
            xmlw.writeCharacters(or.getText());
            xmlw.writeEndElement();
        }        


        //Subject
        for (StudyKeyword keyword : metadata.getStudyKeywords()) {
            xmlw.writeStartElement("dc:subject");
            xmlw.writeCharacters(keyword.getValue());
            xmlw.writeEndElement();
        }
        for (StudyTopicClass topicClass : metadata.getStudyTopicClasses()) {
            xmlw.writeStartElement("dc:subject");
            xmlw.writeCharacters(topicClass.getValue());
            xmlw.writeEndElement();
        }

        // Description
        for (StudyAbstract studyAbstract : metadata.getStudyAbstracts()) {
            xmlw.writeStartElement("dc:description");
            xmlw.writeCharacters(studyAbstract.getText());
            xmlw.writeEndElement();
        }
        xmlw.writeStartElement("dc:description");
        xmlw.writeCharacters("Citation: " + metadata.getTextCitation());
        xmlw.writeEndElement();

        // Coverage
        writeCoverage(xmlw, metadata);

        // Type
        if (!StringUtil.isEmpty(metadata.getKindOfData())) {
            xmlw.writeStartElement("dc:type");
            xmlw.writeCharacters(metadata.getKindOfData());
            xmlw.writeEndElement();
        }

        // Source
        if (!StringUtil.isEmpty(metadata.getDataSources())) {
            xmlw.writeStartElement("dc:source");
            xmlw.writeCharacters(metadata.getDataSources());
            xmlw.writeEndElement();
        }

        // Rights
        writeRights(xmlw, metadata);

        //End root element
        xmlw.writeEndElement();


    }

    private void writeRights(XMLStreamWriter xmlw, Metadata metadata) throws XMLStreamException {
        Study study = metadata.getStudyVersion().getStudy();
        // Rights
        if (study.getOwner().isDownloadTermsOfUseEnabled() && !StringUtil.isEmpty(study.getOwner().getDownloadTermsOfUse())) {
            xmlw.writeStartElement("dc:rights");
            xmlw.writeCharacters(study.getOwner().getDownloadTermsOfUse());
            xmlw.writeEndElement();

        }
        if (!StringUtil.isEmpty(metadata.getConfidentialityDeclaration())) {
            xmlw.writeStartElement("dc:rights");
            xmlw.writeCharacters(metadata.getConfidentialityDeclaration());
            xmlw.writeEndElement();


        }
        if (!StringUtil.isEmpty(metadata.getSpecialPermissions())) {
            xmlw.writeStartElement("dc:rights");
            xmlw.writeCharacters(metadata.getSpecialPermissions());
            xmlw.writeEndElement();

        }
        if (!StringUtil.isEmpty(metadata.getRestrictions())) {
            xmlw.writeStartElement("dc:rights");
            xmlw.writeCharacters(metadata.getRestrictions());
            xmlw.writeEndElement();
        }
        if (!StringUtil.isEmpty(metadata.getContact())) {
            xmlw.writeStartElement("dc:rights");
            xmlw.writeCharacters(metadata.getContact());
            xmlw.writeEndElement();
        }
        if (!StringUtil.isEmpty(metadata.getCitationRequirements())) {
            xmlw.writeStartElement("dc:rights");
            xmlw.writeCharacters(metadata.getCitationRequirements());
            xmlw.writeEndElement();
        }
        if (!StringUtil.isEmpty(metadata.getDepositorRequirements())) {
            xmlw.writeStartElement("dc:rights");
            xmlw.writeCharacters(metadata.getDepositorRequirements());
            xmlw.writeEndElement();
        }
        if (!StringUtil.isEmpty(metadata.getConditions())) {
            xmlw.writeStartElement("dc:rights");
            xmlw.writeCharacters(metadata.getConditions());
            xmlw.writeEndElement();
        }
        if (!StringUtil.isEmpty(metadata.getDisclaimer())) {
            xmlw.writeStartElement("dc:rights");
            xmlw.writeCharacters(metadata.getDisclaimer());
            xmlw.writeEndElement();
        }
    }

    private void writeCoverage(XMLStreamWriter xmlw, Metadata metadata) throws XMLStreamException {
        // Time Period Covered
        String elementText = null;
        if (!StringUtil.isEmpty(metadata.getTimePeriodCoveredStart()) || !StringUtil.isEmpty(metadata.getTimePeriodCoveredEnd())) {
            xmlw.writeStartElement("dc:coverage");
            elementText = "Time Period Covered: ";
            if (!StringUtil.isEmpty(metadata.getTimePeriodCoveredStart())) {
                elementText += metadata.getTimePeriodCoveredStart();
            }
            if (!StringUtil.isEmpty(metadata.getTimePeriodCoveredEnd())) {
                if (!StringUtil.isEmpty(metadata.getTimePeriodCoveredStart())) {
                    elementText += " - ";
                }
                elementText += metadata.getTimePeriodCoveredEnd();
            }
            xmlw.writeCharacters(elementText);
            xmlw.writeEndElement();
        }

        // Date Of Collection
        if (!StringUtil.isEmpty(metadata.getDateOfCollectionStart()) || !StringUtil.isEmpty(metadata.getDateOfCollectionEnd())) {
            xmlw.writeStartElement("dc:coverage");
            elementText = "Date of Collection: ";
            if (!StringUtil.isEmpty(metadata.getDateOfCollectionStart())) {
                elementText += metadata.getDateOfCollectionStart();
            }
            if (!StringUtil.isEmpty(metadata.getDateOfCollectionEnd())) {
                if (!StringUtil.isEmpty(metadata.getDateOfCollectionStart())) {
                    elementText += " - ";
                }
                elementText += metadata.getDateOfCollectionEnd();
            }
            xmlw.writeCharacters(elementText);
            xmlw.writeEndElement();
        }

        //Country/Nation
        if (!StringUtil.isEmpty(metadata.getCountry())) {
            xmlw.writeStartElement("dc:coverage");
            xmlw.writeCharacters("Country/Nation: " + metadata.getCountry());
            xmlw.writeEndElement();
        }

        // Geographic Data
        if (!StringUtil.isEmpty(metadata.getGeographicCoverage())) {
            xmlw.writeStartElement("dc:coverage");
            xmlw.writeCharacters("Geographic Coverage: " + metadata.getGeographicCoverage());
            xmlw.writeEndElement();
        }
        if (!StringUtil.isEmpty(metadata.getGeographicUnit())) {
            xmlw.writeStartElement("dc:coverage");
            xmlw.writeCharacters("Geographic Unit: " + metadata.getGeographicUnit());
            xmlw.writeEndElement();
        }

        for (StudyGeoBounding geoBounding : metadata.getStudyGeoBoundings()) {
            xmlw.writeStartElement("dc:coverage");
            xmlw.writeCharacters("Geographic Bounding: " + geoBounding);
            xmlw.writeEndElement();
        }



    }
}
