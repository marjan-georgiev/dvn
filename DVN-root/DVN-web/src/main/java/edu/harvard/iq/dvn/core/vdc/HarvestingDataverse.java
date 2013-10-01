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
 * VDC.java
 *
 * Created on July 28, 2006, 2:22 PM
 *
 */
package edu.harvard.iq.dvn.core.vdc;

import edu.harvard.iq.dvn.core.admin.UserGroup;
import edu.harvard.iq.dvn.core.admin.VDCUser;
import edu.harvard.iq.dvn.core.harvest.HarvestFormatType;
import edu.harvard.iq.dvn.core.study.Study;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.persistence.*;

/**
 *
 * @author Ellen Kraffmiller
 */
@Entity
public class HarvestingDataverse implements Serializable {
    public static final String SCHEDULE_PERIOD_DAILY="daily";
    public static final String SCHEDULE_PERIOD_WEEKLY="weekly";
    
    public static final String HARVEST_TYPE_OAI="oai";
    public static final String HARVEST_TYPE_NESSTAR="nesstar";
    
    public static final String HARVEST_RESULT_SUCCESS="success";
    public static final String HARVEST_RESULT_FAILED="failed";
    
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastHarvestTime;
    
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastSuccessfulHarvestTime; 
    private Long harvestedStudyCount;
    private Long failedStudyCount;
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastSuccessfulNonZeroHarvestTime;
    private Long harvestedStudyCountNonZero;
    private Long failedStudyCountNonZero;
    private String harvestResult;

    /*
     * ALTER TABLE harvestingdataverse ADD COLUMN lastsuccessfulnonzeroharvesttime timestamp without time zone;
ALTER TABLE harvestingdataverse ADD COLUMN failedstudycountnonzero bigint;
ALTER TABLE harvestingdataverse ADD COLUMN harvestedstudycountnonzero bigint;
     */

   
       
    public Collection<Study> search(String query) {
        //TODO: complete implementation
        return null;
    }
    /** Creates a new instance of VDC */
    public HarvestingDataverse() {
        this.harvestType = harvestType = HARVEST_TYPE_OAI; // default harvestType
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
    


    private boolean subsetRestricted;

    public boolean isSubsetRestricted() {
        return subsetRestricted;
    }

    public void setSubsetRestricted(boolean subsetRestricted) {
        this.subsetRestricted = subsetRestricted;
    }


    

    
    public boolean isSubsetRestrictedForUser(VDCUser user, UserGroup ipUserGroup) {
        if (this.vdc.areFilesRestrictedForUser(user, ipUserGroup)) {
            return this.subsetRestricted;
        } else {
            return false;
        }
    }
    




    /**
     * Holds value of property oaiServer.
     */
    private String serverUrl;

    /**
     * Getter for property oaiServer.
     * @return Value of property oaiServer.
     */
    public String getServerUrl() {
        return this.serverUrl;
    }

    /**
     * Setter for property oaiServer.
     * @param oaiServer New value of property oaiServer.
     */
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl.trim();
    }

    /**
     * Holds value of property harvestingSet.
     */
    private String harvestingSet;

    /**
     * Getter for property harvestingSet.
     * @return Value of property harvestingSet.
     */
    public String getHarvestingSet() {
        return this.harvestingSet;
    }

    /**
     * Setter for property harvestingSet.
     * @param harvestingSet New value of property harvestingSet.
     */
    public void setHarvestingSet(String harvestingSet) {
        this.harvestingSet = harvestingSet;
    }

    /**
     * Holds value of property vdc.
     */
    @OneToOne (mappedBy="harvestingDataverse",cascade={CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST })
    private  VDC vdc;

    /**
     * Getter for property vdc.
     * @return Value of property vdc.
     */
    public VDC getVdc() {
        return this.vdc;
    }

    /**
     * Setter for property vdc.
     * @param vdc New value of property vdc.
     */
    public void setVdc(VDC vdc) {
        this.vdc = vdc;
    }

    /**
     * Holds value of property scheduled.
     */
    private boolean scheduled;

    /**
     * Getter for property scheduled.
     * @return Value of property scheduled.
     */
    public boolean isScheduled() {
        return this.scheduled;
    }

    /**
     * Setter for property scheduled.
     * @param scheduled New value of property scheduled.
     */
    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

