# Curalis — Elden Geçirme Yol Haritası

Bu belge, altı ayrı gözden geçirme adımında toplanan yaklaşık 160 tespiti tek bir sıralı iş listesine dönüştürüyor. Sıralama "önce hangisi düzeltilmeli" mantığına göre yapıldı: bir madde, kendisinden sonrakileri anlamlı kılıyorsa yukarıda; tek başına iyileştirme sayılıyorsa aşağıda.

Her fazın başında, o fazın **neden o sırada olduğu**, önerilen model/efor seviyesi ve durumu yazıyor. Fazlar sırayla yapılmalı; faz içindeki maddeler çoğunlukla birbirinden bağımsız.

**Yürütme kuralı:** Bir faz, kullanıcı "Faz X başla" demeden uygulamaya geçilmez. Faz bitince başlığındaki durum "✅ TAMAMLANDI" olarak işaretlenir.

---

## Önce en önemli tespit

Uygulamanın var oluş sebebi olan iş — **"ilacımı aldım" demek** — şu anda arayüzden yapılamıyor. Bunu yapan ekran yazılmış, çalışıyor, ama hiçbir yerden açılamıyor.

Bunun zinciri şöyle: kullanıcı dozunu işaretleyemiyor → veritabanına uyum kaydı düşmüyor → takvim ekranı boş/gri kalıyor → analiz ekranı sıfır dozdan "%100 uyum" üretiyor → doktora gidecek PDF raporu anlamsız çıkıyor. Yani ilk bakışta beş ayrı ekran sorunu gibi görünen şeyin tek bir kökü var.

Bu yüzden Faz 0'ın ilk maddesi bu. Küçük bir düzeltme (bir gezinme bağlantısı) ama arkasındaki dört ekranı birden anlamlı hâle getiriyor.

---

## Faz 0 — Acil: çöküyor, veri kaybettiriyor veya işlevsiz — ✅ TAMAMLANDI

**Neden önce:** Bu maddelerin her biri ya uygulamayı çökertiyor, ya kullanıcının verisini siliyor, ya da temel işlevi engelliyor. Bunlar dururken görsel iyileştirme yapmak boşa emek.

**Model/efor:** Sonnet 5 · Yüksek

1. ✅ **Doz işaretleme ekranını erişilebilir yap.** Ana ekrandaki "sıradaki ilaç" kartı ve günlük ilerleme kartı artık tıklanabilir, doğrudan günlük zaman çizelgesi ekranını açıyor. Ayrıca gezinme dosyasındaki bu ekrana ait yinelenen (çakışan) kayıt tekilleştirildi.

2. ✅ **Alarm izni kontrolü eklendi.** Alarm kurucu artık her alarmdan önce tam zamanlı alarm izninin olup olmadığını kontrol ediyor; izin yoksa alarmı sessizce atlayıp bunu günlüğe yazıyor (çökme riski ortadan kalktı). İlaç kaydeden ekranda da izin eksikse kullanıcıya "hatırlatıcılar tetiklenmeyebilir" uyarısı gösteriliyor.

3. ✅ **Titreşim izni eklendi.** Uygulama izin listesine titreşim izni eklendi.

4. ✅ **Silinen/düzenlenen ilaçların alarmları iptal ediliyor.** İlaç silindiğinde, o ilaca ait hem veritabanında kayıtlı hatırlatıcıların hem de arka planda kurulmuş tüm alarmların iptali eklendi. İlaç düzenlendiğinde de kaydetmeden önce ilacın eski alarmları iptal edilip yeni saatler için temiz baştan kuruluyor — artık eski/çift alarm kalmıyor.

5. ✅ **Veritabanı güncelleme stratejisi düzeltildi.** Kullanıcı verisi barındıran veritabanı artık "şema değişince her şeyi sil" davranışından çıkarıldı; bundan sonra bir sürüm güncellemesi migration tanımı olmadan yapılırsa uygulama **veri silmek yerine** geliştiriciyi uyaracak şekilde çöküyor (test/yayın öncesi yakalanır, kullanıcı verisi tehlikeye girmez). Gelecekteki güncellemelerin doğru yazılabilmesi için veritabanı şeması artık dosya olarak kaydediliyor. İlaç sözlüğü (TİTCK verisi) kullanıcı verisi olmadığı ve her güncellemede zaten yeniden yüklendiği için o veritabanında eski davranış korundu.

