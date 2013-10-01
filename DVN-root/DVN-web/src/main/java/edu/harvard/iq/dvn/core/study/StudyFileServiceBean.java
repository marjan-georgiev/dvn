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

import edu.harvard.iq.dvn.core.admin.UserServiceLocal;
import edu.harvard.iq.dvn.core.admin.VDCUser;
import edu.harvard.iq.dvn.core.analysis.NetworkDataServiceBean;
import edu.harvard.iq.dvn.core.mail.MailServiceLocal;
import edu.harvard.iq.dvn.core.util.FileUtil;
import edu.harvard.iq.dvn.core.web.util.MD5Checksum;
import edu.harvard.iq.dvn.ingest.dsb.DSBIngestMessage;
import edu.harvard.iq.dvn.ingest.dsb.DSBWrapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.commons.io.FileUtils;
import java.util.zip.*;

/**
 *
 * @author gdurand
 */
@Stateless
public class StudyFileServiceBean implements StudyFileServiceLocal {

    @PersistenceContext(unitName = "VDCNet-ejbPU")
    EntityManager em;
    @Resource(mappedName = "jms/DSBIngest")
    Queue queue;
    @Resource(mappedName = "jms/DSBQueueConnectionFactory")
    QueueConnectionFactory factory;


    @EJB UserServiceLocal userService;
    @EJB MailServiceLocal mailService;
    @EJB StudyServiceLocal studyService;

    private static final Logger logger = Logger.getLogger("edu.harvard.iq.dvn.core.study.StudyFileServiceBean");


    public StudyFile getStudyFile(Long fileId) {
        StudyFile file = em.find(StudyFile.class, fileId);
        if (file == null) {
            throw new IllegalArgumentException("Unknown studyFileId: " + fileId);
        }


        return file;
    }

