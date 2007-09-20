/**
 * 
    SubsettableFileChecker.java
 */

package edu.harvard.hmdc.vdcnet.dsb;

import static java.lang.System.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.regex.*;
import java.util.zip.*;

/**
 * @author akio sone
 *
 */
 
public class SubsettableFileChecker {
    /**
     * 
     */

    // static fields
    
    // default format set
    private static String[] defaultFormatSet = {"POR", "SAV", "DTA", "RDA", "XPT"};
    
    private String[] testFormatSet;
    
    // Map that returns a Stata Release number
    private static Map<Byte, String> stataReleaseNumber= new HashMap<Byte, String>();
    
    // Map that returns a reader-implemented mime-type
    private static Set<String> readableFileTypes = new HashSet<String>();
    
    private static Map<String, Method> testMethods = new HashMap<String, Method>();

    
    public static String SAS_XPT_HEADER_80 ="HEADER RECORD*******LIBRARY HEADER RECORD!!!!!!!000000000000000000000000000000  ";
    
    public static String SAS_XPT_HEADER_11 = "SAS     SAS";

    public static int POR_MARK_POSITION_DEFAULT = 461;
        
    public static String POR_MARK = "SPSSPORT";
        
    private static int DEFAULT_BUFFER_SIZE = 500; 
    
    private static String regex = "^test(\\w+)format$";

    // static initialization block
    private static String rdargx = "^(52)(44)(41|42|58)(31|32)(0A)$";
    private static int RDA_HEADER_SIZE = 5;
    private static Pattern ptn;

    static {

        stataReleaseNumber.put((byte)104, "rel_3");
        stataReleaseNumber.put((byte)105, "rel_4or5");
        stataReleaseNumber.put((byte)108, "rel_6");
        stataReleaseNumber.put((byte)110, "rel_7first");
        stataReleaseNumber.put((byte)111, "rel_7scnd");
        stataReleaseNumber.put((byte)113, "rel_8_or_9");
        stataReleaseNumber.put((byte)114, "rel_10");
        
        readableFileTypes.add("application/x-stata");
        readableFileTypes.add("application/x-spss-sav");
        readableFileTypes.add("application/x-spss-por");
        readableFileTypes.add("application/x-rlang-transport");
        
        Pattern p = Pattern.compile(regex);
        ptn = Pattern.compile(rdargx);

        for (Method m: SubsettableFileChecker.class.getDeclaredMethods()){
            //String mname = m.getName();
            // if (mname.startsWith("test")) && (mname.endsWith("format")){
            
            Matcher mtr = p.matcher(m.getName());
            if (mtr.matches()){
                testMethods.put(mtr.group(1), m);
            }
        }
    }
    
    private boolean windowsNewLine=true;

    
    // constructors
    
    // using the default format set
    public SubsettableFileChecker() {
        this.testFormatSet = defaultFormatSet;
    }
    // using a user-defined customized format set
    public SubsettableFileChecker(String[] requestedFormatSet) {
        this.testFormatSet = requestedFormatSet;
    }
    
    
    // public class methods
    
    public static String[] getDefaultTestFormatSet(){
        return  defaultFormatSet;
    }
    
    
    /**
     *  print the usage
     *
     */
    public static void printUsage(){
        out.println("Usage : java subsettableFileChecker <datafileName>");
    }

    // instance methods
    
    public String[] getTestFormatSet() {
        return this.testFormatSet;
    }
    



    // test methods start here ------------------------------------------------
    /**
     * test this byte buffer against SPSS-SAV spec
     *
     *
     */
    
    public String testSAVformat (MappedByteBuffer buff){
        String result=null;
        buff.rewind();
        boolean DEBUG=false;
        
        if (DEBUG){
            out.println("applying the sav test\n");
        }
        
        byte[] hdr4 = new byte[4];
        buff.get(hdr4, 0, 4);
        String hdr4sav = new String(hdr4);
        if (DEBUG){
            out.println("from string="+hdr4sav);
        }
        if (hdr4sav.equals("$FL2")){
            if (DEBUG){
                out.println("this file is spss-sav type");
            }
            result= "application/x-spss-sav";
        } else {
            if (DEBUG){
                out.println("this file is NOT spss-sav type");
            }
        }
        return result;
    }
    
    
    /**
     * test this byte buffer against STATA DTA spec
     *
     */

