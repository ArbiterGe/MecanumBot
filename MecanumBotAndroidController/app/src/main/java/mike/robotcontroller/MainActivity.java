package mike.robotcontroller;
import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.os.Handler;

import java.io.IOException;
//import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    //    private final String DEVICE_NAME="MyBTBee";
    final String DEVICE_ADDRESS="20:17:03:15:50:59";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    public static OutputStream outputStream;
    //private InputStream inputStream;
    Button stopBtn, connectBtn;
    TextView FLTextView, FRTextView, BLTextView, BRTextView, connectTextView;
    SeekBar leftSeekBar, rightSeekBar, middleSeekBar;
    public static int FL, FR, BL, BR = 90;
    boolean deviceConnected=false;
    //byte buffer[];
    //boolean stopThread;

    static final Integer BLUETOOTH = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stopBtn = (Button) findViewById(R.id.stopBtn);

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leftSeekBar.setProgress(90);
                rightSeekBar.setProgress(90);
                middleSeekBar.setProgress(90);
            }
        });

        connectBtn = (Button) findViewById(R.id.connectBtn);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askForPermission(Manifest.permission.BLUETOOTH, BLUETOOTH);


                if(BTinit())
                {
                    if(BTconnect())
                    {
                        setUiEnabled(true);
                        deviceConnected=true;
                        //beginListenForData();
                        connectTextView.setText("\nConnection Opened!\n");
                        startSendingValues();

                    }
                    else {
                        connectTextView.setText(R.string.BTConnectFailed);
                    }

                }
            }
        });

        FLTextView = (TextView) findViewById(R.id.FLTextView);
        FRTextView = (TextView) findViewById(R.id.FRTextView);
        BLTextView = (TextView) findViewById(R.id.BLTextView);
        BRTextView = (TextView) findViewById(R.id.BRTextView);
        connectTextView = (TextView) findViewById(R.id.connectTextView);

        leftSeekBar = (SeekBar) findViewById(R.id.leftSeekBar);
        leftSeekBar.setProgress(90);
        leftSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                FLTextView.setText(String.valueOf(progress - 90));
                BLTextView.setText(String.valueOf(progress - 90));
                if (progress < 70) progress = 70;
                else if (progress > 110) progress = 110;
                FL = progress;
                BL = progress - (2*(progress - 90));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        rightSeekBar = (SeekBar) findViewById(R.id.rightSeekBar);
        rightSeekBar.setProgress(90);
        rightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                FRTextView.setText(String.valueOf(progress - 90));
                BRTextView.setText(String.valueOf(progress - 90));
                if (progress < 70) progress = 70;
                else if (progress > 110) progress = 110;
                FR = progress;
                BR = progress - (2*(progress - 90));
                //sendMessage(leftSeekBar.getProgress() - 50, progress - 50);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        middleSeekBar = (SeekBar) findViewById(R.id.middleseekBar);
        middleSeekBar.setProgress(90);
        middleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 90) {
                    FLTextView.setText(String.valueOf(progress - 90));
                    BLTextView.setText(String.valueOf(-progress + 90));
                    FRTextView.setText(String.valueOf(progress - 90));
                    BRTextView.setText(String.valueOf(-progress + 90));
                }
                else if (progress > 90) {
                    FLTextView.setText(String.valueOf(progress - 90));
                    BLTextView.setText(String.valueOf(-progress + 90));
                    FRTextView.setText(String.valueOf(-progress + 90));
                    BRTextView.setText(String.valueOf(progress - 90));
                }
                else {
                    FLTextView.setText(String.valueOf(0));
                    BLTextView.setText(String.valueOf(0));
                    FRTextView.setText(String.valueOf(0));
                    BRTextView.setText(String.valueOf(0));
                }
                if (progress < 70) progress = 70;
                else if (progress > 110) progress = 110;
                FL = BL = progress;
                FR = BR = progress - (2*(progress - 90));
                //sendMessage(leftSeekBar.getProgress() - 50, progress - 50);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        setUiEnabled(false);

    }

    public void setUiEnabled(boolean bool)
    {
        connectBtn.setEnabled(!bool);
        stopBtn.setEnabled(bool);
        leftSeekBar.setEnabled(bool);
        FLTextView.setEnabled(bool);
        BLTextView.setEnabled(bool);
        FRTextView.setEnabled(bool);
        BRTextView.setEnabled(bool);
        rightSeekBar.setEnabled(bool);

    }

    public boolean BTinit()
    {
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                //Log.e("Device", iterator.getName() + "   " + iterator.getAddress());
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device=iterator;
                    found=true;
                    break;
                }
            }
            if (!found) connectTextView.setText(R.string.PairDeviceFirst);
        }
        return found;
    }

    public boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //try {
                //inputStream=socket.getInputStream();
            //} catch (IOException e) {
            //    e.printStackTrace();
            //}

        }


        return connected;
    }
/*
    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            handler.post(new Runnable() {
                                public void run()
                                {
                                    connectTextView.setText(string);
                                }
                            });

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }
*/
    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startSendingValues() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                while(true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String msgFinal = FL + ":" + FR + ":" + BL + ":" + BR + '\n';
                                Log.e("Values", msgFinal);
                                outputStream.write(msgFinal.getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(runnable).start();

    }

    //new_value = (old_value - old_bottom) / (old_top - old_bottom) * (new_top - new_bottom) + new_bottom

    //private int mapValue(int X, int oBot, int oTop, int nBot, int nTop) {
    //    return (X - oBot) / (oTop - oBot) * (nTop - nBot) + nBot;
    //}

}
