# Curalis, rakip ilaç hatırlatma uygulamasının gerisinde kaldığı yerler ve bunları kapatma planı

*Ürün & Mimari Notu · Curalis · 6 Ağustos 2026*

Yedi alanda karşılaştırma: takvim ve zaman çizelgesi, görsel tasarım, ilaç veri modeli, alarm motoru, bildirim otomasyonu ve doktor raporu. Sonunda, hangi sırayla neyin yapılacağını gösteren somut bir yol haritası var.

> **Yöntem hakkında:** Bu değerlendirme, rakip uygulamanın yayınlanmış paketi yalnızca özellik ve tasarım araştırması amacıyla incelenerek hazırlandı. Hiçbir kod parçası kopyalanmadı veya Curalis'e aktarılmadı — aşağıdaki her öneri, Curalis'in kendi mimarisine göre sıfırdan yazılacak şekilde tarif edildi. Nihai üründe, kodda, arayüzde veya metinlerde rakip uygulamanın adı ya da izi yer almayacak.

---

## Genel durum, tek bakışta

| Alan | Curalis | Rakip Uygulama | Not |
|---|---|---|---|
| Takvim & Zaman Çizelgesi | 🔴 Yok | 🟢 Çok gelişmiş | Curalis'te geçmişe dönük hiçbir görünüm yok, sadece bugünün tek bir ilerleme halkası var. |
| Görsel Tasarım Sistemi | 🟡 Başlangıç | 🟢 Kapsamlı | İlaç türleri emoji ile gösteriliyor; ilaca özel renk seçimi yok. |
| İlaç Veri Modeli | 🟡 Orta | 🟢 Çok kapsamlı | Aç/tok bilgisi eksik. Son kullanma tarihi ise iki tarafta da yok. |
| Alarm Motoru & Kilit Ekranı | 🔴 Başlangıç | 🟢 Gelişmiş | Cihaz yeniden başlatılınca alarmlar sıfırlanıyor; kilit ekranı üstü uyarı hiç yok. |
| Bildirim & Stok Otomasyonu | 🟢 İyi temel | 🟢 Olgun | Otomatik stok düşümü zaten çalışıyor. Eksik: toplu "Hepsini Al" ve esnek erteleme süreleri. |
| Doktor için PDF Raporu | 🟡 Kısmen çalışıyor | 🟢 Sunucu tabanlı | Rapor üretiliyor ama uyum istatistikleri hiç beslenmiyor. |

---

## 1. Eksik görsel tasarım ve ekranlar

Kullanıcının doğrudan gördüğü, dokunduğu yüzeyler. Bu bölümde bir düzeltme de var: rakip uygulamada da klasik "renkli takvim ızgarası" yok — asıl güçlü olduğu yer, günlük zaman çizelgesi.

### Takvim Görünümü — aylık/haftalık ızgara, ikisinde de tam anlamıyla yok

Başta beklenenin aksine, rakip uygulama da her günü tek bir renkle boyayan klasik bir ay takvimi kullanmıyor. Onun yerine, bir tarih seçici şerit (ileri/geri ok veya haftalık sekmeler) ile o güne ait kart listesini birlikte gösteriyor. Durum bilgisi, gün hücresinde değil, her doz satırının yanındaki küçük bir işaretle (yeşil tik / kırmızı ünlem / gri çarpı / mavi saat) veriliyor.

Curalis'te bu tarih seçici şerit de, doz bazlı durum işareti de yok — sadece bugünün tek bir ilerleme halkası var.

> **Fırsat:** Rakip bile gerçek bir "ay görünümünde renkli gün" takvimi sunmuyor. Curalis burada gerçek bir ay ızgarası (her gün, o güne ait dozların özetine göre yeşil/sarı/kırmızı/gri) yaparsa, bu alanda rakibi geçebilir.

### Günlük Zaman Çizelgesi — saatlere göre gruplanmış doz akışı, büyük ve gerçek bir boşluk

Rakip uygulamanın en olgun özelliği bu: bir günün tüm dozları, aynı saate denk gelenler tek grupta olacak şekilde kronolojik sıralanıyor, her grubun başında saat bilgisi var, birden fazla doz aynı saatteyse grup başında "Hepsini İşaretle" gibi toplu bir kısayol beliriyor. Ayrıca günü kaba dilimlere (sabah/öğle/akşam/gece) ayırıp her dilimi ayrı sekmede toplu işlem yapılabilecek şekilde gösteren ikinci, daha basit bir görünüm de var. Kullanıcı henüz işaretlemediği ya da erteleyip unuttuğu dozlar varsa, ekranda kaybolan yumuşak bir hatırlatma şeridi de çıkıyor.

