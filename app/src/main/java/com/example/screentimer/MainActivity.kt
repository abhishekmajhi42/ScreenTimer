package com.example.screentimer

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private lateinit var btnEnable: Button
    private lateinit var btnLock: Button
    private lateinit var deviceManger: DevicePolicyManager
    private lateinit var compName: ComponentName
    private lateinit var newKeyguardLock: KeyguardManager.KeyguardLock
    private lateinit var wakeLock: WakeLock

    val RESULT_ENABLE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnEnable = findViewById<Button>(R.id.btnEnable)
        btnLock = findViewById<Button>(R.id.btnLock)
        deviceManger = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, DeviceAdmin::class.java)
        val km: KeyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        newKeyguardLock = km.newKeyguardLock("MainActivity");
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE, "appname::WakeLock"
        )

        val active: Boolean = deviceManger.isAdminActive(compName)
        if (active) {
//            btnEnable.setText("Disable")
//            btnLock.setVisibility(View.VISIBLE)
            Log.d("TAG", "app already enabled")
        } else {
//            btnEnable.setText("Enable")
//            btnLock.setVisibility(View.GONE)
            enablePhone()
        }
    }

    fun enablePhone() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You should enable the app!")
        startActivityForResult(intent, RESULT_ENABLE)
    }

    fun lockPhone(view: View?) {
//        Thread(Runnable { kotlin.run {
//            onOff()
//        } }).start()
        onOff()
    }

    private fun onOff() {
        val active = deviceManger.isAdminActive(compName)
        if (active) {

            deviceManger.lockNow()

            Handler().postDelayed(Runnable {
                kotlin.run {
                    newKeyguardLock.disableKeyguard();


                    //acquire will turn on the display
                    wakeLock.acquire()

                    //release will release the lock from CPU, in case of that, screen will go back to sleep mode in defined time bt device settings
                    wakeLock.release()
                }
            }, 30000)

//            Thread.sleep(30000)

        } else {
            enablePhone()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_ENABLE -> {
                if (resultCode == RESULT_OK) {
//                    btnEnable.text = "Disable"
//                    btnLock.visibility = View.VISIBLE
                    Log.d(TAG, "App enabled in device admin")

                } else {
                    Toast.makeText(
                        applicationContext, "Failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    companion object {
        private var TAG = "ScreenTimer"
    }

}