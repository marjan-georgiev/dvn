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
 * VDCRequestBean.java
 *
 * Created on November 1, 2005, 8:42 AM
 */
package edu.harvard.iq.dvn.core.web.common;

import edu.harvard.iq.dvn.core.admin.LockssAuthServiceLocal;
import edu.harvard.iq.dvn.core.admin.PageDef;
import edu.harvard.iq.dvn.core.admin.PageDefServiceLocal;
import edu.harvard.iq.dvn.core.util.PropertyUtil;
import edu.harvard.iq.dvn.core.util.StringUtil;
import edu.harvard.iq.dvn.core.vdc.VDC;
import edu.harvard.iq.dvn.core.vdc.VDCNetwork;
import edu.harvard.iq.dvn.core.vdc.VDCNetworkServiceLocal;
import edu.harvard.iq.dvn.core.web.StudyListing;
import javax.ejb.EJB;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * <p>Request scope data bean for your application.  Create properties
 *  here to represent data that should be made available across different
 *  pages in the same HTTP request, so that the page bean classes do not
 *  have to be directly linked to each other.</p>
 *
 * <p>An instance of this class will be created for you automatically,
 * the first time your application evaluates a value binding expression
 * or method binding expression that references a managed bean using
 * this class.</p>
 */
@Named("VDCRequest")
@ViewScoped
public class VDCRequestBean extends VDCBaseBean implements java.io.Serializable  {
    @EJB PageDefServiceLocal pageDefService;
    @EJB LockssAuthServiceLocal lockssAuthService;
    @EJB VDCNetworkServiceLocal networkService;
   
    /** 
     * <p>Construct a new request data bean instance.</p>
     */
    public VDCRequestBean() {  
    }

    public String getRequestParam(String name) {
        return ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter(name);
    }    
        
    
    /**
     * Holds value of property studyId.
     */
    private Long studyId;

    /**
     * Getter for property editMode.
     * @return Value of property editMode.
     */
    public Long getStudyId() {
        return this.studyId;
    }

    /**
     * Setter for property editMode.
     * @param editMode New value of property editMode.
     */
    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    private Long studyVersionNumber;

    public Long getStudyVersionNumber() {
        return studyVersionNumber;
    }

    public void setStudyVersionNumber(Long studyVersionNumber) {
        this.studyVersionNumber = studyVersionNumber;
    }

    private String studyVersionNumberList;

    public String getStudyVersionNumberList() {
        return studyVersionNumberList;
    }

