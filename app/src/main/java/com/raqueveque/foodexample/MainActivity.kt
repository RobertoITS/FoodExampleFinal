package com.raqueveque.foodexample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    //Sobreescribimos la funcion para que no se ejecute a menos que se cumpla la condicion
    //Para no usuarios navHost
//    override fun onBackPressed() {
//        val frag = this.supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
//        (frag as? IOnBackPressed)?.onBackPressed()?.not()?.let { isCanceled: Boolean ->
//            if (!isCanceled) super.onBackPressed()
//        }
//    }
    //Para usuarios navHost
    override fun onBackPressed() {
        val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                (fragment as? IOnBackPressed)?.onBackPressed()?.not()?.let { isCanceled: Boolean ->
                    Toast.makeText(this, "onBackPressed $isCanceled", Toast.LENGTH_SHORT).show()
                    if (!isCanceled) {
                        super.onBackPressed()
                    }
                }
            }
        }
    }
}