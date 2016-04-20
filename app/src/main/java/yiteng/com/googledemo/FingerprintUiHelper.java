package yiteng.com.googledemo;

import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by neil.zhou on 2016/4/20.
 */
public class FingerprintUiHelper extends FingerprintManager.AuthenticationCallback {
    static final long ERROR_TIMEOUT_MILLIS = 1600;
    static final long SUCCESS_DELAY_MILLIS = 1300;
    private final FingerprintManager mFingerprintManager;
    private final ImageView mImageView;
    private final TextView mErrorTextView;
    private final Callback mCallBack;
    private CancellationSignal mCancellationSignal;

    boolean mSelfCancelled;

    public FingerprintUiHelper(FingerprintManager fingerPrintManager, ImageView icon, TextView errorTextView, Callback callback) {
        mFingerprintManager = fingerPrintManager;
        mImageView = icon;
        mErrorTextView = errorTextView;
        mCallBack = callback;
    }

    public boolean isFingerprintAuthAvailable() {
        if (!mFingerprintManager.hasEnrolledFingerprints()) {
            mErrorTextView.setText(R.string.fingerprint_not_enroll);
            return false;
        }
        if (!mFingerprintManager.isHardwareDetected()){
            mErrorTextView.setText(R.string.fingerprint_hardware_not_support);
            return false;
        }

        return true;
    }

    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        if(!isFingerprintAuthAvailable())
            return;
        mSelfCancelled = false;
        mCancellationSignal = new CancellationSignal();
        mFingerprintManager.authenticate(cryptoObject,mCancellationSignal,0,this,null);
        mImageView.setImageResource(R.drawable.ic_fp_40px);
    }

    public void stopListening(){
        if (mCancellationSignal != null){
            mCancellationSignal.cancel();
            mCancellationSignal = null;
            mSelfCancelled = true;
        }
    }

    public static class FingerPrintUiHelperBuilder{
        private final FingerprintManager mFingerPrintManager;
        public FingerPrintUiHelperBuilder(FingerprintManager fingerprintManager) {
            mFingerPrintManager = fingerprintManager;
        }
        public FingerprintUiHelper build(ImageView icon, TextView errorTextView, Callback callback) {
            return new FingerprintUiHelper(mFingerPrintManager, icon, errorTextView,
                    callback);
        }
    }

    public interface Callback {

        void onAuthenticated();

        void onError();
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        if(!mSelfCancelled) {
            mImageView.postDelayed(new Runnable() {
                @Override
                public void run() {

                    mCallBack.onError();
                }
            }, ERROR_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        showError(helpString);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mErrorTextView.setTextColor(mErrorTextView.getResources().getColor(R.color.success_color,null));
        mErrorTextView.setText(mErrorTextView.getResources().getString(R.string.fingerprint_success));
        mImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCallBack.onAuthenticated();
            }
        },SUCCESS_DELAY_MILLIS);
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        showError(mImageView.getResources().getString(R.string.fingerprint_not_recognized));
    }

    private void showError(CharSequence error){
        mImageView.setImageResource(R.drawable.ic_fingerprint_error);
        mErrorTextView.setText(error);
        mErrorTextView.setTextColor(mErrorTextView.getResources().getColor(R.color.warning_color,null));
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mErrorTextView.postDelayed(mResetErrorTextRunnable,ERROR_TIMEOUT_MILLIS);

    }

    Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            mErrorTextView.setTextColor(mErrorTextView.getResources().getColor(R.color.hint_color,null));
            mErrorTextView.setText(mErrorTextView.getResources().getString(R.string.fingerprint_hint));
            mImageView.setImageResource(R.drawable.ic_fp_40px);
        }
    };
}
