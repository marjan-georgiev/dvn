<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:gui="http://java.sun.com/jsf/facelets"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns:f="http://java.sun.com/jsf/core" 
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:t="/WEB-INF/tlds/scroller"
      xmlns:ui="http://www.sun.com/web/ui"
      >
<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

</head>

<body>
<gui:composition template="/template.xhtml">

<gui:param name="pageTitle" value="DVN - Analysis and Subsetting" />

  <gui:define name="body">
      <f:loadBundle basename="BundleAnalysis" var="bundleAnalysis"/>

    <f:verbatim>
    <script type="text/javascript">
      //<![CDATA[
            // share-remoting related code
            var req=null;
            var cellId;
            var QSrootNodeIdPrefix = "quickSummaryStatTableRootDiv_";
            //var rx =   /\S+:(\d+):showStatistics$/ ;
            var timeoutId;
            function isQStableAttached(rowId){
             // this function tells whether a QS table is already
             // attached to the cell
                //var QSrootId = getQSrootId(imageBttnId);
                var QSrootNode = document.getElementById(QSrootNodeIdPrefix+rowId);
                
                var attached=false;
                if (QSrootNode){
                    attached = true;
                    //alert("QS table is attached");
                }
                return attached;
            }
            
            function getQSrootId(imageBttnId){
                var tokens = imageBttnId.split(/:/); 
                var vId = tokens[tokens.length - 2];
                var QSrootId = imageBttnId.replace(/showStatistics$/, "varSummaryTable");
                return QSrootId;
            }
            
            function QSrequested(bttnId, rowId){
                /*
                var tokens = bttnId.split(/:/); 
                var vId = tokens[tokens.length - 2];
                cellId = bttnId.replace(/showStatistics$/, "varSummaryTable");
                */
                cellId = getQSrootId(bttnId);
                var displayRootNode = document.getElementById(cellId);
                //alert("display attr="+displayRootNode.style.display);
                if (displayRootNode.style.display == "") {
                    //alert("display attr is ''");
                }
                if (displayRootNode.style.display == undefined) {
                    //alert("display attr is undefined");
                }                
                if ((displayRootNode.style.display == "none") || 
                    (displayRootNode.style.display == "")){
                    // show the cell
                    displayRootNode.style.display = "inline";
                    // attache the table if not attached
                    //alert("show case");
                    if (!isQStableAttached(rowId)){
                        //alert("make an ajax call");
                        var cntxpth = "${request.contextPath}";
                        var afterCntxpth = "/sumStat" + "\x3F" + "dtId=${param.dtId}";
                        var qsString = cntxpth + afterCntxpth;
                        var ampVId = "\x26" + "varId=";
                        qsString = qsString + ampVId + rowId;
                        sendRequest(qsString, processQSrequest);
                    }                    
                } else if (displayRootNode.style.display == "inline"){
                    // hide the cell
                    //alert("hide case");
                    displayRootNode.style.display = "none";
                }
            }
            
            function sendRequest(url, handler) {
                initXHR();
                if (req) {
                    try {
                        req.onreadystatechange = handler;
                        req.open("GET", url, true);
                        timeoutId = setTimeout(timesUp, 10000);
                        req.send(null);
                    } catch (errv){
                        alert(
                            "The browser failed to contact the server.\n"+
                            "Please try again later.\n"+
                            "Error message:"+errv.message);
                    }
                } else {
                    alert("Summary statistics cannot be displayed:\n"+
                            "Your browser is incapable of remote-scripting.");
                }
            }
            
            function initXHR() {
                if(window.XMLHttpRequest) {
                    req = new XMLHttpRequest();
                }
                else if(window.ActiveXObject) {
                    req = new ActiveXObject("Microsoft.XMLHTTP");
                }
            }
            
            function processQSrequest() {
                if (req.readyState != 4) {
                    return;
                }
                clearTimeout(timeoutId);
                if (req.status == 200){
                    var QStable = req.responseText;
                    if (QStable){
                        //alert("returned contents = "+QStable);
                        document.getElementById(cellId).innerHTML = QStable;
                    } else {
                        alert("Summary statistics cannot be displayed.\n"+
                        "The browser got an empty string from the server");                        
                    }
                } else if (req.status == 304){
                    alert("Summary statistics cannot be displayed: 304 error");
                } else {
                    alert("Summary statistics cannot be displayed: status code="+req.status);
                }
            }
            
            function timesUp (){
                req.abort();
                alert("The browser failed to get summary statistics from the server.\n"+
                      "Please make sure of your internet connection and try again later");
            }
       // ]]>      
        </script>
     </f:verbatim>
<!-- to be renamed to subview -->


<!-- f:loadBundle basename="vdcnetwar.analysis.AnalysisResources" var="i18nres"/ -->

