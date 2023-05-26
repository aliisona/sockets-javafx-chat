package sockets;

public class MessageCtoS_Private extends Message {
    public String msg;
    public String to;

    public MessageCtoS_Private(String msg, String to) {
        this.msg = msg;
        this.to = to;
    }
    
}