    public StudyFile lookupStudyFile(Long fileId) {
       
        StudyFile file = null; 
        try {
            file = em.find(StudyFile.class, fileId);
        } catch (Exception ex) {
            logger.fine("StudyFileService: caught exception; returning null.");
            return null; 
        }

        return file;
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<FileMetadata> getStudyFilesByExtension(String extension) {

        String queryStr = "SELECT fm from FileMetadata sf where lower(sf.fileName) LIKE :extension";

        Query query = em.createQuery(queryStr);
        query.setParameter("extension", "%." + extension.toLowerCase());

        List<FileMetadata> fmdList = query.getResultList();
        Iterator it = fmdList.iterator();
        while (it.hasNext()) {
            FileMetadata fmd = (FileMetadata) it.next();
            if (!fmd.getStudyVersion().isLatestVersion()) {
                it.remove();
            }
        }
        return fmdList;

    }

    public void updateStudyFile(StudyFile detachedStudyFile) {
        em.merge(detachedStudyFile);
    }



    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public java.util.List<Object[]> getOrderedFilesByStudy(Long studyId) {
  
        /*
         * This query returns one row per filemetadata for each of the studyfiles belonging to
         * this study, ordered by the category
         */
        
        String nativeQuery = "select studyfile_id, fmd.id, sv.versionnumber " +
             "from filemetadata fmd, studyversion sv " + 
             "where fmd.studyversion_id = sv.id " +
             "and sv.study_id = "+ studyId +
             "order by  UPPER(fmd.category), studyfile_id, versionNumber desc";
        Query query = em.createNativeQuery(nativeQuery);
        
       
        return  convertIntegerToLong(query.getResultList(),1);
    }
    
    

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public java.util.List<FileMetadata> getOrderedFilesByStudyVersion (Long svId) {
        // Note: This ordering is case-sensitive, so names beginning with upperclass chars will appear first.
        // (I tried using UPPER(f.name) to make the sorting case-insensitive, but the EJB query language doesn't seem
        // to like this.)
        String queryStr = "SELECT f FROM FileMetadata f  WHERE f.studyVersion.id = " + svId + " ORDER BY f.category, f.label";
        Query query = em.createQuery(queryStr);
        List<FileMetadata> studyFiles = query.getResultList();
   //     for (StudyFile sf: studyFiles) {
   //         sf.getDataTables().size();
   //     }
        return studyFiles;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public java.util.List<FileMetadata> getFilesByStudyVersionOrderedById (Long svId) {
        String queryStr = "SELECT f FROM FileMetadata f  WHERE f.studyVersion.id = " + svId + " ORDER BY f.studyFile.id";
        Query query = em.createQuery(queryStr);
        List<FileMetadata> studyFiles = query.getResultList();

        return studyFiles;
    }
    
    /*@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List getOrderedFileIdsByStudyVersion (Long svId) {
	List<Long> fileIdList = new ArrayList<Long>();

        String queryStr = "SELECT f.id FROM FileMetadata f WHERE f.studyVersion_id = " + svId + " ORDER BY f.category, f.label";
        Query query = em.createNativeQuery(queryStr);
        for (Object currentResult : query.getResultList()) {
            fileIdList.add(new Long(((Integer)currentResult)).longValue());
        }
  
        return fileIdList;
    }*/

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)    
    public Long getCountFilesByStudyVersion(Long svId) {
        String queryString = "SELECT count(f.*) FROM FileMetadata f WHERE f.studyVersion_id = " + svId +" "; 
        Query query = em.createNativeQuery(queryString);        
        Long retVal = (Long) query.getSingleResult();
        return retVal; 
    }
    
    /*@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List getOrderedFileIdsByStudyVersion (Long svId) {
	List<Long> fileIdList = new ArrayList<Long>();

        String queryStr = "SELECT f.id FROM FileMetadata f WHERE f.studyVersion_id = " + svId + " ORDER BY f.category, f.label";
        Query query = em.createNativeQuery(queryStr);
        for (Object currentResult : query.getResultList()) {
            fileIdList.add(new Long(((Integer)currentResult)).longValue());
        }
  
        return fileIdList;
    }*/

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)    
    public List<Long> getOrderedFileIdsByStudyVersion(Long svId) {
        List<FileIdCategory> fileIdCatList = new ArrayList();
        String queryString = "SELECT f.id, f.category FROM FileMetadata f WHERE f.studyVersion_id = " + svId + " ORDER BY f.category, f.label"; 
        Query query = em.createNativeQuery(queryString);
        
        for ( Object result : query.getResultList()) {
            Object[] resultArray = (Object[]) result;
            FileIdCategory idCat = new FileIdCategory();
            idCat.setId( new Long( (Integer) resultArray[0] ) );
            idCat.setCategory( (String) resultArray[1] );
            fileIdCatList.add(idCat);      
        }
        
        Collections.sort(fileIdCatList);
        
        List<Long> fileIdList = new ArrayList<Long>();
        for (FileIdCategory idCat : fileIdCatList) {
            fileIdList.add(idCat.getId());
        } 
            
        return fileIdList; 
    }
           
    public Map<Long,FileMetadata> getFilesByStudyVersionAndIds(Long svId, List<Long> fileIdList) {
        Map fileMap = new HashMap();
                
        if (fileIdList == null || fileIdList.size() == 0) {
            return fileMap;
        }
        
        String fileIds = idListString(fileIdList);
        String queryStr = "SELECT f FROM FileMetadata f WHERE f.studyVersion.id = " + svId + " and f.id IN  (" + fileIds + ")";
        Query query = em.createQuery(queryStr);
        List<FileMetadata> studyFiles = query.getResultList();
        
        for (FileMetadata fmd : studyFiles) {
            fileMap.put(fmd.getId(), fmd);
        }
        
        return fileMap;
    }
    
    private String idListString(List idList) {
        StringBuffer sb = new StringBuffer();
        Iterator iter = idList.iterator();
        while (iter.hasNext()) {
            Long id = (Long) iter.next();
            sb.append(id);
            if (iter.hasNext()) {
                sb.append(",");
            }
        }

        return sb.toString();
    }
    
