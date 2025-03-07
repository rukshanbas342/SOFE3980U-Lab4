package com.ontariotechu.sofe3980U; // Package declaration

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class App {
    public static void main(String[] args) {
        // Array of CSV filenames representing different models
        String[] files = {"model_1.csv", "model_2.csv", "model_3.csv"};
        
        // Initialize variables to track the best model based on the lowest MSE
        double bestError = Double.MAX_VALUE; // Set to a very high initial value
        String bestModel = "";

        // Loop through each file to evaluate the model
        for (String file : files) {
            // Call evaluateModel to compute error metrics for the current model
            double[] metrics = evaluateModel(file);

            // Print the computed error metrics (MSE, MAE, MARE) for the model
            System.out.printf("Model: %s -> MSE: %.4f, MAE: %.4f, MARE: %.4f%n", 
                              file, metrics[0], metrics[1], metrics[2]);

            // Select the model with the lowest Mean Squared Error (MSE) as the best
            if (metrics[0] < bestError) {
                bestError = metrics[0];
                bestModel = file;
            }
        }

        // Print the best model based on MSE
        System.out.println("Recommended model: " + bestModel);
    }

    /**
     * Evaluates the model by calculating error metrics: MSE, MAE, and MARE.
     * 
     * @param filename Name of the CSV file containing true and predicted values.
     * @return An array containing [MSE, MAE, MARE].
     */
    public static double[] evaluateModel(String filename) {
        String filePath = filename; // Store the filename for reading
        double mse = 0, mae = 0, mare = 0; // Initialize error metrics
        int count = 0; // Counter to track the number of data points

        // Try-with-resources ensures that the BufferedReader is closed automatically
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Read and skip the header line

            // Process each line of the file
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Split the CSV line into values
                double trueValue = Double.parseDouble(values[0]); // Extract true value
                double predictedValue = Double.parseDouble(values[1]); // Extract predicted value

                double error = predictedValue - trueValue; // Compute error

                // Accumulate errors for each metric
                mse += error * error; // Mean Squared Error (sum of squared errors)
                mae += Math.abs(error); // Mean Absolute Error (sum of absolute errors)
                mare += Math.abs(error) / Math.abs(trueValue); // Mean Absolute Relative Error

                count++; // Increment count of processed values
            }
        } catch (IOException e) {
            // Handle file reading errors gracefully
            System.err.println("Error reading file: " + filename);
            e.printStackTrace();
        }

        // Compute final average values if there are any data points
        if (count > 0) {
            mse /= count; // Compute average MSE
            mae /= count; // Compute average MAE
            mare /= count; // Compute average MARE
        }

        // Return an array containing the computed error metrics
        return new double[]{mse, mae, mare};
    }
}