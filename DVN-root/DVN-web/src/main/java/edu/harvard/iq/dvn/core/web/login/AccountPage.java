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
 * AccountPage.java
 *
 * Created on October 4, 2006, 1:28 PM
 */
package edu.harvard.iq.dvn.core.web.login;

import com.icesoft.faces.component.paneltabset.PanelTabSet;
import com.icesoft.faces.context.effects.JavascriptContext;
import edu.harvard.iq.dvn.core.admin.UserServiceLocal;
import edu.harvard.iq.dvn.core.admin.VDCUser;
import edu.harvard.iq.dvn.core.web.common.StatusMessage;
import edu.harvard.iq.dvn.core.web.common.VDCBaseBean;
import javax.ejb.EJB;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
@ViewScoped
@Named("AccountPage")
public class AccountPage extends VDCBaseBean implements java.io.Serializable  {
    @EJB UserServiceLocal userService;
    

    /** 
     * <p>Construct a new Page bean instance.</p>
     */
    public AccountPage() {
    }

    public void preRenderView() {
        super.preRenderView();
        if (tabSet1.getSelectedIndex() == 1) {
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "initManageStudiesTableBlockHeight();");
        }
    }



    public void init() {
        super.init();
        if (userId != null) {
            user = userService.find(userId);
        } else {
            user = getVDCSessionBean().getUser();
        }      
    }

    /** 
     * <p>Callback method that is called after the component tree has been
     * restored, but before any event processing takes place.  This method
     * will <strong>only</strong> be called on a postback request that
     * is processing a form submit.  Customize this method to allocate
     * resources that will be required in your event handlers.</p>
     */
    public void preprocess() {
    }

    /** 
     * <p>Callback method that is called just before rendering takes place.
     * This method will <strong>only</strong> be called for the page that
     * will actually be rendered (and not, for example, on a page that
     * handled a postback and then navigated to a different page).  Customize
     * this method to allocate resources that will be required for rendering
     * this page.</p>
     */
    public void prerender() {
    }
    
    /** 
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called (regardless of whether
     * or not this was the page that was actually rendered).  Customize this
     * method to release resources acquired in the <code>init()</code>,
     * <code>preprocess()</code>, or <code>prerender()</code> methods (or
     * acquired during execution of an event handler).</p>
     */
    public void destroy() {
    }

    /**
     * Holds value of property userId.
     */
    private Long userId;

    /**
     * Getter for property userId.
     * @return Value of property userId.
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * Setter for property userId.
     * @param userId New value of property userId.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Holds value of property user.
     */
    private VDCUser user;

    /**
     * Getter for property user.
     * @return Value of property user.
     */
    public VDCUser getUser() {
        return this.user;
    }

    /**
     * Setter for property user.
     * @param user New value of property user.
     */
    public void setUser(VDCUser user) {
        this.user = user;
    }

    /**
     * Holds value of property statusMessage.
     */
    private StatusMessage statusMessage;

    /**
     * Getter for property statusMessage.
     * @return Value of property statusMessage.
     */
    public StatusMessage getStatusMessage() {
        return this.statusMessage;
    }

    /**
     * Setter for property statusMessage.
     * @param statusMessage New value of property statusMessage.
     */
    public void setStatusMessage(StatusMessage statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    private PanelTabSet tabSet1 = new PanelTabSet();
    public PanelTabSet getTabSet1() {return tabSet1;}
    public void setTabSet1(PanelTabSet tabSet1) {this.tabSet1 = tabSet1;}
}

