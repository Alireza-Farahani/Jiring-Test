@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "me.farahani.jiringtest"
  compileSdk = 34

  defaultConfig {
    applicationId = "me.farahani.jiringtest"
    minSdk = 24
    //noinspection OldTargetApi
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.1"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {

  implementation(platform(libs.compose.bom))
  implementation(platform(libs.okhttp.bom))
  implementation(libs.bundles.retrofit)
  implementation(libs.activity.compose)
  implementation(libs.core.ktx)
  implementation(libs.lifecycle.runtime.ktx)
  implementation(libs.lifecycle.compose)
  implementation(libs.material3)
  implementation(libs.navigation.compose)
  implementation(libs.viewmodel.compose)
  api(libs.kotlin.coroutines)
  implementation(libs.okhttp)
  implementation(libs.kotlin.serialization)
  implementation(libs.ui.graphics)
  implementation(libs.ui.tooling.preview)
  implementation(libs.ui)
  debugImplementation(libs.okhttp.loginterceptor)
  debugImplementation(libs.ui.tooling)
  debugImplementation(libs.ui.test.manifest)
  testApi(libs.kotlin.test.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlin.coroutines.test)
  testImplementation(libs.mockk)
  testImplementation(libs.okhttp.mockwebserver)
  testImplementation(libs.turbine)
  androidTestImplementation(platform(libs.compose.bom))
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.espresso.core)
  androidTestImplementation(libs.ui.test.junit4)
}