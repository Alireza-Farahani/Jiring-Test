package me.farahani.jiringtest.localdata

import android.content.Context
import androidx.core.content.edit
import me.farahani.jiringtest.domain.Storage

class AndroidSharedPrefStorage(
  appContext: Context,
  prefName: String,
//  private val json: Json,
) : Storage {
  private val pref = appContext.getSharedPreferences(prefName, Context.MODE_PRIVATE)
  override fun put(key: String, value: String) {
    pref.edit {
      putString(key, value)
    }
  }

  override fun get(key: String): String? {
    return pref.getString(key, null)
  }

  override fun remove(key: String) {
    pref.edit { remove(key) }
  }
}