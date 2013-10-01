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
 * VDCNetwork.java
 *
 * Created on July 28, 2006, 3:08 PM
 *
 */
package edu.harvard.iq.dvn.core.vdc;

import edu.harvard.iq.dvn.core.admin.VDCUser;
import edu.harvard.iq.dvn.core.study.Study;
import edu.harvard.iq.dvn.core.study.Template;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.*;

/**
 *
 * @author Ellen Kraffmiller
 */
@Entity
public class VDCNetwork implements java.io.Serializable  {
    public static final String EXPORT_PERIOD_DAILY="daily";
    public static final String EXPORT_PERIOD_WEEKLY="weekly";
    public static final String EXPORT_PERIOD_NONE="none";
   
    /** Creates a new instance of VDCNetwork */
    public VDCNetwork() {
        vdcNetworkStats = new VDCNetworkStats();
        vdcNetworkStats.setVdcNetwork(this);
        vdcNetworkStats.setDownloadCount(new Long(0));
        vdcNetworkStats.setStudyCount(new Long(0));
        vdcNetworkStats.setFileCount(new Long(0));
    }
   
    public Collection<Study> search(String query) {
        //TODO: 
        return null;
    }
    
    public void login(String userName, String password) {
        //TODO:
    }
    
    public Collection getStudyFields() {
        // TODO:
        return null;
    }

    /**
     * Make the text the default db type for announcements.
     */
    @Column(name="announcements", columnDefinition="TEXT")
    /**
     * Holds value of property announcements.
     */
    private String announcements;

    /**
     * Getter for property announcements.
     * @return Value of property announcements.
     */
    public String getAnnouncements() {
        return this.announcements;
    }

    /**
     * Setter for property announcements.
     * @param announcements New value of property announcements.
     */
    public void setAnnouncements(String announcements) {
        this.announcements = announcements;
    }

 
    /**
     * Holds value of property id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(Long id) {
        this.id = id;
    }
  /**
     * Holds value of property version.
     */
    @Version
    private Long version;

    /**
     * Getter for property version.
     * @return Value of property version.
     */
    public Long getVersion() {
        return this.version;
    }

    /**
     * Setter for property version.
     * @param version New value of property version.
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Holds value of property defaultTemplate.
     */
    @ManyToOne
    private Template defaultTemplate;

    /**
     * Getter for property defaultTemplate.
     * @return Value of property defaultTemplate.
     */
    public Template getDefaultTemplate() {
        return this.defaultTemplate;
    }

