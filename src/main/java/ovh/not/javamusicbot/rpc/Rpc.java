package ovh.not.javamusicbot.rpc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ovh.not.javamusicbot.Config;
import ovh.not.javamusicbot.ShardManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Rpc {
    private final Config config;
    private final ShardManager manager;
    private final Gson gson;
    private Socket socket;

    public Rpc(Config config, ShardManager manager) {
        this.config = config;
        this.manager = manager;
        this.gson = new Gson();
    }

    public void connect() throws IOException {
        this.socket = new Socket(config.master, config.masterPort);
        Thread thread = new Thread(() -> {
            try {
                runLoop();
            } catch (Exception e) {
                return;
            }
        });
        thread.start();
        sendIdentify();
    }

    void runLoop() throws IOException {
        while(true) {

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String content = reader.readLine();
                Message message = gson.fromJson(content, Message.class);
                switch (message.op) {
                    case 1: {
                        System.out.println("RPC up");
                    }
                    case 2: {
                        System.out.println("!!! RPC key invalid !!! this instance running without RPC");
                        return;
                    }
                    default: {
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (socket.isClosed())
            {
                break;
            }
        }
        connect();
    }
    void sendIdentify() throws IOException {
        Message message = new Message(0, new Identify(config.masterKey));
        String json = gson.toJson(message);
        socket.getOutputStream().write(json.getBytes());
    }
}
