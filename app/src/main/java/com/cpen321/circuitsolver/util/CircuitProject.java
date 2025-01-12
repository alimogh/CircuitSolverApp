package com.cpen321.circuitsolver.util;


// Class to fully define the circuit, to keep track of where are the images and data is kept
// hereeeee wee gooooooooooooo


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CircuitProject implements Cloneable {
    private File originalImage = null;
    private File downsizedImage = null;
    private File processedImage = null;
    private File circuitDefinition = null;

    private File savedFolder = null;

    public CircuitProject(File directory) {
        this.savedFolder = directory;
        this.loadFromFolder();
    }

    public CircuitProject(String folderName, File parentDirectory) {
        File tmpFolder = new File(parentDirectory, folderName);
        tmpFolder.mkdir();
        this.savedFolder = tmpFolder;
    }

    public Bitmap getThumbnail() {
        try {
            if (this.downsizedImage != null) {
                return this.getDownsizedImage();
            } else {
                return ImageUtils.downsizeImage(this.getOriginalImage(), Constants.PROCESSING_WIDTH);
            }
        } catch (IOException ex) {
            return null;
        }
    }

    public File getOriginalImageLocation() throws IOException {
        if (this.originalImage != null)
            return this.originalImage;
        else
            return this.generateOriginalImageFile();
    }

    public String getCircuitText() throws IOException{
        return this.getText(this.circuitDefinition);
    }

    public Bitmap getOriginalImage() throws IOException {
        return this.getImage(this.originalImage, false);
    }
    public Bitmap getProcessedImage() throws IOException {
        return this.getImage(this.processedImage, true);
    }
    public Bitmap getDownsizedImage() throws IOException {
        return this.getImage(this.downsizedImage, true);
    }

    private Bitmap getImage(File imageLocation, boolean downsize) throws IOException{
        FileInputStream inStream = new FileInputStream(imageLocation);
        Bitmap thumbnail = BitmapFactory.decodeStream(inStream);

        if (thumbnail == null)
            return null;

        if (downsize) {
            inStream.close();
            return ImageUtils.downsizeImage(thumbnail, Constants.PROCESSING_WIDTH);
        } else {
            inStream.close();
            return thumbnail;
        }

    }

    private String getText(File fileLocation) throws IOException{
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new FileReader(fileLocation));
        String line;
        while ((line = in.readLine()) != null){
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }

    public void saveOriginalImage(Bitmap originalBM) {
        if (this.originalImage == null)
            this.generateOriginalImageFile();
        this.saveImage(originalBM, this.originalImage);
    }

    public void saveDownsizedImage(Bitmap downsizedBM) {
        if (this.downsizedImage == null)
            this.generateDownsizedImageFile();
        this.saveImage(downsizedBM, this.downsizedImage);
    }

    public void saveProcessedImage(Bitmap processedBM) {
        if (this.processedImage == null)
            this.generateProcessedImageFile();
        this.saveImage(processedBM, this.processedImage);
    }

    public void saveCircuitDefinitionFile(String txt){
        if(this.circuitDefinition == null)
            this.generateCircuitDefinitionFile();
        this.saveTextFile(txt, this.circuitDefinition);
    }

    private void saveTextFile(String txt, File filename){
        FileWriter writer = null;
        try {
            writer = new FileWriter(filename);
            writer.write(txt);
            writer.close();
        } catch (FileNotFoundException ex) {
            System.out.println("FILE NOT FOUND");
            ex.printStackTrace();
            return;
        } catch (IOException ex) {
            System.out.println("IO EXCEPTION");
            ex.printStackTrace();
            return;
        }
    }

    private void saveImage(Bitmap imgBmp, File filename) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filename);
            imgBmp.compress(Bitmap.CompressFormat.JPEG, Constants.COMPRESSION_QUALITY, outputStream);
            outputStream.close();
        } catch (FileNotFoundException ex) {
            System.out.println("FILE NOT FOUND");
            ex.printStackTrace();
            return;
        } catch (IOException ex) {
            System.out.println("IO EXCEPTION");
            ex.printStackTrace();
            return;
        }
    }

    private void loadFromFolder() {
        File[] files = this.savedFolder.listFiles();
        if (files == null)
            return;
        for (File file : files) {
            if (file.getName().contains("original_"))
                this.originalImage = file;
            else if (file.getName().contains("downsized_"))
                this.downsizedImage = file;
            else if (file.getName().contains("processed_"))
                this.processedImage = file;
            else if (file.getName().contains("circuit_"))
                this.circuitDefinition = file;
        }
    }

    public File generateCircuitDefinitionFile(){
        // this function generates a file for the circuit definition and assigns it to the class variable
        this.circuitDefinition = this.generateTxtFile("circuit_");
        return this.circuitDefinition;
    }


    public File generateProcessedImageFile() {
        // this function generates a file for the processed image and assigns it to the class variable
        this.processedImage = this.generateImage("processed_");
        return this.processedImage;
    }

    public File generateDownsizedImageFile() {
        // this function generates a file for the processed image and assigns it to the class variable
        this.downsizedImage = this.generateImage("downsized_");
        return this.downsizedImage;
    }

    public File generateOriginalImageFile() {
        // this function generates a file for the processed image and assigns it to the class variable
        this.originalImage = this.generateImage("original_");
        return this.originalImage;
    }

    private File generateTxtFile(String prefix){
        if (this.savedFolder == null){
            System.out.println("saved folder is null....");
            return null;
        }
        File tmpFile = null;
        try {
            String filename = prefix + ImageUtils.getTimeStamp();
            tmpFile = File.createTempFile(filename, ".txt", this.savedFolder);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return tmpFile;
    }

    private File generateImage(String prefix) {
        if (this.savedFolder == null){
            System.out.println("saved folder is null....");
            return null;
        }

        File tmpFile = null;
        try {
            String filename = prefix + ImageUtils.getTimeStamp();
            tmpFile = File.createTempFile(filename, ".jpg", this.savedFolder);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return tmpFile;
    }

    public void convertOriginalToDownsized() {
        try {
            this.saveDownsizedImage(this.getImage(this.originalImage, true));
        } catch (IOException ex) {
            System.out.println(" HUGGGGEEE CRASSSHHHHHH");
            ex.printStackTrace();
            return;
        }
    }

    public String getFolderPath() {
        return this.savedFolder.getAbsolutePath();
    }

    @Override
    public String toString() {
        String tmpString = "";
        if (this.savedFolder != null)
            tmpString += " Folder: " + this.savedFolder.toString();
        if (this.originalImage != null)
            tmpString += " Original Image: " + this.originalImage.toString();
        if (this.processedImage != null)
            tmpString += " Processed Image: " + this.processedImage.toString();
        if(this.circuitDefinition != null)
            tmpString += " Circuit Definition: " + this.circuitDefinition.toString();

        return tmpString;
    }

    public void print() {
        System.out.println(this.toString());
    }

    public String getFolderID() {
        return this.savedFolder.getName();
    }

    public boolean deleteFileSystem() {
        if (this.downsizedImage != null)
            this.downsizedImage.delete();
        if (this.originalImage != null)
            this.originalImage.delete();
        if (this.processedImage != null)
            this.processedImage.delete();
        if (this.circuitDefinition != null)
            this.circuitDefinition.delete();
        if (this.savedFolder != null)
            this.savedFolder.delete();
        return true;
    }

    // Fix java's "protected clone" mistake: http://stackoverflow.com/a/1138790/2734863
    // note: returns null if cloning fails
    @Override
    public CircuitProject clone(){
        //return (CircuitElm) super.clone();
        Object clone = null;
        try {
            clone = super.clone();
        }
        catch (CloneNotSupportedException exception){
            // Why the try/catch? http://stackoverflow.com/a/8609338/2734863
        }
        return (CircuitProject) clone;
    }

}
