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
package edu.harvard.iq.dvn.core.web.dataaccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.logging.Logger;
import java.util.List; 
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap; 

import edu.harvard.iq.dvn.core.study.StudyFile;
import edu.harvard.iq.dvn.core.study.TabularDataFile;
import edu.harvard.iq.dvn.core.study.DataVariable;
import edu.harvard.iq.dvn.core.study.VariableCategory;
import edu.harvard.iq.dvn.core.util.FileUtil;
import edu.harvard.iq.dvn.ingest.dsb.impl.*;



/**
 *
 * @author leonidandreev
 */
public class DataFileConverter {
    private static Logger dbgLog = Logger.getLogger(DataFileConverter.class.getPackage().getName());
    
    public DataFileConverter() {
    }
    
    public static FileAccessObject performFormatConversion (StudyFile file, FileAccessObject fileDownload, String formatRequested, String formatType) {
        
        File tabFile = null; 
        File formatConvertedFile = null;

        String cachedFileSystemLocation = null;

         // initialize the data variables list:

        List<DataVariable> dataVariables = ((TabularDataFile) file).getDataTable().getDataVariables();

        // if the format requested is "D00", and it's already a TAB file,
        // we don't need to do anything:
        if (formatRequested.equals("D00") &&
            file.getFileType().equals("text/tab-separated-values")) {
                
            return fileDownload;
        }

        if (file.isRemote()) {
            //TODO://tabFile = saveRemoteFile (file, fileDownload);
        } else {
            // If it's a local file we may already have a cached copy of this
            // format.

            cachedFileSystemLocation = file.getFileSystemLocation()
                + "."
                + formatRequested;


            if (new File(cachedFileSystemLocation).exists()) {
                formatConvertedFile = new File(cachedFileSystemLocation);
            } else {
                // OK, we don't have a cached copy. So we'll have to run
                // conversion again (below). Let's have the
                // tab-delimited file handy:

                tabFile = new File(file.getFileSystemLocation());
            }
        }
                        
        
        // Check if the tab file is present and run the conversion:

        if (tabFile != null && (tabFile.length() > 0)) {   
            formatConvertedFile = runFormatConversion (file, tabFile, formatRequested);

            // for local files, cache the result:

            if (!file.isRemote() &&
                    formatConvertedFile != null &&
                    formatConvertedFile.exists()) {

                try {
                    File cachedConvertedFile = new File (cachedFileSystemLocation);
                    FileUtil.copyFile(formatConvertedFile,cachedConvertedFile);
                } catch (IOException ex) {
                    // Whatever. For whatever reason we have failed to cache
                    // the format-converted copy of the file we just produced.
                    // But it's not fatal. So we just carry on.
                }
            }

        }

        // Now check the converted file: 
              
        if (formatConvertedFile != null && formatConvertedFile.exists()) {

            fileDownload.closeInputStream();
            fileDownload.setSize(formatConvertedFile.length());

            try {
                fileDownload.setInputStream(new FileInputStream(formatConvertedFile));
            } catch (IOException ex) {
                return null; 
            }

            fileDownload.releaseConnection();
            fileDownload.setHTTPMethod(null);
            fileDownload.setIsLocalFile(true);

            fileDownload.setMimeType(formatType);
            String dbFileName = file.getFileName();

            if (dbFileName == null || dbFileName.equals("")) {
                dbFileName = "f" + file.getId().toString();
            }

            fileDownload.setFileName(generateAltFileName(formatRequested, dbFileName));

            if (formatRequested.equals("D00") && (!fileDownload.noVarHeader())) {

                String varHeaderLine = null;
                List dataVariablesList = ((TabularDataFile) file).getDataTable().getDataVariables();
                //TODO://varHeaderLine = generateVariableHeader(dataVariablesList);
                fileDownload.setVarHeader(varHeaderLine);
            } else {
                fileDownload.setNoVarHeader(true);
                fileDownload.setVarHeader(null);
                // (otherwise, since this is a subsettable file, the variable header
                //  will be added to this R/Stata/etc. file -- which would
                //  totally screw things up!)
            }

            //TODO://setDownloadContentHeaders (fileDownload);

           
            return fileDownload; 
        }
        
        return null; 
    } // end of performformatconversion();

    // Method for (subsettable) file format conversion.
    // The method needs the subsettable file saved on disk as in the
    // TAB-delimited format.
    // Meaning, if this is a remote subsettable file, it needs to be downloaded
    // and stored locally as a temporary file; and if it's a fixed-field file, it
    // needs to be converted to TAB-delimited, before you can feed the file
    // to this method. (See performFormatConversion() method)
    // The method below takes the tab file and sends it to the R server
    // (possibly running on a remote host) and gets back the transformed copy,
    // providing error-checking and diagnostics in the process.
    // This is mostly Akio Sone's code.

