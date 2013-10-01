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
 * VDCNetworkServiceBean.java
 *
 * Created on October 26, 2006, 11:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.vdc;

import edu.harvard.iq.dvn.core.admin.DvnTimerLocal;
import edu.harvard.iq.dvn.core.mail.MailServiceLocal;
import edu.harvard.iq.dvn.core.study.StudyServiceLocal;
import edu.harvard.iq.dvn.core.study.StudyVersion;
import edu.harvard.iq.dvn.core.study.Template;
import edu.harvard.iq.dvn.core.study.TemplateServiceLocal;
import edu.harvard.iq.dvn.core.util.StringUtil;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author roberttreacy
 */
@Stateless
public class VDCNetworkServiceBean implements VDCNetworkServiceLocal {

    @PersistenceContext(unitName="VDCNet-ejbPU")
    private EntityManager em;
    @Resource javax.ejb.TimerService timerService;
    @EJB MailServiceLocal mailService;
    @EJB StudyServiceLocal studyService;
    @EJB VDCNetworkServiceLocal vdcNetworkService; 
    @EJB TemplateServiceLocal templateService;
    @EJB VDCServiceLocal vdcService;

    @EJB
    DvnTimerLocal dvnTimerService;

    private static final Logger logger = Logger.getLogger("edu.harvard.iq.dvn.core.vdc.VDCNetworkServiceBean");    
    /**
     * Creates a new instance of VDCNetworkServiceBean
     */
    public VDCNetworkServiceBean() {
    }

    public void create(VDCNetwork vDCNetwork) {
        em.persist(vDCNetwork);
    }
    

    public void edit(VDCNetwork vDCNetwork) {
        em.merge(vDCNetwork);
    }

    public void destroy(VDCNetwork vDCNetwork) {
        // vDCNetwork is detatched (can't be removed)
        // merge returns the *managed* instance that the state was merged to
        VDCNetwork networkToDelete = em.merge(vDCNetwork);
        // entity must be managed (not detatched) to remove
        em.remove(networkToDelete);
    }

    public void deleteSubnetwork(VDCNetwork subnetworkToDelete) {

        // revert VDCNetwork_id in VDC table to root network Id
        VDCNetwork rootNetwork = vdcNetworkService.findRootNetwork();
        Collection<VDC> vdcs = subnetworkToDelete.getNetworkVDCs();
        for (VDC vdc : vdcs) {
            logger.info("changing vdcNetworkId from " + subnetworkToDelete.getId() + " to root network id (" + rootNetwork.getId() + ") for dataverse alias " + vdc.getAlias() + " (vdc id " + vdc.getId() + ")");
            vdc.setVdcNetwork(rootNetwork);
        }

        //revert subnetworkId on template table to root network id
        List<Long> templateIds = templateService.getSubnetworkTemplates(subnetworkToDelete.getId(), false);
        for (Long templateId : templateIds) {
            Template template = templateService.getTemplate(templateId);
            logger.info("changing vdcNetworkId from " + subnetworkToDelete.getId() + " to root network id (" + rootNetwork.getId() + ") for template " + template.getName() + " (id " + template.getId() + ")");
            template.setVdcNetwork(rootNetwork);
        }

        // delete row from vdcnetwork table
        logger.info("deleting subnetwork " + subnetworkToDelete.getUrlAlias() + " (id " + subnetworkToDelete.getId() + ")");
        destroy(subnetworkToDelete);
    }

    public VDCNetwork find(Object pk) {
        return (VDCNetwork) em.find(VDCNetwork.class, pk);
    }

    public List <VDCNetwork> getVDCSubNetworks(){
        String queryStr = "SELECT n FROM VDCNetwork n where n.id > 0 order by n.name";
        Query query= em.createQuery(queryStr);
        return  query.getResultList();
    }
    
    public List <VDCNetwork> getVDCNetworks(){
        String queryStr = "SELECT n FROM VDCNetwork n";
        Query query= em.createQuery(queryStr);
        return  query.getResultList();
    }
    
    public List <VDCNetwork> getVDCNetworksOrderedById(){
        String queryStr = "SELECT n FROM VDCNetwork n ORDER BY n.id";
        Query query= em.createQuery(queryStr);
        return  query.getResultList();
    }
    
    public VDCNetwork find() {
        VDCNetwork vdcNetwork= (VDCNetwork) em.find(VDCNetwork.class, new Long(0));
        logger.log(Level.FINE, "found vdcNetwork" +vdcNetwork );
        return vdcNetwork;        
    }
    
