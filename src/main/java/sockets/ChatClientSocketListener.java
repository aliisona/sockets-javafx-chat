package sockets;

import java.io.ObjectInputStream;

public class ChatClientSocketListener implements Runnable {
    private ObjectInputStream socketIn;

    public ChatClientSocketListener(ObjectInputStream socketIn) {
        this.socketIn = socketIn;
    }

    private void processWelcomeMessage(MessageStoC_Welcome m) {
        System.out.println(m.userName + " joined the chat!");
    }

    private void processChatMessage(MessageStoC_Chat m) {
        System.out.println(m.userName + ": " + m.msg);
    }

    private void processExitMessage(MessageStoC_Exit m) {
        System.out.println(m.userName + " left the chat!");
    }
    private void processListMessage(MessageStoC_List m) {
        System.out.println("Current list: " + m);
    }
    private void processCatMessage(MessageStoC_Cat m) {
        System.out.println("Just got cat'd by " + m.userName + "!\n\n" + m);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message msg = (Message) socketIn.readObject();
                if (msg instanceof MessageStoC_Welcome) {
                    processWelcomeMessage((MessageStoC_Welcome) msg);
                }
                else if (msg instanceof MessageStoC_Chat) {
                    processChatMessage((MessageStoC_Chat) msg);
                }
                else if (msg instanceof MessageStoC_Exit) {
                    processExitMessage((MessageStoC_Exit) msg);
                }
                else if (msg instanceof MessageStoC_List) {
                    processListMessage((MessageStoC_List) msg);
                }
                else if (msg instanceof MessageStoC_Cat) {
                    processCatMessage((MessageStoC_Cat) msg);
                }
            }
            
        } catch (Exception ex) {
            System.out.println("Exception caught in listener - " + ex);
        } finally{
            System.out.println("Client Listener exiting");
        }
    }
}
