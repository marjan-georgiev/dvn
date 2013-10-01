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
 * StudyPage.java
 *
 * Created on September 5, 2006, 4:25 PM
 */
package edu.harvard.iq.dvn.core.web.study;


import com.icesoft.faces.component.ext.HtmlCommandButton;
//Commenting out: VWP component. -- L.A. 
//import com.sun.jsfcl.data.DefaultTableDataModel;
import edu.harvard.iq.dvn.core.study.EditStudyService;
import edu.harvard.iq.dvn.core.study.Study;
import edu.harvard.iq.dvn.core.study.StudyAbstract;
import edu.harvard.iq.dvn.core.study.StudyAuthor;
import edu.harvard.iq.dvn.core.study.StudyDistributor;
import edu.harvard.iq.dvn.core.study.StudyGeoBounding;
import edu.harvard.iq.dvn.core.study.StudyGrant;
import edu.harvard.iq.dvn.core.study.StudyKeyword;
import edu.harvard.iq.dvn.core.study.StudyNote;
import edu.harvard.iq.dvn.core.study.StudyOtherId;
import edu.harvard.iq.dvn.core.study.StudyOtherRef;
import edu.harvard.iq.dvn.core.study.StudyProducer;
import edu.harvard.iq.dvn.core.study.StudyRelMaterial;
import edu.harvard.iq.dvn.core.study.StudyRelPublication;
import edu.harvard.iq.dvn.core.study.StudyRelStudy;
import edu.harvard.iq.dvn.core.study.StudyServiceLocal;
import edu.harvard.iq.dvn.core.study.StudySoftware;
import edu.harvard.iq.dvn.core.study.StudyTopicClass;
import edu.harvard.iq.dvn.core.study.TemplateField;
import edu.harvard.iq.dvn.core.web.util.SessionCounter;
import edu.harvard.iq.dvn.core.util.StringUtil;
import edu.harvard.iq.dvn.core.study.StudyFieldConstant;
import edu.harvard.iq.dvn.core.web.common.VDCBaseBean;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import com.icesoft.faces.component.ext.HtmlDataTable;
import com.icesoft.faces.component.ext.HtmlInputText;
import com.icesoft.faces.component.ext.HtmlInputTextarea;
import com.icesoft.faces.component.ext.HtmlSelectBooleanCheckbox;
import com.icesoft.faces.component.ext.HtmlSelectOneMenu;
import com.icesoft.faces.component.ext.HtmlSelectOneRadio;
import com.icesoft.faces.component.panelseries.PanelSeries;
import com.icesoft.faces.context.effects.JavascriptContext;
import edu.harvard.iq.dvn.core.doi.DOIEZIdServiceLocal;
import edu.harvard.iq.dvn.core.study.ControlledVocabularyValue;
import edu.harvard.iq.dvn.core.study.Metadata;
import edu.harvard.iq.dvn.core.study.MetadataFieldGroup;
import edu.harvard.iq.dvn.core.study.StudyField;
import edu.harvard.iq.dvn.core.study.StudyFieldValue;
import edu.harvard.iq.dvn.core.study.TemplateServiceLocal;
import java.util.Date;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */

@Named("EditStudyPage")
@ViewScoped
@EJB(name="editStudy", beanInterface=edu.harvard.iq.dvn.core.study.EditStudyService.class)
public class EditStudyPage extends VDCBaseBean implements java.io.Serializable  {
    EditStudyService editStudyService;
    @EJB StudyServiceLocal studyService;
    @EJB TemplateServiceLocal templateService;
    @Inject private VersionNotesPopupBean versionNotesPopup;
    @EJB DOIEZIdServiceLocal doiEZIdService;
    /**
     * <p>Construct a new Page bean instance.</p>
     */
     public EditStudyPage() {
        
    }
     
   public void preRenderView() {
       super.preRenderView();
       // add javascript call on each partial submit to initialize the help tips for added fields
       JavascriptContext.addJavascriptCall(getFacesContext(),"initInlineHelpTip();");
   }     
    
    
    public void init() {
        super.init();
        

        try {
            Context ctx = new InitialContext();
            editStudyService = (EditStudyService) ctx.lookup("java:comp/env/editStudy");

        } catch (NamingException e) {
            e.printStackTrace();
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage errMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null);
            context.addMessage(null, errMessage);

        }
        if (getStudyId() != null) {
            editStudyService.setStudyVersion(studyId);
            study = editStudyService.getStudyVersion().getStudy();             
            metadata = editStudyService.getStudyVersion().getMetadata();
            currentTitle = metadata.getTitle();
            setFiles(editStudyService.getCurrentFiles());

        } else {
            
            Long vdcId = getVDCRequestBean().getCurrentVDC().getId();
            selectTemplateId = getVDCRequestBean().getCurrentVDC().getDefaultTemplate().getId();
            editStudyService.newStudy(vdcId, getVDCSessionBean().getLoginBean().getUser().getId(), selectTemplateId);
            study = editStudyService.getStudyVersion().getStudy();
            metadata = study.getLatestVersion().getMetadata();
            studyId = SessionCounter.getNext();


            study.setStudyId(studyService.generateStudyIdSequence(study.getProtocol(), study.getAuthority()));
            // prefill date of deposit
            metadata.setDateOfDeposit(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            setFiles(editStudyService.getCurrentFiles());
        }
        // Add empty first element to subcollections, so the input text fields will be visible
        metadata.initCollections();
        
        // Initialize map containing required/recommended settings for all fields
        initStudyFields();

        //  initDvnDates();
        
       // doiEZIdService.test();
    }
    