Curalis'te böyle bir ekran hiç yok; ilaç listesi ekranı zaman sırasına göre değil, ilaç bazında listeleniyor.

### Kart Tasarımı ve İkonlar — iskelet benzer, ikon/renk zenginliği çok farklı

Her iki tarafta da kart mantığı aynı iskelete oturuyor: ikon — ilaç adı — doz/güçlülük — saat/talimat satırı — durum işareti. Fark, ikon ve renk tarafında: Curalis şu an 9 ilaç türünü sabit emoji ile gösteriyor ve ilaca özel renk seçimi yok. Rakip uygulamada ise onlarca farklı çizilmiş ilaç şekli ikonu var ve kullanıcı, düzinelerce ton içinden ilacına özel bir renk seçebiliyor; seçilen renk o ikonun üzerine anında uygulanıyor.

- Kart üzerindeki saat/sıklık/stok bilgisi zaten Curalis'te de küçük etiketler (chip) olarak var — bu kısım rakiple aşağı yukarı aynı seviyede.
- Eksik olan: gerçek, renklendirilebilir bir ikon seti ve ilaca özel bir renk seçimi adımı.

### İlerleme Halkası ve Uyum Grafikleri — tek günlük halka var, uzun dönem grafiği yok

Curalis'te sadece bugünün alınan/toplam oranını gösteren tek bir dairesel gösterge var. Rakip uygulamada aynı basit dairesel gösterge, hem "bugün" hem "bu hafta/ay/yıl" seçeneğiyle tekrar kullanılıyor; ayrıca daha yeni eklenen bir trend grafiği (çubuk/nokta şeklinde, tarih aralığı seçilebilir) geçmişe dönük uyum eğilimini gösteriyor.

### Stok Uyarı Rozetleri — ikisi de sade, burada gerçek bir fark yok

Curalis'in kart üzerindeki "Stok Azalıyor" etiketi, işlevsel olarak rakibin ayrı bir bildirim kartıyla yaptığıyla aynı seviyede. Burada büyük bir boşluk yok; sadece bu etiketin tek seferlik özel bir kod parçası yerine, uygulamanın her yerinde kullanılabilecek ortak bir rozet bileşenine dönüştürülmesi önerilir.

---

## 2. Eksik mantık ve veri alanları

Kullanıcının doğrudan görmediği ama davranışı belirleyen kurallar, veri alanları ve arka plan işleyişi.

### İlaç Veri Modeli

- **Aç/tok/yemek talimatı:** Rakip uygulamada dört sabit seçenek var (aç karnına, yemekle birlikte, yemekten sonra, farketmez) artı gerekirse serbest metinle ek not. Curalis'te bu alan hiç yok.
- **Doz birimi:** Curalis'te serbest metin bir alan (kullanıcı ne yazarsa o kaydediliyor). Rakip uygulama bunu sabit bir liste üzerinden yönetiyor. Curalis için rakibin devasa listesine gerek yok — yaygın kullanılan 15-20 birimlik (tablet, ml, damla, ampul, yama, gibi) sabit bir liste yeterli olur.
- **Kullanılmayan alanlar:** Curalis'in veri modelinde döngüsel doz (aktif gün/dinlenme günü) için alanlar zaten tanımlı ama hiçbir ekran veya hesaplama bunları okumuyor ya da yazmıyor — yani şu an "ölü" alanlar. Ya bu özelliğin basit bir sürümü tamamlanmalı, ya da kafa karışıklığını önlemek için bu alanlar kaldırılmalı.
- **Serbest not alanı:** Curalis'in veri modelinde zaten bir "not" alanı var ama ilaç ekleme/düzenleme ekranında hiç gösterilmiyor — küçük ama gözden kaçmış bir eksik.

> **Ortak boşluk:** Son kullanma tarihi takibi ne Curalis'te ne rakip uygulamada var. Bu, "rakibe yetişme" değil, gerçek bir farklılaşma fırsatı.

### Alarm Güvenilirliği — en yüksek öncelikli teknik borç

