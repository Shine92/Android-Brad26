package tw.brad.brad26;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter adapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private boolean isBTEnable = false;
    private ArrayList<BluetoothDevice> pairedList;
    private ArrayList<BluetoothDevice> scanList;

    private MyScanReceiver receiver;
    private ListView btlist;
    private SimpleAdapter listAdapter;
    private LinkedList<HashMap<String,String>> data;
    private String[] from = {"btname"};
    private int[] to = {R.id.item_btname};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receiver = new MyScanReceiver();
        btlist = (ListView)findViewById(R.id.btlist);

        // 以下 Android 6+
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},2);
        }

        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null){
            finish();
        }

        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            isBTEnable = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2){}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK){
            isBTEnable = true;
        }
    }

    public void fetchPairedDevices(View v){
        pairedList = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                pairedList.add(device);
                Log.i("brad", device.getName() + ":" + device.getAddress());
            }
        }
    }

    public void scanDevices(View v){
        scanList = new ArrayList<>(); data = new LinkedList<>();
        listAdapter = new SimpleAdapter(this, data,
                R.layout.layout_btdevice,from,to);
        btlist.setAdapter(listAdapter);

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter); // Don't forget to unregister during onDestroy

        adapter.startDiscovery();
    }

    private class MyScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.i("brad", device.getName() + ":" + device.getAddress());

            scanList.add(device);

            HashMap<String,String> aDevice = new HashMap<>();
            aDevice.put(from[0], device.getName());
            data.add(aDevice);

            listAdapter.notifyDataSetChanged();

        }
    }

    public void discoverability(View v){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
