package com.ledway.btprinter.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.Preference;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import com.ledway.btprinter.BTScanner;
import com.ledway.btprinter.R;
import rx.Subscriber;

/**
 * Created by togb on 2016/5/21.
 */
public class BTPrinterPreference extends Preference {
  private BTScanner btScanner = BTScanner.getDefault();
  public BTPrinterPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public BTPrinterPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public BTPrinterPreference(Context context) {
    super(context);
  }

  @Override protected void onBindView(View view) {
    super.onBindView(view);

    view.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
        builderSingle.setTitle(R.string.select_bt);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
            getContext(),
            android.R.layout.select_dialog_singlechoice);

        builderSingle.setNegativeButton(
            R.string.cancel,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            });
        builderSingle.setAdapter(
            arrayAdapter,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                  String value = arrayAdapter.getItem(which);
                  getEditor().putString(getKey(), value).apply();
                  setSummary(value);
              }
            });
        builderSingle.show();
        btScanner.scan().subscribe(new Subscriber<String>() {
          @Override public void onCompleted() {

          }

          @Override public void onError(Throwable e) {

          }

          @Override public void onNext(String s) {
            arrayAdapter.add(s);
            arrayAdapter.notifyDataSetChanged();
          }
        });
      }
    });
  }
}
