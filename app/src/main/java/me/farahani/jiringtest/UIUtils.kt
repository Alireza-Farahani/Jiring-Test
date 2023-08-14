package me.farahani.jiringtest

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UIString {
  data class RawString(val value: String) : UIString
  data class IdString(@StringRes val id: Int) : UIString
}

@Composable
fun UIString.asString() = when(this) {
  is UIString.RawString -> value
  is UIString.IdString -> stringResource(id)
}