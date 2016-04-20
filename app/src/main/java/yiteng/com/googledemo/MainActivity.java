package yiteng.com.googledemo;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import yiteng.com.googledemo.fragments.FingerprintAuthenticationDialogFragment;

public class MainActivity extends BaseActvity {
    Toolbar mToolbar;
    Button mPurchaseBtn;

    private KeyguardManager mKeyguardManager;
    private FingerprintManager mFingerprintManager;
    private KeyStore mKeySotre;
    private KeyGenerator mKeyGenerator;
    private Cipher mCipher;
    private SharedPreferences mSharedPreferences;
    private FingerprintAuthenticationDialogFragment mFragment;

    private static final String SECRET_MESSAGE = "Very secret message";
    /**
     * Alias for our key in the Android Key Store
     */
    private static final String KEY_NAME = "my_key";
    private static final String DIALOG_FRAGMENT_TAG = "myFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initVariables() {
        mKeyguardManager = this.getSystemService(KeyguardManager.class);
        mFingerprintManager = this.getSystemService(FingerprintManager.class);
        try {
            mKeySotre = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }
        try {
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
        }
        try {
            mCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }
        mFragment = new FingerprintAuthenticationDialogFragment();
    }

    @Override
    protected void initViews() {
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.tb_toolbar);
        setSupportActionBar(mToolbar);

        initPurchase();
        mPurchaseBtn = (Button) findViewById(R.id.btn_purchase);
        mPurchaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchase();
            }
        });
    }

    private void initPurchase() {
        if (!mKeyguardManager.isKeyguardSecure()) {
            showTost("Secure lock screen hasn't set up.\n"
                    + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint");
            mPurchaseBtn.setEnabled(false);
            return;
        }
        if (!mFingerprintManager.hasEnrolledFingerprints()) {
            mPurchaseBtn.setEnabled(false);
            showTost("Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint");
            return;
        }
        createKey();
    }

    private void purchase() {
        findViewById(R.id.tv_encrypted_message).setVisibility(View.GONE);
        findViewById(R.id.tv_confirmation_message).setVisibility(View.GONE);
        if (initCipher()) {
            mFragment.setCryptoObject(new FingerprintManager.CryptoObject(mCipher));
            if(!mFragment.isAdded()) {
                mFragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
        }
    }

    private boolean initCipher() {
        try {
            mKeySotre.load(null);
            SecretKey key = (SecretKey) mKeySotre.getKey(KEY_NAME, null);
            mCipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException | UnrecoverableKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        } catch (InvalidKeyException e) {
            return false;
        }
    }

    private void createKey() {
        try {
            mKeySotre.load(null);
            mKeyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT
                    | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            mKeyGenerator.generateKey();
        } catch (IOException | NoSuchAlgorithmException | CertificateException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void loadData() {
        //nothing to do
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        showLog("onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            showTost("Go to Settings");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onPurchased(boolean withFingerprint) {
        if (withFingerprint) {
            //验证通过
            tryEncrypt();
        } else {
            showFailInfo();
        }
    }

    private void showFailInfo() {
        TextView tv = (TextView) findViewById(R.id.tv_confirmation_message);
        tv.setVisibility(View.VISIBLE);
        tv.setText("Confirm Fail!");
    }

    private void tryEncrypt() {
        try {
            byte[] encrypted = mCipher.doFinal(SECRET_MESSAGE.getBytes());
            showConfirmatin(encrypted);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            showTost("Failed to encrypt the data with the generated key. "
                    + "Retry the purchase");
            showLog("Failed to encrypt the data with the generated key.");
        }
    }

    private void showConfirmatin(byte[] encrypted) {
        findViewById(R.id.tv_confirmation_message).setVisibility(View.VISIBLE);
        if (encrypted != null) {
            TextView tv = (TextView) findViewById(R.id.tv_encrypted_message);
            tv.setVisibility(View.VISIBLE);
            tv.setText(Base64.encodeToString(encrypted, 0));

        }
    }
}
