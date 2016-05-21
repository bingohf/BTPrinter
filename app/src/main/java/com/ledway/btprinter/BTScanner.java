package com.ledway.btprinter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.Set;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by togb on 2016/5/21.
 */
public class BTScanner {
  private final BluetoothAdapter mBtAdapter;
  private Context context;
  private   BroadcastReceiver mReceiver;
  private static BTScanner instance;
  private BTScanner(Context context){
    this.context = context;
    mBtAdapter = BluetoothAdapter.getDefaultAdapter();
  }

  public static BTScanner getDefault(){
    if (instance == null){
      instance = new BTScanner(MApp.getInstance());
    }
    return instance;
  }

  public Observable<String> scan(){
    return Observable.create(new Observable.OnSubscribe<String>() {
      @Override public void call(final Subscriber<? super String> subscriber) {
      mReceiver = new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
              // Get the BluetoothDevice object from the Intent
              BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
              // If it's already paired, skip it, because it's been listed already
              if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                subscriber.onNext(device.getName() + " / " + device.getAddress());
              }
              // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
              subscriber.onCompleted();
            }
          }
        };

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mReceiver, filter);
        if(mBtAdapter.isDiscovering()) {
          mBtAdapter.cancelDiscovery();
        }
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        for(BluetoothDevice bluetoothDevice: pairedDevices){
          subscriber.onNext(bluetoothDevice.getName() +" / " + bluetoothDevice.getAddress());
        }
        mBtAdapter.startDiscovery();
        subscriber.add(Subscriptions.create(new Action0() {
          @Override public void call() {
            if(mBtAdapter.isDiscovering()) {
              mBtAdapter.cancelDiscovery();
            }
            context.unregisterReceiver(mReceiver);
          }
        }));

      }
    }).doOnCompleted(new Action0() {
      @Override public void call() {
        context.unregisterReceiver(mReceiver);
      }
    });
  }

}
