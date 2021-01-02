package com.wheic.floatingedittext;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.wheic.floatingedittext.Common.Common;

public class FloatingWindowGFG extends Service {

    //The reference variables for the
    //ViewGroup, WindowManager.LayoutParams, WindowManager, Button, EditText classes are created
    private ViewGroup floatView;
    private int LAYOUT_TYPE;
    private WindowManager.LayoutParams floatWindowLayoutParam;
    private WindowManager windowManager;
    private Button maximizeBtn;
    private EditText descEditArea;
    private Button saveBtn;

    //As FloatingWindowGFG inherits Service class, it actually overrides the onBind method
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //The screen height and width are calculated, cause
        //the height and width of the floating window is set depending on this
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        //To obtain a WindowManager of a different Display,
        //we need a Context for that display, so WINDOW_SERVICE is used
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //A LayoutInflater instance is created to retrieve the LayoutInflater for the floating_layout xml
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        //inflate a new view hierarchy from the floating_layout xml
        floatView = (ViewGroup) inflater.inflate(R.layout.floating_layout, null);

        //The Buttons and the EditText are connected with
        //the corresponding component id used in floating_layout xml file
        maximizeBtn = floatView.findViewById(R.id.buttonMaximize);
        descEditArea = floatView.findViewById(R.id.descEditText);
        saveBtn = floatView.findViewById(R.id.saveBtn);

        //Just like MainActivity, the text written in Maximized will stay
        descEditArea.setText(Common.currentDesc);
        descEditArea.setSelection(descEditArea.getText().toString().length());
        descEditArea.setCursorVisible(false);

        //WindowManager.LayoutParams takes a lot of parameters to set the
        //the parameters of the layout. One of them is Layout_type.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //If API Level is more than 26, we need TYPE_APPLICATION_OVERLAY
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            //If API Level is lesser than 26, then we can use TYPE_SYSTEM_ERROR,
            //TYPE_SYSTEM_OVERLAY, TYPE_PHONE, TYPE_PRIORITY_PHONE. But these are all
            //deprecated in API 26 and later. Here TYPE_TOAST works best.
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST;
        }

        //Now the Parameter of the floating-window layout is set.
        //1) The Width of the window will be 55% of the phone width.
        //2) The Height of the window will be 58% of the phone height.
        //3) Layout_Type is already set.
        //4) Next Parameter is Window_Flag. Here FLAG_NOT_FOCUSABLE is used. But
        //problem with this flag is key inputs can't be given to the EditText.
        //This problem is solved later.
        //5) Next parameter is Layout_Format. System chooses a format that supports translucency by PixelFormat.TRANSLUCENT
        floatWindowLayoutParam = new WindowManager.LayoutParams(
                (int) (width * (0.55f)),
                (int) (height * (0.58f)),
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        //The Gravity of the Floating Window is set. The Window will appear in the center of the screen
        floatWindowLayoutParam.gravity = Gravity.CENTER;
        //X and Y value of the window is set
        floatWindowLayoutParam.x = 0;
        floatWindowLayoutParam.y = 0;

        //The ViewGroup that inflates the floating_layout.xml is
        //added to the WindowManager with all the parameters
        windowManager.addView(floatView, floatWindowLayoutParam);

        //The button that helps to maximize the app
        maximizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stopSelf() method is used to stop the service if
                //it was previously started
                stopSelf();
                //The window is removed from the screen
                windowManager.removeView(floatView);
                //The app will maximize again. So the MainActivity class will be called again.
                Intent backToHome = new Intent(FloatingWindowGFG.this, MainActivity.class);
                //1) FLAG_ACTIVITY_NEW_TASK flag helps activity to start a new task on the history stack.
                //If a task is already running like the floating window service, a new activity will not be started.
                //Instead the task will be brought back to the front just like the MainActivity here
                //2) FLAG_ACTIVITY_CLEAR_TASK can be used in the conjunction with FLAG_ACTIVITY_NEW_TASK. This flag will
                //kill the existing task first and then new activity is started.
                backToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(backToHome);

            }
        });

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

        //Another feature of the floating window is, the window is movable.
        //The window can be moved at any position on the screen.
        floatView.setOnTouchListener(new View.OnTouchListener() {

            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double x;
            double y;
            double px;
            double py;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    //When the window will be touched, the x and y position of that position will be retrieved
                    case MotionEvent.ACTION_DOWN:
                        x = floatWindowLayoutUpdateParam.x;
                        y = floatWindowLayoutUpdateParam.y;
                        //returns the original raw X coordinate of this event
                        px = event.getRawX();
                        //returns the original raw Y coordinate of this event
                        py = event.getRawY();
                        break;
                    //When the window will be dragged around, it will update the x, y of the Window Layout Parameter
                    case MotionEvent.ACTION_MOVE:
                        floatWindowLayoutUpdateParam.x = (int) ((x + event.getRawX()) - px);
                        floatWindowLayoutUpdateParam.y = (int) ((y + event.getRawY()) - py);

                        //updated parameter is applied to the WindowManager
                        windowManager.updateViewLayout(floatView, floatWindowLayoutUpdateParam);
                        break;
                }

                return false;
            }
        });

        //Floating Window Layout Flag is set to FLAG_NOT_FOCUSABLE, so no input is possible to the EditText. But that's a problem.
        //So, the problem is solved here. The Layout Flag is changed when the EditText is touched.
        descEditArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                descEditArea.setCursorVisible(true);
                WindowManager.LayoutParams floatWindowLayoutParamUpdateFlag = floatWindowLayoutParam;
                //Layout Flag is changed to FLAG_NOT_TOUCH_MODAL which helps to take inputs inside floating window, but
                //while in EditText the back button won't work and FLAG_LAYOUT_IN_SCREEN flag helps to keep the window
                //always over the keyboard
                floatWindowLayoutParamUpdateFlag.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                //WindowManager is updated with the Updated Parameters
                windowManager.updateViewLayout(floatView, floatWindowLayoutParamUpdateFlag);
                return false;
            }
        });


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //saves the text in savedDesc variable
                Common.savedDesc = descEditArea.getText().toString();
                descEditArea.setCursorVisible(false);
                WindowManager.LayoutParams floatWindowLayoutParamUpdateFlag = floatWindowLayoutParam;
                floatWindowLayoutParamUpdateFlag.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                //The Layout Flag is changed back to FLAG_NOT_FOCUSABLE. and the Layout is updated with new Flag
                windowManager.updateViewLayout(floatView, floatWindowLayoutParamUpdateFlag);
                //INPUT_METHOD_SERVICE with Context is used to retrieve a InputMethodManager for
                //accessing input methods which is the soft keyboard here
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //The soft keyboard slides back in
                inputMethodManager.hideSoftInputFromWindow(floatView.getApplicationWindowToken(), 0);
                //A Toast is shown when the text is saved
                Toast.makeText(FloatingWindowGFG.this, "Text Saved!!!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //It is called when stopService() method is called in MainActivity
    @Override
    public void onDestroy() {

        super.onDestroy();
        stopSelf();
        //Window is removed from the screen
        windowManager.removeView(floatView);
    }

}