<!-- html top boiler-plate block -->    

    <h:form id="form1" >
    <input type="hidden" name="dtId" value="${param.dtId}"/>

    <h:inputHidden id="vdcId" value="#{VDCRequest.currentVDCId}"/>

    <div class="dvn_section">
        <span class="dvn_sectionTitleR">
                    <h:outputLink   value="/dvn#{VDCRequest.currentVDCURL}/faces/study/StudyPage.jsp?studyId=#{AnalysisPage.studyId}&amp;tab=files">
                        <h:outputText  value="Back to Study"/>                       
                    </h:outputLink>
         </span>
        <div class="dvn_sectionTitle">                
                <h:outputText value="#{AnalysisPage.studyTitle}"/>                       
                <br />
                <span class="dvn_preFileTitle">Data File: </span>
                <h:outputText styleClass="dvn_fileTitle" value="#{AnalysisPage.fileName}"/>     
        </div>            
        <div class="dvn_sectionBoxNoBorders">  

            <!--tab block starts here -->
        
            <ui:tabSet binding="#{AnalysisPage.tabSet1}" id="tabSet1" lite="true" mini="true" selected="#{AnalysisPage.tab}" styleClass="vdcStudyTabSet">
                
                
                <!-- 1. Download -->
                <ui:tab id="tabDwnld" text="Download Subset" 
                    actionListener="#{AnalysisPage.resetVariableInLBox}" toolTip="Variable Selection and Downloading">
                    <ui:panelLayout id="layoutPanel3" panelLayout="flow" style="width: 100%;">
                        
                        <h:panelGrid cellpadding="0" cellspacing="0" columnClasses="vdcAnalysisCol1, vdcAnalysisCol2" columns="2" id="gridPanel4" width="100%">
                            
                            <ui:panelGroup id="groupPanel2" separator="&lt;br /&gt;">
                                
                                <!-- ui tag solution -->
                                <ui:listbox id="mListboxDwnld" 
                                            items="#{AnalysisPage.varSetAdvStat}" 
                                            rows="10" 
                                            disabled="true"
                                            labelLevel="3" 
                                            monospace="true"  
                                            multiple="true" 
                                            label="#{bundleAnalysis['dwnld.selectedVarBox.title']}"
                                            labelOnTop="true"/>
                                
                                
                                
                                <ui:helpInline id="helpInline2" text="#{bundleAnalysis['dwnld.selectedvarBox.bttmHelpText']}" type="field" rendered="false"/>
   
                         </ui:panelGroup>
                            
                            <ui:panelGroup block="true" id="groupPanel5">
                                <h:graphicImage 
                                    alt="Information" 
                                    title="Information" 
                                    styleClass="vdcNoBorders" 
                                    style="vertical-align: bottom" 
                                    value="/resources/icon_info.gif" />
                                
                                <h:outputText id="outputText13" value="#{bundleAnalysis['dwnld.instruction']}"/>
                                
                                <h:selectOneRadio binding="#{AnalysisPage.dwnldFileTypeSet}" id="dtdwnld" layout="pageDirection">
                                    
                                    <f:selectItem id="dwnldFileTypeText"  itemLabel="#{bundleAnalysis['dwnld.fileType.text']}" itemValue="#{AnalysisPage.dwnldFileTypeItems[0]}"/>
                                    <f:selectItem id="dwnldFileTypeRdata" itemLabel="#{bundleAnalysis['dwnld.fileType.Rdata']}" itemValue="#{AnalysisPage.dwnldFileTypeItems[1]}"/>
                                    <f:selectItem id="dwnldFileTypeSplus" itemLabel="#{bundleAnalysis['dwnld.fileType.Splus']}" itemValue="#{AnalysisPage.dwnldFileTypeItems[2]}"/>
                                    <f:selectItem id="dwnldFileTypeStata" itemLabel="#{bundleAnalysis['dwnld.fileType.stata']}" itemValue="#{AnalysisPage.dwnldFileTypeItems[3]}"/>
                                </h:selectOneRadio>
                                
                                <!-- message block submit button -->
                                <h:panelGroup layout="block">
                                <br/>
                                <h:panelGroup id="pgDwnldErrMsg"
                                    binding="#{AnalysisPage.pgDwnldErrMsg}"
                                    rendered="false"
                                    style="margin:4px 4px;padding:10px;"
                                    >
                                <h:graphicImage 
                                    alt="Error" 
                                    title="Error" 
                                    styleClass="vdcNoBorders" 
                                    style="vertical-align: middle" 
                                    value="/resources/error_small.gif"
                                    rendered="false"/>
                                <ui:staticText id="msgDwnldButton"
                                    binding="#{AnalysisPage.msgDwnldButton}" 
                                    visible="false" 
                                    escape="true" 
                                    styleClass="errorMessage"
                                    
                                    text="#{AnalysisPage.msgDwnldButtonTxt}"
                                    />
                                </h:panelGroup>
                                <br />
                                
                                <h:commandButton id="dwnldBttn"
                                    binding="#{AnalysisPage.dwnldButton}" 
                                    type="submit" 
                                    disabled="true" 
                                    style="margin-top:10px"
                                    value="#{bundleAnalysis['dwnld.button.submit']}" 
                                    action="#{AnalysisPage.dwnldAction}">
                                </h:commandButton>
                                </h:panelGroup>
                            </ui:panelGroup>
   
                        </h:panelGrid>
                        <!-- Edit-variable block-->
                        <ui:panelGroup block="true" rendered="#{AnalysisPage.editVariableActionRendered}" style="padding-top:10px; padding-bottom:10px;">
                            <h:commandLink id="linkAction5" action="#{AnalysisPage.gotoEditVariableAction}">
                                    <h:outputText id="linkAction5Text" value="Edit selected variables"/>   
                            </h:commandLink>
                            <h:outputText id="linkAction5HelpText" styleClass="vdcHelpText" value="(If no variables are selected, all variables will be displayed in Edit Variables page.)"/>
                        </ui:panelGroup>
                    </ui:panelLayout>
                </ui:tab>
                
                
                <!-- 2. Recode -->

                <ui:tab  id="tabRecode" 
                         text="Recode and Case-Subsetting" 
                         actionListener="#{AnalysisPage.resetVariableInLBox}"
                         toolTip="Rocoding and Case Selection">
                    <ui:panelLayout id="layoutPanel2" panelLayout="flow" style="width: 100%;">
                        <h:panelGrid  id="gridPanel7"
                                      cellpadding="0" cellspacing="0" columnClasses="vdcAnalysisCol1, vdcAnalysisCol2" columns="2" width="100%">
                            <!-- 1st column -->
                            <ui:panelGroup id="groupPanel10">
                            <h:panelGrid columns="2">
                                <h:panelGroup>
                                <!-- ui tag solution -->
                                <ui:listbox id="listboxRecode" 
                                    items="#{AnalysisPage.varSetAdvStat}" 
                                    binding="#{AnalysisPage.listboxRecode}"
                                    selected="#{AnalysisPage.selectedRecodeVariable}"
                                    rows="10" 
                                    labelLevel="3" 
                                    monospace="true"  
                                    multiple="false" 
                                    label="#{bundleAnalysis['advStat.selectedVarBox.title']}"
                                    labelOnTop="true"/>
                                <ui:helpInline 
                                    id="helpInline4" 
                                    text="#{bundleAnalysis['recode.selectedvarBox.bttmHelpText']}" 
                                    type="field" 
                                    rendered="false" />
                                </h:panelGroup>
                                <h:panelGroup id="groupPanelRecodeMiddleColumn">
                                
                                <h:commandButton id="moveRecodeVarBttn"
                                     binding="#{AnalysisPage.moveRecodeVarBttn}"
                                     immediate="true"
                                     actionListener="#{AnalysisPage.moveRecodeVariable}"
                                     disabled="true"
                                     style="vertical-align:top"
                                     alt="#{bundleAnalysis['recode.button.moveVar.alt']}"  
                                     value="start" />
                                </h:panelGroup>  
                            </h:panelGrid>
                            </ui:panelGroup>
                            <!-- end: 1st column -->
                            
                            <!-- 2nd column -->
                            
                            <ui:panelGroup id="groupPanel11" block="true">
                                <!-- instruction lines-->
                                <h:panelGroup id="groupPanelRecodeInstruction1" 
                                    binding="#{AnalysisPage.groupPanelRecodeInstruction1}"
                                    rendered="true" 
                                    layout="block">
                                    <h:graphicImage 
                                        alt="Information" 
                                        title="Information" 
                                        styleClass="vdcNoBorders" 
                                        style="vertical-align: bottom" 
                                        value="/resources/icon_info.gif" />
                                    
                                    <h:outputText id="outputText491" 
                                        value="#{bundleAnalysis['recode.instruction.line1']}"/>
                                    <br />
                                </h:panelGroup>
                                
                                <h:panelGroup id="groupPanelRecodeInstruction2" 
                                    binding="#{AnalysisPage.groupPanelRecodeInstruction2}"
                                    rendered="false"
                                    layout="block">
                                    <h:graphicImage 
                                        alt="Information" 
                                        title="Information" 
                                        styleClass="vdcNoBorders" 
                                        style="vertical-align: bottom" 
                                        value="/resources/icon_info.gif" />
                                    <h:outputText id="outputText492" 
                                        value="#{bundleAnalysis['recode.instruction.line2']}"/>
                                    <br />
                                    <h:outputText id="outputText493" 
                                        value="#{bundleAnalysis['recode.instruction.line3']}"/>
                                    <h:panelGroup id="requiredfield" layout="block">
                                    <h:outputText id="rfb" escape="false" value="(" />
                                    <h:graphicImage value="/resources/icon_required.gif" />
                                    <h:outputText id="rfa" style="vdcHelpText"
                                        value="indicates a required field.)" />
                                    </h:panelGroup>
                                </h:panelGroup>
                                <br />
                                <!-- recoding working area -->
                                <h:panelGroup id="groupPanelRecodeTableArea" 
                                    binding="#{AnalysisPage.groupPanelRecodeTableArea}"
                                    rendered="false"
                                    block="true" >
                                    <!-- input boxes for variable name/label -->
                                    <h:panelGrid id="groupPanel15" 
                                        cellpadding="0" 
                                        cellspacing="10px" 
                                        columns="2" >
    
                                        <h:panelGroup id="groupPanelRecodeNewVarInfo" 
                                                binding="#{AnalysisPage.groupPanelRecodeNewVarInfo}" >
                                            <h:panelGroup>
                                            <h:outputText id="currentConditionVar" 
                                                 value="#{bundleAnalysis['recode.currentConditionVar']}"/>
                                                
                                            </h:panelGroup>
                                            <br/>
                                            <h:panelGroup>    
                                            <h:outputText id="recodeNewVarName" 
                                                 value="#{bundleAnalysis['recode.newVar.name']}"/>
                                                <h:graphicImage  value="/resources/icon_required.gif"/>
                                            </h:panelGroup>
                                            <br/>
                                            <h:panelGroup>
                                            <h:outputText id="recodeNewVarLabel"
                                                value="#{bundleAnalysis['recode.newVar.label']}"/>
                                                
                                            </h:panelGroup>
                                            
                                        </h:panelGroup>
                                        <h:panelGroup>
                                            <h:outputText id="currentRecodeVariableName"
                                                value="#{AnalysisPage.currentRecodeVariableName}"
                                                />
                                            <br/>
                                            <h:inputText id="recodeTargetVarName"
                                                         binding="#{AnalysisPage.recodeTargetVarName}"
                                            />
                                            <br />
                                            <h:inputText id="recodeTargetVarLabel"
                                                binding="#{AnalysisPage.recodeTargetVarLabel}"
                                                size="40"/>                                            
                                        </h:panelGroup>
                                    </h:panelGrid>
                                    <!-- end of variable name/label boxes-->
                                    <!-- help pane -->
                                    <ui:panelGroup id="recodeTableHelpPanel" 
                                                   binding="#{AnalysisPage.groupPanelRecodeTableHelp}" 
                                                   >
                                        
                                        <h:graphicImage 
                                            alt="Information" 
                                            title="Information" 
                                            styleClass="vdcNoBorders" 
                                            style="vertical-align: bottom" 
                                            value="/resources/icon_info.gif" />
                                        
                                        <h:outputText id="recodeHowToRecodeInstruction" 
                                                      value="#{bundleAnalysis['recode.howToRecode.instruction']}"/>
                                        

                                        
                                        <h:graphicImage 
                                            alt="Click to show the how-to information" 
                                            title="Click to show/hide how-to information" 
                                            styleClass="vdcNoBorders" 
                                            style="vertical-align: bottom" 
                                            value="/resources/icon_help.gif" onclick="return shwNshwTxt('recodeDetail');"/>
                                            
                                            
                                        <div id="recodeDetail" style="display:none">
                                            <br/>
                                            <h:outputText id="recodeHowToRecodeDetails"
                                                          value="#{bundleAnalysis['recode.howToRecode.details']}"/>
                                        </div>
                                        <br/>
                                        
                                        <h:graphicImage 
                                            alt="Information" 
                                            title="Information" 
                                            styleClass="vdcNoBorders" 
                                            style="vertical-align: bottom" 
                                            value="/resources/icon_info.gif" />
                                        
                                        
                                        <h:outputText id="recodeHowToSubsetInstruction" 
                                                      value="#{bundleAnalysis['recode.howToSubset.instruction']}"/>
                                        
                                        <h:graphicImage 
                                            alt="Click to show the how-to information" 
                                            title="Click to show/hide how-to information" 
                                            styleClass="vdcNoBorders" 
                                            style="vertical-align: bottom" 
                                            value="/resources/icon_help.gif" onclick="return shwNshwTxt('subsetDetail');"/>
                                        
                                        <div id="subsetDetail" style="display:none">
                                            <br/>
                                            <h:outputText id="recodeHowToSubsetDetails"
                                                          value="#{bundleAnalysis['recode.howToSubset.details']}"/>
                                        </div>
                                        <br/>
                                        
                                        <h:graphicImage 
                                            alt="Information" 
                                            title="Information" 
                                            styleClass="vdcNoBorders" 
                                            style="vertical-align: bottom" 
                                            value="/resources/icon_info.gif" />
                                        
                                        
                                        
                                        <h:outputText id="recodeHowToEnterConditionInstruction" 
                                                      value="#{bundleAnalysis['recode.howToEnterCondition.instruction']}"/>
                                        
                                        
                                        <h:graphicImage 
                                            alt="Click to show the how-to information" 
                                            title="Click to show/hide how-to information" 
                                            styleClass="vdcNoBorders" 
                                            style="vertical-align: bottom" 
                                            value="/resources/icon_help.gif" onclick="return shwNshwTxt('conditionDetail');"/>
                                        
                                        <div id="conditionDetail" style="display:none">
                                            <br/>
                                            <h:outputText id="recodeHowToEnterConditionDetails"
                                                          value="#{bundleAnalysis['recode.howToEnterCondition.details']}"/>
                                        </div>
                                        <br/>
                                        
                                        
                                    </ui:panelGroup>
                                    <!-- end of help panel -->
                                    <!-- recoding table panel -->
                                    <h:dataTable id="recodeTable" 
                                        binding="#{AnalysisPage.recodeTable}"
                                        var="rd"
                                        value="#{AnalysisPage.recodeDataList}"
                                        rendered="false"
                                        cellpadding="0" cellspacing="0" columnClasses="vdcRecodesColA, vdcRecodesColB, vdcRecodesColB, vdcRecodesColB, vdcRecodesColB, vdcRecodesColB, vdcRecodesColB, vdcRecodesColB" 
                                        headerClass="vdcRecodesHeader" 
                                        style="margin-top: 20px" >
                                        
                                        <h:column id="recodeClDropx">
                                            <f:facet name="header">
                                                <h:outputText id="recodeHdrDrop" 
                                                    value="#{bundleAnalysis['recode.valueTable.drop.header']}"/>
                                            </f:facet>
                                            <h:selectBooleanCheckbox
                                                id="recodeDropValueCheckboxx"
                                                binding="#{AnalysisPage.recodeDropValueCheckboxx}" 
                                                value="#{rd[0]}" 
                                                title="#{bundleAnalysis['recode.valueTable.drop.checkbox']}"
                                                immediate="true"/>   
                                            
                                        </h:column>
                                        <h:column id="recodeClVariable">
                                            <f:facet name="header">
                                                <h:outputText id="recodeHdrVariable" 
                                                    value="#{AnalysisPage.currentRecodeVariableName}"/>
                                            </f:facet>
                                            
                                            <h:inputText  id="recodeVariableNameBox"
                                                value="#{rd[3]}" 
                                                size="10"/>
                                        </h:column> 
                                        <h:column id="recodeClValue">
                                            <f:facet name="header">
                                                <h:outputText id="recodeHdrValue" 
                                                    value="#{bundleAnalysis['recode.valueTable.value.header']}"/>
                                            </f:facet>
                                            
                                            <h:inputText  id="recodeValueBox" 
                                                          
                                                value="#{rd[1]}"
                                                size="10"/>
                                            
                                        </h:column>
                                        <h:column id="recodeClValueLabel">
                                            <f:facet name="header">
                                                <h:outputText id="recodeHdrValueLabel" 
                                                    value="#{bundleAnalysis['recode.valueTable.valuelabel.header']}"/>
                                            </f:facet>
                                            
                                            <h:inputText  id="recodeValueLabelBox"
                                                value="#{rd[2]}" 
                                                size="25"/>
                                            
                                        </h:column>

                                    </h:dataTable>
                                    <!-- end of recoding table -->
                                    <br />
                                    <h:commandButton id="addValueRangeBttn" 
                                        binding="#{AnalysisPage.addValueRangeBttn}"
                                        value="#{bundleAnalysis['recode.button.addValue']}"
                                        actionListener="#{AnalysisPage.addValueRange}"
                                        rendered ="false"/>
                                    
                                    <ui:panelGroup id="groupPanel18" 
                                        block="true" 
                                        style="padding-left: 300px; white-space: nowrap">
                                        
                                        <h:commandButton id="recodeBttn"
                                            binding="#{AnalysisPage.recodeButton}" 
                                            value="#{bundleAnalysis['recode.button.save']}"
                                            actionListener="#{AnalysisPage.saveRecodedVariable}"
                                            disabled="true" />
                                        
                                        <ui:staticText id="msgSaveRecodeBttn"
                                            binding="#{AnalysisPage.msgSaveRecodeBttn}" 
                                            rendered="false"
                                            escape="false"
                                            styleClass="errorMessage"/>
                                        
                                        
                                    </ui:panelGroup>
                                    
                                </h:panelGroup><!-- recodeTableArea -->

                            </ui:panelGroup><!--end 2nd column  -->
                        </h:panelGrid>
                    </ui:panelLayout>
                </ui:tab>
                
                
                <!-- 3. EDA -->
                <ui:tab id="tabEda" 
                    text="Descriptive Statistics" 
                    actionListener="#{AnalysisPage.resetVariableInLBox}"
                    toolTip="Calculate Numeric and Graphic Summaries">
                    <ui:panelLayout id="layoutPanel4" 
                        panelLayout="flow" 
                        style="width: 100%;">
                        
                        <h:panelGrid id="gridPanel2" 
                            cellpadding="0" cellspacing="0" columnClasses="vdcAnalysisCol1, vdcAnalysisCol2" columns="2" width="100%">
                            
                            <ui:panelGroup id="groupPanel3" separator="&lt;br /&gt;">
                                <!-- ui tag solution -->
                                <ui:listbox id="mListboxEda" 
                                    items="#{AnalysisPage.varSetAdvStat}" 
                                    rows="10" 
                                    labelLevel="3" 
                                    disabled="true"
                                    monospace="true"  
                                    multiple="true" 
                                    label="#{bundleAnalysis['eda.selectedVarBox.title']}"
                                    labelOnTop="true"/>
                                <ui:helpInline id="helpInline3"
                                    text="#{bundleAnalysis['eda.selectedvarBox.bttmHelpText']}" type="field" rendered="false" />
                                
                            </ui:panelGroup>
                            
                            
                            <ui:panelGroup block="true" id="groupPanel4">
                                <h:graphicImage 
                                    alt="Information" 
                                    title="Information" 
                                    styleClass="vdcNoBorders" 
                                    style="vertical-align: bottom" 
                                    value="/resources/icon_info.gif" />
                                
                                <h:outputText id="outputText21" value="#{bundleAnalysis['eda.instruction']}"/>
                                
                                <h:selectManyCheckbox binding="#{AnalysisPage.edaOptionSet}" id="analysis" layout="pageDirection">
                                    
                                    <f:selectItem  id="edaOptionNumeric" 
                                        itemLabel="#{bundleAnalysis['eda.option.numeric']}" 
                                        itemValue="#{AnalysisPage.edaOptionItems[0]}"/>
                                    <f:selectItem  id="edaOptionGraphic" 
                                        itemLabel="#{bundleAnalysis['eda.option.graphic']}"  
                                        itemValue="#{AnalysisPage.edaOptionItems[1]}"/>
                                    
                                </h:selectManyCheckbox>
                                <ui:staticText  id="msgEdaButton"
                                    binding="#{AnalysisPage.msgEdaButton}" 
                                    visible="false" escape="false" 
                                    styleClass="errorMessage" 
                                    text="#{AnalysisPage.msgEdaButtonTxt}"/>
                                                <br />
                                <h:commandButton  id="edaBttn"
                                    binding="#{AnalysisPage.edaButton}" 
                                    disabled="true" 
                                    value="#{bundleAnalysis['eda.button.submit']}" 
                                    action="#{AnalysisPage.edaAction}"/>
                                
                            </ui:panelGroup>
                            
                        </h:panelGrid>
                        
                    </ui:panelLayout>
                </ui:tab>
                
                
                <!-- 5. advStat -->
                <ui:tab id="tabAdvStat" text="Advanced Statistical Analysis"
                    toolTip="Advanced Statistical Modeling">
                    <ui:panelLayout id="layoutPanel5" panelLayout="flow" style="width: 100%;">
                        
                        <h:panelGrid id="gridPanel5"
                                     cellpadding="0" 
                                     cellspacing="0" 
                                     columnClasses="vdcAnalysisCol1, vdcAnalysisCol2" 
                                     columns="2"  
                                     width="100%">
                            
                            <ui:panelGroup id="groupPanel6" separator="&lt;br /&gt;">
                                
                                <!-- ui tag solution -->

                                <ui:listbox id="listboxAdvStat" 
                                            items="#{AnalysisPage.varSetAdvStat}" 
                                            binding="#{AnalysisPage.listboxAdvStat}"
                                            selected="#{AnalysisPage.advStatSelectedVarLBox}"
                                            rows="10" 
                                            labelLevel="3" 
                                            monospace="true"  
                                            multiple="true" 
                                            label="#{bundleAnalysis['advStat.selectedVarBox.title']}"
                                            labelOnTop="true"/>
                                
                                <ui:helpInline id="helpInline1" 
                                               text="#{bundleAnalysis['advStat.selectedvarBox.bttmHelpText']}"
                                               type="field" rendered="false" />
                            </ui:panelGroup><!-- groupPanel6 -->
                            <!-- end of LHS -->
                            <!-- RHS section of the tab -->
                            <ui:panelGroup id="groupPanel7" block="true" >
                                
                                <!-- model menu list -->

                                <h:panelGrid id="gridPanel8out"
                                             cellpadding="0" cellspacing="0" 
                                             columnClasses="vdcAdvAnalysisCol1, vdcAdvAnalysisCol2" 
                                             columns="1" >
                                    
                                    <h:panelGrid id="gridPanel8above"
                                                 cellpadding="0" 
                                                 cellspacing="0" 
                                                 columnClasses="vdcAdvAnalysisCol1, vdcAdvAnalysisCol2" 
                                                 columns="1" >
                                        
                                        <ui:dropDown id="dropDown1"
                                                     binding="#{AnalysisPage.dropDown1}"  
                                                     items="#{AnalysisApplicationBean.modelMenuOptions}"
                                                     onChange="common_timeoutSubmitForm(this.form, '#{AnalysisPage.dropDown1ClientId}');" 
                                                     valueChangeListener="#{AnalysisPage.dropDown1_processValueChange}"
                                                     submitForm="false"
                                                     immediate="true" />
                                        
                                    <h:panelGrid id="modelInfoBox"
                                                 binding="#{AnalysisPage.gridPanelModelInfoBox}"
                                                 cellpadding="0" 
                                                 cellspacing="0" 
                                                 columns="2" 
                                                 rendered="#{AnalysisPage.gridPanelModelInfoBoxRendered}" >

                                        <h:graphicImage 
                                            alt="Information" 
                                            title="Information" 
                                            styleClass="vdcNoBorders" 
                                            style="vertical-align: bottom" 
                                            value="/resources/icon_info.gif" />
                                            
                                            <h:outputLink value="#{AnalysisPage.modelHelpLinkURL}"
                                            target="_blank">
                                            <h:outputText value="#{bundleAnalysis['advStat.model.helpInfo.header']}"/>                       
                                            </h:outputLink>
                                    </h:panelGrid>
                                    </h:panelGrid><!-- end: gridPanel8above -->
                                    
                                    <ui:panelGroup id="groupPanel8below" 
                                                   binding="#{AnalysisPage.groupPanel8below}" 
                                                   rendered="#{AnalysisPage.groupPanel8belowRendered}">
                                        <h:panelGrid id="gridPanel8"
                                                     cellpadding="0" 
                                                     cellspacing="0" 
                                                     columnClasses="vdcAdvAnalysisCol1, vdcAdvAnalysisCol2" 
                                                     columns="2" >
                                            
                                            <!-- column of variable boxes-->
                                            <ui:panelGroup id="groupPanel8" 

                                                           separator="&lt;br /&gt;">
                                                
                                                <!-- 1st box panel: usually dependent -->
                                                <ui:panelGroup id="groupPanel12"
                                                               binding="#{AnalysisPage.groupPanel12}" 
                                                               rendered="true" >
                                                    

                                                    <h:panelGrid columns="2" id="gridPanel12">
                                                        
                                                        <h:panelGrid columns="1" id="gridPanel12in">
                                                            
                                                            <h:commandButton id="moveVar1Bttn"
                                                                             binding="#{AnalysisPage.button4}"
                                                                             actionListener="#{AnalysisPage.addVarBoxR1}"
                                                                             value=" &gt; "/>
                                                            <ui:staticText id="msgMoveVar1Bttn"
                                                                           binding="#{AnalysisPage.msgMoveVar1Bttn}" 
                                                                           rendered="false"
                                                                           escape="false"
                                                                           styleClass="errorMessage"/>
                                                            <h:commandButton id="moveVar1Bttnb"
                                                                             binding="#{AnalysisPage.button4b}"
                                                                             actionListener="#{AnalysisPage.removeVarBoxR1}"
                                                                             value=" &lt; "/>
                                                            
                                                        </h:panelGrid>
                                                        <h:panelGrid columns="1" id="gridPanel12r">

                                                            <ui:label id="varListbox1Lbl" 
                                                                      binding="#{AnalysisPage.varListbox1Lbl}"
                                                                      for="moveVarListbox1"
                                                                      labelLevel="3" 
                                                                      text="1st box"/>
                                                            
                                                            <ui:listbox id="moveVarListbox1"
                                                                        binding="#{AnalysisPage.advStatVarListboxR1}" 
                                                                        items="#{AnalysisPage.advStatVarRBox1}"
                                                                        selected="#{AnalysisPage.advStatSelectedVarRBox1}"
                                                                        multiple="true"
                                                                        monospace="true"
                                                                        separators="false"
                                                                        immediate="true"

                                                                        rows="3" />
                                                            <!-- label="#{bundleAnalysis['advStat.modelVarbox.dependent.label']}"-->
                                                        </h:panelGrid>
                                                    </h:panelGrid>
                                                </ui:panelGroup><!-- groupPanel12 -->

                                                <!-- 2nd box panel: usually independent -->
                                                <ui:panelGroup id="groupPanel13"

                                                               binding="#{AnalysisPage.groupPanel13}" 
                                                               rendered="false">
                                                    
                                                    <h:panelGrid columns="2" id="gridPanel13">
                                                        
                                                        <h:panelGrid columns="1" id="gridPanel13in">
                                                            
                                                            <h:commandButton id="moveVar2Bttn"
                                                                             binding="#{AnalysisPage.button5}" 
                                                                             actionListener="#{AnalysisPage.addVarBoxR2}"
                                                                             value=" &gt; "/>
                                                            
                                                            <ui:staticText id="msgMoveVar2Bttn"
                                                                           binding="#{AnalysisPage.msgMoveVar2Bttn}" 
                                                                           rendered="false"
                                                                           escape="false"
                                                                           styleClass="errorMessage"/>
                                                            
                                                            <h:commandButton id="moveVar2Bttnb"
                                                                             binding="#{AnalysisPage.button5b}" 
                                                                             actionListener="#{AnalysisPage.removeVarBoxR2}"
                                                                             value=" &lt; "/>
                                                            
                                                        </h:panelGrid>
                                                        <h:panelGrid columns="1" id="gridPanel13r">
                                                            
                                                            <ui:label id="varListbox2Lbl" 
                                                                      binding="#{AnalysisPage.varListbox2Lbl}"
                                                                      for="moveVarListbox2"
                                                                      labelLevel="3" 
                                                                      text="2nd box"/>
                                                            
                                                            <ui:listbox id="moveVarListbox2"
                                                                        binding="#{AnalysisPage.advStatVarListboxR2}" 
                                                                        items="#{AnalysisPage.advStatVarRBox2}"
                                                                        selected="#{AnalysisPage.advStatSelectedVarRBox2}"
                                                                        labelLevel="3" 
                                                                        multiple="true"
                                                                        monospace="true"
                                                                        immediate="true"

                                                                        rows="3"/>
                                                            <!-- label="#{bundleAnalysis['advStat.modelVarbox.explanatory.label']}"-->
                                                        </h:panelGrid>
                                                    </h:panelGrid>
                                                </ui:panelGroup><!-- groupPanel13 -->

                                                <!-- 3rd box panel: usually for models of factor anlysis-->

                                                <ui:panelGroup id="groupPanel14"
                                                               binding="#{AnalysisPage.groupPanel14}"  
                                                               rendered="false">
                                                    
                                                    <h:panelGrid columns="2" id="gridPanel14">
                                                        
                                                        <h:panelGrid columns="1" id="gridPanel14in">
                                                            
                                                            <h:commandButton id="moveVar3Bttn" 
                                                                             binding="#{AnalysisPage.button6}" 
                                                                             actionListener="#{AnalysisPage.addVarBoxR3}"
                                                                             value=" &gt; "/>
                                                            
                                                            <ui:staticText id="msgMoveVar3Bttn"
                                                                           binding="#{AnalysisPage.msgMoveVar3Bttn}" 
                                                                           rendered="false"
                                                                           escape="false"
                                                                           styleClass="errorMessage"/>
                                                            
                                                            <h:commandButton id="moveVar3Bttnb" 
                                                                             binding="#{AnalysisPage.button6b}" 
                                                                             actionListener="#{AnalysisPage.removeVarBoxR3}"
                                                                             value=" &lt; "/>
                                                            
                                                        </h:panelGrid>
                                                        <h:panelGrid columns="1" id="gridPanel14r">
                                                            
                                                            
                                                            <ui:label id="varListbox3Lbl" 
                                                                      binding="#{AnalysisPage.varListbox3Lbl}"
                                                                      for="moveVarListbox3"
                                                                      labelLevel="3" 
                                                                      text="3rd box"/>
                                                            
                                                            <ui:listbox id="moveVarListbox3"
                                                                        binding="#{AnalysisPage.advStatVarListboxR3}" 
                                                                        items="#{AnalysisPage.advStatVarRBox3}" 
                                                                        selected="#{AnalysisPage.advStatSelectedVarRBox3}"
                                                                        labelLevel="3"
                                                                        multiple="true"
                                                                        monospace="true"
                                                                        immediate="true"

                                                                        rows="3"/>
                                                            <!-- label="#{bundleAnalysis['advStat.modelVarbox.observed.label']}"-->
                                                        </h:panelGrid>
                                                    </h:panelGrid>
                                                </ui:panelGroup><!-- groupPanel14 -->

                                                <!-- end of column of var boxes -->
                                            </ui:panelGroup><!-- groupPanel8 -->

                                            <!-- column of option panes -->
                                            <ui:panelGroup id="groupPanel9"
                                                           separator="&lt;br /&gt;">
                                                
                                                <!-- output-opton block -->

                                                <ui:panelGroup id="groupPanel25" 
                                                               block="true"
                                                               style="border: 1px groove #999999; padding: 2px; ">
                                                    
                                                    <!-- Output-option block: checkbox set -->
                                                    <h:outputText id="outputText45" value="Output Options"/>
                                                    <!-- ui:checkboxGroup id="checkboxGroup2"
                                                                      binding="{AnalysisPage.checkboxGroup2}" 
                                                                      items="{AnalysisPage.checkboxGroup2DefaultOptions.options}"
                                                                      labelLevel="3" 
                                                                      selected="{AnalysisPage.checkboxGroup2DefaultOptions.selectedValue}"
                                                                      rendered="true"
                                                    / -->
                                                    
                                                    <h:selectManyCheckbox 
                                                            id="chkbxAdvStatOutputOpt" 
                                                            binding="#{AnalysisPage.chkbxAdvStatOutputOpt}"
                                                            
                                                            layout="pageDirection"
                                                            rendered="true"
                                                            >
                                                            <f:selectItems value="#{AnalysisPage.chkbxAdvStatOutputOptMap}"/>
                                                    </h:selectManyCheckbox>
                                                    
                                                    
                                                    <!-- ui:checkboxGroup id="checkboxGroupXtb"
                                                                      binding="{AnalysisPage.checkboxGroupXtb}" 
                                                                      items="{AnalysisPage.checkboxGroupXtbOptions.options}"
                                                                      labelLevel="3" 
                                                                      selected="{AnalysisPage.checkboxGroupXtbOptions.selectedValue}"
                                                                      rendered="false"
                                                                      valueChangeListener="{AnalysisPage.checkboxGroupXtbProcessValueChange}"
                                                    / -->
                                                    <h:selectManyCheckbox 
                                                            id="chkbxAdvStatOutputXtbOpt" 
                                                            binding="#{AnalysisPage.chkbxAdvStatOutputXtbOpt}"
                                                            
                                                            layout="pageDirection"
                                                            rendered="false"
                                                            >
                                                            <f:selectItems value="#{AnalysisPage.chkbxAdvStatOutputXtbOptMap}"/>
                                                    </h:selectManyCheckbox>
                                                </ui:panelGroup><!-- groupPanel25 -->

                                                <!-- Analysis option block: complex -->

                                                <ui:panelGroup id="analysisOptionPanel"
                                                               binding="#{AnalysisPage.analysisOptionPanel}" 
                                                               block="true" 
                                                               separator="&lt;br /&gt;" 
                                                               style="border: 1px groove #999999; padding: 2px; ">
                                                    
                                                    
                                                    <h:outputLabel 
                                                          id="outputText46" 
                                                          for="groupPanelSetxOption"
                                                          value="Analysis Options"/>
                                                    
                                                    <ui:panelGroup id="groupPanelSetxOption"
                                                                   binding="#{AnalysisPage.setxOptionPanel}" 
                                                                   separator="&lt;br /&gt;"
                                                                   rendered="true"
                                                                   block="true" >
                                                        <!-- simulation: checkbox -->
                                                        <ui:checkbox id="checkbox3" 
                                                                     binding="#{AnalysisPage.checkbox3}" 
                                                                     immediate="true"
                                                                     valueChangeListener="#{AnalysisPage.showHideSimulationsOptPanel}"
                                                                     onClick="submit()"
                                                                     label="Simulations" />
                                                        

                                                        <h:panelGroup
                                                                id="groupPanelSimTypeChoice" 
                                                                binding="#{AnalysisPage.groupPanelSimTypeChoice}"
                                                                rendered="false"
                                                                layout="block"
                                                                style="border: 1px groove rgb(153, 153, 153); padding: 2px; "
                                                                >
                                                            <!-- contional simulation: radio selection -->

                                                            <h:outputLabel 
                                                                id="outputTextSimchoice" 
                                                                for="radioSimTypeChoice"
                                                                value="#{bundleAnalysis['advStat.simulation.optionPanel']}"/>
                                                            

                                                            <h:selectOneRadio
                                                                id="radioSimTypeChoice"
                                                                binding="#{AnalysisPage.radioSimTypeChoice}"
                                                                valueChangeListener="#{AnalysisPage.showHideSimCndtnOptPanel}"
                                                                value="#{AnalysisPage.radioSimTypeChoiceSelected}"
                                                                immediate="true"
                                                                layout="pageDirection"
                                                                onclick="this.form.submit();"
                                                                >
                                                                  <f:selectItems  value="#{AnalysisPage.simOptionMap}" />
                                                            </h:selectOneRadio>
                                                            

                                                            <!-- gui when the option of select values is chosen-->

                                                            <h:panelGroup 
                                                                    id="groupPanelSimNonDefault"
                                                                    binding="#{AnalysisPage.groupPanelSimNonDefault}"
                                                                    layout="block"
                                                                    style="border: 1px groove rgb(153, 153, 153); margin: 2px; padding: 2px; "
                                                                    rendered="false"
                                                                >
                                                                <!--1st option: Explnatory variable values -->


                                                                <h:outputLabel 
                                                                  id="setxlabel1"
                                                                  for="gridPanel11"
                                                                  value="#{bundleAnalysis['advStat.simulation.setValues.values']}"
                                                                  />
                                                                <h:panelGrid id="gridPanel11" 
                                                                             binding="#{AnalysisPage.gridPanel11}" columns="3">
                                                                    
                                                                    <ui:dropDown id="dropDown2"
                                                                                 binding="#{AnalysisPage.dropDown2}"  
                                                                                 items="#{AnalysisPage.setxDiffVarBox1}"
                                                                                 submitForm="false"
                                                                                 immediate="true" />
                                                                                 
                                                                    <ui:staticText id="staticText2" text=" = "/>
                                                                    <ui:textField binding="#{AnalysisPage.textField10}" columns="10" id="textField10"/>
                                                                </h:panelGrid>
                                                                
                                                                <!-- 2nd option: Value for the first difference(optional)-->


                                                                <h:outputText 
                                                                    id="setxlabel2"
                                                                    for="gridPanel10"
                                                                    value="#{bundleAnalysis['advStat.simulation.setValues.diff']}"
                                                                    />
                                                                <h:panelGrid id="gridPanel10"
                                                                             binding="#{AnalysisPage.gridPanel10}" columns="3" >
                                                                    
                                                                    <ui:dropDown id="dropDown3" 
                                                                                 binding="#{AnalysisPage.dropDown3}" 
                                                                                 items="#{AnalysisPage.setxDiffVarBox2}"
                                                                                 submitForm="false"
                                                                                 immediate="true" />

                                                                    <ui:staticText id="staticText1" text=" = "/>
                                                                    
                                                                    <ui:textField id="textField8" binding="#{AnalysisPage.textField8}" columns="10"/>
                                                                </h:panelGrid>
                                                            </h:panelGroup><!-- groupPanelSimNonDefault -->
                                                            <!-- /ui:panelGroup --><!-- groupPanel22 -->
                                                        </h:panelGroup><!-- groupPaneSimChoice -->
                                                        <!-- /ui:panelGroup --><!--groupPanel20 -->

                                                        <!-- checkbox: Sensitivity analysis -->
                                                        <!--groupPanel23 -->
                                                    </ui:panelGroup>
                                                    <!-- checkbox: Missing Values -->
                                                    
                                                </ui:panelGroup><!-- analysisOptionPanel-->


                                                <ui:staticText  id="msgAdvStatButton" 
                                                binding="#{AnalysisPage.msgAdvStatButton}" 
                                                visible="false" escape="false" styleClass="errorMessage" 
                                                 text="#{AnalysisPage.msgAdvStatButtonTxt}"/>
                                                <br />


                                                <!-- action="#{AnalysisPage.advStatAction}" -->
                                                <h:commandButton id="advStatBttn" 
                                                                 disabled="true" 
                                                                 binding="#{AnalysisPage.advStatButton}" 
                                                                 value="#{bundleAnalysis['advStat.button.submit']}"
                                                                 action="#{AnalysisPage.advStatAction}"
                                                                 
                                                                 />
                                                
                                                <!-- end of  column of opion panes-->
                                            </ui:panelGroup><!-- groupPanel9: option panes -->

                                        </h:panelGrid><!-- gridPanel8: variable boxes and option panes -->
                                    </ui:panelGroup><!-- groupPanel8below -->
                                </h:panelGrid><!-- gridPanel8out-->
                            </ui:panelGroup><!-- groupPanel7--><!-- end of the RHS of the tab -->

                        </h:panelGrid><!-- gridPanel5 -->
                    </ui:panelLayout><!-- layoutPanel5 -->
                </ui:tab>
            </ui:tabSet>  
            
            
            <!-- end of tab block -->
        
        
            <h:panelGrid  id="pgSubsettingInstruction"
                          cellpadding="0" cellspacing="0"
                          columns="1" 
                          style="margin-left: 8px; margin-top: 10px" width="98%">
                
                <ui:panelGroup id="wrapSubsettingInstruction"
                    rendered="#{AnalysisPage.wrapSubsettingInstructionRendered}"
                >
                    <h:graphicImage 
                        alt="Information" 
                        title="Information" 
                        styleClass="vdcNoBorders" 
                        style="vertical-align: bottom" 
                        value="/resources/icon_info.gif" />
                    
                    <h:outputText id="txtSubsettingInstruction" 
                                  binding="#{AnalysisPage.txtSubsettingInstruction}" 
                                  value="#{bundleAnalysis['subsettingInstruction']}" />
                </ui:panelGroup>
               <ui:panelGroup 
                    id="wrapNonSubsettingInstruction"
                    block="true" 
                    styleClass="successMessage"
                    rendered="#{AnalysisPage.wrapNonSubsettingInstructionRendered}">
                    <h:outputText id="txtNonSubsettingInstruction"
                                 binding="#{AnalysisPage.txtNonSubsettingInstruction}"
                                 value="#{bundleAnalysis['nonSubsettingInstruction']}" />
               </ui:panelGroup>                 
                
                <ui:staticText id="msgVariableSelection"
                               binding="#{AnalysisPage.msgVariableSelection}" 
                               rendered="false"
                               escape="false"
                               styleClass="errorMessage"/>
            </h:panelGrid>
            
            
            <!-- search box -->
        
            <h:panelGrid  cellpadding="0" cellspacing="0"
                          columnClasses="vdcVarHeaderCol1, vdcVarHeaderCol1, vdcVarHeaderCol2" 
                          columns="3" id="gridPanel9"
                          style="margin-left: 8px; margin-top: 10px" width="100%">
                <!--column 1 -->
                <ui:panelGroup id="groupPanel16">
                    
                    <h:outputText id="outputText47" rendered="false" styleClass="vdcSubHeaderColor" value="#{bundleAnalysis['varSearch.displaylabel.initial']}"/>
                    
                    <h:outputText escape="false" id="outputText3" styleClass="vdcSubHeaderColor" value="#{bundleAnalysis['varSearch.displaylabel.afterVarSearch']}" rendered="false" />
                    
                    <h:outputText id="outputText2" rendered="false" value="#{bundleAnalysis['varSearch.displaylabel.afterSelected']}"/>
                    
                    
                    <h:commandLink id="linkAction3" rendered="false">
                        <h:outputText id="linkAction3Text" value="#{bundleAnalysis['varSearch.displayButton.inital']}"/>
                    </h:commandLink>
                    
                    <h:commandLink  id="linkAction4" rendered="false">
                        <h:outputText id="linkAction4Text" value="#{bundleAnalysis['varSearch.displayButton.afterSearch']}"/>
                    </h:commandLink>
                    
                    <h:commandLink id="linkAction5" rendered="false">
                        <h:outputText id="linkAction5Text" value="#{bundleAnalysis['varSearch.displayButton.afterSelected']}"/>
                    </h:commandLink>
                    
                    
                </ui:panelGroup>
                <!-- column 2-->
                <ui:panelGroup id="groupPanel19">
                    <h:inputText binding="#{AnalysisPage.textField4}" id="textField4" rendered="false"/>
                    <h:commandButton id="button8" value="#{bundleAnalysis['varSearch.button.search']}" rendered="false"/>
                </ui:panelGroup>
                
                
                <!-- column 3-->
                <ui:panelGroup id="groupPanel17">
                    <ui:dropDown id="howManyRows"
                                 binding="#{AnalysisPage.howManyRows}"  
                                 items="#{AnalysisPage.howManyRowsOptions.options}"
                                 selected="#{AnalysisPage.howManyRowsOptions.selectedValue}"
                                 onChange="common_timeoutSubmitForm(this.form, '#{AnalysisPage.howManyRowsClientId}');" 
                                 valueChangeListener="#{AnalysisPage.howManyRows_processValueChange}"
                                 submitForm="false"
                                 label="Show "
                                 labelLevel="3"
                                 disabled="false"
                                 immediate="true" />
                    
                    
                </ui:panelGroup>
                
            </h:panelGrid>
            
            
            <!-- data table -->
            <h:panelGrid id="pgDataTable" style="width:100%">
                
                <h:dataTable id="dataTable1" 
                             binding="#{AnalysisPage.data}"
                             var="currentRow" 
                             value="#{AnalysisPage.dt4Display}" 
                             rows="20"
                             cellpadding="0"
                             cellspacing="0"
                             columnClasses="vdcVariablesCol1, vdcVariablesCol2, vdcVariablesCol3, vdcVariablesCol4, vdcVariablesCol5, vdcVariablesCol6"
                             style="margin: 10px 4px 10px 8px; "
                             title="Variable Information"
                             rowClasses="list-row-odd, list-row-even"  
                             width="98%">
                    
                    <h:column id="column1">
                        <f:facet name="header">
                            <!-- h:outputText id="outputText4" value="#{bundleAnalysis['varTable.colHdr.checkBox']}"/ -->
                            <ui:checkbox id="checkboxSelectUnselectAll"
                                         binding="#{AnalysisPage.checkboxSelectUnselectAll}"
                                         selected="#{AnalysisPage.checkboxSelectUnselectAllSelected}"
                                         rendered="#{AnalysisPage.checkboxSelectUnselectAllRendered}"
                                         immediate="true"   
                                         valueChangeListener="#{AnalysisPage.selectUnselectAllCheckbox}" 
                                         onClick="submit();"
                                         toolTip="select or unselect all rows in this view"
                            />
                        </f:facet>
                        <!--
                        <ui:checkbox id="varCheckbox"
                                     binding="#{AnalysisPage.varCheckbox}"
                                     immediate="true"   
                                     selected="#{currentRow[0]}" 
                                     name="varbl_#{currentRow[2]}"
                                     valueChangeListener="#{AnalysisPage.updateCheckBoxState}" 
                                     onClick="submit();" >
                        </ui:checkbox> 
                        -->
                        
                        <h:selectBooleanCheckbox id="varCheckboxx"
                                     binding="#{AnalysisPage.varCheckboxx}" 
                                     value="#{currentRow[0]}" 
                                     title="#{currentRow[3]}"
                                     valueChangeListener="#{AnalysisPage.updateCheckBoxState}" 
                                     onclick="this.form.submit();"
                        />                        
                        
                        
                        
                    </h:column>
                    <h:column id="column2">
                        <f:facet name="header">
                            <h:outputText id="outputText6" value="#{bundleAnalysis['varTable.colHdr.varType']}"/>
                        </f:facet>
                        <h:outputText id="outputText5" value="#{currentRow[1]}"/>
                    </h:column>
                    <h:column id="column3">
                        <f:facet name="header"><!---->
                            <h:outputText  id="outputText8" value="#{bundleAnalysis['varTable.colHdr.Id']}" style="visibility:hidden;"/>
                            
                        </f:facet><!---->
                        <h:outputText id="outputText7" value="#{currentRow[2]}" style="visibility:hidden;"/>
                    </h:column>
                    <h:column id="column4">
                        <f:facet name="header">
                            <h:outputText  id="outputText10" value="#{bundleAnalysis['varTable.colHdr.varName']}"/>
                        </f:facet>
                        <h:outputText id="outputText9" value="#{currentRow[3]}"/>
                    </h:column>
                    <h:column id="column5">
                        <f:facet name="header">
                            <h:outputText  id="outputText12" value="#{bundleAnalysis['varTable.colHdr.varLabel']}"/>
                        </f:facet>
                        <h:outputText id="outputText11" value="#{currentRow[4]}"/>
                    </h:column>
                    <h:column id="column6">
                        <f:facet name="header">
                            <h:outputText id="outputText14" value="#{bundleAnalysis['varTable.colHdr.quickSummary']}"/>
                        </f:facet>
                        
                        <h:commandButton 
                          id="showStatistics" 
                          onclick="QSrequested(this.id, '#{currentRow[2]}');return false;" 
                          alt="Click to show descriptive statistics"
                          title="Click to show/hide descriptive statistics"
                          image="/resources/icon_variables.gif"
                          type="submit" />
                        
                        <h:outputText escape="false" id="varSummaryTable" value="#{currentRow[5]}"  />
                        
                    </h:column>
                </h:dataTable>    
                
                <f:facet name="footer">
                    <ui:panelGroup block="true" id="pgFooterDataTable1">
                        <t:scroller navFacetOrientation="NORTH" for="dataTable1" actionListener="#{AnalysisPage.processScrollEvent}">
                            <f:facet name="next">
                                <h:panelGroup>
                                    <h:outputText value="Next"/>
                                    <h:graphicImage url="/resources/arrow-right.gif" styleClass="vdcNoBorders"/>
                                </h:panelGroup>
                            </f:facet>
                            
                            <f:facet name="previous">
                                <h:panelGroup>
                                    <h:outputText value="Previous"/>
                                    <h:graphicImage url="/resources/arrow-left.gif" styleClass="vdcNoBorders"/>
                                </h:panelGroup>
                            </f:facet>
                            
                            <f:facet name="number">
                            </f:facet>
                            
                            <f:facet name="current">
                            </f:facet>
                            
                        </t:scroller>
                    </ui:panelGroup>
                </f:facet>
                
            </h:panelGrid>
            <!---->
            <h:panelGrid id="belowDT">
                
                <h:panelGroup id="pgRecodedVarTable"
                    binding="#{AnalysisPage.pgRecodedVarTable}"
                    layout="block"
                    rendered="false">
                    
                    <h:outputText id="recodedVarTableTitle" 
                        binding="#{AnalysisPage.recodedVarTableTitle}"
                        value="#{bundleAnalysis['recodedVarTable.title']}"/>
                    <!--