    public VDCNetwork findById(Long networkId) {
        String query = "SELECT n from VDCNetwork n where n.id = :id";
        VDCNetwork vdcNetwork = null;
        try {
            vdcNetwork = (VDCNetwork) em.createQuery(query).setParameter("id", networkId).getSingleResult();
            em.refresh(vdcNetwork); // Refresh because the cached object doesn't include harvestingDataverse object - need to review why this is happening
        } catch (javax.persistence.NoResultException e) {
            // Do nothing, just return null.
        }

        return vdcNetwork;
    }
    
    public VDCNetwork findByAlias(String alias) {
        String query = "SELECT n from VDCNetwork n where lower(n.urlAlias) = lower(:fieldName)";
        VDCNetwork vdcNetwork = null;
        try {
            vdcNetwork = (VDCNetwork) em.createQuery(query).setParameter("fieldName", alias).getSingleResult();
            em.refresh(vdcNetwork); // Refresh because the cached object doesn't include harvestingDataverse object - need to review why this is happening
        } catch (javax.persistence.NoResultException e) {
            // Do nothing, just return null.
        }

        return vdcNetwork;
    }
    
    public VDCNetwork findByName(String name) {
        String query = "SELECT n from VDCNetwork n where lower(n.name) = lower(:fieldName)";
        VDCNetwork vdcNetwork = null;
        try {
            vdcNetwork = (VDCNetwork) em.createQuery(query).setParameter("fieldName", name).getSingleResult();
            em.refresh(vdcNetwork); // Refresh because the cached object doesn't include harvestingDataverse object - need to review why this is happening
        } catch (javax.persistence.NoResultException e) {
            // Do nothing, just return null.
        }

        return vdcNetwork;
    }
    
    public VDCNetwork getVDCNetworkFromRequest(HttpServletRequest request) {
        VDCNetwork vdcNetwork = (VDCNetwork) request.getAttribute("vdcNetwork");
        if (vdcNetwork == null) {
            Iterator iter = request.getParameterMap().keySet().iterator();
            while (iter.hasNext()) {
                Object key = (Object) iter.next();
                if (key instanceof String && ((String) key).indexOf("vdcSubnetworkId") != -1) {
                    try {
                        Long vdcSubnetworkId = new Long((String) request.getParameter((String) key));
                        vdcNetwork = find(vdcSubnetworkId);
                        request.setAttribute("vdcNetwork", vdcNetwork);
                    } catch (NumberFormatException e) {
                    } // param is not a Long, ignore it

                    break;
                }
            }
        }
        return vdcNetwork;
    }

    
    public VDCNetwork findRootNetwork(){
        VDCNetwork vdcNetwork= (VDCNetwork) em.find(VDCNetwork.class, new Long(0));
        return vdcNetwork;         
    }

    public LockssConfig getLockssConfig() {
        LockssConfig lc = null;
        try {
            lc = (LockssConfig) em.createQuery("select l from LockssConfig l where l.vdc is null").getSingleResult();
        } catch (NoResultException e) {
            
            // no result is ok - just return a null object
        }
        return lc;
        
    }
    
    public TwitterCredentials getTwitterCredentials() {
        TwitterCredentials tc = null;
        try {
            tc = (TwitterCredentials) em.createQuery("select t from TwitterCredentials t where t.vdc is null").getSingleResult();
        } catch (NoResultException e) {
            
            // no result is ok - just return a null object
        }
        return tc;        
    }       
      
   public TermsOfUse getCurrentTermsOfUse() {
        String queryStr = "SELECT t FROM TermsOfUse t WHERE t.vdc_id  is null order by createTime";
        Query query= em.createQuery(queryStr);
        List resultList = query.getResultList();
        TermsOfUse termsOfUse=null;
        if (resultList.size()>0) {
            termsOfUse = (TermsOfUse)resultList.get(resultList.size()-1);
        }
        return termsOfUse;
   }     
   
