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
 * HarvestSitesPage.java
 *
 * Created on April 5, 2007, 10:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.web.site;

import edu.harvard.iq.dvn.core.harvest.HarvesterServiceLocal;
import edu.harvard.iq.dvn.core.vdc.HarvestingDataverse;
import edu.harvard.iq.dvn.core.vdc.HarvestingDataverseServiceLocal;
import edu.harvard.iq.dvn.core.vdc.VDC;
import edu.harvard.iq.dvn.core.vdc.VDCServiceLocal;
import java.util.List;
import javax.ejb.EJB;
import edu.harvard.iq.dvn.core.web.common.VDCBaseBean;
import java.util.Date;
import com.icesoft.faces.component.ext.HtmlDataTable;
import edu.harvard.iq.dvn.core.admin.DvnTimerRemote;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

/**
 *
 * @author Ellen Kraffmiller
 */
@ViewScoped
@Named("HarvestSitesPage")
public class HarvestSitesPage extends VDCBaseBean implements java.io.Serializable {

    @EJB
    HarvestingDataverseServiceLocal harvestingDataverseService;
    @EJB
    HarvesterServiceLocal harvesterService;
    @EJB
    VDCServiceLocal vdcService;
    @EJB (name="dvnTimer")
    DvnTimerRemote remoteTimerService;

    /** Creates a new instance of HarvestSitesPage */
    public HarvestSitesPage() {
    }

   
    /**
     * Holds value of property harvestSiteList.
     */
    private List<HarvestingDataverse> harvestSiteList;

    /**
     * Getter for property harvestSiteList.
     * @return Value of property harvestSiteList.
     */
    public List<HarvestingDataverse> getHarvestSiteList() {
        if (harvestSiteList==null) {
            harvestSiteList = harvestingDataverseService.findAll();
        }
        return harvestSiteList;


    }
   
    /**
     * Holds value of property harvestDataTable.
     */
    private HtmlDataTable harvestDataTable;

    /**
     * Getter for property siteDataTable.
     * @return Value of property siteDataTable.
     */
    public HtmlDataTable getHarvestDataTable() {
        return this.harvestDataTable;
    }

    public void doSchedule(ActionEvent ae) {
        HarvestingDataverse hd = (HarvestingDataverse) this.harvestDataTable.getRowData();
        hd.setScheduled(true);
        remoteTimerService.updateHarvestTimer(hd);
        harvestingDataverseService.edit(hd);
        // set list to null, to force a fresh retrieval of data
        harvestSiteList=null;
    }

    public void doUnschedule(ActionEvent ae) {
        HarvestingDataverse hd = (HarvestingDataverse) this.harvestDataTable.getRowData();
        hd.setScheduled(false);
        remoteTimerService.updateHarvestTimer(hd);
        harvestingDataverseService.edit(hd);
        // set list to null, to force a fresh retrieval of data
        harvestSiteList=null;
    }

    /**
     * Setter for property siteDataTable.
     * @param siteDataTable New value of property siteDataTable.
     */
    public void setHarvestDataTable(HtmlDataTable harvestDataTable) {
        this.harvestDataTable = harvestDataTable;
    }

 
    
    
    public void doRunNow(ActionEvent ae) {

        HarvestingDataverse hd = (HarvestingDataverse) this.harvestDataTable.getRowData();
        harvesterService.doAsyncHarvest(hd);
        Date previousDate = hd.getLastHarvestTime();
        HarvestingDataverse tempHD = null;
        Date tempDate = null;
        try {
            do {
                Thread.sleep(100);  // sleep for 1/10 second to wait for harvestingNow or lastHarvestDate to be updated
                tempHD = harvestingDataverseService.find(hd.getId());
                tempDate = tempHD.getLastHarvestTime();
            } while (!tempHD.isHarvestingNow() && !isHarvestingDateUpdated(previousDate, tempDate));
        } catch (InterruptedException e) {
        }
        //       harvesterService.getRecord(hd,"hdl:1902.2/06635",hd.getFormat(),null);
        //       harvesterService.getRecord(hd,"hdl:1902.2/zzzzzzz",hd.getFormat(),null);
        this.harvestSiteList = harvestingDataverseService.findAll();
    }

    private boolean isHarvestingDateUpdated(Date previousDate, Date tempDate) {
        boolean isUpdated=false;
        if (previousDate==null) {
            if (tempDate!=null) {
                isUpdated=true;
            }
        } else if (!previousDate.equals(tempDate)){
            isUpdated= true;
        }
        return isUpdated;
    }
    
    
    public void doRemoveHarvestDataverse(ActionEvent ae) {
        HarvestingDataverse hd = (HarvestingDataverse) this.harvestDataTable.getRowData();
        harvestingDataverseService.delete(hd.getId());
        this.harvestSiteList = harvestingDataverseService.findAll();

    }

   
    /**
     * Holds value of property dataverseDataTable.
     */
    private HtmlDataTable dataverseDataTable;

    /**
     * Getter for property dataverseDataTable.
     * @return Value of property dataverseDataTable.
     */
    public HtmlDataTable getDataverseDataTable() {
        return this.dataverseDataTable;
    }

    /**
     * Setter for property dataverseDataTable.
     * @param dataverseDataTable New value of property dataverseDataTable.
     */
    public void setDataverseDataTable(HtmlDataTable dataverseDataTable) {
        this.dataverseDataTable = dataverseDataTable;
    }
    
}
