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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.study;

import edu.harvard.iq.dvn.core.admin.VDCUser;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 *
 * @author gdurand
 */
@Local
public interface StudyFileServiceLocal {


    public StudyFile getStudyFile(Long fileId);
    public StudyFile lookupStudyFile(Long fileId);

    public List<FileMetadata> getStudyFilesByExtension(String extension);
    public void updateStudyFile(StudyFile detachedStudyFile);

    java.util.List<Object[]> getOrderedFilesByStudy(Long studyId);
    java.util.List<FileMetadata> getOrderedFilesByStudyVersion(Long svId);
    java.util.List<FileMetadata> getFilesByStudyVersionOrderedById(Long svId);
    java.util.List<Long> getOrderedFileIdsByStudyVersion (Long svId);
    Map<Long,FileMetadata> getFilesByStudyVersionAndIds(Long svId, List<Long> fileIdList);
    java.util.List<Long> findAllFileIdsSearch(Long studyId, String searchTerm);

    public Boolean doesStudyHaveSubsettableFiles(Long studyVersionId);
    public Boolean doesStudyHaveTabularFiles(Long studyVersionId);
    public Boolean doesStudyHaveSingleTabularFiles(Long studyVersionId);


    public void addFiles(StudyVersion studyVersion, List<StudyFileEditBean> newFiles, VDCUser user);
    public void addFiles(StudyVersion studyVersion, List<StudyFileEditBean> newFiles, VDCUser user, String ingestEmail);
    public void addIngestedFiles(Long studyId, String versionNote, List fileBeans, Long userId);

    @javax.ejb.TransactionAttribute(value = javax.ejb.TransactionAttributeType.NOT_SUPPORTED)
    public java.lang.Long getCountFilesByStudyVersion(java.lang.Long svId);


    
}
