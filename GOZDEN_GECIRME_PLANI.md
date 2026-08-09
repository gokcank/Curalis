# Curalis Gözden Geçirme Planı

Amaç: Gemini ile yazdırılan mevcut uygulamayı — hem kod hem tasarım tarafında — baştan sona tarafsız bir gözle incelemek, "istenildiği gibi olmayan" noktaları somut bir listeye dökmek. Bu adımların hiçbiri kod değiştirmiyor; hepsi **tespit** aşaması. Elden geçirme (asıl düzeltme/yeniden yazım) bu listenin üzerine kurulacak ayrı bir aşama olacak.

Her adım için önerilen model ve efor seviyesi belirtildi — küçük/mekanik işler için hafif, yargı gerektiren (özellikle tasarım ve önceliklendirme) işler için daha güçlü model önerildi.

**Durum:** Plan onay bekliyor, henüz hiçbir adım çalıştırılmadı. ("Başla" denince adımlar sırayla/paralel başlatılacak.)

**Yürütme kuralı:** Her adım bittiğinde otomatik olarak bir sonrakine geçilmeyecek. Adım çalıştırılıp sonucu kullanıcıya gösterilecek; kullanıcı bir sonraki adım için modeli onaylayana veya değiştirene kadar beklenecek. Ancak onay/seçim geldikten sonra bir sonraki adıma geçilecek.

---

## 1. Kod doğruluğu ve güvenlik taraması — ✅ TAMAMLANDI
Tüm kod tabanında gerçek hata, güvenlik açığı ve kırılgan noktaları arandı.
**Model:** Sonnet 5 · **Efor:** Yüksek

**Sonuç: 16 bulgu** (5 kritik, 2 yüksek, 6 orta, 3 düşük). Öne çıkanlar:
- Tam zamanlı alarm izni hiç kontrol edilmiyor → izin reddedilirse çökme
- Silinen/düzenlenen ilaçlar için alarm hiç iptal edilmiyor → hayalet/yinelenen alarmlar
- Veritabanı "yıkıcı migration" ile kurulu, migration tanımlı değil → şema değişince tüm veri sessizce silinir
- ViewModel kaydet/sil akışlarında hata yakalama yok → sessiz çökmeler
- Bildirim eylemi alıcısında (Al/Ertele/Atla) hata yakalama yok → sessiz çökme
- Yedekler (yerel + Google Drive) şifresiz düz metin
- Yaz saati geçişlerinde "X günde bir" ilaçlar yanlış günde hatırlatılabilir (3 ayrı yerde tekrarlanan hata)

Tüm 16 madde dosya/satır referanslarıyla ayrı bir bulgu listesi olarak raporlandı (sohbet geçmişinde).

## 2. Mimari ve tutarlılık denetimi — ✅ TAMAMLANDI
Klasör/isimlendirme tutarsızlıkları, kullanılmayan/ölü alanlar, yarım kalmış özellikler; projenin kendi mimari dokümanlarına (docs/architecture, docs/adr, AGENTS.md) sadakat açısından denetlendi.
**Model:** Sonnet 5 · **Efor:** Orta

