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

import edu.harvard.iq.dvn.core.study.DataTable;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.*;

/**
 *
 * @author skraffmiller
 */
@Entity
public class VarGrouping implements Serializable {

    private String name;
    public enum GroupingType { MEASURE, FILTER, SOURCE };

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @Enumerated(EnumType.STRING)
    private GroupingType groupingType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public GroupingType getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(GroupingType groupingType) {
        this.groupingType = groupingType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Holds value of property groups.
     */
    @OneToMany(mappedBy="varGrouping", cascade={CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST})
    @OrderBy ("name")
    private List<VarGroup> varGroups;

     /**
     * Getter for property loginAffiliates.
     * @return Value of property loginAffiliates.
     */
    public List<VarGroup> getVarGroups() {
        return this.varGroups;
    }

    /**
     * Setter for property loginAffiliates.
     * @param loginAffiliates New value of property loginAffiliates.
     */
    public void setGroups(List<VarGroup> varGroups) {
        this.varGroups = varGroups;
    }


    /**
     * Holds value of property groups.
     */
    @OneToMany(mappedBy="varGrouping", cascade={CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST})
    @OrderBy ("name")
    private Collection<VarGroupType> varGroupTypes;

     /**
     * Getter for property loginAffiliates.
     * @return Value of property loginAffiliates.
     */
    public Collection<VarGroupType> getVarGroupTypes() {
        return this.varGroupTypes;
    }

    /**
     * Setter for property loginAffiliates.
     * @param loginAffiliates New value of property loginAffiliates.
     */
    public void setVarGroupTypes(Collection<VarGroupType> varGroupTypes) {
        this.varGroupTypes = varGroupTypes;
    }


    /**
     * Holds value of the DataTable
     */
    @ManyToOne
    @JoinColumn(nullable=false)
    private DataTable dataTable;


    public DataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    @OneToMany (mappedBy="varGrouping", cascade={ CascadeType.REMOVE, CascadeType.MERGE,CascadeType.PERSIST})
    private Collection<DataVariableMapping> dataVariableMapping;

    public Collection<DataVariableMapping> getDataVariableMappings() {
        return this.dataVariableMapping;
    }

    public void setDataVariableMappings(Collection<DataVariableMapping> dataVariableMapping) {
        this.dataVariableMapping = dataVariableMapping;
    }



    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {        
        if (!(object instanceof VarGrouping)) {
            return false;
        }
        VarGrouping other = (VarGrouping) object;
        if (this == other){
            return true;
        }
        if ((!this.groupingType.equals(GroupingType.FILTER) && this.groupingType.equals(other.groupingType) )
                || (this.groupingType.equals(GroupingType.FILTER) && this.name.equals(other.name))) {
            return true;
        } else {
            return false;
        }
        /*
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
         * 
         */
    }

    @Override
    public String toString() {
        return "edu.harvard.iq.dvn.core.visualization.VarGrouping[id=" + id + "]";
    }

}