    public String testDTAformat(MappedByteBuffer buff){
        String result=null;
        buff.rewind();
        boolean DEBUG=false;

        if (DEBUG){
            out.println("applying the dta test\n");
        }

        byte[] hdr4 = new byte[4];
        buff.get(hdr4, 0, 4);
        
        if (DEBUG){
            for (int i =0; i< hdr4.length ; ++i){
                out.printf("%d\t%02X\n",i, hdr4[i] );
            }
        }
        
        if (hdr4[2] != 1){
            if (DEBUG){
                out.println("3rd byte is not 1: given file is not stata-dta type");
            }
            return result;
        } else  if ((hdr4[1] != 1 ) && (hdr4[1] != 2)){
            if (DEBUG){
                out.println("2nd byte is neither 0 nor 1: this file is not stata-dta type");
            }
            return result;
        } else if (!SubsettableFileChecker.stataReleaseNumber.containsKey(hdr4[0])){
            if (DEBUG){
                out.println("1st byte ("+ hdr4[0] +
            ") is not within the ingestable range [rel. 3-10]: this file is NOT stata-dta type");
            }
            return result;
        } else {
            if (DEBUG){
                out.println("this file is stata-dta type: "+
                SubsettableFileChecker.stataReleaseNumber.get(hdr4[0])+
                "(No in HEX=" +hdr4[0]+ ")");
            }
            result = "application/x-stata";
        }
        return result;
    }
    
    /**
     * test this byte buffer against SAS Transport(XPT) spec
     *
     */

    public String testXPTformat(MappedByteBuffer buff){
        String result=null;
        buff.rewind();
        boolean DEBUG=false;
        
        if (DEBUG){
            out.println("applying the sas-transport test\n");
        }
        // size test
        if (buff.capacity() < 91 ){
            if (DEBUG){
                out.println("this file is NOT sas-exort type\n");
            }
            
            return result;
        }
        
        byte[] hdr1 = new byte[80];
        byte[] hdr2 = new byte[11];
        buff.get(hdr1, 0, 80);
        buff.get(hdr2, 0, 11);

        String hdr1st80 = new String(hdr1);
        String hdrnxt11 = new String(hdr2);
        
        if (DEBUG){
            out.println("1st-80  bytes="+hdr1st80);
            out.println("next-11 bytes="+hdrnxt11);
        }
        
        if ((hdr1st80.equals(SubsettableFileChecker.SAS_XPT_HEADER_80)) &&
            (hdrnxt11.equals(SubsettableFileChecker.SAS_XPT_HEADER_11))){
            if (DEBUG){
                out.println("this file is sas-export type\n");
            }
            result ="application/x-sas-xport";
        } else {
            if (DEBUG){
                out.println("this file is NOT sas-exort type\n");
            }
        }
        return result;
    }


