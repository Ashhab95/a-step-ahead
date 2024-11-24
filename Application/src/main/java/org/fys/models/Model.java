package org.fys.models;

import org.fys.controller.ConcurrencyManager;
import org.fys.utils.*;

import javafx.scene.image.Image;
import java.io.*;
import java.util.*;
import java.net.URISyntaxException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;



public class Model implements Runnable {

    private ConcurrencyManager _cmHandler;

   
    @Override
    public void run() {
        _cmHandler = ConcurrencyManager.getInstance();
        _cmHandler.dt2ctr(new Token(null,Token.MODEL_ON), Token.MODEL_ID);


        //no idea what is this for
        while(!_cmHandler.IsSynchronus())
        {
            try {
                // Sleep for 1000 milliseconds (1 second)
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Handle the InterruptedException (if needed)
                e.printStackTrace();
            }
        }
        //

        while(true)
        {
            Token tk = _cmHandler.lt2view(Token.MODEL_ID);

            if (tk == null) {
                continue;
            }

            switch (tk.read())
            {
                case (Token.INITIALIZE):

                System.out.println(tk.user_input() + "\n");
                String out_nlm = knock_nlm(tk.user_input());
                String view_out = knock_model(out_nlm);
                _cmHandler.dt2view(new Token(view_out, Token.IMAGE_LOADED), Token.MODEL_ID);
                List<String> output = parseStringList(view_out);
                prepare_model("out\\img\\"+output.get(0)+".jpg");
                _cmHandler.dt2view(new Token("", Token.MODEL_LOADED), Token.MODEL_ID); //need to send the name of the obj


                  
                
                break;
            }
        }
        
    }

    
    public String prepare_model(String inputString)
    {
        try {
            // Input string to send to Python
            // Print the input for debugging
            System.out.println("[ctrl]Sending to Python[img3d]: " + inputString + "\n");
        
            // Build the Python process
            ProcessBuilder pb = new ProcessBuilder("python3", "python/img3d.py");
            pb.redirectErrorStream(true);  // Merge stdout and stderr
            Process process = pb.start();
        
            // Write input string to Python's stdin
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(inputString);
                writer.newLine();  // Ensure a newline character to signal end of input
                writer.flush();
            }
        
            // Read Python's output (stdout + stderr)
            StringBuilder outputBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                System.err.println("Error reading from Python process: " + e.getMessage());
                e.printStackTrace();
                return "";
            }
            
            // Check if Python produced output
            String outputJson = outputBuilder.toString().trim();
            if (outputJson.isEmpty()) {
                System.err.println("Error: Python returned no output.");
                return "";
            }

            // Print Python's output for debugging
            System.err.println("\n########################################");
            System.out.println("Python output from [img3d]: " + outputJson);
            System.err.println("########################################\n");
            // Check for errors in Python's stderr (merged with stdout above)
            if (outputJson.contains("Error")) {
                System.err.println("Error detected in Python output: " + outputJson);
                return "";
            }