    public Boolean defaultTransactionReadOnly() {
        Object object = em.createNativeQuery("SHOW default_transaction_read_only ").getSingleResult();
        if (object.equals("on")) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
    
    private void removeExportTimer() {
        dvnTimerService.removeExportTimer();
    }
    
    public void updateExportTimer() {
        removeExportTimer();
        createExportTimer();
    }
    
    public void createExportTimer() {
            VDCNetwork vdcNetwork = this.find();
            long intervalDuration=0;
            Calendar initExpiration = Calendar.getInstance();
            initExpiration.set(Calendar.MINUTE, 0);
            initExpiration.set(Calendar.SECOND, 0);
            if (StringUtil.isEmpty(vdcNetwork.getExportPeriod())) {
                logger.log(Level.INFO, "No export period found, export not scheduled.");  
                return;
            } else if (vdcNetwork.getExportPeriod().equals(vdcNetwork.EXPORT_PERIOD_WEEKLY)) {
                intervalDuration = 1000*60 *60*24*7; 
                initExpiration.set(Calendar.HOUR_OF_DAY, vdcNetwork.getExportHourOfDay());
                initExpiration.set(Calendar.DAY_OF_WEEK, vdcNetwork.getExportDayOfWeek());
               
                logger.log(Level.INFO, "Scheduling weekly export");  
 

            } else if (vdcNetwork.getExportPeriod().equals(vdcNetwork.EXPORT_PERIOD_DAILY)) {
                 intervalDuration = 1000*60 *60*24; 
                 initExpiration.set(Calendar.HOUR_OF_DAY, vdcNetwork.getExportHourOfDay());  
              //   initExpiration.set(Calendar.MINUTE,15);  //REMOVE!!!!!
                 logger.log(Level.INFO, "Scheduling daily export");  

            }  else if (vdcNetwork.getExportPeriod().equals(vdcNetwork.EXPORT_PERIOD_NONE)) {

                logger.log(Level.INFO, "Unscheduling export");
                return;

            } else {
                logger.log(Level.WARNING, "Could not set timer for export, unknown schedule period: "+ vdcNetwork.getExportPeriod());
                return;
            }
            Date  initExpirationDate = initExpiration.getTime();
            Date currTime = new Date();
            if (initExpirationDate.before(currTime)) {
                initExpirationDate.setTime(initExpiration.getTimeInMillis()+intervalDuration);
            }
            logger.log(Level.INFO, "Setting timer for export, initial expiration: "+ initExpirationDate);
            String exportPeriod = vdcNetwork.getExportPeriod();
            Integer exportHourOfDay= vdcNetwork.getExportHourOfDay();
            Integer exportDayOfWeek = vdcNetwork.getExportDayOfWeek();
            ExportTimerInfo exportTimerInfo = new ExportTimerInfo(exportPeriod,exportHourOfDay, exportDayOfWeek);
            
            logger.info("Just checking timerService: ");
            for (Object timer:timerService.getTimers()) {
                logger.info("Found timer: "+((Timer)timer).getInfo());
            }
//            timerService.createTimer(initExpirationDate, intervalDuration,exportTimerInfo);
            dvnTimerService.createTimer(initExpirationDate, intervalDuration, exportTimerInfo);
      
    }

    @Timeout
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void handleTimeout(javax.ejb.Timer timer) {
        
        try {
            // We have to put all the code in a try/catch block because
            // if an exception is thrown from this method, Glassfish will automatically
            // call the method a second time. (The minimum number of re-tries for a Timer method is 1)
            
            // First, read-only mode check: 
            boolean readOnly = defaultTransactionReadOnly();
            
            if (timer.getInfo() instanceof ExportTimerInfo) {
                if (readOnly) {
                    logger.info("Network is in read-only mode; skipping scheduled (timer) export job.");
                } else {
                    ExportTimerInfo info = (ExportTimerInfo)timer.getInfo();
                    logger.info("handling timeout");
                    studyService.exportUpdatedStudies();  
                }
            }
         } catch (Throwable e) {
            mailService.sendExportErrorNotification(find().getSystemEmail(), this.find().getName());
           logException(e,logger);
        }
    }
    
     private void logException(Throwable e, Logger logger) {
     
       boolean cause=false;
       String fullMessage = "";
        do  {   
            String message = e.getClass().getName()+ " " +e.getMessage();
            if (cause) {
                message = "\nCaused By Exception.................... "+e.getClass().getName()+" "+e.getMessage();
            }
            StackTraceElement[] ste = e.getStackTrace();
            message+= "\nStackTrace: \n";
            for(int m=0;m<ste.length;m++) {
                message+=ste[m].toString()+"\n";
            }
            fullMessage+=message;
            cause=true;
        } while ((e=e.getCause())!=null);
         logger.severe(fullMessage);
    }    
     
    public void addTermsOfUse(TermsOfUse termsOfUse) {
        em.persist(termsOfUse);  
    }
       
    public Long getTotalDataverses(boolean released) {
        Long total = new Long("0");
        boolean bool = !released;
        Object object = ((List)em.createNativeQuery("select COUNT(id) from vdc where vdc.restricted = " + bool).getSingleResult()).get(0);
        total = (Long)object;
        return total;
    }
    
    public Long getTotalStudies(boolean released) {
        Long total = new Long("0");
        total = (Long)em.createNativeQuery("select COUNT(study.id) from study, vdc, studyVersion where study.owner_id = vdc.id AND studyVersion.study_id = study.id AND studyVersion.versionState = '" + StudyVersion.VersionState.RELEASED + "' AND vdc.restricted = " + !released).getSingleResult();
        return total;
    }
    
    public Long getTotalFiles(boolean released) {
        Long total = new Long("0");
        total = ((Long)em.createNativeQuery("select COUNT(studyfile.id) from studyfile, vdc, filemetadata, studyversion, study where study.owner_id = vdc.id AND study.id = studyversion.study_id AND studyversion.versionstate= '" + StudyVersion.VersionState.RELEASED + "' AND filemetadata.studyversion_id = studyversion.id AND studyfile.id = filemetadata.studyfile_id  AND vdc.restricted = " + !released).getSingleResult());
        return total;
    }
    
    public Long getTotalDownloads(boolean released) {
        Long total = new Long("0");
        total = ((Long)em.createNativeQuery("select sum(studyfileactivity.downloadcount) from vdc, studyversion, study, studyfileactivity where study.owner_id = vdc.id AND study.id = studyversion.study_id  AND studyversion.versionstate= '" + StudyVersion.VersionState.RELEASED + "' AND studyfileactivity.study_id = study.id AND vdc.restricted = " + !released).getSingleResult());
        return total;
    }
    
    public void updateDefaultDisplayNumber(VDCNetwork vdcnetwork) {
        if (em.find(VDCNetwork.class, new Long(0)) != null)
            em.merge(vdcnetwork);
    }
    
    public void updateDefaultTemplate(Long templateId) {
          updateDefaultTemplate(templateId, new Long(0));
    }
    
    public void updateDefaultTemplate(Long templateId, Long vdcNetworkId) {
        Template template = em.find(Template.class, templateId);
        find(vdcNetworkId).setDefaultTemplate(template);
    }
    
        
    public Long getTotalStudiesBySubnetwork(Long networkId, boolean released) {
        Long total = (Long)em.createNativeQuery("select COUNT(study.id) from study, vdc, studyVersion where study.owner_id = vdc.id AND studyVersion.study_id = study.id AND studyVersion.versionState = '" 
                + StudyVersion.VersionState.RELEASED + "' AND vdc.restricted = " 
                + !released + " and vdc.vdcnetwork_id = " + networkId).getSingleResult();
        if (total == null){
           return  new Long("0");
        }
        return total;
    }
    
    public List<Object> getStudyListBySubnetwork(Long networkId, boolean released) {
          return em.createNativeQuery("select study.id from study , vdc, studyVersion where study.owner_id = vdc.id AND studyVersion.study_id = study.id AND studyVersion.versionState = '" 
                  + StudyVersion.VersionState.RELEASED +
                  "' AND vdc.restricted = " + !released +
                  " and vdc.vdcnetwork_id = " + networkId
                  ).getResultList();
    }
    
    public Long getTotalFilesBySubnetwork(Long networkId, boolean released) {
        Long total = ((Long)em.createNativeQuery("select COUNT(studyfile.id) from studyfile, vdc, filemetadata, studyversion, study where study.owner_id = vdc.id AND study.id = studyversion.study_id AND studyversion.versionstate= '" 
                + StudyVersion.VersionState.RELEASED + "' AND filemetadata.studyversion_id = studyversion.id " + ""
                + "AND studyfile.id = filemetadata.studyfile_id  AND vdc.restricted = " + !released +
                 " and vdc.vdcnetwork_id = " + networkId).getSingleResult());
        if (total == null){
           return  new Long("0");
        }
        return total;
    }
    
    public Long getTotalDownloadsBySubnetwork(Long networkId, boolean released) {
        Long total = ((Long)em.createNativeQuery("select sum(studyfileactivity.downloadcount) from vdc, studyversion, study, studyfileactivity where study.owner_id = vdc.id AND study.id = studyversion.study_id " + ""
                + " AND studyversion.versionstate= '" + StudyVersion.VersionState.RELEASED + "' AND studyfileactivity.study_id = study.id AND vdc.restricted = " 
                + !released + " and vdc.vdcnetwork_id = " + networkId).getSingleResult());
        if (total == null){
           return  new Long("0");
        }
        return total;
    }

}
