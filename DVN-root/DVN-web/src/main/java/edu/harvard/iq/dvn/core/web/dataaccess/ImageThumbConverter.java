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
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Iterator;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import edu.harvard.iq.dvn.core.study.StudyFile;

/**
 *
 * @author leonidandreev
 */
public class ImageThumbConverter {
    public ImageThumbConverter() {
    }
    
    
    public static FileAccessObject getImageThumb (StudyFile file, FileAccessObject fileDownload) {
        if (file != null && file.getFileType().substring(0, 6).equalsIgnoreCase("image/")) {
            if (generateImageThumb(file.getFileSystemLocation())) {
                File imgThumbFile = new File(file.getFileSystemLocation() + ".thumb");

                if (imgThumbFile != null && imgThumbFile.exists()) {

                    fileDownload.closeInputStream();
                    fileDownload.setSize(imgThumbFile.length());

                    
                    InputStream imgThumbInputStream = null; 
                    
                    try {

                        imgThumbInputStream = new FileInputStream(imgThumbFile);
                    } catch (IOException ex) {
                        return null; 
                    }
                    
                    if (imgThumbInputStream != null) {
                        fileDownload.setInputStream(imgThumbInputStream);
                        fileDownload.setIsLocalFile(true);
                               
                        fileDownload.setMimeType("image/png");
                    } else {
                        return null; 
                    }
                }
            }
        } 
        
        
        return fileDownload;
    }
    
    
    private static boolean generateImageThumb(String fileLocation) {

        String thumbFileLocation = fileLocation + ".thumb";

        // see if the thumb is already generated and saved:

        if (new File(thumbFileLocation).exists()) {
            return true;
        }

        // let's attempt to generate the thumb:

        // (I'm scaling all the images down to 64 pixels horizontally;
        // I picked the number 64 totally arbitrarily;
        // TODO: (?) make the default thumb size configurable
        // through a JVM option??


        if (new File("/usr/bin/convert").exists()) {

            String ImageMagick = "/usr/bin/convert -size 64x64 " + fileLocation + " -resize 64 -flatten png:" + thumbFileLocation;
            int exitValue = 1;

            try {
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec(ImageMagick);
                exitValue = process.waitFor();
            } catch (Exception e) {
                exitValue = 1;
            }

            if (exitValue == 0) {
                return true;
            }
        }

        // For whatever reason, creating the thumbnail with ImageMagick
        // has failed.
        // Let's try again, this time with Java's standard Image
        // library:

        try {
            BufferedImage fullSizeImage = ImageIO.read(new File(fileLocation));

	    if ( fullSizeImage == null ) {
		return false; 
	    }

            double scaleFactor = ((double) 64) / (double) fullSizeImage.getWidth(null);
            int thumbHeight = (int) (fullSizeImage.getHeight(null) * scaleFactor);

	    // We are willing to spend a few extra CPU cycles to generate
	    // better-looking thumbnails, hence the SCALE_SMOOTH flag. 
	    // SCALE_FAST would trade quality for speed. 

	    java.awt.Image thumbImage = fullSizeImage.getScaledInstance(64, thumbHeight, java.awt.Image.SCALE_SMOOTH);

            ImageWriter writer = null;
            Iterator iter = ImageIO.getImageWritersByFormatName("png");
            if (iter.hasNext()) {
                writer = (ImageWriter) iter.next();
            } else {
                return false;
            }

            BufferedImage lowRes = new BufferedImage(64, thumbHeight, BufferedImage.TYPE_INT_RGB);
            lowRes.getGraphics().drawImage(thumbImage, 0, 0, null);

            ImageOutputStream ios = ImageIO.createImageOutputStream(new File(thumbFileLocation));
            writer.setOutput(ios);

            // finally, save thumbnail image:
            writer.write(lowRes);
            writer.dispose();

            ios.close();
            thumbImage.flush();
            fullSizeImage.flush();
            lowRes.flush();
            return true;
        } catch (Exception e) {
            // something went wrong, returning "false":
	    //dbgLog.info("ImageIO: caught an exception while trying to generate a thumbnail for "+fileLocation);

            return false;
        }
    }
}
