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
package edu.harvard.iq.dvn.core.harvest;

import java.io.Serializable;

/**
 *  This class is used when creating an EJB Timer for scheduling Harvesting.
 *  We use this class rather than the HarvestingDataverse entity because
 *  the class must be Serializable, and there is too much info associated with the HarvestingDataverse
 *  in order to realistically serialize it.  (We can't make related mapped entities transient.)
 *
 * @author Ellen Kraffmiller
 */
public class HarvestTimerInfo implements Serializable {
    private Long harvestingDataverseId;
    private String name;
    private String schedulePeriod;
    private Integer scheduleHourOfDay;
    
    public HarvestTimerInfo() {
        
    }
    
   
    public HarvestTimerInfo(Long harvestingDataverseId, String name, String schedulePeriod, Integer scheduleHourOfDay, Integer scheduleDayOfWeek) {
        this.harvestingDataverseId=harvestingDataverseId;
        this.name=name;
        this.schedulePeriod=schedulePeriod;
        this.scheduleDayOfWeek=scheduleDayOfWeek;
        this.scheduleHourOfDay=scheduleHourOfDay;
    }
    
    
    public Long getHarvestingDataverseId() {
        return harvestingDataverseId;
    }

    public void setHarvestingDataverseId(Long harvestingDataverseId) {
        this.harvestingDataverseId = harvestingDataverseId;
    }    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchedulePeriod() {
        return schedulePeriod;
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
    private Integer scheduleDayOfWeek;
  
    
}
