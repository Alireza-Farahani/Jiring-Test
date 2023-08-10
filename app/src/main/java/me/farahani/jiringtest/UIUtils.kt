package me.farahani.jiringtest

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UIString {
  data class RawString(val value: String) : UIString
  data class ResourceIdString(@StringRes val id: Int) : UIString
}

fun UIString.asString(context: Context) = when(this) {
  is UIString.RawString -> value
  is UIString.ResourceIdString -> context.getString(id)
}

@Composable
fun UIString.asString() = when(this) {
  is UIString.RawString -> value
  is UIString.ResourceIdString -> stringResource(id)
}