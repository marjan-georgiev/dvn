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
 * EditBannerFooterPage.java
 *
 * Created on October 10, 2006, 10:46 AM
 */
package edu.harvard.iq.dvn.core.web.admin;

import com.icesoft.faces.component.ext.HtmlInputHidden;
import com.icesoft.faces.component.ext.HtmlInputTextarea;
import edu.harvard.iq.dvn.core.vdc.VDC;
import edu.harvard.iq.dvn.core.vdc.VDCNetwork;
import edu.harvard.iq.dvn.core.vdc.VDCNetworkServiceLocal;
import edu.harvard.iq.dvn.core.vdc.VDCServiceLocal;
import edu.harvard.iq.dvn.core.web.common.VDCBaseBean;
import javax.ejb.EJB;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
@ViewScoped
@Named("EditBannerFooterPage")
public class EditBannerFooterPage extends VDCBaseBean  implements java.io.Serializable {
    @EJB VDCServiceLocal vdcService;
    @EJB VDCNetworkServiceLocal vdcNetworkService;
    
    public void init() {
        super.init();
     
        VDCNetwork vdcNetwork = getVDCRequestBean().getCurrentVdcNetwork();
        
        if (this.getBanner() == null){
            setBanner( (getVDCRequestBean().getCurrentVDCId() == null) ? vdcNetwork.getNetworkPageHeader(): getVDCRequestBean().getCurrentVDC().getHeader());
            setFooter( (getVDCRequestBean().getCurrentVDCId() == null) ? vdcNetwork.getNetworkPageFooter(): getVDCRequestBean().getCurrentVDC().getFooter());         
            
            if (getVDCRequestBean().getCurrentVDCId() != null) {
                setDisplayInFrame(getVDCRequestBean().getCurrentVDC().isDisplayInFrame());
                setParentSite(getVDCRequestBean().getCurrentVDC().getParentSite());
            }
        }
        combinedTextField.setValue(banner + footer);
    }


            
    /** 
     * <p>Construct a new Page bean instance.</p>
     */
    public EditBannerFooterPage() {
    }


    
    private String banner;
    
    public String getBanner(){
        return banner;
    }
    
    public void setBanner(String banner) {
        this.banner = banner;
    }
    
    private String footer;
    
    public String getFooter() {
        return footer;
    }
    
    public void setFooter(String footer) {
        this.footer = footer;
    }
    

    
    // ACTION METHODS
    public String save_action() {
        String forwardPage=null;
        String message = "";
        if (getVDCRequestBean().getCurrentVDCId() == null) {
            String retString = "/networkAdmin/NetworkOptionsPage?faces-redirect=true&tab=settings&tab2=customization";
            // this is a save against the network
            VDCNetwork vdcnetwork = getVDCRequestBean().getVdcNetwork();
            vdcnetwork.setNetworkPageHeader(banner);
            vdcnetwork.setNetworkPageFooter(footer);
            vdcNetworkService.edit(vdcnetwork);
            getVDCRequestBean().getVdcNetwork().setNetworkPageHeader(banner);
            getVDCRequestBean().getVdcNetwork().setNetworkPageFooter(footer);
            forwardPage=retString;
            message = "Successfully updated network customization.";
        } else {
            VDC vdc = vdcService.find(new Long(getVDCRequestBean().getCurrentVDC().getId()));
            vdc.setHeader(banner);
            vdc.setFooter(footer);
            vdc.setDisplayInFrame(displayInFrame);
            vdc.setParentSite(parentSite);
            vdcService.edit(vdc);
            forwardPage="/admin/OptionsPage?faces-redirect=true" + getContextSuffix();
            message = "Successfully updated dataverse customization.";
        }
        getVDCRenderBean().getFlash().put("successMessage",message);
        return forwardPage;
    }

    public String cancel_action(){
        if (getVDCRequestBean().getCurrentVDCId() == null) {
            setBanner(getVDCRequestBean().getVdcNetwork().getNetworkPageHeader());
            setFooter(getVDCRequestBean().getVdcNetwork().getNetworkPageFooter());
            return "/networkAdmin/NetworkOptionsPage?faces-redirect=true";
        } else {
                setBanner(getVDCRequestBean().getCurrentVDC().getHeader());
                setFooter(getVDCRequestBean().getCurrentVDC().getFooter());
                setDisplayInFrame(getVDCRequestBean().getCurrentVDC().isDisplayInFrame());
                setParentSite(getVDCRequestBean().getCurrentVDC().getParentSite());
            return "/admin/OptionsPage?faces-redirect=true" + getContextSuffix();
        }
    }

    protected HtmlInputTextarea bannerTextField = new HtmlInputTextarea();

    /**
     * Get the value of bannerTextField
     *
     * @return the value of bannerTextField
     */
    public HtmlInputTextarea getBannerTextField() {
        return bannerTextField;
    }

    /**
     * Set the value of bannerTextField
     *
     * @param bannerTextField new value of bannerTextField
     */
    public void setBannerTextField(HtmlInputTextarea bannerTextField) {
        this.bannerTextField = bannerTextField;
    }

    protected HtmlInputTextarea footerTextField = new HtmlInputTextarea();

    /**
     * Get the value of footerTextarea
     *
     * @return the value of footerTextarea
     */
    public HtmlInputTextarea getFooterTextField() {
        return footerTextField;
    }

    /**
     * Set the value of footerTextarea
     *
     * @param footerTextarea new value of footerTextarea
     */
    public void setFooterTextField(HtmlInputTextarea footerTextField) {
        this.footerTextField = footerTextField;
    }

    protected HtmlInputHidden combinedTextField = new HtmlInputHidden();

    /**
     * Get the value of inputHidden
     *
     * @return the value of inputHidden
     */
    public HtmlInputHidden getCombinedTextField() {
       
        return combinedTextField;
    }

    /**
     * Set the value of inputHidden
     *
     * @param inputHidden new value of inputHidden
     */
    public void setCombinedTextField(HtmlInputHidden combinedTextField) {
        this.combinedTextField = combinedTextField;
    }

    // these are only valid for a vdc
    private boolean displayInFrame;
    private String parentSite;

    public boolean isDisplayInFrame() {
        return displayInFrame;
    }

    public void setDisplayInFrame(boolean displayInFrame) {
        this.displayInFrame = displayInFrame;
    }

    public String getParentSite() {
        return parentSite;
    }

    public void setParentSite(String parentSite) {
        this.parentSite = parentSite;
    }
    
  
}