6. ✅ **Hata yakalama eklendi.** İlaç kaydetme/silme akışlarında ve bildirim üzerindeki "Aldım / Ertele / Atla / Tümünü Al" eylemlerinde artık hata yakalanıyor; bir şey ters giderse uygulama çökmek yerine kullanıcıya anlaşılır bir mesaj gösteriyor (liste ekranında ekranın altında beliren kısa bir bildirim şeklinde).

7. ✅ **Yaz saati hatası düzeltildi.** "X günde bir" hesaplamasının tekrarlandığı üç yerde de, milisaniye farkını sabit 24 saate bölen hatalı yöntem yerine takvim gününü doğru sayan bir yönteme geçildi — yaz saati geçişlerinde artık gün kaymıyor.

**Doğrulama:** Değişiklikler sonrası proje hatasız derlendi (`assembleDebug`/`compileDebugKotlin`) ve mevcut testler geçti. Cihaz üzerinde manuel arayüz testi yapılmadı — bir sonraki adımda (veya kullanıcı isterse) emülatörde doğrulanabilir.

---

## Faz 1 — Güven, gizlilik ve yasal zorunluluklar — ✅ TAMAMLANDI

**Neden burada:** Bu bir sağlık uygulaması. Kullanıcının hangi ilacı kullandığı hassas veridir ve bunu koruma sorumluluğu yasal. Ayrıca yayına çıkmadan önce bulunması zorunlu uyarılar eksik.

**Model/efor:** Sonnet 5 · Yüksek

8. ✅ **Yedekler şifrelendi.** Hem telefona alınan hem Google Drive'a yüklenen yedekler artık kullanıcının belirlediği bir şifreyle şifreleniyor (şifre cihaza değil, kullanıcıya bağlı — telefon değişse de aynı şifreyle geri yüklenebilir). Şifre en az 4 karakter, yedek alırken iki kez, geri yüklerken bir kez soruluyor. Bu sürümden önce alınmış şifresiz yedek dosyaları da hâlâ okunabiliyor (geriye dönük uyumluluk).

9. ✅ **Bildirim kanalları ayrıldı.** Doz hatırlatmaları ve stok uyarıları artık iki ayrı bildirim kanalında; kullanıcı sistem ayarlarından "stok uyarılarını sustur, doz hatırlatmaları kalsın" diyebiliyor. Sessiz saatler için ayrıca sessiz bir üçüncü kanal eklendi (madde 11).

10. ✅ **Kilit ekranı gizliliği eklendi.** Yeni ayarlar ekranında "Kilit ekranında ilaç adını gizle" seçeneği var; açıldığında bildirim kilitli ekranda görünür ama ilacın adı gizlenir.

11. ✅ **Sessiz saatler eklendi.** Hem ayar ekranında (başlangıç/bitiş saati seçimi) hem bildirim gönderme mantığında gerçek karşılığı var: bu saatler arasında hatırlatıcı sessiz kanaldan, ekranı uyandırmadan geliyor — ama tamamen bastırılmıyor, kullanıcı uyandığında hâlâ görebiliyor (doz kaçırma riskini artırmamak için).

12. ✅ **Bildirim ve hatırlatıcı ayarları ekranı oluşturuldu.** Ayarlar ekranından ulaşılan yeni bir ekranda 10-11. maddeler ve sistem bildirim kategorisi ayarlarına kısayol bir araya geldi.

13. ✅ **Yasal uyarılar eklendi.** "Hakkında" ekranına kalıcı, her zaman görünen bir tıbbi uyarı kartı ("bu uygulama tıbbi tavsiye yerine geçmez") ve ayrıntılı bir gizlilik politikası/KVKK aydınlatma metni (nerede hangi verinin tutulduğunu, yedeklerin nasıl şifrelendiğini, hiçbir reklam/izleme olmadığını anlatan) eklendi.

14. ✅ **"Doğrulanmış bilgi / kullanıcı girişi" ayrımı eklendi.** İlaç kaydına yeni bir alan eklendi: resmî öneriden seçilen ilaçlar "✓ Doğrulanmış Kaynak", elle girilenler "✎ Elle Girildi" rozetiyle işaretleniyor; bu bilgi hem ilaç listesinde hem ekleme/düzenleme ekranında her zaman görünür. Kullanıcı önerilen bir ismi seçtikten sonra elle değiştirirse rozet otomatik "elle girildi"ye dönüyor.

**Yan not:** Bu madde veritabanı şemasını değiştirdiği için, Faz 0'da hazırlanan gerçek migration alt yapısı ilk kez burada kullanıldı (sürüm 7→8, veri kaybı olmadan).

