package com.hcmus.student.vanke.mymuseum;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements BeaconConsumer,
        EasyPermissions.PermissionCallbacks
{

    ProgressBar progressBar;
    TextView tvStatusBLT;
    Button btnTurnOnBluetooth;
    Toolbar toolbar;
    PopupWindow mPopupWindow = null;
    LinearLayout linearLayout;
    TextView tvPictureInfo;
    ImageView imgPicture;

    final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BeaconManager mBeaconManager;
    Beacon mBeacon = null;
    final String IBEACON_LAYOUT = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24";
    final String TAG = "scanningBeacon";
    final Relay<Object> rxbus = PublishRelay.create().toSerialized();
    RelativeLayout rootView;
    Disposable disposable;
    final CompositeDisposable subscription = new CompositeDisposable();



    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        stopScanning();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        setButtonText(getResources().getString(R.string.turning_off_bluetooth));
                        progressBar.setVisibility(View.INVISIBLE);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        startScanning();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        setButtonText(getResources().getString(R.string.turning_on_bluetooth));
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        /*Intent intent = new Intent(this, ScanningActivity.class);
        this.startActivity(intent);*/
        progressBar = findViewById(R.id.progress);
        tvStatusBLT = findViewById(R.id.tvStatusBLT);
        btnTurnOnBluetooth = findViewById(R.id.btnTurnOnBluetooth);
        DoubleBounce doubleBounce = new DoubleBounce();
        progressBar.setIndeterminateDrawable(doubleBounce);
        rootView = findViewById(R.id.activity_scanning);
        toolbar = findViewById(R.id.my_toolbar);
        //linearLayout = findViewById(R.id.popup_layout);


        setSupportActionBar(toolbar);

        initBeaconManager();

        if(mBluetoothAdapter.isEnabled()) {
            startScanning();
        }else{
            showAlertBluetoothDisableDialog();
        }

        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_bluetooth_state) {
            turnOnBluetooth();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void initBeaconManager() {
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        // Sets the delay between each scans according to the settings
        mBeaconManager.setForegroundBetweenScanPeriod(200);

        // add only i-beacon to discover
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_LAYOUT));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if(grantResults.length > 0 && String.valueOf(Manifest.permission.ACCESS_COARSE_LOCATION).equals(permissions[0])
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            }
            else{
                stopScanning();
            }
        }
    }

    void startScanning() {
        requestLocationPermission();
        if(!EasyPermissions.hasPermissions(this,
                String.valueOf(Manifest.permission.ACCESS_COARSE_LOCATION))){
            return;
        }

        if(mBluetoothAdapter.isEnabled()) {
            setButtonText(getResources().getString(R.string.stop_scan));
            tvStatusBLT.setText(getResources().getString(R.string.scanning));
            progressBar.setVisibility(View.VISIBLE);
            if (!(mBeaconManager.isBound(this))) {
                Log.d(TAG, "binding beaconManager");
                mBeaconManager.bind(this);
                subScribeRxBus();
            }
        }else {
            showAlertBluetoothDisableDialog();
        }
    }

    void subScribeRxBus() {
        disposable = rxbus.toFlowable(BackpressureStrategy.LATEST)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(e -> Log.d("xxx", "subscribe success!"))
                .filter(e -> e instanceof RangeBeaconEvent && !((RangeBeaconEvent) e).beacons.isEmpty())
                .subscribe(e -> handleScanResult((RangeBeaconEvent) e));
        subscription.add(disposable);
    }

    void handleScanResult(RangeBeaconEvent result) {
        Log.d("xxx | onSubscribe","Handle Scan result");
        //Collection<Beacon> beacons = result.beacons;
        ArrayList<Beacon> beacons = new ArrayList<>(result.beacons);
        if(beacons.size() > 0)
        {
            if(mPopupWindow == null) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                // Inflate the custom layout/view
                if(inflater == null){
                    return;
                }
                View customView = inflater.inflate(R.layout.popup_scan_result, linearLayout);
                tvPictureInfo = customView.findViewById(R.id.tvInfoPicture);
                imgPicture = customView.findViewById(R.id.img_picture);
                mPopupWindow = new PopupWindow(
                        customView,
                        Toolbar.LayoutParams.WRAP_CONTENT,
                        Toolbar.LayoutParams.WRAP_CONTENT
                );
                // Set an elevation value for popup window
                // Call requires API level 21
                if(Build.VERSION.SDK_INT>=21){
                    mPopupWindow.setElevation(5.0f);
                }
                try {
                    mPopupWindow.showAtLocation(customView, Gravity.CENTER, 0, 0);
                }catch (Exception e){
                    Log.d("PopUp",e.getMessage());
                    mPopupWindow = null;
                }
            }

            //the nearest beacon to user
            Beacon goodBeacon = beacons.get(0);

            for(Beacon beacon : beacons) {
                String message = "|name: %s|distance: %s|rssi:%s";
                message = String.format(message, beacon.getBluetoothName(),String.valueOf(beacon.getDistance()),
                        String.valueOf(beacon.getRssi()));
                Log.d("sss", message);

                //get the nearest beacon
                if(beacon.getDistance() < goodBeacon.getDistance()){
                    goodBeacon = beacon;
                }
            }
            mBeacon = goodBeacon;
            if(mPopupWindow.isShowing()){
                String message = mBeacon.getBluetoothName() + "\n" + mBeacon.getId2() + "\n"
                        + mBeacon.getId3();
                tvPictureInfo.setText(message);
                if(mBeacon.getBluetoothName().contains("1")){
                    imgPicture.setImageDrawable(getResources().getDrawable(R.drawable.number_one));
                } else if(mBeacon.getBluetoothName().contains("2")){
                    imgPicture.setImageDrawable(getResources().getDrawable(R.drawable.number_two));
                }else if(mBeacon.getBluetoothName().contains("3")){
                    imgPicture.setImageDrawable(getResources().getDrawable(R.drawable.number_three));
                }
            }

        } else {
            if(mPopupWindow.isShowing()){
                tvPictureInfo.setText("no beacon detected");
                imgPicture.setImageDrawable(getResources().getDrawable(R.drawable.place_holder));
            }
        }
    }

    void stopScanning() {
        setButtonText(getResources().getString(R.string.start_scan));
        if(!EasyPermissions.hasPermissions(this,
                String.valueOf(Manifest.permission.ACCESS_COARSE_LOCATION))) {
            setTvStatusBLTText(getResources().getString(R.string.location_permission_denied));
        }else {
            setTvStatusBLTText(getResources().getString(R.string.scan_stopped));
        }
        progressBar.setVisibility(View.INVISIBLE);
        if (mBeaconManager.isBound(this)) {
            Log.d(TAG, "Unbinding from beaconManager");
            mBeaconManager.unbind(this);
        }
    }

    void setButtonText(String s) {
        btnTurnOnBluetooth.setText(s);
    }

    void setTvStatusBLTText(String s) {
        tvStatusBLT.setText(s);
    }

    void showAlertBluetoothDisableDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Bluetooth is disable")
                .setMessage("Are you sure you want to enable the Bluetooth?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mBluetoothAdapter.enable();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        stopScanning();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    void turnOnBluetooth() {
        if (mBluetoothAdapter.isEnabled()) {
            setTvStatusBLTText(getResources().getString(R.string.bluetooth_disabled));
            mBluetoothAdapter.disable();
        } else {
            mBluetoothAdapter.enable();
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.d(TAG, "beaconManager is bound, ready to start scanning");
        Toast.makeText(getApplicationContext(),"beaconManager is bound, ready to start scanning",
                Toast.LENGTH_SHORT).show();

        mBeaconManager.addRangeNotifier((collection, region) ->{
            rxbus.accept(new RangeBeaconEvent(collection,region));
            //Log.d("xxx | count", String.valueOf(collection.size()));
        } );

        try {
            mBeaconManager.startRangingBeaconsInRegion(new
                    Region("com.hcmus.student.vanke.mymuseum", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        startScanning();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        //stopScanning();
    }

    void requestLocationPermission() {
        if(!EasyPermissions.hasPermissions(this,
                String.valueOf(Manifest.permission.ACCESS_COARSE_LOCATION))) {
            Log.d(TAG, "request access location permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    public void onClickButtonScan(View view) {
        if(mBeaconManager.isBound(this)){
            stopScanning();
        }
        else {
            startScanning();
        }
    }

    @Override
    public void onBackPressed() {
        if(mPopupWindow != null &&mPopupWindow.isShowing()) {
            stopScanning();
            mPopupWindow.dismiss();
            mPopupWindow = null;
            mBeacon = null;
            tvPictureInfo.setText("null");
        }
    }
}
