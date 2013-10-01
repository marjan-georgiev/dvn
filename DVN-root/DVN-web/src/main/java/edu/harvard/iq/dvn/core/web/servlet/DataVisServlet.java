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
 * DataVisServlet.java
 *
 * Created on July 28, 2011, 2:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.web.servlet;


import edu.harvard.iq.dvn.core.util.FileUtil;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import java.io.OutputStream;
import java.io.PrintWriter;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.URL;
import java.net.URLDecoder;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.imageio.IIOException;

import java.util.logging.Logger;

/**
 *
 * @author landreev
 * uses Steve Kraffmiller's code for generating visualization graph images
 *
 */
public class DataVisServlet extends HttpServlet {

    /** Creates a new instance of DataVisServlet */
    public DataVisServlet() {
    }

    /** Sets the logger (use the package name) */
    private static Logger dbgLog = Logger.getLogger(DataVisServlet.class.getPackage().getName());


    private String graphTitle = "";
    private String yAxisLabel = "";
    private String sources = "";
    private String heightCode = "";
    private Integer heightInt = new Integer(0);


    public void service(HttpServletRequest req, HttpServletResponse res) {

        // Parameters:

        String googleImageURL = req.getParameter("googleImageUrl");
        graphTitle = req.getParameter("graphTitle");
        yAxisLabel = req.getParameter("yAxisLabel");
        sources = req.getParameter("sources");
        heightCode = req.getParameter("heightCode");
        heightInt = new Integer(heightCode);
        OutputStream out = null;

        try {
            System.out.println("googleImageURL " + googleImageURL);
            String decoded = URLDecoder.decode(googleImageURL, "UTF-8");
            System.out.println("decoded " + decoded);


            out = res.getOutputStream();

            if (!decoded.isEmpty()){
                URL imageURLnew = new URL(googleImageURL);
                System.out.println("googleImageURL " + googleImageURL);
                try{
                    BufferedImage image =     ImageIO.read(imageURLnew);
                    BufferedImage combinedImage = getCompositeImage(image);

                    if (combinedImage == null) {
                        res.setHeader("Content-Type", "text/plain");
                        //out = res.getWriter();
                        PrintWriter wout = new PrintWriter (out);
                        wout.println("Sorry, image could not be generated due to system instability; please try again later.");
                    } else {

                        // MIME type header:

                        res.setHeader("Content-Type", "image/png");
                        // image content:

                        ImageIO.write(combinedImage, "png", out);
                    }

                } catch (IIOException io){
                    // TODO:
                    // log diagnostic messages;
                    // provide more info in the error response;
                     System.out.println(io.getMessage().toString());
                     System.out.println(io.getCause().toString());
                     System.out.println("IIOException " + imageURLnew);
                    createErrorResponse404(res);
                } catch (FontFormatException ff){
                    System.out.println("FontFormatException " + imageURLnew);
                    
                    System.out.println("FontFormatException " + ff.toString());
                }
            }


        } catch (UnsupportedEncodingException uee){
            // TODO:
            // log diagnostic messages;
            // provide more info in the error response;
            createErrorResponse404(res);

        } catch (MalformedURLException mue){
            // TODO:
            // log diagnostic messages;
            // provide more info in the error response;
        } catch (IOException io){
            // TODO:
            // log diagnostic messages;
            // provide more info in the error response;
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException x) {}
        }