**Doğrulama:** Değişiklikler sonrası proje hatasız derlendi ve mevcut testler geçti; veritabanı şeması hem eski hem yeni sürüm için doğru şekilde dışa aktarıldı. Cihaz üzerinde manuel arayüz testi yapılmadı.

---

## Faz 2 — Tasarım sistemi ve okunabilirlik — ✅ TAMAMLANDI

**Neden burada:** Faz 0-1'den sonra uygulama doğru ve güvenli çalışıyor olacak. Şimdi sıra "istediğim gibi görünmüyor" şikâyetinin asıl kaynağında. Bu fazın maddeleri birbirine bağlı olduğu için tek tek değil, bütün olarak ele alındı.

**Model/efor:** Opus 5 · Yüksek

15. ✅ **Renk sistemi kuruldu.** Asıl sebep bulundu: palete yalnızca 7 renk verilmişti, geri kalan onlarca renk yuvası Material'ın hazır mor/lila şablonundan geliyordu — açık temada kartların lilaya dönmesinin sebebi buydu. Artık açık ve koyu tema için tam palet tanımlı. Ayrıca şartnamenin istediği ama hiç var olmayan üç anlamsal renk (uyarı / başarı / bilgi) eklendi ve **hata rengi yalnızca gerçek hatalara ayrıldı**: düşük stok, kaçırılan doz ve düşük uyum artık kırmızı değil turuncu (uyarı) gösteriliyor.

16. ✅ **Kontrast standarda çıkarıldı.** Tüm metin/zemin çiftleri erişilebilirlik alt sınırı (4,5:1) hedefiyle yeniden seçildi. Koyu tema da düzeltildi: neredeyse saf siyah olan zemin, şartnamenin "saf siyahtan kaçının" kuralına uygun yumuşak bir koyu griye çekildi. Ayrıca yazı tipi ölçeğinin tamamı (başlıktan etikete 13 seviye) tanımlandı; önceden sadece 2 seviye tanımlıydı, gerisi varsayılana düşüyordu.

17. ✅ **Emoji ikonlar tamamen kaldırıldı.** Kod tabanında **56 satırda** emoji vardı; hepsi gerçek ikonlara çevrildi ve tek bir ikon ailesinde birleştirildi. İkon eşlemesi arayüz katmanına taşındı (veri modelleri artık görsel bilgi taşımıyor).

18. ✅ **Dekoratif arka plan kaldırıldı.** Ana ekranda tam ekran, doygun turkuaz bir stok görseli vardı — palet rengiyle ilgisiz, metnin arkasında kontrastı düşürüyordu ve şartnamenin iki ayrı yasağını çiğniyordu. Yerine temanın kendi renklerinden türeyen çok hafif bir zemin geçti; böylece açık/koyu temaya kendiliğinden uyum sağlıyor. **Yan kazanç: uygulama boyutundan ~11,7 MB düştü** (10,4 MB kullanılan + 1,3 MB hiç kullanılmayan ölü görsel).

19. ✅ **Metin kırılmaları giderildi.** Uzun ilaç/doktor/randevu adları artık taşmak yerine düzgün kısaltılıyor; "%100" değeri artık ikiye bölünmüyor; ölçüm formundaki taşan etiket ve ilaç formunda iki kez yazılan etiket düzeltildi. Ayrıca kart köşe yarıçapları tek bir ölçekte toplandı (her ekran kendi değerini seçiyordu).

20. ✅ **Boş durumlar ve veriler dürüst hâle getirildi.** Bu fazın en önemli düzeltmesi: analiz ekranı **sıfır dozdan "%100 uyum"** üretiyordu. Artık değerlendirilecek doz yoksa yüzde yerine "—" ve "henüz veri yok" gösteriliyor; ayrıca uyum oranı yalnızca sonuçlanmış dozlar üzerinden hesaplanıyor (saati henüz gelmemiş dozlar oranı haksız yere düşürmüyor). Takvimde "gelecek gün" ile "o gün ilaç planlanmamıştı" ayrımı yapıldı — 31 günün aynı gri görünmesinin sebebi buydu. Aramada sonuç çıkmadığında artık "hiç ilaç eklemediniz" değil, "sonuç bulunamadı" ve aramayı temizleme seçeneği çıkıyor. Tüm boş ekranlar ortak bir tasarıma alındı: ikon + ne olduğu + ne yapılacağı.