            // Handle the exit status of the Python process
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Python script exited with code: " + exitCode);
                return "";
            }
            return outputJson;

            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during process execution: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        } 
        return "";
    }


    public String knock_model(String inputString){
        try {
            // Input string to send to Python
            // Print the input for debugging
            System.out.println("[ctrl]Sending to Python[model]: " + inputString + "\n");
        
            // Build the Python process
            ProcessBuilder pb = new ProcessBuilder("python3", "python/model.py");
            pb.redirectErrorStream(true);  // Merge stdout and stderr
            Process process = pb.start();
        
            // Write input string to Python's stdin
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(inputString);
                writer.newLine();  // Ensure a newline character to signal end of input
                writer.flush();
            }
        
            // Read Python's output (stdout + stderr)
            StringBuilder outputBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                System.err.println("Error reading from Python process: " + e.getMessage());
                e.printStackTrace();
                return "";
            }
            
            // Check if Python produced output
            String outputJson = outputBuilder.toString().trim();
            if (outputJson.isEmpty()) {
                System.err.println("Error: Python returned no output.");
                return "";
            }

            // Print Python's output for debugging
            System.err.println("\n########################################");
            System.out.println("Python output from [model]: " + outputJson);
            System.err.println("########################################\n");
            // Check for errors in Python's stderr (merged with stdout above)
            if (outputJson.contains("Error")) {
                System.err.println("Error detected in Python output: " + outputJson);
                return "";
            }

            // Handle the exit status of the Python process
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Python script exited with code: " + exitCode);
                return "";
            }
            return outputJson;

            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during process execution: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        } 
        return "";

    }

    public static List<String> parseStringList(String input) {
        // Remove the surrounding square brackets and any leading/trailing whitespace
        String trimmed = input.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        } else {
            throw new IllegalArgumentException("Input is not in the expected format of a list.");
        }

        // Split the string by commas and remove quotes around each element
        String[] elements = trimmed.split(",");
        List<String> result = new ArrayList<>();

        for (String element : elements) {
            // Remove any surrounding quotes and trim extra spaces
            String cleanedElement = element.trim();
            if (cleanedElement.startsWith("\"") && cleanedElement.endsWith("\"")) {
                cleanedElement = cleanedElement.substring(1, cleanedElement.length() - 1);
            }
            result.add(cleanedElement);
        }

        return result;
    }






    public String knock_nlm(String inputString){

        try {
            // Input string to send to Python
            // Print the input for debugging
            System.out.println("[ctrl]Sending to Python[nlm]: " + inputString + "\n");
        
            // Build the Python process
            ProcessBuilder pb = new ProcessBuilder("python3", "python/nlp.py");
            pb.redirectErrorStream(true);  // Merge stdout and stderr
            Process process = pb.start();
        
            // Write input string to Python's stdin
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(inputString);
                writer.newLine();  // Ensure a newline character to signal end of input
                writer.flush();
            }
        
            // Read Python's output (stdout + stderr)
            StringBuilder outputBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                System.err.println("Error reading from Python process: " + e.getMessage());
                e.printStackTrace();
                return "";
            }
            
            // Check if Python produced output
            String outputJson = outputBuilder.toString().trim();
            if (outputJson.isEmpty()) {
                System.err.println("Error: Python returned no output.");
                return "";
            }

            // Print Python's output for debugging
            System.err.println("\n########################################");
            System.out.println("Python output from [nlm]: " + outputJson);
            System.err.println("########################################\n");
            // Check for errors in Python's stderr (merged with stdout above)
            if (outputJson.contains("Error")) {
                System.err.println("Error detected in Python output: " + outputJson);
                return "";
            }

            // Handle the exit status of the Python process
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Python script exited with code: " + exitCode);
                return "";
            }
            return outputJson;

            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during process execution: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        } 
        return "";
    }
     


    public Model(){}
}



/*
 *

                try {
                    // Input list to send to Python (e.g., a list of integers)
                    List<Integer> inputList = Arrays.asList(1, 2, 3);
        
                    // Convert the list to a JSON string
                    String inputJson = new Gson().toJson(inputList);
        
                    // Print the JSON input for debugging
                    System.out.println("Sending to Python: " + inputJson);
        
                    // Build the Python process
                    ProcessBuilder pb = new ProcessBuilder("python3", "python/nlpConversion.py");
                    pb.redirectErrorStream(true);  // Merge stdout and stderr
                    Process process = pb.start();
        
                    // Write input JSON to Python's stdin
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                        writer.write(inputJson);
                        writer.flush();
                    } catch (IOException e) {
                        System.err.println("Error writing to Python process: " + e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                    
                    // Read Python's output (stdout + stderr)
                    StringBuilder outputBuilder = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            outputBuilder.append(line).append("\n");
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading from Python process: " + e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                    
                    // Check if Python produced output
                    String outputJson = outputBuilder.toString().trim();
                    if (outputJson.isEmpty()) {
                        System.err.println("Error: Python returned no output.");
                        return;
                    }
        
                    // Print Python's output for debugging
                    System.out.println("Python output: " + outputJson);
                    System.err.println("2");
                    // Check for errors in Python's stderr (merged with stdout above)
                    if (outputJson.contains("Error")) {
                        System.err.println("Error detected in Python output: " + outputJson);
                        return;
                    }
        
                    // Handle the exit status of the Python process
                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        System.err.println("Python script exited with code: " + exitCode);
                        return;
                    }
        
                    
        
                
        
                } catch (IOException | InterruptedException e) {
                    System.err.println("Error during process execution: " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    System.err.println("Unexpected error: " + e.getMessage());
                    e.printStackTrace();
                }
                
 */

























