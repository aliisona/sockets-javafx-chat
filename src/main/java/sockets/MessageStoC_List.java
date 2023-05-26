package sockets;

import java.util.List;

public class MessageStoC_List extends Message {
    public List<String> clientNames;

    public MessageStoC_List(List<String> c) {
        this.clientNames = c;
    }

    public String toString() {
        String rtn = "\n";
        for (String name: clientNames) {
            rtn+= "     " + name + "\n";
        }
        return rtn;
    }

}