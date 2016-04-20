package yiteng.com.googledemo.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by neil.zhou on 2016/4/19.
 */
public class ToastUtils {
    private static Toast mToast;
    public static void showTost(Context context,CharSequence text,int duration){
        if(mToast == null){
            mToast = Toast.makeText(context,text,duration);
        }else {
            mToast.setText(text);
            mToast.setDuration(duration);
        }
        mToast.show();
    }
}
