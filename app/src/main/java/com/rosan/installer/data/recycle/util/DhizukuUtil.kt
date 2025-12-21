package com.rosan.installer.data.recycle.util

import android.content.pm.PackageManager
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener
import com.rosan.installer.data.recycle.model.exception.DhizukuNotWorkException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay

suspend fun <T> requireDhizukuPermissionGranted(action: suspend () -> T): T {
    callbackFlow {
        Dhizuku.init()
        delay(1000) // Dhizuku.init() 之后等待一秒钟
        if (Dhizuku.isPermissionGranted()) send(Unit)
        else {
            Dhizuku.requestPermission(object : DhizukuRequestPermissionListener() {
                override fun onRequestPermission(grantResult: Int) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) trySend(Unit)
                    else close(Exception("dhizuku permission denied"))
                }
            })
        }
        awaitClose()
    }.catch {
        throw DhizukuNotWorkException(it)
    }.first()
    return action()
}
