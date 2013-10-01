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
   Version 3.1.
*/
package edu.harvard.iq.dvn.core.vdc;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author skraffmiller
 */
@Entity
public class CustomQuestion implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(nullable=false)
    private GuestBookQuestionnaire guestBookQuestionnaire;
    
    @OneToMany(mappedBy="customQuestion",cascade={CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST},orphanRemoval=true)
    private List<CustomQuestionResponse> customQuestionResponses;

    @OneToMany(mappedBy="customQuestion",cascade={CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST},orphanRemoval=true)
    private List<CustomQuestionValue> customQuestionValues;
    
    private String questionType;
    private String questionString;
    private boolean required;
    
    private boolean hidden;  //when a question is marked for removal, but it has data it is set to hidden

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public GuestBookQuestionnaire getGuestBookQuestionnaire() {
        return guestBookQuestionnaire;
    }

    public void setGuestBookQuestionnaire(GuestBookQuestionnaire guestBookQuestionnaire) {
        this.guestBookQuestionnaire = guestBookQuestionnaire;
    }

    public String getQuestionString() {
        return questionString;
    }

    public void setQuestionString(String questionString) {
        this.questionString = questionString;
    }

    public List<CustomQuestionValue> getCustomQuestionValues() {
        return customQuestionValues;
    }
    
    public String getCustomQuestionValueString(){
        String retString = "";
        
        if (customQuestionValues != null && !this.customQuestionValues.isEmpty()){
            for (CustomQuestionValue customQuestionValue : this.customQuestionValues){
                if (!retString.isEmpty()){
                    retString += ", ";
                } else {
                    retString += "Answers:  ";
                }
                retString += customQuestionValue.getValueString();
            }
        }
        
        return retString;
    }

    public void setCustomQuestionValues(List<CustomQuestionValue> customQuestionValues) {
        this.customQuestionValues = customQuestionValues;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
    
    public List<CustomQuestionResponse> getCustomQuestionResponses() {
        return customQuestionResponses;
    }

    public void setCustomQuestionResponses(List<CustomQuestionResponse> customQuestionResponses) {
        this.customQuestionResponses = customQuestionResponses;
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
        if (!(object instanceof CustomQuestion)) {
            return false;
        }
        CustomQuestion other = (CustomQuestion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "edu.harvard.iq.dvn.core.vdc.CustomQuestion[ id=" + id + " ]";
    }
    
}
