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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.web.admin;

import edu.harvard.iq.dvn.core.study.ControlledVocabulary;
import edu.harvard.iq.dvn.core.study.ControlledVocabularyValue;
import edu.harvard.iq.dvn.core.study.TemplateServiceLocal;
import edu.harvard.iq.dvn.core.util.DateUtil;
import edu.harvard.iq.dvn.core.web.common.VDCBaseBean;
import edu.harvard.iq.dvn.core.web.util.EmailValidator;
import edu.harvard.iq.dvn.core.web.util.UrlValidator;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.inject.Named;

/**
 *
 * @author gdurand
 */

@Named
@ViewScoped
public class EditControlledVocabularyPage extends VDCBaseBean implements java.io.Serializable  {

    @EJB TemplateServiceLocal templateService;
       
    private Long cvId;
    private ControlledVocabulary controlledVocabulary;   
    private String newControlledVocabularyValue;
    private List<String> selectedControlledVocabularyValues;
    private boolean showPopup = false;
    
    public void init(){
        if (cvId != null) {
            controlledVocabulary = templateService.getControlledVocabulary(cvId);
        } else {
           controlledVocabulary = new ControlledVocabulary();
           controlledVocabulary.setControlledVocabularyValues(new ArrayList());
        }  
    }

    public ControlledVocabulary getControlledVocabulary() {
        return controlledVocabulary;
    }

    public void setControlledVocabulary(ControlledVocabulary controlledVocabulary) {
        this.controlledVocabulary = controlledVocabulary;
    }

    public Long getCvId() {
        return cvId;
    }

    public void setCvId(Long cvId) {
        this.cvId = cvId;
    }

    public String getNewControlledVocabularyValue() {
        return newControlledVocabularyValue;
    }

    public void setNewControlledVocabularyValue(String newControlledVocabularyValue) {
        this.newControlledVocabularyValue = newControlledVocabularyValue;
    }
    
    public boolean isShowPopup() {
        return showPopup;
    }

    public void setShowPopup(boolean showPopup) {
        this.showPopup = showPopup;
    }
    
    public List<String> getSelectedControlledVocabularyValues() {
        return selectedControlledVocabularyValues;
    }

    public void setSelectedControlledVocabularyValues(List<String> selectedControlledVocabularyValues) {
        this.selectedControlledVocabularyValues = selectedControlledVocabularyValues;
    }
    
    public void exit() {
    }
    
    public List<SelectItem> getControlledVocabularySelectItems() {
        List selectItems = new ArrayList();
            for (ControlledVocabularyValue cvv : controlledVocabulary.getControlledVocabularyValues()) {
                SelectItem si = new SelectItem(cvv.getValue());
                selectItems.add(si);
            }
        return selectItems;
    }
    
    public void addControlledVocabularyValue() {
        boolean success = true;
        if(newControlledVocabularyValue.isEmpty()){
            return;
        }
        for (ControlledVocabularyValue cvv: controlledVocabulary.getControlledVocabularyValues() ){
            if (newControlledVocabularyValue.equals(cvv.getValue())) {
                return;
            }
        }
               
        ControlledVocabularyValue cvv = new ControlledVocabularyValue();
        if (!newControlledVocabularyValue.contains("\n")){
            cvv.setControlledVocabulary(controlledVocabulary);
            cvv.setValue(newControlledVocabularyValue);
            controlledVocabulary.getControlledVocabularyValues().add(cvv);
        } else {
            String splitString[] = newControlledVocabularyValue.split("\n");
            for (int i = 0; i < splitString.length; i++) {
                boolean added = false;
                ControlledVocabularyValue cvvm = new ControlledVocabularyValue();
                System.out.print(splitString[i] + " " + i);
                cvvm.setControlledVocabulary(controlledVocabulary);
                cvvm.setValue(splitString[i]);

                for (ControlledVocabularyValue cvvTestMulti: controlledVocabulary.getControlledVocabularyValues() ){
                    if (splitString[i].equals(cvvTestMulti.getValue())) {
                        success = false;
                        added = true;
                    }
                }
                if (!added){
                    controlledVocabulary.getControlledVocabularyValues().add(cvvm);
                }
            }           
        }
        if (success){
           //If added successfully clear out the value(s)
           newControlledVocabularyValue = "";
        }

        Collections.sort(controlledVocabulary.getControlledVocabularyValues());     
    }
            
    public void removeControlledVocabularyValues() {              
        for (String selectedCVV: selectedControlledVocabularyValues ){
            for (Iterator<ControlledVocabularyValue> it = controlledVocabulary.getControlledVocabularyValues().iterator(); it.hasNext();) {
                if (selectedCVV.equals(it.next().getValue())) {
                    it.remove();
                }
            }
        }       
    }

    public String save_action() {
        boolean isNewControlledVocabulary = controlledVocabulary.getId() == null;
        if (validateEntries()) {
            templateService.saveControlledVocabulary(controlledVocabulary);

            if (isNewControlledVocabulary) {
                getVDCRenderBean().getFlash().put("successMessage", "Successfully added new Controlled Vocabulary.");
            } else {
                getVDCRenderBean().getFlash().put("successMessage", "Successfully updated Controlled Vocabulary.");
            }
            return "/networkAdmin/NetworkOptionsPage.xhtml?faces-redirect=true&tab=vocabulary";

        } else {
            getVDCRenderBean().getFlash().put("warningMessage", "One or more entries do not match the chosen Field Type.");
            return "";
        }
    }
    
    public String cancel_action(){
         return "/networkAdmin/NetworkOptionsPage.xhtml?faces-redirect=true&tab=vocabulary";
    }
    
    private boolean validateDescription(){
        boolean isValid = true;
        /*
        if (controlledVocabulary.getDescription().length() > 255){
            isValid = false;
        }*/
        return isValid;
    }
    
    private boolean validateEntries(){
        boolean isValid = true;
        if (controlledVocabulary.getFieldType() == null || controlledVocabulary.getFieldType().isEmpty() || controlledVocabulary.getControlledVocabularyValues().isEmpty()){
            return isValid;
        }
        List <ControlledVocabularyValue> testValues = controlledVocabulary.getControlledVocabularyValues();
        EmailValidator emailVaildator = new EmailValidator();
        UrlValidator urlVaildator = new UrlValidator();
        if (controlledVocabulary.getFieldType().equals("email")){
            for (ControlledVocabularyValue entry: testValues ){
                isValid &= emailVaildator.validateEmail(entry.getValue());
            }
        }        
        if (controlledVocabulary.getFieldType().equals("url")){
            for (ControlledVocabularyValue entry: testValues ){
                try {
                    isValid &= urlVaildator.validateUrl(entry.getValue());
                } catch (MalformedURLException ex) {
                    isValid = false;
                    Logger.getLogger(EditControlledVocabularyPage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
       if (controlledVocabulary.getFieldType().equals("date")){
            for (ControlledVocabularyValue entry: testValues ){
                isValid &= DateUtil.validateDate(entry.getValue());
            }
        } 
        return isValid;
    }

}