    /**
     * Setter for property defaultTemplate.
     * @param defaultTemplate New value of property defaultTemplate.
     */
    public void setDefaultTemplate(Template defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

    /**
     * Holds value of property restrictionText.
     * Text to show to user if a file or study is restricted from them.
     */
    private String restrictionText;

    /**
     * Getter for property restrictionText.
     * @return Value of property restrictionText.
     */
    public String getRestrictionText() {
        return this.restrictionText;
    }

    /**
     * Setter for property restrictionText.
     * @param restrictionText New value of property restrictionText.
     */
    public void setRestrictionText(String restrictionText) {
        this.restrictionText = restrictionText;
    }

    /**
     * Holds value of property name.
     */
    private String name;

    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    private String urlAlias;

    public String getUrlAlias() {
        return urlAlias;
    }

    public void setUrlAlias(String urlAlias) {
        this.urlAlias = urlAlias;
    }
    

    /**
     * Path to logo in domain1/docroot/images
     */
    private String logo;

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    /**
     * Short description, used on homepage widget
     */
    private String shortDescription;

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * Holds value of property systemEmail.
     */
    private String systemEmail;

    /**
     * Getter for property systemEmail.
     * @return Value of property systemEmail.
     */
    public String getSystemEmail() {
        return this.systemEmail;
    }

    /**
     * Setter for property systemEmail.
     * @param systemEmail New value of property systemEmail.
     */
    public void setSystemEmail(String systemEmail) {
        this.systemEmail = systemEmail;
    }
    
    @OneToMany(mappedBy="vdcNetwork", cascade={CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST})
    private java.util.Collection<NetworkContactSubject> networkContactSubjects;

    public java.util.Collection<NetworkContactSubject> getContactSubjects() {
        return networkContactSubjects;
    }

    public void setNetworkdContactSubjects(java.util.Collection<NetworkContactSubject> networkContactSubjects) {
        this.networkContactSubjects = networkContactSubjects;
    }
    
    @OneToMany(mappedBy="vdcNetwork", cascade={CascadeType.MERGE})
    private Collection<VDC> networkVDCs;
    
    public Collection<VDC> getNetworkVDCs(){
        return networkVDCs;
    }
    
    public void setNetworkVDCs(Collection<VDC> networkVDCs) {
        this.networkVDCs = networkVDCs;
    }

    @OneToMany(mappedBy="vdcNetwork")
    private Collection<Template> networkTemplates;
    
    public Collection<Template> getNetworkTemplates(){
        return networkTemplates;
    }
    
    public void setNetworkTemplates(Collection<Template> networkTemplates) {
        this.networkTemplates = networkTemplates;
    }
    
    /**
     * Holds value of property allowCreateRequest.
     */
    private boolean allowCreateRequest;

    /**
     * Getter for property allowCreateRequest.
     * @return Value of property allowCreateRequest.
     */
    public boolean isAllowCreateRequest() {
        return this.allowCreateRequest;
    }

    /**
     * Setter for property allowCreateRequest.
     * @param allowCreateRequest New value of property allowCreateRequest.
     */
    public void setAllowCreateRequest(boolean allowCreateRequest) {
        this.allowCreateRequest = allowCreateRequest;
    }

    /**
     * Make the text the default db type for defaultvdcheader.
     */
    @Column(name="defaultVDCHeader", columnDefinition="TEXT")
    
    /**
     * Holds value of property defaultVDCHeader.
     */
    private String defaultVDCHeader;

    /**
     * Getter for property defaultVDCHeader.
     * @return Value of property defaultVDCHeader.
     */
    public String getDefaultVDCHeader() {
        return this.defaultVDCHeader;
    }

    /**
     * Setter for property defaultVDCHeader.
     * @param defaultVDCHeader New value of property defaultVDCHeader.
     */
    public void setDefaultVDCHeader(String defaultVDCHeader) {
        this.defaultVDCHeader = defaultVDCHeader;
    }

    /**
     * Make the text the default db type for defaultvdcfooter.
     */
    @Column(name="defaultVDCFooter", columnDefinition="TEXT")
    
    /**
     * Holds value of property defaultVDCFooter.
     */
    private String defaultVDCFooter;

    /**
     * Getter for property defaultVDCFooter.
     * @return Value of property defaultVDCFooter.
     */
    public String getDefaultVDCFooter() {
        return this.defaultVDCFooter;
    }

    /**
     * Setter for property defaultVDCFooter.
     * @param defaultVDCFooter New value of property defaultVDCFooter.
     */
    public void setDefaultVDCFooter(String defaultVDCFooter) {
        this.defaultVDCFooter = defaultVDCFooter;
    }

    /**
     * Make the text the default db type for networkPageHeader.
     */
    @Column(name="networkPageHeader", columnDefinition="TEXT")
    
    /**
     * Holds value of property networkPageHeader.
     */
    private String networkPageHeader;

    /**
     * Getter for property networkPageHeader.
     * @return Value of property networkPageHeader.
     */
    public String getNetworkPageHeader() {
        return this.networkPageHeader;
    }

    /**
     * Setter for property networkPageHeader.
     * @param networkPageHeader New value of property networkPageHeader.
     */
    public void setNetworkPageHeader(String networkPageHeader) {
        this.networkPageHeader = networkPageHeader;
    }

    /**
     * Make the text the default db type for networkPageFooter.
     */
    @Column(name="networkPageFooter", columnDefinition="TEXT")
    
    /**
     * Holds value of property networkPageFooter.
     */
    private String networkPageFooter;

    /**
     * Getter for property networkPageFooter.
     * @return Value of property networkPageFooter.
     */
    public String getNetworkPageFooter() {
        return this.networkPageFooter;
    }

    /**
     * Setter for property networkPageFooter.
     * @param networkPageFooter New value of property networkPageFooter.
     */
    public void setNetworkPageFooter(String networkPageFooter) {
        this.networkPageFooter = networkPageFooter;
    }

    /**
     * Holds value of property displayAnnouncements.
     */
    private boolean displayAnnouncements;

    /**
     * Getter for property displayAnnouncements.
     * @return Value of property displayAnnouncements.
     */
    public boolean isDisplayAnnouncements() {
        return this.displayAnnouncements;
    }

    /**
     * Setter for property displayAnnouncements.
     * @param displayAnnouncements New value of property displayAnnouncements.
     */
    public void setDisplayAnnouncements(boolean displayAnnouncements) {
        this.displayAnnouncements = displayAnnouncements;
    }

    /**
     * Make the text the default db type for networkPageFooter.
     */
    @Column(name="aboutThisDataverseNetwork", columnDefinition="TEXT")
    /**
     * Holds value of property aboutThisDataverseNetwork.
     */
    private String aboutThisDataverseNetwork;

    /**
     * Getter for property aboutThisDataverseNetwork.
     * @return Value of property aboutThisDataverseNetwork.
     */
    public String getAboutThisDataverseNetwork() {
        return this.aboutThisDataverseNetwork;
    }

    /**
     * Setter for property aboutThisDataverseNetwork.
     * @param aboutThisDataverseNetwork New value of property aboutThisDataverseNetwork.
     */
    public void setAboutThisDataverseNetwork(String aboutThisDataverseNetwork) {
        this.aboutThisDataverseNetwork = aboutThisDataverseNetwork;
    }

    /**
     * Make the text the default db type.
     */
    @Column(name="defaultVDCAnnouncements", columnDefinition="TEXT")
    /**
     * Holds value of property defaultVDCAnnouncements.
     */
    private String defaultVDCAnnouncements;

    /**
     * Getter for property defaultVDCAnnouncements.
     * @return Value of property defaultVDCAnnouncements.
     */
    public String getDefaultVDCAnnouncements() {
        return this.defaultVDCAnnouncements;
    }

    /**
     * Setter for property defaultVDCAnnouncements.
     * @param defaultVDCAnnouncements New value of property defaultVDCAnnouncements.
     */
    public void setDefaultVDCAnnouncements(String defaultVDCAnnouncements) {
        this.defaultVDCAnnouncements = defaultVDCAnnouncements;
    }

    /**
     * Holds value of property displayVDCAnnouncements.
     */
    private boolean displayVDCAnnouncements;

    /**
     * Getter for property displayVDCAnnouncements.
     * @return Value of property displayVDCAnnouncements.
     */
    public boolean isDisplayVDCAnnouncements() {
        return this.displayVDCAnnouncements;
    }

    /**
     * Setter for property displayVDCAnnouncements.
     * @param displayVDCAnnouncements New value of property displayVDCAnnouncements.
     */
    public void setDisplayVDCAnnouncements(boolean displayVDCAnnouncements) {
        this.displayVDCAnnouncements = displayVDCAnnouncements;
    }

    /**
     * Holds value of property displayVDCRecentStudies.
     */
    private boolean displayVDCRecentStudies;

    /**
     * Getter for property displayVDCRecentStudies.
     * @return Value of property displayVDCRecentStudies.
     */
    public boolean isDisplayVDCRecentStudies() {
        return this.displayVDCRecentStudies;
    }

    /**
     * Setter for property displayVDCRecentStudies.
     * @param displayVDCRecentStudies New value of property displayVDCRecentStudies.
     */
    public void setDisplayVDCRecentStudies(boolean displayVDCRecentStudies) {
        this.displayVDCRecentStudies = displayVDCRecentStudies;
    }

     /**
     * Make the text the default db type.
     */
    @Column(name="defaultVDCAboutText", columnDefinition="TEXT")
    
    /**
     * Holds value of property defaultVDCAboutText.
     */
    private String defaultVDCAboutText;

    /**
     * Getter for property defaultVDCAboutText.
     * @return Value of property defaultVDCAboutText.
     */
    public String getDefaultVDCAboutText() {
        return this.defaultVDCAboutText;
    }

    /**
     * Setter for property defaultVDCAboutText.
     * @param defaultVDCAboutText New value of property defaultVDCAboutText.
     */
    public void setDefaultVDCAboutText(String defaultVDCAboutText) {
        this.defaultVDCAboutText = defaultVDCAboutText;
    }

    /**
     * Holds value of property defaultNetworkAdmin.
     */
    @OneToOne
    private VDCUser defaultNetworkAdmin;

    /**
     * Getter for property defaultNetworkAdmin.
     * @return Value of property defaultNetworkAdmin.
     */
    public VDCUser getDefaultNetworkAdmin() {
        return this.defaultNetworkAdmin;
    }

    /**
     * Setter for property defaultNetworkAdmin.
     * @param defaultNetworkAdmin New value of property defaultNetworkAdmin.
     */
    public void setDefaultNetworkAdmin(VDCUser defaultNetworkAdmin) {
        this.defaultNetworkAdmin = defaultNetworkAdmin;
    }
    
    /**
     * Holds value of property creator.
     */
    @OneToOne
    private VDCUser creator;

    /**
     * Getter for property creator.
     * @return Value of property defaultNetworkAdmin.
     */
    public VDCUser getCreator() {
        return this.creator;
    }

    /**
     * Setter for property creator.
     * @param creator New value of property creator.
     */
    public void setCreator(VDCUser creator) {      
        this.creator = creator;
    }

    /**
     * Holds value of property protocol.
     */
    private String protocol;

    /**
     * Getter for property protocol.
     * @return Value of property protocol.
     */
    public String getProtocol() {
        return this.protocol;
    }

    /**
     * Setter for property protocol.
     * @param protocol New value of property protocol.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    /**
     * Holds value of property affiliation.
     */
    private String affiliation;
    public String getAffiliation() {
        return this.affiliation;
    }
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }
    
