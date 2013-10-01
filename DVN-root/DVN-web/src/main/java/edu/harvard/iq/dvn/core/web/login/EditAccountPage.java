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
 * AddAccountPage.java
 *
 * Created on October 4, 2006, 1:04 PM
 */
package edu.harvard.iq.dvn.core.web.login;

import edu.harvard.iq.dvn.core.admin.EditUserService;
import edu.harvard.iq.dvn.core.admin.UserServiceLocal;
import edu.harvard.iq.dvn.core.admin.VDCUser;
import edu.harvard.iq.dvn.core.web.util.CharacterValidator;
import edu.harvard.iq.dvn.core.web.common.StatusMessage;
import edu.harvard.iq.dvn.core.web.common.VDCBaseBean;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import com.icesoft.faces.component.ext.HtmlSelectOneRadio;
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
@Named("EditAccountPage")
public class EditAccountPage extends VDCBaseBean implements java.io.Serializable  {
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;
    
    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
      }
   
    
    // </editor-fold>
    @EJB EditUserService editUserService;
    @EJB UserServiceLocal userService;
    private Long userId;
    private VDCUser user;
    private String returnPage;

    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public EditAccountPage() {
    }
    
    
    
    public void init() {
        if ("viewAccount".equals(returnPage)) {
            returnPage = "/login/AccountPage.xhtml?faces-redirect=true&userId="+userId;
        } else {
            returnPage = "/login/AccountOptionsPage?faces-redirect=true&userId="+userId;
        }
        
        editUserService.setUser(userId);        
        user = editUserService.getUser();       
        
    }
       
     
    
  
    public void validateUserName(FacesContext context, 
                          UIComponent toValidate,
                          Object value) {
    CharacterValidator charactervalidator = new CharacterValidator();
    charactervalidator.validateWEmail(context, toValidate, value);
    String userName = (String) value;
    
    boolean userNameFound = false;
    VDCUser foundUser = userService.findByUserName(userName);
    if (foundUser!=null && !foundUser.getId().equals(user.getId())) {
        userNameFound=true;
    }
    
    if (userNameFound) {
       ((UIInput)toValidate).setValid(false);

        FacesMessage message = new FacesMessage("This Username is already taken.");
        context.addMessage(toValidate.getClientId(context), message);
    }

}
    
    public String saveAction() {        
        editUserService.save();
        // If the currently logged-in user is updating is account, reset the User object in the session
        if (getVDCSessionBean().getLoginBean().getUser().getId().equals(user.getId())) {
            this.getVDCSessionBean().getLoginBean().setUser(user);
        }

        getVDCRenderBean().getFlash().put("successMessage","User account updated successfully."); 
        return returnPage;
    }
    
    public String cancelAction() {
        editUserService.cancel();
        return returnPage;
    }
    
    public EditUserService getEditUserService() {
        return editUserService;
    }
    

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

    public String getReturnPage() {
        return returnPage;
    }

    public void setReturnPage(String returnPage) {
        this.returnPage = returnPage;
    }

}

