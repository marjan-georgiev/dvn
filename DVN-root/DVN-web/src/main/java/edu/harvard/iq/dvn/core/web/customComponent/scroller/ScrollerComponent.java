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
 * ScrollerComponent.java
 *
 * Created on November 13, 2006, 9:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.web.customComponent.scroller;

// original copyright notice
/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: ScrollerComponent.java,v 1.3 2007/03/02 15:03:51 asone Exp $ */

//package com.sun.javaee.blueprints.components.ui.components;

import javax.el.MethodExpression;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;

import java.io.IOException;
import java.util.Map;

/**
 * This component produces a search engine style scroller that facilitates
 * easy navigation over results that span across several pages. It
 * demonstrates how a component can do decoding and encoding
 * without delegating it to a renderer.
 */
public class ScrollerComponent extends UICommand implements java.io.Serializable  {

    private static final String NORTH = "NORTH";
    private static final String SOUTH = "SOUTH";
    private static final String EAST = "EAST";
    private static final String WEST = "WEST";   

    private static final byte ACTION_NEXT = -1;
    private static final byte ACTION_PREVIOUS = -2;    

    private static final String FORM_NUMBER_ATTR = "com.sun.faces.FormNumber";
                     

    /**
     * The component attribute that tells where to put the user supplied
     * markup in relation to the "jump to the Nth page of results"
     * widget.
     */
    private static final String FACET_MARKUP_ORIENTATION_ATTR =
        "navFacetOrientation";


    public ScrollerComponent() {
        super();
        this.setRendererType(null);
    }

    @Override
    public void decode(FacesContext context) {

        String clientId = getClientId(context);
        Map<String,String> requestParameterMap =
              context.getExternalContext(). getRequestParameterMap();
        String action = requestParameterMap.get(clientId + "_action");
        if (action == null || action.length() == 0) {
            // nothing to decode
            return;
        }
        MethodExpression me = context.getApplication().getExpressionFactory()
              .createMethodExpression(context.getELContext(),
                                      action,
                                      null,
                                      new Class[]{});        
        this.setActionExpression(me);
        
        String curPage = requestParameterMap.get(clientId + "_curPage");
        int currentPage = Integer.valueOf(curPage);
        int actionInt = Integer.valueOf(action);

        // Assert that action's length is 1.
        switch (actionInt) {
            case ACTION_NEXT:
                currentPage++;
                break;
            case ACTION_PREVIOUS:
                currentPage--;
                // Assert 1 < currentPage
                break;
            default:
                currentPage = actionInt;
                break;
        }
        // from the currentPage, calculate the current row to scroll to.
        int currentRow = (currentPage - 1) * getRowsPerPage(context);
        this.getAttributes().put("currentPage", currentPage);
        this.getAttributes().put("currentRow", currentRow);
        this.queueEvent(new ActionEvent(this));
    }


    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        int currentPage = 1;

        ResponseWriter writer = context.getResponseWriter();

        String clientId = getClientId(context);
        Integer curPage = (Integer) getAttributes().get("currentPage");
        if (curPage != null) {
            currentPage = curPage;
        }
        int totalPages = getTotalPages(context);

        writer.write("<table border=\"0\" cellpadding=\"0\" align=\"right\">");
        writer.write("<tr align=\"center\" valign=\"top\">");
        //writer.write(
        //    "<td><font size=\"-1\">Result&nbsp;Page:&nbsp;</font></td>");

        // write the Previous link if necessary
        writer.write("<td>");
        writeNavWidgetMarkup(context, clientId, ACTION_PREVIOUS,
                             (1 < currentPage));
        // last arg is true iff we're not the first page
        writer.write("</td>");

        // render the page navigation links       
        int first = 1;
        int last = totalPages;

        if (10 < currentPage) {
            first = currentPage - 10;
        }
        if ((currentPage + 9) < totalPages) {
            last = currentPage + 9;
        }
        for (int i = first; i <= last; i++) {
            writer.write("<td>");
            writeNavWidgetMarkup(context, clientId, i, (i != currentPage));
            writer.write("</td>");
        }