21. ✅ **Tema seçenekleri tamamlandı.** Altyapı zaten hazırmış, yalnızca arayüzde sunulmuyormuş: açık/kapalı anahtarı yerine **Sistem / Açık / Koyu** üçlü seçimi eklendi.

22. ✅ **Tasarım sistemi karnesi büyük ölçüde kapatıldı.** Yukarıdakilere ek olarak şartnamenin zorunlu kıldığı boşluk, köşe yarıçapı ve ikon boyutu ölçekleri tanımlandı; durum rozetleri tek bir bileşende toplandı (renkleri iki ayrı ekranda kopyalanmış ve **koyu temada beyaz lekeler** oluşturuyordu); her durum artık ikon + metin + renk üçlüsüyle birlikte geliyor, yani hiçbir bilgi yalnızca renge dayanmıyor; takvim günlerine ekran okuyucu açıklaması eklendi.

**Doğrulama:** Temiz derleme yapıldı, testler geçti, kod tabanında hiç emoji kalmadığı doğrulandı. Cihaz üzerinde manuel görsel test yapılmadı — renk/kontrast değişikliklerinin göz kararı doğrulanması için emülatörde gezilmesi önerilir.

---

## Faz 3 — Eksik ve yarım kalmış özellikler — ✅ TAMAMLANDI (29 kullanıcı kararıyla ertelendi)

**Neden burada:** Bunlar uygulamayı bozmuyor, ama söz verilmiş veya yarım bırakılmış işler. Faz içinde ucuzdan pahalıya sıralandı.

**Model/efor:** Sonnet 5 · Orta (23-28) — "Tedavi" kavramı (29) büyük olduğu için ayrı ele alınacak, kullanıcı isteğiyle bu fazın sonunda durulup bekleniyor.

23. ✅ **Randevuya doktor seçimi eklendi.** *(Önceki bir tespitin düzeltmesi: "kodda hiç yok" denmişti, doğru değilmiş.)* Veri modeli, veritabanı ilişkisi ve ekran mantığı zaten hazırdı; randevu ekleme ekranına eksik olan seçim kutusu eklendi. Artık her randevuya bir doktor bağlanabiliyor.

24. ✅ **PDF rapora önizleme eklendi.** Rapor artık önce ekranda tam boyutlu gösteriliyor, kullanıcı "Paylaş" demeden paylaşılmıyor.

25. ✅ **Stok geçmişi tutulmaya başlandı.** Her doz alındığında ve kullanıcı stoğu elle değiştirdiğinde (yeni kutu girişi/düzeltme) kalıcı bir kayıt düşülüyor; ilaç düzenleme ekranından bu geçmiş görüntülenebiliyor. Veritabanı sürümü buna göre güncellendi (9. sürüm, veri kaybı olmadan).

26. ✅ **Hatırlatıcı durum döngüsü tamamlandı.** Yeni bir ara durum ("İletildi") eklendi: alarm tetiklenip bildirim gösterildiğinde bu artık kalıcı olarak kaydediliyor. Böylece "alarm hiç çalmadı" ile "çaldı ama yanıtlanmadı" ayrımı yapılabiliyor. Yan kazanç: bu çalışma sırasında, bir hatırlatıcı hiç kaydedilmemişse "kaçırıldı" durumunun hiçbir zaman kaydedilmediği gerçek bir hata da bulunup düzeltildi.

27. ✅ **Döngüsel doz (21 gün kullan / 7 gün ara) tamamlandı.** Veritabanı alanları zaten hazırdı; artık bu sıklık türü seçilebiliyor, aktif/dinlenme gün sayısı girilebiliyor ve hatırlatıcılar buna göre doğru hesaplanıyor. Kaldırmak yerine bitirilmesi tercih edildi çünkü altyapının neredeyse tamamı zaten hazırdı.

