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
package edu.harvard.iq.dvn.core.visualization;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;
import javax.persistence.*;

/**
 *
 * @author skraffmiller
 */
@Entity
public class VarGroup implements Serializable, Comparable <VarGroup> {
    public static final String ORDER_BY_NAME = "name";
    public static final String ORDER_BY_GROUPTYPE = "groupTypes";
    private String name;
    private String units;

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

    /**
     * Holds value of property userGroup.
     */
    @ManyToOne
    @JoinColumn(nullable=false)
    private VarGrouping varGrouping;

    /**
     * Getter for property userGroup.
     * @return Value of property userGroup.
     */
    public VarGrouping getGroupAssociation() {
        return this.varGrouping;
    }

    /**
     * Setter for property userGroup.
     * @param userGroup New value of property userGroup.
     */
    public void setGroupAssociation(VarGrouping varGrouping) {
        this.varGrouping = varGrouping;
    }



    @ManyToMany (cascade={ CascadeType.PERSIST, CascadeType.REMOVE })
    @JoinTable(name = "GROUP_GROUPTYPES",
    joinColumns = @JoinColumn(name = "group_id"),
    inverseJoinColumns = @JoinColumn(name = "group_type_id"))
    private List<VarGroupType> groupTypes;

    /**
     * Getter for property issues.
     * @return Value of property issues.
     */
    public List<VarGroupType> getGroupTypes() {
        return this.groupTypes;
    }

    public void setGroupTypes(List<VarGroupType> groupTypes) {
        this.groupTypes = groupTypes;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof VarGroup)) {
            return false;
        }
        VarGroup other = (VarGroup) object;
        if (this == other){
            return true;
        }
        if (this.getGroupAssociation().equals(other.getGroupAssociation())
                && this.getName().equals(other.getName())) {
            return true;
        } else {
            return false;
        }
        /*
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        System.out.println("default true:  "+ this.getName());
        return true;
         * */

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public int compareTo(VarGroup o) {
        List <VarGroupType> thisVarGroupTypeList = this.getGroupTypes();
        List <VarGroupType> oVarGroupTypeList = o.getGroupTypes();
        
        if(thisVarGroupTypeList.isEmpty() && oVarGroupTypeList.isEmpty() ){
            return this.getName().compareTo(o.getName());
        }

        if(thisVarGroupTypeList.isEmpty() && !oVarGroupTypeList.isEmpty() ){
            return -1;
        }
        
        if(!thisVarGroupTypeList.isEmpty() && oVarGroupTypeList.isEmpty() ){
            return 1;
        }
        
        VarGroupType thisFirstGroupType = thisVarGroupTypeList.get(0);
        VarGroupType oFirstGroupType = oVarGroupTypeList.get(0);
        
        if (!thisFirstGroupType.equals(oFirstGroupType)){
            return thisFirstGroupType.getName().compareTo(oFirstGroupType.getName());
        }  else {
            return this.getName().compareTo(o.getName());
        }

    }

}