    private String getStudyIdFromRequest() {
        HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String studyIdParam=request.getParameter("studyId");
        if (studyIdParam==null) {
            Iterator iter = request.getParameterMap().keySet().iterator();
            while (iter.hasNext()) {
                Object key = (Object) iter.next();
                if ( key instanceof String && ((String) key).indexOf("studyId") != -1 ) {
                    studyIdParam = request.getParameter((String)key);
                    break;
                }
            }
        }
        return studyIdParam;
        
    }    
    
    
    /**
     * <p>Callback method that is called after the component tree has been
     * restored, but before any event processing takes place.  This method
     * will <strong>only</strong> be called on a postback request that
     * is processing a form submit.  Customize this method to allocate
     * resources that will be required in your event handlers.</p>
     */
    public void preprocess() {
        System.out.println("in preprocess");
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
        System.out.println("in prerender");
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
    
  
    
   
    
    public boolean getShowTemplateList() {
        if ( editStudyService.isNewStudy() ) {
            Map templatesMap = getTemplatesMap();
            return templatesMap != null && templatesMap.size() > 1;
        }

        return false;
    }
    
    public String changeTemplateAction() {
        Object value= this.selectTemplate.getValue();
        if (value!=null ) {           
            editStudyService.changeTemplate((Long)value);
            metadata = editStudyService.getStudyVersion().getMetadata();
            JavascriptContext.addJavascriptCall(getFacesContext(),"dataCitationWidgetSync();");
        }
        initStudyFields();  // Reset Recommended flag for all fields
        return "";
     }
     
    /**
     * Holds value of property study.
     */
    private Study study;
    private Metadata metadata;
    private String currentTitle;
    
    /**
     * Getter for property study.
     * @return Value of property study.
     */
    public Study getStudy() {
        
        return this.study;
    }
    
    /**
     * Setter for property study.
     * @param study New value of property study.
     */
    public void setStudy(Study study) {
        this.study = study;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    public void setCurrentTitle(String currentTitle) {
        this.currentTitle = currentTitle;
    }
   
    //private DefaultTableDataModel dataTable5Model = new DefaultTableDataModel();
    
    //public DefaultTableDataModel getDataTable5Model() {
    //    return dataTable5Model;
    //}
    
    //public void setDataTable5Model(DefaultTableDataModel dtdm) {
    //    this.dataTable5Model = dtdm;
    //}
    
    
    private Map studyMap;
    
    public Map getStudyMap() {
        return studyMap;
    }
    
    public void initStudyFields() {
        // first, let's get the values into the transient study field list of metadata
        metadata.getStudyFields();
        //and remove from the regular list or they will still get saved!
        metadata.getStudyFieldValues().clear();

        String controlledVocabularyUpdateMessage = "";
        String errorMessage = "";
        studyMap = new HashMap();
        for (Iterator<TemplateField> it = study.getTemplate().getTemplateFields().iterator(); it.hasNext();) {
            TemplateField tf = it.next();
            if (!tf.getStudyField().isCustomField()) {
                StudyMapValue smv = new StudyMapValue();
                smv.setTemplateFieldUI(new TemplateFieldUI(tf));
                studyMap.put(tf.getStudyField().getName(), smv);
            }
            if (tf.getControlledVocabulary() != null) {
                errorMessage += verifyControlledVocabValues(tf);
            }
        }
        if (!errorMessage.isEmpty()) {
            controlledVocabularyUpdateMessage = "Please review the following data entries:";
            controlledVocabularyUpdateMessage += errorMessage;
            getVDCRenderBean().getFlash().put("warningMessage", controlledVocabularyUpdateMessage);
        }
    }
    
    private String getMetadataValueForControlledVocabularyValidation(StudyField studyField){
        if (studyField.getName().equals(StudyFieldConstant.productionPlace)){
            return metadata.getProductionPlace();
        }
        if (studyField.getName().equals(StudyFieldConstant.fundingAgency)){
            return metadata.getFundingAgency();
        }
        if (studyField.getName().equals(StudyFieldConstant.depositor)){
            return metadata.getDepositor();
        }
        if (studyField.getName().equals(StudyFieldConstant.country)){
            return metadata.getCountry();
        }
        if (studyField.getName().equals(StudyFieldConstant.geographicCoverage)){
            return metadata.getGeographicCoverage();
        }
        if (studyField.getName().equals(StudyFieldConstant.geographicUnit)){
            return metadata.getGeographicUnit();
        }
        if (studyField.getName().equals(StudyFieldConstant.unitOfAnalysis)){
            return metadata.getUnitOfAnalysis();
        }
        if (studyField.getName().equals(StudyFieldConstant.universe)){
            return metadata.getUniverse();
        }
        if (studyField.getName().equals(StudyFieldConstant.kindOfData)){
            return metadata.getKindOfData();
        } 
        if (studyField.getName().equals(StudyFieldConstant.timeMethod)){
            return metadata.getTimeMethod();
        }
        if (studyField.getName().equals(StudyFieldConstant.dataCollector)){
            return metadata.getDataCollector();
        }
        if (studyField.getName().equals(StudyFieldConstant.frequencyOfDataCollection)){
            return metadata.getFrequencyOfDataCollection();
        }
        if (studyField.getName().equals(StudyFieldConstant.samplingProcedure)){
            return metadata.getSamplingProcedure();
        }
        if (studyField.getName().equals(StudyFieldConstant.deviationsFromSampleDesign)){
            return metadata.getDeviationsFromSampleDesign();
        }
        if (studyField.getName().equals(StudyFieldConstant.collectionMode)){
            return metadata.getCollectionMode();
        }
        if (studyField.getName().equals(StudyFieldConstant.researchInstrument)){
            return metadata.getResearchInstrument();
        }
        if (studyField.getName().equals(StudyFieldConstant.dataSources)){
            return metadata.getDataSources();
        }
        if (studyField.getName().equals(StudyFieldConstant.originOfSources)){
            return metadata.getOriginOfSources();
        }
        if (studyField.getName().equals(StudyFieldConstant.characteristicOfSources)){
            return metadata.getCharacteristicOfSources();
        }
        if (studyField.getName().equals(StudyFieldConstant.accessToSources)){
            return metadata.getAccessToSources();
        }
        if (studyField.getName().equals(StudyFieldConstant.dataCollectionSituation)){
            return metadata.getDataCollectionSituation();
        }
        if (studyField.getName().equals(StudyFieldConstant.actionsToMinimizeLoss)){
            return metadata.getActionsToMinimizeLoss();
        }
        if (studyField.getName().equals(StudyFieldConstant.controlOperations)){
            return metadata.getControlOperations();
        }
        if (studyField.getName().equals(StudyFieldConstant.weighting)){
            return metadata.getWeighting();
        }
        if (studyField.getName().equals(StudyFieldConstant.cleaningOperations)){
            return metadata.getCleaningOperations();
        }
        if (studyField.getName().equals(StudyFieldConstant.studyLevelErrorNotes)){
            return metadata.getStudyLevelErrorNotes();
        }
        if (studyField.getName().equals(StudyFieldConstant.responseRate)){
            return metadata.getResponseRate();
        }
        if (studyField.getName().equals(StudyFieldConstant.samplingErrorEstimates)){
            return metadata.getSamplingErrorEstimate();
        }
        if (studyField.getName().equals(StudyFieldConstant.otherDataAppraisal)){
            return metadata.getOtherDataAppraisal();
        }
        if (studyField.getName().equals(StudyFieldConstant.placeOfAccess)){
            return metadata.getPlaceOfAccess();
        }
        if (studyField.getName().equals(StudyFieldConstant.originalArchive)){
            return metadata.getOriginalArchive();
        }
        if (studyField.getName().equals(StudyFieldConstant.availabilityStatus)){
            return metadata.getAvailabilityStatus();
        }
        if (studyField.getName().equals(StudyFieldConstant.collectionSize)){
            return metadata.getCollectionSize();
        }        
        if (studyField.getName().equals(StudyFieldConstant.studyCompletion)){
            return metadata.getStudyCompletion();
        }
        if (studyField.getName().equals(StudyFieldConstant.confidentialityDeclaration)){
            return metadata.getConfidentialityDeclaration();
        } 
        if (studyField.getName().equals(StudyFieldConstant.specialPermissions)){
            return metadata.getSpecialPermissions();
        }
        if (studyField.getName().equals(StudyFieldConstant.restrictions)){
            return metadata.getRestrictions();
        }
        if (studyField.getName().equals(StudyFieldConstant.contact)){
            return metadata.getContact();
        }
        if (studyField.getName().equals(StudyFieldConstant.citationRequirements)){
            return metadata.getCitationRequirements();
        }
        if (studyField.getName().equals(StudyFieldConstant.depositorRequirements)){
            return metadata.getDepositorRequirements();
        }
        if (studyField.getName().equals(StudyFieldConstant.conditions)){
            return metadata.getConditions();
        }
        if (studyField.getName().equals(StudyFieldConstant.disclaimer)){
            return metadata.getDisclaimer();
        }
        return "";
    }
    
    private String verifyControlledVocabValues(TemplateField tfIn){
        List <String> studyFieldValues = new ArrayList();
        StudyField studyField = new StudyField();
        if (tfIn.getStudyField().isCustomField()){
            for (StudyField sf : metadata.getStudyFields()) {
                if (sf.getName().equals(tfIn.getStudyField().getName())) {
                    studyField = sf;
                    break;
                }
            }
            for (String studyFieldValue: studyField.getStudyFieldValueStrings()){
                studyFieldValues.add(studyFieldValue);
            }
        } else {
            String metadataValue = getMetadataValueForControlledVocabularyValidation(tfIn.getStudyField());
            if (metadataValue != null && !metadataValue.isEmpty()){
                studyFieldValues.add(metadataValue);
            }            
        }

        String errorMessage = "";
        boolean inControlledVocab = false;
        for (String studyFieldValue: studyFieldValues){
            inControlledVocab = false;
            for (ControlledVocabularyValue controlledVocabValue : tfIn.getControlledVocabulary().getControlledVocabularyValues() ){
                if(studyFieldValue.equals(controlledVocabValue.getValue())){
                    inControlledVocab = true;
                }
            }
            if(!inControlledVocab){
                errorMessage += "<br/>" + tfIn.getStudyField().getName() +  " has had value " + studyFieldValue + " removed from its controlled vocabulary. ";
            }
        }          
        return errorMessage;
    }
        
    public void changeSingleValCVOld(ValueChangeEvent event) {
        
        int rowIndex = customFieldsPanelSeries.getRowIndex();
        DataModel fieldsDataModel = getCustomFieldsDataModel();
        fieldsDataModel.setRowIndex(rowIndex);

        Object[] fieldsRowData = (Object[]) fieldsDataModel.getRowData();
        TemplateField templateField = (TemplateField) fieldsRowData[0];  
        StudyField studyField = templateField.getStudyField();
        
        // first remove all the old values
        List valuesWrappedData = (List) ((ListDataModel) fieldsRowData[1]).getWrappedData();
        for (Object elem : valuesWrappedData) {
            Object[] valuesRowData = (Object[]) elem;
            List<StudyFieldValue> studyFieldValues = (List)valuesRowData[1];
            editStudyService.removeCollectionElement((List) valuesRowData[1], valuesRowData[0]);
        }
                               
        StudyFieldValue elem = new StudyFieldValue();
        elem.setStudyField(studyField);
        elem.setMetadata(metadata);
        elem.setStrValue((String) event.getNewValue());
        elem.setDisplayOrder(0);
        List values = new ArrayList();
        values.add(elem);
        studyField.setStudyFieldValues(values);           
    }
    
    public void changeMultiValCVOld(ValueChangeEvent event) {
        int rowIndex = customFieldsPanelSeries.getRowIndex();
        DataModel fieldsDataModel = getCustomFieldsDataModel();
        fieldsDataModel.setRowIndex(rowIndex);

        Object[] fieldsRowData = (Object[]) fieldsDataModel.getRowData();
        TemplateField templateField = (TemplateField) fieldsRowData[0];
        StudyField studyField = templateField.getStudyField();

        // first remove all the old values
        List valuesWrappedData = (List) ((ListDataModel) fieldsRowData[1]).getWrappedData();
        for (Object elem : valuesWrappedData) {
            Object[] valuesRowData = (Object[]) elem;
            List<StudyFieldValue> studyFieldValues = (List) valuesRowData[1];
            editStudyService.removeCollectionElement((List) valuesRowData[1], valuesRowData[0]);
        }

        List inStringList = (List) event.getNewValue();

        List values = new ArrayList();
        int counter = 0;
        for (Object inObj : inStringList) {
            String inStr = (String) inObj;
            StudyFieldValue elem = new StudyFieldValue();
            elem.setStudyField(studyField);
            elem.setMetadata(metadata);
            elem.setStrValue(inStr);
            elem.setDisplayOrder(counter++);
            values.add(elem);
        }
        studyField.setStudyFieldValues(values);

    }
    
    public void changeSingleValCV(ValueChangeEvent event) {
        Object[] fieldsRowData = getCustomFieldsRowData( customFieldsPanelSeries.getRowIndex() );  
        StudyField studyField = (StudyField) fieldsRowData[3];
        removeStudyFieldValues(studyField);  
        
        // now add new value
        StudyFieldValue sfv = new StudyFieldValue( studyField, metadata, (String)event.getNewValue() );
        studyField.getStudyFieldValues().add( sfv );
      
    }
    
    public void changeMultiValCV(ValueChangeEvent event) {       
        Object[] fieldsRowData = getCustomFieldsRowData( customFieldsPanelSeries.getRowIndex() );
        StudyField studyField = (StudyField) fieldsRowData[3];
        
        removeStudyFieldValues(studyField); 
        
        // now add new valuea
        List<String> newValues = (List) event.getNewValue();
        if (newValues != null && newValues.size() > 0) {
            for (String newVal : newValues){ 
                StudyFieldValue sfv = new StudyFieldValue(studyField, metadata, newVal);
                studyField.getStudyFieldValues().add( sfv );
            }
        } else {
            // add one blank row, in case user removed controlled vocabulary from template field
            StudyFieldValue sfv = new StudyFieldValue(studyField, metadata, null);
            studyField.getStudyFieldValues().add( sfv );            
        }
    }
    
    private void removeStudyFieldValues(StudyField studyField) {
        for (Iterator it = studyField.getStudyFieldValues().iterator(); it.hasNext();) {
            StudyFieldValue sfv = (StudyFieldValue) it.next();
            editStudyService.removeCollectionElement(it,sfv);

            /* if this value is already in db, we also need to remove it from the metadata's list of studyFieldValues
            if ( sfv.getId()!= null ) {
                metadata.getStudyFieldValues().remove( sfv );
            } */          
        }        
    }
    
    public Object[] getCustomFieldsRowData(int rowIndex) {
        // first set the rowindex of the data model to the row index of the panel series
        // TODO: (until we cache the DataModel, we have to do this)        
        DataModel fieldsDataModel = getCustomFieldsDataModel();
        fieldsDataModel.setRowIndex(rowIndex);
        return (Object[]) fieldsDataModel.getRowData();
    }
    
    public Map getTemplatesMap() {
        // getVdcTemplatesMap is called with currentVDC, since for a new study the current VDC IS the owner   
        return templateService.getVdcTemplatesMap(getVDCRequestBean().getCurrentVDC());
    }
    
    
    
    public boolean isTitleRequired() {
        TemplateField tf = (TemplateField)( editStudyService.getStudyMap().get("title"));
        return tf.isRequired();        
    }
    
    public void addCustomRow(ActionEvent ae) {
        HtmlDataTable dataTable = (HtmlDataTable) ae.getComponent().getParent().getParent();        
        Object[] data = (Object[]) ((ListDataModel) dataTable.getValue()).getRowData();
        StudyFieldValue newElem = new StudyFieldValue();
        newElem.setMetadata(metadata);
        newElem.setStudyField(((StudyFieldValue) data[0]).getStudyField());
        newElem.setStrValue("");
        ((List) data[1]).add(dataTable.getRowIndex() + 1, newElem);
    }

    public void removeCustomRow(ActionEvent ae) {
        HtmlDataTable dataTable = (HtmlDataTable) ae.getComponent().getParent().getParent();
        if (dataTable.getRowCount() > 1) {
            Object[] data = (Object[]) ((ListDataModel) dataTable.getValue()).getRowData();
            editStudyService.removeCollectionElement((List) data[1], data[0]);
        }
    }
    
    int count = 1;

    public void addRow(ActionEvent ae) {
        
        //      UIComponent dataTable = ae.getComponent().getParent().getParent().getParent();
        HtmlDataTable dataTable = (HtmlDataTable)ae.getComponent().getParent().getParent();
        
        if (dataTable.equals(dataTableOtherIds)) {
            StudyOtherId newElem = new StudyOtherId();
            newElem.setMetadata(metadata);
            metadata.getStudyOtherIds().add(dataTable.getRowIndex()+1,newElem);
        } else  if (dataTable.equals(dataTableAuthors)) {
            StudyAuthor newElem = new StudyAuthor();
            newElem.setMetadata(metadata);
            metadata.getStudyAuthors().add(dataTable.getRowIndex()+1,newElem);
            JavascriptContext.addJavascriptCall(getFacesContext(),"initAddAuthorSync();");
        } else  if (dataTable.equals(dataTableAbstracts)) {
            StudyAbstract newElem = new StudyAbstract();
            newElem.setMetadata(metadata);
            metadata.getStudyAbstracts().add(dataTable.getRowIndex()+1,newElem);
        } else  if (dataTable.equals(dataTableDistributors)) {
            StudyDistributor newElem = new StudyDistributor();
            newElem.setMetadata(metadata);
            metadata.getStudyDistributors().add(dataTable.getRowIndex()+1,newElem);
            JavascriptContext.addJavascriptCall(getFacesContext(),"initAddDistributorSync();");
        } else  if (dataTable.equals(dataTableGrants)) {
            StudyGrant newElem = new StudyGrant();
            newElem.setMetadata(metadata);
            metadata.getStudyGrants().add(dataTable.getRowIndex()+1,newElem);
        } else  if (dataTable.equals(dataTableKeywords)) {
            StudyKeyword newElem = new StudyKeyword();
            newElem.setMetadata(metadata);
            metadata.getStudyKeywords().add(dataTable.getRowIndex()+1,newElem);
        } else  if (dataTable.equals(dataTableNotes)) {
            StudyNote newElem = new StudyNote();
            newElem.setMetadata(metadata);
            metadata.getStudyNotes().add(dataTable.getRowIndex()+1,newElem);
        } else  if (dataTable.equals(dataTableProducers)) {
            StudyProducer newElem = new StudyProducer();
            newElem.setMetadata(metadata);
            metadata.getStudyProducers().add(dataTable.getRowIndex()+1,newElem);
        } else  if (dataTable.equals(dataTableSoftware)) {
            StudySoftware newElem = new StudySoftware();
            newElem.setMetadata(metadata);
            metadata.getStudySoftware().add(dataTable.getRowIndex()+1,newElem);
        } else  if (dataTable.equals(dataTableTopicClass)) {
            StudyTopicClass newElem = new StudyTopicClass();
            newElem.setMetadata(metadata);
            metadata.getStudyTopicClasses().add(dataTable.getRowIndex()+1,newElem);
        } else  if (dataTable.equals(this.dataTableGeoBoundings)) {
            StudyGeoBounding newElem = new StudyGeoBounding();
            newElem.setMetadata(metadata);
            metadata.getStudyGeoBoundings().add(dataTable.getRowIndex()+1,newElem);
        }  else  if (dataTable.equals(this.dataTableRelPublications)) {
            StudyRelPublication newElem = new StudyRelPublication();
            newElem.setMetadata(metadata);
            metadata.getStudyRelPublications().add(dataTable.getRowIndex()+1,newElem);
        } else  if (dataTable.equals(this.dataTableRelMaterials)) {
            StudyRelMaterial newElem = new StudyRelMaterial();
            newElem.setMetadata(metadata);
            metadata.getStudyRelMaterials().add(dataTable.getRowIndex()+1,newElem);
        } else  if (dataTable.equals(this.dataTableRelStudies)) {
            StudyRelStudy newElem = new StudyRelStudy();
            newElem.setMetadata(metadata);
            metadata.getStudyRelStudies().add(dataTable.getRowIndex()+1,newElem);
        } else  if (dataTable.equals(this.dataTableOtherReferences)) {
            StudyOtherRef newElem = new StudyOtherRef();
            newElem.setMetadata(metadata);
            metadata.getStudyOtherRefs().add(dataTable.getRowIndex()+1,newElem);
        }                          
    }
    
    public void removeRow(ActionEvent ae) {
        
        HtmlDataTable dataTable = (HtmlDataTable)ae.getComponent().getParent().getParent();
        if (dataTable.getRowCount()>1) { 
            List data = (List)dataTable.getValue();
            editStudyService.removeCollectionElement(data,dataTable.getRowIndex());
        }
        if (dataTable.equals(dataTableAuthors)) {
            JavascriptContext.addJavascriptCall(getFacesContext(),"initAddAuthorSync();");
        }
        if (dataTable.equals(dataTableDistributors)) {
            JavascriptContext.addJavascriptCall(getFacesContext(),"initAddDistributorSync();");
        }
    }
    
    public void toggleInlineHelp(ActionEvent ae) {
        String id =ae.getComponent().getId();
        String studyField = id.substring(5, id.length());
        UIComponent helpText = FacesContext.getCurrentInstance().getViewRoot().findComponent("help_text_"+studyField);
        helpText.setRendered(!helpText.isRendered());
    }
    /**
     * Holds value of property dataTableOtherIds.
     */
    private HtmlDataTable dataTableOtherIds;
    
    /**
     * Getter for property dataTableOtherIds.
     * @return Value of property dataTableOtherIds.
     */
    public HtmlDataTable getDataTableOtherIds() {
        return this.dataTableOtherIds;
    }
    
    /**
     * Setter for property dataTableOtherIds.
     * @param dataTableOtherIds New value of property dataTableOtherIds.
     */
    public void setDataTableOtherIds(HtmlDataTable dataTableOtherIds) {
        this.dataTableOtherIds = dataTableOtherIds;
    }

    private boolean isGroupEmpty(List mfgList) {
        if (mfgList==null || mfgList.size()==0) {
            return true;
        }
        for (Object obj : mfgList) {
            if (!((MetadataFieldGroup)obj).isEmpty())
                return false;
        }
        return true;
    }

    //
    //  The following methods are used in the Edit Study form display logic for metadata dependent collections
    //
    private HtmlSelectOneMenu inputDistributorContactName;

    public HtmlSelectOneMenu getInputDistributorContactName() {
        return inputDistributorContactName;
    }

    public void setInputDistributorContactName(HtmlSelectOneMenu inputDistributorContactName) {
        this.inputDistributorContactName = inputDistributorContactName;
    }
    
    private HtmlSelectOneMenu inputStudyProducerName;

    public HtmlSelectOneMenu getInputStudyProducerName() {
        return inputStudyProducerName;
    }

    public void setInputStudyProducerName(HtmlSelectOneMenu inputStudyProducerName) {
        this.inputStudyProducerName = inputStudyProducerName;
    }
    
    private HtmlSelectOneMenu inputStudyOtherId;

    public HtmlSelectOneMenu getInputStudyOtherId() {
        return inputStudyOtherId;
    }

    public void setInputStudyOtherId(HtmlSelectOneMenu inputStudyOtherId) {
        this.inputStudyOtherId = inputStudyOtherId;
    }

    // StudyAuthor
    private HtmlSelectOneMenu inputStudyAuthorName;

    public HtmlSelectOneMenu getInputStudyAuthorName() {
        return inputStudyAuthorName;
    }

    public void setInputStudyAuthorName(HtmlSelectOneMenu inputStudyAuthorName) {
        this.inputStudyAuthorName = inputStudyAuthorName;
    }
    
        // StudyAuthor
    private HtmlSelectOneMenu inputStudyDistributorName;

    public HtmlSelectOneMenu getInputStudyDistributorName() {
        return inputStudyDistributorName;
    }

    public void setInputStudyDistributorName(HtmlSelectOneMenu inputStudyDistributorName) {
        this.inputStudyDistributorName = inputStudyDistributorName;
    }
    
    public boolean isStudyAuthorsEmpty() {
        return isGroupEmpty(metadata.getStudyAuthors());
    }
    
    private HtmlSelectOneMenu inputStudySoftwareName;

    public HtmlSelectOneMenu getInputStudySoftwareName() {
        return inputStudySoftwareName;
    }

    public void setInputStudySoftwareName(HtmlSelectOneMenu inputStudySoftwareName) {
        this.inputStudySoftwareName = inputStudySoftwareName;
    }
    
    private HtmlSelectOneMenu inputStudyGrantNumber;

    public HtmlSelectOneMenu getInputStudyGrantNumber() {
        return inputStudyGrantNumber;
    }

    public void setInputStudyGrantNumber(HtmlSelectOneMenu inputStudyGrantNumber) {
        this.inputStudyGrantNumber = inputStudyGrantNumber;
    }
    
    private HtmlSelectOneMenu inputStudySeriesName;

    public HtmlSelectOneMenu getInputStudySeriesName() {
        return inputStudySeriesName;
    }

    public void setInputStudySeriesName(HtmlSelectOneMenu inputStudySeriesName) {
        this.inputStudySeriesName = inputStudySeriesName;
    }
    private HtmlSelectOneMenu inputStudyVersionDate;

    public HtmlSelectOneMenu getInputStudyVersionDate() {
        return inputStudyVersionDate;
    }

    public void setInputStudyVersionDate(HtmlSelectOneMenu inputStudyVersionDate) {
        this.inputStudyVersionDate = inputStudyVersionDate;
    }
    
    private HtmlSelectOneMenu inputStudyVersionName;

    public HtmlSelectOneMenu getInputStudyVersionName() {
        return inputStudyVersionName;
    }

    public void setInputStudyVersionName(HtmlSelectOneMenu inputStudyVersionName) {
        this.inputStudyVersionName = inputStudyVersionName;
    }
    
    private HtmlSelectOneMenu inputStudyKeywordValue;

    public HtmlSelectOneMenu getInputStudyKeywordValue() {
        return inputStudyKeywordValue;
    }

    public void setInputStudyKeywordValue(HtmlSelectOneMenu inputStudyKeywordValue) {
        this.inputStudyKeywordValue = inputStudyKeywordValue;
    }
    
    
    private HtmlSelectOneMenu inputStudyKeywordVocab;

    public HtmlSelectOneMenu getInputStudyKeywordVocab() {
        return inputStudyKeywordVocab;
    }

    public void setInputStudyKeywordVocab(HtmlSelectOneMenu inputStudyKeywordVocab) {
        this.inputStudyKeywordVocab = inputStudyKeywordVocab;
    }
    
        
    private HtmlSelectOneMenu inputStudyAbstractDate;

    public HtmlSelectOneMenu getInputStudyAbstractDate() {
        return inputStudyAbstractDate;
    }

    public void setInputStudyAbstractDate(HtmlSelectOneMenu inputStudyAbstractDate) {
        this.inputStudyAbstractDate = inputStudyAbstractDate;
    }
    
    
    private HtmlSelectOneMenu inputStudyNoteType;

    public HtmlSelectOneMenu getInputStudyNoteType() {
        return inputStudyNoteType;
    }

    public void setInputStudyNoteType(HtmlSelectOneMenu inputStudyNoteType) {
        this.inputStudyNoteType = inputStudyNoteType;
    }

    private HtmlSelectOneMenu inputStudyNoteSubject;

    public HtmlSelectOneMenu getInputStudyNoteSubject() {
        return inputStudyNoteSubject;
    }

    public void setInputStudyNoteSubject(HtmlSelectOneMenu inputStudyNoteSubject) {
        this.inputStudyNoteSubject = inputStudyNoteSubject;
    }
        private HtmlSelectOneMenu inputStudyNoteText;

    public HtmlSelectOneMenu getInputStudyNoteText() {
        return inputStudyNoteText;
    }

    public void setInputStudyNoteText(HtmlSelectOneMenu inputStudyNoteText) {
        this.inputStudyNoteText = inputStudyNoteText;
    }
    
    private HtmlSelectOneMenu inputStudyAbstractText;

    public HtmlSelectOneMenu getInputStudyAbstractText() {
        return inputStudyAbstractText;
    }

    public void setInputStudyAbstractText(HtmlSelectOneMenu inputStudyAbstractText) {
        this.inputStudyAbstractText = inputStudyAbstractText;
    }
    
    private HtmlSelectOneMenu inputStudyKeywordVocabURI;

    public HtmlSelectOneMenu getInputStudyKeywordVocabURI() {
        return inputStudyKeywordVocabURI;
    }

    public void setInputStudyKeywordVocabURI(HtmlSelectOneMenu inputStudyKeywordVocabURI) {
        this.inputStudyKeywordVocabURI = inputStudyKeywordVocabURI;
    }
    
    private HtmlSelectOneMenu inputStudyTopicClassValue;

    public HtmlSelectOneMenu getInputStudyTopicClassValue() {
        return inputStudyTopicClassValue;
    }

    public void setInputStudyTopicClassValue(HtmlSelectOneMenu inputStudyTopicClassValue) {
        this.inputStudyTopicClassValue = inputStudyTopicClassValue;
    }
    
    
    private HtmlSelectOneMenu inputStudyTopicClassVocab;

    public HtmlSelectOneMenu getInputStudyTopicClassVocab() {
        return inputStudyTopicClassVocab;
    }

    public void setInputStudyTopicClassVocab(HtmlSelectOneMenu inputStudyTopicClassVocab) {
        this.inputStudyTopicClassVocab = inputStudyTopicClassVocab;
    }
    
    private HtmlSelectOneMenu inputStudyTopicClassVocabURI;

    public HtmlSelectOneMenu getInputStudyTopicClassVocabURI() {
        return inputStudyTopicClassVocabURI;
    }

    public void setInputStudyTopicClassVocabURI(HtmlSelectOneMenu inputStudyTopicClassVocabURI) {
        this.inputStudyTopicClassVocabURI = inputStudyTopicClassVocabURI;
    }
    // StudyAbstract

    public boolean isStudyAbstractsEmpty() {
        return isGroupEmpty(metadata.getStudyAbstracts());
    }

    // StudyDistributor

    public boolean isStudyDistributorsEmpty() {
        return isGroupEmpty(metadata.getStudyDistributors());
    }

    // StudyGrant

    public boolean isStudyGrantsEmpty() {
        return isGroupEmpty(metadata.getStudyGrants());
    }

    // StudyGeobounding

    public boolean isStudyGeoBoundingsEmpty() {
        return isGroupEmpty(metadata.getStudyGeoBoundings());
    }

    // StudyKeyword

    public boolean isStudyKeywordsEmpty() {
        return isGroupEmpty(metadata.getStudyKeywords());
    }

    // StudyNote

    public boolean isStudyNotesEmpty() {
        return isGroupEmpty(metadata.getStudyNotes());
    }

    // StudyOtherId

    public boolean isStudyOtherIdsEmpty() {
        return isGroupEmpty(metadata.getStudyOtherIds());
    }

    // StudyProducer

    public boolean isStudyProducersEmpty() {
        return isGroupEmpty(metadata.getStudyProducers());
    }

    // StudySoftware

    public boolean isStudySoftwareEmpty() {
        return isGroupEmpty(metadata.getStudySoftware());
    }

    // StudyTopicClass

    public boolean isStudyTopicClassesEmpty() {
        return isGroupEmpty(metadata.getStudyTopicClasses());
    }

    // StudyRelMaterial

    public boolean isStudyRelMaterialsEmpty() {
        return isGroupEmpty(metadata.getStudyRelMaterials());
    }

    // StudyRelPublication

    public boolean isStudyRelPublicationsEmpty() {
        return isGroupEmpty(metadata.getStudyRelPublications());
    }

    // StudyRelStudy

    public boolean isStudyRelStudiesEmpty() {
        return isGroupEmpty(metadata.getStudyRelStudies());
    }

    // StudyOtherRef

    public boolean isStudyOtherRefsEmpty() {
        return isGroupEmpty(metadata.getStudyOtherRefs());
    }
    
    public boolean isCustomFieldEmpty(StudyField sf){
        return isGroupEmpty(sf.getStudyFieldValues());
    }     


    private void removeEmptyRows() {
        // Remove empty collection rows
        
        // StudyAuthor
        for (Iterator<StudyAuthor> it = metadata.getStudyAuthors().iterator(); it.hasNext();) {
            StudyAuthor elem =  it.next();
            if (elem.isEmpty()) {
                  editStudyService.removeCollectionElement(it,elem);
            }
        }
        
        // StudyAbstract
        for (Iterator<StudyAbstract> it = metadata.getStudyAbstracts().iterator(); it.hasNext();) {
            StudyAbstract elem =  it.next();
            if (elem.isEmpty()) {
                   editStudyService.removeCollectionElement(it,elem);
            }
        }
        
        // StudyDistributor
        for (Iterator<StudyDistributor> it = metadata.getStudyDistributors().iterator(); it.hasNext();) {
            StudyDistributor elem =  it.next();
            if (elem.isEmpty()) {
                   editStudyService.removeCollectionElement(it,elem);
           }
        }
        
        // StudyGrant
        for (Iterator<StudyGrant> it = metadata.getStudyGrants().iterator(); it.hasNext();) {
            StudyGrant elem =  it.next();
            if (elem.isEmpty()) {
                    editStudyService.removeCollectionElement(it,elem);
           }
        }
        // StudyGeobounding
        for (Iterator<StudyGeoBounding> it = metadata.getStudyGeoBoundings().iterator(); it.hasNext();) {
            StudyGeoBounding elem =  it.next();
            if (elem.isEmpty()) {
                  editStudyService.removeCollectionElement(it,elem);
            }
        }
        // StudyKeyword
        for (Iterator<StudyKeyword> it = metadata.getStudyKeywords().iterator(); it.hasNext();) {
            StudyKeyword elem =  it.next();
            if (elem.isEmpty()) {
                   editStudyService.removeCollectionElement(it,elem);
            }
        }
        // StudyNote
        for (Iterator<StudyNote> it = metadata.getStudyNotes().iterator(); it.hasNext();) {
            StudyNote elem =  it.next();
            if (elem.isEmpty()) {
                  editStudyService.removeCollectionElement(it,elem);
            }
        }
        // StudyOtherId
        for (Iterator<StudyOtherId> it = metadata.getStudyOtherIds().iterator(); it.hasNext();) {
            StudyOtherId elem =  it.next();
            if (elem.isEmpty()) {
                  editStudyService.removeCollectionElement(it,elem);
            }
        }
        // StudyProducer
        for (Iterator<StudyProducer> it = metadata.getStudyProducers().iterator(); it.hasNext();) {
            StudyProducer elem =  it.next();
            if ( elem.isEmpty()) {
                  editStudyService.removeCollectionElement(it,elem);
            }
        }
        
        // StudySoftware
        for (Iterator<StudySoftware> it = metadata.getStudySoftware().iterator(); it.hasNext();) {
            StudySoftware elem =  it.next();
            if (elem.isEmpty()) {
                   editStudyService.removeCollectionElement(it,elem);
           }
        }
        
        // StudyTopicClass
        for (Iterator<StudyTopicClass> it = metadata.getStudyTopicClasses().iterator(); it.hasNext();) {
            StudyTopicClass elem =  it.next();
            if (elem.isEmpty()) {
                   editStudyService.removeCollectionElement(it,elem);
           }
        }
        // StudyRelMaterial
        for (Iterator<StudyRelMaterial> it = metadata.getStudyRelMaterials().iterator(); it.hasNext();) {
            StudyRelMaterial elem =  it.next();
            if (elem.isEmpty()) {
                   editStudyService.removeCollectionElement(it,elem);
           }
        }
        // StudyRelPublication
        for (Iterator<StudyRelPublication> it = metadata.getStudyRelPublications().iterator(); it.hasNext();) {
            StudyRelPublication elem =  it.next();
            if (elem.isEmpty()) {
                   editStudyService.removeCollectionElement(it,elem);
           }
        }
        // StudyRelStudy
        for (Iterator<StudyRelStudy> it = metadata.getStudyRelStudies().iterator(); it.hasNext();) {
            StudyRelStudy elem =  it.next();
            if (elem.isEmpty()) {
                   editStudyService.removeCollectionElement(it,elem);
           }
        }
        // StudyOtherRef
        for (Iterator<StudyOtherRef> it = metadata.getStudyOtherRefs().iterator(); it.hasNext();) {
            StudyOtherRef elem =  it.next();
            if (elem.isEmpty()) {
                   editStudyService.removeCollectionElement(it,elem);
           }
        }

        // custom fields
        for (StudyField studyField : metadata.getStudyFields()) {
            for (Iterator<StudyFieldValue> it = studyField.getStudyFieldValues().iterator(); it.hasNext();) {
                StudyFieldValue elem =  it.next();
                if (elem.isEmpty()) {
                    editStudyService.removeCollectionElement(it,elem);
                }
            }
        }  
        
        
    }
    
    
    
    
    public String cancel() {
        editStudyService.cancel();

        if (study.getId()==null) {
            // Cancelling the creation of a new study
            return "/admin/OptionsPage?faces-redirect=true" + getContextSuffix();
        }

        Long versionNumber;

        if ( metadata.getStudyVersion().getId() == null  && study.getReleasedVersion() != null ) {
            // We are canceling the creation of a new version, so return to the previous version that the user was viewing.
            if (study.isReleased()) {
                versionNumber =  study.getReleasedVersion().getVersionNumber();
            } else {
                // The only other option is that the study is deaccessioned
                versionNumber = study.getDeaccessionedVersion().getVersionNumber();
            }
        } else {
            // We are cancelling the edit of an existing version, so just return to that version.
            versionNumber = metadata.getStudyVersion().getVersionNumber();
        }

        
        return "/study/StudyPage?faces-redirect=true&studyId=" + study.getId()+ "&versionNumber=" + versionNumber + getContextSuffix();

    }
    
    
    
    /**
     * Holds value of property editMode.
     */
    private String editMode;
    
    /**
     * Getter for property editMode.
     * @return Value of property editMode.
     */
    public String getEditMode() {
        return this.editMode;
    }
    
    /**
     * Setter for property editMode.
     * @param editMode New value of property editMode.
     */
    public void setEditMode(String editMode) {
        this.editMode = editMode;
    }
    
    /**
     * Holds value of property dataTableAuthors.
     */
    private HtmlDataTable dataTableAuthors;
    
    /**
     * Getter for property dataTableAuthors.
     * @return Value of property dataTableAuthors.
     */
    public HtmlDataTable getDataTableAuthors() {
        return this.dataTableAuthors;
    }
    
    /**
     * Setter for property dataTableAuthors.
     * @param dataTableAuthors New value of property dataTableAuthors.
     */
    public void setDataTableAuthors(HtmlDataTable dataTableAuthors) {
        this.dataTableAuthors = dataTableAuthors;
    }
    
    /**
     * Holds value of property dataTableAbstracts.
     */
    private HtmlDataTable dataTableAbstracts;
    
    /**
     * Getter for property dataTableAbstract.
     * @return Value of property dataTableAbstract.
     */
    public HtmlDataTable getDataTableAbstracts() {
        return this.dataTableAbstracts;
    }
    
    /**
     * Setter for property dataTableAbstract.
     * @param dataTableAbstract New value of property dataTableAbstract.
     */
    public void setDataTableAbstracts(HtmlDataTable dataTableAbstracts) {
        this.dataTableAbstracts = dataTableAbstracts;
    }
    
    /**
     * Holds value of property dataTableDistributors.
     */
    private HtmlDataTable dataTableDistributors;
    
    /**
     * Getter for property dataTableDistributor.
     * @return Value of property dataTableDistributor.
     */
    public HtmlDataTable getDataTableDistributors() {
        return this.dataTableDistributors;
    }
    
    /**
     * Setter for property dataTableDistributor.
     * @param dataTableDistributor New value of property dataTableDistributor.
     */
    public void setDataTableDistributors(HtmlDataTable dataTableDistributors) {
        this.dataTableDistributors = dataTableDistributors;
    }
    
    /**
     * Holds value of property dataTableGrants.
     */
    private HtmlDataTable dataTableGrants;
    
    /**
     * Getter for property dataTableGrant.
     * @return Value of property dataTableGrant.
     */
    public HtmlDataTable getDataTableGrants() {
        return this.dataTableGrants;
    }
    
    /**
     * Setter for property dataTableGrant.
     * @param dataTableGrant New value of property dataTableGrant.
     */
    public void setDataTableGrants(HtmlDataTable dataTableGrants) {
        this.dataTableGrants = dataTableGrants;
    }
    
    /**
     * Holds value of property dataTableKeywords.
     */
    private HtmlDataTable dataTableKeywords;
    
    /**
     * Getter for property dataTableKeyword.
     * @return Value of property dataTableKeyword.
     */
    public HtmlDataTable getDataTableKeywords() {
        return this.dataTableKeywords;
    }
    
    /**
     * Setter for property dataTableKeyword.
     * @param dataTableKeyword New value of property dataTableKeyword.
     */
    public void setDataTableKeywords(HtmlDataTable dataTableKeywords) {
        this.dataTableKeywords = dataTableKeywords;
    }
    
    /**
     * Holds value of property dataTableNotes.
     */
    private HtmlDataTable dataTableNotes;
    
    /**
     * Getter for property dataTableNotes.
     * @return Value of property dataTableNotes.
     */
    public HtmlDataTable getDataTableNotes() {
        return this.dataTableNotes;
    }
    
    /**
     * Setter for property dataTableNotes.
     * @param dataTableNotes New value of property dataTableNotes.
     */
    public void setDataTableNotes(HtmlDataTable dataTableNotes) {
        this.dataTableNotes = dataTableNotes;
    }
    
    /**
     * Holds value of property dataTableProducers.
     */
    private HtmlDataTable dataTableProducers;
    
    /**
     * Getter for property dataTableProducers.
     * @return Value of property dataTableProducers.
     */
    public HtmlDataTable getDataTableProducers() {
        return this.dataTableProducers;
    }
    
    /**
     * Setter for property dataTableProducers.
     * @param dataTableProducers New value of property dataTableProducers.
     */
    public void setDataTableProducers(HtmlDataTable dataTableProducers) {
        this.dataTableProducers = dataTableProducers;
    }
    
    /**
     * Holds value of property dataTableSoftware.
     */
    private HtmlDataTable dataTableSoftware;
    
    /**
     * Getter for property dataTableSoftware.
     * @return Value of property dataTableSoftware.
     */
    public HtmlDataTable getDataTableSoftware() {
        return this.dataTableSoftware;
    }
    
    /**
     * Setter for property dataTableSoftware.
     * @param dataTableSoftware New value of property dataTableSoftware.
     */
    public void setDataTableSoftware(HtmlDataTable dataTableSoftware) {
        this.dataTableSoftware = dataTableSoftware;
    }
    
    /**
     * Holds value of property dataTableTopicClass.
     */
    private HtmlDataTable dataTableTopicClass;
    
    /**
     * Getter for property dataTableTopicClass.
     * @return Value of property dataTableTopicClass.
     */
    public HtmlDataTable getDataTableTopicClass() {
        return this.dataTableTopicClass;
    }
    
    /**
     * Setter for property dataTableTopicClass.
     * @param dataTableTopicClass New value of property dataTableTopicClass.
     */
    public void setDataTableTopicClass(HtmlDataTable dataTableTopicClass) {
        this.dataTableTopicClass = dataTableTopicClass;
    }
    
 
    
   

    private List<TemplateField> getStudyMapTemplateFields(String... fieldNames) {
        List templateFieldList = new ArrayList();
        
        for (String fieldName : fieldNames) {
            if (((StudyMapValue) getStudyMap().get(fieldName))!=null) {
                StudyMapValue smv = (StudyMapValue) getStudyMap().get(fieldName);
                templateFieldList.add(smv.getTemplateField());
            }
        }        
        
        return templateFieldList;
    }
    
    private List<TemplateField> getCustomTemplateFields() {
        List customFieldList = new ArrayList();
        for (TemplateField tf : study.getTemplate().getTemplateFields()) {
            if (tf.getStudyField().isCustomField()) {
                customFieldList.add(tf);
            }
        }
        
        return customFieldList;
    }
    
    private String getInputLevel(List<TemplateField> tfList) {
        boolean hasRecommended = false;
        boolean hasOptional = false;
        
        for (TemplateField tf : tfList) {
            if (tf.isRequired()) {
                return "required";
            } else if (tf.isRecommended()) {
                hasRecommended = true;
            }  else if (tf.isOptional()) {
                hasOptional = true;
            }
        }
        
        if (hasRecommended) {
            return "recommended";
        }
        if (hasOptional) {
            return "optional";
        }
        
        return "hidden";        
    }      
    
     public String getAbstractAndScopeInputLevel() {
        // Commented out those study fields which are part of a compund field and cannot actually have their input levels set through the UI
        // nor do they determine if the field as a whole is shown
        List tfList = getStudyMapTemplateFields(
               StudyFieldConstant.description,
               StudyFieldConstant.keyword,
               StudyFieldConstant.topicClassification,
               StudyFieldConstant.relatedMaterial,
               StudyFieldConstant.relatedStudies,
               StudyFieldConstant.otherReferences,
               StudyFieldConstant.timePeriodCoveredEnd,
               StudyFieldConstant.timePeriodCoveredStart,
               StudyFieldConstant.dateOfCollectionEnd,
               StudyFieldConstant.dateOfCollectionStart,
               StudyFieldConstant.country,
               StudyFieldConstant.geographicCoverage,
               StudyFieldConstant.geographicUnit,            
               StudyFieldConstant.geographicBoundingBox,
               StudyFieldConstant.unitOfAnalysis,
               StudyFieldConstant.kindOfData,
               StudyFieldConstant.universe);
        
        return getInputLevel(tfList);
     }

     public boolean isAbstractAndScopeEmpty() {
         return this.isStudyAbstractsEmpty() &&
                 this.isStudyKeywordsEmpty() &&
                 this.isStudyTopicClassesEmpty() &&
                 this.isStudyRelMaterialsEmpty() &&
                 this.isStudyOtherRefsEmpty() &&
                 StringUtil.isEmpty(metadata.getTimePeriodCoveredStart()) &&
                 StringUtil.isEmpty(metadata.getTimePeriodCoveredEnd()) &&
                 StringUtil.isEmpty(metadata.getDateOfCollectionStart()) &&
                 StringUtil.isEmpty(metadata.getDateOfCollectionEnd()) &&
                 StringUtil.isEmpty(metadata.getCountry()) &&
                 StringUtil.isEmpty(metadata.getGeographicCoverage()) &&
                 StringUtil.isEmpty(metadata.getGeographicUnit()) &&
                 this.isStudyGeoBoundingsEmpty() &&
                 StringUtil.isEmpty(metadata.getUnitOfAnalysis()) &&
                 StringUtil.isEmpty(metadata.getKindOfData()) &&
                 StringUtil.isEmpty(metadata.getUniverse());
     }

    public String getDataCollectionMethodologyInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.timeMethod,
                StudyFieldConstant.dataCollector,
                StudyFieldConstant.frequencyOfDataCollection,
                StudyFieldConstant.samplingProcedure,
                StudyFieldConstant.deviationsFromSampleDesign,
                StudyFieldConstant.collectionMode,
                StudyFieldConstant.researchInstrument,
                StudyFieldConstant.dataSources,
                StudyFieldConstant.originOfSources,
                StudyFieldConstant.characteristicOfSources,
                StudyFieldConstant.accessToSources,
                StudyFieldConstant.dataCollectionSituation,
                StudyFieldConstant.actionsToMinimizeLoss,
                StudyFieldConstant.controlOperations,
                StudyFieldConstant.weighting,
                StudyFieldConstant.studyLevelErrorNotes,
                StudyFieldConstant.studyLevelErrorNotes,
                StudyFieldConstant.samplingErrorEstimates,
                StudyFieldConstant.otherDataAppraisal);
        
        // add all the custom fields
        tfList.addAll(getCustomTemplateFields());
        
        return getInputLevel(tfList);
    }
    


