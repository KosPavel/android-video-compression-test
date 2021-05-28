package com.kospavel.android_silicompressor_test

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val koinModules = module {
    single { TestViewModel(androidApplication()) }
}