    private static File runFormatConversion (StudyFile file, File tabFile, String formatRequested) {

        if ( formatRequested.equals ("D00") ) {
            // if the *requested* format is TAB-delimited, we don't
            // need to call R to do any conversions, we can just
            // send back the TAB file we have just produced.

            return tabFile;
        }

        List<DataVariable> dataVariables = ((TabularDataFile) file).getDataTable().getDataVariables();
        DvnRJobRequest sro = null;
        Map<String, List<String>> paramListToR = null;
        Map<String, Map<String, String>> vls = null;

        dbgLog.fine(" ***** remote: set-up block for format conversion cases *****");

        paramListToR = new HashMap<String, List<String>>();

        paramListToR.put("dtdwnld", Arrays.asList(formatRequested));
        paramListToR.put("requestType", Arrays.asList("Download"));

        //vls = getValueTablesForAllRequestedVariables();
        vls = getValueTableForRequestedVariables(dataVariables);
        dbgLog.fine("format conversion: variables(getDataVariableForRequest())="+dataVariables+"\n");
        dbgLog.fine("format conversion: variables(dataVariables)="+dataVariables+"\n");
        dbgLog.fine("format conversion: value table(vls)="+vls+"\n");

        Long tabFileSize = tabFile.length();
        paramListToR.put("subsetFileName", Arrays.asList(tabFile.getAbsolutePath()));
        paramListToR.put("subsetDataFileName",Arrays.asList(tabFile.getName()));

        File frmtCnvrtdFile = null;
        Map<String, String> resultInfo = new HashMap<String, String>();

        dbgLog.fine("local: paramListToR="+paramListToR);

        sro = new DvnRJobRequest(dataVariables, paramListToR, vls);

        // create the service instance
        DvnRforeignFileConversionServiceImpl dfcs = new DvnRforeignFileConversionServiceImpl();

        // execute the service
        resultInfo = dfcs.execute(sro);

        //resultInfo.put("offlineCitation", citation);
        dbgLog.fine("resultInfo="+resultInfo+"\n");

        // check whether a requested file is actually created

        if (resultInfo.get("RexecError").equals("true")){
            dbgLog.fine("R-runtime error trying to convert a file.");
            return  null;
        } else {
            String wbDataFileName = resultInfo.get("wbDataFileName");
            dbgLog.fine("wbDataFileName="+wbDataFileName);

            frmtCnvrtdFile = new File(wbDataFileName);

            if (frmtCnvrtdFile.exists()){
                dbgLog.fine("frmtCnvrtdFile:length="+frmtCnvrtdFile.length());
            } else {
                dbgLog.warning("Format-converted file was not properly created.");
                return null;
            }
        }

        return frmtCnvrtdFile;
    }

    private static Map<String, Map<String, String>> getValueTableForRequestedVariables(List<DataVariable> dvs){
        Map<String, Map<String, String>> vls = new LinkedHashMap<String, Map<String, String>>();
        for (DataVariable dv : dvs){
            List<VariableCategory> varCat = new ArrayList<VariableCategory>();
            varCat.addAll(dv.getCategories());
            Map<String, String> vl = new HashMap<String, String>();
            for (VariableCategory vc : varCat){
                if (vc.getLabel() != null){
                    vl.put(vc.getValue(), vc.getLabel());
                }
            }
            if (vl.size() > 0){
                vls.put("v"+dv.getId(), vl);
            }
        }
        return vls;
    }
    
    // This shouldn't be here, really - should be part of DataFileFormatType
    
    private static String generateAltFileName(String formatRequested, String xfileId) {
        String altFileName = xfileId;

        if ( altFileName == null || altFileName.equals("")) {
            altFileName = "Converted";
        }

        if ( formatRequested != null ) {
            if (formatRequested.equals("D00")) {
                altFileName = FileUtil.replaceExtension(altFileName, "tab");
            } else if ( formatRequested.equals("D02") ) {
                altFileName = FileUtil.replaceExtension(altFileName, "ssc");
            } else if ( formatRequested.equals("D03") ) {
                altFileName = FileUtil.replaceExtension(altFileName, "dta");
            } else if ( formatRequested.equals("D04") ) {
                altFileName = FileUtil.replaceExtension(altFileName, "RData");
            } else {
                altFileName = FileUtil.replaceExtension(altFileName, formatRequested);
            }
        }

        return altFileName;
    }
    
}
