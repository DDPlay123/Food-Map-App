package com.side.project.foodmap.util.tools

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Coroutines {
    fun io(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.IO).launch { work() }

    fun main(work: suspend () -> Unit) =
        CoroutineScope(Dispatchers.Main).launch { work() }

    fun default(work: suspend () -> Unit) =
        CoroutineScope(Dispatchers.Default).launch { work() }

    fun unconfined(work: suspend () -> Unit) =
        CoroutineScope(Dispatchers.Unconfined).launch { work() }
}