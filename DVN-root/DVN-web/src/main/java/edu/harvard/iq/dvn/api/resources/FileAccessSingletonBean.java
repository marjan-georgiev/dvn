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
package edu.harvard.iq.dvn.api.resources;


import edu.harvard.iq.dvn.api.entities.DownloadInfo; 


import java.util.List;
import javax.ejb.Singleton;
import javax.ejb.EJB;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

import edu.harvard.iq.dvn.core.study.StudyFileServiceLocal;
import edu.harvard.iq.dvn.core.study.StudyServiceLocal;
import edu.harvard.iq.dvn.core.admin.UserServiceLocal;
import edu.harvard.iq.dvn.core.vdc.VDCNetworkServiceLocal;
//import edu.harvard.iq.dvn.core.study.Study;
import edu.harvard.iq.dvn.core.study.StudyFile;
import edu.harvard.iq.dvn.core.study.StudyVersion; 
import edu.harvard.iq.dvn.core.study.DataFileFormatType;
import edu.harvard.iq.dvn.core.study.FileMetadata; 

import edu.harvard.iq.dvn.core.admin.VDCUser; 
import edu.harvard.iq.dvn.core.web.dataaccess.OptionalAccessService;

import edu.harvard.iq.dvn.core.util.FileUtil;
import edu.harvard.iq.dvn.core.util.StringUtil;



/**
 *
 * @author leonidandreev
 */
@Singleton
public class FileAccessSingletonBean {
    @EJB private StudyFileServiceLocal studyFileService; 
    @EJB private UserServiceLocal userService;
    @EJB private StudyServiceLocal studyService; 
    @EJB private VDCNetworkServiceLocal vdcNetworkService;
    
    private List<DataFileFormatType> allSupportedTypes = null; 
    
    public FileAccessSingletonBean() {
    }
    
    // Looks up StudyFile by (database) ID:
    
    public DownloadInfo getDownloadInfo(Long studyFileId) {
        return getDownloadInfo(studyFileId, null);
    }
    