28. ✅ **Barkod okuma eklendi** (kullanıcı seçimiyle: tam kamera taraması). İlaç ekleme ekranına, kutunun barkodunu kamerayla okutabilen bir alan eklendi (Google'ın hazır barkod tarama bileşeni kullanıldı — ayrı bir kamera izni istemeye gerek kalmadı). **İkincil ilaç kaynağı** eklenmedi: bağlanacak gerçek bir dış ilaç veritabanı servisi yok, uydurma bir entegrasyon yapılmadı. Barkod, otomatik ilaç eşleştirmesi yapmıyor (TİTCK yerel verisinde barkod bilgisi yok) — yalnızca kullanıcının kendi kutusunu tanımasına yarıyor.

29. ⏸️ **"Tedavi" kavramı — ERTELENDİ.** Projenin kendi sözlüğünde ve ürün belgesinde temel bir kavram olarak tanımlı ("bir tedavi birden fazla ilaç içerebilir"), yol haritasında "tedavi geçmişi" bir teslim maddesi. Kodda bu kavramın hiçbir izi yok — her şey tekil ilaç kayıtları üzerinden çalışıyor.
   
   **Karar:** Kullanıcının günlük ilaç kullanımı çoğunlukla **sürekli/uzun vadeli** (tansiyon ilacı, vitamin gibi) — "başlangıcı-sonu belli kür" senaryosu baskın değil. Bu yüzden şimdilik ne tam implementasyon ne belge güncellemesi yapılıyor; karar yol haritası içinde not edilip, gelecekte kullanım şekli değişirse tekrar gündeme alınabilir duruma alınıyor.

---

## Faz 4 — Dil ve metin düzeni — ✅ TAMAMLANDI

**Neden burada:** Uygulama çalışır ve doğru göründükten sonra yapılması en verimli iş; erken yapılırsa Faz 2-3'te yazılan yeni metinler yüzünden tekrar edilmesi gerekir.

**Önerilen model/efor:** Haiku 4.5 · Düşük — mekanik metin taşıma ve dosya düzenleme, yargı gerektiren kısmı (üslup kararı) küçük.

30. ✅ **Varsayılan dil kaynağını İngilizce'ye çevir.** `/values` (varsayılan) artık İngilizce, `/values-tr` Türkçe. Sistem dili İngilizce olmayan cihazlarda uygulama artık İngilizce açılıyor; Türkçe sistemlerde Türkçe.

31. ✅ **43 sabit kodlanmış metni dil dosyalarına taşı.** Tüm UI metinleri `strings.xml` dosyalarında tanımlı; hardcoded metin yoktur. Ayrıca Faz 3'te eksik kalan bildirim kanalları, tema seçenekleri ve gizlilik metinleri de eklendi.

32. ✅ **İlaç formu, doz birimi ve yemek talimatı listelerinin İngilizcesini ekle.** `MedicationForm`, `DosageUnit` ve `MealInstruction` enum'larına `displayNameEn` alanları eklendi; İngilizce dil seçildiğinde doğru çeviriler kullanılıyor.

33. ✅ **Üslup kılavuzu oluştur ve uygula.** Boş durum mesajları ("Henüz hiç ... eklemediniz" Türkçe, "No ... added yet" İngilizce) ve başlıklar uyumlu; tutarlı bir üslupta.

34. ✅ **Hata bildirim şablonu uyumsuzluğu.** `bug_report_body` ve diğer hata mesajları her dilde uygun şekilde çevrilmiş ve tutarlı; hiçbir tutarsızlık yok.

---

## Faz 5 — Teknik borç ve proje sağlığı — ✅ TAMAMLANDI

**Neden en sonda:** Bunlar kullanıcının hiç görmediği ama sonraki her değişikliği yavaşlatan veya riskli hâle getiren şeyler. Faz 0-4 bittikten sonra yapıldığında, o fazlarda yazılan yeni kod da bu temizlikten faydalanır.

35. ✅ **Otomatik denetim (lint) kontrol hattına eklendi.** Artık her derlemede denetim otomatik çalışıyor; o ana kadar birikmiş eski uyarılar bir referans listesinde saklanıp göz ardı ediliyor, ama bundan sonra çıkacak yeni bir gerçek hata derlemeyi durduruyor. Faz 0'daki gibi kritik bir hatanın aylarca fark edilmeden kalması artık mümkün değil.

36. ✅ **Kritik alanlara test kapsamı oluşturuldu.** Öncelik sırasına uyularak: önce Faz 0'da düzeltilen alarm izni davranışı (Android 12+ cihazlarda izin geri alındığında uygulamanın çökmek yerine sessizce atlaması), sonra uyum/takvim hesaplamalarının kalbindeki tekrarlama mantığı (günlük, aralıklı, haftanın belirli günleri, aktif-pasif döngü gibi tüm ilaç kullanım şekilleri) test altına alındı. Toplam test sayısı 2'den 15'e çıktı. Kalan ekranların ve veri depolarının test edilmesi, kod tabanı büyüdükçe ayrı bir çalışmada sürdürülebilir.

37. ✅ **Katman ihlalleri düzeltildi.** Yedekleme ekranının bağımlılık düzenini atlayıp ilgili sınıfı elle oluşturması giderildi; artık düzenli şekilde sağlanıyor. Android'e doğrudan bağımlı olan alan modeli, bu bağımlılığı ekran katmanına taşıyacak şekilde temizlendi. Kullanılmayan ve yanlış katmanda duran eski zamanlama üretici dosyası silindi. Aynı işi (kullanıcı tercihini hatırlama) yapan iki farklı yöntemden eskisi kaldırılıp tek, tutarlı yönteme geçildi.

38. ✅ **İsimlendirme kuralları uygulandı.** Projenin kendi kodlama belgesine aykırı olarak alt çizgi içeren üç klasör adı (ör. "repository_impl") standart isimlendirmeye çevrildi; aynı işi yapan sınıfların iki farklı klasörde dağınık durması da bu sırada tek bir yerde toplanarak giderildi.

39. ✅ **Kütüphaneler güvenli şekilde güncellendi.** Büyük risk taşıyan en güncel (deneysel) sürümlere atlamak yerine, mevcut yapıyla uyumlu kalan en güncel sürümlere geçildi: temel derleme araçları, arayüz kütüphanesi ve tüm bağımlılıklar güncellendi; sadece kırılma riski yüksek olan birkaç büyük sürüm sıçraması (ileride ayrıca konuşulmak üzere) bilinçli olarak ertelendi. Bu sırada ortaya çıkan kullanımdan kaldırılmış bir titreşim çağrısı da güncellendi. Google ile giriş yapma akışının kullandığı eski API'nin yenilenmesi ise ayrı, gerçek cihazda test gerektiren bir çalışma olduğu için kapsam dışı bırakıldı.

40. ✅ **Kullanılmayan kaynaklar temizlendi.** Kodda hiç kullanılmayan 37 metin kaynağı, boş bir kaynak klasörü ve düzensiz tanımlanmış bir kütüphane bağımlılığı temizlendi.

**Not:** Madde 39'da ertelenen büyük sürüm sıçramaları (temel derleme aracının ve arayüz aracının bir sonraki büyük sürümü) ayrı bir oturumda, cihazda test imkânı olduğunda ele alınmalı.

**Emülatörde doğrulama sırasında bulunan ve düzeltilen ek bir hata:** Bildirim kategorileri (İlaç Hatırlatıcıları, Sessiz Saatler, Stok Uyarıları) telefonun Ayarlar > Bildirimler ekranında hiç görünmüyordu. Sebebi: bu kategoriler yalnızca ilk hatırlatıcı tetiklendiğinde oluşturuluyordu; yeni kurulumda henüz hiç alarm çalmadığı için kullanıcı Ayarlar'a girdiğinde kategoriler boş kalıyordu. Artık kategoriler uygulama ilk açıldığı anda oluşturuluyor, ilk alarmı beklemiyor.

---

## En hızlı kazanımlar

Zaman kısıtlıysa şu beş madde en az emekle en çok fark yaratır:

| Madde | Neden |
|---|---|
| **1** — Doz işaretleme ekranını erişilebilir yap | Bir gezinme bağlantısı; dört ekranı birden anlamlı kılıyor |
| **23** — Randevuya doktor seçimi | Dört katmandan üçü hazır; sadece bir seçim kutusu eksik |
| **4** — Silinen ilaçların alarmını iptal et | Küçük düzeltme; en görünür kullanıcı şikâyetini bitiriyor |
| **35** — Denetimi kontrol hattına ekle | Tek yapılandırma değişikliği; bundan sonraki hataları kendiliğinden yakalar |
| **30** — Varsayılan dili İngilizce'ye çevir | Dosya taşıma işi; yanlış dil sorununu tamamen çözer |

---

## Süreçle ilgili bir not

İncelemede iki ayrı yerde şu görüldü: **belge "tamamlandı" diyor, kod öyle demiyor.** Bir geliştirme fazı, kendi tanımladığı sekiz görevin ikisi hiç yapılmamışken tamamlandı olarak işaretlenmiş; bir yol haritası bölümü de altyapısı hiç var olmayan bir teslim maddesini içeriyor.

Bu, tek tek maddelerden daha önemli bir bulgu: bundan sonraki elden geçirmede "tamamlandı" işaretinin, belgedeki kontrol listesine değil koddaki gerçek duruma bakılarak konması gerekiyor. Aksi hâlde bu liste de zamanla aynı duruma düşer.

---

*Bu belge, altı gözden geçirme adımının çıktısıdır. Adımların ayrıntılı bulguları GOZDEN_GECIRME_PLANI.md dosyasında özetlenmiştir.*
