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
 * AddSitePage.java
 *
 * Created on September 19, 2006, 9:57 AM
 */
package edu.harvard.iq.dvn.core.web.site;
import com.icesoft.faces.component.ext.*;
import edu.harvard.iq.dvn.core.admin.RoleServiceLocal;
import edu.harvard.iq.dvn.core.admin.UserServiceLocal;
import edu.harvard.iq.dvn.core.mail.MailServiceLocal;
import edu.harvard.iq.dvn.core.study.StudyFieldServiceLocal;
import edu.harvard.iq.dvn.core.vdc.VDC;
import edu.harvard.iq.dvn.core.vdc.VDCCollectionServiceLocal;
import edu.harvard.iq.dvn.core.vdc.VDCGroupServiceLocal;
import edu.harvard.iq.dvn.core.vdc.VDCNetworkServiceLocal;
import edu.harvard.iq.dvn.core.vdc.VDCServiceLocal;
import edu.harvard.iq.dvn.core.web.common.StatusMessage;
import edu.harvard.iq.dvn.core.web.common.VDCBaseBean;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import edu.harvard.iq.dvn.core.admin.VDCUser;
import edu.harvard.iq.dvn.core.study.Template;
import edu.harvard.iq.dvn.core.study.TemplateServiceLocal;
import edu.harvard.iq.dvn.core.web.util.CharacterValidator;
import edu.harvard.iq.dvn.core.util.PropertyUtil;
import edu.harvard.iq.dvn.core.vdc.*;
import java.util.*;
import java.util.logging.Logger;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
@Named("AddSitePage")
@ViewScoped
public class AddSitePage extends VDCBaseBean implements java.io.Serializable  {

       // </editor-fold>
    private static final Logger logger = Logger.getLogger("edu.harvard.iq.dvn.core.web.site.AddSitePage");
    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public AddSitePage() {

    }

    @EJB
    VDCServiceLocal vdcService;
    @EJB
    VDCGroupServiceLocal vdcGroupService;
    @EJB
    VDCCollectionServiceLocal vdcCollectionService;
    @EJB
    VDCNetworkServiceLocal vdcNetworkService;
    @EJB
    StudyFieldServiceLocal studyFieldService;
    @EJB
    UserServiceLocal userService;
    @EJB
    RoleServiceLocal roleService;
    @EJB
    MailServiceLocal mailService;
    StatusMessage msg;
    
    //private BundleReader messagebundle = new BundleReader("Bundle");
    
    
    private ResourceBundle messagebundle = ResourceBundle.getBundle("Bundle");

    public StatusMessage getMsg() {
        return msg;
    }

    public void setMsg(StatusMessage msg) {
        this.msg = msg;
    }
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;
    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    public void init() {
        super.init();
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        Iterator iterator = request.getParameterMap().keySet().iterator();
        while (iterator.hasNext()) {
            Object key = (Object) iterator.next();
            if (key instanceof String && ((String) key).indexOf("dataverseType") != -1 && !request.getParameter((String) key).equals("")) {
                this.setDataverseType(request.getParameter((String) key));
            }
        }
        //check to see if a dataverse type is in request
         VDCNetwork checkForSubnetwork = getVDCRequestBean().getCurrentVdcNetwork();
         if (!checkForSubnetwork.equals(vdcNetworkService.findRootNetwork())) {
                selectSubNetworkId = checkForSubnetwork.getId();
            } else {

                selectSubNetworkId = new Long (0);
         }  
          networkSelectItems = loadNetworkSelectItems(); 
    }
    

    //copied from manageclassificationsPage.java
    private boolean result;
 
