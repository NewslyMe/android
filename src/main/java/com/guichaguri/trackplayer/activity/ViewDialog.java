package com.guichaguri.trackplayer.activity;
import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.ContextCompat;

import com.guichaguri.trackplayer.R;
import com.guichaguri.trackplayer.SeekBar.BubbleSeekBar;
import com.guichaguri.trackplayer.module.NewsItemStorage;
import com.guichaguri.trackplayer.preferences.SharedPrefHelper;

public class ViewDialog {

    private float selectedSpeed;
    public void showSpeedChangeDialog(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog_speed);
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView dialogBtn_cancel =  dialog.findViewById(R.id.txtCancel);
        TextView txtSelectedSpeed =  dialog.findViewById(R.id.txtSelectedSpeed);
        BubbleSeekBar seekBar = dialog.findViewById(R.id.seekbar);

        if(SharedPrefHelper.getSpeed(activity) != 1.0F) {
            selectedSpeed = SharedPrefHelper.getSpeed(activity);
            txtSelectedSpeed.setText(String.valueOf(selectedSpeed));
        }else{
            selectedSpeed = 1.0F;
            txtSelectedSpeed.setText(String.valueOf(1.0));
        }
        seekBar.getConfigBuilder()
                .min(0.50f)
                .max(2.0f)
                .progress(selectedSpeed)
                .sectionCount(15)
                .floatType()
                .secondTrackColor(ContextCompat.getColor(activity, R.color.color_gray))
                .showSectionText()
                .showProgressInFloat()
                .showThumbText()
                .build();

        seekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

                if(fromUser) {
                    txtSelectedSpeed.setText(String.valueOf(progressFloat));
                    CustomView.playerManager.setSpeed(progressFloat);
                    selectedSpeed = progressFloat;
                }
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });
        TextView dialogBtn_okay = (TextView) dialog.findViewById(R.id.txtOk);
        dialogBtn_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefHelper.setSpeed(activity,selectedSpeed);
                dialog.cancel();
            }
        });

        dialogBtn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomView.playerManager.setSpeed(SharedPrefHelper.getSpeed(activity));
                dialog.cancel();
            }
        });

        dialog.show();
    }

}
