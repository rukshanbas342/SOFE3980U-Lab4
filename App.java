package com.ontariotechu.sofe3980U; // Package declaration

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import com.opencsv.CSVReader; // OpenCSV library to read CSV files
import com.opencsv.exceptions.CsvException;
import java.util.Arrays; // Import Arrays utility for printing arrays

public class App {
    public static void main(String[] args) {
        // Define the path to the CSV file
        String filePath = "model.csv";

        // Define the number of classes in the classification problem
        int numClasses = 5; // Classes are indexed from 1 to 5

        // Initialize the confusion matrix (1-based index)
        // The matrix is (numClasses+1) x (numClasses+1) to match class indexing
        int[][] confusionMatrix = new int[numClasses + 1][numClasses + 1];

        // Variables to track total samples and incorrect predictions
        int totalSamples = 0;
        int incorrectPredictions = 0;

        try {
            // Open the CSV file for reading
            FileReader filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReader(filereader);

            // Read all lines from the CSV file
            List<String[]> allData = csvReader.readAll();

            // Close the CSV reader to free resources
            csvReader.close();

            // Process each row in the CSV file (skipping the header)
            for (int i = 1; i < allData.size(); i++) {
                String[] row = allData.get(i); // Read a row of data

                // Parse the true label (first column)
                int trueLabel = Integer.parseInt(row[0]);

                // Initialize variables to find the predicted class with max probability
                double maxProb = -1; // Store the highest probability found
                int predictedLabel = -1; // Store the class corresponding to max probability

                // Iterate over the probability values (columns 1 to numClasses)
                for (int j = 1; j <= numClasses; j++) {
                    double prob = Double.parseDouble(row[j]); // Parse probability
                    if (prob > maxProb) {
                        maxProb = prob; // Update max probability
                        predictedLabel = j; // Store the corresponding class (1-based index)
                    }
                }

                // Update confusion matrix: increment the count for (trueLabel, predictedLabel)
                confusionMatrix[trueLabel][predictedLabel]++;

                // Check if the prediction is incorrect
                if (trueLabel != predictedLabel) {
                    incorrectPredictions++;
                }

                // Increment the total sample count
                totalSamples++;
            }

            // Calculate classification error as the proportion of incorrect predictions
            double classificationError = (double) incorrectPredictions / totalSamples;

            // Print the confusion matrix
            System.out.println("Confusion Matrix:");
            for (int i = 1; i <= numClasses; i++) {
                // Print each row of the matrix, excluding the unused 0 index
                System.out.println(Arrays.toString(Arrays.copyOfRange(confusionMatrix[i], 1, numClasses + 1)));
            }

            // Print the classification error (formatted to 4 decimal places)
            System.out.printf("Classification Error (CE): %.4f%n", classificationError);

        } catch (IOException e) {
            e.printStackTrace(); // Handle and print file reading errors
        }
    }
}