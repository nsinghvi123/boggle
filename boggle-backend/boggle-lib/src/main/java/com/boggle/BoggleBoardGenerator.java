package com.boggle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Image;
import com.google.protobuf.ByteString;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BoggleBoardGenerator {

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

    public static void main(String[] args) throws IOException {
        byte[] fileBytes = Files.readAllBytes(
                Paths.get("/Users/natashasinghvi/Documents/boggle/boggle-backend/boggle-lib/src/main/resources/IMG_0426.jpeg"));
        returnBoggleBoard(fileBytes);
    }

    public static Character[][] returnBoggleBoard(byte[] fileBytes) throws IOException {
        ImageSize newImageSize = calculateImageSize(fileBytes);
        int width = newImageSize.getWidth();
        int height = newImageSize.getHeight();
        Character[][] arrayOfLettersOfBoard = new Character[5][5];
        BoggleBoardGenerator newMap = new BoggleBoardGenerator();
        List<EntityAnnotation> finalAnnotation = new ArrayList<EntityAnnotation>();
        finalAnnotation = returnAnnotationsViaGoogle(fileBytes);
        Map<Vertex, Character> allEntries = new HashMap<>();
        Map<Vertex, Character> noRepetitionEntries = new HashMap<>();

        for (int i = 1; i < finalAnnotation.size(); i++){
            BoggleBoardGenerator boggleBoardGenerator = new BoggleBoardGenerator();
            BoggleAnnotations boggleAnnotations = new BoggleAnnotations(finalAnnotation.get(i), width, height, finalAnnotation.get(i).getDescription());
            int colMin = boggleAnnotations.findClosestLineNumber(boggleAnnotations.getTopLeftX(), boggleAnnotations.rowPixelIncrement);
            int colMax = boggleAnnotations.findClosestLineNumber(boggleAnnotations.getTopRightX(), boggleAnnotations.rowPixelIncrement);
            int rowMin = boggleAnnotations.findClosestLineNumber(boggleAnnotations.getTopRightY(), boggleAnnotations.colPixelIncrement);
            int rowMax = boggleAnnotations.findClosestLineNumber(boggleAnnotations.getBottomRightY(), boggleAnnotations.colPixelIncrement);
            List<Integer> arrayRow = boggleBoardGenerator.printAllInBetween(rowMin, rowMax);
            List<Integer> arrayCol = boggleBoardGenerator.printAllInBetween(colMin, colMax);
            Map<Vertex, Character> vertexCharacterCombinations =
                    boggleBoardGenerator.getVertexCharacterCombinations(arrayRow, arrayCol, boggleAnnotations.description);
            allEntries.putAll(vertexCharacterCombinations);
        }
        noRepetitionEntries = newMap.checkVertexRepetition(allEntries);
        arrayOfLettersOfBoard = generateBoard(noRepetitionEntries);
        for (int i = 0; i < arrayOfLettersOfBoard.length; i++) {
            for (int a = 0; a < arrayOfLettersOfBoard.length; a++) {
                System.out.print(arrayOfLettersOfBoard[i][a] + " ");
            }
            System.out.print("\n");
        }
        return arrayOfLettersOfBoard;
    }

    public static List<EntityAnnotation> returnAnnotationsViaGoogle(byte[] fileBytes){
        List <EntityAnnotation> realAnnotation = new ArrayList<EntityAnnotation>();
            // Instantiates a client
            try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

                ByteString imgBytes = ByteString.copyFrom(fileBytes);

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

    public static ImageSize calculateImageSize(byte[] fileBytes) throws IOException {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileBytes);
            BufferedImage bimg = ImageIO.read(byteArrayInputStream);
            int width = bimg.getWidth();
            int height = bimg.getHeight();
            ImageSize imagesize = new ImageSize(height, width);
            return imagesize;
    }

    public ArrayList printAllInBetween(int minimum, int maximum){
            List<Integer> array = new ArrayList<>();
            for (int i = minimum + 1; i <= maximum; i++){
                array.add(i);
            }
            return (ArrayList) array;
        }

    public Map<Vertex, Character> getVertexCharacterCombinations(List<Integer> row, List<Integer> col, String description){
        Map<Vertex, Character> combinations = new HashMap<>();
        int letterNum = 0;
        for (int i = 0; i < row.size(); i++){
            for (int a = 0; a < col.size(); a++){
                Vertex newVertex = new Vertex(row.get(i), col.get(a));
                combinations.put(newVertex, description.charAt(letterNum));
                letterNum++;
            }
        }
        return combinations;
    }

    public static Character[][] generateBoard(Map<Vertex,Character> finalMap){
        Character arr[][] = new Character[5][5];
        for (Map.Entry<Vertex,Character> entry : finalMap.entrySet()) {
           int x = entry.getKey().getX();
           int y = entry.getKey().getY();
           Character letter = entry.getValue();
           arr[x-1][y-1] = letter;
        }
        return arr;
    }

    public Map<Vertex, Character> checkVertexRepetition(Map<Vertex, Character> allAnnotatedEntries){
        int highest = 0;
        Map<String,Character> alreadyProcessedVertices = new HashMap<>();
        Map<Vertex, Character> finalEntries = new HashMap<>();
        Character mostFrequentLetter = null;
        List<Character> sameVertexLetter = new ArrayList<>();

        for (Map.Entry<Vertex,Character> entry : allAnnotatedEntries.entrySet()){
            int count = 0;
            int x = entry.getKey().getX();
            int y = entry.getKey().getY();
            Character letter = entry.getValue();
            Vertex original = new Vertex(x, y);

            if (alreadyProcessedVertices.containsKey(original.toString())) {
                continue;
            }
            else {
                alreadyProcessedVertices.put(original.toString(), letter);
                for (Map.Entry<Vertex,Character> entryOne : allAnnotatedEntries.entrySet()){
                    int anotherX = entryOne.getKey().getX();
                    int anotherY = entryOne.getKey().getY();
                    Character anotherLetter = entryOne.getValue();
                    if (anotherX == x && anotherY == y) {
                        sameVertexLetter.add(anotherLetter);
                        count++;
                    }
                }
                if (count > 1){
                    for (int i = 0; i < sameVertexLetter.size(); i++){
                        int num = Collections.frequency(sameVertexLetter, sameVertexLetter.get(i));
                        if (num > highest){
                            highest = num;
                            mostFrequentLetter = sameVertexLetter.get(i);
                        }
                        else {
                            mostFrequentLetter = null;
                        }
                    }
                    Vertex newVertex = new Vertex(x, y);
                    finalEntries.put(newVertex, mostFrequentLetter);
                    alreadyProcessedVertices.put(newVertex.toString(), mostFrequentLetter);
                }
                else {
                    Vertex sameVertex = new Vertex(x, y);
                    finalEntries.put(sameVertex, letter);
                    alreadyProcessedVertices.put(sameVertex.toString(), letter);
                }
                sameVertexLetter.clear();
                highest = 0;
            }
        }
        return finalEntries;
    }
}

