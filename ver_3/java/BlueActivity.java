package com.example.minch.my_semi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BlueActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; // 블루투스 소켓
    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; // 문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼
    private int readBufferPosition; // 버퍼 내 문자 저장 위치



    TextView recieveText, ipTextview, textview_address;
    EditText pwpText, idText, pwText, addressText, portText, del_pwpText;
    Button connectBtn, clearBtn, buttonback, ShowLocationButton;
    String response = "";
    RadioButton new_user, change_user, clear_id;
    RadioGroup select_func;
    static int info=0;
    static int verify=0;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue);
        verify=0;


        //앱 기본 스타일 설정
        getSupportActionBar().setElevation(0);
        buttonback = (Button) findViewById(R.id.buttonback);
        connectBtn = (Button) findViewById(R.id.buttonConnect);
        clearBtn = (Button) findViewById(R.id.buttonClear);
        pwpText = (EditText) findViewById(R.id.pwpText);
        recieveText = (TextView) findViewById(R.id.textViewReciev);
        idText = (EditText) findViewById(R.id.idText);
        pwText = (EditText) findViewById(R.id.pwText);
        select_func = (RadioGroup) findViewById(R.id.select_func);
        del_pwpText = (EditText) findViewById(R.id.del_pwpText);
        del_pwpText.setVisibility(View.GONE);




        buttonback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        // 라디오 그룹을 이용한 정보등록, 정보변경, 정보삭제 옵션 선택
        select_func.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==R.id.new_user){
                    info=0;
                    verify++;
                    del_pwpText.setVisibility(View.GONE);
                    pwpText.setHint("사용할 문장을 입력하세요");
                    Toast.makeText(BlueActivity.this, "신규등록", Toast.LENGTH_SHORT).show();
                }
                if(i==R.id.change_user){
                    info=1;
                    verify++;
                    del_pwpText.setVisibility(View.VISIBLE);
                    pwpText.setHint("새롭게 사용할 문장을 입력하세요");
                    del_pwpText.setHint("기존에 사용하던 문장을 입력하세요");
                    Toast.makeText(BlueActivity.this, "정보변경", Toast.LENGTH_SHORT).show();
                }
                if(i==R.id.clear_id){
                    info=2;
                    verify++;
                    del_pwpText.setVisibility(View.GONE);
                    pwpText.setHint("등록했던 문장을 입력하세요.");
                    Toast.makeText(BlueActivity.this, "정보삭제", Toast.LENGTH_SHORT).show();
                }

            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBluetoothDevice(); //블루투스의 재연결

            }
        });//b_back

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 전송할 사용자 정보의 누락을 막기위해
                String getPwp = pwpText.getText().toString();
                getPwp = getPwp.trim();
                String getId = idText.getText().toString();
                getId = getId.trim();
                String getPw = pwText.getText().toString();
                getPw = getPw.trim();

                if(getPwp.getBytes().length <= 0){
                    Toast.makeText(BlueActivity.this, "문장을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else if(getId.getBytes().length <= 0){
                    Toast.makeText(BlueActivity.this, "ID를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else if(getPw.getBytes().length <= 0){
                    Toast.makeText(BlueActivity.this, "PW를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (verify != 0) {
                        sendData(info, pwpText.getText().toString(), del_pwpText.getText().toString(), idText.getText().toString(), pwText.getText().toString());
                        //messageText.setText("");
                        Toast.makeText(BlueActivity.this, "전송 버튼 클릭", Toast.LENGTH_SHORT).show();//toast메시지 출력
                    } else {
                        Toast.makeText(BlueActivity.this, "신규등록혹은 정보변경을 선택해주세요", Toast.LENGTH_SHORT).show();//toast메시지 출력
                    }
                }
            }
        });

        // 블루투스 활성화하기
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 블루투스 어댑터를 디폴트 어댑터로 설정
        if(bluetoothAdapter == null) { // 디바이스가 블루투스를 지원하지 않을 때
            Toast.makeText(BlueActivity.this, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
        else { // 디바이스가 블루투스를 지원 할 때
            if (bluetoothAdapter.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있음)
                selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
            } else { // 블루투스가 비 활성화 상태 (기기에 블루투스가 꺼져있음)
                // 블루투스를 활성화 하기 위한 다이얼로그 출력
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // 선택한 값이 onActivityResult 함수에서 콜백된다.
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
        }

    }//onCreate


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT :
                if(requestCode == RESULT_OK) { // '사용'을 눌렀을 때
                    selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
                    break;
                }
                else { // '취소'를 눌렀을 때
                    break;
                }
        }

    }

    public void selectBluetoothDevice() {
        // 이미 페어링 되어있는 블루투스 기기를 찾습니다.
        devices = bluetoothAdapter.getBondedDevices();
        // 페어링 된 디바이스의 크기를 저장
        int pariedDeviceCount = devices.size();
        // 페어링 되어있는 장치가 없는 경우
        if(pariedDeviceCount == 0) {
            // 페어링을 하기위한 함수 호출
        }
        // 페어링 되어있는 장치가 있는 경우
        else {
            // 디바이스를 선택하기 위한 다이얼로그 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");
            // 페어링 된 각각의 디바이스의 이름과 주소를 저장
            List<String> list = new ArrayList<>();
            // 모든 디바이스의 이름을 리스트에 추가
            for(BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
            }
            list.add("취소");

            // List를 CharSequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);

            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 해당 디바이스와 연결하는 함수 호출
                    connectDevice(charSequences[which].toString());
                }
            });
            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
            builder.setCancelable(false);
            // 다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }
    public void connectDevice(String deviceName) {
        // 페어링 된 디바이스들을 모두 탐색
        for(BluetoothDevice tempDevice : devices) {
            // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if(deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }
        }
        // UUID 생성
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            // 데이터 송,수신 스트림을 얻어옵니다.
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            // 데이터 수신 함수 호출
            receiveData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveData() {
        final Handler handler = new Handler();
        // 데이터를 수신하기 위한 버퍼를 생성
        readBufferPosition = 0;
        readBuffer = new byte[2048];
        response="";
        recieveText.setText(response);

        // 데이터를 수신하기 위한 쓰레드 생성
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!(Thread.currentThread().isInterrupted())) {
                    try {
                        // 데이터를 수신했는지 확인합니다.
                        int byteAvailable = inputStream.available();
                        // 데이터가 수신 된 경우
                        if(byteAvailable > 0) {
                            // 입력 스트림에서 바이트 단위로 읽어 옵니다.
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);
                            // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.
                            for(int i = 0; i < byteAvailable; i++) {
                                byte tempByte = bytes[i];
                                // 개행문자를 기준으로 받음(한줄)
                                if(tempByte == '\n') {
                                    // readBuffer 배열을 encodedBytes로 복사
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    // 인코딩 된 바이트 배열을 문자열로 변환
                                    final String text = new String(encodedBytes, "UTF8");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // 텍스트 뷰에 출력
                                            response = response + "\n" + text;
                                            recieveText.setText(response);
                                        }
                                    });
                                } // 개행 문자가 아닐 경우
                                else {
                                    readBuffer[readBufferPosition++] = tempByte;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        // 1초마다 받아옴
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        workerThread.start();
    }

    void sendData(int info_send, String pwpm, String del_pwp, String idm, String pwm){
        ;
        String myMessage = "";
        if(info==0 || info ==2){
            myMessage = info_send+"\n"+pwpm+"\n"+"null"+"\n"+idm+"\n"+pwm;
        }
        else{
            myMessage = info_send+"\n"+pwpm+"\n"+del_pwp+"\n"+idm+"\n"+pwm;
        }


        try{
            outputStream.write(myMessage.getBytes());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}