- **Cihaz yeniden başlatıldığında alarm kaybolur:** Curalis şu an telefon yeniden başlatıldığında kurulu hatırlatıcıları geri yüklemiyor — kullanıcı fark etmeden hatırlatıcılar sessizce durur. Bu bir özellik eksiği değil, düzeltilmesi gereken bir güvenilirlik sorunu.
- **Kaçırılan doz hiç işaretlenmiyor:** Curalis'in veri modelinde "kaçırıldı" durumu tanımlı ama hiçbir mekanizma bir dozu otomatik olarak bu duruma geçirmiyor. Rakip uygulama, hatırlatma gönderildikten belirli bir süre (yaklaşık yarım saat) sonra hâlâ yanıtsız kalan dozları otomatik olarak "kaçırıldı" yapıyor.
- **Erteleme süresi sabit 10 dakika:** Kullanıcının seçme şansı yok. Rakip uygulama 5/10/15/30/60/120 dakika gibi seçenekler sunuyor.
- **Saat dilimi değişikliği ele alınmıyor:** Düşük öncelikli ama not edilmeye değer; seyahat eden kullanıcılar için ileride sorun çıkarabilir.

### Kilit Ekranı Uyarısı — tam ekran, cihazı uyandıran alarm ekranı hiç yok

Curalis'teki hatırlatma şu an sıradan bir bildirim olarak geliyor. Rakip uygulamada ise hatırlatma anında, alarm saati uygulamalarına benzer şekilde, ekranı uyandıran ve kilit ekranının üzerinde açılan tam ekran bir uyarı sayfası çıkıyor; bu sayfada aynı al/atla/ertele seçenekleri, ilaç bilgisi ve bir uyarı sesi bulunuyor. Bu, "hatırlatıcıyı gerçekten kaçırmama" hissi açısından en belirgin fark yaratan özelliklerden biri.

### Bildirim ve Stok Otomasyonu — temel zaten sağlam, tek eksik toplu işlem

Curalis'in bugünkü davranışı rakiple büyük ölçüde örtüşüyor: bildirimde Al/Ertele/Atla seçenekleri var, "Al" seçildiğinde stok otomatik düşüyor, stok eşiğin altına inince ayrı bir uyarı bildirimi gidiyor. Gerçek fark şu: aynı saatte birden fazla ilaç varsa Curalis her biri için ayrı bildirim gönderiyor; rakip uygulama bunları tek bildirimde birleştirip yanına bir "Hepsini Al" kısayolu ekliyor.

### Doktor için PDF Raporu — altyapı hazır, veri bağlantısında küçük ama etkili bir eksik

Curalis'in raporu telefonda üretiliyor ve paylaşım menüsünden gönderilebiliyor — bu iyi bir temel. Ancak rapordaki "alınan/atlanan/kaçırılan" özet sayıları şu an hiçbir ekrandan gerçek veriyle beslenmiyor, hep sıfır olarak görünüyor. Ayrıca ana ekrandaki rapor kısayolu, ilaç listesini boş göndererek çalışıyor. İlginç bir not: rakip uygulamanın "PDF"i aslında kendi sunucusunda üretiliyor ve uygulama içinde şu an fiilen kullanılan yol bir tablo/e-tablo dosyası gönderiyor, klasik anlamda tasarlanmış bir PDF değil. Yani Curalis'in yerelde PDF üretebilen mevcut altyapısı, aslında rakibinkinden daha doğrudan ve bağımsız bir yaklaşım — sadece veri bağlantısı tamamlanmalı.

---

## 3. Somut aktarım ve tasarım adımları

Aşağıdaki sıralama rastgele değil: bir sonraki aşama, bir öncekinin üzerine kuruluyor. Örneğin takvim ekranı, alarm motoru güvenilir olmadan anlamlı veri gösteremez.

**1. Veri Modeli → 2. Alarm Güvenilirliği → 3. Takvim & Zaman Çizelgesi → 4. Görsel Tasarım → 5. Bildirim İyileştirme → 6. PDF Rapor**

### Aşama 1 — Veri modelini tamamla

*Neden önce bu: sonraki her ekran (takvim, zaman çizelgesi, ilaç kartı) bu alanlara ihtiyaç duyacak.*

- İlaç ekleme/düzenleme ekranına aç/tok/yemek talimatı seçimini ekle (dört sabit seçenek + serbest not).
- Zaten var olan ama ekranda görünmeyen serbest not alanını ilaç ekleme/düzenleme ekranına ekle.
- Doz birimini serbest metinden, yaygın birimlerden oluşan sabit bir listeye çevir.
- Kullanılmayan döngüsel doz alanlarına karar ver: ya basit bir "aktif gün / ara gün" özelliğiyle tamamla, ya da kaldır.
- *(İsteğe bağlı, farklılaştırıcı)* Son kullanma tarihi alanı ve "yakında sona eriyor" uyarısı ekle — rakipte de olmayan bir özellik.

