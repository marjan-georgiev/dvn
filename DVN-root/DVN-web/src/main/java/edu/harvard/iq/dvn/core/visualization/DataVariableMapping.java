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
import edu.harvard.iq.dvn.core.study.DataVariable;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author skraffmiller
 */
@Entity
public class DataVariableMapping implements Serializable {
    private boolean x_axis;
    private String label;



    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (cascade={ CascadeType.PERSIST })
    @JoinColumn(nullable=true)
    private VarGroup varGroup;

    @ManyToOne (cascade={ CascadeType.PERSIST })
    @JoinColumn(nullable=false)
    private DataTable dataTable;

    @ManyToOne (cascade={ CascadeType.PERSIST })
    @JoinColumn(nullable=true)
    private VarGrouping varGrouping;

    public DataVariable getDataVariable() {
        return dataVariable;
    }

    public void setDataVariable(DataVariable dataVariable) {
        this.dataVariable = dataVariable;
    }

    @ManyToOne
    @JoinColumn(nullable=false)
    private DataVariable dataVariable;

    public DataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isX_axis() {
        return x_axis;
    }

    public void setX_axis(boolean x_axis) {
        this.x_axis = x_axis;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public VarGroup getGroup() {
        return varGroup;
    }

    public void setGroup(VarGroup group) {
        this.varGroup = group;
    }


    public VarGrouping getVarGrouping() {
        return varGrouping;
    }

    public void setVarGrouping(VarGrouping varGrouping) {
        this.varGrouping = varGrouping;
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
        if (!(object instanceof DataVariableMapping)) {
            return false;
        }
        DataVariableMapping other = (DataVariableMapping) object;
        if (this == other){            
            return true;
        }
        if (this.dataVariable.equals(other.dataVariable)
                && ((this.varGroup == null  && other.varGroup == null) || this.varGroup.equals(other.varGroup)) &&
                ((this.varGrouping == null && other.varGrouping == null) || this.varGrouping.equals(other.varGrouping))){           
            return true;
        } else {
            
            return false;
        }
        /*
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
         
         */
    }

    @Override
    public String toString() {
        return "edu.harvard.iq.dvn.core.visualization.DataVaraibleMapping[id=" + id + "]";
    }

}