    public List findAllFileIdsSearch(Long studyId, String searchTerm) {
        String lowerSearchString = searchTerm.toLowerCase();
        List fileList = new ArrayList();
        String nativeQuery = "select sf1.id from filemetadata fm1, studyfile sf1 " +
            " where lower(fm1.label) like '%" + lowerSearchString.replaceAll("'", "''") +
            "%' and fm1.studyfile_id = sf1.id " +
            " and  fm1.id in " +
            " (select max(fm.id) " +
            " from studyfile sf, study s, filemetadata fm, studyversion sv " +
            " where sf.study_id = s.id " +
            " and fm.studyfile_id = sf.id " +
            " and sv.study_id = s.id " +
            " and s.id = "+ studyId +
            " group by sf.id) " +
            " order by  UPPER(fm1.category)"; 
        Query query = em.createNativeQuery(nativeQuery);
        for (Object currentResult : query.getResultList()) {
            fileList.add(new Long(((Integer)currentResult).longValue()));
        }
        return fileList;
    } 

    public Boolean doesStudyHaveSubsettableFiles(Long studyVersionId) {
        List<String> subsettableList = new ArrayList();
        Query query = em.createNativeQuery("select fileclass from studyfile sf, filemetadata fmd where fmd.studyfile_id = sf.id and studyversion_id = " + studyVersionId);
        for (Object currentResult : query.getResultList()) {
            subsettableList.add( (String)currentResult );
        }

        if ( !subsettableList.isEmpty() ) {
            for (String fclass : subsettableList) {
                if ("TabularDataFile".equals(fclass) || "NetworkDataFile".equals(fclass))
                    return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
            
        // It appears (according to Gustavo) that the method does return null 
        // instead of a Boolean here ON PURPOSE! So leave it be, just be careful
        // to check for nullness if you're using this. 
        return null; 


    }

    public Boolean doesStudyHaveTabularFiles(Long studyVersionId) {
        List<String> subsettableList = new ArrayList();
        Query query = em.createNativeQuery("select fileclass from studyfile sf, filemetadata fmd where fmd.studyfile_id = sf.id and studyversion_id = " + studyVersionId);
        for (Object currentResult : query.getResultList()) {
            subsettableList.add( (String)currentResult );
        }

        if ( !subsettableList.isEmpty() ) {
            for (String fclass : subsettableList) {
                if ("TabularDataFile".equals(fclass))
                    return Boolean.TRUE;
            }

        }

        return Boolean.FALSE;

    }
    
    public Boolean doesStudyHaveSingleTabularFiles(Long studyVersionId) {

        List<Long> subsettableList = new ArrayList();
        Query query = em.createNativeQuery("SELECT count(d.*) FROM  DataTable d, studyfile sf,  studyversion sv where d.studyfile_id = sf.id and sf.study_id = sv.study_id and sv.id = " + studyVersionId + " group by studyfile_id");
        for (Object currentResult : query.getResultList()) {
            subsettableList.add( (Long)currentResult );
        }


        if ( !subsettableList.isEmpty() ) {
            for (Long fclass : subsettableList) {
                if (fclass.intValue() == 1){
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;

    }

    public void addFiles(StudyVersion studyVersion, List<StudyFileEditBean> newFiles, VDCUser user) {
        addFiles(studyVersion, newFiles, user, user.getEmail(), DSBIngestMessage.INGEST_MESAGE_LEVEL_ERROR);
    }

    public void addFiles(StudyVersion studyVersion, List<StudyFileEditBean> newFiles, VDCUser user, String ingestEmail) {
        addFiles(studyVersion, newFiles, user, ingestEmail, DSBIngestMessage.INGEST_MESAGE_LEVEL_INFO);
    }

    private void addFiles(StudyVersion studyVersion, List<StudyFileEditBean> newFiles, VDCUser user, String ingestEmail, int messageLevel) {

        Study study = studyVersion.getStudy();
        MD5Checksum md5Checksum = new MD5Checksum();

        // step 1: divide the files, based on subsettable or not
        List subsettableFiles = new ArrayList();
        List otherFiles = new ArrayList();

        Iterator iter = newFiles.iterator();
        while (iter.hasNext()) {
            StudyFileEditBean fileBean = (StudyFileEditBean) iter.next();
            // Note that for the "special" OtherFiles we want to utilize the 
            // same ingest scheme as for subsettables: they will be queued and 
            // processed asynchronously, and the user will be notified by email.
            // - L.A.
            if (fileBean.getStudyFile().isSubsettable() || fileBean.getStudyFile() instanceof SpecialOtherFile) {
                subsettableFiles.add(fileBean);
            } else {
                otherFiles.add(fileBean);
                // also add to study, so that it will be flushed for the ids
                fileBean.getStudyFile().setStudy(study);
                study.getStudyFiles().add(fileBean.getStudyFile());

            }
        }

        if (otherFiles.size()>0) {
             // Only persist the studyVersion we are adding a file that doesn't need to be ingested (non-subsettable)
            if (studyVersion.getId() == null) {
                em.persist(studyVersion);
                em.flush(); // populates studyVersion_id
            } else {
                // There is a problem merging the existing studyVersion,
                // so since all we need from the exisiting version is the versionNote,
                // we get a fresh copy of the object from the database, and update it with the versionNote.
                String versionNote = studyVersion.getVersionNote();
                studyVersion = em.find(StudyVersion.class, studyVersion.getId());
                studyVersion.setVersionNote(versionNote);
            }

        }

        // step 2: iterate through nonsubsettable files, moving from temp to new location
        File newDir = FileUtil.getStudyFileDir(study);
        iter = otherFiles.iterator();
        while (iter.hasNext()) {
            StudyFileEditBean fileBean = (StudyFileEditBean) iter.next();
            StudyFile f = fileBean.getStudyFile();
            File tempFile = new File(fileBean.getTempSystemFileLocation());
            File newLocationFile = new File(newDir, f.getFileSystemName());
            try {
                FileUtil.copyFile(tempFile, newLocationFile);
                tempFile.delete();
                f.setFileSystemLocation(newLocationFile.getAbsolutePath());

                fileBean.getFileMetadata().setStudyVersion( studyVersion );

                em.persist(fileBean.getStudyFile());
                em.persist(fileBean.getFileMetadata());

            } catch (IOException ex) {
                throw new EJBException(ex);
            }
            f.setMd5(md5Checksum.CalculateMD5(f.getFileSystemLocation()));
        }

        // step 3: iterate through subsettable files, sending a message via JMS
        if (subsettableFiles.size() > 0) {
            QueueConnection conn = null;
            QueueSession session = null;
            QueueSender sender = null;
            try {
                conn = factory.createQueueConnection();
                session = conn.createQueueSession(false, 0);
                sender = session.createSender(queue);

                DSBIngestMessage ingestMessage = new DSBIngestMessage(messageLevel);
                ingestMessage.setFileBeans(subsettableFiles);
                ingestMessage.setIngestEmail(ingestEmail);
                ingestMessage.setIngestUserId(user.getId());
                ingestMessage.setStudyId(study.getId());
                ingestMessage.setStudyVersionId(studyVersion.getId());
                ingestMessage.setVersionNote(studyVersion.getVersionNote());

                ingestMessage.setStudyTitle( studyVersion.getMetadata().getTitle() );
                ingestMessage.setStudyGlobalId( studyVersion.getStudy().getGlobalId() );
                ingestMessage.setStudyVersionNumber( studyVersion.getVersionNumber().toString() );
                ingestMessage.setDataverseName( studyVersion.getStudy().getOwner().getName() );                 
                
                Message message = session.createObjectMessage(ingestMessage);

                String detail = "Ingest processing for " + subsettableFiles.size() + " file(s).";
                studyService.addStudyLock(study.getId(), user.getId(), detail);
                try {
                    sender.send(message);
                } catch (Exception ex) {
                    // If anything goes wrong, remove the study lock.
                    studyService.removeStudyLock(study.getId());
                    ex.printStackTrace();
                }

                // send an e-mail
                if (ingestMessage.sendInfoMessage()) {
                    mailService.sendIngestRequestedNotification(ingestMessage, subsettableFiles);
                }

            } catch (JMSException ex) {
                ex.printStackTrace();
            } finally {
                try {

                    if (sender != null) {
                        sender.close();
                    }
                    if (session != null) {
                        session.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (!otherFiles.isEmpty()) {
            studyService.saveStudyVersion(studyVersion, user.getId());
           
        }

       
    }


    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addIngestedFiles( Long studyId, String versionNote, List fileBeans, Long userId) {
        // if no files, then just return
        if (fileBeans.isEmpty()) {
            return;
        }

        // first some initialization
        StudyVersion studyVersion = null;
        Study study = null;
        MD5Checksum md5Checksum = new MD5Checksum();

        study = em.find(Study.class, studyId);
        studyVersion = study.getEditVersion();
        if(studyVersion.getId()==null) {
            em.persist(studyVersion);
            em.flush();
        }

        studyVersion.setVersionNote(versionNote);
        
        VDCUser user = userService.find(userId);

        File newDir = new File(FileUtil.getStudyFileDir(), study.getAuthority() + File.separator + study.getStudyId());
        if (!newDir.exists()) {
            newDir.mkdirs();
        }

        // now iterate through fileBeans
        Iterator iter = fileBeans.iterator();
        while (iter.hasNext()) {
            StudyFileEditBean fileBean = (StudyFileEditBean) iter.next();

            // for now the logic is if the DSB does not return a file, don't copy
            // over anything; this is to cover the situation with the Ingest servlet
            // that uses takes a control card file to add a dataTable to a prexisting
            // file; this will have to change if we do this two files method at the
            // time of the original upload
            // (TODO: figure out what this comment means - ? - L.A.)
            // (is this some legacy thing? - it's talking about "ingest servlet"...)
            // (did we ever have a mechanism for adding a data table to an existing
            //  tab file?? - that's actually kinda cool)
            
            StudyFile f = fileBean.getStudyFile();
            
            // So, if there is a file: let's move it to its final destination
            // in the study directory. 
            //
            // First, if it's a subsettable or network, or any other
            // kind potentially, that gets transformed on ingest: 
            
            File newIngestedLocationFile = null; 
            
            if (fileBean.getIngestedSystemFileLocation() != null) {

                String originalFileType = f.getFileType();
                

                // 1. move ingest-created file:
                
                File tempIngestedFile = new File(fileBean.getIngestedSystemFileLocation());
                newIngestedLocationFile = new File(newDir, f.getFileSystemName());
                try {
                    FileUtil.copyFile(tempIngestedFile, newIngestedLocationFile);
                    tempIngestedFile.delete();
                    if (f instanceof TabularDataFile ){
                        f.setFileType("text/tab-separated-values");
                    }
                    f.setFileSystemLocation(newIngestedLocationFile.getAbsolutePath());

                } catch (IOException ex) {
                    throw new EJBException(ex);
                }
                // 1b. If this is a NetworkDataFile,  move the SQLite file from the temp Ingested location to the system location
                if (f instanceof NetworkDataFile) {
                    File tempSQLDataFile = new File(tempIngestedFile.getParent(), FileUtil.replaceExtension(tempIngestedFile.getName(),NetworkDataServiceBean.SQLITE_EXTENSION));
                    File newSQLDataFile = new File(newDir, f.getFileSystemName()+"."+NetworkDataServiceBean.SQLITE_EXTENSION);

                    File tempNeo4jDir =  new File(tempIngestedFile.getParent(), FileUtil.replaceExtension(tempIngestedFile.getName(),NetworkDataServiceBean.NEO4J_EXTENSION));
                    File newNeo4jDir = new File(newDir, f.getFileSystemName()+"."+NetworkDataServiceBean.NEO4J_EXTENSION);
                    
                    try {
                        FileUtil.copyFile(tempSQLDataFile, newSQLDataFile);
                        FileUtils.copyDirectory(tempNeo4jDir, newNeo4jDir);
                        tempSQLDataFile.delete();
                        FileUtils.deleteDirectory(tempNeo4jDir);
                        f.setOriginalFileType(originalFileType);
                        
                    } catch (IOException ex) {
                        throw new EJBException(ex);
                    }
                }

                // 2. also move original file for archiving
                File tempOriginalFile = new File(fileBean.getTempSystemFileLocation());
                File newOriginalLocationFile = new File(newDir, "_" + f.getFileSystemName());
                try {
                    if (fileBean.getControlCardSystemFileLocation() != null && fileBean.getControlCardType() != null) {
                        // 2a. For the control card-based ingests (SPSS and DDI), we save
                        // a zipped bundle of both the card and the raw data file
                        // (TAB-delimited or CSV):
                                           
                        FileInputStream instream = null;
                        byte[] dataBuffer = new byte[8192];
                               
                        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(newOriginalLocationFile));
                        
                        // First, the control card:
                        
                        File controlCardFile = new File(fileBean.getControlCardSystemFileLocation());

                        ZipEntry ze = new ZipEntry(controlCardFile.getName());
                        instream = new FileInputStream(controlCardFile);
                        zout.putNextEntry(ze);
                        
                        int k = 0;
                        while ( ( k = instream.read (dataBuffer) ) > 0 ) {
                            zout.write(dataBuffer,0,k);
                            zout.flush(); 
                        }

                        instream.close();
                        
                        // And then, the data file:
                                                
                        ze = new ZipEntry(tempOriginalFile.getName());
                        instream = new FileInputStream(tempOriginalFile);
                        zout.putNextEntry(ze);
                                                                
                        while ( ( k = instream.read (dataBuffer) ) > 0 ) {
                            zout.write(dataBuffer,0,k);
                            zout.flush(); 
                        }

                        instream.close();
                    
                        zout.close();
                        
                        // and control card file can be deleted now:
                        controlCardFile.delete();
                        
                        // Mime types: 
                        // These are custom, made-up types, used to identify the 
                        // type of the source data:
                        
                        if (fileBean.getControlCardType().equals("spss")) {
                            f.setOriginalFileType("application/x-dvn-csvspss-zip");
                        } else if (fileBean.getControlCardType().equals("ddi")) {
                            f.setOriginalFileType("application/x-dvn-tabddi-zip");
                        } else {
                            logger.info("WARNING: unknown control card-based Ingest type? -- "+fileBean.getControlCardType());
                            f.setOriginalFileType(originalFileType);
                        }
                        f.setMd5(md5Checksum.CalculateMD5(tempOriginalFile.getAbsolutePath()));
                        
                    } else {
                        // 2b. Otherwise, simply store the data that was used for
                        // ingest as the original:

                        FileUtil.copyFile(tempOriginalFile, newOriginalLocationFile);
                        f.setOriginalFileType(originalFileType);
                        f.setMd5(md5Checksum.CalculateMD5(newOriginalLocationFile.getAbsolutePath()));
                    }
                    tempOriginalFile.delete();
                } catch (IOException ex) {
                    throw new EJBException(ex);
                }
            } else if (f instanceof SpecialOtherFile) {
            // "Special" OtherFiles are still OtherFiles; we just add the file
            // uploaded by the user to the study as is:
                
                File tempIngestedFile = new File(fileBean.getTempSystemFileLocation());
                newIngestedLocationFile = new File(newDir, f.getFileSystemName());
                try {
                    FileUtil.copyFile(tempIngestedFile, newIngestedLocationFile);
                    tempIngestedFile.delete();
                    f.setFileSystemLocation(newIngestedLocationFile.getAbsolutePath());
                    f.setMd5(md5Checksum.CalculateMD5(newIngestedLocationFile.getAbsolutePath()));
                } catch (IOException ex) {
                    throw new EJBException(ex);
                }
            }
            
            // Finally, if the file was copied sucessfully, 
            // attach file to study version and study
            
            if (newIngestedLocationFile != null && newIngestedLocationFile.exists()) {
                
                fileBean.getFileMetadata().setStudyVersion( studyVersion );
                studyVersion.getFileMetadatas().add(fileBean.getFileMetadata());
                fileBean.getStudyFile().setStudy(study );
                // don't need to set study side, since we're no longer using persistence cache
                //study.getStudyFiles().add(fileBean.getStudyFile());
                //fileBean.addFiletoStudy(study);

                em.persist(fileBean.getStudyFile());
                em.persist(fileBean.getFileMetadata());
                
            } else {
                //fileBean.getStudyFile().setSubsettable(true);
                em.merge(fileBean.getStudyFile());
            }
        }
        // calcualte UNF for study version
        try {
            studyVersion.getMetadata().setUNF(new DSBWrapper().calculateUNF(studyVersion));
        } catch (IOException e) {
            throw new EJBException("Could not calculate new study UNF");
        }

        studyService.saveStudyVersion(studyVersion, user.getId());
    }


        private List<Object[]> convertIntegerToLong(List<Object[]> list, int index) {
        for (Object[] item : list) {
            item[index] = new Long( (Integer) item[index]);
        }
           
        return list;
    } 
}
