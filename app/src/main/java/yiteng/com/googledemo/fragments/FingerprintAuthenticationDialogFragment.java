package yiteng.com.googledemo.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import yiteng.com.googledemo.FingerprintUiHelper;
import yiteng.com.googledemo.MainActivity;
import yiteng.com.googledemo.R;
import yiteng.com.googledemo.util.ToastUtils;

/**
 * Created by neil.zhou on 2016/4/19.
 */
public class FingerprintAuthenticationDialogFragment extends DialogFragment implements FingerprintUiHelper.Callback {
    private Button mCancelButton;
    private Button mPwdButton;
    private View mFingerprintContent;

    private FingerprintUiHelper mFingerprintUiHelper;
    private FingerprintUiHelper.FingerPrintUiHelperBuilder mFingerPrintUiHelperBuilder;
    private FingerprintManager mFingerprintManager;
    private MainActivity mActivity;
    private FingerprintManager.CryptoObject mCryptoObject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerPrintUiHelperBuilder = new FingerprintUiHelper.FingerPrintUiHelperBuilder(mFingerprintManager);
    }

    @Override
    public void onResume() {
        super.onResume();
        mFingerprintUiHelper.startListening(mCryptoObject);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Sign in");
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        mCancelButton = (Button) v.findViewById(R.id.btn_cancel);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mPwdButton = (Button) v.findViewById(R.id.btn_pwd_dialog);
        mPwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showTost(getActivity(), "input pwd!", Toast.LENGTH_SHORT);
                dismiss();
            }
        });
        mFingerprintContent = v.findViewById(R.id.fingerprint_container);
        mFingerprintUiHelper = mFingerPrintUiHelperBuilder.build((ImageView) mFingerprintContent.findViewById(R.id.iv_fingerprint_icon)
                , (TextView) mFingerprintContent.findViewById(R.id.tv_fingerprint_status)
                , this);

        updateUI();
        mFingerprintUiHelper.isFingerprintAuthAvailable();
        return v;
    }

    private void updateUI() {
        mCancelButton.setText(R.string.cancel);
        mPwdButton.setText(R.string.use_password);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    @Override
    public void onAuthenticated() {
        mActivity.onPurchased(true);
        dismiss();
    }

    @Override
    public void onError() {
        mActivity.onPurchased(false);
        dismiss();
    }

    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintUiHelper.stopListening();
    }
}
