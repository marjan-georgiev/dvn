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
 * FileMetadataField.java
 *
 * Created on Feb 13, 2013, 3:13 PM
 *
 */
package edu.harvard.iq.dvn.core.study;

import edu.harvard.iq.dvn.core.util.StringUtil;
import java.util.Collection;

import edu.harvard.iq.dvn.core.vdc.VDC;
import edu.harvard.iq.dvn.core.vdc.VDCCollection;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Leonid Andreev
 */

// This is the studyfile-level equivalent of the StudyField table; this will 
// store metadata fields associated with study files. 
// For consistency with the StudyField and StudyFieldValue, I could have called 
// it "StudyFileField"; but decided to go wtih "FileMetadataField" and 
// "FileMetadataFieldValue", to have the names that are more descriptive. 


@Entity
public class FileMetadataField implements Serializable {
    /**
     * Properties: 
     * ==========
     */
    /**
     * Holds value of property id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", columnDefinition="TEXT")
    private String name;    // This is the internal, DDI-like name, no spaces, etc.
    @Column(name="title", columnDefinition="TEXT")
    private String title;   // A longer, human-friendlier name - punctuation allowed
    @Column(name="description", columnDefinition="TEXT")
    private String description; // A user-friendly Description; will be used for 
                                // mouse-overs, etc. 
    
    // TODO: 
    // decide if we even need this "custom field" flag; since all the file-level
    // fields are going to be custom. 
    // On the other hand, we may want to add a set of standard file-level fields 
    // - something like "author", "date" and "keyword" maybe? - General enough
    // attributes that can be associated with any document. 
    
    private boolean customField; 
    private boolean basicSearchField;
    private boolean advancedSearchField;
    private boolean searchResultField;
    private boolean prefixSearchable;
   
    private String fileFormatName;
    
    private int displayOrder;
    
    /**
     * Constructors: 
     * ============
     */
   
    /** Creates a new instance of FileMetadataField */
    public FileMetadataField() {
    }

    
    /**
     * Getters and Setters:
     * ===================
     */
    
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    public int getDisplayOrder() {
        return this.displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isCustomField() {
        return customField;
    }

    public void setCustomField(boolean customField) {
        this.customField = customField;
    }
   
    public String getFileFormatName() {
        return fileFormatName;
    }

    public void setFileFormatName(String fileFormatName) {
        this.fileFormatName = fileFormatName;
    }

    public boolean isBasicSearchField() {
        return this.basicSearchField;
    }

    public void setBasicSearchField(boolean basicSearchField) {
        this.basicSearchField = basicSearchField;
    }

    public boolean isAdvancedSearchField() {
        return this.advancedSearchField;
    }

    public void setAdvancedSearchField(boolean advancedSearchField) {
        this.advancedSearchField = advancedSearchField;
    }

    public boolean isSearchResultField() {
        return this.searchResultField;
    }

    public void setSearchResultField(boolean searchResultField) {
        this.searchResultField = searchResultField;
    }


    public boolean isPrefixSearchable() {
        return this.prefixSearchable;
    }

    public void setPrefixSearchable(boolean prefixSearchale) {
        this.prefixSearchable = prefixSearchable;
    }
    /* 
     * TODO: 
     * decide if we need nested fields; 
     * I would guess not. But then we may need them sometime in the future - 
     * would it be prudent to implement them from the get go then?
     * - L.A.
     */
    
    /*
    @OneToMany(mappedBy = "parentFileMetadataField", cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST})
    @OrderBy("displayOrder ASC")
    private Collection<FileMetadataField> childFileMetadataFields;

    public Collection<FileMetadataField> getChildFileMetadataFields() {
        return this.childFileMetadataFields;
    }

    public void setChildFileMetadataFields(Collection<FileMetadataField> childFileMetadataFields) {
        this.childFileMetadataFields = childFileMetadataFields;
    }
    @ManyToOne(cascade = CascadeType.MERGE)
    private FileMetadataField parentFileMetadataField;

    public FileMetadataField getParentFileMetadataField() {
        return parentFileMetadataField;
    }

    public void setParentFileMetadataField(FileMetadataField parentFileMetadataField) {
        this.parentFileMetadataField = parentFileMetadataField;
    }
    * */
    
    
    /**
     * TODO: 
     * ====
     * Decide if we want to replicate the StudyField mappings for VDC- and 
     * Collection-level advanced/any/etc. search fields, below:
     */

    /*
    @ManyToMany(mappedBy="advSearchFields",cascade={CascadeType.REMOVE })
    private Collection<VDC> advSearchFieldVDCs;

   @ManyToMany(mappedBy="searchResultFields",cascade={CascadeType.REMOVE })
    private Collection<VDC> searchResultFieldVDCs;
   
   @ManyToMany(mappedBy="anySearchFields",cascade={CascadeType.REMOVE })
    private Collection<VDC> anySearchFieldVDCs;
    
   @ManyToMany(mappedBy="advSearchFields",cascade={CascadeType.REMOVE })
    private Collection<VDCCollection> advSearchFieldColls;

   @ManyToMany(mappedBy="searchResultFields",cascade={CascadeType.REMOVE })
    private Collection<VDCCollection> searchResultFieldColls;
   
   @ManyToMany(mappedBy="anySearchFields",cascade={CascadeType.REMOVE })
    private Collection<VDCCollection> anySearchFieldColls;
    * */

    /**
     * TODO: 
     * ====
     * review if we want/need persistence in this direction:
    @OneToMany (mappedBy="fileMetadataField",  cascade={ CascadeType.REMOVE, CascadeType.MERGE,CascadeType.PERSIST})
    private List<FileMetadataFieldValue> fileMetadataFieldValues;

    public List<FileMetadataFieldValue> getFileMetadataFieldValues() {
        return fileMetadataFieldValues;
    }  
       
    public void setFileMetadataFieldValues(List<FileMetadataFieldValue> fileMetadataFieldValues) {
        this.fileMetadataFieldValues = fileMetadataFieldValues;
    }      
    
    // helper methods for getting the internal string values
    public List<String> getFileMetadataFieldValueStrings() {
        List <String> retString = new ArrayList();
        for (FileMetadataFieldValue sfv:fileMetadataFieldValues){
            if ( !StringUtil.isEmpty(sfv.getStrValue()) ) {
                retString.add(sfv.getStrValue());
            }
        }
        return retString;
    }
    
    public String getFileMetadataFieldValueSingleString() {
        return fileMetadataFieldValues.size() > 0 ? fileMetadataFieldValues.get(0).getStrValue() : "";
    }
    
    public void setFileMetadataFieldValueStrings(List<String> newValList) {}

    public void setFileMetadataFieldValueSingleString(String newVal) {}
    */
      
    /**
     * Helper methods:
     * ==============
     * 
     */
    public int hashCode() {
        int hash = 0;
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FileMetadataField)) {
            return false;
        }
        FileMetadataField other = (FileMetadataField)object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) return false;
        return true;
    }      
    
}
