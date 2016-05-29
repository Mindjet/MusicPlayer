package com.mindjet.com.musicplayer.Utils;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.mindjet.com.musicplayer.Constant.AppConstant;
import com.mindjet.com.musicplayer.Constant.PlayerSource;

/**
 * @author Mindjet
 * @date 2016/5/26
 */
public class PhoneCallListener extends PhoneStateListener{


    private Context context;

    public PhoneCallListener(Context context) {
        this.context = context;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {

        Intent intent = new Intent();
        intent.setAction("com.mindjet.meida.MUSIC_SERVICE");

        switch (state){

            case TelephonyManager.CALL_STATE_IDLE:
                intent.putExtra("MSG", AppConstant.PlayerMsg.CONTINUE_MSG);
                context.startService(intent);
                PlayerSource.isPlaying = true;
                PlayerSource.isPause = false;
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
            case TelephonyManager.CALL_STATE_RINGING:
                intent.putExtra("MSG",AppConstant.PlayerMsg.PAUSE_MSG);
                context.startService(intent);
                PlayerSource.isPause = true;
                PlayerSource.isPlaying = false;
                break;

        }

    }
}
