# Yusuf Aile Kilit — Kurulum ve Build Rehberi

## Geliştirici: Yusuf Güzel

---

## 📥 Projeyi GitHub'a Yükle ve APK Al

### Adım 1: GitHub'a Yükle
1. GitHub'da yeni bir repository oluşturun (örn. `YusufAileKilit`)
2. Bu klasörün içeriğini o repoya yükleyin

### Adım 2: GitHub Actions'tan APK İndir
1. GitHub'da Actions sekmesine gelin
2. "Android APK Build" workflow'unu seçin
3. Push işlemi otomatik olarak build başlatır
4. Build tamamlanınca "Artifacts" bölümünden `YusufAileKilit-debug-APK` indirin
5. ZIP içindeki `app-debug.apk` dosyasını telefonunuza atın

---

## 🖥️ Lokal Build (Android Studio ile)

### Gereksinimler
- Android Studio Hedgehog veya üstü
- Java 17 (Android Studio ile birlikte gelir)
- Android SDK 34

### Adımlar
1. Bu klasörü Android Studio'da açın: `File → Open → YusufAileKilit klasörü`
2. Gradle sync tamamlanmasını bekleyin
3. `Build → Build Bundle(s) / APK(s) → Build APK(s)` menüsüne gidin
4. APK oluşturulacaktır: `app/build/outputs/apk/debug/app-debug.apk`
5. APK'yı telefona atın ve kurun

---

## 📱 Telefona Kurulum

### Bilinmeyen Kaynak İzni
1. Telefon Ayarlar → Güvenlik
2. "Bilinmeyen kaynaklardan yükleme" → APK dosyasına izin ver
3. APK dosyasına tıklayarak kurun

### İlk Kurulum Sonrası Yapılacaklar
1. Uygulamayı açın
2. Splash screen sonrası PIN oluşturma ekranı gelir
3. Güvenli bir PIN belirleyin (4-6 haneli)
4. İzinleri sırasıyla verin:
   - ✅ Kullanım İstatistikleri İzni (ZORUNLU)
   - ✅ Diğer Uygulamaların Üzerinde Görüntüle (KİLİT EKRANI için)
   - ✅ Erişilebilirlik Servisi (daha güçlü engelleme için)
   - ✅ Pil Optimizasyonu Kapatma (servisin arka planda çalışması için)
5. Ana panelden günlük süre limitini ayarlayın

---

## ⚙️ Özellikler

| Özellik | Durum |
|---------|-------|
| Splash Screen (Yusuf Güzel) | ✅ |
| PIN Sistemi | ✅ |
| YouTube / YouTube Kids Kilidi | ✅ |
| UsageStatsManager | ✅ |
| Foreground Service | ✅ |
| BootReceiver (yeniden başlatmada devam) | ✅ |
| Accessibility Service | ✅ |
| Overlay Kilit Ekranı | ✅ |
| Device Admin Yönlendirmesi | ✅ |
| Ödül Sistemi | ✅ |
| TV Kurulum (Android TV Remote) | ✅ |
| Router Engelleme Rehberi | ✅ |
| Pil Optimizasyonu Kapatma | ✅ |

---

## 📺 TV Kontrolü Hakkında

Arçelik TV ve Turkcell TV+ kutusu, Android TV Remote protokolünü (port 6466) destekliyorsa
uygulama üzerinden kontrol edilebilir.

**Desteklemiyorsa:** Uygulama bunu açıkça bildirir ve modem/router engelleme rehberi sunar.
Sahte kontrol yoktur.

---

## 🔒 Güvenlik Notları

- PIN olmadan hiçbir ayar değiştirilemez
- Servis kapanırsa 3 saniye içinde otomatik yeniden başlar
- Telefon yeniden başlatılsa bile servis devam eder (BootReceiver)
- Device Admin aktif edilirse uygulamanın kaldırılması zorlaşır
- Pil optimizasyonu kapatılırsa servis daha kararlı çalışır

---

## 📁 Proje Yapısı

```
YusufAileKilit/
├── .github/workflows/android-build.yml    ← GitHub Actions
├── app/
│   ├── build.gradle
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── kotlin/com/yusuf/ailekilit/
│   │   │   ├── activity/
│   │   │   │   ├── SplashActivity.kt
│   │   │   │   ├── SetupPinActivity.kt
│   │   │   │   ├── PinActivity.kt
│   │   │   │   ├── DashboardActivity.kt
│   │   │   │   ├── LockScreenActivity.kt
│   │   │   │   ├── SettingsActivity.kt
│   │   │   │   ├── TvSetupActivity.kt
│   │   │   │   ├── RewardActivity.kt
│   │   │   │   ├── RouterGuideActivity.kt
│   │   │   │   └── PermissionsActivity.kt
│   │   │   ├── service/
│   │   │   │   ├── MonitorService.kt      ← Foreground Service
│   │   │   │   ├── AppMonitorAccessibilityService.kt
│   │   │   │   └── BootReceiver.kt
│   │   │   ├── admin/
│   │   │   │   └── DeviceAdminReceiver.kt
│   │   │   ├── tv/
│   │   │   │   └── TvController.kt
│   │   │   ├── data/
│   │   │   │   └── PrefsManager.kt        ← SharedPreferences
│   │   │   └── util/
│   │   │       └── UsageStatsHelper.kt
│   │   └── res/
│   │       ├── layout/    ← 10 layout dosyası
│   │       ├── values/    ← colors, strings, themes
│   │       ├── xml/       ← accessibility config, device admin
│   │       ├── drawable/  ← ic_shield, ic_lock, input_bg
│   │       └── anim/      ← shake animasyonu
├── build.gradle
├── settings.gradle
└── gradlew
```