    /**
     * Holds value of property authority.
     */
    private String authority;

    /**
     * Getter for property authority.
     * @return Value of property authority.
     */
    public String getAuthority() {
        return this.authority;
    }

    /**
     * Setter for property authority.
     * @param authority New value of property authority.
     */
    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getDepositTermsOfUse() {
        return depositTermsOfUse;
    }

    public void setDepositTermsOfUse(String depositTermsOfUse) {
        this.depositTermsOfUse = depositTermsOfUse;
    }

    public boolean isDepositTermsOfUseEnabled() {
        return depositTermsOfUseEnabled;
    }

    public void setDepositTermsOfUseEnabled(boolean depositTermsOfUseEnabled) {
        this.depositTermsOfUseEnabled = depositTermsOfUseEnabled;
    }

    public String getTermsOfUse() {
        return termsOfUse;
    }

    public void setTermsOfUse(String termsOfUse) {
        this.termsOfUse = termsOfUse;
    }

      public int hashCode() {
        int hash = 0;
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof VDCNetwork)) {
            return false;
        }
        VDCNetwork other = (VDCNetwork)object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) return false;
        return true;
    }       

    /**
     * Holds value of property handleRegistration.
     */
    private boolean handleRegistration;

    /**
     * Getter for property handleRegistration.
     * @return Value of property handleRegistration.
     */
    public boolean isHandleRegistration() {
        return this.handleRegistration;
    }

    /**
     * Setter for property handleRegistration.
     * @param handleRegistration New value of property handleRegistration.
     */
    public void setHandleRegistration(boolean handleRegistration) {
        this.handleRegistration = handleRegistration;
    }
 
   
    public String getExportPeriod() {
        return exportPeriod;
    }

    public void setExportPeriod(String exportPeriod) {
        this.exportPeriod = exportPeriod;
    }

    public Integer getExportHourOfDay() {
        return exportHourOfDay;
    }

    public void setExportHourOfDay(Integer exportHourOfDay) {
        this.exportHourOfDay = exportHourOfDay;
    }

    public Integer getExportDayOfWeek() {
        return exportDayOfWeek;
    }

    public boolean isTermsOfUseEnabled() {
        return termsOfUseEnabled;
    }

    public void setTermsOfUseEnabled(boolean termsOfUseEnabled) {
        this.termsOfUseEnabled = termsOfUseEnabled;
    }

    public void setExportDayOfWeek(Integer exportDayOfWeek) {
        this.exportDayOfWeek = exportDayOfWeek;
    }
   
 
    private String exportPeriod;

    private Integer exportHourOfDay;

    private Integer exportDayOfWeek;
    
    @Column(name="termsOfUse", columnDefinition="TEXT")
    /**
     * Holds value of property announcements.
     */
    private String termsOfUse;
    
    private boolean termsOfUseEnabled;  
    
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date termsOfUseUpdated;

    public Date getTermsOfUseUpdated() {
        return termsOfUseUpdated;
    }

    public void setTermsOfUseUpdated(Date termsOfUseUpdated) {
        this.termsOfUseUpdated = termsOfUseUpdated;
    }
    
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date networkCreated;

    public Date getNetworkCreated() {
        return networkCreated;
    }

    public void setNetworkCreated(Date networkCreated) {
        this.networkCreated = networkCreated;
    }


    public String getDownloadTermsOfUse() {
        return downloadTermsOfUse;
    }

    public void setDownloadTermsOfUse(String downloadTermsOfUse) {
        this.downloadTermsOfUse = downloadTermsOfUse;
    }

    public boolean isDownloadTermsOfUseEnabled() {
        return downloadTermsOfUseEnabled;
    }

    public void setDownloadTermsOfUseEnabled(boolean downloadTermsOfUseEnabled) {
        this.downloadTermsOfUseEnabled = downloadTermsOfUseEnabled;
    }

    
    @Column(name="depositTermsOfUse", columnDefinition="TEXT")
    /**
     * Holds value of property announcements.
     */
    private String depositTermsOfUse;
    
    private boolean depositTermsOfUseEnabled;  
    
     @Column(name="downloadTermsOfUse", columnDefinition="TEXT")
    /**
     * Holds value of property announcements.
     */
    private String downloadTermsOfUse;
    
    private boolean downloadTermsOfUseEnabled;  
    
    private Long defaultDisplayNumber;

    public Long getDefaultDisplayNumber() {
        return defaultDisplayNumber;
    }

    public void setDefaultDisplayNumber(Long defaultDisplayNumber) {
        this.defaultDisplayNumber = defaultDisplayNumber;
    }
    
    private String defaultDVSortColumn;

    public String getDefaultDVSortColumn() {
        return defaultDVSortColumn;
    }

    public void setDefaultDVSortColumn(String defaultDVSortColumn) {
        this.defaultDVSortColumn = defaultDVSortColumn;
    }

    private boolean requireDVaffiliation;

    public boolean isRequireDVaffiliation() {
        return requireDVaffiliation;
    }

    public void setRequireDVaffiliation(boolean requireDVaffiliation) {
        this.requireDVaffiliation = requireDVaffiliation;
    }

    private boolean requireDVclassification;

    public boolean isRequireDVclassification() {
        return requireDVclassification;
    }

    public void setRequireDVclassification(boolean requireDVclassification) {
        this.requireDVclassification = requireDVclassification;
    }

    private boolean requireDVdescription;

    public boolean isRequireDVdescription() {
        return requireDVdescription;
    }

    public void setRequireDVdescription(boolean requireDVdescription) {
        this.requireDVdescription = requireDVdescription;
    }

    private boolean requireDVstudiesforrelease;

    public boolean isRequireDVstudiesforrelease() {
        return requireDVstudiesforrelease;
    }

    public void setRequireDVstudiesforrelease(boolean requireDVstudiesforrelease) {
        this.requireDVstudiesforrelease = requireDVstudiesforrelease;
    }
    
    
    @Column(name="statusnotice", columnDefinition="TEXT")
    private String statusNotice;

    public String getStatusNotice() {
        return statusNotice;
    }

    public void setStatusNotice(String statusNotice) {
        this.statusNotice = statusNotice;
    }
    
    /**
     * Holds value of property contactEmail. 
     */
    private String contactEmail;

    /**
     * Getter for property contactEmail.
     * @return Value of property contactEmail.
     */
    public String getContactEmail() {
        return this.contactEmail;
    }

    /**
     * Setter for property contactEmail.
     * @param contactEmail New value of property contactEmail.
     */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    
    @ManyToMany(mappedBy="linkedToNetworks")
    private Collection<Study> linkedStudies;
    
    
    
    public Collection<Study> getLinkedStudies() {
        return linkedStudies;
    }
    
    public void setLinkedStudies(Collection<Study> studies) {
        this.linkedStudies = studies;
    }
    
    @OneToOne(mappedBy="vdcNetwork",cascade={CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST })
    private VDCNetworkStats vdcNetworkStats;

    public VDCNetworkStats getVdcNetworkStats() {
        return vdcNetworkStats;
    }

    public void setVdcNetworkStats(VDCNetworkStats vdcNetworkStats) {
        this.vdcNetworkStats = vdcNetworkStats;
    }
    
}
