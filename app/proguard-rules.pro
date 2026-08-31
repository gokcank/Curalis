# Curalis release ProGuard/R8 rules.
#
# Room, Hilt, WorkManager, biometric ve Glance kendi consumer-rules.pro'larını AAR'larıyla
# birlikte getiriyor (Room/Hilt/WorkManager sürümleri bunu destekliyor) — burada elle
# tekrarlanmıyor. Aşağıdaki kurallar yalnızca yansıma (reflection) tabanlı, R8'in kod
# küçültme/gizleme adımlarında kırılabilecek üç alan için: (1) Gson ile elle serileştirdiğimiz
# yedekleme verisi, (2) Google Drive REST istemcisi, (3) SQLCipher.

# ---------- Gson (bkz. LocalBackupManager.kt — Room varlıkları BackupData içinde JSON'a
# çevriliyor; alan adları @SerializedName ile eşlenmiyor, doğrudan alan adı okunuyor) ----------
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.stream.** { *; }
-keep class com.gokcank.curalis.domain.model.BackupData { <fields>; }
-keep class com.gokcank.curalis.data.local.entity.** { <fields>; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ---------- Google Drive REST istemcisi (bkz. GoogleDriveManager.kt) ----------
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.drive.** { *; }
-dontwarn com.google.api.client.**
-dontwarn org.apache.http.**
-dontwarn org.joda.time.**

# ---------- SQLCipher (bkz. DatabaseModule.kt, LegacyDatabaseMigrator.kt) ----------
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# ---------- Hilt Worker (bkz. ReminderWindowWorker.kt) ----------
-keep class com.gokcank.curalis.core.work.ReminderWindowWorker { *; }
