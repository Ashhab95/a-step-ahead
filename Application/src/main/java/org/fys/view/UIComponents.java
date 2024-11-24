package org.fys.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.fys.controller.ConcurrencyManager;
import org.fys.utils.Token;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UIComponents extends Application {

    private ImageView imageView;
    private List<String> lin;
    private ConcurrencyManager _cmHandler;

    @Override
public void start(Stage primaryStage) {
    _cmHandler = ConcurrencyManager.getInstance();
    _cmHandler.dt2ctr(new Token(null, Token.VIEW_ON), Token.VP_ID);

    // Main layout (StackPane)
    StackPane root = new StackPane();

    // Title label
    Label titleLabel = new Label("Find Your Dream Shoes");
    titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #000000;");

    // Chat display area (non-editable text area)
    TextArea chatDisplay = new TextArea();
    chatDisplay.setEditable(false); // Users cannot modify chat history
    chatDisplay.setWrapText(true);
    chatDisplay.setPrefHeight(300); // Height for chat display
    chatDisplay.setPrefWidth(300);  // Width to match chatbox
    chatDisplay.setStyle("-fx-control-inner-background: #F8F8F8;"); // Slightly off-white background

    // Input field for user messages with placeholder text
    TextField userInput = new TextField();
    userInput.setPromptText("Enter your text here...");
    userInput.setStyle("-fx-control-inner-background: #F8F8F8;"); // Slightly off-white background

    // Clear button to clear chat history
    Button clearButton = new Button("Clear Chat");
    clearButton.setOnAction(e -> chatDisplay.clear());

    // Input container (TextField and Clear Button)
    HBox inputContainer = new HBox(5, userInput, clearButton);
    inputContainer.setAlignment(Pos.CENTER);
    inputContainer.setPadding(new Insets(5, 10, 5, 5)); // Top, Right, Bottom, Left
    HBox.setHgrow(userInput, Priority.ALWAYS); // Make the userInput field grow horizontally

    // Chatbox container with adjusted height
    VBox chatBox = new VBox(5, titleLabel, chatDisplay, inputContainer); // Added inputContainer
    chatBox.setAlignment(Pos.BOTTOM_CENTER);
    chatBox.setStyle(
        "-fx-background-color: #f5f5f5; " + // Gray outer layer
        "-fx-border-color: #ccc; " +
        "-fx-border-width: 1px; " +
        "-fx-background-radius: 10px; " + // Rounded corners for background
        "-fx-border-radius: 10px;"        // Rounded corners for border
    );
    chatBox.setPadding(new Insets(5));      // Minimal padding around the chatbox
    chatBox.setMaxWidth(300);               // Limit the width of the chatbox
    chatBox.setMaxHeight(375);              // Max height as before

    // Image display area
    imageView = new ImageView();
    imageView.setFitWidth(200); // Set a larger width for the image
    imageView.setPreserveRatio(true);
    imageView.setVisible(false); // Initially hidden

    VBox imageBox = new VBox(imageView);
    imageBox.setAlignment(Pos.TOP_CENTER); // Center the image at the top
    imageBox.setPadding(new Insets(20, 0, 0, 0));

    // StackPane setup: Centering the image
    StackPane.setAlignment(imageBox, Pos.CENTER);
    root.getChildren().add(imageBox);

    // Align the chatbox to the bottom-center
    StackPane.setAlignment(chatBox, Pos.BOTTOM_CENTER);
    StackPane.setMargin(chatBox, new Insets(10, 0, 50, 0)); // Adjust bottom margin to prevent overlap
    root.getChildren().add(chatBox);

    listen2Model(chatDisplay);

    // Scene and Stage
    Scene scene = new Scene(root, 600, 400); // Window size as before
    primaryStage.setTitle("ChatBox");
    primaryStage.setScene(scene);
    primaryStage.show();

    // Event handler for user input
    userInput.setOnAction(e -> {
        String message = userInput.getText();
        if (!message.isBlank()) {
            chatDisplay.appendText("You: " + message + "\n");
            userInput.clear();
            chatDisplay.setScrollTop(Double.MAX_VALUE); // Auto-scroll to bottom

            // Send user input to backend and get image path
            dispatchMessage2Model(new Token(message, Token.INITIALIZE));
        }
    });
}

    private void listen2Model(TextArea chatDisplay) {
        new Thread(() -> {
            while (true) {
                Token tk = _cmHandler.lt2model(Token.VP_ID);
                if (tk.read() == Token.IMAGE_LOADED) {
                    Platform.runLater(() -> {
                        lin = parseStringList(tk.user_input());
                        displayBackendMessages(lin.subList(1, lin.size()), chatDisplay);
                        String name = lin.get(0) + ".jpg";
                        System.out.println(name);
                        displayImage(name);
                    });
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public static List<String> parseStringList(String input) {
        String trimmed = input.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        } else {
            throw new IllegalArgumentException("Input is not in the expected format of a list.");
        }

        String[] elements = trimmed.split(",");
        List<String> result = new ArrayList<>();
        for (String element : elements) {
            String cleanedElement = element.trim();
            if (cleanedElement.startsWith("\"") && cleanedElement.endsWith("\"")) {
                cleanedElement = cleanedElement.substring(1, cleanedElement.length() - 1);
            }
            result.add(cleanedElement);
        }

        return result;
    }

    public void dispatchMessage2Model(Token tk) {
        if (tk != null) {
            _cmHandler.dt2model(tk, Token.VP_ID);
        }
    }

    private void displayImage(String imagePath) {
        File file = new File("out\\img\\" + imagePath);

        if (!file.exists()) {
            System.out.println("Invalid file. Image not found: " + imagePath);
            return;
        }

        Image image = new Image(file.toURI().toString());
        imageView.setImage(image);
        imageView.setVisible(true);
    }

    private void displayBackendMessages(List<String> messages, TextArea chatDisplay) {
        for (String message : messages) {
            chatDisplay.appendText("Bot: " + message + "\n");
        }
        chatDisplay.setScrollTop(Double.MAX_VALUE);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
