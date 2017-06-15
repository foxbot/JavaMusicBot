package ovh.not.javamusicbot.rpc;

public class Message {
    public int op;
    public Object data;

    public Message(int op, Object data) {
        this.op = op;
        this.data = data;
    }
}
