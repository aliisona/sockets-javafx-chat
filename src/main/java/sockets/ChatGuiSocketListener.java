package sockets;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.control.TextInputDialog;

public class ChatGuiSocketListener implements Runnable {

    private ObjectInputStream socketIn;
    private ChatGuiClient chatGuiClient;
    
    private String username = null;

    // volatile guarantees that different threads reading the same variable will
    // always see the latest write.
    // volatile variables are not stored in caches.
    volatile boolean appRunning = false;

    public ChatGuiSocketListener(ObjectInputStream socketIn,
            ChatGuiClient chatClient) {
        this.socketIn = socketIn;
        this.chatGuiClient = chatClient;
    }

    private void processWelcomeMessage(MessageStoC_Welcome m) {
        String user = m.userName;

        if (user.equals(this.username)) {
            Platform.runLater(() -> {
                chatGuiClient.getStage().setTitle("Chatter - " + username);
                chatGuiClient.getTextInput().setEditable(true);
                chatGuiClient.getSendButton().setDisable(false);
                chatGuiClient.getCatButton().setDisable(false);
                chatGuiClient.getMessageArea().appendText("Welcome to the chat, " + username + "\n");
            });
        }

        else {
            Platform.runLater(() -> {
                chatGuiClient.getMessageArea().appendText(m.userName + " joined the chat!\n");
            });
        }
        chatGuiClient.sendMessage(new MessageCtoS_List(user));
    }

    private void processListMessage(MessageStoC_List m) {
        Platform.runLater(() -> {

        for (String name : m.clientNames) {
            //System.out.println(name + " contained? " + m.clientNames.contains(name));

            if (!chatGuiClient.getNames().contains(name)) { //if name doesnt  exist
                chatGuiClient.getNames().add(name);
                Collections.sort(chatGuiClient.getNames());
            }
        }
        //System.out.println(m.clientNames);
       // System.out.println(chatGuiClient.getNames());

        });

    }

    private void processChatMessage(MessageStoC_Chat m) {
        Platform.runLater(() -> {
            chatGuiClient.getMessageArea().appendText("[Public] " + m.userName + ": " + m.msg + "\n");
        });
    }

    private void processExitMessage(MessageStoC_Exit m) {
        Platform.runLater(() -> {
            chatGuiClient.getMessageArea().appendText(m.userName + " has left the chat!\n");
            chatGuiClient.getNames().remove(m.userName);
        });
    }

    private void processPrivateMessage(MessageStoC_Private m) {
        Platform.runLater(() -> {
            if (m.fromUserName.equals(username)) { //client sent itself the message
                chatGuiClient.getMessageArea().appendText("[Private to " + m.toUserName + "]: " + m.msg + "\n");
            } else {
                chatGuiClient.getMessageArea().appendText("[Private from " + m.fromUserName + "]: " + m.msg + "\n");
            }
            
        });
    }
    private void processCatMessage(MessageStoC_Cat m) {
        Platform.runLater(() -> {
            chatGuiClient.getMessageArea().appendText("Just got cat'd by " + m.userName + "!\n\n" + m);
        });
    }

    public void run() {
        try {
            appRunning = true;

            // Ask the gui to show the username dialog and update username
            // getName launches a modal, therefore needs to run in the JavaFx thread, hence
            // wrapping this with run later
            // save the username for later comparison in processWelcomeMessage
            // Send to the server

            Platform.runLater(() -> {
                this.username = getName();
                chatGuiClient.sendMessage(new MessageCtoS_Join(username));
            });

            // If the user closes the UI window in ChatGuiClient, appRunning is changed to
            // false to exit this loop.
            while (appRunning) {
                Message msg = (Message) socketIn.readObject();

                if (msg instanceof MessageStoC_Welcome) {
                    processWelcomeMessage((MessageStoC_Welcome) msg);
                } else if (msg instanceof MessageStoC_Chat) {
                    processChatMessage((MessageStoC_Chat) msg);
                } else if (msg instanceof MessageStoC_Exit) {
                    processExitMessage((MessageStoC_Exit) msg);
                }
                else if (msg instanceof MessageStoC_List) {
                    processListMessage((MessageStoC_List) msg);
                }
                else if (msg instanceof MessageStoC_Private) {
                    processPrivateMessage((MessageStoC_Private) msg);
                } 
                else if (msg instanceof MessageStoC_Cat) {
                    processCatMessage((MessageStoC_Cat) msg);
                } 
                else {
                    System.out.println("Unhandled message type: " + msg.getClass());
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception caught in listener - " + ex);
        } finally {
            System.out.println("Client Listener exiting");
        }
    }

    private String getName() {
        String username = "";
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Enter Chat Name");
        nameDialog.setHeaderText("Please enter your username.");
        nameDialog.setContentText("Name: ");

        while (username.equals("")) {
            Optional<String> name = nameDialog.showAndWait();
            if (!name.isPresent() || name.get().trim().equals(""))
                nameDialog.setHeaderText("You must enter a nonempty name: ");
            else
                username = name.get().trim();
        }
        return username;
    }

}