**Sonuç: 13 bulgu** (3 yüksek, 6 orta, 4 düşük). Öne çıkanlar:
- Yedekleme ekranı, Google Drive işlemleri için dependency injection'ı atlayıp ilgili sınıfı elle oluşturuyor
- Bir zamanlanmış görev üretici (ScheduleGenerator) tamamen ölü kod; ayrıca yanlış katmanda (domain içinde Room'a bağımlı)
- Bir domain modeli (VitalType) doğrudan Android'e bağımlı
- Aynı kavram (use case toplama) için iki farklı, tutarsız desen kullanılıyor
- Projenin kendi kodlama dokümanının yasakladığı "Manager/Helper/Utils" isimleri yaygın kullanılmış
- Test kapsamı neredeyse sıfır (13 ViewModel'in hiçbiri, hiçbir repository/mapper test edilmemiş)
- Varsayılan dil kaynağı fiilen İngilizce değil Türkçe (dil eşleşmeyen cihazlarda beklenmedik dile düşme riski)

Tüm 13 madde dosya referanslarıyla ayrı bir bulgu listesi olarak raporlandı (sohbet geçmişinde).

## 3. Canlı arayüz/tasarım incelemesi — ✅ TAMAMLANDI
Emülatörde ulaşılabilen tüm ekranlar gezildi, 20+ ekran görüntüsü alınıp görsel olarak değerlendirildi; açık/koyu tema ve sistem karanlık modu test edildi.
**Model:** Opus 5 · **Efor:** Yüksek

**Sonuç: 60+ bulgu, 13 ekran.** En kritik 5 madde:
1. **Uygulamanın ana işi arayüzden yapılamıyor** — planlı bir dozu "aldım" olarak işaretlemenin hiçbir yolu yok. Bunu yapan "Günlük Zaman Çizelgesi" ekranı yazılmış ama hiçbir yerden açılamıyor; ana ekrandaki "Sıradaki İlaç" kartı da tıklanmıyor. Takvim ve analiz ekranlarının anlamsız veri göstermesinin kökeni bu.
2. **Gözle görülür metin kırılmaları/çakışmalar** — takvimde ilaç adı rozetin altına girip kesiliyor, rozet kelime ortasından bölünüyor; analizde "%100" değeri ikiye ayrılıp başlığa yapışıyor; ölçüm formunda etiket çerçeveden taşıyor; ilaç formunda etiket iki kez yazılmış.
3. **Renk sistemi yok** — tek uygulamada 6 vurgu rengi; hata rengi zararsız bir yedekleme kartında, kırmızı uyarı "0 atlanan" değerinde. Açık temada kartlar lila/pembeye dönüyor (palet büyük ölçüde hazır şablona bırakılmış). Kontrast ölçümleri WCAG sınırının altında (2.1:1 – 2.74:1).
4. **Dekoratif arka plan okunabilirliği bozuyor, emoji ikon yerine kullanılmış** — projenin kendi illüstrasyon kılavuzunun iki açık yasağı birden çiğnenmiş.
5. **Boş durumlar ve veri görselleştirme yanlış bilgi veriyor** — aramada sonuç yokken "hiç ilaç eklemediniz"; uyum takviminde 31 gün de aynı gri; analizde 0 dozdan %100 uyum ve dolu yeşil çubuk.

Ayrıca: Ayarlar'da hiçbir bildirim/hatırlatıcı ayarı yok, "sistemi izle" tema seçeneği yok, Hakkında'da gizlilik/KVKK/"tıbbi tavsiye değildir" uyarısı yok, PDF rapor önizlemesiz doğrudan paylaşıma gidiyor, randevu doktora bağlanamıyor.

Tasarım sistemi karnesi: şartnamedeki 17 maddenin neredeyse tamamı karşılanmıyor. Ekran görüntüleri scratchpad'de (ss_*.png).

## 4. Özellik eksiksizliği çapraz kontrolü — ✅ TAMAMLANDI
Curalis'in kendi ürün planlama belgeleri (roadmap, implementation-plan, reminders, glossary) koddaki gerçek durumla tek tek karşılaştırıldı.
**Model:** Sonnet 5 · **Efor:** Orta

**A) Planlanmış ama hiç yapılmamış (9 madde):** "Tedavi" (Treatment) kavramı kodda hiç yok (sadece tekil ilaç/hatırlatıcı kayıtları var); stok geçmişi tutulmuyor (üzerine yazılıyor, log yok); bildirim kanalları tek kanalda toplanmış (kullanıcı doz hatırlatmasıyla stok uyarısını ayrı ayrı susturamaz); kilit ekranı bildirim içerik gizliliği seçeneği yok; ikincil ilaç veritabanı kaynağı entegre değil; barkod okuma yok (alan var, kullanılmıyor); sessiz saatler (quiet hours) hem arayüzde hem alt mantıkta tamamen yok; "doğrulanmış / kullanıcı girişi" ayrımı hiçbir yerde yok.

**B) "Tamamlandı" işaretlenmiş ama gerçekte eksik (2 madde):** Bir geliştirme fazı ("Faz 5") commit'te tamamlandı diye işaretlenmiş ama kendi tanımladığı 8 görevden 2'si (ikincil kaynak entegrasyonu, barkod okuma) hiç yapılmamış. Yol haritasındaki "Geçmiş ve İçgörüler" bölümü de 4 hedeften 3'ü yapılmış görünüyor ama 4.'sü ("Tedavi geçmişi") "Tedavi" kavramı hiç olmadığı için yapılması imkansız.

