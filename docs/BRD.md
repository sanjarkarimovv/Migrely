# Migrely — Business Requirements Document (BRD)

| | |
|---|---|
| Versiya | 1.0 |
| Sana | 2026-10-23 |
| Holat | Tasdiqlangan (Product Owner qarorlari bilan) |
| Platforma | Android, Google Play, butun dunyo |
| Nom | Migrely (trademark tekshiruvidan keyin yakuniy) |

---

## 1. Qisqacha

Migrely — migrenli odamlar uchun Android ilova. U oflayn ishlaydi, akkaunt talab qilmaydi, xurujni 3 bosishda yozadi va shifokorga tayyor hisobot beradi.

Biznes model — freemium: foydalanuvchi kiritgan hamma narsa bepul va hech qachon qulflanmaydi; ilova hisoblab beradigan chuqur tahlil va hisobotlar premium.

Ilova butun dunyoda chiqadi. Asosiy daromad bozorlari: AQSH, Buyuk Britaniya, Kanada, Avstraliya, Germaniya. MVP taxminan 6–7 haftada, production'ga chiqish taxminan 8–10 haftada.

---

## 2. Biznes konteksti (dalillar)

Manba: Google Play AQSH qidiruvi tahlili (2026-10-23), 58 ta migren ilovasi va 800+ sharh; RevenueCat State of Subscription Apps 2026.

- **Talab bor va yangi ilovalar ham oʻsa oladi.** 2025–2026-yilda chiqqan va top-10 ga kirgan yangi migren ilovalari oyiga median ~380, eng yaxshilari ~1 000–1 550 oʻrnatish olmoqda. Tekshirilgan 6 ta nisha ichida bu eng yaxshi natija (perimenopauza ~100, PCOS ~110, IBS ~200).
- **Odamlar pul toʻlaydi.** Migraine Buddy va Migraine Insight subscription sotadi.
- **Lider kuchli, lekin zaif joylari bor.** Migraine Buddy (3,2M oʻrnatish, 4.82) sharhlarida: kun davomidagi ogʻriq oʻzgarishi pullik qilingan, doimiy GPS ruxsati soʻraladi, akkaunt majburiy, kiritish noqulay (60 daqiqalik gʻildirak).
- **Tarixni qulflash foydalanuvchini haydaydi.** Migraine Insight oʻtgan oylar tarixini pullik qilgani uchun foydalanuvchilar ketmoqda.
- **Eksport yoʻq.** Headache Calendar maʼlumotni kompyuterga chiqarib bermaydi.
- **Raqobat zich.** 2025–2026-yilda nishaga 42 ta yangi ilova kirgan. Demak, ustunlik kodda emas: sifat, sharhlardagi muammolarni hal qilish va ASO hal qiladi.
- **Realistik kutish.** Android'da yuklab olishdan toʻlovgacha 0,9% (iOS'da 2,6%), trial'dan toʻlovga ~32%. Yangi subscription ilovaning median daromadi 1 yildan keyin ~$72/oy. Faqat 17,3% ilova 2 yilda oyiga $1K ga yetadi.

---

## 3. Mahsulot tamoyillari

Har qanday bahsli qarorda shu tamoyillar hal qiluvchi hisoblanadi.

1. **Tezlik.** Xurujni ilova ochilgandan ≤3 bosishda va ≤5 soniyada yozish mumkin. Faqat boshlanish vaqti majburiy.
2. **Maʼlumot foydalanuvchiniki.** Kiritilgan maʼlumot hech qachon pullik qilinmaydi va istalgan vaqtda bepul eksport qilinadi.
3. **Kiritish bepul, tahlil premium.** Foydalanuvchi kiritadigan hamma narsa bepul. Ilova hisoblab beradigan chuqur tahlil, trendlar va hisobotlar premium.
4. **Sogʻliq maʼlumoti telefondan chiqmaydi.** Akkaunt yoʻq, server yoʻq.
5. **Xuruj paytidagi odam uchun dizayn.** Qorongʻi va past yorugʻlik, katta tugmalar, minimal matn.
6. **Tashxis qoʻymaymiz.** Ilova kundalik va maʼlumot vositasi. Xavfli belgilarda shifokorga yoʻnaltiramiz.
7. **Dark pattern yoʻq.** Paywall'ning yopish tugmasi doim koʻrinadi, trial tugashidan oldin ogohlantiramiz.

---

## 4. Maqsadlar va KPI

Launch'dan keyingi 90 kun uchun:

| KPI | Maqsad | Qayerdan oʻlchanadi |
|---|---|---|
| Oʻrnatishlar | Oyiga ≥1 000 | Play Console |
| Onboarding'ni oxirigacha oʻtganlar | ≥60% | Analytics |
| Birinchi xurujni yozganlar (aktivatsiya) | ≥40% | Analytics |
| Oʻrnatishdan trial'ga | ≥3% | RevenueCat |
| Trial'dan toʻlovga | ≥20% | RevenueCat |
| Reyting | ≥4.5 | Play Console |
| Crash-free foydalanuvchilar | ≥99,5% | Android vitals |
| ANR darajasi | <0,47% | Android vitals |
| 1-yil oxiridagi MRR | $300–1 000 | RevenueCat |

**Qaror nuqtasi (launch + 60–90 kun):**
- Agar trial boshlash <3% boʻlsa: onboarding, paywall va listing'ni tuzatamiz.
- Agar trial'dan toʻlovga <20% boʻlsa: premium qiymatini qayta koʻrib chiqamiz.
- Agar ikkalasi yaxshi, lekin oʻrnatishlar kam boʻlsa: ASO va marketing ustida ishlaymiz.

---

## 5. Personalar

| Persona | Kim | Asosiy ehtiyoj | MVP nima beradi |
|---|---|---|---|
| **P1, epizodik migren** (asosiy) | Asosan 25–45 yoshli ayol, oyiga 2–8 ta xuruj | Tez yozish, shifokorga hisobot, hayz bilan bogʻliqlikni bilish | Tez yozish, hayz kunlari, PDF hisobot, triggerlar |
| **P2, surunkali migren** (asosiy) | Oyiga 15 kundan koʻp ogʻriydi, kun boʻyi ogʻriq oʻzgaradi, koʻp dori ichadi | Uzoq xurujni yozish, dorini koʻp ichish xavfi | Ogʻriq grafigi (bir nechta nuqta), tugamaydigan xuruj rejimi, overuse ogohlantirishi |
| **P3, AQSH faxriysi** (ikkinchi darajali) | VA nogironlik arizasi uchun hujjat yigʻadi | "Yotib qolgan" xurujlar soni | "Funksional taʼsir" maydoni va hisobotda uning soni. VA'ga maxsus shablon v1.1+ da |

---

## 6. Scope

### 6.1 MVP (v1.0)

1. Xurujni yozish (tez yozish, oʻtgan xurujni qoʻshish, tahrirlash)
2. Tarix: kalendar va roʻyxat
3. Dorilar va overuse ogohlantirishi
4. Hayz kunlari va ogʻriqsiz kunlarni belgilash
5. Oddiy oylik statistika
6. Eslatmalar
7. Maʼlumotni eksport va import qilish (JSON, CSV)
8. Onboarding
9. Subscription va paywall
10. Shifokor uchun PDF hisobot (premium)
11. Trigger va patternlar tahlili (premium)
12. Sozlamalar, maxfiylik, xavfsizlik maʼlumotlari
13. App shortcut: ilova belgisini bosib turib "Xuruj boshlandi"
14. "Sizning osmoningiz" bosh ekrani va xuruj paytidagi "qorongʻi xona" rejimi (18-boʻlim)

### 6.2 Keyinroq (v1.1+, bozor maʼlumotiga qarab)

