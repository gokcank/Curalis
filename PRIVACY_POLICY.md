# Curalis - Gizlilik Politikası (Privacy Policy)

**Son Güncelleme Tarihi:** 10 Ağustos 2026
**Uygulama Adı:** Curalis (com.gokcank.curalis)
**Geliştirici:** Gökcan Kahraman (`destek.gokcank@gmail.com`)

Curalis olarak kişisel verilerinizin ve sağlık bilgilerinizin gizliliğine son derece önem veriyoruz. Bu Gizlilik Politikası, Curalis mobil uygulamasını kullanırken verilerinizin nasıl toplandığını, saklandığını ve korunduğunu açıklamaktadır.

---

## 1. Temel İlke: Yerel Veri Mülkiyeti (Privacy by Design)

Curalis, **"Tasarım Gereği Gizlilik"** prensibiyle inşa edilmiştir.
- Girdiğiniz tüm ilaç, dozaj, hatırlatıcı, doktor, randevu ve ölçüm bilgileri **yalnızca cihazınızın yerel veritabanında** (Room SQLite) saklanır.
- Bu veriler, siz açıkça bir yedekleme işlemi başlatmadığınız sürece cihazınızdan dışarı çıkmaz; harici bir sunucuya veya merkezi bir veri tabanına aktarılmaz.
- Verileriniz asla satılmaz, üçüncü taraflarla paylaşılmaz veya reklam/pazarlama amacıyla kullanılmaz.

---

## 2. İzinler ve Kullanım Amaçları

Curalis, işlevlerini yerine getirebilmek için cihazınızdan yalnızca aşağıdaki izinleri talep eder:

* **Bildirim İzni (`POST_NOTIFICATIONS`):** İlaç içme zamanlarınızı ve doktor randevularınızı size bildirim olarak anında iletmek için kullanılır.
* **Hassas Alarm İzni (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`):** İlaç hatırlatıcılarının zamanında ve milisaniyelik hassasiyetle çalmasını sağlamak için gereklidir.
* **İnternet İzni (`INTERNET`):** Yalnızca sizin açık rızanızla başlattığınız **Google Drive Yedekleme** işlemi için kullanılır. İlaç arama önerileri cihaza önceden yüklenmiş yerel bir veritabanından (T.C. Sağlık Bakanlığı TİTCK ilaç listesi) okunur; bu aramalar için internete hiçbir istek gönderilmez.

---

## 3. Yedekleme (Yerel ve Google Drive)

Curalis, verilerinizi yedeklemeniz için iki seçenek sunar:

- **Yerel yedekleme:** Yedek dosyası, siz açıkça bir yedekleme başlatana kadar oluşturulmaz. Oluşturulduğunda, **sizin belirlediğiniz bir şifreyle şifrelenerek** kendi seçtiğiniz konuma (cihaz depolaması veya tercih ettiğiniz herhangi bir bulut servisi) kaydedilir.
- **Google Drive yedeklemesi (opsiyonel):** Varsayılan olarak kapalıdır ve yalnızca "Google ile Giriş Yap" butonuna basmanızla etkinleşir. Yedek, yerel yedekle aynı şekilde **sizin belirlediğiniz şifreyle şifrelenip**, Google Drive hesabınızın yalnızca Curalis'e özel, gizli alanına (`appDataFolder`) yüklenir.
- Her iki durumda da geliştirici dahil hiçbir üçüncü taraf yedek dosyalarınızın içeriğine erişemez; şifreyi yalnızca siz bilirsiniz. Şifrenizi unutursanız yedek geri yüklenemez.

---

## 4. Hata Bildirimi ve Destek (Bug Reporting)

Uygulama içindeki "Hata Bildir" özelliğini kullandığınızda:
- Cihazınızda oluşan teknik hata kayıtları (`logcat.txt`) geçici bir dosya olarak hazırlanır.
- Bu dosya, yalnızca siz onay verip kendi e-posta uygulamanız üzerinden gönderim yaptığınızda `destek.gokcank@gmail.com` adresine iletilir.
- Otomatik veya gizli arka plan veri aktarımı yapılmaz.

---

## 5. Üçüncü Taraf Analitik ve Reklamlar

- **Reklam Yok:** Curalis içerisinde hiçbir üçüncü taraf reklam ağı (AdMob vb.) bulunmamaktadır.
- **Takip / Analitik Yok:** Uygulamamızda Google Analytics, Firebase Tracking veya herhangi bir kullanıcı izleme (telemetri) aracı **bulunmamaktadır**.

---

## 6. KVKK Kapsamında Aydınlatma ve Kullanıcı Hakları

6698 sayılı Kişisel Verilerin Korunması Kanunu ("KVKK") uyarınca aşağıdaki hususlarda bilginizi rica ederiz:

- **Veri sorumlusu:** Gökcan Kahraman (`destek.gokcank@gmail.com`).
- **İşlenen kişisel sağlık verileri:** İlaç, dozaj, hatırlatıcı, doktor, randevu ve ölçüm (tansiyon, kan şekeri, nabız, kilo, ateş vb.) bilgileri.
- **İşlenme amacı ve yeri:** Bu veriler yalnızca uygulamanın temel işlevini (hatırlatma ve takip) yerine getirmek amacıyla, **yalnızca sizin cihazınızda** işlenir. Veri sorumlusu sıfatıyla geliştirici tarafından hiçbir sunucuda saklanmaz veya işlenmez; geliştiricinin bu verilere erişimi yoktur.
- **Aktarım:** Verileriniz, siz açıkça bir yedekleme başlatmadığınız sürece cihazınızdan dışarı çıkmaz. Yedekleme durumunda dahi veriler şifrelenmiş olarak yalnızca sizin seçtiğiniz konuma veya kendi Google hesabınıza aktarılır (bkz. Madde 3); üçüncü bir kişiye veya yurt dışına aktarım yapılmaz.
- **Haklarınız:** KVKK'nın 11. maddesi kapsamında, verilerinizin işlenip işlenmediğini öğrenme, işlenme amacını öğrenme, verilerinizin silinmesini veya yok edilmesini isteme haklarına sahipsiniz. Verileriniz üzerinde teknik olarak da tam kontrol sizdedir:
  - Uygulama içerisinden dilediğiniz veriyi anında silebilirsiniz.
  - Uygulamayı cihazınızdan kaldırdığınızda, cihazda saklanan tüm veriler kalıcı olarak silinir.
  - Google Drive yedeğinizi dilediğiniz an Google Hesap Ayarları > Bağlı Uygulamalar bölümünden temizleyebilirsiniz.
- Haklarınızı kullanmak veya sorularınız için Madde 7'deki iletişim bilgilerinden bize ulaşabilirsiniz.

---

## 7. İletişim

Gizlilik politikamız veya kişisel verilerinizle ilgili her türlü soru, görüş ve talepleriniz için bizimle iletişime geçebilirsiniz:

- **E-posta:** `destek.gokcank@gmail.com`
- **Geliştirici:** Gökcan Kahraman

---
---

# Curalis - Privacy Policy (English)

**Last Updated:** August 10, 2026
**Application Name:** Curalis (com.gokcank.curalis)
**Developer:** Gökcan Kahraman (`destek.gokcank@gmail.com`)

At Curalis, we take the privacy of your personal and health information very seriously. This Privacy Policy explains how your data is collected, stored, and protected when you use the Curalis mobile application.

## 1. Core Principle: Privacy by Design & Local Ownership
- Your health information (medications, dosages, reminders, doctors, appointments, and vitals) is stored **strictly on your local device** (Room SQLite local database).
- This data never leaves your device unless you explicitly start a backup; it is **never uploaded to external servers or central databases**.
- We do not sell, track, or share your data with third parties or advertisers.

## 2. Device Permissions
- **Notifications (`POST_NOTIFICATIONS`):** Used strictly to send medication and appointment reminders.
- **Exact Alarms (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`):** Required to trigger medication alarms precisely on schedule.
- **Internet (`INTERNET`):** Used exclusively for the user-initiated **Google Drive backup** feature. Medication search suggestions are read from a local database bundled with the app (the Turkish Ministry of Health TİTCK medication list); no network request is made for these searches.

## 3. Backups (Local and Google Drive)
- **Local backup:** No backup file is created until you explicitly start one. Once created, it is **encrypted with a password you choose** and saved to the location you pick (device storage or any cloud service you prefer).
- **Google Drive backup (optional):** Disabled by default and only activated when you tap "Sign in with Google." The backup is encrypted the same way, with your own password, before being uploaded to your Google Drive account's private, app-only folder (`appDataFolder`).
- In both cases, neither the developer nor any third party can access the contents of your backup files — only you know the password. If you forget it, the backup cannot be restored.

## 4. No Ads & No Analytics
- Curalis contains **no third-party ads**.
- Curalis uses **no hidden telemetry, tracking scripts, or analytics tools**.

## 5. Legal Basis and Your Rights (KVKK / GDPR)
- **Data controller:** Gökcan Kahraman (`destek.gokcank@gmail.com`).
- Your personal health data is processed only on your own device, for the sole purpose of running the app's reminder and tracking features; it is not stored or processed on any server by the developer, who has no access to it.
- Data leaves your device only if you explicitly start a backup, and even then only encrypted and only to a destination you choose (see Section 3) — never to a third party.
- You may exercise your rights (access, deletion, objection) at any time: delete any record from within the app, uninstall the app to permanently erase all local data, or remove your Google Drive backup from your Google Account's Connected Apps settings.

## 6. Contact Information
For any questions regarding privacy or data rights, please contact:
**Email:** `destek.gokcank@gmail.com`