        // write the Next link if necessary
        writer.write("<td>");
        writeNavWidgetMarkup(context, clientId, ACTION_NEXT,
                             (currentPage < totalPages));
        writer.write("</td>");
        writer.write("</tr>");
        writer.write(getHiddenFields(clientId));
        writer.write("</table>");
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }


    /**
     * <p>Return the component family for this component.</p>
     */
    @Override
    public String getFamily() {

        return ("Scroller");

    }

    //
    // Helper methods
    // 

    /**
     * Write the markup to render a navigation widget.  Override this to
     * replace the default navigation widget of link with something
     * else.
     */
    protected void writeNavWidgetMarkup(FacesContext context,
                                        String clientId,
                                        int navActionType,
                                        boolean enabled) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String facetOrientation = NORTH;
        String facetName;
        String linkText;
        UIComponent facet;
        boolean isPageNumber = false;

        // Assign values for local variables based on the navActionType
        switch (navActionType) {
            case ACTION_NEXT:
                facetName = "next";
                linkText = "Next";
                break;
            case ACTION_PREVIOUS:
                facetName = "previous";
                linkText = "Previous";
                break;
            default:
                facetName = "number";
                linkText = "" + navActionType;
                isPageNumber = true;
                // heuristic: if navActionType is number, and we are not
                // enabled, this must be the current page.
                if (!enabled) {
                    facetName = "current";
                }
                break;
        }

        // leverage any navigation facets we have
        writer.write("\n&nbsp;");
        if (enabled) {
            writer.write("<a " + getAnchorAttrs(context, clientId,
                                                navActionType) + ">");
        }

        facet = getFacet(facetName);
        // render the facet pertaining to this widget type in the NORTH
        // and WEST cases.
        if (facet != null) {
            // If we're rendering a "go to the Nth page" link
            if (isPageNumber) {
                // See if the user specified an orientation
                facetOrientation = (String) getAttributes().get(
                      FACET_MARKUP_ORIENTATION_ATTR);
                // verify that the orientation is valid
                if (!(facetOrientation.equalsIgnoreCase(NORTH) ||
                      facetOrientation.equalsIgnoreCase(SOUTH) ||
                      facetOrientation.equalsIgnoreCase(EAST) ||
                      facetOrientation.equalsIgnoreCase(WEST))) {
                    facetOrientation = NORTH;
                }
            }

            // output the facet as specified in facetOrientation
            if (facetOrientation.equalsIgnoreCase(NORTH) ||
                facetOrientation.equalsIgnoreCase(EAST)) {
                facet.encodeBegin(context);
                if (facet.getRendersChildren()) {
                    facet.encodeChildren(context);
                }
                facet.encodeEnd(context);
            }
            // The difference between NORTH and EAST is that NORTH
            // requires a <br>.
            if (facetOrientation.equalsIgnoreCase(NORTH)) {
                writer.startElement("br", null); // PENDING(craigmcc)
                writer.endElement("br");
            }
        }

        // if we have a facet, only output the link text if
        // navActionType is number
        if (null != facet) {
            if (navActionType != ACTION_NEXT &&
                navActionType != ACTION_PREVIOUS) {
                writer.write(linkText);
            }
        } else {
            writer.write(linkText);
        }

        // output the facet in the EAST and SOUTH cases
        if (null != facet) {
            if (facetOrientation.equalsIgnoreCase(SOUTH)) {
                writer.startElement("br", null); // PENDING(craigmcc)
                writer.endElement("br");
            }
            // The difference between SOUTH and WEST is that SOUTH
            // requires a <br>.
            if (facetOrientation.equalsIgnoreCase(SOUTH) ||
                facetOrientation.equalsIgnoreCase(WEST)) {
                facet.encodeBegin(context);
                if (facet.getRendersChildren()) {
                    facet.encodeChildren(context);
                }
                facet.encodeEnd(context);
            }
        }

        if (enabled) {
            writer.write("</a>");
        }

    }


    /**
     * <p>Build and return the string consisting of the attibutes for a
     * result set navigation link anchor.</p>
     *
     * @param context  the FacesContext
     * @param clientId the clientId of the enclosing UIComponent
     * @param action   the value for the rhs of the =
     *
     * @return a String suitable for setting as the value of a navigation
     *         href.
     */
    private String getAnchorAttrs(FacesContext context, String clientId,
                                  int action) {
        int currentPage = 1;
        //int formNumber = getFormNumber(context);
        Integer curPage = (Integer) getAttributes().get("currentPage");
        if (curPage != null) {
            currentPage = curPage;
        }
        String formId = getFormId(context);
        if (formId == null){
          return
            ("href=\"javascript:void(0);document.forms[0]['" + clientId +
            "_action'].value='" +
            action +
            "'; " +
            "document.forms[0]['" + clientId +
            "_curPage'].value='" +
            currentPage +
            "'; " +
            "document.forms[0].submit()\"");
        } else {
          return
            ("href=\"javascript:void(0);document.forms['" + formId + "']['" + clientId +
            "_action'].value='" +
            action +
            "'; " +
            "document.forms['" + formId + "']['" + clientId +
            "_curPage'].value='" +
            currentPage +
            "'; " +
            "document.forms['" + formId + "'].submit()\"");
        }
    }

    /*
      This id-based solution was taken from 
      http://forum.java.sun.com/thread.jspa?threadID=502032&messageID=2394163
    */
    protected String getFormId(FacesContext context) {
      UIComponent lookingForForm = this; 
      while (lookingForForm != null) {
        if (lookingForForm instanceof UIForm){
          return lookingForForm.getClientId(context);
        }
        lookingForForm = lookingForForm.getParent(); 
      }
      return null;
    }


    private String getHiddenFields(String clientId) {
        
        return  
            ("<input type=\"hidden\" name=\"" + clientId + "_action\"/>\n" +
            "<input type=\"hidden\" name=\"" + clientId + "_curPage\"/>");
        
    }


    // PENDING: avoid doing this each time called.  Perhaps
    // store in our own attr?
    protected UIForm getForm(FacesContext context) {
        UIComponent parent = this.getParent();
        while (parent != null) {
            if (parent instanceof UIForm) {
                break;
            }
            parent = parent.getParent();
        }
        return (UIForm) parent;
    }


    protected int getFormNumber(FacesContext context) {
        Map<String,Object> requestMap = 
              context.getExternalContext().getRequestMap();               
        Integer formsInt = (Integer) requestMap.get(FORM_NUMBER_ATTR);
        // find out the current number of forms in the page.
        if (formsInt != null) {
            formsInt--;
        } else {
            formsInt = 0;
        }
        return formsInt;
    }


    /**
     * Returns the total number of pages in the result set based on
     * <code>rows</code> and <code>rowCount</code> of <code>UIData</code>
     * component that this scroller is associated with.
     * For the purposes of this demo, we are assuming the <code>UIData</code> to
     * be child of <code>UIForm</code> component and not nested inside a custom
     * NamingContainer.
     */
    protected int getTotalPages(FacesContext context) {
        String forValue = (String) getAttributes().get("for");
        UIData uiData = (UIData) getForm(context).findComponent(forValue);
        if (uiData == null) {
            return 0;
        }
        int rowsPerPage = uiData.getRows();                
        int totalRows = uiData.getRowCount();
        int result = totalRows / rowsPerPage;
        if (0 != (totalRows % rowsPerPage)) {
            result++;
        }
        return result;
    }


    /**
     * Returns the number of rows to display by looking up the
     * <code>UIData</code> component that this scroller is associated with.
     * For the purposes of this demo, we are assuming the <code>UIData</code> to
     * be child of <code>UIForm</code> component and not nested inside a custom
     * NamingContainer.
     */
    protected int getRowsPerPage(FacesContext context) {
        String forValue = (String) getAttributes().get("for");
        UIData uiData = (UIData) getForm(context).findComponent(forValue);
        if (uiData == null) {
            return 0;
        }
        return uiData.getRows();
    }
} 

