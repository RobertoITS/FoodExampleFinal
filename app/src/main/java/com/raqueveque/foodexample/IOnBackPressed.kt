package com.raqueveque.foodexample

interface IOnBackPressed {
    //Creamos esta interfaz para cancelar el BackPressed del main activity
    fun onBackPressed(): Boolean
}