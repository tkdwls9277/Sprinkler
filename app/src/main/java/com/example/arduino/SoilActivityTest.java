package com.example.arduino;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class SoilActivityTest extends AppCompatActivity {

    private TextView soilValueView;

    private TextView connStatusView;
    private TextView ipNumberView;
    private TextView portNumberView;
    private Socket socket;
    private boolean isConnected = false;

    private Thread receiverThread;

    private BufferedReader bufferedReader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil);

        soilValueView = findViewById(R.id.soilValueView);

        connStatusView = findViewById(R.id.connStatusView);
        ipNumberView = findViewById(R.id.ipNumberView);
        portNumberView = findViewById(R.id.portNumberView);

        new Thread(new ConnectThread("192.168.0.7", 8090)).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isConnected = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        isConnected = false;
    }
    private class ConnectThread implements Runnable {

        private String serverIP;
        private int serverPort;

        public ConnectThread(String ip, int port) {
            serverIP = ip;
            serverPort = port;
            connStatusView.setText("Connecting to " + serverIP + ":" + serverPort);
        }

        @Override
        public void run() {
            try {
                socket = new Socket(serverIP, serverPort);
            }
            catch( UnknownHostException e )
            {
                Log.e("ConnectThread",  "can't find host");
            }
            catch( SocketTimeoutException e )
            {
                Log.e("ConnectThread", "ConnectThread: timeout");
            }
            catch (Exception e) {

                Log.e("ConnectThread", e.getMessage());
            }


            if (socket != null) {
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

                    PrintWriter sendSignal = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
                    sendSignal.println("S");
                    sendSignal.flush();

                    isConnected = true;
                }
                catch (IOException e) {
                    Log.e("ConnectThread", e.getMessage());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isConnected) {
                            connStatusView.setText("Connected to Server");
                            ipNumberView.setText("IP Number : " + serverIP);
                            portNumberView.setText("Port Number : " + serverPort);

                            receiverThread = new Thread(new ReceiverThread());
                            receiverThread.start();
                            Log.e("ConnectThread", "ReceiverThread start");
                        }
                    }
                });

            }
            else {
                Log.e("ConnectThread","Socket is null");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connStatusView.setText("Socket is Null");
                        ipNumberView.setText("F A I L E D");
                        portNumberView.setText("F A I L E D");
                    }
                });
            }

        }
    }

    private class ReceiverThread implements Runnable {

        @Override
        public void run() {
            try {
                while (isConnected) {
                    Log.e("ReceiverThread", "while");
                    if (bufferedReader == null) {
                        Log.e("ReceiverThread", "bufferedReader is null");
                        break;
                    }

                    final String recvMessage = bufferedReader.readLine();
                    Log.e("ReceiverThread", recvMessage);
                    if (recvMessage != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                soilValueView.setText(recvMessage);
                            }
                        });
                    }

                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("ReceiverThread", e.getMessage());
                }

            }
            catch (IOException e) {
                Log.e("ReceiverThread", e.getMessage());
            }

            if (socket != null) {
                try {
                    socket.close();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connStatusView.setText("Socket is Null");
                            ipNumberView.setText("F A I L E D");
                            portNumberView.setText("F A I L E D");
                        }
                    });
                } catch (IOException e) {
                    Log.e("ReceiverThread", e.getMessage());
                }
            }
        }

    }
}