**C) Dokümante edilmemiş ama koda yarım bırakılmış (3 madde, biri önemli bir düzeltme):**
- **Düzeltme:** Randevu-doktor ilişkisi önceki bir bulguda "kodda hiç yok" denmişti — bu **güncel değildi**. Gerçekte veri modeli, veritabanı ve ViewModel'in tamamı bu ilişkiyi destekliyor; eksik olan tek şey randevu ekleme ekranındaki doktor seçim arayüzü. Yani üç katmandan üçü hazır, sadece bir ekran elemanı eksik — düzeltme gerekli çünkü asıl iş çok daha az.
- Hatırlatıcı durumu, dokümanda tarif edilen yaşam döngüsünün (Planlandı→Bekliyor→İletildi→Onaylandı→Alındı/Atlandı/Ertelendi/Kaçırıldı) sadece bir kısmını kodluyor; "bildirim hiç tetiklenmedi" ile "tetiklendi, kullanıcı henüz yanıtlamadı" ayrımı yapılamıyor.
- Döngüsel doz alanları (aktif gün/dinlenme günü) veritabanında tam olarak duruyor ama hiçbir ekranda girilemiyor, hiçbir hesaplamada okunmuyor.

## 5. Metin/dil tutarlılığı taraması — ✅ TAMAMLANDI
Sabit kodlanmış Türkçe/İngilizce metinler, çeviri dosyalarıyla uyumsuzluk, tutarsız üslup.
**Model:** Haiku 4.5 · **Efor:** Düşük

**Sonuç: 6 ana kategori, 43 hardcoded metin**
- **Hardcoded metinler (43 adet):** Özellikle BackupScreen (16) ve AddEditMedicationScreen (19) çok sayıda `Text("...")` içeriyor; bu metinler strings.xml'de tanımlı değil, sadece kodda var.
- **Domain Model çeviriler eksik:** MealInstruction ve DosageUnit enumlarında Türkçe displayName var ama İngilizce karşılığı yok; MedicationForm hardcoded string'ler kullanıyor (forma göre değişmiyor).
- **Dil yapılandırması yanlış:** Android best practice'e göre, `values/` (qualifier olmayan, fallback) **İngilizce** olmalı ama şu anda Türkçe — desteklenmeyen bir dile düşenlerde (ör. Fransızca cihaz) sistem Türkçe'ye fallback yapıyor, bu yanlış.
- **Boş durum mesajları tutarsız:** DailyTimelineScreen "Bugün için planlanmış ilaç dozu bulunmamaktadır" (formal), strings.xml'de "Henüz hiç ilaç eklemediniz" (casual).
- **bug_report_body uyumsuzluğu:** values/ ile values-tr/ arasında tamamen farklı metin.
- **Üslup karışıklığı:** Başlıklar title case, bazı yerlerde normal cümle; buton metinleri "İptal" (1 kelime) vs "Çıkış Yap" (3 kelime), tutarlı kılavuz yok.