     public int hashCode() {
        int hash = 0;
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof HarvestingDataverse)) {
            return false;
        }
        HarvestingDataverse other = (HarvestingDataverse)object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) return false;
        return true;
    }    

    public Date getLastHarvestTime() {
        return lastHarvestTime;
    }

    public void setLastHarvestTime(Date lastHarvestTime) {
        this.lastHarvestTime = lastHarvestTime;
    }

    /**
     * Holds value of property harvestingNow.
     */
    private boolean harvestingNow;

    /**
     * Getter for property harvestingNow.
     * @return Value of property harvestingNow.
     */
    public boolean isHarvestingNow() {
        return this.harvestingNow;
    }

    /**
     * Setter for property harvestingNow.
     * @param harvestingNow New value of property harvestingNow.
     */
    public void setHarvestingNow(boolean harvestingNow) {
        this.harvestingNow = harvestingNow;
    }

    /**
     * Holds value of property handlePrefix.
     */
    @ManyToOne
    private HandlePrefix handlePrefix;

    public String getSchedulePeriod() {
        return schedulePeriod;
    }

    public String getScheduleDescription() {
        Date date = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        SimpleDateFormat weeklyFormat = new SimpleDateFormat(" E h a ");
        SimpleDateFormat  dailyFormat = new SimpleDateFormat(" h a ");
        String desc = "Not Scheduled";
        if (schedulePeriod!=null && schedulePeriod!="") {
            cal.set(Calendar.HOUR_OF_DAY, scheduleHourOfDay);
            if (schedulePeriod.equals(this.SCHEDULE_PERIOD_WEEKLY)) {
                cal.set(Calendar.DAY_OF_WEEK,scheduleDayOfWeek);
                desc="Weekly, "+weeklyFormat.format(cal.getTime());
            } else {
                desc="Daily, "+dailyFormat.format(cal.getTime());
            }
        }
        return desc;
    }
    
    public void setSchedulePeriod(String schedulePeriod) {
        this.schedulePeriod = schedulePeriod;
    }

    public Integer getScheduleHourOfDay() {
        return scheduleHourOfDay;
    }

    public void setScheduleHourOfDay(Integer scheduleHourOfDay) {
        this.scheduleHourOfDay = scheduleHourOfDay;
    }

    public Integer getScheduleDayOfWeek() {
        return scheduleDayOfWeek;
    }

    public void setScheduleDayOfWeek(Integer scheduleDayOfWeek) {
        this.scheduleDayOfWeek = scheduleDayOfWeek;
    }

    /**
     * Getter for property handlePrefix.
     * @return Value of property handlePrefix.
     */
    public HandlePrefix getHandlePrefix() {
        return this.handlePrefix;
    }

    /**
     * Setter for property handlePrefix.
     * @param handlePrefix New value of property handlePrefix.
     */
    public void setHandlePrefix(HandlePrefix handlePrefix) {
        this.handlePrefix = handlePrefix;
    }

   
    private String schedulePeriod;

    private Integer scheduleHourOfDay;

    private Integer scheduleDayOfWeek;
    
    @ManyToOne
    @JoinColumn(nullable=false)
    private HarvestFormatType harvestFormatType;

    public HarvestFormatType getHarvestFormatType() {
        return harvestFormatType;
    }

    public void setHarvestFormatType(HarvestFormatType harvestFormatType) {
        this.harvestFormatType = harvestFormatType;
    }
    
    private boolean generateRandomIds;

    public boolean isGenerateRandomIds() {
        return generateRandomIds;
    }

    public void setGenerateRandomIds(boolean generateRandomIds) {
        this.generateRandomIds = generateRandomIds;
    }
    
    String harvestType;

    public String getHarvestType() {
        return harvestType;
    }

    public void setHarvestType(String harvestType) {
        this.harvestType = harvestType;
    }
    
    public boolean isOai() {
        return HARVEST_TYPE_OAI.equals(harvestType);
    }
    
    public boolean isNesstar() {
        return HARVEST_TYPE_NESSTAR.equals(harvestType);
        
    }   
     
    public Long getFailedStudyCount() {
        return failedStudyCount;
    }

    public void setFailedStudyCount(Long failedStudyCount) {
        this.failedStudyCount = failedStudyCount;
    }

    public String getHarvestResult() {
        return harvestResult;
    }

    public void setHarvestResult(String harvestResult) {
        this.harvestResult = harvestResult;
    }

    public String getHarvestResultSuccess() {
        return this.HARVEST_RESULT_SUCCESS;
    }
    
    public String getHarvestResultFailed() {
        return this.HARVEST_RESULT_FAILED;
    }
  
    public Long getHarvestedStudyCount() {
        return harvestedStudyCount;
    }

    public void setHarvestedStudyCount(Long harvestedStudyCount) {
        this.harvestedStudyCount = harvestedStudyCount;
    }

    public Date getLastSuccessfulHarvestTime() {
        return lastSuccessfulHarvestTime;
    }

    public void setLastSuccessfulHarvestTime(Date lastSuccessfulHarvestTime) {
        this.lastSuccessfulHarvestTime = lastSuccessfulHarvestTime;
    }

    public Long getFailedStudyCountNonZero() {
        return failedStudyCountNonZero;
    }

    public void setFailedStudyCountNonZero(Long failedStudyCountNonZero) {
        this.failedStudyCountNonZero = failedStudyCountNonZero;
    }

    public Long getHarvestedStudyCountNonZero() {
        return harvestedStudyCountNonZero;
    }

    public void setHarvestedStudyCountNonZero(Long harvestedStudyCountNonZero) {
        this.harvestedStudyCountNonZero = harvestedStudyCountNonZero;
    }

    public Date getLastSuccessfulNonZeroHarvestTime() {
        return lastSuccessfulNonZeroHarvestTime;
    }

    public void setLastSuccessfulNonZeroHarvestTime(Date lastSuccessfulNonZeroHarvestTime) {
        this.lastSuccessfulNonZeroHarvestTime = lastSuccessfulNonZeroHarvestTime;
    }
}
