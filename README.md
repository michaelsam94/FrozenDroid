# FrozenDroid 🧊

A clean, high-performance, no-root Android application designed to suspend/freeze resource-intensive background processes and carrier bloatware. FrozenDroid safely operates via high-privilege layers (Shizuku, ADB over Wi-Fi, or Root) with full Material 3 compliance and a local-first, privacy-respecting architecture.

---

## 🚀 Setup Guides

FrozenDroid requires elevated execution privileges to communicate with the Android Package Manager. Setting up Shizuku or wireless ADB pairing takes less than 2 minutes and does not require a computer!

### Method 1: Shizuku Service (Recommended)
This is the most seamless configuration. No computer is required on Android 11+.

1. **Install Shizuku**: Download the official Shizuku application from the Google Play Store.
2. **Enable Developer Options**:
   * Navigate to `Settings` -> `About Phone`.
   * Tap `Build Number` 7 times until you see the "You are now a developer" toast.
3. **Configure Wireless Debugging**:
   * Open `Developer Options`.
   * Enable both **USB Debugging** and **Wireless Debugging**.
4. **Pair Shizuku**:
   * Open Shizuku and tap **Pairing** -> **Developer Options** -> **Wireless Debugging** -> **Pair device with pairing code**.
   * Note the 6-digit Wi-Fi pairing code and enter it into the Shizuku persistent notification.
5. **Start Shizuku**:
   * Return to the Shizuku app home screen and tap **Start**.
   * When FrozenDroid is launched, accept the permissions popup prompt to bind the high-privilege package commands service.

---

### Method 2: Fallback ADB over Wi-Fi
If Shizuku is unavailable, you can start the background pairing loop fallbacks directly via ADB.

1. Ensure your computer and Android device are connected to the same Wi-Fi network.
2. Connect your device via USB to your computer.
3. Run the following port redirection command in your terminal:
   ```bash
   adb tcpip 5555
   ```
4. Find your device's local IP address (in `Settings` -> `About Status` -> `IP Address`).
5. Disconnect the USB cable and establish the wireless bridge:
   ```bash
   adb connect <your_device_ip>:5555
   ```

---

## 🧊 Common Safe-to-Freeze Packages

The **Safe Directory** in FrozenDroid preloads manufacturer categorization indexes offline. Here is a quick reference guide of highly recommended bloatware packages that are 100% safe to freeze inside the application:

| Manufacturer | Package Name | Description |
| :--- | :--- | :--- |
| **Samsung** | `com.samsung.android.bixby.agent` | Voice Assistant Agent |
| **Samsung** | `com.samsung.android.game.gamehome` | Samsung Game Launcher overlay |
| **Xiaomi / POCO** | `com.miui.analytics` | Xiaomi background telemetry metrics |
| **Xiaomi / POCO** | `com.miui.daemon` | Performance reporting background daemon |
| **Oppo / Realme**| `com.oppo.market` | Alternative app market catalog |
| **Universal / Social** | `com.facebook.system` | Preloaded background installer service |
| **Universal / Social** | `com.facebook.services` | Preloaded background updates manager |

---

## 🛡️ The Safety Gate Architecture

FrozenDroid incorporates a hard-coded security fence to protect users from breaking core operating system frameworks:
* **SAFE**: Standard overlays, assistants, and alternative store lines. Completely safe to suspend.
* **CAUTION**: Custom customizers, secondary launcher setups. Require verification.
* **DANGEROUS**: Vital components (e.g., `com.android.systemui`, telephony services). **All freeze operations are strictly locked for these packages.**
* **User Overrides**: Experienced users can manually downgrade or upgrade a package's safety tier via the Safe Directory page.

---

## 🛠️ Build & Architecture Specs

* **Presentation Layer**: Jetpack Compose compiling material design components. Runs ViewModels and StateFlow architectures with zero main thread blocking.
* **Domain Layer**: Clean Kotlin business use cases without Android Framework bindings.
* **Data Layer**: Robust offline storage backed by Room database and DataStore settings.
