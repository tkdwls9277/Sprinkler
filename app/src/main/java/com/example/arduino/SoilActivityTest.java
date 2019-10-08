package com.example.arduino;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil);

        soilValueView = findViewById(R.id.soilValueView);

        connStatusView = findViewById(R.id.connStatusView);
        ipNumberView = findViewById(R.id.ipNumberView);
        portNumberView = findViewById(R.id.portNumberView);

        // new Thread(new ConnectThread()).start();
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
                    final InputStream inputStream = socket.getInputStream();
                    // 만약에 ConnectThread가 사리지지 않고 계속 남아있다면 InputStream이 final로 선언된 형태라서 문제가 발생할 수 있다.
                    // 만약에 첫 스트림으로 40이라는 값이 들어왔으면 다음 스트림으로 어떤 값이 들어오더라도 값 변경이 되지 않을 것이다.
                    isConnected = true;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isConnected) {
                                connStatusView.setText("Connected to Server");
                                ipNumberView.setText("IP Number : " + serverIP);
                                portNumberView.setText("Port Number : " + serverPort);

                                receiverThread = new Thread(new ReceiverThread(inputStream));
                                receiverThread.start();
                            }
                        }
                    });
                }
                catch (IOException e) {
                    Log.e("ConnectThread", e.getMessage());

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
            else {
                Log.e("ConnectThread","Socket is null");
            }

        }
    }

    private class ReceiverThread implements Runnable {
        private InputStream inputStream;
        public ReceiverThread(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                while (isConnected) {

                    int readBufferPosition = 0;
                    byte[] readBuffer = new byte[1024];

                    int byteAvailable = inputStream.available();

                    if (byteAvailable > 0) {
                        byte[] bytes = new byte[byteAvailable];
                        inputStream.read(bytes); // 입력 스트림에서 값을 받아와 bytes 배열에 저장하는 것 같다.

                        for (int i = 0; i < byteAvailable; i++) {
                            byte tempByte = bytes[i];

                            if (tempByte == '\n') { // bytes 배열의 문자를 하나씩 읽어서 개행문자가 나오면
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length); // readBuffer 배열을 encodedBytes 배열로 복사

                                final String text = new String(encodedBytes, "US-ASCII");
                                // 현재 지금 어떤 값이 넘어오는 지 정확하게 알지 못하기 때문에 일단 US-ASCII로 인코딩
                                readBufferPosition = 0;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 추후에 UI에 출력할 형식을 결정해서 이 부분을 수정하도록 하자.

                                        soilValueView.setText(text);
                                    }
                                });
                            } else { // 개행 문자가 아닐 경우
                                readBuffer[readBufferPosition++] = tempByte;
                            }
                        }
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
                } catch (IOException e) {
                    Log.e("ReceiverThread", e.getMessage());
                }
            }
        }

    }
}