        return;
    } // end of the main service() method.


    // The 3 methods below, compositeImage, rotateImage and stringToImage, have
    // been cut-and-pasted from ExploreDataPage by Steve Kraffmiller;
    // In the long run, we'll want this code to be in one place and not
    // duplicated. For now, I'm just trying to get it all to work.

    private BufferedImage getCompositeImage(BufferedImage image) throws FontFormatException, IOException{ Integer heightAdjustment = new Integer(0);
        if (this.heightInt == 1){
            heightAdjustment = 40;
        }
        if (this.heightInt == 3){
            heightAdjustment = -100;
        }        
        
        BufferedImage yAxisImage = new BufferedImage(100, 500, BufferedImage.TYPE_INT_ARGB);
        BufferedImage combinedImage = new BufferedImage(776, 575 + heightAdjustment , BufferedImage.TYPE_INT_ARGB);

        if(graphTitle.trim().isEmpty()){
            graphTitle = " ";
        }
       
        // Do we need some diagnostics here? Should we throw an exception or
        // display an error message if generateImageString returns null? 
        // Or is this on purpose (the title being considered a non-essential
        // thing)? 
        //
        // - L.A. 
        
        File retFile = generateImageString("16", "620x", "South", "0", graphTitle);       
        BufferedImage titleImage =     ImageIO.read(retFile);
        
        if (!FileUtil.keepTempFiles("core.web.servlet.DataVisServlet")) {
            if (retFile != null && retFile.exists()) {
                retFile.delete();
            }
            
        }

        String source = "";

        if (!sources.trim().isEmpty()) {
             source = sources;
        }
        
        if(source.trim().isEmpty()){
            source = " ";
        }
        retFile = generateImageString("14", "676x", "NorthWest", "0", source);

        if (retFile == null) {
            dbgLog.info("failed to generate \"source\" text image.");
            return null;
        }

        BufferedImage sourceImage =     ImageIO.read(retFile);
        
        if(yAxisLabel.trim().isEmpty()){
            yAxisLabel = " ";
        }  
        
        if (!FileUtil.keepTempFiles("core.web.servlet.DataVisServlet")) {
            if (retFile.exists()) {
                retFile.delete();
            }
            
        }
        
        retFile = generateImageString("14", new Integer(350 + heightAdjustment) + "x", "South", "-90", yAxisLabel);        
        BufferedImage yAxisVertImage =     ImageIO.read(retFile);
        
        if (!FileUtil.keepTempFiles("core.web.servlet.DataVisServlet")) {
            if (retFile != null && retFile.exists()) {
                retFile.delete();
            }
            
        }
        
        Graphics2D yag2 = yAxisImage.createGraphics();
        Graphics2D cig2 = combinedImage.createGraphics();
        //Graphics2D sig2 = sourceImage.createGraphics();
        

        cig2.setColor(Color.WHITE);
        yag2.setColor(Color.WHITE);
        yag2.fillRect(0, 0, 676, 500);
        cig2.fillRect(0, 0, 776, 550);

        cig2.drawImage(yAxisImage, 0, 0, null);
        cig2.drawImage(yAxisVertImage, 15, 50 , null);
        cig2.drawImage(image, 50, 50, null);
        cig2.drawImage(titleImage, 85, 15, null);
        cig2.drawImage(sourceImage, 45, 475 + heightAdjustment, null);

        yag2.dispose();
        //sig2.dispose();
        cig2.dispose();        
        
        return combinedImage;
    }
    
    private File generateImageString(String size, String width, String orientation, String rotate, String inStr) throws IOException {
        // let's attempt to generate the Text image:
        int exitValue = 0;
        File file = File.createTempFile("imageString","tmp");
        if (new File("/usr/bin/convert").exists()) {           
            
            String ImageMagick = "/usr/bin/convert  -background white  -font Helvetica " +
                    "-pointsize 14  -gravity center  -size 676x  caption:\'Graph Title\'" +
                    " png:" + file.getAbsolutePath();
            
            String ImageMagickCmd[] = new String[15];
            
            ImageMagickCmd[0] = "/usr/bin/convert";
            ImageMagickCmd[1] = "-background";
            ImageMagickCmd[2] = "white";
            ImageMagickCmd[3] = "-font";
            ImageMagickCmd[4] = "Helvetica";
            ImageMagickCmd[5] = "-pointsize";
            ImageMagickCmd[6] = size;
            ImageMagickCmd[7] = "-gravity";
            ImageMagickCmd[8] = orientation;
            ImageMagickCmd[9] = "-rotate";
            ImageMagickCmd[10] = rotate;
            ImageMagickCmd[11] = "-size";
            ImageMagickCmd[12] = width;
            ImageMagickCmd[13] = "caption:" + inStr;
            ImageMagickCmd[14] = "png:" + file.getAbsolutePath();
                       
            
            try {
                Runtime runtime = Runtime.getRuntime();

                long freeMem = runtime.freeMemory();

                dbgLog.info("free memory: "+freeMem); 

                if ( freeMem < 150000000 ) {
                    // let's collect us some garbage:
                    runtime.gc();

                    // 150M appears to be a safe estimate of how much
                    // memory our ImageMagick process needs to run -- L.A.

                    freeMem = runtime.freeMemory();
                    dbgLog.info("free memory, after GC: "+freeMem);

                }

                Process process = runtime.exec(ImageMagickCmd);
                exitValue = process.waitFor();
            } catch (Exception e) {
                dbgLog.info("Exception caught attempting to run external ImageMagick process!");
                dbgLog.info(e.getMessage());

                exitValue = 1;
            }

            if (exitValue == 0) {
                return file;
            }

            dbgLog.info("non-zero exit value ("+exitValue+") from the exec() of ImageMagick.");

            //return file;
            return null;
        }

        dbgLog.info("Could not find convert program; returning null.");

        return null;
    }


    // This method can be used to send extra diagnostics information with
    // error responses:

    private void createErrorResponseGeneric(HttpServletResponse res, int status, String statusLine) {
        res.setContentType("text/html");

        if (status == 0) {
            status = 200;
        }

        res.setStatus(status);
        PrintWriter out = null;
        try {
            out = res.getWriter();
            out.println("<HTML>");
            out.println("<HEAD><TITLE>File Download</TITLE></HEAD>");
            out.println("<BODY>");
            out.println("<BIG>" + statusLine + "</BIG>");

            if (status == res.SC_NOT_FOUND) {
                for (int i = 0; i < 10; i++) {
                    out.println("<!-- This line is filler to handle IE case for 404 errors   -->");
                }
            }

            out.println("</BODY></HTML>");
            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }
    

    


    private void createErrorResponse404(HttpServletResponse res) {
        createErrorResponseGeneric(res, res.SC_NOT_FOUND, "Sorry. The file you are looking for could not be found.");
        //createRedirectResponse(res, "/dvn/faces/ErrorPage.xhtml?errorMsg=Sorry. The file you are looking for could not be found.&errorCode=404");
    }

}

