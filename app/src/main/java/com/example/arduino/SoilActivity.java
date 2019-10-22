package com.example.arduino;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class SoilActivity extends AppCompatActivity {

    private String serverIP = "192.168.43.28"; // IP 수정
    private int serverPort = 8080; // 포트 번호 수정

    private TextView soilValueView;
    private TextView connStatusView;
    private TextView ipNumberView;
    private TextView portNumberView;
    private Button updateBtn;
    private Socket socket;
    private boolean isConnected = false;

    private Thread receiverThread;

    private BufferedReader bufferedReader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil);

        soilValueView = findViewById(R.id.soilValueView);
        connStatusView = findViewById(R.id.sConnStatusView);
        ipNumberView = findViewById(R.id.sIpNumberView);
        portNumberView = findViewById(R.id.sPortNumberView);
        updateBtn = findViewById(R.id.sUpdateButton);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected)
                    new Thread(new SenderThread("S")).start();
                else
                    Toast.makeText(v.getContext(), "Not Connected", Toast.LENGTH_LONG).show();
            }
        });

        new Thread(new ConnectThread(serverIP, serverPort)).start(); // 117.16.152.128
    }
    public void onClickView(View v) {
        if(v.getId()==R.id.menu){
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            Menu menu = popupMenu.getMenu();

            inflater.inflate(R.menu.popupmenu, menu);

            popupMenu.setOnMenuItemClickListener
                    (new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Intent intent;
                            switch (item.getItemId()) {
                                case R.id.mainmenu:
                                    intent = new Intent(SoilActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    return true;
                                case R.id.weathermenu:
                                    intent = new Intent(SoilActivity.this, Weather5days.class);
                                    startActivity(intent);
                                    return true;
                                case R.id.soilmenu:
                                    intent = new Intent(SoilActivity.this, SoilActivity.class);
                                    startActivity(intent);
                                    return true;
                                case R.id.watermenu:
                                    intent = new Intent(SoilActivity.this, WaterActivity.class);
                                    startActivity(intent);
                                    return true;
                            }
                            return false;
                        }
                    });
            popupMenu.show();

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        new Thread(new SenderThread("e")).start();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new Thread(new SenderThread("e")).start();

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

                    if (bufferedReader == null) {
                        Log.e("ReceiverThread", "bufferedReader is null");
                        break;
                    }

                    final String recvMessage = bufferedReader.readLine();
                    // Log.e("ReceiverThread", recvMessage);
                    if (recvMessage != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                soilValueView.setText(recvMessage);
                            }
                        });
                    }

                }
                /*
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("ReceiverThread", e.getMessage());
                }
                */
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

    private class SenderThread implements Runnable {

        private String msg;

        public SenderThread (String msg) {
            this.msg = msg;
        }
        @Override
        public void run() {
            if (isConnected && socket != null){
                try {
                    PrintWriter sendSignal = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
                    sendSignal.println(msg);
                    sendSignal.flush();

                }
                catch (IOException e) {
                    Log.e("SenderThread", e.getMessage());
                }
            }
            else {
                Log.e("SenderThread", "wtf"); // 뒤로가기 버튼을 누르면 (종료코드) 여기로 온다 왜 그럴까
            }

            if (msg.equals("E")) isConnected = false; // 종료 코드
        }
    }
}