    public boolean isDataCollectionMethodologyEmpty() {
        return StringUtil.isEmpty(metadata.getTimeMethod()) &&
                StringUtil.isEmpty(metadata.getDataCollector()) &&
                StringUtil.isEmpty(metadata.getFrequencyOfDataCollection()) &&
                StringUtil.isEmpty(metadata.getSamplingProcedure()) &&
                StringUtil.isEmpty(metadata.getDeviationsFromSampleDesign()) &&
                StringUtil.isEmpty(metadata.getCollectionMode()) &&
                StringUtil.isEmpty(metadata.getResearchInstrument()) &&
                StringUtil.isEmpty(metadata.getDataSources()) &&
                StringUtil.isEmpty(metadata.getOriginOfSources()) &&
                StringUtil.isEmpty(metadata.getCharacteristicOfSources()) &&
                StringUtil.isEmpty(metadata.getAccessToSources()) &&
                StringUtil.isEmpty(metadata.getDataCollectionSituation()) &&
                StringUtil.isEmpty(metadata.getActionsToMinimizeLoss()) &&
                StringUtil.isEmpty(metadata.getControlOperations()) &&
                StringUtil.isEmpty(metadata.getWeighting()) &&
                StringUtil.isEmpty(metadata.getCleaningOperations()) &&
                StringUtil.isEmpty(metadata.getStudyLevelErrorNotes()) &&
                StringUtil.isEmpty(metadata.getResponseRate()) &&
                StringUtil.isEmpty(metadata.getSamplingErrorEstimate()) &&
                StringUtil.isEmpty(metadata.getOtherDataAppraisal())  && 
                isCustomFieldsEmpty();
    }
    