<br />

<h:outputText id="recodedVarTableState" 
  binding="#{AnalysisPage.recodedVarTableState}"
  value="#{bundleAnalysis['recodedVarTable.state']}"/>
                -->
                    <h:dataTable id="recodedVarTable"
                        binding="#{AnalysisPage.recodedVarTable}" 
                        var="rcw"
                        value="#{AnalysisPage.recodedVarSet}"
                        headerClass="list-header"  
                        rowClasses="list-row-odd,list-row-even"
                        cellpadding="2px"
                        cellspacing="0"
                        columnClasses="vdcVariablesCol3, vdcVariablesCol4, vdcVariablesCol5, vdcVariablesCol2"
                        style="margin: 10px 4px 10px 8px;">
                        
                        <!-- chekcbox: remove --><!---->
                        <h:column id="RVTcolumn0">
                            <f:facet name="header">
                                <h:outputText id="RVThdr0" 
                                value="#{bundleAnalysis['recodedVarTable.colHdr.remove']}"/>
                            </f:facet>

                            
                            <h:commandLink id="removeRecodedVariable"
                                actionListener="#{AnalysisPage.removeRecodedVariable}"
                                title="#{bundleAnalysis['recodedVarTable.tooltip.remove']}"
                                immediate="true"
                                shape="rect" 
                                >
                                <h:outputText value="#{bundleAnalysis['recodedVarTable.hyperLink.title.remove']}"/>
                            </h:commandLink>
                            
                        </h:column>
                        
                        <!-- variable name --><!---->
                        <h:column id="RVTcolumn1">
                            <f:facet name="header">
                                <h:outputText id="RVThdr1" value="#{bundleAnalysis['recodedVarTable.colHdr.varName']}"/>
                            </f:facet>
                            <h:outputText id="RVTcolv1" value="#{rcw[0]}"/>
                        </h:column>
                        
                        <!-- variable label --><!---->
                        <h:column id="RVTcolumn2">
                            <f:facet name="header">
                                <h:outputText id="RVThdr2" value="#{bundleAnalysis['recodedVarTable.colHdr.varLabel']}"/>
                            </f:facet>
                            <h:outputText id="RVTcolv2" value="#{rcw[1]}"/>
                        </h:column>
                        
                        <!-- checkbox: modify --><!---->
                        <h:column id="RVTcolumn3">
                            <f:facet name="header">
                                <ui:staticText  id="RVThdr3" text="#{bundleAnalysis['recodedVarTable.colHdr.modify']}"  visible="false" />
                            </f:facet>
                            <!-- h:outputText id="RVTcolv3" value="#{rcw[3]}"/ -->
                            <ui:hyperlink id="RVTcolv3" 
                                actionListener="#{AnalysisPage.editRecodedVariable}"
                                immediate="true" 
                                text="#{bundleAnalysis['recodedVarTable.hyperLink.title.modify']}" 
                                toolTip="#{bundleAnalysis['recodedVarTable.tooltip.modify']}"
                                visible="false"
                            /> 
                            
                        </h:column>
                        <!-- variable Id  --><!---->
                        <h:column id="RVTcolumn4" >
                            <f:facet name="header">
                                <h:outputText id="RVThdr4" value=""/>
                            </f:facet>
                            <ui:staticText id="RVTcolv4" text="#{rcw[2]}" visible="false"/> 
                            <!-- h:outputText id="RVTcolv4" value="#{rcw[2]}" / -->
                        </h:column>
                        
                    </h:dataTable>
                    
                </h:panelGroup>
            </h:panelGrid>
            
            
            
        </div>
    </div>
        
    </h:form>