     //fields from dvrecordsmanager
    private ArrayList itemBeans = new ArrayList();
    private static String GROUP_INDENT_STYLE_CLASS = "GROUP_INDENT_STYLE_CLASS";
    private static String GROUP_ROW_STYLE_CLASS = "groupRow";
    private static String CHILD_INDENT_STYLE_CLASS = "CHILD_INDENT_STYLE_CLASS";
    private String CHILD_ROW_STYLE_CLASS;
    private static String CONTRACT_IMAGE = "tree_nav_top_close_no_siblings.gif";
    private static String EXPAND_IMAGE = "tree_nav_top_open_no_siblings.gif";

    //these static variables have a dependency on the Network Stats Server e.g.
    // they should be held as constants in a constants file ... TODO
    private static Long   SCHOLAR_ID                    = new Long("-1");
    private static String   SCHOLAR_SHORT_DESCRIPTION   = new String("A short description for the research scholar group");
    private static Long   OTHER_ID                      = new Long("-2");
    private static String   OTHER_SHORT_DESCRIPTION     = new String("A short description for the unclassified dataverses group (other).");
    
     //Manage classification
    
   


  
     public boolean getResult() {
        return result;
    }
     //setters

  

     public void setResult(boolean result) {
        this.result = result;
    }

 

    public ArrayList getItemBeans() {
        return itemBeans;
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



    private HtmlOutputLabel componentLabel1 = new HtmlOutputLabel();

    public HtmlOutputLabel getComponentLabel1() {
        return componentLabel1;
    }

    public void setComponentLabel1(HtmlOutputLabel hol) {
        this.componentLabel1 = hol;
    }
    private HtmlOutputText componentLabel1Text = new HtmlOutputText();

    public HtmlOutputText getComponentLabel1Text() {
        return componentLabel1Text;
    }

    public void setComponentLabel1Text(HtmlOutputText hot) {
        this.componentLabel1Text = hot;
    }
    private HtmlInputText dataverseName = new HtmlInputText();

    public HtmlInputText getDataverseName() {
        return dataverseName;
    }

    public void setDataverseName(HtmlInputText hit) {
        this.dataverseName = hit;
    }
    private HtmlOutputLabel componentLabel2 = new HtmlOutputLabel();

    public HtmlOutputLabel getComponentLabel2() {
        return componentLabel2;
    }

    public void setComponentLabel2(HtmlOutputLabel hol) {
        this.componentLabel2 = hol;
    }
    private HtmlOutputText componentLabel2Text = new HtmlOutputText();

    public HtmlOutputText getComponentLabel2Text() {
        return componentLabel2Text;
    }

    public void setComponentLabel2Text(HtmlOutputText hot) {
        this.componentLabel2Text = hot;
    }
    private HtmlInputText dataverseAlias = new HtmlInputText();

    public HtmlInputText getDataverseAlias() {
        return dataverseAlias;
    }

    public void setDataverseAlias(HtmlInputText hit) {
        this.dataverseAlias = hit;
    }

  
    

    private HtmlCommandButton button1 = new HtmlCommandButton();

    public HtmlCommandButton getButton1() {
        return button1;
    }

    public void setButton1(HtmlCommandButton hcb) {
        this.button1 = hcb;
    }
    private HtmlCommandButton button2 = new HtmlCommandButton();

    public HtmlCommandButton getButton2() {
        return button2;
    }

    public void setButton2(HtmlCommandButton hcb) {
        this.button2 = hcb;
    }

    // I'm initializing classificationList below in order to get the page 
    // to work; otherwise (if it's set to null), the page dies quietly in 
    // classificationList.getClassificationUIs() (in saveClassifications), 
    // after creating the new DV. 
    // I'm still not quite sure how/where this list was initialized before?
    ClassificationList classificationList = new ClassificationList();//null;

    public ClassificationList getClassificationList() {
        return classificationList;
    }

    public void setClassificationList(ClassificationList classificationList) {
        this.classificationList = classificationList;
    }

   

    public String create() {

    //    Long selectedgroup  = this.getSelectedGroup();
        String dtype        = dataverseType;
        String name         = (String) dataverseName.getValue();
        String alias        = (String) dataverseAlias.getValue();
        String strAffiliation = (String) affiliation.getValue();
        String strShortDescription = (String) shortDescription.getValue();
        Long userId = getVDCSessionBean().getLoginBean().getUser().getId();

        boolean success = true;
        if (validateClassificationCheckBoxes()) {
            vdcService.create(userId, name, alias, dtype);
            VDC createdVDC = vdcService.findByAlias(alias);
            saveClassifications(createdVDC);
            createdVDC.setDtype(dataverseType);
            createdVDC.setDisplayNetworkAnnouncements(getVDCRequestBean().getCurrentVdcNetwork().isDisplayAnnouncements());
            createdVDC.setDisplayAnnouncements(getVDCRequestBean().getCurrentVdcNetwork().isDisplayVDCAnnouncements());
            createdVDC.setDisplayNewStudies(getVDCRequestBean().getCurrentVdcNetwork().isDisplayVDCRecentStudies());
            createdVDC.setAboutThisDataverse(getVDCRequestBean().getCurrentVdcNetwork().getDefaultVDCAboutText());
            createdVDC.setContactEmail(getVDCSessionBean().getLoginBean().getUser().getEmail());
            createdVDC.setAffiliation(strAffiliation);
            createdVDC.setDvnDescription(strShortDescription);
            createdVDC.setAnnouncements(strShortDescription); // also set default dv home page description from the the DVN home page short description
            VDCNetwork vdcNetwork;
            
            if (selectSubNetworkId != null && selectSubNetworkId > 0){
                vdcNetwork = vdcNetworkService.findById(selectSubNetworkId);
                createdVDC.setVdcNetwork(vdcNetwork);
            } else {
                vdcNetwork = vdcNetworkService.findRootNetwork();
                createdVDC.setVdcNetwork(vdcNetwork);
            } 
            //Set template to the network's default template
            Template template = vdcNetwork.getDefaultTemplate();
            createdVDC.setDefaultTemplate(template); 
            
            //on create if description is blank uncheck display flag
            if(strShortDescription.isEmpty()){
                createdVDC.setDisplayAnnouncements(false);
            }
            vdcService.edit(createdVDC);

            String hostUrl = PropertyUtil.getHostUrl();
            VDCUser creator = userService.findByUserName(getVDCSessionBean().getLoginBean().getUser().getUserName());
            String toMailAddress = getVDCSessionBean().getLoginBean().getUser().getEmail();
            String siteAddress = hostUrl + "/dvn/dv/" + createdVDC.getAlias();
            
            logger.fine("created dataverse; site address: "+siteAddress);

            mailService.sendAddSiteNotification(toMailAddress, name, siteAddress);

            // Refresh User object in LoginBean so it contains the user's new role of VDC administrator.
            getVDCSessionBean().getLoginBean().setUser(creator);
            getVDCRenderBean().getFlash().put("successMessage","Your new dataverse has been created!");
            return "/site/AddSiteSuccessPage?faces-redirect=true&vdcId=" + createdVDC.getId();
        }
        else {
            success = false;
            return null;
        }

    }

    private void saveClassifications(VDC createdVDC) {
        for (ClassificationUI classUI: classificationList.getClassificationUIs()) {
            if (classUI.isSelected()) {
                createdVDC.getVdcGroups().add(classUI.getVdcGroup());
            }
        }
    }

    public String createScholarDataverse() {
        String dataversetype = dataverseType;
     
        String name = (String) dataverseName.getValue();
        String alias = (String) dataverseAlias.getValue();
        String strAffiliation = (String) affiliation.getValue();
        String strShortDescription = (String) shortDescription.getValue();
        Long userId = getVDCSessionBean().getLoginBean().getUser().getId();

        if (validateClassificationCheckBoxes()) {
            vdcService.createScholarDataverse(userId, firstName, lastName, name, strAffiliation, alias, dataversetype);
            VDC createdScholarDataverse = vdcService.findScholarDataverseByAlias(alias);
            saveClassifications(createdScholarDataverse);
  
            //  add default values to the VDC table and commit/set the vdc bean props
            createdScholarDataverse.setDisplayNetworkAnnouncements(getVDCRequestBean().getCurrentVdcNetwork().isDisplayAnnouncements());           
            createdScholarDataverse.setDisplayAnnouncements(getVDCRequestBean().getCurrentVdcNetwork().isDisplayVDCAnnouncements());
             //on create if description is blank uncheck display flag
            if(strShortDescription.isEmpty()){
                createdScholarDataverse.setDisplayAnnouncements(false);
            }
            createdScholarDataverse.setAnnouncements(getVDCRequestBean().getCurrentVdcNetwork().getDefaultVDCAnnouncements());
            createdScholarDataverse.setDisplayNewStudies(getVDCRequestBean().getCurrentVdcNetwork().isDisplayVDCRecentStudies());
            createdScholarDataverse.setAboutThisDataverse(getVDCRequestBean().getCurrentVdcNetwork().getDefaultVDCAboutText());
            createdScholarDataverse.setContactEmail(getVDCSessionBean().getLoginBean().getUser().getEmail());
            createdScholarDataverse.setDvnDescription(strShortDescription);
            createdScholarDataverse.setAnnouncements(strShortDescription); // also set default dv home page description from the the DVN home page short description
            VDCNetwork vdcNetwork;
            if (selectSubNetworkId != null && selectSubNetworkId > 0){
                 vdcNetwork = vdcNetworkService.findById(selectSubNetworkId);
                createdScholarDataverse.setVdcNetwork(vdcNetwork);
            } else {
                 vdcNetwork = vdcNetworkService.findRootNetwork();
                createdScholarDataverse.setVdcNetwork(vdcNetwork);
            }
            //Set default template to subnet's default template
            Template template = vdcNetwork.getDefaultTemplate();
            createdScholarDataverse.setDefaultTemplate(template); 
            
            vdcService.edit(createdScholarDataverse);
    
            String hostUrl = PropertyUtil.getHostUrl();           
            VDCUser creator = userService.findByUserName(getVDCSessionBean().getLoginBean().getUser().getUserName());
            String toMailAddress = getVDCSessionBean().getLoginBean().getUser().getEmail();
            String siteAddress = hostUrl + "/dvn/dv/" + createdScholarDataverse.getAlias();

            mailService.sendAddSiteNotification(toMailAddress, name, siteAddress);

            // Refresh User object in LoginBean so it contains the user's new role of VDC administrator.
            getVDCSessionBean().getLoginBean().setUser(creator);
            getVDCRenderBean().getFlash().put("successMessage","Your new dataverse has been created!");
            return "/site/AddSiteSuccessPage?faces-redirect=true&vdcId=" + createdScholarDataverse.getId();
        }
        else {
            return null;
        }

    }
    
    public String cancel() {
        VDCUser user = getVDCSessionBean().getLoginBean().getUser();
        if (user.isNetworkAdmin()) {
            return "/networkAdmin/NetworkOptionsPage?faces-redirect=true";
        } else {
            return "/login/AccountOptionsPage?faces-redirect=true&userId="+user.getId();
        }
    }



    public boolean validateClassificationCheckBoxes() {

        if (!getVDCRequestBean().getCurrentVdcNetwork().isRequireDVclassification()){
            return true;
        }
        else {
            for (ClassificationUI classUI: classificationList.getClassificationUIs()) {
                if (classUI.isSelected()) {
                    return true;
                }
            }

            FacesMessage message = new FacesMessage("You must select at least one classification for your dataverse.");
            FacesContext.getCurrentInstance().addMessage("addsiteform", message);
            return false;
        }

    }



    public void validateShortDescription(FacesContext context,
                UIComponent toValidate,
                Object value) {
            String newValue = (String)value;
            if (newValue != null && newValue.trim().length() > 0) {
                if (newValue.length() > 255) {
                    ((UIInput)toValidate).setValid(false);
                    FacesMessage message = new FacesMessage("The field cannot be more than 255 characters in length.");
                    context.addMessage(toValidate.getClientId(context), message);
                }
            }
            if ((newValue == null || newValue.trim().length() == 0) && getVDCRequestBean().getCurrentVdcNetwork().isRequireDVdescription()) {
                FacesMessage message = new FacesMessage("The field must have a value.");
                context.addMessage(toValidate.getClientId(context), message);
                ((UIInput) toValidate).setValid(false);
                context.renderResponse();
            }

        }

    public void validateName(FacesContext context,
            UIComponent toValidate,
            Object value) {
        String name = (String) value;
        if (name != null && name.trim().length() == 0) {
            FacesMessage message = new FacesMessage("The dataverse name field must have a value.");
            context.addMessage(toValidate.getClientId(context), message);
            context.renderResponse();
        }
        boolean nameFound = false;
        VDC vdc = vdcService.findByName(name);
        if (vdc != null) {
            nameFound = true;
        }
        if (nameFound) {
            ((UIInput) toValidate).setValid(false);

            FacesMessage message = new FacesMessage("This name is already taken.");
            context.addMessage(toValidate.getClientId(context), message);
        }

        resetScholarProperties();
    }

    public void validateAlias(FacesContext context,
            UIComponent toValidate,
            Object value) {
        CharacterValidator charactervalidator = new CharacterValidator();
        charactervalidator.validate(context, toValidate, value);
        String alias = (String) value;

        boolean isValid = false;
        VDC vdc = vdcService.findByAlias(alias);
        if (alias.equals("") || vdc != null) {
            isValid = true;
        }

        if (isValid) {
            ((UIInput) toValidate).setValid(false);

            FacesMessage message = new FacesMessage("This alias is already taken.");
            context.addMessage(toValidate.getClientId(context), message);
        }
        resetScholarProperties();
    }

    private void resetScholarProperties() {
        if (dataverseType != null) {
            this.setDataverseType(dataverseType);
        }
    }
    /**
     * Changes for build 16
     * to support scholar
     * dataverses and display
     *
     * @author wbossons
     */
    
    private List<SelectItem> loadNetworkSelectItems() {
        List selectItems = new ArrayList<SelectItem>();
        List<VDCNetwork> networkList = vdcNetworkService.getVDCSubNetworks();

        if (networkList.size() > 0) {
            selectItems.add(new SelectItem(0, "<None>"));
            for (VDCNetwork vdcNetwork : networkList) {
                selectItems.add(new SelectItem(vdcNetwork.getId(), vdcNetwork.getName()));
            }
        }
        return selectItems;
    }

    private List <SelectItem> networkSelectItems = new ArrayList();

    public List<SelectItem> getNetworkSelectItems() {
        return this.networkSelectItems;
    }
    
    private HtmlSelectOneMenu selectSubnetwork;   
    public HtmlSelectOneMenu getSelectSubnetwork() {return selectSubnetwork;}
    public void setSelectSubnetwork(HtmlSelectOneMenu selectSubnetwork) {this.selectSubnetwork = selectSubnetwork;}
    /**
     * Used to set the discriminator value
     * in the entity
     *
     */
    private String dataverseType = null;

    public String getDataverseType() {
        if (dataverseType == null) {
            setDataverseType("Basic");
        }
        return dataverseType;
    }

    public void setDataverseType(String dataverseType) {
        this.dataverseType = dataverseType;
    }
    /**
     * Used to set the discriminator value
     * in the entity
     *
     */
    private String selected = null;

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }
    /**
     * set the possible options
     *
     *
     */

