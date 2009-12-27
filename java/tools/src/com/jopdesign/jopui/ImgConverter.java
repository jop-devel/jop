/*
 * This file is a part of the Jop-UI 
 * Copyright (C) 2009, 	Stefan Resch (e0425306@student.tuwien.ac.at)
 * 						Stefan Rottensteiner (e0425058@student.tuwien.ac.at)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.jopui;

import java.io.*;
import java.awt.image.AffineTransformOp;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.lang.reflect.Array;
import java.lang.Integer;
import java.awt.RenderingHints;
import javax.imageio.*;
import java.util.ArrayList;
import java.awt.image.Raster;
import java.awt.image.DataBuffer;


class ImgConverter {

        private String outfile;
        private DataOutputStream dout;
        private static String[] image_types = ImageIO.getWriterFormatNames();
        private ArrayList<String> files;
        private boolean verbose;
        private boolean print_filetypes;
        
        ImgConverter() {
                this.outfile = null;
                this.dout = null;
                files = new ArrayList<String>();
                verbose = false;
                print_filetypes = false;                
        }

        public static void main (String[] args)
        {
                ImgConverter app = new ImgConverter();
                
                for(int i = 0; i<Array.getLength(args); i++) {
                        app.configure(args[i]);
                }

                app.execute();
        }

        public static boolean isImageFile(String filename)
        {
                for(int i=0; i < image_types.length; i++) {
                        if(filename.endsWith("." + image_types[i]))
                                return true;
                }
                return false;
        }
                
        private void configure(String arg)
        {
                if(arg.startsWith("-")) {
                        
                        if(arg.compareTo("-v") == 0) {
                                this.verbose = true;
                        } else if(arg.compareTo("-p") == 0) {
                                this.print_filetypes = true;
                        } else if(arg.startsWith("-o")) {
                                this.outfile = arg.substring(2);
                                if(this.outfile.length() == 0) {
                                        System.out.println("Use -o like -oimage.jpf .");
                                        System.exit(0);
                                }
                        } else {
                                System.out.println("Parameter "+ arg + " unkown.");
                        }
                } else {
                        this.addfile(arg);
                }
        }
        
        private void addfile(String path)
        {
                File imgFile = new File(path);
                
                if(!imgFile.exists()) {
                        System.out.println("File or Directory " + path + " does not exist.");
                        return;
                }
                
                if (imgFile.isDirectory()) {
                        for(String sfile: imgFile.list()) {
                                this.addfile(path + "/" + sfile);
                        }
                } else if (imgFile.isFile()) {
                        if(!this.isImageFile(path)) {
                                System.out.println("File " + path + " is no image-file");
                                return;
                        }
                        files.add(path);
                }
        }

        private void execute()
        {
                if(this.outfile == null) {
                        System.out.println("Please specify an outfile with -o .");
                        return;
                }

                try {
                       this.dout = new DataOutputStream(new FileOutputStream(this.outfile));
                        
                } catch (IOException e) {
                        System.out.println("Outfile " + this.outfile + " could not be opened.");
                        return;
                }

                if(this.verbose || this.print_filetypes){
                        System.out.println("Supported file types:");

                        for(int i=0; i < image_types.length; i++) {
                                System.out.println("    " + image_types[i]);
                        }
                }

                for(int i=0; i<files.size(); i++) {
                        appendfile(files.get(i));
                }
                try {
                       this.dout.writeByte('E'); // end of file
                       this.dout.close();
                        
                } catch (IOException e) {
                        System.out.println(this.outfile + " could not be closed.");
                        return;
                }
                 
        }
        
        private void appendfile(String path)
        {       
                BufferedImage imgIn;
		Raster imgRaster;
                if(verbose) {
                        System.out.println("processing " + path + "... ");
                }
                
                try {
                        File infile = new File(path);
                        imgIn = ImageIO.read(infile);
                        ColorModel cm = imgIn.getColorModel();
			imgRaster = imgIn.getRaster();

                        dout.writeBytes("JPF");                         // marker at beginning
                        dout.writeByte(0);                              // version number;
                        dout.writeByte(infile.getName().length());      // name length
                        dout.writeBytes(infile.getName());              // name
                        dout.writeByte((imgIn.getWidth()>>8) & 0xFF);   // file width high byte
                        dout.writeByte((imgIn.getWidth()) & 0xFF);      // file width low byte
                        dout.writeByte((imgIn.getHeight()>>8) & 0xFF);  // file heigth high byte
                        dout.writeByte((imgIn.getHeight()) & 0xFF);     // file height low byte    

                        for(int y=0; y < imgIn.getHeight(); y++) {      // write pixel line after line

                                for(int x=0; x < imgIn.getWidth(); x++) {
                                        
					//System.out.println(Integer.toHexString(pcol));
					Object outdata = null;
 
        				switch (imgRaster.getDataBuffer().getDataType()) {
        					case DataBuffer.TYPE_BYTE:
            						outdata = new byte[imgRaster.getNumBands()];
            						break;
        					case DataBuffer.TYPE_USHORT:
            						outdata = new short[imgRaster.getNumBands()];
            						break;
        					case DataBuffer.TYPE_INT:
            						outdata = new int[imgRaster.getNumBands()];
            						break;
        					case DataBuffer.TYPE_FLOAT:
            						outdata = new float[imgRaster.getNumBands()];
            						break;
        					case DataBuffer.TYPE_DOUBLE:
							outdata = new double[imgRaster.getNumBands()];
           	 					break;
					        default:
        				}

					Object pcol = imgRaster.getDataElements(x, y, outdata);
                                        int pcolout = 0;
                                        pcolout |= (((byte)cm.getRed(pcol) >> 5) & 0x07)<<5;
                                        pcolout |= (((byte)cm.getGreen(pcol) >> 5) & 0x07)<<2;
                                        pcolout |= (((byte)cm.getBlue(pcol) >> 6) & 0x03)<<0;
                                        dout.writeByte(pcolout);
                                }                        
                        }                              
                        
                } catch (IOException e) {
                        e.printStackTrace();        
                }
                
        }
        
}