### Aşama 2 — Alarm motorunu sağlamlaştır

*Neden önce bu: takvim ve zaman çizelgesi, doğru "kaçırıldı" ve "alındı" verisine dayanıyor; bu veri güvenilir değilse yeni ekranlar yanlış bilgi gösterir.*

- Cihaz yeniden başlatıldığında kurulu tüm hatırlatıcıların otomatik olarak geri yüklenmesini sağla.
- Hatırlatma gönderildikten belirli bir süre sonra hâlâ yanıtsız kalan dozları otomatik olarak "kaçırıldı" durumuna geçiren bir arka plan kontrolü ekle.
- Sabit 10 dakikalık ertelemeyi, kullanıcının birkaç süre arasından seçebildiği bir seçeneğe çevir.
- Hatırlatma anında ekranı uyandıran ve kilit ekranının üzerinde açılan tam ekran bir uyarı sayfası oluştur; aynı al/atla/ertele seçeneklerini burada da sun.

### Aşama 3 — Takvim ve zaman çizelgesi ekranlarını kur

*Bu, kullanıcının en çok fark edeceği görsel boşluk ve raporun asıl talebiydi.*

- Gerçek bir ay görünümü takvimi oluştur: her gün, o güne ait dozların özetine göre yeşil/sarı/kırmızı/gri renklendirilsin. (Curalis'in mevcut tarih aralığı sorgusu bu veriyi zaten sağlayabilecek durumda — sadece yeni bir ekran ve renklendirme mantığı gerekiyor.)
- Bir günlük zaman çizelgesi ekranı oluştur: dozlar saate göre gruplu, her satırda durum işareti (alındı/atlandı/kaçırıldı/ertelendi), aynı saatteki birden fazla doz için toplu işaretleme kısayolu.
- İsteğe bağlı, daha basit bir katman olarak günü sabah/öğle/akşam/gece dilimlerine ayıran bir sekmeli görünüm ekle.
- Bu iki yeni ekranı ana ekran ve ilaç listesi ekranından erişilebilir hale getir.

### Aşama 4 — Görsel tasarım sistemini zenginleştir

*Yeni ekranlar (Aşama 3) devreye girdikten sonra bu bileşenler hem kartlarda hem takvimde hem zaman çizelgesinde tutarlı görünmeli.*

- Emoji tabanlı ilaç ikonlarını, kullanıcının kendi rengini seçebildiği küçük bir özel ikon setiyle değiştir; seçilen renk kart, takvim ve zaman çizelgesinde tutarlı şekilde kullanılsın.
- Stok uyarı etiketini, uygulamanın her yerinde kullanılabilecek ortak bir rozet bileşenine dönüştür.
- Geçmişe dönük bir uyum özeti (uzun dönem ilerleme halkası veya basit bir trend grafiği) ekle — bu, Curalis'in kendi yol haritasında zaten planlanan "Geçmiş ve İçgörüler" bölümüyle örtüşüyor.

### Aşama 5 — Bildirimlerde toplu işlemi tamamla

*Diğer bildirim/stok davranışları zaten rakiple aynı seviyede; bu tek eksik kaldı.*

- Aynı saate denk gelen birden fazla ilacı tek bildirimde birleştir ve yanına, tek tek seçeneklerin yanı sıra bir "Hepsini Al" kısayolu ekle.

### Aşama 6 — PDF raporunu tamamla

*Bu bir onarım, sıfırdan inşa değil — üretim altyapısı zaten çalışıyor.*

- Rapor ekranına, gerçek "alındı/atlandı/kaçırıldı" sayılarını ve ana ekrandan tetiklendiğinde gerçek ilaç listesini ilet.
- *(İsteğe bağlı)* Aşama 3'te oluşan takvim/zaman çizelgesi görselini, raporun içine küçük bir özet olarak ekleyerek raporu doktor için daha okunaklı hale getir.

---

*Bu belge, Curalis projesinin iç ürün ve mimari değerlendirmesi olarak hazırlanmıştır. Karşılaştırma amaçlı incelenen rakip uygulamanın adı yalnızca bu raporda, referans amacıyla geçer; yukarıdaki hiçbir öneri Curalis kod tabanında bu ada veya üçüncü taraf materyale atıfta bulunmayı içermez.*