    private List<SelectItem> dataverseOptions = null;

    public List getDataverseOptions() {
        if (this.dataverseOptions == null) {
            dataverseOptions = new ArrayList();
            /**
             * Choose Scholar if this dataverse will have your 
             * own name and will contain your own research, 
             * and Basic for any other dataverse.
             * 
             * 
               Select the group that will most likely fit your 
             * dataverse, be it a university department, a journal, 
             * a research center, etc. If you create a Scholar dataverse, 
             * it will be automatically entered under the Scholar group.        
             * 
             */
            try {
                String scholarOption       = messagebundle.getString("scholarOption");
                String basicOption          = messagebundle.getString("basicOption");
                String scholarLabel        = messagebundle.getString("scholarOptionDetail");
                String basicLabel          = messagebundle.getString("basicOptionDetail");
                dataverseOptions.add(new SelectItem(basicOption, basicLabel));
                dataverseOptions.add(new SelectItem(scholarOption, scholarLabel));
            } catch (Exception uee) {
                System.out.println("Exception:  " + uee.toString());
            }
        }
        return dataverseOptions;
    }
    /**
     * Holds value of property firstName.
     */
    private String firstName = new String("");

    /**
     * Getter for property firstName.
     * @return Value of property firstName.
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Setter for property firstName.
     * @param firstName New value of property firstName.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    /**
     * Holds value of property lastName.
     */
    private String lastName;

