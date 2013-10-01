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
 * AllUsersPage.java
 *
 * Created on October 20, 2006, 4:59 PM
 *
 */
package edu.harvard.iq.dvn.core.web.networkAdmin;

import edu.harvard.iq.dvn.core.admin.UserServiceLocal;
import edu.harvard.iq.dvn.core.admin.VDCUser;
import edu.harvard.iq.dvn.core.vdc.VDCNetworkServiceLocal;
import edu.harvard.iq.dvn.core.web.common.VDCBaseBean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJB;
import com.icesoft.faces.component.ext.HtmlDataTable;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
@ViewScoped
@Named("AllUsersPage")
public class AllUsersPage extends VDCBaseBean implements java.io.Serializable  {
    @EJB UserServiceLocal userService;
    @EJB VDCNetworkServiceLocal vdcNetworkService;
    private HtmlDataTable dataTable;
    
    public void init() {
        super.init();
        initUserData();
    }
    
    public void initUserData() {
        
        userData = new ArrayList();
        List users = userService.findAll();
        for (Iterator it = users.iterator(); it.hasNext();) {
            VDCUser elem = (VDCUser) it.next();
            boolean defaultNetworkAdmin = elem.getNetworkRole()!=null
                    && elem.getId().equals(vdcNetworkService.find().getDefaultNetworkAdmin().getId());
            userData.add(new AllUsersDataBean(elem, defaultNetworkAdmin));
            System.out.println("elem is "+elem+", elem id is "+elem.getId());
        }
    }
    
    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public AllUsersPage() {
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
     * Holds value of property userData.
     */
    private List<edu.harvard.iq.dvn.core.web.networkAdmin.AllUsersDataBean> userData;
    
    /**
     * Getter for property users.
     * @return Value of property users.
     */
    public List<edu.harvard.iq.dvn.core.web.networkAdmin.AllUsersDataBean> getUserData() {
        return this.userData;
    }
    
    /**
     * Setter for property users.
     * @param users New value of property users.
     */
    public void setUserData(List<edu.harvard.iq.dvn.core.web.networkAdmin.AllUsersDataBean> userData) {
        this.userData = userData;
    }
    
    public void activateUser(ActionEvent ae) {
        AllUsersDataBean bean=(AllUsersDataBean)dataTable.getRowData();
        VDCUser user = bean.getUser();
        userService.setActiveStatus(bean.getUser().getId(),true);
        initUserData();  // Re-fetch list to reflect Delete action
        
        
    }
    
     public void deactivateUser(ActionEvent ae) {
        AllUsersDataBean bean=(AllUsersDataBean)dataTable.getRowData();
        VDCUser user = bean.getUser();
        userService.setActiveStatus(bean.getUser().getId(),false);
        initUserData();  // Re-fetch list to reflect Delete action
        
        
    }
    
    
    public HtmlDataTable getDataTable() {
        return dataTable;
    }
    
    public void setDataTable(HtmlDataTable dataTable) {
        this.dataTable = dataTable;
    }
    
    
}

