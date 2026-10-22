# Hambi Lite — mustaqil Android loyiha

Loyiha foydalanuvchi yuborgan HTML prototipining dizaynini saqlashga mo'ljallangan: sariq-qora Hambi Lite uslubi, xizmat kartalari, pastki menyu, til tanlash, xizmatlar va profil sahifalari.

## GitHub orqali APK yig'ish (Android Studio o'rnatmasdan)
1. GitHub'da yangi repository yarating (masalan, `hambi-lite`).
2. ZIP ichidagi `HambiLite` papkasining **ichidagi barcha fayl va papkalarni** repository ildiziga yuklang. `.github/workflows/android-apk.yml` fayli ham joyida qolishi kerak.
3. Fayllarni `main` branch'ga commit qiling.
4. GitHub repository'da **Actions** bo'limini oching.
5. `Build Hambi Lite APK` workflow'ni tanlab, **Run workflow** tugmasini bosing. Push qilganda ham avtomatik ishga tushadi.
6. Build tugagach, muvaffaqiyatli run sahifasidagi **Artifacts** bo'limidan `HambiLite-debug-apk` ni yuklab oling.
7. Yuklangan artifact ZIP'ini ochib, `app-debug.apk` faylini Samsung telefoniga o'tkazing va o'rnating. Telefon noma'lum manbadan o'rnatishga ruxsat so'rashi mumkin.

## Lokal build
1. Android Studio: https://developer.android.com/studio
2. `HambiLite` papkasini Android Studio'da oching.
3. Gradle sync tugashini kuting.
4. `Build > Build Bundle(s) / APK(s) > Build APK(s)` ni tanlang.

## USSD
- Balans: `*102#`
- Internet qoldig'i: `*103#`
- Tarif: `*110*05#`
- Daqiqalar: `*106#`
- SMS: `*105#`
- Xizmatlar menyusi: `*111#`

Android `TelephonyManager.sendUssdRequest()` orqali USSD so'rovi yuborilishi mumkin, ammo ishlashi telefon, Android versiyasi, SIM tanlovi va operator qo'llab-quvvatlashiga bog'liq. Haqiqiy Samsung/Beeline SIM bilan sinovdan o'tkazilmagan.

## Muhim cheklovlar
Bu mustaqil konseptual ilova, Hambi yoki Beeline rasmiy ilovasi emas. USSD real operator javobini olishi mumkin. Tarif sotib olish, balans to'ldirish, pul o'tkazish va boshqa to'lov amallari demo bo'lib qoladi; haqiqiy amallar uchun ruxsatli API/integratsiya zarur.