    /**
     * test this byte buffer against SPSS Portable (POR) spec
     *
     */
    public String testPORformat(MappedByteBuffer buff){
        String result=null;
        buff.rewind();
        boolean DEBUG=false;

        if (DEBUG){
            out.println("applying the spss-por test\n");
        }
        
        // size test
        if (buff.capacity() < 491 ){
            if (DEBUG){
                out.println("this file is NOT spss-por type\n");
            }
            return result;
        }

        //windows [0D0A]=> [1310]
        //unix    [0A]  => [10]
        //mac     [0D]  => [13]
        
        // expected results
        // unix    case: [0A]   : [80], [161], [242], [323], [404], [485]
        // windows case: [0D0A] : [81], [163], [245], [327], [409], [491]
        
        
        buff.rewind();
        byte[] nlch = new byte[18];
        int pos1;
        int pos2;
        int ucase=0;
        int wcase=0;
        int mcase=0;
        for (int i=0; i<6; ++i){
            pos1 = 80*(i+1) + i;
            buff.position(pos1);
            int j = 3*i;
            nlch[j]= buff.get();
            if (nlch[j] == 10){
                ucase++;
            } else if (nlch[j]== 13){
                mcase++;
            }
            pos2 = 80*(i+1) +2*i;
            buff.position(pos2);
            if (DEBUG){
                out.println("\tposition="+buff.position());
            }
            nlch[j+1] = buff.get();
            nlch[j+2] = buff.get();
            
            if (DEBUG){
                out.println(i+"-th iteration position ="+nlch[j]+"\t"+nlch[j+1] +"\t"+nlch[j+2]);
            }
            if ( (nlch[j+1] == 13) && (nlch[j+2] == 10) ){
                wcase++;
            }
            buff.rewind();
        }

        if ((ucase == 6) && (wcase < 6)){
            if (DEBUG){
                out.println("0A case");
            }
            windowsNewLine = false;
        } else if ((ucase < 6) && (wcase == 6)){
            if (DEBUG){
                out.println("0D0A case");
            }
        } else if ( (mcase == 6) && (wcase < 6) ){
            if (DEBUG){
                out.println("0D case");
            }
        }
        

        buff.rewind();
        int PORmarkPosition = POR_MARK_POSITION_DEFAULT;
        if (windowsNewLine){
            PORmarkPosition = PORmarkPosition+5;
        }
        
        byte[] pormark = new byte[8];
        buff.position(PORmarkPosition);
        buff.get(pormark, 0, 8);
        String pormarks = new String(pormark);

        if (DEBUG){
            out.println("pormark =>"+pormarks+"<-");
        }
        
        if (pormarks.equals(POR_MARK)){
            if (DEBUG){
                out.println("this file is spss-por type");
            }
            result ="application/x-spss-por";
        } else {
            if (DEBUG){
                out.println("this file is NOT spss-por type");
            }
        }
    
        return result;
    }
    
   
    /**
     * test this byte buffer against R data file
     *
     */
    public String testRDAformat(MappedByteBuffer buff){
        String result=null;
        buff.rewind();
        
        boolean DEBUG=false;
        if (DEBUG){
            out.println("applying the RData test\n");
            out.println("buffer capacity="+buff.capacity());
        }
        if (DEBUG){
            byte[] rawhdr = new byte[4];
            buff.get(rawhdr, 0, 4);
            for (int j=0; j<4; j++){
                out.printf("%02X ", rawhdr[j]);
            }
            out.println();
            buff.rewind();
        }
        // get the first 4 bytes as an int and check its value; 
        // if it is 0x1F8B0800, then gunzip and its first 4 bytes
        int magicNumber = buff.getInt();

        if (DEBUG){
            out.println("magicNumber in decimal ="+magicNumber);
            out.println("in binary="+Integer.toBinaryString(magicNumber));
            out.println("in oct="+Integer.toOctalString(magicNumber));
            out.println("in hex="+Integer.toHexString(magicNumber));
        }
        try {
            if (magicNumber == 0x1F8B0800){
                if (DEBUG){
                    out.println("magicNumber is GZIP");
                }
                // gunzip the first 5 bytes and check their bye-pattern

                // get gzip buffer size

                int gzip_buffer_size= this.getGzipBufferSize(buff);

                byte[] hdr = new byte[gzip_buffer_size];
                buff.get(hdr, 0, gzip_buffer_size);

                GZIPInputStream gzin = new GZIPInputStream(new ByteArrayInputStream(hdr));

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < RDA_HEADER_SIZE; i++){
                    sb.append(String.format("%02X", gzin.read()));
                }
                String fisrt5bytes = sb.toString();

                result = this.checkUncompressedFirst5bytes(fisrt5bytes);
                // end of compressed case
            } else {
                // uncompressed case?
                if (DEBUG){
                    out.println("magicNumber is not GZIP:"+magicNumber);
                    out.println("test as an uncompressed RData file");
                }

                buff.rewind();
                byte[] uchdr = new byte[5];
                buff.get(uchdr, 0, 5);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < uchdr.length; i++){
                    sb.append(String.format("%02X", uchdr[i]));
                }
                String fisrt5bytes = sb.toString();

                result = this.checkUncompressedFirst5bytes(fisrt5bytes);
                // end of uncompressed case
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }
        return result;
    }

   
   
   
   
    // public instance methods ------------------------------------------------
    
    public String detectSubsettableFormat(File fh) throws FileNotFoundException, IOException {
        boolean DEBUG=false;
        String readableFormatType=null;
        int buffer_size = this.getBufferSize(fh);
        // set-up a FileChannel instance for a given file object
        FileChannel srcChannel = new FileInputStream(fh).getChannel();
        
        // create a read-only MappedByteBuffer
        MappedByteBuffer buff = srcChannel.map(FileChannel.MapMode.READ_ONLY, 0, buffer_size);
        
        //this.printHexDump(buff, "hex dump of the byte-buffer");
        
        //for (String fmt : defaultFormatSet){
        buff.rewind();
        
        for (String fmt : this.getTestFormatSet() ){
            // get a test method
            Method mthd = testMethods.get(fmt);
            try {
                // invoke this method
                Object retobj = mthd.invoke(this, buff);
                String result = (String)retobj;

                if (result != null){
                    if (DEBUG){
                        out.println("result for ("+fmt+")="+result);
                    }
                    if (readableFileTypes.contains(result)) {
                        readableFormatType = result;
                    }
                    return readableFormatType;
                } else {
                    if (DEBUG){
                        out.println("null was returned for "+fmt+" test");
                    }
                }
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                err.format(cause.getMessage());
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return readableFormatType;
    }

    /**
     * identify the first 5 bytes
     *
     */    
    
    private String checkUncompressedFirst5bytes(String fisrt5bytes){
        boolean DEBUG=false;
        String result=null;
        if (DEBUG){
            out.println("first5bytes="+fisrt5bytes);
        }
        Matcher mtr = ptn.matcher(fisrt5bytes);

        if (mtr.matches()){
            if (DEBUG){
                out.println("RDATA type");
            }
            result = "application/x-rlang-transport";
        } else {
            if (DEBUG){
                out.println("not binary RDATA type");
            }
        }
        
        return result;
    }
    
    /**
     * adjust the size of the buffer according to the size of 
     * the file if necessary; otherwise, use the default size
     */

    private int getBufferSize(File fh){
        boolean DEBUG=false;
        int BUFFER_SIZE = DEFAULT_BUFFER_SIZE;
        if (fh.length() < DEFAULT_BUFFER_SIZE){
            BUFFER_SIZE = (int)fh.length();
            if (DEBUG){
                out.println("non-default buffer_size: new size="+BUFFER_SIZE);
            }
        }
        return BUFFER_SIZE;
    }

    private int getGzipBufferSize(MappedByteBuffer buff){
        int GZIP_BUFFER_SIZE=120;
        /*
           note: 
           gzip buffer size <= 118  causes "java.io.EOFException: 
           Unexpected end of ZLIB input stream" 
           with a byte buffer of 500 bytes
        */
        // adjust gzip buffer size if necessary
        // file.size might be less than the default gzip buffer size
        if (buff.capacity() < GZIP_BUFFER_SIZE){
            GZIP_BUFFER_SIZE = buff.capacity();
        }
        buff.rewind();
        return GZIP_BUFFER_SIZE;
    }


    /**
     * dump the data buffer in HEX
     *
     */    
    public void printHexDump (MappedByteBuffer buff, String hdr){
        int counter = 0;
        if (hdr != null){
            out.println(hdr);
        }
        for (int i = 0; i < buff.capacity(); i++){
            counter = i+1;
            out.print(String.format("%02X ",buff.get()) );
            if ( counter % 16 == 0){
                out.println();
            } else {
                if (counter % 8 == 0){
                    out.print(" ");
                }
            }
        }
        out.println();
        buff.rewind();
    }

}
