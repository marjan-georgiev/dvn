/*
 * Dataverse Network - A web application to distribute, share and analyze quantitative data.
 * Copyright (C) 2007
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation,Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

/*
 * HarvesterServiceLocal.java
 *
 * Created on May 1, 2007, 1:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.harvard.hmdc.vdcnet.harvest;

import edu.harvard.hmdc.vdcnet.jaxb.oai.ResumptionTokenType;
import edu.harvard.hmdc.vdcnet.vdc.HarvestingDataverse;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.lang.mutable.MutableBoolean;

/**
 *
 * @author Ellen Kraffmiller
 */
public interface HarvesterServiceLocal {
    public void createHarvestTimer();
    public void doAsyncHarvest(HarvestingDataverse dataverse);

    public List<SetDetailBean> getSets(String oaiUrl); 
    public List<String> getMetadataFormats(String oaiUrl);
    public ResumptionTokenType harvestFromIdentifiers(Logger hdLogger, ResumptionTokenType resumptionToken, HarvestingDataverse dataverse, String from, String until, List<Long> harvestedStudyIds, boolean allowUpdates, MutableBoolean harvestErrorOccurred);
    public Long getRecord(Logger hdLogger, HarvestingDataverse dataverse, String identifier, String metadataPrefix, boolean allowUpdates, MutableBoolean harvestErrorOccurred);
          
}