    private boolean isCustomFieldsEmpty(){
        for (StudyField sf: metadata.getStudyFields()){
            if(!isGroupEmpty(sf.getStudyFieldValues())) {
                return false;
            }
        }
        return true;
    }
    
      
    public String getTermsOfUseInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.disclaimer,
                StudyFieldConstant.conditions,
                StudyFieldConstant.depositorRequirements,
                StudyFieldConstant.citationRequirements,
                StudyFieldConstant.contact,
                StudyFieldConstant.restrictions,
                StudyFieldConstant.specialPermissions,
                StudyFieldConstant.confidentialityDeclaration);
        
        return getInputLevel(tfList);

    }

    public boolean isTermsOfUseEmpty() {
        return StringUtil.isEmpty(metadata.getDisclaimer()) &&
                StringUtil.isEmpty(metadata.getConditions()) &&
                StringUtil.isEmpty(metadata.getDepositorRequirements()) &&
                StringUtil.isEmpty(metadata.getCitationRequirements()) &&
                StringUtil.isEmpty(metadata.getContact()) &&
                StringUtil.isEmpty(metadata.getRestrictions()) &&
                StringUtil.isEmpty(metadata.getSpecialPermissions()) &&
                StringUtil.isEmpty(metadata.getConfidentialityDeclaration());
    }

    public String getDataSetAvailabilityInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.placeOfAccess,
                StudyFieldConstant.originalArchive,
                StudyFieldConstant.availabilityStatus,
                StudyFieldConstant.collectionSize,
                StudyFieldConstant.studyCompletion);
        
        return getInputLevel(tfList);

    }

    public boolean isDataSetAvailabilityEmpty() {
       return StringUtil.isEmpty(metadata.getPlaceOfAccess()) &&
                StringUtil.isEmpty(metadata.getOriginalArchive()) &&
                StringUtil.isEmpty(metadata.getAvailabilityStatus()) &&
                StringUtil.isEmpty(metadata.getCollectionSize()) &&
                StringUtil.isEmpty(metadata.getStudyCompletion());

    }
    public String getOtherInformationInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.note);
        
        return getInputLevel(tfList);

    }

    public String getOtherIdLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.otherId);
        
        return getInputLevel(tfList);
    }

    public String getAuthorInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.author);
        
        return getInputLevel(tfList);
    }

    public String getProducerInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.producer);
        
        return getInputLevel(tfList);
    }

    public String getSeriesInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.series);
        
        return getInputLevel(tfList);
    }
    
    public String getVersionInputLevel() {
        List tfList = getStudyMapTemplateFields(

                StudyFieldConstant.studyVersion);

        return getInputLevel(tfList);
    }

    public String getSoftwareInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.software);

        return getInputLevel(tfList);
    }

    public String getGrantInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.grantNumber);
        
        return getInputLevel(tfList);
    }

    public String getDistributorInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.distributor);

        return getInputLevel(tfList);
    }

    public String getContactInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.distributorContact);

        return getInputLevel(tfList);
    }

    public String getAbstractInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.description);

        return getInputLevel(tfList);
    }

    public String getKeywordInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.keyword);

        return getInputLevel(tfList);
    }

    public String getTopicInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.topicClassification);
        
        return getInputLevel(tfList);
    }

    public String getNoteInputLevel() {
        List tfList = getStudyMapTemplateFields(
                StudyFieldConstant.note);
        
        return getInputLevel(tfList);
    }


   
    
    private Long studyId;
    
    public Long getStudyId() {
        return studyId;
    }
    
    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

   
        
    private List files;
    
    public List getFiles() {
        return files;
    }
    
    public void setFiles(List files) {
        this.files = files;
    }

    public VersionNotesPopupBean getVersionNotesPopup() {
        return versionNotesPopup;
    }

    public void setVersionNotesPopup(VersionNotesPopupBean versionNotesPopup) {
        this.versionNotesPopup = versionNotesPopup;
    }

    public void openPopup(ActionEvent ae) {
        versionNotesPopup.setActionType(VersionNotesPopupBean.ActionType.EDIT_STUDY);
        versionNotesPopup.setVersionNote(metadata.getStudyVersion().getVersionNote());
        versionNotesPopup.openPopup(ae);
    }
    
    public String save() {
        metadata.getStudyVersion().setVersionNote(versionNotesPopup.getVersionNote());
           
        versionNotesPopup.setShowPopup(false);

        removeEmptyRows();
        
        // check to see if any of the publications are replication data
        for (StudyRelPublication publication : metadata.getStudyRelPublications()) {
            if (publication.isReplicationData()) {
                if (!metadata.getTitle().startsWith("Replication data for:")) {
                    metadata.setTitle("Replication data for: "+metadata.getTitle());
                }
                break;
            }
        }

        editStudyService.save(getVDCRequestBean().getCurrentVDCId(),getVDCSessionBean().getLoginBean().getUser().getId());
       
        return "/study/StudyPage?faces-redirect=true&studyId=" + study.getId()+ "&versionNumber=" + metadata.getStudyVersion().getVersionNumber() + getContextSuffix();

    }
    
    


    private List<SelectItem> fileCategoriesItems = null;
    
    
    public List getFileCategoryItems() {
        if (fileCategoriesItems == null) {
            fileCategoriesItems = new ArrayList();
            for (String catName : editStudyService.getStudyVersion().getFileCategories()) {
                fileCategoriesItems.add( new SelectItem(catName));
            }
        }

        return fileCategoriesItems;
    }
    
    
    
    public void validateLongitude(FacesContext context,
            UIComponent toValidate,
            Object value) {
        boolean valid=true;
        if (isValidateRequired()) {
            try {
                Double longitude = new Double(value.toString().trim());
                BigDecimal decimalLongitude = new BigDecimal(value.toString().trim());
                BigDecimal maxLongitude = new BigDecimal("180");
                BigDecimal minLongitude = new BigDecimal("-180");

                // To be valid longitude must be between 180 and -180
                if (decimalLongitude.compareTo(maxLongitude)==1 || decimalLongitude.compareTo(minLongitude)==-1) {
                    valid=false;
                }
            } catch (NumberFormatException e) {
                valid=false;
            }

            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Invalid Longitude.  Value must be between -180 and 180. (Unit is decimal degrees.)");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }        
    }
    
    public void validateLatitude(FacesContext context,
            UIComponent toValidate,
            Object value) {
        boolean valid=true;
        
        if (isValidateRequired()) {
            try {
                 BigDecimal decimalLatitude = new BigDecimal(value.toString().trim());
                 BigDecimal maxLatitude = new BigDecimal("90");
                 BigDecimal minLatitude = new BigDecimal("-90");

                // To be valid latitude must be between 90 and -90
                if (decimalLatitude.compareTo(maxLatitude)==1 || decimalLatitude.compareTo(minLatitude)==-1) {
                    valid=false;
                }
            } catch (NumberFormatException e) {
                valid=false;
            }
          
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Invalid Latitude.  Value must be between -90 and 90. (Unit is decimal degrees.)");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
    }
    
    public void validateStudyAuthor(FacesContext context,
            UIComponent toValidate,
            Object value) {
        if (isValidateRequired()) {
            boolean valid=true;
            // StudyAuthor get name from text or dropdown input
            String name = (String)inputAuthorName.getLocalValue(); //text
            if (name == null){
                name = (String) inputStudyAuthorName.getValue(); //dropdown
            }
            if (name == null){
                name = "";
            }
            name = name.trim();
            String affiliation = value.toString();
            affiliation = affiliation.trim();
            if (StringUtil.isEmpty(name) && !StringUtil.isEmpty(affiliation)) {
                valid=false;
            }
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Author name is required if Affiliation is entered.");
                context.addMessage(toValidate.getClientId(context), message);
                }
            }
        
    }
    
     public void validateStudyOtherId(FacesContext context,
            UIComponent toValidate,
            Object value) {
        
        if (isValidateRequired()) {
            boolean valid=true;
            // Study Other Id get name from text or dropdown input
            String name = (String)inputOtherId.getLocalValue(); //text
            if (name == null){
                name = (String) inputStudyOtherId.getValue(); //dropdown
            }
            if (name == null){
                name = "";
            }
            name = name.trim();
            if (StringUtil.isEmpty(name)
            && !StringUtil.isEmpty((String)value)  ) {
                valid=false;
            }
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Other ID  is required if Agency is entered.");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
    }
     
      public void validateSeries(FacesContext context,
            UIComponent toValidate, Object value) {
        
        if (isValidateRequired()) {
            boolean valid=true;
            String name = (String)inputSeries.getLocalValue(); //text
            if (name == null){
                name = (String) inputStudySeriesName.getValue(); //dropdown
            }
            if (name == null){
                name = "";
            }
            name = name.trim();
            if (StringUtil.isEmpty(name)
            && !StringUtil.isEmpty((String)value))   {
                valid=false;
            }
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Series is required if Series Information is entered.");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
    }
     
 public void validateVersion(FacesContext context,
            UIComponent toValidate, Object value) {
        
        if (isValidateRequired()){
            boolean valid=true;
            String name = (String)inputVersion.getLocalValue(); //text
            if (name == null){
                name = (String) inputStudyVersionName.getValue(); //dropdown
            }
            if (name == null){
                name = "";
            }
            name = name.trim();
            if (StringUtil.isEmpty(name)
            && !StringUtil.isEmpty((String)value)) {
                valid=false;
            }
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Version is required if Version Date is entered.");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
        
    }     
    public void validateStudyAbstract(FacesContext context,
            UIComponent toValidate,
            Object value) {
        
        if (isValidateRequired()) {
            boolean valid=true;
            String text = (String)inputAbstractText.getLocalValue(); //text
            if (text == null){
                text = (String) inputStudyAbstractText.getValue(); //dropdown
            }
            if (text == null){
                text = "";
            }
            if (StringUtil.isEmpty((String)text)
            && !StringUtil.isEmpty((String)value)  ) {
                valid=false;
            }
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Abstract text  is required if Date is entered.");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
        
    }   
         public void validateStudyNote(FacesContext context,
            UIComponent toValidate,
            Object value) {
        
        if (isValidateRequired()) {
            boolean valid=true;
            
            String type = (String)inputNoteType.getLocalValue(); //text
            if (type == null){
                type = (String) inputStudyNoteType.getValue(); //dropdown
            }
            if (type == null){
                type = "";
            }
            type = type.trim();
            String text = (String)inputNoteText.getLocalValue(); //text
            if (text == null){
                text = (String) inputStudyNoteText.getValue(); //dropdown
            }
            if (text == null){
                text = "";
            }
            text = text.trim();
            String subject = (String)inputNoteSubject.getLocalValue(); //text
            if (subject == null){
                subject = (String) inputStudyNoteSubject.getValue(); //dropdown
            }
            if (subject == null){
                subject = "";
            }
            subject = subject.trim();
            
            if (StringUtil.isEmpty(type)
            && (!StringUtil.isEmpty(text)
                || !StringUtil.isEmpty(subject)
                || !StringUtil.isEmpty((String)value))  ) {
                valid=false;
            }
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Note type is required if other note data is entered.");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
        
    }   
     
     public void validateStudySoftware(FacesContext context,
            UIComponent toValidate,
            Object value) {
        
        if (isValidateRequired()) {
            
            boolean valid=true;
            
                        // Study Other Id get name from text or dropdown input
            String name = (String)inputSoftwareName.getLocalValue(); //text
            if (name == null){
                name = (String) inputStudySoftwareName.getValue(); //dropdown
            }
            if (name == null){
                name = "";
            }
            name = name.trim();
            if (StringUtil.isEmpty(name)
            && !StringUtil.isEmpty((String)value)  ) {
                valid=false;
            }
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Software Name is required if Version is entered.");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
    }     
    
        public void validateStudyGrant(FacesContext context,
            UIComponent toValidate,
            Object value) {
        
        if (isValidateRequired()){
            boolean valid=true;
            String name = (String)inputGrantNumber.getLocalValue(); //text
            if (name == null){
                name = (String) inputStudyGrantNumber.getValue(); //dropdown
            }
            if (name == null){
                name = "";
            }
            name = name.trim();
            if (StringUtil.isEmpty(name)
            && !StringUtil.isEmpty((String)value)  ) {
                valid=false;
            }
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Grant Number is required if Grant Number Agency is entered.");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
    }       
     
     
    public void validateStudyDistributor(FacesContext context,
            UIComponent toValidate,
            Object value) {
        
        if (isValidateRequired()) {
            boolean valid=true;
            
                        // StudyDistributor get name from text or dropdown input
            String name = (String)inputDistributorName.getLocalValue(); //text
            if (name == null){
                name = (String) inputStudyDistributorName.getValue(); //dropdown
            }
            if (name == null){
                name = "";
            }
            name = name.trim();
            if (StringUtil.isEmpty(name)
            && (!StringUtil.isEmpty((String)inputDistributorAbbreviation.getLocalValue())
                 || !StringUtil.isEmpty((String)inputDistributorAffiliation.getLocalValue())
                 || !StringUtil.isEmpty((String)inputDistributorLogo.getLocalValue())
                 || !StringUtil.isEmpty((String)value) )) {
                valid=false;
            }
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Distributor name is required if other distributor data is entered.");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
        
    }
    
       public void validateDistributorContact(FacesContext context,
            UIComponent toValidate,
            Object value) {
        
        if (isValidateRequired()) {
                                    // StudyDistributor get name from text or dropdown input
            String name = (String)inputDistributorContact.getLocalValue(); //text
            if (name == null){
                name = (String) inputDistributorContactName.getValue(); //dropdown
            }
            if (name == null){
                name = "";
            }
            name = name.trim();
            boolean valid=true;
            if (StringUtil.isEmpty((String)inputDistributorContact.getLocalValue())
            && (!StringUtil.isEmpty((String)inputDistributorContactAffiliation.getLocalValue())
                 || !StringUtil.isEmpty((String)inputDistributorContactEmail.getLocalValue())
                 || !StringUtil.isEmpty((String)value) )) {
                valid=false;
            }
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Distributor contact name is required if distributor contact affiliation is entered.");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
        
    }
    
    public void validateStudyKeyword(FacesContext context,
            UIComponent toValidate,
            Object value) {
        
        if (isValidateRequired()) {
            boolean valid=true;
            String name = (String)inputKeywordValue.getLocalValue(); //text
            if (name == null){
                name = (String) inputStudyKeywordValue.getValue(); //dropdown
            }
            if (name == null){
                name = "";
            }
            name = name.trim();
            String vocab = (String)inputKeywordVocab.getLocalValue(); //text
            if (vocab == null){
                vocab = (String) inputStudyKeywordVocab.getValue(); //dropdown
            }
            if (vocab == null){
                vocab = "";
            }
            vocab = vocab.trim();
            String uri = (String)inputKeywordVocabUri.getLocalValue(); //text
            if (uri == null){
                uri = (String) inputStudyKeywordVocabURI.getValue(); //dropdown
            }
            if (uri == null){
                uri = "";
            }
            uri = uri.trim();
            if (StringUtil.isEmpty(name)
            && (!StringUtil.isEmpty(vocab)
                 || !StringUtil.isEmpty(uri) 
                    || !StringUtil.isEmpty((String)value) )) {
                valid=false;
            }
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Keyword value is required if other keyword data is entered.");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
        
    }
    
   public void validateStudyTopicClass(FacesContext context,
            UIComponent toValidate,
            Object value) {
        
        if( isValidateRequired()) {
            boolean valid=true;
            String name = (String)inputTopicClassValue.getLocalValue(); //text
            if (name == null){
                name = (String) inputStudyTopicClassValue.getValue(); //dropdown
            }
            if (name == null){
                name = "";
            }
            name = name.trim();
            String vocab = (String)inputTopicClassVocab.getLocalValue(); //text
            if (vocab == null){
                vocab = (String) inputStudyTopicClassVocab.getValue(); //dropdown
            }
            if (vocab == null){
                vocab = "";
            }
            vocab = vocab.trim();
            String uri = (String)inputTopicClassVocabUri.getLocalValue(); //text
            if (uri == null){
                uri = (String) inputStudyTopicClassVocabURI.getValue(); //dropdown
            }
            if (uri == null){
                uri = "";
            }
            uri = uri.trim();
            if (StringUtil.isEmpty(name)
            && (!StringUtil.isEmpty(vocab
                    )
                 || !StringUtil.isEmpty((String)value) )) {
                valid=false;
            }
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Topic Classification value is required if other topic classification data is entered.");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
        
    }
           
   public void validateGeographicBounding(FacesContext context,
            UIComponent toValidate,
            Object value) {
        if (isValidateRequired()) {
             boolean requiredValid = true;
             boolean countValid=true;
             
            int numLatitude = 0;
            int numLongitude = 0;

            if (!StringUtil.isEmpty((String)inputWestLongitude.getLocalValue())){numLongitude++;}
            if (!StringUtil.isEmpty((String)inputEastLongitude.getLocalValue())){numLongitude++;}
            if (!StringUtil.isEmpty((String)inputNorthLatitude.getLocalValue())){numLatitude++;}
            if (!StringUtil.isEmpty((String)inputSouthLatitude.getLocalValue())){numLatitude++;}

            if (numLatitude != numLongitude) {
                countValid = false;
            }
            
            if( ((StudyMapValue) getStudyMap().get(StudyFieldConstant.geographicBoundingBox)).isRequired()  && numLatitude==0 && numLongitude==0 ){
                 requiredValid = false;
             }

            if (!countValid) {
               inputSouthLatitude.setValid(false);
               FacesMessage message = new FacesMessage("Enter a single geographic point or a full bounding box.");
               context.addMessage(inputSouthLatitude.getClientId(context), message);
           }
            if (!requiredValid) {
               inputSouthLatitude.setValid(false);
               FacesMessage message = new FacesMessage("Geographic bounding is required.");
               context.addMessage(inputSouthLatitude.getClientId(context), message);
           }
        }
   }
   
    public void validateStudyProducer(FacesContext context,
            UIComponent toValidate,
            Object value) {
        if (isValidateRequired()) {
            boolean valid = true;
            String name = (String)inputProducerName.getLocalValue(); //text
            if (name == null){
                name = (String) inputStudyProducerName.getValue(); //dropdown
            }
            if (name == null){
                name = "";
            }
            name = name.trim();
        if (StringUtil.isEmpty(name)
        && (!StringUtil.isEmpty((String)inputProducerAbbreviation.getLocalValue())
             || !StringUtil.isEmpty((String)inputProducerAffiliation.getLocalValue())
             || !StringUtil.isEmpty((String)inputProducerLogo.getLocalValue())
             || !StringUtil.isEmpty((String)value) )) {
            valid=false;
            }
            if (!valid) {
                ((UIInput) toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Producer name is required if other producer data is entered.");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }

    }
    
    public void validateStudyPublication(FacesContext context,
            UIComponent toValidate,
            Object value) {
        
        if (isValidateRequired()) {
            boolean valid=true;
            String testString = (String)inputRelPublicationText.getLocalValue();
            if (testString == null) {
                testString = (String) inputRelPublicationCitation.getValue();
            }
            if (StringUtil.isEmpty(testString)
            && ( (value instanceof String && !StringUtil.isEmpty((String)value)) 
                    || (value instanceof Boolean && ((Boolean)value).booleanValue()) )){

                valid=false;
            }
            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                FacesMessage message = new FacesMessage("Publication citation is required if other publication data is entered.");
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
        
    }    
      
  
    public void validateStudyId(FacesContext context,
            UIComponent toValidate,
            Object value) {
        boolean valid=true;
                
        if (isValidateRequired()) {
            String studyId = (String)value;
            FacesMessage message=null;
            if (!studyService.isUniqueStudyId(studyId, study.getProtocol(),study.getAuthority())) {
                valid=false;
                message = new FacesMessage("Study ID is already used in this dataverse.");
            }
            if (valid) {
                if (!studyService.isValidStudyIdString(studyId)) {
                    valid = false;
                    message = new FacesMessage("Study ID can only contain characters a-z, A-Z, 0-9, dash or underscore (no spaces allowed)");
                }
            }

            if (!valid) {
                ((UIInput)toValidate).setValid(false);
                context.addMessage(toValidate.getClientId(context), message);
            }
        }
    }
    
    public void validateFileName(FacesContext context, UIComponent toValidate, Object value) {
        String fileName = (String) value;
        int rowIndex = getFilesDataTable().getRowIndex();
        String errorMessage = null;
        
        // add (or replace) name to list for validation of uniqueness
        if (validationFileNames.size() < rowIndex + 1) {
            validationFileNames.add(rowIndex, fileName);
        } else {
            validationFileNames.set(rowIndex, fileName);
        }

        // check invalid characters
        if (    fileName.contains("\\") ||
                fileName.contains("/") ||
                fileName.contains(":") ||
                fileName.contains("*") ||
                fileName.contains("?") ||
                fileName.contains("\"") ||
                fileName.contains("<") ||
                fileName.contains(">") ||
                fileName.contains("|") ||
                fileName.contains(";") ||
                fileName.contains("#")) {
            errorMessage = "cannot contain any of the following characters: \\ / : * ? \" < > | ; #";

        } else if (validationFileNames.subList(0, rowIndex).contains(fileName)) { // check versus current list
            errorMessage = errorMessage = "must be unique.";
        }
        
        
        
        if (errorMessage != null) {
            ((UIInput)toValidate).setValid(false);
            
            FacesMessage message = new FacesMessage("Invalid File Name - " + errorMessage);
            context.addMessage(toValidate.getClientId(context), message);
        }
        
    }

    public String getShowAllString() {
        return showAllString;
    }

    public void setShowAllString(String showAllString) {
        this.showAllString = showAllString;
    }
    private String showAllString;

    private boolean showAll;

    public boolean isShowAll() {
        return showAll;
    }

    public void setShowAll(boolean showAll) {
            this.showAll = showAll;
         }

    
    private Long selectTemplateId;

    public Long getSelectTemplateId() {
        return selectTemplateId;
    }

    public void setSelectTemplateId(Long selectTemplateId) {
        this.selectTemplateId = selectTemplateId;
    }
    
    
    HtmlSelectOneMenu selectTemplate;

    public HtmlSelectOneMenu getSelectTemplate() {
        return selectTemplate;
    }

    public void setSelectTemplate(HtmlSelectOneMenu selectTemplate) {
        this.selectTemplate = selectTemplate;
    }
    
    
    /**
     * Holds value of property dataTableGeoBoundings.
     */
    private HtmlDataTable dataTableGeoBoundings;
    
    /**
     * Getter for property dataTableGeoBoundings.
     * @return Value of property dataTableGeoBoundings.
     */
    public HtmlDataTable getDataTableGeoBoundings() {
        return this.dataTableGeoBoundings;
    }
    
    /**
     * Setter for property dataTableGeoBoundings.
     * @param dataTableGeoBoundings New value of property dataTableGeoBoundings.
     */
    public void setDataTableGeoBoundings(HtmlDataTable dataTableGeoBoundings) {
        this.dataTableGeoBoundings = dataTableGeoBoundings;
    }
    
    private List validationFileNames = new ArrayList();
    
    public List getValidationFileNames() {
        return validationFileNames;
    }
    
    /**
     * Holds value of property dataTableRelMaterials.
     */
    private HtmlDataTable dataTableRelMaterials;
    
    /**
     * Getter for property dataTableRelMaterials.
     * @return Value of property dataTableRelMaterials.
     */
    public HtmlDataTable getDataTableRelMaterials() {
        return this.dataTableRelMaterials;
    }
    
    /**
     * Setter for property dataTableRelMaterials.
     * @param dataTableRelMaterials New value of property dataTableRelMaterials.
     */
    public void setDataTableRelMaterials(HtmlDataTable dataTableRelMaterials) {
        this.dataTableRelMaterials = dataTableRelMaterials;
    }
    
    /**
     * Holds value of property dataTableRelStudies.
     */
    private HtmlDataTable dataTableRelStudies;
    
    /**
     * Getter for property dataTableRelStudies.
     * @return Value of property dataTableRelStudies.
     */
    public HtmlDataTable getDataTableRelStudies() {
        return this.dataTableRelStudies;
    }
    
    /**
     * Setter for property dataTableRelStudies.
     * @param dataTableRelStudies New value of property dataTableRelStudies.
     */
    public void setDataTableRelStudies(HtmlDataTable dataTableRelStudies) {
        this.dataTableRelStudies = dataTableRelStudies;
    }
    
    /**
     * Holds value of property dataTableRelPublications.
     */
    private HtmlDataTable dataTableRelPublications;
    
    /**
     * Getter for property dataTableRelPublications.
     * @return Value of property dataTableRelPublications.
     */
    public HtmlDataTable getDataTableRelPublications() {
        return this.dataTableRelPublications;
    }
    
    /**
     * Setter for property dataTableRelPublications.
     * @param dataTableRelPublications New value of property dataTableRelPublications.
     */
    public void setDataTableRelPublications(HtmlDataTable dataTableRelPublications) {
        this.dataTableRelPublications = dataTableRelPublications;
    }
    
    /**
     * Holds value of property dataTableOtherReferences.
     */
    private HtmlDataTable dataTableOtherReferences;
    
    /**
     * Getter for property dataTableOtherReferences.
     * @return Value of property dataTableOtherReferences.
     */
    public HtmlDataTable getDataTableOtherReferences() {
        return this.dataTableOtherReferences;
    }
    
    /**
     * Setter for property dataTableOtherReferences.
     * @param dataTableOtherReferences New value of property dataTableOtherReferences.
     */
    public void setDataTableOtherReferences(HtmlDataTable dataTableOtherReferences) {
        this.dataTableOtherReferences = dataTableOtherReferences;
    }

    /**
     * Holds value of property inputAuthorName.
     */
    private HtmlInputText inputAuthorName;

    /**
     * Getter for property studyAuthorName.
     * @return Value of property studyAuthorName.
     */
    public HtmlInputText getInputAuthorName() {
        return this.inputAuthorName;
    }

    /**
     * Setter for property studyAuthorName.
     * @param studyAuthorName New value of property studyAuthorName.
     */
    public void setInputAuthorName(HtmlInputText inputAuthorName) {
        this.inputAuthorName = inputAuthorName;
    }

    private HtmlInputText inputTitle;

    public HtmlInputText getInputTitle() {
        return inputTitle;
    }

    public void setInputTitle(HtmlInputText inputTitle) {
        this.inputTitle = inputTitle;
    }
    
    
    /**
     * Holds value of property inputAuthorAffiliation.
     */
    private HtmlInputText inputAuthorAffiliation;

    /**
     * Getter for property studyAuthorAffiliation.
     * @return Value of property studyAuthorAffiliation.
     */
    public HtmlInputText getInputAuthorAffiliation() {
        return this.inputAuthorAffiliation;
    }

    /**
     * Setter for property studyAuthorAffiliation.
     * @param studyAuthorAffiliation New value of property studyAuthorAffiliation.
     */
    public void setInputAuthorAffiliation(HtmlInputText inputAuthorAffiliation) {
        this.inputAuthorAffiliation = inputAuthorAffiliation;
    }

    /**
     * Holds value of property sessionCounter.
     */
    private int sessionCounter;

    /**
     * Getter for property sessionCounter.
     * @return Value of property sessionCounter.
     */
    public int getSessionCounter() {
        return this.sessionCounter;
    }

    /**
     * Setter for property sessionCounter.
     * @param sessionCounter New value of property sessionCounter.
     */
    public void setSessionCounter(int sessionCounter) {
        this.sessionCounter = sessionCounter;
    }

    /**
     * Holds value of property inputProducerName.
     */
    private HtmlInputText inputProducerName;

    /**
     * Getter for property inputStudyProducerName.
     * @return Value of property inputStudyProducerName.
     */
    public HtmlInputText getInputProducerName() {
        return this.inputProducerName;
    }

    /**
     * Setter for property inputStudyProducerName.
     * @param inputStudyProducerName New value of property inputStudyProducerName.
     */
    public void setInputProducerName(HtmlInputText inputProducerName) {
        this.inputProducerName = inputProducerName;
    }

    /**
     * Holds value of property inputProducerAffiliation.
     */
    private HtmlInputText inputProducerAffiliation;

    /**
     * Getter for property inputProducerAffiliation.
     * @return Value of property inputProducerAffiliation.
     */
    public HtmlInputText getInputProducerAffiliation() {
        return this.inputProducerAffiliation;
    }

    /**
     * Setter for property inputProducerAffiliation.
     * @param inputProducerAffiliation New value of property inputProducerAffiliation.
     */
    public void setInputProducerAffiliation(HtmlInputText inputProducerAffiliation) {
        this.inputProducerAffiliation = inputProducerAffiliation;
    }

    /**
     * Holds value of property inputProducerAbbreviation.
     */
    private HtmlInputText inputProducerAbbreviation;

    /**
     * Getter for property inputProducerAbbreviation.
     * @return Value of property inputProducerAbbreviation.
     */
    public HtmlInputText getInputProducerAbbreviation() {
        return this.inputProducerAbbreviation;
    }

    /**
     * Setter for property inputProducerAbbreviation.
     * @param inputProducerAbbreviation New value of property inputProducerAbbreviation.
     */
    public void setInputProducerAbbreviation(HtmlInputText inputProducerAbbreviation) {
        this.inputProducerAbbreviation = inputProducerAbbreviation;
    }

    /**
     * Holds value of property inputProducerUrl.
     */
    private HtmlInputText inputProducerUrl;

    /**
     * Getter for property inputProducerUrl.
     * @return Value of property inputProducerUrl.
     */
    public HtmlInputText getInputProducerUrl() {
        return this.inputProducerUrl;
    }

    /**
     * Setter for property inputProducerUrl.
     * @param inputProducerUrl New value of property inputProducerUrl.
     */
    public void setInputProducerUrl(HtmlInputText inputProducerUrl) {
        this.inputProducerUrl = inputProducerUrl;
    }

    /**
     * Holds value of property inputProducerLogo.
     */
    private HtmlInputText inputProducerLogo;

    /**
     * Getter for property inputProducerLogo.
     * @return Value of property inputProducerLogo.
     */
    public HtmlInputText getInputProducerLogo() {
        return this.inputProducerLogo;
    }

    /**
     * Setter for property inputProducerLogo.
     * @param inputProducerLogo New value of property inputProducerLogo.
     */
    public void setInputProducerLogo(HtmlInputText inputProducerLogo) {
        this.inputProducerLogo = inputProducerLogo;
    }

    /**
     * Holds value of property inputSoftwareVersion.
     */
    private HtmlInputText inputSoftwareVersion;

    /**
     * Getter for property inputSoftwareVersion.
     * @return Value of property inputSoftwareVersion.
     */
    public HtmlInputText getInputSoftwareVersion() {
        return this.inputSoftwareVersion;
    }

    /**
     * Setter for property inputSoftwareVersion.
     * @param inputSoftwareVersion New value of property inputSoftwareVersion.
     */
    public void setInputSoftwareVersion(HtmlInputText inputSoftwareVersion) {
        this.inputSoftwareVersion = inputSoftwareVersion;
    }

    /**
     * Holds value of property inputSoftwareName.
     */
    private HtmlInputText inputSoftwareName;

    /**
     * Getter for property inputSoftwareName.
     * @return Value of property inputSoftwareName.
     */
    public HtmlInputText getInputSoftwareName() {
        return this.inputSoftwareName;
    }

    /**
     * Setter for property inputSoftwareName.
     * @param inputSoftwareName New value of property inputSoftwareName.
     */
    public void setInputSoftwareName(HtmlInputText inputSoftwareName) {
        this.inputSoftwareName = inputSoftwareName;
    }

    /**
     * Holds value of property inputGrantNumber.
     */
    private HtmlInputText inputGrantNumber;

    /**
     * Getter for property inputGrantNumber.
     * @return Value of property inputGrantNumber.
     */
    public HtmlInputText getInputGrantNumber() {
        return this.inputGrantNumber;
    }

    /**
     * Setter for property inputGrantNumber.
     * @param inputGrantNumber New value of property inputGrantNumber.
     */
    public void setInputGrantNumber(HtmlInputText inputGrantNumber) {
        this.inputGrantNumber = inputGrantNumber;
    }

    /**
     * Holds value of property inputDistributorName.
     */
    private HtmlInputText inputDistributorName;

    /**
     * Getter for property inputDistributorName.
     * @return Value of property inputDistributorName.
     */
    public HtmlInputText getInputDistributorName() {
        return this.inputDistributorName;
    }

    /**
     * Setter for property inputDistributorName.
     * @param inputDistributorName New value of property inputDistributorName.
     */
    public void setInputDistributorName(HtmlInputText inputDistributorName) {
        this.inputDistributorName = inputDistributorName;
    }

    /**
     * Holds value of property inputDistributorAffiliation.
     */
    private HtmlInputText inputDistributorAffiliation;

    /**
     * Getter for property inputDistributorAffiliation.
     * @return Value of property inputDistributorAffiliation.
     */
    public HtmlInputText getInputDistributorAffiliation() {
        return this.inputDistributorAffiliation;
    }

    /**
     * Setter for property inputDistributorAffiliation.
     * @param inputDistributorAffiliation New value of property inputDistributorAffiliation.
     */
    public void setInputDistributorAffiliation(HtmlInputText inputDistributorAffiliation) {
        this.inputDistributorAffiliation = inputDistributorAffiliation;
    }

    /**
     * Holds value of property inputDistributorAbbreviation.
     */
    private HtmlInputText inputDistributorAbbreviation;

    /**
     * Getter for property inputDistributorAbbreviation.
     * @return Value of property inputDistributorAbbreviation.
     */
    public HtmlInputText getInputDistributorAbbreviation() {
        return this.inputDistributorAbbreviation;
    }

    /**
     * Setter for property inputDistributorAbbreviation.
     * @param inputDistributorAbbreviation New value of property inputDistributorAbbreviation.
     */
    public void setInputDistributorAbbreviation(HtmlInputText inputDistributorAbbreviation) {
        this.inputDistributorAbbreviation = inputDistributorAbbreviation;
    }

    /**
     * Holds value of property inputDistributorUrl.
     */
    private HtmlInputText inputDistributorUrl;

    /**
     * Getter for property inputDistributorUrl.
     * @return Value of property inputDistributorUrl.
     */
    public HtmlInputText getInputDistributorUrl() {
        return this.inputDistributorUrl;
    }

    /**
     * Setter for property inputDistributorUrl.
     * @param inputDistributorUrl New value of property inputDistributorUrl.
     */
    public void setInputDistributorUrl(HtmlInputText inputDistributorUrl) {
        this.inputDistributorUrl = inputDistributorUrl;
    }

    /**
     * Holds value of property inputDistributorLogo.
     */
    private HtmlInputText inputDistributorLogo;

    /**
     * Getter for property inputDistributorLogo.
     * @return Value of property inputDistributorLogo.
     */
    public HtmlInputText getInputDistributorLogo() {
        return this.inputDistributorLogo;
    }

    /**
     * Setter for property inputDistributorLogo.
     * @param inputDistributorLogo New value of property inputDistributorLogo.
     */
    public void setInputDistributorLogo(HtmlInputText inputDistributorLogo) {
        this.inputDistributorLogo = inputDistributorLogo;
    }

    /**
     * Holds value of property inputRelPublicationName.
     */
    private HtmlInputTextarea inputRelPublicationText;
    private HtmlSelectOneMenu inputRelPublicationCitation;
    private HtmlSelectBooleanCheckbox inputRelPublicationReplicationData;
    private HtmlSelectOneMenu inputRelPublicationIDType;
    private HtmlInputText inputRelPublicationIDNumber;
    private HtmlInputText inputRelPublicationURL;

    
    public HtmlSelectOneMenu getInputRelPublicationCitation() {
        return inputRelPublicationCitation;
    }

    public void setInputRelPublicationCitation(HtmlSelectOneMenu inputRelPublicationCitation) {
        this.inputRelPublicationCitation = inputRelPublicationCitation;
    }
    
    public HtmlInputText getInputRelPublicationIDNumber() {
        return inputRelPublicationIDNumber;
    }

    public void setInputRelPublicationIDNumber(HtmlInputText inputRelPublicationIDNumber) {
        this.inputRelPublicationIDNumber = inputRelPublicationIDNumber;
    }

    public HtmlSelectOneMenu getInputRelPublicationIDType() {
        return inputRelPublicationIDType;
    }

    public void setInputRelPublicationIDType(HtmlSelectOneMenu inputRelPublicationIDType) {
        this.inputRelPublicationIDType = inputRelPublicationIDType;
    }

    public HtmlSelectBooleanCheckbox getInputRelPublicationReplicationData() {
        return inputRelPublicationReplicationData;
    }

    public void setInputRelPublicationReplicationData(HtmlSelectBooleanCheckbox inputRelPublicationReplicationData) {
        this.inputRelPublicationReplicationData = inputRelPublicationReplicationData;
    }

    public HtmlInputTextarea getInputRelPublicationText() {
        return inputRelPublicationText;
    }

    public void setInputRelPublicationText(HtmlInputTextarea inputRelPublicationText) {
        this.inputRelPublicationText = inputRelPublicationText;
    }

    public HtmlInputText getInputRelPublicationURL() {
        return inputRelPublicationURL;
    }

    public void setInputRelPublicationURL(HtmlInputText inputRelPublicationURL) {
        this.inputRelPublicationURL = inputRelPublicationURL;
    }

    /**
     * Holds value of property inputRelMaterial.
     */
    private HtmlInputTextarea inputRelMaterial;

    /**
     * Getter for property inputRelMaterial.
     * @return Value of property inputRelMaterial.
     */
    public HtmlInputTextarea getInputRelMaterial() {
        return this.inputRelMaterial;
    }

    /**
     * Setter for property inputRelMaterial.
     * @param inputRelMaterial New value of property inputRelMaterial.
     */
    public void setInputRelMaterial(HtmlInputTextarea inputRelMaterial) {
        this.inputRelMaterial = inputRelMaterial;
    }

    /**
     * Holds value of property inputRelStudy.
     */
    private HtmlInputTextarea inputRelStudy;

    /**
     * Getter for property inputRelStudy.
     * @return Value of property inputRelStudy.
     */
    public HtmlInputTextarea getInputRelStudy() {
        return this.inputRelStudy;
    }

    /**
     * Setter for property inputRelStudy.
     * @param inputRelStudy New value of property inputRelStudy.
     */
    public void setInputRelStudy(HtmlInputTextarea inputRelStudy) {
        this.inputRelStudy = inputRelStudy;
    }

    /**
     * Holds value of property inputOtherReference.
     */
    private HtmlInputText inputOtherReference;

    /**
     * Getter for property inputOtherReference.
     * @return Value of property inputOtherReference.
     */
    public HtmlInputText getInputOtherReference() {
        return this.inputOtherReference;
    }

    /**
     * Setter for property inputOtherReference.
     * @param inputOtherReference New value of property inputOtherReference.
     */
    public void setInputOtherReference(HtmlInputText inputOtherReference) {
        this.inputOtherReference = inputOtherReference;
    }

    /**
     * Holds value of property inputKeywordValue.
     */
    private HtmlInputText inputKeywordValue;

    /**
     * Getter for property inputKeywordValue.
     * @return Value of property inputKeywordValue.
     */
    public HtmlInputText getInputKeywordValue() {
        return this.inputKeywordValue;
    }

    /**
     * Setter for property inputKeywordValue.
     * @param inputKeywordValue New value of property inputKeywordValue.
     */
    public void setInputKeywordValue(HtmlInputText inputKeywordValue) {
        this.inputKeywordValue = inputKeywordValue;
    }

    /**
     * Holds value of property inputKeywordVocab.
     */
    private HtmlInputText inputKeywordVocab;

    /**
     * Getter for property inputKeywordVocab.
     * @return Value of property inputKeywordVocab.
     */
    public HtmlInputText getInputKeywordVocab() {
        return this.inputKeywordVocab;
    }

    /**
     * Setter for property inputKeywordVocab.
     * @param inputKeywordVocab New value of property inputKeywordVocab.
     */
    public void setInputKeywordVocab(HtmlInputText inputKeywordVocab) {
        this.inputKeywordVocab = inputKeywordVocab;
    }

    /**
     * Holds value of property inputKeywordVocabUri.
     */
    private HtmlInputText inputKeywordVocabUri;

    /**
     * Getter for property inputKeywordVocabUri.
     * @return Value of property inputKeywordVocabUri.
     */
    public HtmlInputText getInputKeywordVocabUri() {
        return this.inputKeywordVocabUri;
    }

    /**
     * Setter for property inputKeywordVocabUri.
     * @param inputKeywordVocabUri New value of property inputKeywordVocabUri.
     */
    public void setInputKeywordVocabUri(HtmlInputText inputKeywordVocabUri) {
        this.inputKeywordVocabUri = inputKeywordVocabUri;
    }

    /**
     * Holds value of property inputTopicClassValue.
     */
    private HtmlInputText inputTopicClassValue;

    /**
     * Getter for property inputTopicClassValue.
     * @return Value of property inputTopicClassValue.
     */
    public HtmlInputText getInputTopicClassValue() {
        return this.inputTopicClassValue;
    }

    /**
     * Setter for property inputTopicClassValue.
     * @param inputTopicClassValue New value of property inputTopicClassValue.
     */
    public void setInputTopicClassValue(HtmlInputText inputTopicClassValue) {
        this.inputTopicClassValue = inputTopicClassValue;
    }

    /**
     * Holds value of property inputTopicClassVocab.
     */
    private HtmlInputText inputTopicClassVocab;

    /**
     * Getter for property inputTopicClassVocab.
     * @return Value of property inputTopicClassVocab.
     */
    public HtmlInputText getInputTopicClassVocab() {
        return this.inputTopicClassVocab;
    }

    /**
     * Setter for property inputTopicClassVocab.
     * @param inputTopicClassVocab New value of property inputTopicClassVocab.
     */
    public void setInputTopicClassVocab(HtmlInputText inputTopicClassVocab) {
        this.inputTopicClassVocab = inputTopicClassVocab;
    }

    /**
     * Holds value of property inputTopicClassVocabUri.
     */
    private HtmlInputText inputTopicClassVocabUri;

    /**
     * Getter for property inputTopicVocabUri.
     * @return Value of property inputTopicVocabUri.
     */
    public HtmlInputText getInputTopicClassVocabUri() {
        return this.inputTopicClassVocabUri;
    }

    /**
     * Setter for property inputTopicVocabUri.
     * @param inputTopicVocabUri New value of property inputTopicVocabUri.
     */
    public void setInputTopicClassVocabUri(HtmlInputText inputTopicClassVocabUri) {
        this.inputTopicClassVocabUri = inputTopicClassVocabUri;
    }

    /**
     * Holds value of property inputAbstractText.
     */
    private javax.faces.component.html.HtmlInputTextarea inputAbstractText;

    /**
     * Getter for property inputAbstractText.
     * @return Value of property inputAbstractText.
     */
    public javax.faces.component.html.HtmlInputTextarea getInputAbstractText() {
        return this.inputAbstractText;
    }

    /**
     * Setter for property inputAbstractText.
     * @param inputAbstractText New value of property inputAbstractText.
     */
    public void setInputAbstractText(javax.faces.component.html.HtmlInputTextarea inputAbstractText) {
        this.inputAbstractText = inputAbstractText;
    }

    /**
     * Holds value of property inputAbstractDate.
     */
    private HtmlInputText inputAbstractDate;

    /**
     * Getter for property inputAbstractDate.
     * @return Value of property inputAbstractDate.
     */
    public HtmlInputText getInputAbstractDate() {
        return this.inputAbstractDate;
    }

    /**
     * Setter for property inputAbstractDate.
     * @param inputAbstractDate New value of property inputAbstractDate.
     */
    public void setInputAbstractDate(HtmlInputText inputAbstractDate) {
        this.inputAbstractDate = inputAbstractDate;
    }

    /**
     * Holds value of property inputNoteText.
     */
    private HtmlInputText inputNoteText;

    /**
     * Getter for property inputNoteText.
     * @return Value of property inputNoteText.
     */
    public HtmlInputText getInputNoteText() {
        return this.inputNoteText;
    }

    /**
     * Setter for property inputNoteText.
     * @param inputNoteText New value of property inputNoteText.
     */
    public void setInputNoteText(HtmlInputText inputNoteText) {
        this.inputNoteText = inputNoteText;
    }

    /**
     * Holds value of property inputNoteSubject.
     */
    private HtmlInputText inputNoteSubject;

    /**
     * Getter for property inputNoteSubject.
     * @return Value of property inputNoteSubject.
     */
    public HtmlInputText getInputNoteSubject() {
        return this.inputNoteSubject;
    }

    /**
     * Setter for property inputNoteSubject.
     * @param inputNoteSubject New value of property inputNoteSubject.
     */
    public void setInputNoteSubject(HtmlInputText inputNoteSubject) {
        this.inputNoteSubject = inputNoteSubject;
    }

    /**
     * Holds value of property inputNoteType.
     */
    private HtmlInputText inputNoteType;

    /**
     * Getter for property inputNoteType.
     * @return Value of property inputNoteType.
     */
    public HtmlInputText getInputNoteType() {
        return this.inputNoteType;
    }

    /**
     * Setter for property inputNoteType.
     * @param inputNoteType New value of property inputNoteType.
     */
    public void setInputNoteType(HtmlInputText inputNoteType) {
        this.inputNoteType = inputNoteType;
    }

    /**
     * Holds value of property inputOtherId.
     */
    private HtmlInputText inputOtherId;

    /**
     * Getter for property inputOtherId.
     * @return Value of property inputOtherId.
     */
    public HtmlInputText getInputOtherId() {
        return this.inputOtherId;
    }

    /**
     * Setter for property inputOtherId.
     * @param inputOtherId New value of property inputOtherId.
     */
    public void setInputOtherId(HtmlInputText inputOtherId) {
        this.inputOtherId = inputOtherId;
    }

    /**
     * Holds value of property inputOtherIdAgency.
     */
    private HtmlInputText inputOtherIdAgency;

    /**
     * Getter for property inputOtherIdAgency.
     * @return Value of property inputOtherIdAgency.
     */
    public HtmlInputText getInputOtherIdAgency() {
        return this.inputOtherIdAgency;
    }

    /**
     * Setter for property inputOtherIdAgency.
     * @param inputOtherIdAgency New value of property inputOtherIdAgency.
     */
    public void setInputOtherIdAgency(HtmlInputText inputOtherIdAgency) {
        this.inputOtherIdAgency = inputOtherIdAgency;
    }

    /**
     * Holds value of property inputGrantAgency.
     */
    private HtmlInputText inputGrantAgency;

    /**
     * Getter for property inputGrantAgency.
     * @return Value of property inputGrantAgency.
     */
    public HtmlInputText getInputGrantAgency() {
        return this.inputGrantAgency;
    }

    /**
     * Setter for property inputGrantAgency.
     * @param inputGrantAgency New value of property inputGrantAgency.
     */
    public void setInputGrantAgency(HtmlInputText inputGrantAgency) {
        this.inputGrantAgency = inputGrantAgency;
    }

    /**
     * Holds value of property inputSeries.
     */
    private HtmlInputText inputSeries;

    /**
     * Getter for property inputSeries.
     * @return Value of property inputSeries.
     */
    public HtmlInputText getInputSeries() {
        return this.inputSeries;
    }

    /**
     * Setter for property inputSeries.
     * @param inputSeries New value of property inputSeries.
     */
    public void setInputSeries(HtmlInputText inputSeries) {
        this.inputSeries = inputSeries;
    }

    /**
     * Holds value of property inputSeriesInformation.
     */
    private HtmlInputText inputSeriesInformation;

    /**
     * Getter for property inputSeriesInformation.
     * @return Value of property inputSeriesInformation.
     */
    public HtmlInputText getInputSeriesInformation() {
        return this.inputSeriesInformation;
    }

    /**
     * Setter for property inputSeriesInformation.
     * @param inputSeriesInformation New value of property inputSeriesInformation.
     */
    public void setInputSeriesInformation(HtmlInputText inputSeriesInformation) {
        this.inputSeriesInformation = inputSeriesInformation;
    }

    /**
     * Holds value of property inputVersion.
     */
    private HtmlInputText inputVersion;

    /**
     * Getter for property inputVersion.
     * @return Value of property inputVersion.
     */
    public HtmlInputText getInputVersion() {
        return this.inputVersion;
    }

    /**
     * Setter for property inputVersion.
     * @param inputVersion New value of property inputVersion.
     */
    public void setInputVersion(HtmlInputText inputVersion) {
        this.inputVersion = inputVersion;
    }

    /**
     * Holds value of property inputVersionDate.
     */
    private HtmlInputText inputVersionDate;

    /**
     * Getter for property inputVersionDate.
     * @return Value of property inputVersionDate.
     */
    public HtmlInputText getInputVersionDate() {
        return this.inputVersionDate;
    }

    /**
     * Setter for property inputVersionDate.
     * @param inputVersionDate New value of property inputVersionDate.
     */
    public void setInputVersionDate(HtmlInputText inputVersionDate) {
        this.inputVersionDate = inputVersionDate;
    }

    /**
     * Holds value of property inputDistributorContact.
     */
    private HtmlInputText inputDistributorContact;

    /**
     * Getter for property inputDistributorContact.
     * @return Value of property inputDistributorContact.
     */
    public HtmlInputText getInputDistributorContact() {
        return this.inputDistributorContact;
    }

    /**
     * Setter for property inputDistributorContact.
     * @param inputDistributorContact New value of property inputDistributorContact.
     */
    public void setInputDistributorContact(HtmlInputText inputDistributorContact) {
        this.inputDistributorContact = inputDistributorContact;
    }

    /**
     * Holds value of property inputDistributorContactAffiliation.
     */
    private HtmlInputText inputDistributorContactAffiliation;

    /**
     * Getter for property inputDistributorContactAffiliation.
     * @return Value of property inputDistributorContactAffiliation.
     */
    public HtmlInputText getInputDistributorContactAffiliation() {
        return this.inputDistributorContactAffiliation;
    }

    /**
     * Setter for property inputDistributorContactAffiliation.
     * @param inputDistributorContactAffiliation New value of property inputDistributorContactAffiliation.
     */
    public void setInputDistributorContactAffiliation(HtmlInputText inputDistributorContactAffiliation) {
        this.inputDistributorContactAffiliation = inputDistributorContactAffiliation;
    }

    /**
     * Holds value of property inputDistributorContactEmail.
     */
    private HtmlInputText inputDistributorContactEmail;

    /**
     * Getter for property inputDistributorContactEmail.
     * @return Value of property inputDistributorContactEmail.
     */
    public HtmlInputText getInputDistributorContactEmail() {
        return this.inputDistributorContactEmail;
    }

    /**
     * Setter for property inputDistributorContactEmail.
     * @param inputDistributorContactEmail New value of property inputDistributorContactEmail.
     */
    public void setInputDistributorContactEmail(HtmlInputText inputDistributorContactEmail) {
        this.inputDistributorContactEmail = inputDistributorContactEmail;
    }

    /**
     * Holds value of property inputWestLongitude.
     */
    private HtmlInputText inputWestLongitude;

    /**
     * Getter for property inputWestLongitude.
     * @return Value of property inputWestLongitude.
     */
    public HtmlInputText getInputWestLongitude() {
        return this.inputWestLongitude;
    }

    /**
     * Setter for property inputWestLongitude.
     * @param inputWestLongitude New value of property inputWestLongitude.
     */
    public void setInputWestLongitude(HtmlInputText inputWestLongitude) {
        this.inputWestLongitude = inputWestLongitude;
    }

    /**
     * Holds value of property inputEastLongitude.
     */
    private HtmlInputText inputEastLongitude;

    /**
     * Getter for property inputEastLongitude.
     * @return Value of property inputEastLongitude.
     */
    public HtmlInputText getInputEastLongitude() {
        return this.inputEastLongitude;
    }

    /**
     * Setter for property inputEastLongitude.
     * @param inputEastLongitude New value of property inputEastLongitude.
     */
    public void setInputEastLongitude(HtmlInputText inputEastLongitude) {
        this.inputEastLongitude = inputEastLongitude;
    }

    /**
     * Holds value of property inputNorthLatitude.
     */
    private HtmlInputText inputNorthLatitude;

    /**
     * Getter for property inputNorthLatitude.
     * @return Value of property inputNorthLatitude.
     */
    public HtmlInputText getInputNorthLatitude() {
        return this.inputNorthLatitude;
    }

    /**
     * Setter for property inputNorthLatitude.
     * @param inputNorthLatitude New value of property inputNorthLatitude.
     */
    public void setInputNorthLatitude(HtmlInputText inputNorthLatitude) {
        this.inputNorthLatitude = inputNorthLatitude;
    }

    /**
     * Holds value of property inputSouthLatitude.
     */
    private HtmlInputText inputSouthLatitude;

    /**
     * Getter for property inputSouthLatitude.
     * @return Value of property inputSouthLatitude.
     */
    public HtmlInputText getInputSouthLatitude() {
        return this.inputSouthLatitude;
    }

    /**
     * Setter for property inputSouthLatitude.
     * @param inputSouthLatitude New value of property inputSouthLatitude.
     */
    public void setInputSouthLatitude(HtmlInputText inputSouthLatitude) {
        this.inputSouthLatitude = inputSouthLatitude;
    }
/*    
    private DvnDate productionDate;

    public DvnDate getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(DvnDate productionDate) {
        this.productionDate = productionDate;
    }
  
    private void initDvnDates() {
        productionDate = new DvnDate(study.getProductionDate());
        
    }
  */
    
    

     private HtmlCommandButton saveCommand1;
     private HtmlCommandButton saveCommand2;
     private HtmlSelectOneRadio showFields;
    
    public HtmlSelectOneRadio getShowFields() {
        return showFields;
    }

    public void setShowFields(HtmlSelectOneRadio showFields) {
        this.showFields = showFields;
    }

    public HtmlCommandButton getSaveCommand1() {
        return saveCommand1;
    }

    public void setSaveCommand1(HtmlCommandButton saveCommand1) {
        this.saveCommand1 = saveCommand1;
    }

    public HtmlCommandButton getSaveCommand2() {
        return saveCommand2;
    }

    public void setSaveCommand2(HtmlCommandButton saveCommand2) {
        this.saveCommand2 = saveCommand2;
    }
     


    /**
     * Check to see if this request requires validation.  Validation should occur
     * when the user clicks either Save button.
     *
     * @return
     */
    public boolean isValidateRequired() {
        // Check to see if the current request is from the user clicking one of the save buttons.

        FacesContext fc = FacesContext.getCurrentInstance();
        Map reqParams = fc.getExternalContext().getRequestParameterMap();
        
        boolean validateRequired=  reqParams.containsKey( saveCommand1.getClientId(fc)) || reqParams.containsKey(saveCommand2.getClientId(fc));
        return validateRequired;
    }


    private HtmlDataTable filesDataTable = new HtmlDataTable();

    public HtmlDataTable getFilesDataTable() {
        return filesDataTable;
    }

    public void setFilesDataTable(HtmlDataTable filesDataTable) {
        this.filesDataTable = filesDataTable;
    }
    public DataModel getCustomFieldsDataModel() {
        List values = new ArrayList(); 
        
        for (int i = 0; i < study.getTemplate().getTemplateFields().size(); i++) {
            TemplateField templateField = study.getTemplate().getTemplateFields().get(i);
            StudyField studyField = null;

            if (templateField.getStudyField().isCustomField()) {
                for (StudyField sf : metadata.getStudyFields()) {
                    if (sf.getName().equals(templateField.getStudyField().getName())) { 
                        studyField = sf;
                        break;
                    }                
                }            
            
                Object[] row = new Object[4];
                row[0] = templateField;
                row[1] = getCustomValuesDataModel(studyField);
                row[2] = new Integer(i);
                row[3] = studyField;                
                values.add(row);
            }
            
        }        
        
        return new ListDataModel(values);
    }
    
    private PanelSeries customFieldsPanelSeries;

    public PanelSeries getCustomFieldsPanelSeries() {
        return customFieldsPanelSeries;
    }

    public void setCustomFieldsPanelSeries(PanelSeries customFieldsPanelSeries) {
        this.customFieldsPanelSeries = customFieldsPanelSeries;
    }

    private DataModel getCustomValuesDataModel(StudyField studyField) {
        List values = new ArrayList();   
        
        for (StudyFieldValue sfv : studyField.getStudyFieldValues()) {
            Object[] row = new Object[2];
            row[0] = sfv;
            row[1] = studyField.getStudyFieldValues(); // used by the remove method
            values.add(row);
        }
                
        return new ListDataModel(values);
    }
  
}

