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
 * VDCUser.java
 *
 * Created on July 28, 2006, 1:36 PM
 */
package edu.harvard.iq.dvn.core.admin;

import edu.harvard.iq.dvn.core.study.Study;
import edu.harvard.iq.dvn.core.study.StudyComment;
import edu.harvard.iq.dvn.core.study.StudyFile;
import edu.harvard.iq.dvn.core.study.StudyLock;
import edu.harvard.iq.dvn.core.study.VersionContributor;
import edu.harvard.iq.dvn.core.vdc.VDC;
import edu.harvard.iq.dvn.core.vdc.VDCNetwork;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Ellen Kraffmiller
 */
@Entity
public class VDCUser implements java.io.Serializable  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "user", cascade={CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST})
    private List<StudyLock> studyLocks;

    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private boolean agreedTermsOfUse;
    // User is responsible for complying with and enforcing any Terms of Use
    // associated with any Digital Objects from this DVN, and thus authorized
    // direct access to Terms of Use-restricted objects, without TOU page
    // redirects. 
    // This is subject to a LEGAL agreement between the individual and the 
    // DVN administrators. 
    private boolean bypassTermsOfUse;
 //   private String password;

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }
    
    @Column(columnDefinition="text")
    private String encryptedPassword;
    
    @ManyToMany (cascade={ CascadeType.PERSIST })
    private Collection<UserGroup> userGroups;
    
    @ManyToOne (cascade={CascadeType.PERSIST })
    private edu.harvard.iq.dvn.core.admin.NetworkRole networkRole;
    
    @OneToMany(mappedBy="vdcUser", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    private Collection<RoleRequest> roleRequests;
    
      @OneToMany(mappedBy="vdcUser", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    private Collection<NetworkRoleRequest> networkRoleRequests;
  
    
    @OneToMany(mappedBy="vdcUser", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    private Collection<VDCRole> vdcRoles;
    
    @ManyToMany (cascade={ CascadeType.PERSIST })
    private List<StudyComment> flaggedStudyComments;

    @OneToMany(mappedBy="commentCreator", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    private List<StudyComment> studyComments;

    @OneToMany(mappedBy="contributor", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    private List<VersionContributor> versionContributors;

    public List<VersionContributor> getVersionContributors() {
        return versionContributors;
    }

    public void setVersionContributors(List<VersionContributor> versionContributors) {
        this.versionContributors = versionContributors;
    }
    /**
     * Getter for property id.
     * @return Value of property id.
     */
       
    public Long getId() {
        return this.id;
    }
   /**
     * Creates a new instance of VDCUser
     */
    public VDCUser() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

 /*   public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
*/
    /**
     * Getter for property vdcRoles.
     * @return Value of property vdcRoles.
     */
    public Collection<VDCRole> getVdcRoles() {
        return this.vdcRoles;
    }
    
    public VDCRole getVDCRole(VDC vdc) {
        if (vdc != null) {
            for (Iterator it = vdcRoles.iterator(); it.hasNext();) {
                VDCRole elem = (VDCRole) it.next();
                if (elem.getVdc().getId().equals(vdc.getId())) {
                    return elem;
                }
            }
        }
        return null;
    }
    /**
     *
     * @param vdc - the dataverse where we are checking the role
     * @return true  if this user has been assigned the contributor role in the VDC.
     *
     */
    public boolean isPrivilegedViewer(VDC vdc) {
        VDCRole vdcRole = getVDCRole(vdc);
        return  vdcRole!=null && vdcRole.getRole().getName().equals(RoleServiceLocal.PRIVILEGED_VIEWER);
    }

    /**
     *
     * @param vdc - the dataverse where we are checking the role
     * @return true  if this user has been assigned the contributor role in the VDC.
     *
     */
    public boolean isContributor(VDC vdc) {
        VDCRole vdcRole = getVDCRole(vdc);
        return  vdcRole!=null && vdcRole.getRole().getName().equals(RoleServiceLocal.CONTRIBUTOR);
    }


    /**
     *
     * @param vdc - the dataverse where we are checking the role
     * @return true if this user has been assigned the curator role in the VDC.
     *
     *  Simply a helper method - easier than calling getVDCRole() and checking the result.
     */
    public boolean isCurator(VDC vdc) {
        VDCRole vdcRole = getVDCRole(vdc);
        return  vdcRole!=null && vdcRole.getRole().getName().equals(RoleServiceLocal.CURATOR);
    }


    public boolean isNetworkAdmin() {
        return networkRole !=null && networkRole.getName().equals(NetworkRoleServiceLocal.ADMIN);
    }

    /**
     *
     * @param vdc - the dataverse where we are checking the role
     * @return true if this user has created, or been assigned the curator role in the 
     * dataverse.
     *
     *  Simply a helper method - easier than calling getVDCRole() and checking the result.
     */
    public boolean isAdmin(VDC vdc) {
        VDCRole vdcRole = getVDCRole(vdc);
        return  vdcRole!=null && vdcRole.getRole().getName().equals(RoleServiceLocal.ADMIN);
    }

    /**
     *
     * The network-wide version of isAdmin()
     * @return true if this user has created a dataverse, or been assigned the admin role 
     * for a dataverse anywhere on the network. 
     *
     *  Simply a helper method - easier than calling getVDCRole() and checking the result.
     */
    public boolean isAdmin() {
        for (Iterator it = vdcRoles.iterator(); it.hasNext();) {
            VDCRole elem = (VDCRole) it.next();
            if (elem != null && elem.getRole() != null) {
                if (RoleServiceLocal.ADMIN.equals(elem.getRole().getName())) {
                    return true;
                }
            }
        }
        return false; 
    }

    /**
     * Setter for property vdcRoles.
     * @param vdcRoles New value of property vdcRoles.
     */
    public void setVdcRoles(Collection<VDCRole> vdcRoles) {
        this.vdcRoles = vdcRoles;
    }

    public Collection<UserGroup> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(Collection<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }



    /**
     * Setter for property id.
     * @param long New value of property id.
     */
    public void setLong(Long id) {
        this.id = id;
    }

    private int test;

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
     * Holds value of property studies.
     */
      @ManyToMany(mappedBy="allowedUsers", cascade={CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST })
    private Collection<Study> studies;

    /**
     * Getter for property studies.
     * @return Value of property studies.
     */
    public Collection<Study> getStudies() {
        return this.studies;
    }

    /**
     * Setter for property studies.
     * @param studies New value of property studies.
     */
    public void setStudies(Collection<Study> studies) {
        this.studies = studies;
    }
 
  /**   
     * Holds value of property studyFiles.
     */
    @ManyToMany(mappedBy="allowedUsers", cascade={CascadeType.REMOVE }) 
    private Collection<StudyFile> studyFiles;

    /**
     * Getter for property studyFiles.
     * @return Value of property studyFiles.
     */
    public Collection<StudyFile> getStudyFiles() {
        return this.studyFiles;
    }

    /**
     * Setter for property studyFiles.
     * @param studyFiles New value of property studyFiles.
     */
    public void setStudyFiles(Collection<StudyFile> studyFiles) {
        this.studyFiles = studyFiles;
    }   
 
  

    /**
     * Holds value of property institution.
     */
    private String institution;

    /**
     * Getter for property institution.
     * @return Value of property institution.
     */
    public String getInstitution() {
        return this.institution;
    }

    /**
     * Setter for property institution.
     * @param institution New value of property institution.
     */
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    /**
     * Holds value of property position.
     */
    private String position;

    /**
     * Getter for property position.
     * @return Value of property position.
     */
    public String getPosition() {
        return this.position;
    }

    /**
     * Setter for property position.
     * @param position New value of property position.
     */
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * Holds value of property phoneNumber.
     */
    private String phoneNumber;

    /**
     * Getter for property phoneNumber.
     * @return Value of property phoneNumber.
     */
    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    /**
     * Setter for property phoneNumber.
     * @param phoneNumber New value of property phoneNumber.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public edu.harvard.iq.dvn.core.admin.NetworkRole getNetworkRole() {
        return networkRole;
    }

    public void setNetworkRole(edu.harvard.iq.dvn.core.admin.NetworkRole networkRole) {
        this.networkRole = networkRole;
    }

    /**
     * Holds value of property vdcNetwork.
     */
    @OneToOne(mappedBy="defaultNetworkAdmin")
    private VDCNetwork vdcNetwork;

    /**
     * Getter for property vdcNetwork.
     * @return Value of property vdcNetwork.
     */
    public VDCNetwork getVdcNetwork() {
        return this.vdcNetwork;
    }

    /**
     * Setter for property vdcNetwork.
     * @param vdcNetwork New value of property vdcNetwork.
     */
    public void setVdcNetwork(VDCNetwork vdcNetwork) {
        this.vdcNetwork = vdcNetwork;
    }

    public Collection<NetworkRoleRequest> getNetworkRoleRequests() {
        return networkRoleRequests;
    }

    public void setNetworkRoleRequests(Collection<NetworkRoleRequest> networkRoleRequests) {
        this.networkRoleRequests = networkRoleRequests;
    }

    /**
     * Holds value of property active.
     */
    private boolean active;

    /**
     * Getter for property active.
     * @return Value of property active.
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Setter for property active.
     * @param active New value of property active.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    public List<StudyLock> getStudyLocks() {
        return studyLocks;
    }

    public void setStudyLocks(List<StudyLock> studyLocks) {
        this.studyLocks = studyLocks;
    }

    public boolean isAgreedTermsOfUse() {
        return agreedTermsOfUse;
    }

    public void setAgreedTermsOfUse(boolean agreedTermsOfUse) {
        this.agreedTermsOfUse = agreedTermsOfUse;
    }
    
    public boolean isBypassTermsOfUse() {
        return bypassTermsOfUse; 
    }
    
    public void setBypassTermsOfUse(Boolean bypass) {
        this.bypassTermsOfUse = bypass; 
    }
    
     public int hashCode() {
        int hash = 0;
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof VDCUser)) {
            return false;
        }
        VDCUser other = (VDCUser)object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) return false;
        return true;
    } 
    
    @ManyToMany(mappedBy="allowedFileUsers")
    @OrderBy("name ASC")
    private java.util.List<VDC> allowedFileVdcs;

    /**
     * Getter for property memberVdcs.
     * @return Value of property memberVdcs.
     */
    public java.util.List<VDC> getAllowedFileVdcs() {
        return this.allowedFileVdcs;
    }

    /**
     * Setter for property memberVdcs.
     * @param memberVdcs New value of property memberVdcs.
     */
    public void setAllowedFileVdcs(java.util.List<VDC> memberVdcs) {
        this.allowedFileVdcs = memberVdcs;
    }

    /**
     * @return the studyComments
     */
    public Collection<StudyComment> getStudyComments() {
        return studyComments;
    }

    /**
     * @param studyComments the studyComments to set
     */
    public void setStudyComments(List<StudyComment> studyComments) {
        this.studyComments = studyComments;
    }

    /**
     * @return the flaggedStudyComments
     */
    @ManyToMany(mappedBy ="flaggedByUsers")
    public List<StudyComment> getFlaggedStudyComments() {
        return flaggedStudyComments;
    }

    /**
     * @param flaggedStudyComments the flaggedStudyComments to set
     */
    public void setFlaggedStudyComments(List<StudyComment> flaggedStudyComments) {
        this.flaggedStudyComments = flaggedStudyComments;
    }
        
}
