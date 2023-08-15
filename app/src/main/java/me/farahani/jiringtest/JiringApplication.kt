package me.farahani.jiringtest

import android.app.Application

class JiringApplication : Application(){
  @Suppress("MemberVisibilityCanBePrivate")
  lateinit var serviceLocator: ServiceLocator

  override fun onCreate() {
    super.onCreate()
    initServiceLocator()
  }

  private fun initServiceLocator() {
    serviceLocator = ServiceLocator(this)
  }
}