    public DownloadInfo getDownloadInfo(Long studyFileId, String authCredentials) {
        DownloadInfo di = null; 
        StudyFile sf = null; 
        VDCUser authenticatedUser = null; 
        
        if (studyFileId != null) {
            try {
                sf = studyFileService.lookupStudyFile(studyFileId);
                if (sf != null) {
                    // Check if the file is part of a released study AND a 
                    // released Dataverse; (note that the "released" status 
                    // is actually called "restricted" on the dataverse level!)
                    if (!sf.getStudy().isReleased() 
                            || sf.getStudy().getOwner().isRestricted()) {
                        // if not - we are simply going to say "NOT FOUND!"
                        return null; 
                    }
                    // Similarly, if the file no longer belongs to the released
                    // version of the study, we are going to pretend it does 
                    // not exist either: 
                    StudyVersion releasedVersion = sf.getStudy().getReleasedVersion();
                    
                    if (releasedVersion != null) {
                        List<FileMetadata> fileList = releasedVersion.getFileMetadatas();
                        Boolean inReleasedVersion = false; 
                        int i = 0;
                        while (i < fileList.size() && !inReleasedVersion) {
                            FileMetadata fileMD = fileList.get(i);
                            if (fileMD.getStudyFile().getId().compareTo(sf.getId()) == 0) {
                                inReleasedVersion = true; 
                            }
                            i++;
                        }
                        
                        if (!inReleasedVersion) {
                            return null; 
                        }
                    } else {
                        return null; 
                    }
                    
                    di = new DownloadInfo (sf);
                    if (di == null) {
                        return null; 
                    }
                } else {
                    return null; 
                }
            } catch (Exception ex) {
                return null; 
                // We don't care much what happened - but for whatever 
                // reason, we haven't been able to look up the file.
                // It could have been a study file that does not exist; 
                // or a null pointer exception, because the study
                // file isn't referenced by a valid study, etc. 
                //
                // We don't need to do anything special here -- we simply
                // return null, and Jersey app will cook a proper 404 
                // response. 
            } 
            
            // Let's try to authenticate: 
            
            if (authCredentials != null) {
                di.setAuthMethod("password");
                authenticatedUser = authenticateAccess(authCredentials);
                if (authenticatedUser != null) {
                    di.setAuthUserName(authenticatedUser.getUserName());
                }
            } else {
                di.setAuthMethod("anonymous");
            }
            
            // And authorization: 
            // 1st, for Access permissions: 
            
            if (checkAccessPermissions(authenticatedUser, di)) {
                di.setPassAccessPermissions(true);
            }
            
            // and then, for any Access Restrictions (Terms of Use)
            if (checkAccessRestrictions(authenticatedUser, di)) {
                di.setPassAccessRestrictions(true);
            }
            
            // Add optional services, if available: 
            
            String fileMimeType = sf.getFileType();
            
            // Image Thumbnail:
            
            if (fileMimeType != null && fileMimeType.startsWith("image/")) {
                di.addServiceAvailable(new OptionalAccessService("thumbnail", "image/png", "imageThumb=true", "Image Thumbnail (64x64)"));
            }
            
            // Services for subsettable files: 
            
            if (sf.isSubsettable()) {
                // Subsetting: (TODO: separate auth)
                // Not supported in this release?
                //di.addServiceAvailable(new OptionalAccessService("subset", "text/tab-separated-values", "variables=&lt;LIST&gt;", "Column-wise Subsetting"));
            
                // "saved original" file, if available: 
                
                String originalFormatType = sf.getOriginalFileType();
                String userFriendlyOriginalFormatName = null;

                if ( !StringUtil.isEmpty( originalFormatType ) ) {

                    userFriendlyOriginalFormatName = FileUtil.getUserFriendlyOriginalType(sf);
                    String originalTypeLabel = "";

                    if (!StringUtil.isEmpty(userFriendlyOriginalFormatName)) {
                        originalTypeLabel = userFriendlyOriginalFormatName;
                    } else {
                        originalTypeLabel = originalFormatType;
                    }

                    String originalFileDesc = "Saved original (" + originalTypeLabel + ")";
                    String originalFileServiceArg = "fileFormat=original";
                    
                    di.addServiceAvailable(new OptionalAccessService(
                            "original", 
                            originalFormatType, 
                            originalFileServiceArg, 
                            originalFileDesc));
                }

            
                // "No variable header" download
                
                di.addServiceAvailable(new OptionalAccessService("dataonly", "text/tab-separated-values", "noVarHeader=true", "Data only, no variable header"));
                
                // Finally, conversion formats: 
                
                if (allSupportedTypes == null) {
                    allSupportedTypes = studyService.getDataFileFormatTypes();
                }
                
                for (DataFileFormatType dft : allSupportedTypes) {
                    if (originalFormatType == null ||
                            !originalFormatType.equals(dft.getMimeType()) ) {
                        
                        String formatServiceArg = "fileFormat="+dft.getValue();
                        String formatDescription = "Data in "+ dft.getName() + " format (generated)";
                        di.addServiceAvailable(new OptionalAccessService(
                            dft.getName(), 
                            dft.getMimeType(), 
                            formatServiceArg, 
                            formatDescription));
                    }
                }
            } 
            
            // Finally, Terms of Use (experimental)
            
            if (di.isAccessRestrictionsApply()) {
                di.addServiceAvailable(new OptionalAccessService("termsofuse", "text/plain", "TermsOfUse=true", "Terms of Use/Access Restrictions associated with the data file"));
                di.addServiceAvailable(new OptionalAccessService("bundleTOU", "application/zip", "package=WithTermsOfUse", "Data File and the Terms of Use in a Zip archive"));
            }
        } 
        
        return di; 
    }
    
    // Decodes the Base64 credential string (passed with the request in 
    // the "Authenticate: " header), extracts the username and password, 
    // and attempts to authenticate the user with the DVN User Service. 
    
