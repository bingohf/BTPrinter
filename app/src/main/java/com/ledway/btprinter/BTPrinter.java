package com.ledway.btprinter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by togb on 2016/5/21.
 */
public class BTPrinter {
  public static final int STATUS_DISCONNECT = 0;
  public static final int STATUS_CONNECTED = 1;
  public static final int STATUS_CONNECTING = 2;

  private static final UUID MY_UUID_SECURE =
      UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  private final BluetoothDevice device;

  private final String name;
  private final String macAddress;
  private int status = STATUS_DISCONNECT;
  private PublishSubject<Integer> pStatus = PublishSubject.create();
  private PublishSubject<byte[]> pReceiverData = PublishSubject.create();
  private BluetoothSocket bluetoothSocket;
  private OutputStream outputStream;

  public BTPrinter(String name,String macAddress){
    this.name = name;
    this.macAddress = macAddress;
    this.device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.macAddress);
  }
  private synchronized void setStatus(int status){
    if (status != this.status){
      this.status = status;
      pStatus.onNext(status);
    }
  }
  private synchronized int getStatusValue(){
    return this.status;
  }
  private void connectFail() {
    setStatus(STATUS_CONNECTED);
    if(bluetoothSocket != null){
      try {
        bluetoothSocket.close();
        bluetoothSocket = null;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public Observable<Boolean> connect(){
    return Observable.create(new Observable.OnSubscribe<Boolean>() {
      @Override public void call(Subscriber<? super Boolean> subscriber) {
        if (getStatusValue() == STATUS_DISCONNECT){
          try {
            setStatus(STATUS_CONNECTING);
            bluetoothSocket = device
                .createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            setStatus(STATUS_CONNECTED);
            subscriber.onNext(true);
            subscriber.onCompleted();
          } catch (IOException e) {
            e.printStackTrace();
            subscriber.onError(e);
          }
        }else {
          subscriber.onCompleted();
        }
      }
    }).doOnNext(new Action1<Boolean>() {
      @Override public void call(Boolean aBoolean) {
        Observable.create(new Observable.OnSubscribe<Boolean>() {
          @Override public void call(Subscriber<? super Boolean> subscriber) {
            byte[] buffer = new byte[32];
            int bytes;
            try {
              InputStream inputStream = bluetoothSocket.getInputStream();
              while (getStatusValue() == STATUS_CONNECTED) {
                bytes = inputStream.read(buffer);
              }
            } catch (IOException e) {
              e.printStackTrace();
              connectFail();
            }

          }
        }).observeOn(Schedulers.newThread()).subscribe();
      }
    });
  }


  public void disconnect(){
    if (bluetoothSocket != null){
      try {
        bluetoothSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  public Observable<Boolean> sendData(final byte[] bytes){
    return Observable.create(new Observable.OnSubscribe<Boolean>() {
      @Override public void call(Subscriber<? super Boolean> subscriber) {
        if(getStatusValue() == STATUS_CONNECTED){
          try {
            outputStream =bluetoothSocket.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            subscriber.onNext(true);
            subscriber.onCompleted();
          } catch (IOException e) {
            e.printStackTrace();
            subscriber.onError(e);
          }
        }else {
          subscriber.onError(new Exception(MApp.getInstance().getString(R.string.bluetooth_disconnected)));
        }
      }
    });
  }

  public Observable<Integer> getStatus(){
    return Observable.create(new Observable.OnSubscribe<Integer>() {
      @Override public void call(final Subscriber<? super Integer> subscriber) {
        subscriber.onNext(status);
        pStatus.asObservable().subscribe(new Subscriber<Integer>() {
          @Override public void onCompleted() {
            subscriber.onCompleted();
          }

          @Override public void onError(Throwable e) {
            subscriber.onError(e);
          }

          @Override public void onNext(Integer integer) {
            subscriber.onNext(integer);
          }
        });
      }
    });
  }





}
