package com.ontariotechu.sofe3980U; // Package declaration

import java.io.FileReader;
import java.util.*; // Importing utilities such as List and ArrayList

import com.opencsv.CSVReader; // Import OpenCSV for reading CSV files
import com.opencsv.CSVReaderBuilder; // Import CSVReaderBuilder to skip the header row

public class App {
    public static void main(String[] args) {
        // List of CSV files representing different models
        String[] modelFiles = {"model_1.csv", "model_2.csv", "model_3.csv"};

        // Variables to keep track of the best F1-score and corresponding model
        double bestF1 = 0;
        String bestModel = "";

        // Loop through each model file to evaluate its performance
        for (String filePath : modelFiles) {
            List<Double> trueLabels = new ArrayList<>(); // List to store true labels
            List<Double> predictedScores = new ArrayList<>(); // List to store predicted probabilities

            try {
                // Read CSV file, skipping the header row
                FileReader filereader = new FileReader(filePath);
                CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
                List<String[]> allData = csvReader.readAll();

                // Parse CSV data into true labels and predicted scores
                for (String[] row : allData) {
                    trueLabels.add(Double.parseDouble(row[0])); // True label
                    predictedScores.add(Double.parseDouble(row[1])); // Predicted probability
                }
            } catch (Exception e) {
                // Handle errors in reading CSV file
                System.out.println("Error reading the CSV file: " + filePath);
                continue; // Skip to the next file
            }

            // Print which model is being evaluated
            System.out.println("Evaluating: " + filePath);

            // Calculate different evaluation metrics
            double bce = calculateBCE(trueLabels, predictedScores); // Binary Cross-Entropy
            int[][] confMatrix = getConfusionMatrix(trueLabels, predictedScores, 0.5); // Confusion Matrix with threshold 0.5
            double accuracy = calculateAccuracy(confMatrix);
            double precision = calculatePrecision(confMatrix);
            double recall = calculateRecall(confMatrix);
            double f1Score = calculateF1Score(precision, recall);
            double auc = calculateAUC(trueLabels, predictedScores); // AUC-ROC Score

            // Print the computed metrics
            System.out.printf("BCE: %.4f, Accuracy: %.4f, Precision: %.4f, Recall: %.4f, F1-score: %.4f, AUC-ROC: %.4f%n",
                    bce, accuracy, precision, recall, f1Score, auc);

            // Keep track of the best model based on the highest F1-score
            if (f1Score > bestF1) {
                bestF1 = f1Score;
                bestModel = filePath;
            }
        }

        // Print the best-performing model based on the F1-score
        System.out.println("Best model based on F1-score: " + bestModel);
    }

    // Function to compute Binary Cross-Entropy (BCE) loss
    public static double calculateBCE(List<Double> yTrue, List<Double> yPred) {
        double bce = 0;
        int n = yTrue.size();
        for (int i = 0; i < n; i++) {
            double y = yTrue.get(i);
            double p = yPred.get(i);
            p = Math.min(Math.max(p, 1e-15), 1 - 1e-15); // Avoid log(0) error
            bce += y * Math.log(p) + (1 - y) * Math.log(1 - p);
        }
        return -bce / n; // Return the averaged BCE loss
    }

    // Function to compute the confusion matrix given a threshold
    public static int[][] getConfusionMatrix(List<Double> yTrue, List<Double> yPred, double threshold) {
        int tp = 0, fp = 0, tn = 0, fn = 0; // Initialize confusion matrix values

        // Iterate over each sample
        for (int i = 0; i < yTrue.size(); i++) {
            int actual = yTrue.get(i).intValue(); // Convert true label to integer (0 or 1)
            int predicted = yPred.get(i) >= threshold ? 1 : 0; // Assign 1 if predicted probability >= threshold, else 0

            // Update confusion matrix counts
            if (actual == 1 && predicted == 1) tp++; // True Positive
            else if (actual == 1 && predicted == 0) fn++; // False Negative
            else if (actual == 0 && predicted == 0) tn++; // True Negative
            else if (actual == 0 && predicted == 1) fp++; // False Positive
        }
        return new int[][]{{tn, fp}, {fn, tp}}; // Return 2x2 confusion matrix
    }

    // Function to calculate accuracy from confusion matrix
    public static double calculateAccuracy(int[][] cm) {
        double correct = cm[0][0] + cm[1][1]; // TN + TP
        double total = correct + cm[0][1] + cm[1][0]; // TN + TP + FP + FN
        return correct / total;
    }

    // Function to calculate precision from confusion matrix
    public static double calculatePrecision(int[][] cm) {
        return cm[1][1] / (double) (cm[1][1] + cm[0][1]); // TP / (TP + FP)
    }

    // Function to calculate recall from confusion matrix
    public static double calculateRecall(int[][] cm) {
        return cm[1][1] / (double) (cm[1][1] + cm[1][0]); // TP / (TP + FN)
    }

    // Function to compute F1-score given precision and recall
    public static double calculateF1Score(double precision, double recall) {
        return 2 * (precision * recall) / (precision + recall); // Harmonic mean of precision and recall
    }

    // Function to compute the AUC-ROC score
    public static double calculateAUC(List<Double> yTrue, List<Double> yPred) {
        List<double[]> sorted = new ArrayList<>(); // Store (score, label) pairs

        // Populate the list with (predicted probability, actual label)
        for (int i = 0; i < yTrue.size(); i++) {
            sorted.add(new double[]{yPred.get(i), yTrue.get(i)});
        }

        // Sort list in descending order based on predicted scores
        sorted.sort((a, b) -> Double.compare(b[0], a[0]));

        int tp = 0, fp = 0, fn = 0, tn = 0;

        // Compute total number of positive (1) and negative (0) labels
        for (double[] pair : sorted) {
            if (pair[1] == 1) fn++; // Count total positives
            else tn++; // Count total negatives
        }

        double prevFPR = 0, prevTPR = 0, auc = 0;

        // Compute True Positive Rate (TPR) and False Positive Rate (FPR)
        for (double[] pair : sorted) {
            if (pair[1] == 1) {
                tp++;
                fn--;
            } else {
                fp++;
                tn--;
            }

            double tpr = tp / (double) (tp + fn); // True Positive Rate
            double fpr = fp / (double) (fp + tn); // False Positive Rate

            // Compute AUC using the trapezoidal rule
            auc += (fpr - prevFPR) * (tpr + prevTPR) / 2;
            prevFPR = fpr;
            prevTPR = tpr;
        }
        return auc; // Return final AUC score
    }
}