    /**
     * Getter for property lastName.
     * @return Value of property lastName.
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Setter for property lastName.
     * @param lastName New value of property lastName.
     */
    public void setLastName(String lastname) {
        this.lastName = lastname;
    }
    /**
     * Holds value of property affiliation.
     */
    private HtmlInputText affiliation;

    /**
     * Getter for property affiliation.
     * @return Value of property affiliation.
     */
    public HtmlInputText getAffiliation() {
        return this.affiliation;
    }

    public HtmlInputTextarea getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(HtmlInputTextarea shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * Setter for property affiliation.
     * @param affiliation New value of property affiliation.
     */
    public void setAffiliation(HtmlInputText affiliation) {
        this.affiliation = affiliation;
    }

    HtmlInputTextarea shortDescription;

    //END Group Select widgets
    /**
     * value change listeners and validators
     *
     *
     */
    
    private Long selectSubNetworkId;

    public Long getSelectSubNetworkId() {
        return selectSubNetworkId;
    }

    public void setSelectSubNetworkId(Long selectSubNetworkId) {
        this.selectSubNetworkId = selectSubNetworkId;
    }


    public void changeDataverseOption(ValueChangeEvent event) {
        String newValue = (String) event.getNewValue();
        this.setDataverseType(newValue);
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        request.setAttribute("dataverseType", newValue);
        FacesContext.getCurrentInstance().renderResponse();
    }

    public void changeFirstName(ValueChangeEvent event) {
        String newValue = (String) event.getNewValue();
        this.setFirstName(newValue);
    }

    public void changeLastName(ValueChangeEvent event) {
        String newValue = (String) event.getNewValue();
        this.setLastName(newValue);
    }

    public void validateIsEmpty(FacesContext context,
            UIComponent toValidate,
            Object value) {
        String newValue = (String) value;
        if (newValue == null || newValue.trim().length() == 0) {
            ((UIInput) toValidate).setValid(false);
            FacesMessage message = new FacesMessage("The field must have a value.");
            context.addMessage(toValidate.getClientId(context), message);
            context.renderResponse();
        }
    }
    public void validateIsEmptyRequiredAffiliation(FacesContext context,
            UIComponent toValidate,
            Object value) {
        String newValue = (String) value;
        if ((newValue == null || newValue.trim().length() == 0) && getVDCRequestBean().getCurrentVdcNetwork().isRequireDVaffiliation()) {
                FacesMessage message = new FacesMessage("The field must have a value.");
                context.addMessage(toValidate.getClientId(context), message);
                context.renderResponse();
                ((UIInput) toValidate).setValid(false);
            }
    }

}

