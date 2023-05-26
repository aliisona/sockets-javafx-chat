package sockets;

public class MessageStoC_Private extends Message {
    public String msg;
    public String fromUserName;
    public String toUserName;

    public MessageStoC_Private(String msg, String toUserName, String fromUserName) {
        this.msg = msg;
        this.toUserName = toUserName;
        this.fromUserName = fromUserName;

    }
    
}
