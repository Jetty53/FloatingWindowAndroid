package com.wheic.floatingedittext;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wheic.floatingedittext.Common.Common;

public class MainActivity extends AppCompatActivity {

    //The reference variables for the
    //Button, AlertDialog, EditText classes are created
    private Button minimizeBtn;
    private AlertDialog dialog;
    private EditText descEditArea;
    private Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //The Buttons and the EditText are connected with
        //the corresponding component id used in layout file
        minimizeBtn = findViewById(R.id.buttonMinimize);
        descEditArea = findViewById(R.id.descEditText);
        save = findViewById(R.id.saveBtn);

        //If the app is started again while the floating window service is running
        //then the floating window service will stop
        if (isMyServiceRunning()) {
            //onDestroy() method in FloatingWindowGFG class will be called here
            stopService(new Intent(MainActivity.this, FloatingWindowGFG.class));
        }

        //currentDesc String will be empty at first time launch
        //but the text written in floating window will not gone
        descEditArea.setText(Common.currentDesc);
        descEditArea.setSelection(descEditArea.getText().toString().length());

        //The EditText string will be stored in currentDesc while writing
        descEditArea.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Not Necessary
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Common.currentDesc = descEditArea.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Not Necessary
            }
        });

        //Here the save button is used just to store the EditText string in saveDesc variable
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.savedDesc = descEditArea.getText().toString();
                descEditArea.setCursorVisible(false);
                descEditArea.clearFocus();
                Toast.makeText(MainActivity.this, "Text Saved!!!", Toast.LENGTH_SHORT).show();
            }
        });

        //The Main Button that helps to minimize the app
        minimizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //First it confirms whether the 'Display over other apps' permission in given
                if (checkOverlayDisplayPermission()) {
                    //FloatingWindowGFG service is started
                    startService(new Intent(MainActivity.this, FloatingWindowGFG.class));
                    //The MainActivity closes here
                    finish();
                } else {
                    //If permission is not given, it shows the AlertDialog box and
                    //redirects to the Settings
                    requestOverlayDisplayPermission();
                }

            }
        });

    }

    private boolean isMyServiceRunning() {
        //The ACTIVITY_SERVICE is needed to retrieve a ActivityManager for interacting with the global system
        //It has a constant String value "activity".
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //A loop is needed to get Service information that are currently running in the System.
        //So ActivityManager.RunningServiceInfo is used. It helps to retrieve a
        //particular service information, here its this service.
        //getRunningServices() method returns a list of the services that are currently running
        //and MAX_VALUE is 2147483647. So at most this many services can be returned by this method.
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            //If this service is found as a running, it will return true or else false.
            if (FloatingWindowGFG.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void requestOverlayDisplayPermission() {
        //An AlertDialog is created
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //This dialog can be closed, just by taping anywhere outside the dialog-box
        builder.setCancelable(true);
        //The title of the Dialog-box is set
        builder.setTitle("Screen Overlay Permission Needed");
        //The message of the Dialog-box is set
        builder.setMessage("Enable 'Display over other apps' from System Settings.");
        //The event of the Positive-Button is set
        builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //The app will redirect to the 'Display over other apps' in Settings.
                //This is an Implicit Intent. This is needed when any Action is needed to perform, here it is
                //redirecting to an other app(Settings).
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                //This method will start the intent. It takes two parameter, one is the Intent and the other is
                //an requestCode Integer. Here it is -1.
                startActivityForResult(intent, RESULT_OK);
            }
        });
        dialog = builder.create();
        //The Dialog will show in the screen
        dialog.show();
    }

    private boolean checkOverlayDisplayPermission() {
        //Android Version is lesser than Marshmallow or the API is lesser than 23
        //doesn't need 'Display over other apps' permission enabling.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //If 'Display over other apps' is not enabled it will return false or else true
            if (!Settings.canDrawOverlays(this)) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

}