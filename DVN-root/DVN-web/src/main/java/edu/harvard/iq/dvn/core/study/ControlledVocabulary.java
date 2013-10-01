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
package edu.harvard.iq.dvn.core.study;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 *
 * @author gdurand
 */
@Entity
public class ControlledVocabulary implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String fieldType;

    @Column(columnDefinition="TEXT") 
    private String description;    
    @OneToMany(mappedBy="controlledVocabulary", orphanRemoval=true, cascade={CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST})
    @OrderBy ("value")
    private List<ControlledVocabularyValue> controlledVocabularyValues;   
    @OneToMany(mappedBy="controlledVocabulary")
    private List<TemplateField> templateFields;    
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;   
    }

    public List<ControlledVocabularyValue> getControlledVocabularyValues() {
        return controlledVocabularyValues;
    }

    public void setControlledVocabularyValues(List<ControlledVocabularyValue> controlledVocabularyValues) {
        this.controlledVocabularyValues = controlledVocabularyValues;
    }
        
    public List<SelectItem> getSelectItems(){
        List selectItems = new ArrayList<SelectItem>();
            for (ControlledVocabularyValue cvv : controlledVocabularyValues) {
                selectItems.add(new SelectItem(cvv.getValue(), cvv.getValue()));
            }
        return selectItems;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
    
    public List<TemplateField> getTemplateFields() {
        return templateFields;
    }

    public void setTemplateFields(List<TemplateField> templateFields) {
        this.templateFields = templateFields;
    }
    
    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ControlledVocabulary)) {
            return false;
        }
        ControlledVocabulary other = (ControlledVocabulary) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "edu.harvard.iq.dvn.core.study.ControlledVocabulary[ id=" + id + " ]";
    }
    
}
