package com.necdetzr.common.network

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val bleDispatcher : BleDispatchers)

enum class BleDispatchers {
    Default,
    IO
}