    public void setStudyVersionNumberList(String studyVersionNumberList) {
        this.studyVersionNumberList = studyVersionNumberList;
    }

    
/**
     *  This parsing logic is separated out so it can be called
     *
     * @param HttpServletRequest - to get versionNumberList parameter, which
     * should be a String of two integers, separatated by ","
     * @return null if "versionNumberList" is not found in the list of request parameters
     * @throws NumberFormatException if tokens in parameter aren't Longs
     * @throws IllegalArgumentException if parameter value cannot be parsed into tokens separated by ","
     */
    public static Long[] parseVersionNumberList(HttpServletRequest request) {
        String strList = VDCBaseBean.getParamFromRequestOrComponent("versionNumberList", request);
        Long[] versionNumbers = null;
        if (!StringUtil.isEmpty(strList)) {
            String[] versionNumTokens = strList.split(",");
            versionNumbers = new Long[2];
           
            try {
                for (int i = 0; i < versionNumTokens.length && i < 2; i++) {
                    if (versionNumTokens[i] != null) {

                        versionNumbers[i] = new Long(versionNumTokens[i]);

                    }
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot parse versionNumberList, string=" + strList + ", request = " + request);
            }
        }
        return versionNumbers;
    }

    private String selectedTab;

    public String getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(String selectedTab) {
        this.selectedTab = selectedTab;
    }    

    private String actionMode;

    public String getActionMode() {
        return actionMode;
    }

    public void setActionMode(String am) {
        this.actionMode = am;
    }



    private Boolean logoutPage = null;

    public boolean isLogoutPage() {
        if (logoutPage == null) {
            HttpServletRequest httpRequest = (HttpServletRequest)this.getExternalContext().getRequest();
            PageDef pageDef = pageDefService.findByPath(httpRequest.getPathInfo());
            if (pageDef!=null && pageDef.getName().equals(PageDefServiceLocal.LOGOUT_PAGE)) {
                logoutPage = Boolean.TRUE;
            } else {
                logoutPage = Boolean.FALSE;
            }
        }

        return logoutPage.booleanValue();

    }   
    
    private Boolean readOnlyDatabase = null;
    
    public boolean isReadOnlyDatabase() {
            if (networkService.defaultTransactionReadOnly()) {
                readOnlyDatabase = Boolean.TRUE;
            } else {
                readOnlyDatabase = Boolean.FALSE;
            }
        return readOnlyDatabase.booleanValue();
    } 

    public String getPageDefName() {

        HttpServletRequest httpRequest = (HttpServletRequest)this.getExternalContext().getRequest();
        PageDef pageDef = pageDefService.findByPath(httpRequest.getPathInfo());
        if (pageDef!=null) {
                return pageDef.getName();
        }

        return "";

    }


    private VDCNetwork rootNetwork;
    private VDCNetwork currentSubnetwork;    
    private VDC currentVDC;  
    private boolean currentVDCinitialized;
    private boolean currentVDCNetworkinitialized;
    
     /**
     * Getter for property vdcId.
     * @return Value of property vdcId.
     */
    public VDC getCurrentVDC() {
        if (!currentVDCinitialized) {
            HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            setCurrentVDC( vdcService.getVDCFromRequest(req) );
            // set method also sets initialization boolean to true
        }        
        return currentVDC;
    }    

    public void setCurrentVDC(VDC currentVDC) {
        this.currentVDC = currentVDC;
        currentVDCinitialized = true;
    }
    

    /**
     * Getter for property vdcNetwork.
     * @return Value of property vdcNetwork.
     */
    public VDCNetwork getCurrentVdcNetwork() {
        return (getCurrentSubnetwork() != null ? getCurrentSubnetwork() : getVdcNetwork());
    }    
    
    public VDCNetwork getCurrentSubnetwork() {
        if (!currentVDCNetworkinitialized) {
            HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            currentSubnetwork = vdcNetworkService.getVDCNetworkFromRequest(req);
            currentVDCNetworkinitialized = true;
        }
        
        return currentSubnetwork;
    }

    
    public Long getCurrentVDCId() {
        if (getCurrentVDC() != null) {
            return getCurrentVDC().getId();
        } 
        
        return null;
    }
    
    public void setCurrentVDCId(Long id) {}  // dummy method since the get is just a wrapper around currentVDC

    public String getCurrentVDCURL() {
        String dataverseURL="";
        if (getCurrentVDC() != null) { 
            dataverseURL +="/dv/"+getCurrentVDC().getAlias();
        } else if (getCurrentVdcNetwork().getId().intValue() > 0) { 
            dataverseURL +="/dataverses/"+getCurrentVdcNetwork().getUrlAlias();
        }
        // needed for error page when service is passed, but still an exception thrown in the app. wjb Sept 2007
        HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        request.setAttribute("dataverseURL", dataverseURL); 
        // end code for exception case
        return dataverseURL;
    }
    
    public String getCurrentVdcNetworkURL() {
        String networkURL="";
        if (getCurrentSubnetwork() != null) { 
            networkURL +="/dataverses/"+getCurrentSubnetwork().getUrlAlias();
        }  
        HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        request.setAttribute("networkURL", networkURL); 
        // TODO: networkURL is not beng used in any way; we need to refactor the error handling
        return networkURL;
    }
    
    
    public void setCurrentVDCURL(String dataverseURL) {}  // dummy method since the get is just a wrapper 

    public String getRequestedPage() {
            HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
            return "/faces/" + request.getPathInfo();
        }
    

    

    
    public VDCNetwork getVdcNetwork() {
        if (rootNetwork == null) {
            rootNetwork = vdcNetworkService.findRootNetwork();
        }
        return rootNetwork;
    }

    /**
     * Setter for property vdcNetwork.
     * @param vdcNetwork New value of property vdcNetwork.
     */
    public void setVdcNetwork(VDCNetwork rootNetwork) {
        this.rootNetwork = rootNetwork;
    }
    
    private StudyListing studyListing;

    public StudyListing getStudyListing() {
        return studyListing;
    }

    public void setStudyListing(StudyListing studyListing) {
        this.studyListing = studyListing;
    }

    private Long dtId;
    
    public Long getDtId() {
        return dtId;
    }

    public void setDtId(Long dtId) {
        this.dtId = dtId;
    }
    
    private String dvFilter;

    public String getDvFilter() {
        return dvFilter;
    }

    public void setDvFilter(String dvFilter) {
        this.dvFilter = dvFilter;
    }
    
    public String getHostUrl() {
        return PropertyUtil.getHostUrl();
    }  
    
    public String home() {
        if (getCurrentVDC() != null) {
            return "/StudyListingPage.xhtml?faces-redirect=true" + getContextSuffix();
        } else {
            return "/HomePage.xhtml?faces-redirect=true" + getContextSuffix();
        }
    }


    String studyListingIndex;

    public String getStudyListingIndex() {
        return studyListingIndex;
    }

    public void setStudyListingIndex(String studyListingIndex) {
        if (studyListingIndex != null) {
            // check if index from this session
            String sessionId = ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false)).getId();
            if (!sessionId.equals(studyListingIndex.substring(studyListingIndex.indexOf("_") + 1))) {
                this.studyListingIndex = null;
            } else {
                this.studyListingIndex = studyListingIndex;
            }
        }
    }


    public String getStudyListingIndexAsParameter() {
        //String studyListingIndex = getVDCRequestBean().getStudyListingIndex();
        return studyListingIndex != null ? "&studyListingIndex=" + studyListingIndex : "";
    }
    
    public String getDataversePageTitle() {
        String title = getVdcNetwork().getName() + " Dataverse Network";

        //add vdc or subnetwork name (as prefix) if available
        if (this.getCurrentVDC()!=null) {
            title = getCurrentVDC().getName() + " Dataverse - " + title;
        } else if (getCurrentSubnetwork() != null) {
            title = getCurrentSubnetwork().getName() + " Dataverses " + title;
        }
         
        return title;
    }


    public boolean isAuthorizedLockssServer(){
        HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return lockssAuthService.isAuthorizedLockssServer(currentVDC, request );
    }

    public boolean isRenderManifestLink() {
        LoginBean loginBean = getVDCSessionBean().getLoginBean();
        if (currentVDC == null) {
            if ( (!isLogoutPage() && loginBean != null && loginBean.isNetworkAdmin() ) || isAuthorizedLockssServer() ) {
                return (vdcNetworkService.getLockssConfig() != null);
            }

        } else {
             if ( (!isLogoutPage() && loginBean != null && (loginBean.isAdmin() || loginBean.isNetworkAdmin()))  || isAuthorizedLockssServer()   ) {
                return (currentVDC.getLockssConfig() != null);
             }


        }
        return false;
    }

    private boolean isDisableCustomization() {
        String vdcAlias = getCurrentVDC() != null ? getCurrentVDC().getAlias() : "";
        String disableCustomizationParam = getRequestParam("disableCustomization");

        if (disableCustomizationParam != null) {
            if ("true".equals(disableCustomizationParam) ){
                sessionPut("disableCustomization", vdcAlias);
            } else if ("false".equals(disableCustomizationParam) ){
               sessionPut("disableCustomization", null);
            }
        }

        return vdcAlias.equals(sessionGet("disableCustomization"));
    }

    public boolean isDisplayInFrame() {
        // only valid for VDCs
        if (getCurrentVDC() != null) {
            return getCurrentVDC().isDisplayInFrame() && !isDisableCustomization();
        }

        return false;
    }

    public boolean isDisplayDVNCustomization() {
        if (getCurrentVDC() != null) {
            return !getCurrentVDC().isDisplayInFrame() && !isDisableCustomization();
        } else {
            return  !isDisableCustomization();
        }
    }

}
