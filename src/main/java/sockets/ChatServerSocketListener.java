package sockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ChatServerSocketListener  implements Runnable {
    private ClientConnectionData client;
    private List<ClientConnectionData> clientList;

    public ChatServerSocketListener(ClientConnectionData client, List<ClientConnectionData> clientList) {
        this.client = client;
        this.clientList = clientList;
    }

    private void processChatMessage(MessageCtoS_Chat m) {
        System.out.println("Chat received from " + client.getUserName() + " - broadcasting");
        broadcast(new MessageStoC_Chat(client.getUserName(), m.msg), null);
    }
    private void processListMessage(List<ClientConnectionData> clients) {
        System.out.println("List command received from " + client.getUserName() + " - sent");

        List<String> clientNames = new ArrayList<>();

        for (ClientConnectionData c : clientList) {
            if (!c.getUserName().equals(client.getUserName()))
            clientNames.add(c.getUserName());
        }

        sendMessageToClient(new MessageStoC_List(clientNames), client);
    }
    private void processCatMessage(MessageCtoS_Cat m) {
        System.out.println("Cat command received from " + client.getUserName() + " - broadcasting and sent");
        broadcast(new MessageStoC_Cat(client.getUserName()), client);
        sendMessageToClient(new MessageStoC_Cat("yourself"), client);
    }

    public void processPrivateMessage(MessageCtoS_Private m) {
        try {
            for (ClientConnectionData c : clientList) {
                if (m.to.equals(c.getUserName())) {
                    sendMessageToClient(new MessageStoC_Private(m.msg, c.getUserName(), client.getUserName()),  c); //sending message to user FROM client
                    System.out.println("Sending private message from " + client.getUserName() + " to " + c.getUserName());
                    
                    sendMessageToClient(new MessageStoC_Private(m.msg, c.getUserName(), client.getUserName()), client); //sending message from client to client (to show on screen)
                    System.out.println("Showing private message from " + client.getUserName() + " to " + c.getUserName() + " for " + client.getUserName());

                    return;
                }
            }

            System.out.println("Recieved private message from " + client.getUserName() + ", but cannot find user directed to.");

        }  catch (Exception ex) {
            System.out.println("sending message caught exception: " + ex);
            ex.printStackTrace();
        }    
    }

    public void sendMessageToClient(Message m, ClientConnectionData c) {
        try {
            if ((c.getUserName()!= null)){
                System.out.println("Sent object! to " + c.getUserName());
                c.getOut().writeObject(m);
                } 
            } catch (Exception ex) {
                System.out.println("sending message caught exception: " + ex);
                ex.printStackTrace();
            }    
    }

    /**
     * Broadcasts a message to all clients connected to the server.
     */
    public void broadcast(Message m, ClientConnectionData skipClient) {
        try {
            System.out.println("broadcasting: " + m);
            for (ClientConnectionData c : clientList){
                // if c equals skipClient, then c.
                // or if c hasn't set a userName yet (still joining the server)
                if ((c != skipClient) && (c.getUserName()!= null)){
                    c.getOut().writeObject(m);
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }        
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = client.getInput();

            MessageCtoS_Join joinMessage = (MessageCtoS_Join)in.readObject();
            client.setUserName(joinMessage.userName);
            broadcast(new MessageStoC_Welcome(joinMessage.userName), null);

            while(true) {
                Message m = (Message)in.readObject();
                if (m instanceof MessageCtoS_Quit) {
                    break;
                } else if (m instanceof MessageCtoS_Chat) {
                    processChatMessage((MessageCtoS_Chat) m);
                } else if (m instanceof MessageCtoS_List) {
                    processListMessage(clientList);
                } else if (m instanceof MessageCtoS_Cat) {
                    processCatMessage((MessageCtoS_Cat) m); 
                } else if (m instanceof MessageCtoS_Private) {
                    processPrivateMessage((MessageCtoS_Private) m); 
                }
                else {
                    System.out.println("Unhandled message type: " + m.getClass());
                }

            }
            
        } catch (Exception ex) {
            if (ex instanceof SocketException) {
                System.out.println("Caught socket ex for " + 
                    client.getName());
            } else {
                System.out.println(ex);
                ex.printStackTrace();
            }
        } finally {
            //Remove client from clientList
            clientList.remove(client); 
            

            // Notify everyone that the user left.
            broadcast(new MessageStoC_Exit(client.getUserName()), client);
            //broadcast(new MessageStoC_List(clientList));


            try {
                client.getSocket().close();
            } catch (IOException ex) {}
        }
    }
        
}
