package yiteng.com.googledemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import yiteng.com.googledemo.util.Logger;
import yiteng.com.googledemo.util.ToastUtils;

/**
 * Created by neil.zhou on 2016/4/19.
 */
public abstract class BaseActvity extends AppCompatActivity {
    private static String TAG;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = this.getClass().getSimpleName();

        initVariables();
        initViews();
        loadData();
    }

    protected abstract void initVariables();
    protected abstract void initViews();
    protected abstract void loadData();


    protected void showTost(String msg) {
        ToastUtils.showTost(this, msg, Toast.LENGTH_SHORT);
    }

    protected void showLog(String msg) {
        Logger.show(TAG, msg);
    }

}