- Home screen widget
- Biometrik qulf (app lock)
- Havo bosimi bilan bogʻliqlik (GPS'siz, shahar boʻyicha)
- Kundalik check-in: uyqu, stress, ovqat
- VA hisobot shabloni
- DE, ES, FR, JA tillari
- Health Connect (uyqu maʼlumoti)
- Profilaktik dorilarni muntazam ichishni kuzatish
- Wear OS
- Yillik osmon koʻrinishi (12 oy bir ekranda)
- Foydalanuvchi oʻzi xohlasa, oylik osmon rasmini ulashish (share sheet orqali)

### 6.3 MVP doirasidan tashqari

- Cloud sync va akkaunt
- Hamjamiyat va chat
- AI chatbot yoki tibbiy maslahat
- Reklama
- iOS (bozor sinovidan keyin alohida qaror qilinadi)

---

## 7. Monetizatsiya

### 7.1 Bepul va premium

| Bepul (abadiy) | Premium |
|---|---|
| Cheksiz xuruj yozish, tahrirlash | Shifokor uchun PDF hisobot |
| Toʻliq tarix: kalendar va roʻyxat | Trigger va patternlar tahlili |
| Dorilar, samaradorlik soʻrovi, **overuse ogohlantirishi** | 12 oylik trendlar |
| Hayz va ogʻriqsiz kunlar | Dorilar samaradorligi tahlili |
| Oddiy oylik statistika va **surunkali migren indikatori** | Funksional taʼsir tahlili |
| Barcha eslatmalar | |
| JSON va CSV eksport, import | |

Xavfsizlik funksiyalari (overuse ogohlantirishi, surunkali migren indikatori, xavfli belgilar) **hech qachon** premium boʻlmaydi.

### 7.2 Mahsulotlar va narxlar

| Mahsulot | AQSH narxi | Shartlar |
|---|---|---|
| Yillik subscription | $29.99 | 7 kunlik bepul trial, faqat yangi obunachilar uchun |
| Oylik subscription | $4.99 | Trial yoʻq |
| Lifetime | $59.99 | Bir martalik xarid (non-consumable) |

Boshqa davlatlarda narxni Play avtomatik konvertatsiya qiladi. Hindiston, Braziliya, Indoneziya kabi past xarid qobiliyatli bozorlarda AQSH narxining ~50% iga qoʻlda tushiramiz. Ilovada narx hech qachon qattiq yozilmaydi: doim Play'dan olingan mahalliy narx koʻrsatiladi.

### 7.3 Paywall qoidalari

- **Qayerda chiqadi:** onboarding oxirida, premium funksiya bosilganda va "qiymat lahzasida" (5-xuruj yozilgandan keyin: "patternlaringizni koʻring").
- **Qanchalik tez-tez:** foydalanuvchi oʻzi bosmagan paywall onboarding'dan keyin 7 kunda koʻpi bilan 1 marta chiqadi.
- **Nima yoziladi:** narx, davr, trial shartlari va qanday bekor qilish (Play subscription policy talabi).
- **Yopish tugmasi** birinchi soniyadan koʻrinadi.
- **Trial tugashidan 1 kun oldin** ilova ichida eslatma va banner koʻrsatiladi.

---

## 8. Funksional talablar

### ATK — Xurujni yozish

- **ATK-F1.** Bosh ekranda katta "Xuruj boshlandi" tugmasi. Bosilganda boshlanish vaqti hozirgi vaqt boʻlgan davom etayotgan xuruj yaratiladi. Keyin ixtiyoriy bir bosishli ogʻriq kuchi soʻraladi: yengil (3), oʻrtacha (6), kuchli (9).
- **ATK-F2.** "Xuruj tugadi" tugmasi tugash vaqtini hozirgi vaqtga qoʻyadi. Keyin ixtiyoriy savol: "qaysi dori yordam berdi?".
- **ATK-F3.** "Oʻtgan xurujni qoʻshish": boshlanish va tugash vaqti qoʻlda kiritiladi.
- **ATK-F4.** Ogʻriq 1–10 shkalada. Bir xurujga istalgan vaqtda yangi ogʻriq nuqtasi (vaqt, kuch) qoʻshish mumkin. Xurujning eng yuqori ogʻrigʻi shu nuqtalarning maksimumi.
- **ATK-F5.** Ixtiyoriy maydonlar:
  - **Joylashuv** (bir nechtasini tanlash mumkin): chap, oʻng, ikkala tomon, peshona, ensa, koʻz atrofi, butun bosh.
  - **Belgilar:** aura, koʻngil aynishi, qusish, yorugʻlikka sezgirlik, tovushga sezgirlik, hidga sezgirlik, bosh aylanishi, boʻyin ogʻrigʻi, shuningdek foydalanuvchining oʻz belgilari.
  - **Triggerlar:** stress, kam uyqu, koʻp uyqu, ovqatni oʻtkazib yuborish, suvsizlik, alkogol, kofein, hayz, ob-havo, yorugʻ chiroq yoki ekran, hid, jismoniy zoʻriqish, sayohat, shuningdek foydalanuvchining oʻz triggerlari.
  - **Funksional taʼsir:** 0 — taʼsir yoʻq; 1 — faoliyat kamaydi; 2 — ishni toʻxtatishga majbur boʻldim; 3 — yotib qoldim.
  - **Ichilgan dorilar:** vaqti va dozasi bilan.
  - **Izoh:** 1 000 belgigacha.
- **ATK-F6.** Har bir oʻzgarish darhol avtomatik saqlanadi. "Saqlash" tugmasi yoʻq.
- **ATK-F7.** Oʻchirishda dialog chiqmaydi: 5 soniyalik "Bekor qilish" (Undo) snackbar koʻrsatiladi.

### HIS — Tarix

- **HIS-F1.** Oylik kalendar. Kun rangi oʻsha kungi eng kuchli ogʻriqqa qarab beriladi. Hayz kunlari va ogʻriqsiz kunlar alohida belgi bilan koʻrinadi. Davom etayotgan xuruj ajratib koʻrsatiladi.
- **HIS-F2.** Xurujlar roʻyxati, yangilari tepada, sahifalab yuklanadi (paging).
- **HIS-F3.** Kunni bosish shu kungi xurujlarni, ogʻriqsiz kun va hayz belgilarini koʻrsatadi va tahrirlash imkonini beradi.

### MED — Dorilar

- **MED-F1.** Dori qoʻshish: nomi (erkin matn, MVP'da dori bazasi yoʻq), turi (xurujni toʻxtatuvchi yoki profilaktik), standart doza.
- **MED-F2.** Xurujni toʻxtatuvchi dorining toifasi: oddiy ogʻriq qoldiruvchi (ibuprofen, paratsetamol, aspirin); triptan; kombinatsiyalangan yoki boshqa.
- **MED-F3.** Dorini xuruj ichida yoki xurujsiz yozish mumkin. Ikkalasi ham overuse hisobiga kiradi.
- **MED-F4.** Dori ichilgandan 2 soat keyin eslatma: "Yordam berdimi?". Javoblar: yoʻq, biroz, yaxshi, toʻliq.
- **MED-F5.** Overuse ogohlantirishi (ICHD-3 mezonlari asosida, bepul). Oxirgi 30 kunda dori ichilgan kunlar sanaladi:
  - oddiy ogʻriq qoldiruvchilar uchun chegara 15 kun;
  - triptan, kombinatsiyalangan va boshqa dorilar uchun chegara 10 kun.

  Chegaradan 2 kun oldin yumshoq eslatma, chegarada esa "Shifokoringiz bilan maslahatlashing" xabari chiqadi. Bu xabar maʼlumot beradi, tashxis qoʻymaydi.

### DAY — Kunlik belgilar

- **DAY-F1.** Hayz kunlarini belgilash: kalendarda bosish yoki tez tugma orqali. Onboarding'da yoqiladi yoki oʻchiriladi va sozlamalarda oʻzgartirish mumkin.
- **DAY-F2.** "Ogʻriqsiz kun" belgisi: kalendardan yoki kunlik eslatmadagi tugmadan qoʻyiladi.

### STA — Oddiy statistika (bepul)

- **STA-F1.** Oy boʻyicha koʻrsatiladi:
  - xurujlar soni;
  - bosh ogʻrigʻi kunlari;
  - belgilangan ogʻriqsiz kunlar;
  - oʻrtacha eng yuqori ogʻriq;
  - dori ichilgan kunlar.
- **STA-F2.** Surunkali migren indikatori: oyiga ≥15 ta bosh ogʻrigʻi kuni boʻlsa, "Bu koʻp — shifokor bilan gaplashing" maʼlumot kartasi chiqadi.

### INS — Tahlil (premium)

- **INS-F1.** Eng koʻp uchraydigan triggerlar: xurujlarning necha foizida qayd etilgani.
- **INS-F2.** Patternlar: xurujlar haftaning qaysi kuni va kunning qaysi vaqtida koʻproq boʻlishi.
- **INS-F3.** Hayz bilan bogʻliqlik: xurujlarning qancha qismi hayzdan 2 kun oldin va hayzning dastlabki 3 kunida boʻlgani.
- **INS-F4.** Dorilar samaradorligi: har bir dori necha foiz holatda "yaxshi" yoki "toʻliq" yordam bergani.
- **INS-F5.** 12 oylik trendlar: xurujlar, bosh ogʻrigʻi kunlari, dori kunlari va funksional taʼsir.
- **INS-F6.** Matnda sabab-oqibat aytilmaydi. "Koʻpincha … bilan birga uchraydi" deb yoziladi, "… sabab boʻladi" deb yozilmaydi.

### REP — Shifokor hisoboti (premium)

- **REP-F1.** Davr tanlanadi: 1, 3, 6, 12 oy yoki ixtiyoriy oraliq. Default — 3 oy.
- **REP-F2.** Hisobot tarkibi:
  - 1-sahifada xulosa, shifokor uni 30 soniyada oʻqiy olishi kerak: oyiga oʻrtacha bosh ogʻrigʻi kunlari, xurujlar soni, dori kunlari va overuse holati, "yotib qolgan" xurujlar soni;
  - kalendar issiqlik xaritasi;
  - xurujlar jadvali;
  - dorilar jadvali;
  - ixtiyoriy ravishda bemor izohlari.
- **REP-F3.** Har bir sahifa pastida yoziladi: "Bemor tomonidan kiritilgan maʼlumot. Tashxis emas."
- **REP-F4.** Hisobot Android share sheet orqali ulashiladi (FileProvider). Xotiraga yozish ruxsati soʻralmaydi.

### BAK — Eksport va import (bepul)

- **BAK-F1.** JSON eksport: barcha maʼlumot, sxema versiyasi bilan. Qayta import qilish uchun moʻljallangan.
- **BAK-F2.** CSV eksport: Excel'da ochish uchun jadval.
- **BAK-F3.** JSON import ikki rejimda ishlaydi: "hammasini almashtirish" yoki "qoʻshish" (bir xil ID'lar oʻtkazib yuboriladi).
- **BAK-F4.** Android Auto Backup yoqilgan: maʼlumot bazasi va sozlamalar zaxiralanadi, cache va PDF'lar zaxiraga kirmaydi.
- **BAK-F5.** Fayllar Storage Access Framework orqali tanlanadi. Xotira ruxsati soʻralmaydi.

### REM — Eslatmalar

- **REM-F1.** Kunlik eslatma "Bugun qanday?". Onboarding'da taklif qilinadi, default vaqt 20:00. Bildirishnomada ikki tugma bor: "Ogʻriqsiz kun" va "Xurujni yozish".
- **REM-F2.** Dori samaradorligi eslatmasi (MED-F4).
- **REM-F3.** Trial tugashidan 1 kun oldin eslatma.

### ONB — Onboarding

- **ONB-F1.** Koʻpi bilan 4 ta ekran:
  1. Maqsad: shifokor uchun kuzatish, triggerlarni topish yoki dorini kamaytirish.
  2. Xurujlar chastotasi: oyiga <4, 4–14 yoki ≥15 marta. Bu UI'ni moslashtiradi.
  3. Hayz kuzatish: ha, yoʻq yoki oʻtkazib yuborish.
  4. Kunlik eslatma.

  Undan keyin paywall chiqadi.
- **ONB-F2.** Har bir qadamni oʻtkazib yuborish mumkin. Onboarding tugamasa ham ilova toʻliq ishlaydi.
- **ONB-F3.** Bir qatorli tibbiy disclaimer koʻrsatiladi, uni oʻqib chiqishga majburlovchi devor yoʻq.

### SUB — Subscription

- **SUB-F1.** Paywall'da uchta mahsulot, narxlar Play'dan olinadi.
- **SUB-F2.** Sozlamalarda: obuna holati, trial tugash sanasi, "Xaridlarni tiklash" tugmasi va "Obunani boshqarish" (Play'ga havola).

### SET — Sozlamalar

- **SET-F1.** Sozlamalar roʻyxati:
  - tema: qorongʻi (default), yorugʻ yoki tizim;
  - hayz kuzatish;
  - eslatmalar;
  - eksport va import;
  - "Hamma maʼlumotni oʻchirish" (tasdiqlash uchun "DELETE" soʻzini yozish kerak);
  - analytics'ni oʻchirish;
  - privacy policy;
  - disclaimer;
  - litsenziyalar;
  - versiya.

### SAF — Tibbiy xavfsizlik (bepul)

- **SAF-F1.** "Xavfli belgilar" kartasi Yordam boʻlimida turadi:
  - toʻsatdan boshlangan va hayotdagi eng kuchli ogʻriq;
  - jarohatdan keyingi ogʻriq;
  - isitma bilan birga boʻyin qotishi;
  - oʻtib ketmaydigan zaiflik yoki gapirish qiyinlashishi;
  - 50 yoshdan keyin birinchi marta boshlangan ogʻriq.
- **SAF-F2.** Foydalanuvchi birinchi marta 10 ballik ogʻriq yozganda bloklamaydigan karta chiqadi: "Agar bu toʻsatdan boshlangan, hayotingizdagi eng kuchli ogʻriq boʻlsa — tez yordam chaqiring".
- **SAF-F3.** Xuruj 72 soatdan oshsa, "Hali davom etyaptimi?" soʻraladi va yonida maʼlumot beriladi: "72 soatdan uzoq xuruj — shifokorga murojaat qiling".

---

## 9. Edge case qarorlari

### 9.1 Xurujlar va maʼlumot

| ID | Holat | Qaror | MVP |
|---|---|---|---|
| EC-ATK-01 | Davom etayotgan xuruj bor va yana "Xuruj boshlandi" bosildi | Yangi xuruj yaratilmaydi. Mavjud xuruj ochiladi va "Ogʻriqni yangilash" yoki "Tugatish" taklif qilinadi. Bir vaqtda faqat 1 ta davom etayotgan xuruj boʻladi | ✅ |
| EC-ATK-02 | Xuruj 72 soatdan oshdi (tugatish esdan chiqqan yoki surunkali migren) | Avtomatik yopilmaydi. "Hali davom etyaptimi?" soʻraladi: "Ha", "Tugadi (vaqtni tanlash)" yoki "Bu xurujda boshqa soʻrama". Yonida SAF-F3 maʼlumoti | ✅ |
| EC-ATK-03 | Boshlanish yoki tugash vaqti kelajakda | Ruxsat berilmaydi. 5 daqiqalik tolerantlik bor (soat farqi uchun). Xato xabari forma ichida chiqadi | ✅ |
| EC-ATK-04 | Tugash vaqti boshlanishdan oldin | Saqlanmaydi, forma ichida xato koʻrsatiladi | ✅ |
| EC-ATK-05 | Yangi xuruj mavjud xuruj bilan ustma-ust tushadi | Saqlanmaydi. Ikki variant taklif qilinadi: "Birlashtirish" (bitta xuruj, vaqtlar kengayadi, maʼlumotlar qoʻshiladi) yoki "Vaqtni oʻzgartirish" | ✅ |
| EC-ATK-06 | Xuruj yarim tundan oʻtadi | Bu bitta xuruj. Kalendarda u tegib oʻtgan har bir kun bosh ogʻrigʻi kuni sifatida belgilanadi. Statistikada xuruj boshlangan kuni boʻyicha 1 marta sanaladi | ✅ |
| EC-ATK-07 | Bir kunda bir nechta xuruj | Ruxsat beriladi (ustma-ust boʻlmasa). Kalendar rangi oʻsha kungi eng kuchli ogʻriqqa qarab beriladi | ✅ |
| EC-ATK-08 | Ogʻriq kuchi kiritilmagan | Xuruj saqlanadi, kuchi "nomaʼlum" boʻladi. Kuchga bogʻliq statistikaga kirmaydi, sanoqqa kiradi | ✅ |
| EC-ATK-09 | Ogʻriq nuqtalari xuruj vaqti oraligʻidan tashqarida | Ruxsat berilmaydi. Tugash vaqti oʻzgarsa va nuqtalar tashqarida qolsa, ogohlantirish chiqadi va ular oʻchiriladi (Undo bilan) | ✅ |
| EC-ATK-10 | Ilova kiritish oʻrtasida yopildi (process death) | Har bir maydon darhol saqlangani uchun (ATK-F6) maʼlumot yoʻqolmaydi. Ochilmagan dialog holati tiklanmaydi | ✅ |
| EC-ATK-11 | Foydalanuvchi oʻzi qoʻshgan trigger yoki belgini oʻchirdi | Tarixdagi yozuvlarda saqlanib qoladi (arxivlanadi), yangi tanlash roʻyxatida koʻrinmaydi | ✅ |
| EC-ATK-12 | Oʻzi qoʻshadigan teglar chegarasi | Nom 1–40 belgi, takror nom (katta-kichik harf farqisiz) qoʻshilmaydi, har turdan koʻpi bilan 100 ta | ✅ |
| EC-ATK-13 | Izoh 1 000 belgidan uzun | Kiritish 1 000 belgida toʻxtaydi, hisoblagich koʻrinadi | ✅ |
| EC-ATK-14 | Oʻchirilgan xurujni qaytarish | Faqat 5 soniyalik Undo. Keyin butunlay oʻchadi | ✅ |
| EC-ATK-15 | Yangi yoki oʻzgartirilgan vaqt bir nechta xuruj bilan ustma-ust tushadi | Hammasi vaqtlari bilan sanab koʻrsatiladi. "Birlashtirish" ularning hammasini bitta xurujga qoʻshadi (D27) | ✅ |
| EC-ATK-16 | Birlashtirilgan izohlar 1 000 belgidan oshadi | Izohlar vaqt tartibida boʻsh qator bilan ulanadi. Ulangan matn 1 000 belgida oxiridan kesiladi, yaʼni eng yangi izohlar oxiridan qisqaradi. Belgi yarmidan kesilmaydi, oxirida boʻsh qator qolmaydi. Yagona izoh oʻzgarmaydi (EC-ATK-13) | ✅ |
| EC-ATK-17 | "Add migraine" yoki "Merge them" ikki marta tez bosildi | Faqat bitta xuruj yaratiladi yoki bir marta birlashtiriladi: tugma natija kelguncha bosilmaydi, saqlash esa bitta tranzaksiyada ustma-ust tushishni qayta tekshiradi | ✅ |
| EC-ATK-18 | Vaqt butun daqiqada tanlanadi, saqlangan vaqtda esa soniyalar bor, va ular bir daqiqadan kamroq ustma-ust tushadi | Bu ustma-ust tushish emas: vaqt qoʻshni xurujning chegarasiga yopishtiriladi (ular bir lahzada tegib turadi) | ✅ |

### 9.2 Vaqt

| ID | Holat | Qaror | MVP |
|---|---|---|---|
| EC-TIM-01 | Saqlash formati | Har bir vaqt UTC Instant va xuruj boshlangan joydagi vaqt zonasi (ZoneId) bilan saqlanadi | ✅ |
| EC-TIM-02 | Foydalanuvchi sayohatda, vaqt zonasi oʻzgardi | Xuruj qaysi kunga tegishli ekani u yozilgan joyning zonasi boʻyicha aniqlanadi, shuning uchun tarix "siljimaydi". Vaqtlar yozilgan zonada, zona belgisi bilan koʻrsatiladi | ✅ |
| EC-TIM-03 | Yozgi vaqtga oʻtish (DST) | Davomiylik Instant'lar orqali hisoblanadi, shuning uchun toʻgʻri chiqadi | ✅ |
| EC-TIM-04 | Telefon soati notoʻgʻri yoki qoʻlda oʻzgartirilgan | Maxsus himoya yoʻq, faqat EC-ATK-03 tolerantligi. Server vaqti ishlatilmaydi | ✅ |
| EC-TIM-05 | 12/24 soat formati, sana formati, hafta qaysi kundan boshlanishi | Tizim locale'idan olinadi | ✅ |

### 9.3 Dorilar

| ID | Holat | Qaror | MVP |
|---|---|---|---|
| EC-MED-01 | Tarixi bor dori oʻchirildi | Arxivlanadi: tarixda qoladi, tanlash roʻyxatida koʻrinmaydi | ✅ |
| EC-MED-02 | Bir kunda bir xil dori bir necha marta ichildi | Har bir qabul alohida yoziladi, lekin overuse hisobida 1 kun sanaladi | ✅ |
| EC-MED-03 | Bir kunda turli toifadagi dorilar ichildi | Har bir toifa oʻz chegarasi boʻyicha alohida sanaladi | ✅ |
| EC-MED-04 | Dori toifasi tanlanmagan | "Kombinatsiyalangan yoki boshqa" deb hisoblanadi, ya'ni ehtiyotkor 10 kunlik chegara qoʻllanadi | ✅ |
| EC-MED-05 | Bildirishnoma ruxsati yoʻq, "Yordam berdimi?" eslatmasi chiqmadi | Keyingi safar ilova ochilganda shu savol ilova ichida beriladi (24 soat ichida) | ✅ |
| EC-MED-06 | Overuse ogohlantirishi bir necha marta chiqishi | Har bir 30 kunlik davr va toifa uchun koʻpi bilan 1 marta. Doimiy indikator statistika ekranida turadi | ✅ |

### 9.4 Tarix, statistika va tahlil

| ID | Holat | Qaror | MVP |
|---|---|---|---|
| EC-HIS-01 | Hali maʼlumot yoʻq | Kalendar va statistika ekranida "Birinchi xurujni yozing" tugmasi va qisqa tushuntirish chiqadi | ✅ |
| EC-HIS-02 | Koʻp maʼlumot (10 yilda 3 600+ xuruj) | Roʻyxat sahifalab yuklanadi (paging), soʻrovlar indekslangan, kalendar faqat koʻrinayotgan oyni yuklaydi | ✅ |
| EC-INS-01 | Tahlil uchun maʼlumot yetarli emas | Tahlil kamida 5 ta xuruj va 30 kunlik tarix boʻlganda koʻrsatiladi. Undan oldin "Yana N ta xuruj kerak" degan progress chiqadi. Premium ekranida bu oldindan aytiladi | ✅ |
| EC-INS-02 | Hayz bilan bogʻliqlikni hisoblash uchun hayz maʼlumoti yetarli emas | Kamida 2 ta hayz sikli belgilangan boʻlishi kerak, aks holda bu boʻlim yashiriladi | ✅ |
| EC-INS-03 | Hayz kuzatish oʻchirilgan | Hayz bilan bogʻliq UI va tahlil butunlay yashiriladi | ✅ |

### 9.5 Hisobot, eksport va import

| ID | Holat | Qaror | MVP |
|---|---|---|---|
| EC-REP-01 | Tanlangan davrda maʼlumot yoʻq | "Yaratish" tugmasi oʻchiq, yonida sababi yoziladi | ✅ |
| EC-REP-02 | Juda katta davr | PDF fonda progress bilan yaratiladi. Xurujlar jadvali 50 sahifadan oshsa, hisobot qisqartiriladi va bu ochiq yoziladi | ✅ |
| EC-REP-03 | PDF yaratish oʻrtasida ilova yopildi | Ish bekor qilinadi, qisman fayl oʻchiriladi, keyingi ochilishda qayta yaratish mumkin | ✅ |
| EC-BAK-01 | Import fayli buzilgan yoki boshqa formatda | Hech narsa oʻzgarmaydi, aniq xato chiqadi. Import tranzaksiya ichida bajariladi: yo hammasi saqlanadi, yo hech narsa | ✅ |
| EC-BAK-02 | Fayl ilovaning yangiroq versiyasidan | Rad etiladi: "Ilovani yangilang" | ✅ |
| EC-BAK-03 | Fayl ilovaning eskiroq versiyasidan | Ilova ichidagi migratsiya bilan import qilinadi | ✅ |
| EC-BAK-04 | Import paytida bazada maʼlumot bor | Ikki variant soʻraladi: "Almashtirish" (oldin avtomatik zaxira olinadi) yoki "Qoʻshish" | ✅ |
| EC-BAK-05 | Yangi telefonda Auto Backup tiklanmadi | Onboarding'da "Avvalgi faylingizdan import qilish" havolasi bor | ✅ |

### 9.6 Eslatmalar va ruxsatlar

| ID | Holat | Qaror | MVP |
|---|---|---|---|
| EC-REM-01 | Android 13+ da bildirishnoma ruxsati | Faqat eslatma yoqilayotgan paytda soʻraladi, birinchi ochilishda emas | ✅ |
| EC-REM-02 | Ruxsat rad etildi | Eslatma oʻchiq qoladi. Sozlamalarda tushuntirish va tizim sozlamalariga havola koʻrinadi. Qayta soʻrash 30 kunda koʻpi bilan 1 marta | ✅ |
| EC-REM-03 | Aniq vaqtli eslatma (exact alarm) | Ishlatilmaydi: WorkManager orqali taxminiy vaqt (±15 daqiqa). Bu bilan maxsus ruxsat va policy deklaratsiyasi kerak boʻlmaydi | ✅ |
| EC-REM-04 | Telefon qayta yoqildi | WorkManager eslatmalarni oʻzi tiklaydi | ✅ |
| EC-REM-05 | Vaqt zonasi oʻzgardi | Kunlik eslatma yangi mahalliy vaqt boʻyicha qayta rejalashtiriladi | ✅ |
| EC-REM-06 | Bildirishnomadagi "Ogʻriqsiz kun" bosildi, lekin oʻsha kuni xuruj yozilgan | Belgi qoʻyilmaydi. "Bugun xuruj yozilgan" degan qisqa xabar chiqadi | ✅ |

### 9.7 Toʻlov

| ID | Holat | Qaror | MVP |
|---|---|---|---|
| EC-BIL-01 | Internet yoʻq | Oxirgi maʼlum premium holati saqlanadi va muddati tugaguncha, yana 3 kunlik zaxira bilan amal qiladi. Internet paydo boʻlganda yangilanadi | ✅ |
| EC-BIL-02 | Trial ichida bekor qilindi | Premium trial oxirigacha ishlaydi, pul yechilmaydi (Play boshqaradi). Sozlamalarda "Trial X sanada tugaydi" koʻrinadi | ✅ |
| EC-BIL-03 | Toʻlov oʻtmadi, grace period | Play Console'da grace period yoqiladi. Bu davrda premium davom etadi va "Toʻlov usulini yangilang" banneri koʻrinadi | ✅ |
| EC-BIL-04 | Account hold | Premium oʻchadi, maʼlumot toʻliq saqlanadi, Play'ga havolali banner chiqadi | ✅ |
| EC-BIL-05 | Refund yoki xarid bekor qilindi | Keyingi yangilanishda premium oʻchadi. Maʼlumot hech qachon oʻchirilmaydi | ✅ |
| EC-BIL-06 | Premium tugadi | Premium funksiyalar qulflanadi, kiritilgan maʼlumotlarning hammasi va bepul funksiyalar toʻliq qoladi | ✅ |
| EC-BIL-07 | Yangi telefon | Xarid oʻsha Google akkaunt orqali avtomatik tiklanadi. Qoʻshimcha ravishda "Xaridlarni tiklash" tugmasi bor | ✅ |
| EC-BIL-08 | Kutilayotgan (pending) toʻlov, masalan naqd toʻlov usullari | "Toʻlov kutilmoqda" holati koʻrsatiladi. Premium faqat toʻlov tasdiqlangandan keyin yoqiladi | ✅ |
| EC-BIL-09 | Obunasi faol foydalanuvchi lifetime sotib oldi | Xariddan keyin obunani Play'da qanday bekor qilish koʻrsatiladi. Ilova obunani oʻzi bekor qila olmaydi | ✅ |
| EC-BIL-10 | Oylikdan yillikka oʻtish | Play'ning standart upgrade va proration mexanizmi ishlatiladi | ✅ |
| EC-BIL-11 | Play billing mavjud boʻlmagan davlat | Bepul qism toʻliq ishlaydi. Paywall'da "Sizning hududingizda mavjud emas" yoziladi | ✅ |
| EC-BIL-12 | Family Library | MVP'da oʻchiq | ✅ |

### 9.8 Saqlash va ilovaning hayot sikli

| ID | Holat | Qaror | MVP |
|---|---|---|---|
| EC-STO-01 | Maʼlumot bazasi sxemasi oʻzgardi | Faqat yozilgan migratsiyalar ishlatiladi. Destructive migration taqiqlanadi, har bir migratsiya test qilinadi | ✅ |
| EC-STO-02 | Telefon xotirasi toʻla | Xato xabari chiqadi, kiritilgan maʼlumot UI'da qoladi va qayta urinish mumkin | ✅ |
| EC-STO-03 | "Hamma maʼlumotni oʻchirish" | "DELETE" soʻzini yozib tasdiqlash kerak. Maʼlumot bazasi, sozlamalar va fayllar oʻchiriladi. Premium holati saqlanadi, chunki u Play'da turadi | ✅ |
| EC-STO-04 | Maʼlumotni qoʻshimcha shifrlash | MVP'da faqat Android'ning standart shifrlashi (file-based encryption). Biometrik qulf v1.1 da | ✅ |

### 9.9 Accessibility va UX

| ID | Holat | Qaror | MVP |
|---|---|---|---|
| EC-UX-01 | Xuruj paytida yorugʻlikka sezgirlik | Default qorongʻi tema, sof oq rang ishlatilmaydi, miltillovchi animatsiyalar yoʻq. Tizimdagi "Reduce motion" sozlamasi hurmat qilinadi | ✅ |
| EC-UX-02 | Xuruj rejimidagi tugmalar | Kamida 64dp, matn minimal | ✅ |
| EC-UX-03 | Shrift 200% gacha kattalashtirilgan | Muhim maʼlumot kesilib qolmaydi, ekranlar scroll boʻladi | ✅ |
| EC-UX-04 | TalkBack | Barcha elementlarda tavsif bor. Ogʻriq slider'i accessibility action'lari bilan ishlaydi | ✅ |
| EC-UX-05 | RTL tillar | Layout oʻngdan chapga aks etishga tayyor (RTL tillar MVP'da yoʻq) | ✅ |
| EC-UX-06 | Planshet va katta ekran | Ishlaydi, lekin maxsus layout v1.1+ da | ✅ |
| EC-UX-07 | Xuruj rejimiga oʻtish animatsiyasi | Koʻpi bilan 1,5 soniya, sekin. Soniyasiga 3 tadan ortiq yorugʻlik oʻzgarishi (miltillash) yoʻq (WCAG 2.3.1). Chaqmoq hech qachon miltillamaydi, faqat xira chiziq | ✅ |
| EC-UX-08 | Tizimda "Reduce motion" yoqilgan | Animatsiyalar (yomgʻir, oʻtish) oʻynalmaydi, darhol yakuniy holat koʻrsatiladi | ✅ |
| EC-UX-09 | Kunning osmoni qanday aniqlanadi | Faqat maʼlumotdan: ogʻriqsiz deb belgilangan → ochiq; eng kuchli ogʻriq 1–3 → tumanli, 4–6 → bulutli, 7–10 → boʻron; ogʻriq kuchi nomaʼlum xuruj (EC-ATK-08) → bulutli. Hech narsa yozilmagan kun → punktir chegara, hech qachon "ochiq" deb taxmin qilinmaydi. Kelajak kunlar → boʻsh | ✅ |
| EC-UX-10 | Bir kunda bir nechta xuruj yoki davom etayotgan xuruj | Kun eng kuchli xuruj boʻyicha chiziladi (EC-ATK-07 bilan mos). Davom etayotgan xuruj bugungi kartada yomgʻir bilan koʻrinadi. Bugun hali belgilanmagan boʻlsa: "Calm so far today", "ochiq" emas | ✅ |
| EC-UX-11 | Maʼlumot import qilindi yoki oʻchirildi | Osmon va taraqqiyot xabari har safar maʼlumotlardan qayta hisoblanadi, alohida saqlanmaydi | ✅ |
| EC-UX-12 | Xuruj davom etayotganda ilova ochildi | Ilova darhol "qorongʻi xona" rejimida, animatsiyasiz ochiladi | ✅ |
| EC-UX-13 | Xuruj rejimi | Mukofot, paywall, baho soʻrash va taraqqiyot xabarlari koʻrsatilmaydi. Ekranda bir qator matn va katta tugmalar | ✅ |
| EC-UX-14 | Taraqqiyot xabari ("3 fewer migraine days than September") | Faqat yaxshilanish boʻlsa iliq rangda koʻrsatiladi. Yomonlashsa, neytral raqam beriladi, ayblov yoʻq. Oldingi oyda 10 kundan kam yozuv boʻlsa, taqqoslash koʻrsatilmaydi (ishonchsiz) | ✅ |
| EC-UX-15 | Rang koʻrlik va TalkBack | Holat faqat rang bilan emas, shakl bilan ham ajraladi: yulduz, toʻlqin, bulut, chaqmoq. TalkBack har bir kunni oʻqiydi, masalan "15 October, storm, severe migraine" | ✅ |
| EC-UX-16 | Ekrandagi qisqa xabarlar (snackbar) | Ekran tepasida, xira rangda. Xabar turi rang bilan birga ikonka shakli bilan ham ajraladi: tasdiq ✓, maʼlumot i, xato !. Undo 5 soniya turadi. Foydalanuvchi Android'da "Harakat qilish vaqti"ni (Accessibility timeout) uzaytirgan boʻlsa, shu vaqtgacha uzayadi. Qolgan vaqt ingichka chiziq bilan koʻrsatiladi, "Reduce motion"da chiziq chiqmaydi. Maʼlumot xabarlari va umumiy xato 8 soniya turadi. "Xotira toʻldi" foydalanuvchi oʻzi yopguncha turadi. Yangi xabar eskisining oʻrnini darhol egallaydi. Ekran burilsa yoki ilova qisqa vaqt fonga oʻtsa, xabar qolgan vaqti bilan saqlanadi. Xabarni surib yopish mumkin. Undo bajarilgach "Undone" deb tasdiqlanadi, bajarib boʻlmasa sababi aytiladi. TalkBack xabarni oʻqiydi, xato xabarini darhol oʻqiydi | ✅ |
| EC-UX-17 | Matn chiziqlari koʻzni charchatadi (pattern glare, migrenda kuchliroq) | Asosiy matnda qator oraligʻi taxminan 1,5 baravar. Ingichka (300 va undan past) ogʻirlik ishlatilmaydi. Vaqt va davomiylik tabular raqamlarda yoziladi, sanab turganda matn sakramaydi. Shrift ilova ichida keladi va internetsiz ishlaydi (D20) | ✅ |

### 9.10 Lokalizatsiya

| ID | Holat | Qaror | MVP |
|---|---|---|---|
| EC-L10N-01 | Matnlar | Hammasi string resource'larda, koʻplik shakllari plurals bilan. Ingliz tili 100% | ✅ |
| EC-L10N-02 | Nemis tilida matn uzunroq | Layout matn 40% ga uzayishiga moʻljallab quriladi | ✅ |
| EC-L10N-03 | Raqam va sana formatlari | Locale boʻyicha formatlanadi | ✅ |

---

## 10. Non-functional talablar

| Soha | Talab |
|---|---|
| Tezlik | Cold start mid-range telefonda <1,5 s. Tez yozish ≤3 bosish va ≤5 s |
| Hajm | APK/AAB yuklab olish hajmi <15 MB |
| Barqarorlik | Crash-free ≥99,5%. Foydalanuvchi sezgan crash <1,09% va ANR <0,47% (Play'ning "yomon xulq" chegaralari) |
| Oflayn | Bepul va premium funksiyalarning hammasi internetsiz ishlaydi (premium holati keshda turadi) |
| Maxfiylik | Sogʻliq maʼlumoti tarmoqqa umuman yuborilmaydi. Analytics'da sogʻliq qiymatlari yoʻq |
| Batareya | Fonda faqat eslatmalar ishlaydi (WorkManager) |
| Moslik | minSdk 26 (Android 8.0). targetSdk — Google Play talab qiladigan eng soʻnggi daraja |
| Accessibility | WCAG 2.1 AA kontrasti, TalkBack, 200% shrift |
| Test | Har bir feature uchun use case, mapper, ViewModel, repository va datasource unit testlari, Konsist arxitektura testlari, Room migratsiya testlari |

---

## 11. Policy, maxfiylik va tibbiy xavfsizlik

1. **Pozitsiya.** Ilova "headache diary & tracker". U tibbiy qurilma emas, tashxis qoʻymaydi va davolashni vaʼda qilmaydi. Disclaimer ilovada, listing'da va PDF'da bor (Play Deceptive Behavior policy).
2. **Health apps declaration.** Play Console → App content boʻlimidagi sogʻliq ilovalari deklaratsiyasi toʻldiriladi. Joriy shakl setup paytida tekshiriladi.
3. **Data safety.**
   - Sogʻliq maʼlumoti: "collected" emas, chunki u faqat qurilmada qayta ishlanadi.
   - Xaridlar tarixi: RevenueCat orqali toʻplanadi.
   - Ilova bilan oʻzaro taʼsir: anonim analytics orqali, oʻchirib qoʻyish mumkin.
   - Maʼlumotni oʻchirish imkoniyati bor.

   Deklaratsiya kodga toʻliq mos boʻlishi shart.
4. **Privacy policy.** Ochiq URL'da joylashtiriladi (GitHub Pages yoki oddiy sayt). Unga AQSH shtatlari qonunlari uchun alohida **Consumer Health Data** boʻlimi kiritiladi (Washington MHMDA, Nevada, Connecticut). Biz sogʻliq maʼlumotini toʻplamaymiz, lekin bu boʻlim har ehtimolga qarshi yoziladi.
5. **Maqsadli auditoriya: 18+.** Bu Families policy va COPPA talablaridan qochish uchun. Content rating anketasi halol toʻldiriladi.
6. **Reklama yoʻq.**
7. **Subscription policy.** Paywall narx, davr, trial shartlari va bekor qilish yoʻlini aniq koʻrsatadi.
8. **Metadata.** Listing'da raqobatchilarning nomlari ishlatilmaydi. "Migrely" nomi launch'dan oldin USPTO va EUIPO bazalarida tekshiriladi.
9. **Tibbiy mazmun.** Overuse chegaralari (ICHD-3), 72 soat (status migrainosus) va xavfli belgilar matni xalqaro bosh ogʻrigʻi tasnifiga tayanadi. Matnlarda "shifokor bilan maslahatlashing" deyiladi, buyruq berilmaydi.

---

## 12. Texnik qarorlar (ADR)

| # | Qaror | Sabab |
|---|---|---|
| ADR-01 | Native Kotlin va Jetpack Compose (Material 3) | Faqat Android; clean-arch skill'i Android uchun; app shortcut va keyinroq widget uchun eng yaxshi tanlov |
| ADR-02 | Clean Architecture (Boudjnah, 2-nashr), multi-module, 12 qoida, Konsist | Foydalanuvchi talabi; har bir feature'ni alohida test qilib, mustaqil rivojlantirish mumkin |
| ADR-03 | Navigation Compose 2 (type-safe route'lar). Nav3'ga `upgrade-toolchain` change'ida oʻtiladi | `cag` Nav2 yaratadi va kitob ham Nav2'dan foydalanadi. Feature kodi Nav2 va Nav3'da bir xil, shuning uchun keyingi oʻtish faqat `:app` va `:architecture:ui` ga tegadi (`docs/ARCHITECTURE.md` §5, §13) |
| ADR-04 | Room (maʼlumot) va DataStore (sozlamalar) | Oflayn-first; Auto Backup bilan mos |
| ADR-05 | Eslatmalar uchun WorkManager, exact alarm yoʻq | Maxsus ruxsat va policy deklaratsiyasi kerak boʻlmaydi (EC-REM-03) |
| ADR-06 | Billing uchun RevenueCat, `:datasource:billing` interfeysi ortida | Oʻz serverimiz boʻlmasa ham xaridlar serverda tekshiriladi; grace period va hold'ni boshqaradi. Interfeys ortida turgani uchun keyin Play Billing'ga almashtirish mumkin |
| ADR-07 | PDF uchun Android `PdfDocument` | Tashqi kutubxona va litsenziya kerak emas |
| ADR-08 | Eksport uchun kotlinx.serialization | Kotlin-native, sxema versiyalash oson |
| ADR-09 | Privacy-first analytics (TelemetryDeck), faqat funnel event'lari | Sogʻliq qiymatlari yuborilmaydi. Event'lar: onboarding qadamlari, paywall koʻrildi, birinchi xuruj yozildi (1 marta) |
| ADR-10 | Crashlytics yoʻq, Play Console Android vitals ishlatiladi | Maxfiylik vaʼdasi toza qoladi. Stack trace'lar mapping fayli orqali vitals'da koʻrinadi |
| ADR-11 | `applicationId = com.migrely.app`. Kelajakdagi iOS bundle ID ham shu boʻladi | Publish qilingandan keyin **oʻzgartirib boʻlmaydi**. Platformaga bogʻliq emas, shuning uchun ikkala store'da bir xil ID ishlatiladi |
| ADR-12 | CI: GitHub Actions — build, unit testlar, Konsist, lint gate (detekt/ktlint) | Skill qoidasi 12: "Done" ning tekshiruvi |
| ADR-13 | Arxitekturada yagona manbalar: `EranBoudjnah/CleanArchitectureForAndroid` sample repo, kitob (2-nashr) va skill qoidalari. Loyiha skeleti, har bir feature, use case, ViewModel va datasource `CleanArchitectureGenerator` bilan yaratiladi (`cag` CLI yoki Android Studio plugin) | Owner talabi — "toza" Clean Architecture. Generator boilerplate'ni bir xil yaratadi va qoʻlda xato qilish ehtimolini kamaytiradi |

**Modullar xaritasi** (toʻliq va yakuniy shakli `docs/ARCHITECTURE.md` §3 da):

- `:app` — Hilt, Navigation Compose 2, Application.
- `:architecture:*` — UseCase, UseCaseExecutor, bazaviy ViewModel va UI klasslari.
- `:features:<f>:domain | presentation | ui | data`, bu yerda `f` quyidagilardan biri: `attack`, `history`, `medication`, `daylog`, `insights`, `report`, `backup`, `reminder`, `onboarding`, `subscription`, `settings`.
- `:datasource:source` (JVM: interfeyslar va modellar) va `:datasource:implementation` (Android: `*Impl`). Bu sample va `cag` uslubi. Har bir resurs alohida package'da: `database`, `preferences`, `billing`, `scheduler`, `pdf`, `file`, `analytics`, `clock`.

Premium holati ham boshqa barcha narsalar kabi skill qoidasiga boʻysunadi: har bir feature uni oʻzining use case'i va repository interfeysi orqali `:datasource` dagi `billing` resursidan oʻqiydi. Feature'lar bir-biriga bogʻlanmaydi.

---

## 13. OpenSpec rejasi

**Setup:**

```bash
npm install -g @fission-ai/openspec@latest
openspec init
openspec config set telemetry.enabled false
```

Ish sikli: `/opsx:explore` (ixtiyoriy) → `/opsx:propose <nom>` → review → `/opsx:apply` → verify → `/opsx:archive`.

**Konvensiyalar:**

- **Change nomi:** kebab-case va feʼldan boshlanadi (`add-`, `update-`, `fix-`).
- **`proposal.md`:** nega va nima. Tegishli BRD ID'lariga (FR, EC) havola beradi.
- **Spec'lar** ingliz tilida yoziladi (`### Requirement:` … SHALL, `#### Scenario:` WHEN/THEN). Har bir EC qarori kamida bitta scenario'ga aylanadi.
- **`design.md`:** skill'ning `checklists/new-feature.md` tartibi boʻyicha qatlamlar va modullar.
- **`tasks.md`:** domain → presentation → data/datasource → UI → DI/navigation (`:app`) → testlar → verify.

**Definition of Done** (skill qoidasi 12):

- kod yozilgan;
- unit testlar yozilgan;
- Konsist testlari yashil;
- lint gate yashil;
- `./gradlew build` yashil;
- OpenSpec validate oʻtgan;
- change archive qilingan.

**Change'lar tartibi:**

| # | Change | BRD |
|---|---|---|
| 0 | `setup-project-foundation` — modullar skeleti, `docs/ARCHITECTURE.md`, dizayn tizimi (qorongʻi tema), Konsist, CI | ADR-01…12, NFR |
| 1 | `add-attack-logging` — tez boshlash/tugatish, ogʻriq kuchi, "qorongʻi xona", bugungi osmon, app shortcut, Room asosi | ATK-F1, F2, F4, F6, EC-ATK-01, 02, 03, 06, 07, 08, 10, EC-TIM-01…03, SAF-F3, EC-UX-07, 08, 10, 12, 13 |
| 1a | `refine-messages-and-type` — brend shrifti (Fraunces va Figtree), barcha holatlarni hisobga olgan maxsus snackbar | 18.5, EC-UX-01, 03, 04, 08, 16, 17, ATK-F1, F2, D20, D21 |
| 1b | `add-attack-details` — xuruj tafsilotlari ekrani (vaqtlar, ogʻriq nuqtalari, joylashuv, belgilar, triggerlar, taʼsir, izoh), oʻz teglari, Undo bilan oʻchirish, birinchi 10 ball kartasi | ATK-F4, F5, F6, F7, EC-ATK-03, 04, 09…14, EC-TIM-04, 05, SAF-F2 |
| 1c | `add-past-attacks` — oʻtgan xurujni qoʻshish, ustma-ust tushganda birlashtirish yoki vaqtni oʻzgartirish | ATK-F3, EC-ATK-03, 04, 05, 15…18, D26–D28 |
| 2 | `add-attack-history` | HIS-F*, EC-HIS-* |
| 3 | `add-medication-tracking` | MED-F*, EC-MED-* |
| 4 | `add-day-markers` | DAY-F* |
| 5 | `add-basic-stats` | STA-F* |
| 6 | `add-reminders` | REM-F*, EC-REM-* |
| 7 | `add-data-export-import` | BAK-F*, EC-BAK-* |
| 8 | `add-onboarding` | ONB-F* |
| 9 | `add-subscription` | 7-boʻlim, SUB-F*, EC-BIL-* |
| 10 | `add-doctor-report` | REP-F*, EC-REP-* |
| 11 | `add-trigger-insights` | INS-F*, EC-INS-* |
| 12 | `add-settings-and-safety` | SET-F*, SAF-F1, EC-STO-* |
| 13 | `prepare-release` — listing, privacy policy, Data safety hujjati, shortcut | 11- va 14-boʻlimlar |
| 14 | `upgrade-toolchain` — AGP 9, Kotlin 2.4, SDK 37, Navigation 3 (sample'ga moslash). `cag` AGP 9'ni qoʻllab-quvvatlaganda yoki Play API 37'ni talab qilishidan oldin, qaysi biri birinchi boʻlsa | ADR-03, `docs/ARCHITECTURE.md` §2, §13 |

---

## 14. Launch va oʻsish rejasi

### 14.1 Vaqt jadvali

| Hafta | Ish |
|---|---|
| 1 | BRD ✅, UX va wireframe'lar, change 0 (setup). Play Console'da ilova yozuvi yaratiladi (doʻstning akkauntida), Owner'ga shu ilova uchun Admin ruxsati beriladi |
| 2 | Change 1–2. Hafta oxirida yaqinlar bilan **internal testing** boshlanadi (majburiy emas, sifat uchun) |
| 3 | Change 3–5 |
| 4 | Change 6–8 |
| 5 | Change 9–10 |
| 6 | Change 11–13, polish, listing |
| 7 | Production, staged rollout |

Ilova 2023-yil 13-noyabrdan oldin ochilgan akkauntda chiqadi, shuning uchun 12 tester va 14 kunlik closed testing talabi bizga taalluqli emas (D12). Shunga qaramay, sifat uchun internal testing track'ida yaqinlar bilan sinov oʻtkazamiz.

### 14.2 Store listing (ASO)

- **Title** (≤30 belgi): `Migrely: Migraine Tracker`
- **Short description** (≤80 belgi): `Log migraines in 3 taps. Private headache diary & doctor-ready reports.`
- **Kalit soʻzlar:** migraine tracker, headache diary, migraine log, headache tracker, migraine app, menstrual migraine, medication overuse.
- **Grafika:** 8 ta skrinshot (har birida bitta foyda: tez yozish, maxfiylik, PDF, kalendar, dorilar, tahlil, qorongʻi tema, bepul tarix), feature graphic 1024×500, ikonka 512×512.

### 14.3 Launch

- Barcha davlatlar yoqiladi (Play billing mavjud boʻlmaganlarida bepul rejim ishlaydi).
- Staged rollout 7 kun davomida: 20% → 50% → 100%. Har bosqichda vitals tekshiriladi.

### 14.4 Launch'dan keyin

- **Sharhlar:** har bir sharhga 48 soat ichida javob beramiz.
- **Baho soʻrash:** In-App Review 3-xuruj yozilgandan keyin, crash boʻlmagan sessiyada soʻraladi.
- **Tajribalar:** har oy 1 ta store listing experiment, 3-oydan narx tajribalari.
- **Kontent:** r/migraine kabi hamjamiyatlarda (ularning qoidalari doirasida) va TikTok/Shorts'da: triggerlar, hayz migreni, dorini koʻp ichish xavfi.
- **Pullik reklama:** RPI > CPI isbotlanmaguncha yoʻq.
- **Halollik:** oʻrnatish yoki sharh hech qachon sotib olinmaydi.

---

## 15. Xavflar

| Xavf | Ehtimol | Taʼsir | Kamaytirish |
|---|---|---|---|
| Nisha zich (42 ta yangi ilova) | Yuqori | Yuqori | Tezlik, maxfiylik, surunkali migrenni qoʻllab-quvvatlash, bepul tarix; ASO; sifat |
| Android'da past konversiya (0,9%) | Yuqori | Oʻrta | Paywall onboarding oxirida, qiymat lahzalarida va trial bilan |
| Tibbiy javobgarlik | Past | Yuqori | Disclaimer, tashxis yoʻq, xavfli belgilar, ICHD-3 ga tayangan matnlar |
| Policy rad etishi (Data safety, health deklaratsiyasi) | Oʻrta | Oʻrta | Deklaratsiyalar kodga aniq mos; ADR-09 va ADR-10 |
| Billing xatolari (Play'dagi bekor qilishlarning 31% i) | Yuqori | Oʻrta | Grace period va account hold, EC-BIL-03 va EC-BIL-04 |
| Yakka dasturchining vaqti yetmasligi | Oʻrta | Yuqori | Qatʼiy MVP, kichik OpenSpec change'lar |
| Nom yoki trademark konflikti (yaqin nom: MigreLief — migren uchun qoʻshimchalar brendi) | Past–oʻrta | Oʻrta | Listing'dan oldin USPTO va EUIPO tekshiruvi. Kerak boʻlsa faqat ilova nomi oʻzgaradi, package name qoladi |
| Ilova doʻstning developer akkauntida | Oʻrta | Yuqori | Yozma kelishuv: ilova va kod Owner'niki, daromad taqsimoti, talab qilinganda app transfer. Akkauntdagi boshqa ilovalarning policy holati kuzatiladi |

---

## 16. Tekshirilishi kerak boʻlgan ochiq savollar

1. Doʻstning akkauntida merchant (payments profile) faol ekanini va subscription sotish mumkinligini tasdiqlash. Akkaunt turi va ochilgan sanasini ham tasdiqlash (closed testing talabi yoʻqligi uchun).
2. Google Play'ning joriy target API talabi.
3. RevenueCat'ning joriy narxi va DPA; TelemetryDeck Kotlin SDK holati.
4. Health apps declaration'ning joriy shakli.
5. "Migrely" nomi: USPTO va EUIPO.
6. Developer akkaunt roʻyxatdan oʻtish toʻlovi ($25 deb aytiladi, rasmiy tasdiqlanmagan).
7. Daromad oshsa: AQSH shtatlari sogʻliq maʼlumoti qonunlari boʻyicha yurist maslahati.
8. ~~`CleanArchitectureGenerator` navigatsiyasi va modul nomlari~~ — hal qilindi: Nav2, `features/<f>/…`, `datasource/{source,implementation}` (D14, `docs/ARCHITECTURE.md`).

---

## 17. Qarorlar jurnali

| # | Qaror | Kim | Sana |
|---|---|---|---|
| D1 | Freemium: kiritish va tarix abadiy bepul | Owner | 2026-10-23 |
| D2 | Maʼlumot faqat telefonda, akkaunt yoʻq | Owner | 2026-10-23 |
| D3 | MVP + Premium scope | Owner | 2026-10-23 |
| D4 | Narxlar: $29.99 yillik (7 kunlik trial), $4.99 oylik, $59.99 lifetime | Owner | 2026-10-23 |
| D5 | Owner qolgan qarorlarni PO'ga topshirdi | Owner | 2026-10-23 |
| D6 | "Kiritish bepul, tahlil premium" tamoyili; xavfsizlik funksiyalari doim bepul | PO | 2026-10-23 |
| D7 | P3 (faxriylar) ikkinchi darajali persona: MVP'da faqat funksional taʼsir maydoni | PO | 2026-10-23 |
| D8 | Nom: Migrely. Package name `com.migrely.app` (dastlab `com.migrely.android` edi; iOS'ni hisobga olib oʻzgartirildi) | PO | 2026-10-23 |
| D9 | Native Kotlin/Compose, Room, WorkManager, RevenueCat, TelemetryDeck, Crashlytics'siz. Navigation D14 ga qarang | PO | 2026-10-23 |
| D10 | ~~Closed testing 2-hafta oxirida boshlanadi~~ (D12 bilan bekor qilindi) | PO | 2026-10-23 |
| D11 | Spec'lar ingliz tilida, BRD va tushuntirishlar oʻzbek tilida | PO | 2026-10-23 |
| D12 | Ilova doʻstning eski Play Console akkauntida chiqadi. Closed testing talabi yoʻq, internal testing ixtiyoriy | Owner | 2026-10-23 |
| D13 | Faqat toza Clean Architecture: sample repo, kitob, skill va CleanArchitectureGenerator | Owner | 2026-10-23 |
| D14 | Hozircha `cag` toolchain'i (AGP 8.13, Kotlin 2.2.20, SDK 36) va Nav2. Sample'ga (AGP 9, Nav3) `upgrade-toolchain` change'ida oʻtiladi | PO | 2026-10-23 |
| D15 | ~~"Tinch xona" va mushuk~~ → "Sizning osmoningiz" va "qorongʻi xona". Manbadan tamoyil olindi, tashqi koʻrinish emas (18-boʻlim) | PO | 2026-10-23 |
| D16 | Hissiy dizayn qoidalari G1–G6: maskot yoʻq, streak yoʻq, xurujlar jazolanmaydi va mukofotlanmaydi | PO | 2026-10-23 |
| D18 | Owner barcha mahsulot va dizayn qarorlarini PO'ga topshirdi. Ulashilgan manbalardan tamoyil olinadi, nusxa koʻchirilmaydi | Owner | 2026-10-23 |
| D19 | Illustratsiya va ikonkalar protsedurali vektor sifatida (kod bilan) chiziladi, tashqi illustrator kerak emas | PO | 2026-10-23 |
| D17 | Launch'dan oldin 20–40 nafar migrenli bemor bilan 3–4 haftalik beta | PO | 2026-10-23 |
| D20 | Shrift: sarlavhalar uchun Fraunces (Soft), matn uchun Figtree. Rad etilganlar: Roboto va Inter (oddiy, "standart Android" koʻrinishi), Atkinson Hyperlegible Next (har bir vaqt va sanada nolni chiziqli qilib koʻrsatadi, buni oʻchirib boʻlmaydi), Lexend (tabular raqamlari yoʻq), Plus Jakarta Sans va Manrope (zich, matn chiziqlari koʻpayadi), Newsreader (ingichka shtrixlar qorongʻi fonda yoyilib koʻrinadi), Nunito Sans (x-height past). Kirill yozuvi kerak boʻlsa, shu tilda tizim shrifti ishlatiladi | PO | 2026-10-23 |
| D21 | Snackbar qoidalari EC-UX-16 boʻyicha. Material standart snackbar oʻrniga `:designsystem`dagi maxsus komponent | PO | 2026-10-23 |
| D22 | 1b ikkiga boʻlindi: 1b tafsilotlar, 1c oʻtgan xuruj va birlashtirish. 1c gacha vaqtni boshqa xuruj bilan ustma-ust tushadigan qilib oʻzgartirish rad etiladi va oʻsha xurujning vaqti aytiladi | PO | 2026-10-23 |
| D23 | Oʻchirilgan xuruj Undo oynasi davomida yashirin turadi (soft delete) va 2 daqiqadan keyin butunlay oʻchiriladi. 2 daqiqa Android'dagi eng uzun "Harakat qilish vaqti". Foydalanuvchi uchun u 5 soniyalik Undo'dan keyin yoʻq | PO | 2026-10-23 |
| D24 | "Qorongʻi xona"da xira "Details" havolasi bor. Koʻp kun davom etadigan xurujda (P2) belgilarni xuruj tugashini kutmasdan yozish mumkin | PO | 2026-10-23 |
| D25 | Tayyor joylashuv, belgi va triggerlar kodda barqaror kalitlar bilan turadi, foydalanuvchining oʻz teglari bazada saqlanadi | PO | 2026-10-23 |
| D26 | "Add a past migraine" havolasi faqat bosh ekranning oddiy rejimida, "Migraine started" ostida turadi ("qorongʻi xona"da yoʻq, G4). Oʻtgan xuruj ekranida faqat boshlanish va tugash bor: standart qiymat 2 soat oldin va hozir, daqiqagacha yaxlitlangan. Boshlanish oʻzgarsa, davomiylik saqlanadi (kalendardagi kabi). Qoʻshilgandan keyin tafsilotlar ekrani ochiladi. Oʻtgan xuruj doim tugagan boʻladi: hali davom etayotgan xuruj "Migraine started" va "Started earlier?" orqali yoziladi | PO | 2026-10-23 |
| D27 | Birlashtirish qoidalari: bitta xuruj qoladi (tahrirlanayotgani, yangi xurujda esa eng birinchi boshlangani). Vaqt hamma xurujlar va ogʻriq nuqtalarini qamraydi va hech qachon qisqarmaydi. Ogʻriq nuqtalari va teglar qoʻshiladi, taʼsirning eng yuqorisi olinadi, izohlar vaqt tartibida ulanadi (EC-ATK-16). Birortasi davom etayotgan boʻlsa, natija ham davom etayotgan boʻladi. Qolganlari bitta tranzaksiyada oʻchiriladi | PO | 2026-10-23 |
| D28 | Birlashtirishda Undo yoʻq. Foydalanuvchi qaysi xurujlar bilan ustma-ust tushganini koʻrib, "Merge them"ni oʻzi tanlaydi va kiritilgan maʼlumot yoʻqolmaydi (faqat pastroq taʼsir darajasi va 1 000 belgidan ortiq izoh). Keyin "Migraines merged." xabari chiqadi. Beta'da xato birlashtirishlar koʻp boʻlsa, Undo qayta koʻriladi | PO | 2026-10-23 |

---

## 18. Tajriba va dizayn

**Manba:** Starter Story'dagi ikki video: "This Simple App Makes $18K/Month" (V_4kiR3R4QU) va "The Design Process Behind This $18K/Month App" (iL3D57bncE8). Ulardan **tamoyillar** olindi, tashqi koʻrinish (mushuk, xona) emas. Tafsilot D15 va D18 da.

### 18.1 Cat on Chair'dan olingan dalillar

| Nima | Ular qilgan ish |
|---|---|
| Natija | Jami 127K yuklab olish. 8–9 oyda ~$80K, oxirgi 30 kunda $18K+ (muallif soʻzlari, Stripe skrinshotlari bilan) |
| Monetizatsiya | Ilova bepul, premium funksiyalar pullik. Lifetime $29.99, yillik $19.99. **Sotuvlarning koʻpi lifetime'dan** |
| Asosiy gʻoya | Foydalanuvchining real hayotdagi tajribasini ("mushuk yonimda") ilovaga aylantirish. Oddiy Pomodoro taymerni hissiy mahsulotga aylantirgan |
| Dizayn | Insoniy, qoʻlda chizilgan, ataylab "mukammal emas". Rasm koʻp, matn kam |
| Jarayon | Launch'dan oldin oʻnlab odam bilan 1 oylik beta. Launch'dan keyin feedback doskasi orqali foydalanuvchilar soʻragan funksiyalar qoʻshilgan |
| Oʻsish | Pullik reklama yoʻq. Odamlar chiroyli dizaynni oʻzlari ulashgan, "app collection" postlari tarqalgan |

**Ehtiyot boʻlish kerak boʻlgan jihatlar:** muvaffaqiyat qisman omadga bogʻliq, bozori va platformasi boshqa, kanal bootcamp sotadi.

### 18.2 Migrely'ga koʻchirilgan tamoyillar

| Cat on Chair tamoyili | Migrely'dagi koʻrinishi |
|---|---|
| Real tajribani raqamlashtirish | Migrenli odam xuruj paytida qorongʻi va jim xonaga kiradi, oʻqiy olmaydi → **ilovaning oʻzi "qorongʻi xona"ga aylanadi** |
| Hissiy yadro | Migrenli odamlarning tilida migren — "boʻron". Ularning eng katta ehtiyoji — tushunilish va umid → **"Sizning osmoningiz"** va taraqqiyot xabari |
| Insoniy, oʻziga xos dizayn | Qoʻlda chizilgandek, notekis chiziqli osmonlar. Ular maʼlumotdan chiziladi, shuning uchun har kimniki oʻziga xos |
| Rasm koʻp, matn kam | Oyni bir qarashda oʻqish mumkin. Xuruj paytida esa bir qator matn |
| Real foydalanuvchilar bilan beta | 18.6 |
| Lifetime koʻp sotiladi | D4 dagi $59.99 lifetime tasdiqlandi |

### 18.3 Konsepsiya

**A. "Sizning osmoningiz" (bosh ekran)**

- Yuqorida bugungi osmon kartasi bor. Bugun hali belgilanmagan boʻlsa: "Calm so far today".
- Ostida oylik kalendar. Har bir kun kichik osmon: ochiq, tumanli, bulutli yoki boʻron (EC-UX-09).
- Taraqqiyot xabari: "3 fewer migraine days than September" (EC-UX-14).
- Pastda bitta katta tugma: "Migraine started" va kichik havola "Add a past migraine" (D26).
- Shifokor uchun bu aslida ogʻirlik boʻyicha issiqlik xaritasi, faqat insoniy koʻrinishda. PDF hisobotda ham xuddi shu osmonlar ishlatiladi.

**B. "Qorongʻi xona" (xuruj rejimi)**

- Butun ekran yanada qorongʻi boʻladi (fon `#08090C`). Bugungi kartada sekin yomgʻir yogʻadi (Reduce motion yoqilgan boʻlsa, statik).
- Ekranda bitta qator matn qoladi: "Rest now. We'll keep time." va xuruj boshlangan vaqt.
- Faqat katta tugmalar: ogʻriq kuchi (Mild / Moderate / Severe), "+ Medication taken", "Migraine ended".
- Tugmalar oddiy kundagidan xiraroq, barchasi barmoq bilan oson bosiladigan pastki qismda.

**Style frame'lar:** `docs/design/style-frames/home-your-sky.svg` va `attack-dark-room.svg`.

### 18.4 Hissiy dizayn qoidalari

| Qoida | Mazmuni |
|---|---|
| G1 | Xurujlar hech qachon jazolanmaydi va mukofotlanmaydi. Boʻron — maʼlumot, ayb emas |
| G2 | Streak (ketma-ket kunlar) yoʻq. Zanjir uzilishi bemorni aybdor his qildiradi |
| G3 | Taraqqiyot faqat taqqoslash orqali koʻrsatiladi va ayblovsiz boʻladi (EC-UX-14) |
| G4 | Xuruj rejimida mukofot, paywall, baho soʻrash va taraqqiyot xabarlari yoʻq (EC-UX-13) |
| G5 | Maskot yoki multfilm qahramoni yoʻq. Migren jiddiy kasallik, ilova shifokor oldida ham ishonchli koʻrinishi kerak |
| G6 | Ekrandagi har bir holat faqat maʼlumotdan hisoblanadi va hech narsa taxmin qilinmaydi (EC-UX-09, EC-UX-11) |

### 18.5 Vizual til

- **Palitra.** Tungi osmon tonlari D7 palitrasiga mos. Eng yorugʻ element (oy) `#C8CCD2` dan oshmaydi. Chaqmoq xira amber rangda (`#B8975A`).
- **Holat ranglari.**
  - ochiq: koʻk tun va yulduzlar;
  - tumanli: toʻlqin chiziqlar;
  - bulutli: bulut;
  - boʻron: qorongʻi bulut va chaqmoq.

  Har bir holat rangdan tashqari shakl bilan ham ajraladi (EC-UX-15).
- **"Qoʻlda chizilgan" his.** Bulut va toʻlqin konturlari ataylab biroz notekis, ustida past kontrastli tekstura bor.
- **Shrift (D20).** Sarlavhalar va "qorongʻi xona"dagi bitta tinchlantiruvchi gap uchun **Fraunces** (yumshoq serif: Soft 100, Wonky oʻchirilgan). Qolgan barcha matn, tugma va raqamlar uchun **Figtree**. Kundalik ruhidagi iliq ovoz va tugmalarda aniq oʻqiladigan matn shu juftlikdan chiqadi. Ikkalasi ham OFL litsenziyali variable font, ilova ichiga joylanadi (taxminan 0,4 MB).
- **Texnik ijro.** Osmon kartochkalari Compose Canvas'da kod bilan chiziladi. Rastr rasm va illustrator kerak emas (D19), APK hajmi kichik qoladi.

### 18.6 Bemorlar bilan beta

- **Ishtirokchilar:** 20–40 nafar migrenli tester. Ular migren hamjamiyatlari (qoidalari doirasida), tanishlar va shifokorlar orqali topiladi.
- **Muddat:** Internal testing track'ida 3–4 hafta.
- **Fikr-mulohaza:** Ilova ichida "Fikr bildirish" tugmasi (bepul feedback doskasiga havola). Har hafta 3–5 bemor bilan 15 daqiqalik suhbat.
- **Mezon:** Testerlarning ≥70% i 2 hafta ichida kamida 3 ta yozuv qiladi. Testerlar bergan oʻrtacha baho ≥4.5.

### 18.7 Oʻsish (pullik reklamasiz)

- **Store skrinshotlari:** osmon kalendari asosiy vizual. U oʻziga xos va bir qarashda tushuniladi.
- **Hamkorlik:** chronic illness va migren mavzusidagi creator'lar hamda "app collection" postlari (#migrainewarrior, #spoonie).
- **Ulashish (v1.1):** foydalanuvchi oʻzi xohlasa, oylik osmon rasmini ulashadi. Bu sogʻliq maʼlumoti, shuning uchun faqat foydalanuvchining oʻz harakati bilan va share sheet orqali.

### 18.8 Rad etilgan variantlar

| Variant | Nega rad etildi |
|---|---|
| Mushuk yoki boshqa hamroh qahramon | Migrenga aloqasi yoʻq, kasallikni yengillashtirib koʻrsatadi, shifokor oldida ishonchni pasaytiradi, hammaga yoqmaydi |
| Oʻsuvchi oʻsimlik | Boshqa ilovalardan olingan metafora, migrenga xos emas. Surunkali bemorlarda deyarli oʻsmaydi |
| Faqat "qorongʻi xona" (osmonsiz) | Dark Logger (2026-yilning eng tez oʻsgan yangi ilovasi) shu yoʻlda. Faqat shu bilan ajralib turib boʻlmaydi |
