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
 * StudyUI.java
 *
 * Created on November 10, 2006, 7:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.web.study;

import edu.harvard.iq.dvn.core.admin.RoleServiceLocal;
import edu.harvard.iq.dvn.core.admin.UserGroup;
import edu.harvard.iq.dvn.core.admin.VDCRole;
import edu.harvard.iq.dvn.core.admin.VDCUser;
import edu.harvard.iq.dvn.core.ddi.DDIServiceBean;
import edu.harvard.iq.dvn.core.study.FileIdCategory;
import edu.harvard.iq.dvn.core.study.FileMetadata;
import edu.harvard.iq.dvn.core.study.Metadata;
import edu.harvard.iq.dvn.core.study.Study;
import edu.harvard.iq.dvn.core.study.StudyAbstract;
import edu.harvard.iq.dvn.core.study.StudyAuthor;
import edu.harvard.iq.dvn.core.study.StudyDistributor;
import edu.harvard.iq.dvn.core.study.StudyField;
import edu.harvard.iq.dvn.core.study.StudyFieldValue;
import edu.harvard.iq.dvn.core.study.StudyFileServiceLocal;
import edu.harvard.iq.dvn.core.study.StudyGeoBounding;
import edu.harvard.iq.dvn.core.study.StudyGrant;
import edu.harvard.iq.dvn.core.study.StudyKeyword;
import edu.harvard.iq.dvn.core.study.StudyNote;
import edu.harvard.iq.dvn.core.study.StudyOtherId;
import edu.harvard.iq.dvn.core.study.StudyOtherRef;
import edu.harvard.iq.dvn.core.study.StudyProducer;
import edu.harvard.iq.dvn.core.study.StudyRelMaterial;
import edu.harvard.iq.dvn.core.study.StudyRelPublication;
import edu.harvard.iq.dvn.core.study.StudyRelStudy;
import edu.harvard.iq.dvn.core.study.StudyServiceLocal;
import edu.harvard.iq.dvn.core.study.StudySoftware;
import edu.harvard.iq.dvn.core.study.StudyTopicClass;
import edu.harvard.iq.dvn.core.study.StudyVersion;
import edu.harvard.iq.dvn.core.web.util.DvnDate;
import edu.harvard.iq.dvn.core.util.StringUtil;
import edu.harvard.iq.dvn.core.vdc.VDC;
import edu.harvard.iq.dvn.core.web.util.EmailValidator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.event.ActionEvent;
import javax.naming.InitialContext;

/**
 *
 * @author gdurand
 *
 * Convenience class for Study and StudyVersion information needed in multiple pages.
 * The class does lazy loading of Study and StudyVersion objects.  It can be created either with a Study, studyId, StudyVersion or studyVersionId.
 * If a Study or studyId is passed to the constructor, then the StudyVersion is initialized according to the logic in initVersionForStudy()
 *
 */
public class StudyUI  implements java.io.Serializable {
    
    private Study study;
    private StudyVersion studyVersion;
    private Long studyId;
    private Long studyVersionId;
    private Map studyFields;
    private VDCUser user;
    private UserGroup ipUserGroup;

    private StudyServiceLocal studyService = null;
    private StudyFileServiceLocal studyFileService = null;

