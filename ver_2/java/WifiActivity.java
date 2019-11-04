package com.example.minch.my_semi;


import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class WifiActivity extends AppCompatActivity {

    TextView recieveText;
    EditText pwpText, idText, pwText, addressText, portText, del_pwpText;
    Button connectBtn, clearBtn, buttonback;
    String response = "";
    RadioButton new_user, change_user;
    RadioGroup select_func;
    static int info=0;
    static int verify=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        verify=0;

        //앱 기본 스타일 설정
        getSupportActionBar().setElevation(0);
        buttonback = (Button) findViewById(R.id.buttonback);
        addressText = (EditText) findViewById(R.id.ipAdd);
        connectBtn = (Button) findViewById(R.id.buttonConnect);
        pwpText = (EditText) findViewById(R.id.pwpText);
        recieveText = (TextView) findViewById(R.id.textViewReciev);
        idText = (EditText) findViewById(R.id.idText);
        pwText = (EditText) findViewById(R.id.pwText);
        select_func = (RadioGroup) findViewById(R.id.select_func);
        portText = (EditText) findViewById(R.id.portNum);
        clearBtn = (Button) findViewById(R.id.clearBtn);
        del_pwpText = (EditText) findViewById(R.id.del_pwpText);
        del_pwpText.setVisibility(View.GONE);


        select_func.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==R.id.new_user){
                    info=0;
                    verify++;
                    del_pwpText.setVisibility(View.GONE);
                    pwpText.setHint("사용할 문장을 입력하세요");
                    Toast.makeText(WifiActivity.this, "신규등록", Toast.LENGTH_SHORT).show();
                }
                if(i==R.id.change_user){
                    info=1;
                    verify++;
                    del_pwpText.setVisibility(View.VISIBLE);
                    pwpText.setHint("새롭게 사용할 문장을 입력하세요");
                    del_pwpText.setHint("기존에 사용하던 문장을 입력하세요");
                    Toast.makeText(WifiActivity.this, "정보변경", Toast.LENGTH_SHORT).show();
                }
                if(i==R.id.clear_id){
                    info=2;
                    verify++;
                    del_pwpText.setVisibility(View.GONE);
                    pwpText.setHint("등록했던 문장을 입력하세요.");
                    Toast.makeText(WifiActivity.this, "정보삭제", Toast.LENGTH_SHORT).show();
                }

            }
        });

        buttonback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        //connect 버튼 클릭
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // HOST ip,PORT, Message 전달 -> myClientTask
                String getIp = addressText.getText().toString();
                getIp = getIp.trim();
                String getPort = portText.getText().toString();
                getPort = getPort.trim();
                String getPwp = pwpText.getText().toString();
                getPwp = getPwp.trim();
                String getId = idText.getText().toString();
                getId = getId.trim();
                String getPw = pwText.getText().toString();
                getPw = getPw.trim();
                if(getIp.getBytes().length <= 0 || getIp.getBytes().length > 14 || getIp.getBytes().length < 7){//빈값이 넘어올때의 처리
                    Toast.makeText(WifiActivity.this, "IP를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else if(getPort.getBytes().length <= 0 || getPort.getBytes().length > 4 || getPort.getBytes().length < 4){
                    Toast.makeText(WifiActivity.this, "Port를 입력하세요."+getIp.getBytes().length, Toast.LENGTH_SHORT).show();
                }
                else if(getPwp.getBytes().length <= 0){
                    Toast.makeText(WifiActivity.this, "문장을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else if(getId.getBytes().length <= 0){
                    Toast.makeText(WifiActivity.this, "ID를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else if(getPw.getBytes().length <= 0){
                    Toast.makeText(WifiActivity.this, "PW를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(verify != 0){
                        MyClientTask myClientTask = new MyClientTask(info, addressText.getText().toString(), Integer.parseInt(portText.getText().toString()), pwpText.getText().toString(), del_pwpText.getText().toString(), idText.getText().toString(), pwText.getText().toString());
                        myClientTask.execute();
                        //messageText.setText("");
                        Toast.makeText(WifiActivity.this, "로그인 버튼 클릭", Toast.LENGTH_SHORT).show();//toast메시지 출력
                    }
                    else{
                        Toast.makeText(WifiActivity.this, "신규등록혹은 정보변경을 선택해주세요", Toast.LENGTH_SHORT).show();//toast메시지 출력
                    }
                }


            }
        });

        //clear 버튼 클릭
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recieveText.setText("");
                idText.setText("");
                pwText.setText("");
                Toast.makeText(WifiActivity.this, "초기화 버튼 클릭", Toast.LENGTH_SHORT).show();
            }
        });

    }



    public class MyClientTask extends AsyncTask<Void, Void, Void> {
        String dstAddress;
        int dstPort;
        String myMessage = "";

        //constructor
        MyClientTask(int info_send, String addr, int port, String pwpm, String del_pwp, String idm, String pwm){
            dstAddress = addr;
            dstPort = port;
            if(info==0){
                myMessage = info_send+"\n"+pwpm+"\n"+""+"\n"+idm+"\n"+pwm;
            }
            else{
                myMessage = info_send+"\n"+pwpm+"\n"+del_pwp+"\n"+idm+"\n"+pwm;
            }

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Socket socket = new Socket();
            response = "";
            myMessage = myMessage.toString();
            try {
                SocketAddress addr = new InetSocketAddress(dstAddress, dstPort);
                socket.connect(addr);

                //수신
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream inputStream = socket.getInputStream();
                //송신
                OutputStream out = socket.getOutputStream();
                out.write(myMessage.getBytes());

    /*
     * notice:
     * inputStream.read() will block if no data return
     */
                while ((bytesRead = inputStream.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString();

                }
                response = "서버의 응답: " + response;

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response += "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response += "IOException: " + e.toString() + "내부적";

            } catch (SecurityException se) {
                se.printStackTrace();
                response += "SecurityException: " +se.toString();
            }finally{
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        response += "IOException: " + e.toString() + "소켓 생성 실패";
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            recieveText.setText(response);
            super.onPostExecute(result);
            Toast.makeText(WifiActivity.this, response, Toast.LENGTH_SHORT).show();
        }

    }

}