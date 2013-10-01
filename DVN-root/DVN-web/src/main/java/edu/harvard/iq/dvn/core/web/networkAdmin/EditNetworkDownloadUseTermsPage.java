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
 * EditDownloadUseTermsPage.java
 *
 * 
 */
package edu.harvard.iq.dvn.core.web.networkAdmin;

import edu.harvard.iq.dvn.core.web.util.ExceptionMessageWriter;
import edu.harvard.iq.dvn.core.vdc.VDCNetwork;
import edu.harvard.iq.dvn.core.vdc.VDCNetworkServiceLocal;
import edu.harvard.iq.dvn.core.web.common.VDCBaseBean;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.FacesException;
import javax.faces.bean.ViewScoped;
import javax.inject.Named;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
@ViewScoped
@Named("EditNetworkDownloadUseTermsPage")
public class EditNetworkDownloadUseTermsPage extends VDCBaseBean implements java.io.Serializable  {

    @EJB  VDCNetworkServiceLocal vdcNetworkService;
    private boolean termsOfUseEnabled;
    private String termsOfUse;
    
    public void init() {
        super.init();
        termsOfUse = getVDCRequestBean().getVdcNetwork().getDownloadTermsOfUse();
        termsOfUseEnabled = getVDCRequestBean().getVdcNetwork().isDownloadTermsOfUseEnabled();
    }    

    public boolean isTermsOfUseEnabled() {
        return termsOfUseEnabled;
    }

    public void setTermsOfUseEnabled(boolean termsOfUseEnabled) {
        this.termsOfUseEnabled = termsOfUseEnabled;
    }

    public String getTermsOfUse() {
        return termsOfUse;
    }

    public void setTermsOfUse(String termsOfUse) {
        this.termsOfUse = termsOfUse;
    }

    public String save_action() {      
        if (validateTerms()) {
            // action code here
            VDCNetwork vdcNetwork = vdcNetworkService.find();
            vdcNetwork.setDownloadTermsOfUse(termsOfUse);
            vdcNetwork.setDownloadTermsOfUseEnabled(termsOfUseEnabled);
            vdcNetworkService.edit(vdcNetwork);
            getVDCRenderBean().getFlash().put("successMessage", "Successfully updated terms for file download.");
            return "/networkAdmin/NetworkOptionsPage?faces-redirect=true";
        } else {
            return null;
        }
    }

    public String cancel_action() {
        return "/networkAdmin/NetworkOptionsPage?faces-redirect=true";
    }
    
 


    private boolean validateTerms() {
        String elementValue = termsOfUse;
        boolean isUseTerms = true;
        if ((elementValue == null || elementValue.equals("")) && (termsOfUseEnabled)) {
            isUseTerms = false;
            FacesMessage message = new FacesMessage("To enable this feature, you must also enter terms of use in the field below.  Please enter terms of use as either plain text or html.");
            FacesContext.getCurrentInstance().addMessage("form1:textArea1", message);
        }
        return isUseTerms;
    }

}