    private static DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM);
    EmailValidator emailValidator = new EmailValidator();
    
    /** Creates a new instance of StudyUI
     *
     *  Used in VDCCollectionTree
     *  this constructor does not initialize the file category ui list
     *  and is meant to be used in places where you do not need them
     *  e.g. the StudyListingPage or the CollectionTree
     */
    public StudyUI(Study s) {
        this.study = s;
        this.studyId = s.getId();
        initVersionForStudy(study);

    }
   


    /**
     *  Used in StudyListingPage
     * @param sid
     * @param studyFields
     * @param user
     * @param ipUserGroup
     */
    public StudyUI(Long sid, Map studyFields, VDCUser user, UserGroup ipUserGroup) {
        this.studyId = sid;
        this.studyFields = studyFields;
        this.user = user;
        this.ipUserGroup = ipUserGroup;
    }

    /**
     * Used in EditCollectionPage
     * @param sid
     * @param user
     * @param ipUserGroup
     * @param selected
     */
    public StudyUI(Long sid, VDCUser user, UserGroup ipUserGroup, boolean selected) {
        this.studyId = sid;
        this.user = user;
        this.ipUserGroup = ipUserGroup;        
        this.selected = selected;
    }
    /**
     * Used in EditCollectionPage
     * @param s
     * @param user
     * @param ipUserGroup
     * @param selected
     */
    public StudyUI(Study s, VDCUser user, UserGroup ipUserGroup, boolean selected) {
        this.study = s;
        this.studyId = s.getId();
        initVersionForStudy(study);
        this.user = user;
        this.ipUserGroup = ipUserGroup;        
        this.selected = selected;
    }

    /**
     * Used for  ManageStudiesList page - to determine
     * if user is authorized to Edit or change the state
     * @param studyVersionId
     * @param user
     */
    public StudyUI(Long studyVersionId, VDCUser user) {
        this.studyVersionId = studyVersionId;
        this.user = user;
    }

 
    /**
     *  Used in View Study Page - user is required to determine permissions
     */
    public StudyUI(StudyVersion sv, VDCUser u) {
        this.studyVersion = sv;
        this.studyVersionId = studyVersion.getId();
        this.study = sv.getStudy();
        this.studyId = this.study.getId();
        this.user = u;
    }


   


   
    /**
     * Used In StudyPage
     * Creates a new instance of StudyUI
     * this constructor initializes the file category ui list
     * Use this constructor if you want to set the StudyFileUI.fileRestrictedFor user value
     */
    public StudyUI(StudyVersion sv, VDCUser user, UserGroup ipUserGroup) {
        this(sv, user, ipUserGroup, false);
    }
    
    /* used in FileRequest Page
     * Loads all files for the study
     */
    public StudyUI(StudyVersion sv, VDCUser user, UserGroup ipUserGroup, boolean loadAllFiles) {
        this.studyVersion = sv;
        this.studyVersionId = studyVersion.getId();
        this.study = sv.getStudy();
        this.studyId = this.study.getId();

        this.user = user;
        this.ipUserGroup = ipUserGroup;
        initFileCategoryUIList(user, ipUserGroup, loadAllFiles);
    }    


    /**
     * Used In StudyVersionDifferencesPage
     * Creates a new instance of StudyUI used for detecting differences
     * between study versions.
     */

    public StudyUI(StudyVersion sv, VDCUser user, boolean withFiles) {
        this.studyVersion = sv;
        this.studyVersionId = studyVersion.getId();
        this.study = sv.getStudy();
        this.studyId = this.study.getId();
        this.user = user;
        if (withFiles) {
            initFileMetadataList(sv);
        }
    }


    private void initStudyService() {
        if (studyService == null) {
            try {
                studyService = (StudyServiceLocal) new InitialContext().lookup("java:comp/env/studyService");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initStudyFileService() {
        if (studyFileService == null) {
            try {
                studyFileService = (StudyFileServiceLocal) new InitialContext().lookup("java:comp/env/studyFileService");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Long getStudyId() {
        if (studyId!=null) {
            return studyId;
        } else if (studyVersionId !=null) {
            initStudyAndVersion();
            return studyVersion.getStudy().getId();
        }
        return null;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }    
    
    public Study getStudy() {
        // check to see if study is loaded or if we only have the studyId
        if (study == null) {
            initStudyAndVersion();
        }        
        return study;
    }


    private void initStudyAndVersion() {
        initStudyService();
        if (studyId!=null) {
                study = studyService.getStudyForSearch(studyId, studyFields);
                initVersionForStudy(study);


        } else if (studyVersionId!=null) {
                studyVersion = studyService.getStudyVersionById(studyVersionId);
                study = studyVersion.getStudy();
                studyId = study.getId();
        }
    }

    /**
     * Determine which StudyVersion to display if the StudyUI was initialized with a Study or studyId
     * @param study
     * @return
     */
    private void initVersionForStudy(Study study) {
        
        if (study.isReleased()) {
        studyVersion = study.getReleasedVersion();
        }
        else if (study.isDeaccessioned()) {
                studyVersion =study.getDeaccessionedVersion();
            }
            else { 
                studyVersion = study.getLatestVersion();
            }
        
        studyVersionId = studyVersion.getId();
    }

    public Metadata getMetadata() {
        return getStudyVersion().getMetadata();
    }
    


    public StudyVersion getStudyVersion() {
        if (studyVersion==null) {
                initStudyAndVersion();
        }
        return studyVersion;
    }
    
    /**
     * Return for each studyAuthor: Author (Affiliation), only if affiliation is not empty
     */
    public String getAuthors() {
        String str = "";
        for (Iterator<StudyAuthor> it = getMetadata().getStudyAuthors().iterator(); it.hasNext();) {
            StudyAuthor sa = it.next();
            if (!StringUtil.isEmpty(sa.getName())) {
                if (str != "") {
                    str += "; ";
                }
                str += sa.getName();
                
            }
        }
        return str;
        
    }
    
    public String getAuthorAffiliations() {
        String str = "";
        boolean hasAffiliation = false;
        
        for (Iterator<StudyAuthor> it = getMetadata().getStudyAuthors().iterator(); it.hasNext();) {
            StudyAuthor sa = it.next();
            if (!StringUtil.isEmpty(sa.getName())) {
                if (str != "") {
                    str += "; ";
                }
                str += sa.getName();
                
            }
            if (!StringUtil.isEmpty(sa.getAffiliation())) {
                hasAffiliation = true;
                str += " (" + sa.getAffiliation() + ")";
            }
        }
        /** commented (MC) - show authors always
         * if (!hasAffiliation) {
         *     str="";
         * }
         */
        return str;
        
    }
    
    public String getAbstracts() {
        String str = "";
        for (Iterator<StudyAbstract> it = getMetadata().getStudyAbstracts().iterator(); it.hasNext();) {
            StudyAbstract elem = it.next();
            if (!StringUtil.isEmpty(elem.getText())) {
                str += "<p>" + elem.getText() + "</p>";
            }
            
        }
        return str;
    }
    
    public String getDistributorContact() {
        String str = "";
        if (!StringUtil.isEmpty(getMetadata().getDistributorContact())) {
            str += getMetadata().getDistributorContact();
        }
        if (!StringUtil.isEmpty(getMetadata().getDistributorContactAffiliation())) {
            str += " (" + getMetadata().getDistributorContactAffiliation() + ")";
        }
        if (!StringUtil.isEmpty(getMetadata().getDistributorContactEmail())) {
            if (str != "") {
                str += ", ";
            }
            if (!getMetadata().getDistributorContactEmail().contains(",")) {
                if (emailValidator.validateEmail(getMetadata().getDistributorContactEmail())) {
                    str += "<a href='mailto:" + getMetadata().getDistributorContactEmail() + "'>" + getMetadata().getDistributorContactEmail() + "</a>";
                } else {
                    str += getMetadata().getDistributorContactEmail();
                }
            } else {
                String[] input = getMetadata().getDistributorContactEmail().split(",");
                int i = 0;
                while (i < input.length) {
                    if (i>0){
                       str += ", "; 
                    }
                    if (emailValidator.validateEmail(input[i])) {
                        str += "<a href='mailto:" + input[i] + "'>" + input[i] + "</a>";
                    } else {
                        str += input[i];
                    }
                    i++;
                }                
            }
        }
        /*"Distributor Contact (affiliation), e-mail"*/
        return str;
    }
    
    public String getSeries() {
        String str = "";
        if (!StringUtil.isEmpty(getMetadata().getSeriesName())) {
            str += getMetadata().getSeriesName();
        }
        if (!StringUtil.isEmpty(getMetadata().getSeriesInformation())) {
            if (str != "") {
                str += ", ";
            }
            str += getMetadata().getSeriesInformation();
        }
        return str;
    }
    
    public String getStudyVersionText() {
        String str = "";
        if (!StringUtil.isEmpty(getMetadata().getStudyVersionText())) {
            str += getMetadata().getStudyVersionText();
        }
        if (!StringUtil.isEmpty(getMetadata().getVersionDate())) {
            if (str != "") {
                str += ", ";
            }
            str += reformatDate(getMetadata().getVersionDate());
        }
        return str;
    }
    
    public String getTimePeriodCovered() {
        String str = "";
        if (!StringUtil.isEmpty(getMetadata().getTimePeriodCoveredStart())) {
            str += reformatDate(getMetadata().getTimePeriodCoveredStart());
        }
        if (!StringUtil.isEmpty(getMetadata().getTimePeriodCoveredEnd())) {
            if (str != "") {
                str += " - ";
            }
            str += reformatDate(getMetadata().getTimePeriodCoveredEnd());
        }
        return str;
        
    }
    
    public String getProductionDate() {
        return reformatDate(getMetadata().getProductionDate());
    }
    
    public String getDistributionDate() {
        return reformatDate(getMetadata().getDistributionDate());
    }
    
    public String getDateOfDeposit() {
        return reformatDate(getMetadata().getDateOfDeposit());
    }
    
    private String reformatDate(String dateString) {
         
        Date date = DvnDate.convertFromPattern(dateString, "yyyyyyyyy-MM-dd GG");
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("MMMMM dd, yyyy GG");
            return formatter.format(date);
        }
        date = DvnDate.convertFromPattern(dateString, "yyyyyyyyy-MM GG");
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("MMMMM, yyyy GG");
            return formatter.format(date);
        }
        date = DvnDate.convertFromPattern(dateString, "yyyyyyyyy GG");
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy GG");
            return formatter.format(date);
        }
        date = DvnDate.convertFromPattern(dateString, "yyyy-MM-dd");
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("MMMMM dd, yyyy");
            return formatter.format(date);
        }
        date = DvnDate.convertFromPattern(dateString, "yyyy-MM");
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("MMMMM, yyyy");
            return formatter.format(date);
        }

        return dateString;
        
    }
    
    public String getDateOfCollection() {
        String str = "";
        if (!StringUtil.isEmpty(getMetadata().getDateOfCollectionStart())) {
            str += reformatDate(getMetadata().getDateOfCollectionStart());
        }
        if (!StringUtil.isEmpty(getMetadata().getDateOfCollectionEnd())) {
            if (str != "") {
                str += " - ";
            }
            str += reformatDate(getMetadata().getDateOfCollectionEnd());
        }
        return str;
    }
    
    public String getTruncatedAbstracts() {
        String abstracts = getAbstracts();
        String truncatedAbstracts = StringUtil.truncateString(abstracts, 200);
        
        return truncatedAbstracts;
    }
    
    public boolean isRenderAbstractsMoreLink() {
        String abstracts = getAbstracts();
        String truncatedAbstracts = StringUtil.truncateString(abstracts, 200);
        
        return !truncatedAbstracts.equals(abstracts);
    }

    private Long numberOfFiles;
    
    public Long getNumberOfFiles(){
        if (numberOfFiles == null){
            initFileFlags();
        }
        return numberOfFiles;
    }
    
    
    private Boolean isFiles;
    private Boolean isSubsettable;
    private Boolean isVisualization;
  
    
    
    public boolean isFiles() {
        if (isFiles == null) {
            initFileFlags();
        }
    
        return isFiles.booleanValue();
    }
   
    public boolean isSubsettable() {
        if (isSubsettable == null) {
            initFileFlags();
        }

        return isSubsettable.booleanValue();
    }
    
    public boolean isVisualization() {
        if (isVisualization == null) {
            initFileFlags();
        }
        if(isSubsettable){
            isVisualization = studyFileService.doesStudyHaveSingleTabularFiles(studyVersionId);
        } else {
            isVisualization = Boolean.FALSE;
        }
        return isVisualization.booleanValue();
    }

    public boolean getHasMultipleVersions() {
        return getViewableStudyVersions().size() > 1;
    }
   
    private void initFileFlags() {
        initStudyFileService();
        numberOfFiles = studyFileService.getCountFilesByStudyVersion(getStudyVersion().getId());
        Boolean doesStudyHaveSubsettableFiles = studyFileService.doesStudyHaveSubsettableFiles(getStudyVersion().getId());
        isFiles = (doesStudyHaveSubsettableFiles != null);
        isSubsettable = (doesStudyHaveSubsettableFiles != null && doesStudyHaveSubsettableFiles);
    }

    
    public String getProducers() {
        String str = "";
        for (Iterator<StudyProducer> it = getMetadata().getStudyProducers().iterator(); it.hasNext();) {
            StudyProducer elem = it.next();
            if (!StringUtil.isEmpty(elem.getName())) {
                if (str != "") {
                    str += "; ";
                }
                if (!StringUtil.isEmpty(elem.getUrl())) {
                    str += "<a href='" + elem.getUrl() + "' target='_blank' title='" + elem.getName() + "'>" + elem.getName() + "</a>";
                } else {
                    str += elem.getName();
                }
                
            }
            if (!StringUtil.isEmpty(elem.getAbbreviation())) {
                str += " (" + elem.getAbbreviation() + ")";
            }
            if (!StringUtil.isEmpty(elem.getAffiliation())) {
                str += ", " + elem.getAffiliation();
            }
            if (!StringUtil.isEmpty(elem.getLogo())) {
                str += " <img src='" + elem.getLogo() + "' height='30px' alt='Logo' title='Logo' />";
            }
            
            
            
        }
        return str;
        
    }
    
    public String getAbstractDates() {
        String str = "";
        for (Iterator<StudyAbstract> it = getMetadata().getStudyAbstracts().iterator(); it.hasNext();) {
            StudyAbstract elem = it.next();
            if (!StringUtil.isEmpty(elem.getDate())) {
                if (str != "") {
                    str += "; ";
                }
                str += reformatDate(elem.getDate());
            }
            
        }
        return str;
        
    }
    
    public String getNotes() {
        String str = "";
        for (Iterator<StudyNote> it = getMetadata().getStudyNotes().iterator(); it.hasNext();) {
            StudyNote elem = it.next();
            if (elem.getType()==null || !elem.getType().equals(DDIServiceBean.NOTE_TYPE_TERMS_OF_USE)) {
                if (!StringUtil.isEmpty(elem.getType())) {
                    if (str != "") {
                        str += "; ";
                    }
                    str += elem.getType();
                }
                if (!StringUtil.isEmpty(elem.getSubject())) {
                    str += " (" + elem.getSubject() + ")";
                }
                if (!StringUtil.isEmpty(elem.getText())) {
                    str += " " + elem.getText();
                }
            }
            
        }
        return str;
    }
    
    public String getReplicationFor() {
        String str = "";
        for (Iterator<StudyRelPublication> it = getMetadata().getStudyRelPublications().iterator(); it.hasNext();) {
            StudyRelPublication elem = it.next();
            if (elem.isReplicationData() && !StringUtil.isEmpty(elem.getText())) {
                str = getPublication(elem);
                break;
            }
            
        }
        return str;       
    }
    
    public String getRelPublications() {
        String str = "";
        for (Iterator<StudyRelPublication> it = getMetadata().getStudyRelPublications().iterator(); it.hasNext();) {
            StudyRelPublication elem = it.next();
            if (!elem.isReplicationData() && !StringUtil.isEmpty(elem.getText())) {
                if (str != "") {
                    str += "<br/><br/>";
                }
                str += getPublication(elem);
            }
            
        }
        return str;
    }
    
    private String getPublication(StudyRelPublication publication) {
        String str = publication.getText();

        if (!StringUtil.isEmpty(publication.getIdNumber())) {                
            str += " <br/>ID: ";
            if (!StringUtil.isEmpty(publication.getIdType())) {
                str += publication.getIdType() + ":";
            }
            str += publication.getIdNumber();
        }

        if (!StringUtil.isEmpty(publication.getUrl())) {
            str += "<br/><a href='" + publication.getUrl() + "' target='_blank' title='" + publication.getUrl() + "'>Link</a>";
        }

        return str;        
    }
    
    public String getRelMaterials() {
        String str = "";
        for (Iterator<StudyRelMaterial> it = getMetadata().getStudyRelMaterials().iterator(); it.hasNext();) {
            StudyRelMaterial elem = it.next();
            if (!StringUtil.isEmpty(elem.getText())) {
                if (str != "") {
                    str += "; ";
                }
                str += elem.getText();
            }
            
        }
        return str;
    }
    
    public String getRelStudies() {
        String str = "";
        for (Iterator<StudyRelStudy> it = getMetadata().getStudyRelStudies().iterator(); it.hasNext();) {
            StudyRelStudy elem = it.next();
            if (!StringUtil.isEmpty(elem.getText())) {
                if (str != "") {
                    str += "; ";
                }
                str += elem.getText();
            }
            
        }
        return str;
    }
    
    public String getOtherRefs() {
        String str = "";
        for (Iterator<StudyOtherRef> it = getMetadata().getStudyOtherRefs().iterator(); it.hasNext();) {
            StudyOtherRef elem = it.next();
            if (!StringUtil.isEmpty(elem.getText())) {
                if (str != "") {
                    str += "; ";
                }
                str += elem.getText();
            }
            
        }
        return str;
    }
    
    public String getSoftware() {
        String str = "";
        for (Iterator<StudySoftware> it = getMetadata().getStudySoftware().iterator(); it.hasNext();) {
            StudySoftware ss = it.next();
            if (!StringUtil.isEmpty(ss.getName())) {
                if (str != "") {
                    str += "; ";
                }
                str += ss.getName();
            }
            if (!StringUtil.isEmpty(ss.getSoftwareVersion())) {
                str += ", " + ss.getSoftwareVersion();
            }
        }
        return str;
    }
    
    public String getGrants() {
        String str = "";
        for (Iterator<StudyGrant> it = getMetadata().getStudyGrants().iterator(); it.hasNext();) {
            StudyGrant elem = it.next();
            if (!StringUtil.isEmpty(elem.getNumber())) {
                if (str != "") {
                    str += "; ";
                }
                str += elem.getNumber();
                if (!StringUtil.isEmpty(elem.getAgency())) {
                    str += ", ";
                }
            }
            if (!StringUtil.isEmpty(elem.getAgency())) {
                str += elem.getAgency();
            }
            
        }
        return str;
    }
    
    public String getOtherIds() {
        String str = "";
        for (Iterator<StudyOtherId> it = getMetadata().getStudyOtherIds().iterator(); it.hasNext();) {
            StudyOtherId elem = it.next();
            if (!StringUtil.isEmpty(elem.getAgency()) || !StringUtil.isEmpty(elem.getOtherId())) {
                if (str != "") {
                    str += "; ";
                }
            }
            if (!StringUtil.isEmpty(elem.getAgency())){
                str += elem.getAgency();
                if (elem.getAgency() != null) {
                    str += ": ";
                }
            }
            if (!StringUtil.isEmpty(elem.getOtherId())) {
                str += elem.getOtherId();
            }
            
        }
        return str;
    }
    
    public String getGeographicBoundings() {
        String str = "";
        for (Iterator it = getMetadata().getStudyGeoBoundings().iterator(); it.hasNext();) {
            StudyGeoBounding elem = (StudyGeoBounding) it.next();
            String boundingStr = "";
            if (!StringUtil.isEmpty(elem.getWestLongitude())) {
                boundingStr += "West Bounding Longitude: " + elem.getWestLongitude() + " ";
            }
            if (!StringUtil.isEmpty(elem.getEastLongitude())) {
                boundingStr += "East Bounding Longitude: " + elem.getEastLongitude() + " ";
            }
            if (!StringUtil.isEmpty(elem.getNorthLatitude())) {
                boundingStr += "North Bounding Latitude: " + elem.getNorthLatitude() + " ";
            }
            if (!StringUtil.isEmpty(elem.getSouthLatitude())) {
                boundingStr += "South Bounding Latitude: " + elem.getSouthLatitude();
            }
            
            if (boundingStr != "") {
                if (str != "") {
                    str += "; ";
                }
                str += boundingStr;
            }
        }
        
        return str;
    }
    
    public String getKeywords() {
        String str = "";
        for (Iterator<StudyKeyword> it = getMetadata().getStudyKeywords().iterator(); it.hasNext();) {
            StudyKeyword elem = it.next();
            if (!StringUtil.isEmpty(elem.getValue())) {
                if (str != "") {
                    str += "; ";
                }
                str += elem.getValue();
            }
            if (!StringUtil.isEmpty(elem.getVocab())) {
                if (!StringUtil.isEmpty(elem.getVocabURI())) {
                    str += "&#32; (<a href='" + elem.getVocabURI() + "' target='_blank'>" + elem.getVocab() + "</a>)";
                } else {
                    str += "&#32; (" + elem.getVocab() + ")";
                }
            }
        }
        
        return str;
    }
    
    public String getTopicClasses() {
        String str = "";
        for (Iterator<StudyTopicClass> it = getMetadata().getStudyTopicClasses().iterator(); it.hasNext();) {
            StudyTopicClass elem = it.next();
            if (!StringUtil.isEmpty(elem.getValue())) {
                if (str != "") {
                    str += "; ";
                }
                str += elem.getValue();
            }
            if (!StringUtil.isEmpty(elem.getVocab())) {
                if (!StringUtil.isEmpty(elem.getVocabURI())) {
                    str += "&#32; (<a href='" + elem.getVocabURI() + "' target='_blank'>" + elem.getVocab() + "</a>)";
                } else {
                    str += "&#32; (" + elem.getVocab() + ")";
                }
            }
        }
        
        return str;
    }
    
    /**
     * If a Terms of Use StudyNote exists, return that, else return the study Owner terms of use (if it exists)
     * @return
     */
    public String getDataverseTermsOfUse() {
        if (getStudy().getOwner().isDownloadTermsOfUseEnabled()) {
            return getStudy().getOwner().getDownloadTermsOfUse();
        }
        return "";
    }
    
    public String getDistributors() {
        String str = "";
        for (Iterator<StudyDistributor> it = getMetadata().getStudyDistributors().iterator(); it.hasNext();) {
            StudyDistributor elem = it.next();
            if (!StringUtil.isEmpty(elem.getName())) {
                if (str != "") {
                    str += "; ";
                }
                
                if (!StringUtil.isEmpty(elem.getUrl())) {
                    str += "<a href='" + elem.getUrl() + "' target='_blank' title='" + elem.getName() + "'>" + elem.getName() + "</a>";
                } else {
                    str += elem.getName();
                }
                
            }
            if (!StringUtil.isEmpty(elem.getAbbreviation())) {
                str += " (" + elem.getAbbreviation() + ")";
            }
            if (!StringUtil.isEmpty(elem.getAffiliation())) {
                str += ", " + elem.getAffiliation();
            }
            if (!StringUtil.isEmpty(elem.getLogo())) {
                str += " <img src='" + elem.getLogo() + "' height='30px' alt='Logo' title='Logo' />";
            }
            
            
            
        }
        return str;
        
    }
    private boolean abstractAndScopePanelIsRendered;
    private boolean citationInformationPanelIsRendered;
    private boolean dataCollectionPanelIsRendered;
    private boolean notesPanelIsRendered;
    private boolean dataAvailPanelIsRendered;
    private boolean termsOfUsePanelIsRendered;
    
    public boolean isAbstractAndScopePanelIsRendered() {
        return abstractAndScopePanelIsRendered;
    }
    
    public void setAbstractAndScopePanelIsRendered(boolean abstractAndScopePanelIsRendered) {
        this.abstractAndScopePanelIsRendered = abstractAndScopePanelIsRendered;
    }
    
    public boolean isCitationInformationPanelIsRendered() {
        return citationInformationPanelIsRendered;
    }
    
    public void setCitationInformationPanelIsRendered(boolean citationInformationPanelIsRendered) {
        this.citationInformationPanelIsRendered = citationInformationPanelIsRendered;
    }
    
    public boolean isDataCollectionPanelIsRendered() {
        return dataCollectionPanelIsRendered;
    }
    
    public void setDataCollectionPanelIsRendered(boolean dataCollectionPanelIsRendered) {
        this.dataCollectionPanelIsRendered = dataCollectionPanelIsRendered;
    }
    
    public boolean isNotesPanelIsRendered() {
        return notesPanelIsRendered;
    }
    
    public void setNotesPanelIsRendered(boolean notesPanelIsRendered) {
        this.notesPanelIsRendered = notesPanelIsRendered;
    }
    
    public boolean isDataAvailPanelIsRendered() {
        return dataAvailPanelIsRendered;
    }
    
    public void setDataAvailPanelIsRendered(boolean dataAvailPanelIsRendered) {
        this.dataAvailPanelIsRendered = dataAvailPanelIsRendered;
    }
    
    public boolean isTermsOfUsePanelIsRendered() {
        return termsOfUsePanelIsRendered;
    }
    
    public void setTermsOfUsePanelIsRendered(boolean termsOfUsePanelIsRendered) {
        this.termsOfUsePanelIsRendered = termsOfUsePanelIsRendered;
    }
    
    /* replace this method with a new one in order to improve performance. -xyang
    public void initFileCategoryUIList(VDC vdc, VDCUser user, UserGroup ipUserGroup) {
        categoryUIList = new ArrayList<FileCategoryUI>();
        StudyServiceLocal studyService = null;
        try {
            studyFileService = (StudyFileServiceLocal) new InitialContext().lookup("java:comp/env/studyFileService");
        } catch (Exception e) {
            e.printStackTrace();
        }


        FileCategoryUI catUI = null;
        for (FileMetadata fmd : studyFileService.getOrderedFilesByStudyVersion(getStudyVersion().getId())) {
            if (catUI == null || !fmd.getCategory().equals(catUI.getCategory())) {
                catUI = new FileCategoryUI(fmd.getCategory());
                categoryUIList.add(catUI);
            }
            StudyFileUI sfui = new StudyFileUI(fmd, vdc, user, ipUserGroup);
            catUI.getStudyFileUIs().add(sfui);
        }


        Collections.sort(categoryUIList);

    /*
    List categories = studyService.getOrderedFileCategories(getStudy().getId());
    Iterator iter = categories.iterator();
    while (iter.hasNext()) {
    FileCategory fc = (FileCategory) iter.next();
    FileCategoryUI catUI = new FileCategoryUI(fc,vdc,user, ipUserGroup);
    categoryUIList.add(catUI); 
    } */
    
    //add new method to replace the old one with the same name to improve performance. -xyang
    public void initFileCategoryUIList(VDCUser user, UserGroup ipUserGroup, boolean loadAllFiles) {
        categoryUIList = new ArrayList<FileCategoryUI>();
        try {
            studyFileService = (StudyFileServiceLocal) new InitialContext().lookup("java:comp/env/studyFileService");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
	if (fileIdList == null) {  
            fileIdList = studyFileService.getOrderedFileIdsByStudyVersion (getStudyVersion().getId());
        } 
        
        if (loadAllFiles) {
            getFileCategoryUIList(getStudyVersion().getId(), fileIdList, user, ipUserGroup );
            fileIdList = null;            
        } else {
            getFileCategoryUIList(getStudyVersion().getId(), getSubFileIdList(fileIdList), user, ipUserGroup);
        }
    }
    
    public List <FileCategoryUI> getFileCategoryUIList(ActionEvent ae)
    {
        return getFileCategoryUIList(getStudyVersion().getId(), getSubFileIdList(fileIdList), user, ipUserGroup );
    }

    private List <FileCategoryUI> getFileCategoryUIList(Long studyVersionId, List<Long> fIdList, VDCUser user, UserGroup ipUserGroup) {
        //set catUI to the last element of categoryUIList if categoryUIList is not empty. -gdurand
        FileCategoryUI catUI = categoryUIList.size() == 0 ? null : categoryUIList.get( categoryUIList.size() -1 );
                
        Map<Long,FileMetadata> filesMap = studyFileService.getFilesByStudyVersionAndIds(getStudyVersion().getId(), fIdList);

        for (Long fileId : fIdList) {
            FileMetadata fmd = filesMap.get(fileId);
            if ((catUI == null || !fmd.getCategory().equals(catUI.getCategory()))) {
                catUI = new FileCategoryUI(fmd.getCategory());
                categoryUIList.add(catUI);
            }
            StudyFileUI sfui = new StudyFileUI(fmd, user, ipUserGroup);
            catUI.getStudyFileUIs().add(sfui);
        }
        
        return categoryUIList;
    }
    
    public List <FileCategoryUI> getAllFileCategoryUIList(ActionEvent ae) {
        List<FileCategoryUI> allCategoryUIList = new ArrayList<FileCategoryUI>(); 
        allCategoryUIList = getFileCategoryUIList(getStudyVersion().getId(), fileIdList, user, ipUserGroup );
        fileIdList = null;
        return allCategoryUIList;
    }
    
    private List getSubFileIdList(List studyFileIdList) {
        
        List subFileIdList = null;
        if (studyFileIdList.size() <= FILE_NUMBERS) {
            subFileIdList = studyFileIdList;
            fileIdList = null;
        } else {
            subFileIdList = studyFileIdList.subList(0, FILE_NUMBERS); 
            fileIdList = studyFileIdList.subList(FILE_NUMBERS, studyFileIdList.size());
        }
        return subFileIdList;
    }
    
    public int getFileIdListSize() {
        int iRet=0;
        if (fileIdList != null) {
            iRet = fileIdList.size();
        }
        return iRet;
    }
    
    public void initFileMetadataList(StudyVersion sv) {
        fileMetadataList = new ArrayList<FileMetadata>();
        StudyServiceLocal studyService = null;
        try {
            studyFileService = (StudyFileServiceLocal) new InitialContext().lookup("java:comp/env/studyFileService");
        } catch (Exception e) {
            e.printStackTrace();
        }

        fileMetadataList = studyFileService.getFilesByStudyVersionOrderedById(sv.getId());
    }

    public boolean isAnyFileUnrestricted() {
        
        for (Iterator it = categoryUIList.iterator(); it.hasNext();) {
            FileCategoryUI catUI = (FileCategoryUI) it.next();
            if (catUI.isAnyFileUnrestricted()) {
                return true;
            }
        }
        return false;
        
    }
    
    public boolean isAnyFileRestricted() {
        for (Iterator it = categoryUIList.iterator(); it.hasNext();) {
            FileCategoryUI catUI = (FileCategoryUI) it.next();
            for (Iterator it2 = catUI.getStudyFileUIs().iterator(); it2.hasNext();) {
                StudyFileUI studyFileUI = (StudyFileUI) it2.next();
                if (studyFileUI.isRestrictedForUser()) {
                    return true;
                }
            }
            
        }
        return false;
        
    }
    
    public static List filterVisibleStudies(List originalStudies, VDC vdc, VDCUser user, UserGroup ipUserGroup) {
        return filterVisibleStudies(originalStudies, vdc, user, ipUserGroup, -1);
    }
    
    public static List filterVisibleStudies(List originalStudies, VDC vdc, VDCUser user, UserGroup ipUserGroup, int numResults) {
        List filteredStudies = new ArrayList();
        
        if (numResults != 0) {
            int count = 0;
            Iterator iter = originalStudies.iterator();
            while (iter.hasNext()) {
                Study study = (Study) iter.next();
                if (StudyUI.isStudyVisibleToUser(study, vdc, user) || isStudyVisibleToGroup(study, vdc, ipUserGroup)) {
                    filteredStudies.add(study);
                    if (numResults > 0 && ++count >= numResults) {
                        break;
                    }
                }
            }
        }
        
        return filteredStudies;
    }
    
    public static List filterVisibleStudyUIs(List originalStudies, VDC vdc, VDCUser user, UserGroup ipUserGroup, int numResults) {
        List filteredStudies = new ArrayList();
        
        if (numResults != 0) {
            int count = 0;
            Iterator iter = originalStudies.iterator();
            while (iter.hasNext()) {
                Study study = (Study) iter.next();
                if (StudyUI.isStudyVisibleToUser(study, vdc, user) || isStudyVisibleToGroup(study, vdc, ipUserGroup)) {
                    StudyUI studyUIToAdd = new StudyUI(study);
                    filteredStudies.add(studyUIToAdd);
                    if (numResults > 0 && ++count >= numResults) {
                        break;
                    }
                }
            }
        }        
        return filteredStudies;
    }
    
    public static boolean isStudyVisibleToUser(Study study, VDC vdc, VDCUser user) {
        // if restricted vdc, only visible in that VDC
        if (study.getOwner().isRestricted() &&
                (vdc == null || !study.getOwner().getId().equals(vdc.getId()))) {
            return false;
        }
        
        // only visible if released
        if (!study.isReleased()) {
            return false;
        }
        
        // lastly check restrictions
        return !study.isStudyRestrictedForUser(user);
        
        
    }
    
    /**
     * check if this is visible for the ipgroup
     *
     * @author wbossons
     */
    public static boolean isStudyVisibleToGroup(Study study, VDC vdc, UserGroup usergroup) {
        // if restricted vdc, only visible in that VDC
        if (study.getOwner().isRestricted() &&
                (vdc == null || !study.getOwner().getId().equals(vdc.getId()))) {
            return false;
        }
        
        // only visible if released
        if (!study.isReleased()) {
            return false;
        }
        
        // lastly check restrictions
        return !study.isStudyRestrictedForGroup(usergroup);
        
    }
    
    //property to hold the number of files to display each time. -xyang
    private static final int FILE_NUMBERS = 25;
    
    public int getFILE_NUMBERS() {
        return FILE_NUMBERS;
    }
    
    /**
     * Holds value of property fileIdList.
     */
    private List fileIdList = null;
    
    /**
     * Getter for property fileIdList.
     * @return Value of property fileIdList.
     */
    public List getFileIdList() {
        return this.fileIdList; 
    }
    
    /**
     * Setter for property fileIdList.
     * @param fileIdList New value of property fileIdList.
     */
    public void setFileIdList(List fileIdList) {
        this.fileIdList = fileIdList;
    }
    
    /**
     * Holds value of property categoryUIList.
     */
    private List<FileCategoryUI> categoryUIList = new ArrayList<FileCategoryUI>();
    

    
    /**
     * Getter for property categoryUIList.
     * @return Value of property categoryUIList.
     */
    public List<FileCategoryUI> getCategoryUIList() {
        return this.categoryUIList;
    }
    
    /**
     * Setter for property categoryUIList.
     * @param categoryUIList New value of property categoryUIList.
     */
    public void setCategoryUIList(List<FileCategoryUI> categoryUIList) {
        this.categoryUIList = categoryUIList;
    }


    /**
     * fileMetadataList is a single list of FileMetadata objects
     * (i.e., not split by file categories). This is created when
     * the StudyUI is going to be used for comparing one study version
     * to another on the Study Differences page.
     */

    private List<FileMetadata> fileMetadataList;


    public List<FileMetadata> getFileMetadataList() {
        return this.fileMetadataList;
    }

     public void setFileMetadataList(List<FileMetadata> fml) {
        this.fileMetadataList = fml;
    }

    
    private List foundInVariables;
    
    public List getFoundInVariables() {
        return foundInVariables;
    }
    
    public void setFoundInVariables(List foundInVariables) {
        this.foundInVariables = foundInVariables;
    }

    private List foundInVersions;

    /**
     * @return the foundInVersions
     */
    public List getFoundInVersions() {
        return foundInVersions;
    }

    /**
     * @param foundInVersions the foundInVersions to set
     */
    public void setFoundInVersions(List foundInVersions) {
        this.foundInVersions = foundInVersions;
    }

    private boolean displayVersions;

    /*
     * The "foundInStudyFiles" list and the getter and setter methods below
     * are the equivalents of the corresponding mechanisms above for the 
     * variable and version searches. These are being added as we are
     * adding searching on file-level metadata to the application. 
     *  -- L.A. 
     */
    
    private List foundInStudyFiles;
    
    public List getFoundInStudyFiles() {
        return foundInStudyFiles;
    }
    
    public void setFoundInStudyFiles(List foundInStudyFiles) {
        this.foundInStudyFiles = foundInStudyFiles;
    }
    
    public static boolean isStudyInList(Study study, List list) {
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            Study s = (Study) iter.next();
            if ( s.getId().equals(study.getId()) ) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean isStudyInList(Long studyId, List list) {
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            Study s = (Study) iter.next();
            if ( s.getId().equals(studyId) ) {
                return true;
            }
        }
        
        return false;
    }

    private Long downloadCount = null;
    private Long versionDownloadCount = null;
    
    public Long getDownloadCount() {
        if (downloadCount == null) {
            initStudyService();
            downloadCount = studyService.getStudyDownloadCount(studyId);
        }

        return downloadCount;
    }

    public Long getVersionDownloadCount() {
        if (versionDownloadCount == null) {
            initStudyService();
            versionDownloadCount = studyService.getStudyVersionDownloadCount(studyVersionId);
        }

        return versionDownloadCount;
    }
    

    private boolean selected;
    private boolean selectable = true;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }
    
    public boolean isStudyRestricted() {
        return getStudy().isStudyRestrictedForUser(user, ipUserGroup);
    }

    public String getLastUpdateTime() {
        return dateFormatter.format( getStudy().getLastUpdateTime() );
    }

    public String getReleaseTime() {
        return dateFormatter.format( getStudyVersion().getReleaseTime() );
    }

   public boolean isUserAuthorizedToEdit() {
        boolean authorized = false;
        if (user != null) {
            authorized = getStudy().isUserAuthorizedToEdit(user);
        }
        return authorized;
    }

    public boolean isUserAuthorizedToRelease() {
        boolean authorized = false;
        if (user != null) {
            authorized = getStudy().isUserAuthorizedToRelease(user);
        }
        return authorized;
    }

    public boolean isUserCuratorOrAdmin() {
        boolean ret = false;
         VDCRole vdcRole = user.getVDCRole(study.getOwner());
         if (vdcRole!=null && (vdcRole.getRole().getName().equals(RoleServiceLocal.ADMIN)||vdcRole.getRole().getName().equals(RoleServiceLocal.CURATOR ))) {
             ret = true;
         }
         return ret;
    }

    public List<StudyVersion> getViewableStudyVersions() {
        if (study.getLatestVersion().isWorkingCopy() && !isUserAuthorizedToEdit()) {
            return study.getStudyVersions().subList(1, study.getStudyVersions().size());
        } else {
            return study.getStudyVersions();
            }
    }

    /**
     * @return the displayVersions
     */
    public boolean isDisplayVersions() {
        return displayVersions;
    }

    /**
     * @param displayVersions the displayVersions to set
     */
    public void setDisplayVersions(boolean displayVersions) {
        this.displayVersions = displayVersions;
    }
        

    public String getStudyFieldValue(StudyField studyField) {
        String str = "";
        for (StudyFieldValue elem : studyField.getStudyFieldValues()) {
            if (!StringUtil.isEmpty(elem.getStrValue())) {
                if (str != "") {
                    str += "; ";
                }
                str += elem.getStrValue();
            }           
        }

        return str;
    }    

}