## 6. Derleme/test sağlığı kontrolü — ✅ TAMAMLANDI
Testlerin gerçekten geçip geçmediği, derlemenin temiz olup olmadığı, test kapsamının nerede boş olduğu.
**Model:** Sonnet 5 · **Efor:** Orta

**Sonuç:**
- **Derleme temiz** (hatasız) ama 9 uyarı var: eski/kullanımdan kaldırılmış API çağrıları (durum çubuğu rengi, titreşim, Google Giriş), bir kullanılmayan değişken, bir isim çakışması riski.
- **Denetim (lint) başarısız oluyor** — 2 kritik hata: alarm tam ekran uyarısında titreşim izni kontrolsüz çağrılıyor (izin manifestte yok/lint göremiyor). Ayrıca 122 uyarı (çoğu güncel olmayan kütüphane sürümü, kullanılmayan kaynak dosyası).
- **Test kapsamı doğrulandı, gerçekten neredeyse sıfır:** Tüm projede sadece **1 test dosyası, 2 test** var (bir zamanlama hesaplayıcısı için). 13 ekran mantığının (ViewModel), 5 veri deposunun, 2 dönüştürücünün hiçbirine ait tek bir test yok. Cihaz üzerinde çalışan (arayüz) test altyapısı da hiç kurulmamış.
- **Kütüphaneler epey geride:** Android derleme aracı, Kotlin, Hilt, Room, Compose gibi temel bileşenlerin sürümleri 1-2 yıl gerisinde (Compose kütüphanesi 2 yıl eski). Bunlar birbirine bağımlı olduğu için tek tek değil birlikte güncellenmesi gerekiyor.
- **Otomatik kontrol (CI) var ama eksik:** GitHub üzerinde her değişiklikte derleme ve test otomatik çalışıyor, ama denetim (lint) hiç çalıştırılmıyor — yani yukarıdaki kritik izin hatası hiçbir zaman otomatik yakalanmıyor.

## 7. Sonuçları tek bir önceliklendirilmiş listede birleştirme — ✅ TAMAMLANDI
Yukarıdaki 6 adımın çıktısı, "önce şunu düzelt, sonra şunu yap" mantığıyla sıralı bir elden-geçirme listesine dönüştürüldü.
**Model:** Opus 5 · **Efor:** Yüksek

**Çıktı: `ELDEN_GECIRME_YOL_HARITASI.md`** — 6 fazda 40 madde:
- **Faz 0 (Acil, 7 madde):** Çöken, veri silen veya temel işlevi engelleyen sorunlar. Başında: doz işaretleme ekranının erişilemez olması.
- **Faz 1 (Güven ve gizlilik, 7 madde):** Yedek şifreleme, bildirim kanalları, kilit ekranı gizliliği, sessiz saatler, yasal uyarılar.
- **Faz 2 (Tasarım sistemi, 8 madde):** Renk sistemi, kontrast, emoji ikonlar, metin kırılmaları, dürüst boş durumlar.
- **Faz 3 (Eksik/yarım özellikler, 7 madde):** Ucuzdan pahalıya — doktor seçimi (neredeyse hazır) → "Tedavi" kavramı (sıfırdan).
- **Faz 4 (Dil ve metin, 5 madde):** Varsayılan dil, 43 sabit metin, üslup birliği.
- **Faz 5 (Teknik borç, 6 madde):** Denetimin kontrol hattına eklenmesi, test kapsamı, katman ihlalleri, kütüphane güncellemesi.

Ayrıca: **5 maddelik "en hızlı kazanımlar"** tablosu ve süreçle ilgili bir uyarı (iki yerde belge "tamamlandı" derken kod öyle demiyor).

---

## Yürütme sırası

Yedi adımın tamamı tamamlandı. Tespit aşaması bitti; sıradaki aşama `ELDEN_GECIRME_YOL_HARITASI.md` üzerinden fiilî düzeltme.