<!-- hr / -->

    <f:verbatim>   
    <script type="text/javascript">
      //<![CDATA[
  function testJs(){
    alert("js test");
  }
  
  function checkParameters(){
    alert("request is submitted");
  }

// for a checkbox
  function showHideMonitorBlock(id, shw){
    var trgt = document.getElementById(id);
    if (shw) {
      trgt.style.display="";
    } else {
      trgt.style.display="none";
    }
  }
 
// show-no show button with a state-dependent button label
function shwNshwLnk(id, srcId, label) {
  // id     whose block to be shown/hidden
  // srcId  button id
  // label  button lable
  var srcEl = document.getElementById(id);
  
  if (srcEl.style.display == "none") {
    srcEl.style.display = "";
    // this part depends on the design of a button
    document.getElementById(srcId).value="Hide " +label;
  } else if (srcEl.style.display == "") {
    srcEl.style.display = "none";
    // this part depends on the design of a button
    document.getElementById(srcId).value ="Show " +label;
  }
}


// show-no show button with a state-dependent button label
function shwNshwTxt(id) {
  // id     whose block to be shown/hidden
  // srcId  button id
  // label  button lable
  var srcEl = document.getElementById(id);
  if (srcEl.style.display == "none") {
    srcEl.style.display = "";
  } else if (srcEl.style.display == "") {
    srcEl.style.display = "none";
  }
}

       // ]]>    
  </script>
     </f:verbatim>  
<input type="button" id="monitorBttn" style="display:none" value="show monitor block" onclick="shwNshwLnk('monitorBlock', 'monitorBttn', 'monitor block');"/>
<div id="monitorBlock" style="display:none">
</div>


<!-- to be renamed to subview -->
</gui:define>
</gui:composition>
</body>

</html>

