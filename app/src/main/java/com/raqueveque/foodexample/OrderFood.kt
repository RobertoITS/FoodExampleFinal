package com.raqueveque.foodexample

//oldPosition guarda la posición en la lista principal
data class OrderFood (var name: String, var oldPosition: Int,
                      var variation: String, var price: Long)

//Vamos a usar una funcion nativa de Kotlin (tambien esta en java :P)
//La idea es la siguiente, guardar el pedido original en una sola variable, y separar por comas
/**
 * La secuencia es la siguiente:
 * en la seccion de extras (la complicada) se guarda todo junto
 * "Mayonesa, Ketchup, Papas fritas"
 * luego creamos la lista con los valores separados:
 * val string = lista[posicion].split(",").toTypedArray()
 * Despues simplemento los buscamos en la coleccion correspondiente, y si se encuentra,
 * se da el true al checkBox
 * **/