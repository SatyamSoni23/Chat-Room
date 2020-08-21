package com.secure.chatroom;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {

    private WebSocket webSocket;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView messageList = findViewById(R.id.messageList);
        final EditText messageBox = findViewById(R.id.messageBox);
        TextView send = findViewById(R.id.send);

        instantiateWebSocket();

        adapter = new MessageAdapter(); //Create Object of message adapter
        messageList.setAdapter(adapter); //Set the adapter of message list to the adapter that we created

        //When send is click then message is send to the server
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageBox.getText().toString();
                if(!message.isEmpty()){
                    webSocket.send(message); //If message is not empty the we will send message to the server
                    messageBox.setText(""); //After sending text remove text from the message box
                    //Now create the JSONobject that will hold two values
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("message", message); //First will be the message that is sent
                        jsonObject.put("byServer", false); //Second is boolean which tells message is sent by the server
                        adapter.addItem(jsonObject); //We can add these item to the adapter
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void instantiateWebSocket() {
        //Create OKhttp client Object
        OkHttpClient client = new OkHttpClient();
        //Build Okhttp request
        Request request = new Request.Builder().url("ws://192.168.43.160:8080").build();
        //Create Object of socket listener and pass Main Activity its parameter
        SocketListner socketListner = new SocketListner(this);
        //Create Web Socket Object pass
        webSocket = client.newWebSocket(request, socketListner);
    }

    //Create SocketListener class that extend websocket listener
    // that implement different methods that will will called after query fires up
    //All thses methods call from background threads we cannot touch directly android views
    public class SocketListner extends WebSocketListener{

        public MainActivity activity;
        //These constructor will make a refrence to main activity class
        public SocketListner(MainActivity activity){
            this.activity = activity;
        }

        //Call when connection establish with the server
        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Connection Successfull Established", Toast.LENGTH_LONG).show();
                }
            });
        }
        //Call when their is a new messages in strings
        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull final String text) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = new JSONObject(); //Create a json object and this object hold the string
                    try {
                        jsonObject.put("message", text);
                        //Tell whether the message is send by the server or not true mean yes message is sent by the server
                        jsonObject.put("byServer", true);
                        //We can add json object to the adapter
                        adapter.addItem(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        //Call when their is a new messages in bytes
        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
        }
        //Call just before the connecton is close
        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosing(webSocket, code, reason);
        }
        //Call when the connection closes
        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);
        }
        //Call when some error occurs
        @Override
        public void onFailure(@NotNull final WebSocket webSocket, @NotNull final Throwable t, @Nullable final Response response) {
            super.onFailure(webSocket, t, response);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Some Error Occur " + t, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    //Create Message Adapter that extends BaseAdapter
    public class MessageAdapter extends BaseAdapter{

        //Our Message is in the form of json object so lets create list of json object
        List<JSONObject> messageList = new ArrayList<>();

        @Override
        public int getCount() {
            return messageList.size(); //return number of items in the list i.e. in messageList
        }

        @Override
        public Object getItem(int i) {
            return messageList.get(i); //return the item at index i;
        }

        @Override
        public long getItemId(int i) {
            return i; //return the position the item
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            // if the view is null then only we will inflated otherwise it result in unpresence scrolling of the messages
            if(view == null){
                view = getLayoutInflater().inflate(R.layout.message_list_item, viewGroup, false);
            }
            TextView sentMessage = view.findViewById(R.id.sentMessage);
            TextView receivedMessage = view.findViewById(R.id.receiveMessage);

            //Obtain the message that we have in the list for this perticular view item
            JSONObject item = messageList.get(i);
            //if message is send by the server
            try {
                if(item.getBoolean("byServer")){
                    receivedMessage.setVisibility(View.VISIBLE);
                    receivedMessage.setText(item.getString("message"));
                    sentMessage.setVisibility(View.INVISIBLE);
                } else{
                    sentMessage.setVisibility(View.VISIBLE);
                    sentMessage.setText(item.getString("message"));
                    receivedMessage.setVisibility(View.INVISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return view; //return the view
        }

        //Create Method additem this will take json object as parameter
        void addItem(JSONObject item){
            messageList.add(item); //Add this item object to messagelist
            notifyDataSetChanged(); //For updating message
        }
    }
}