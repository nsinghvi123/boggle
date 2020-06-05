package com.boggle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Image;
import com.google.protobuf.ByteString;

import javax.imageio.ImageIO;
import javax.swing.text.html.parser.Entity;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BoggleBoardGenerator {
    static Map<Vertex,Character> map = new HashMap<>();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module());

    public static byte[] extractBytes (String ImageName) throws IOException {
        // open image
        File imgPath = new File(ImageName);
        BufferedImage bufferedImage = ImageIO.read(imgPath);

        // get DataBufferBytes from Raster
        WritableRaster raster = bufferedImage .getRaster();
        DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();

        return data.getData();
    }

    public static void main(String[] args) throws Exception {
        int width = getWidth();
        int height = getHeight();
        List<EntityAnnotation> finalAnnotation = new ArrayList<EntityAnnotation>();
        finalAnnotation = returnAnnotationsViaGoogle();


        for (int i = 1; i < finalAnnotation.size(); i++){
            BoggleBoardGenerator boggleBoardGenerator = new BoggleBoardGenerator();
            BoggleAnnotations boggleAnnotations = new BoggleAnnotations(finalAnnotation.get(i), width, height, finalAnnotation.get(i).getDescription());
            System.out.println("Index: " + i); //prints out the information for each index of finalAnnotation
            int colMin = boggleAnnotations.findClosestLineNumber(boggleAnnotations.getTopLeftX(), boggleAnnotations.rowPixelIncrement);
            int colMax = boggleAnnotations.findClosestLineNumber(boggleAnnotations.getTopRightX(), boggleAnnotations.rowPixelIncrement);
            int rowMin = boggleAnnotations.findClosestLineNumber(boggleAnnotations.getTopRightY(), boggleAnnotations.colPixelIncrement);
            int rowMax = boggleAnnotations.findClosestLineNumber(boggleAnnotations.getBottomRightY(), boggleAnnotations.colPixelIncrement);
            List<Integer> arrayRow = boggleBoardGenerator.printAllInBetween(rowMin, rowMax);
            List<Integer> arrayCol = boggleBoardGenerator.printAllInBetween(colMin, colMax);
            boggleBoardGenerator.AllVertexCombinations(arrayRow, arrayCol, boggleAnnotations.description);
            System.out.println(map);
        }
    }

        public static List<EntityAnnotation> returnAnnotationsViaGoogle(){
        List <EntityAnnotation> realAnnotation = new ArrayList<EntityAnnotation>();
            // Instantiates a client
            try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

                // The path to the image file to annotate
                String fileName = "/Users/natashasinghvi/Documents/boggle/src/main/java/com/boggle/TASHUPhotoboggle.jpeg";

                // Reads the image file into memory
                Path path = Paths.get(fileName);
                byte[] data = Files.readAllBytes(path);
                ByteString imgBytes = ByteString.copyFrom(data);

                // Builds the image annotation request
                List<AnnotateImageRequest> requests = new ArrayList<>();
                Image img = Image.newBuilder().setContent(imgBytes).build();
                Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
                AnnotateImageRequest request =
                        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
                requests.add(request);

                // Performs label detection on the image file
                BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

                AnnotateImageResponse annotateImageResponse = responses.get(0);
                List<EntityAnnotation> annotations = annotateImageResponse.getTextAnnotationsList();
                realAnnotation = annotations;
                return annotations;
            } catch (IOException e) {
                e.printStackTrace();
            }
           return realAnnotation;
        }

        public static int getWidth() throws IOException {
            BufferedImage bimg = ImageIO.read(new File("/Users/natashasinghvi/Documents/boggle/src/main/java/com/boggle/TASHUPhotoboggle.jpeg"));
            int width = bimg.getWidth();
            return width;
        }

        public static int getHeight() throws IOException {
            BufferedImage bimg = ImageIO.read(new File("/Users/natashasinghvi/Documents/boggle/src/main/java/com/boggle/TASHUPhotoboggle.jpeg"));
            int height = bimg.getHeight();
            return height;
        }

        public ArrayList printAllInBetween(int minimum, int maximum){
            List<Integer> array = new ArrayList<>();
            for (int i = minimum + 1; i <= maximum; i++){
                array.add(i);
            }
            return (ArrayList) array;
        }

    public void AllVertexCombinations(List<Integer> row, List<Integer> col, String description){
        int letterNum = 0;
        for (int i = 0; i < row.size(); i++){
            for (int a = 0; a < col.size(); a++){
                System.out.println(row.get(i) + col.get(a));
                Vertex newVertex = new Vertex(row.get(i), col.get(a));
                map.put(newVertex, description.charAt(letterNum));
                letterNum++;
            }
        }
    }
}