    public VDCUser authenticateAccess (String authCredentials) {
        VDCUser vdcUser = null;
        Base64 base64codec = new Base64(); 
        
        String decodedCredentials = ""; 
        byte[] authCredBytes = authCredentials.getBytes();
        
        try {
            byte[] decodedBytes = base64codec.decode(authCredBytes);
            decodedCredentials = new String (decodedBytes, "ASCII");
        } catch (UnsupportedEncodingException e) {
            return null; 
        }

        if (decodedCredentials != null ) {
            int i = decodedCredentials.indexOf(':');
            if (i != -1) { 
                String userPassword = decodedCredentials.substring(i+1);
                String userName = decodedCredentials.substring(0, i);
                
                if (!"".equals(userName)) {
                    vdcUser = userService.findByUserName(userName, true);
                    if (vdcUser == null || 
                        !userService.validatePassword(vdcUser.getId(),userPassword)) {
                        return null;
                    } 
                }
            }
        } 
        
        return vdcUser; 
    }
    
    // Access Permissions:
    
    private Boolean checkAccessPermissions (VDCUser vdcUser, DownloadInfo di) {
        StudyFile studyFile = di.getStudyFile();
        
        if (studyFile == null) {
            return false; 
        }
 
        // file is 
        if (!isPublicAccess(studyFile)) {
            di.setAccessPermissionsApply(true);
        } else {
            return true; 
        }
        
        if (studyFile.isFileRestrictedForUser(vdcUser, null)) {
            return false; 
        }
        
        return true; 
    }
    
    
    // Access Restrictions: (Terms of Use)
    
    private Boolean checkAccessRestrictions (VDCUser vdcUser, DownloadInfo di) {
        StudyFile studyFile = di.getStudyFile();        
        
        if (studyFile == null) {
            return false; 
        }
        
        if (!isUnderTermsOfUse(studyFile)) {
            return true; 
        }       
        
        di.setAccessRestrictionsAply(true);
        
        if (vdcUser == null) {
            return false; 
        }
            
        // Finally, heck if the user is authorized to be responsible for the 
        // enforcement of the Terms of use:
        
        if (vdcUser.isBypassTermsOfUse()) {
            return true; 
        }
        
        return false; 

    }
  
    // Check if this file is freely available ("public")
    // note that restrictions can apply on multiple levels. 
    
    private Boolean isPublicAccess (StudyFile studyFile) {
        // Restrictions on the StudyFile itself: 
        if (studyFile.isRestricted()) {
            return false; 
        }
        
        if (studyFile.getStudy() != null) {
            // Study-level restrictions: 
            if (studyFile.getStudy().isRestricted()) {
                return false; 
            }
            // And Dataverse-level: 
            if (studyFile.getStudy().getOwner() != null) {
                // Note that there are 2 ways in which access to the study file
                // can be "restricted" on the DV level:  
                // it can be either through the "Files Restricted" setting, and 
                // this is what we are checking here; or the Dataverse itself 
                // can be "restricted" - but that must be just a legacy name, 
                // and what it really means is more like the equivalent of 
                // the "unrleleased" status on the study level. 
                if (studyFile.getStudy().getOwner().isFilesRestricted()) {
                //         || studyFile.getStudy().getOwner().isRestricted()) {
                    return false; 
                }
            }
        }
        
        return true; 
    }
    
    // Check if any Terms of Use apply
    
    private Boolean isUnderTermsOfUse (StudyFile studyFile) {
        if (studyFile == null) {
            return false; 
        }
        
        // There are multiple levels on which Terms of Use can apply:
        
        if (studyFile.getStudy() != null) {
 
            // Network level: 
            if (vdcNetworkService.find().isDownloadTermsOfUseEnabled()) {
                return true; 
            }
            
            // Dataverse level: 
            if (studyFile.getStudy().getOwner() != null) {
                if (studyFile.getStudy().getOwner().isDownloadTermsOfUseEnabled()) {
                    return true; 
                }
            }
        
            if (studyFile.getStudy().getReleasedVersion() != null &&
                    studyFile.getStudy().getReleasedVersion().getMetadata() != null) {
                if (studyFile.getStudy().getReleasedVersion().getMetadata().isTermsOfUseEnabled()) {
                    return true; 
                }
            }
        } 
        
        return false; 
    }
    
    
}
