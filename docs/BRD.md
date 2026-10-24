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
| EC-MED-07 | Bir xil nomli dori ikkinchi marta qoʻshildi (katta-kichik harf farqisiz) | Qoʻshilmaydi, mavjudi koʻrsatiladi. Arxivdagi dori nomi qayta kiritilsa, oʻsha dori arxivdan qaytariladi va tarixi saqlanadi | ✅ |
| EC-MED-08 | Xuruj paytida "+ Medication taken" bosildi, lekin hali birorta dori qoʻshilmagan | Shu ekranning oʻzida nomi va toifasi bilan dori qoʻshish taklif qilinadi, keyin u darhol yoziladi | ✅ |
| EC-MED-09 | Dori xurujdan tashqarida (bosh ekrandan) yozildi, lekin vaqti biror xuruj ichiga tushadi | Dori oʻsha xurujga bogʻlanadi va uning tafsilotlarida koʻrinadi | ✅ |
| EC-MED-10 | Dorilari bor xuruj oʻchirildi yoki boshqa xuruj bilan birlashtirildi | Oʻchirilganda dorilar ham u bilan yashiriladi, Undo ularni ham qaytaradi va Undo oynasidan keyin xuruj bilan birga butunlay oʻchiriladi. Birlashtirishda dorilar qolgan xurujga oʻtadi (D27) | ✅ |
| EC-MED-11 | Bir nechta dori "Yordam berdimi?" javobini kutib turibdi | Bosh ekranda bitta karta chiqadi, eng oldin ichilgani birinchi. Javob berilsa yoki karta yopilsa, keyingisi chiqadi. Profilaktik dori uchun savol berilmaydi | ✅ |
| EC-MED-12 | Javobni keyin berish yoki oʻzgartirish, 24 soat oʻtib ketgan boʻlsa ham | Xuruj tafsilotlarida dori qatori bosilsa, javob tanlanadi yoki oʻzgartiriladi. Xurujsiz yozilgan dori uchun javob faqat karta orqali, 24 soat ichida beriladi (statistika 5-change'da) | ✅ |

### 9.4 Tarix, statistika va tahlil

| ID | Holat | Qaror | MVP |
|---|---|---|---|
| EC-HIS-01 | Hali maʼlumot yoʻq | Kalendar va statistika ekranida "Birinchi xurujni yozing" tugmasi va qisqa tushuntirish chiqadi | ✅ |
| EC-HIS-02 | Koʻp maʼlumot (10 yilda 3 600+ xuruj) | Roʻyxat sahifalab yuklanadi (paging), soʻrovlar indekslangan, kalendar faqat koʻrinayotgan oyni yuklaydi | ✅ |
| EC-HIS-03 | Birinchi xurujdan oldingi yoki kelajakdagi oyga oʻtish | Ruxsat berilmaydi: oldingi oy tugmasi birinchi xuruj yoki birinchi kun belgisi qaysi oyda boʻlsa, oʻsha oyda, keyingi oy tugmasi joriy oyda oʻchadi. Hech qanday xuruj va belgi boʻlmasa, faqat joriy oy koʻrinadi (D51) | ✅ |
| EC-HIS-04 | Tarixdan ochilgan xuruj tafsilotlar ekranida oʻchirildi | Foydalanuvchi tarix ekraniga qaytadi va u yerda "Migraine deleted" va "Undo" chiqadi (bosh ekrandagidek, EC-ATK-14). Kalendar va roʻyxat darhol yangilanadi | ✅ |
| EC-DAY-01 | Xuruj yozilgan kunni "ogʻriqsiz" deb belgilash | Ruxsat berilmaydi: tugma oʻchiq va "Bu kunda migren yozilgan" deb tushuntiriladi. Belgidan keyin oʻsha kunga xuruj yozilsa, xuruj ustun: kun ogʻriqsiz koʻrinmaydi va sanalmaydi. Xuruj oʻchirilsa, belgi yana kuchga kiradi | ✅ |
| EC-DAY-02 | Bir kunda hayz va ogʻriqsiz kun, yoki hayz va xuruj | Ikkalasi ham mumkin: hayz ogʻriq emas. Hayz va migren bir kunda boʻlishi aynan kuzatiladigan bogʻliqlik (INS-F3) | ✅ |
| EC-DAY-03 | Kelajakdagi kunni belgilash | Ruxsat berilmaydi: kelajakdagi kun tanlanmaydi (EC-HIS-03 bilan mos) | ✅ |
| EC-DAY-04 | Sayohat va vaqt zonasi | Belgi kalendar sanasiga qoʻyiladi va zona bilan bogʻlanmaydi: belgilangan sana har qanday zonada oʻsha sana boʻlib qoladi | ✅ |
| EC-DAY-05 | Belgini notoʻgʻri qoʻydi | Oʻsha tugmani qayta bosish belgini olib tashlaydi, alohida Undo kerak emas. Holat darhol saqlanadi | ✅ |
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
| EC-UX-18 | Tugma ikki marta tez bosildi va rejim birinchi bosishdayoq almashdi, ikkinchi bosish yangi rejimdagi tugmaga tushdi ("Migraine started" ostida "Migraine ended" turadi) | Oddiy va xuruj rejimi oʻrtasida almashgandan keyin yangi rejimdagi boshqaruvlar 1 soniya bosishni qabul qilmaydi (D32). Ilova ochilganda va ekran burilganda bu kutish yoʻq | ✅ |
| EC-UX-19 | Ilova klaviatura yoki Switch Access bilan boshqariladi | Bosiladigan har bir element (roʻyxat qatorlari, kalendar kunlari) fokusga oʻtadi va accessibility xizmatlariga focusable boʻlib koʻrinadi (WCAG 2.1.1). Ovozli tavsif ham saqlanadi | ✅ |
| EC-UX-20 | Telefon landscape'da: xuruj rejimida matn chap ustunda, boshqaruvlar oʻng ustunda turadi | Xabar (masalan, Undo bilan "Migraine started") faqat chap ustun ustida chiqadi. Ogʻriq tanlovi, "+ Medication taken" va "Migraine ended" xabar turgan butun vaqt davomida koʻrinib turadi va bosiladi | ✅ |

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
| 2 | `add-attack-history` — oylik osmon kalendari (bosh ekranda joriy oy, "History" ekranida oyma-oy), oyning xurujlar roʻyxati, kunni tanlash, tanlangan kunga oʻtgan xurujni qoʻshish, tarixda oʻchirishni Undo qilish | HIS-F1…F3, EC-HIS-01…04, EC-ATK-06, 07, EC-TIM-02, 05, EC-UX-09, 15, D29–D31 |
| 2a | `fix-home-qa-findings` — QA topilmalari #12–#15: rejim almashganda ikki marta bosish, xuruj rejimidagi matn kontrasti, klaviatura bilan qatorlarga oʻtish, boshlanish surilganda davomiylik | EC-UX-18, 19, D32, 10-boʻlim (WCAG AA) |
| 2b | `add-quality-automation` — QA avtomatlashtirish: spec scenario'lariga bogʻlangan Maestro E2E testlari, real qurilma piksellarida accessibility audit, qurilma scenario'lari (soat, vaqt zonasi, process death), monkey va ruxsatlar auditi, release Go/No-Go roʻyxati. Mahsulot kodi oʻzgarmaydi | 10-boʻlim (Test, Accessibility, Barqarorlik, Maxfiylik), 14.3, EC-ATK-02, EC-ATK-10, EC-TIM-02, EC-UX-03, 04, 16, D40–D44 |
| 2c | `fix-today-zone-and-landscape-message` — QA topilmalari #19 va #20: bugungi karta va "Logged today" xurujni oʻzi yozilgan zonadagi kunlari boʻyicha oladi, vaqt oraligʻida shahar bir marta yoziladi; landscape xuruj rejimida xabar faqat chap ustun ustida chiqadi | EC-TIM-02, EC-UX-16, EC-UX-20, D36–D38 |
| 3 | `add-medication-tracking` — dorilar roʻyxati (qoʻshish, tahrirlash, arxivlash), dorini xuruj paytida, tafsilotlarda va bosh ekrandan yozish (D33–D35) | MED-F1, F2, F3, EC-MED-01, 07…10, ATK-F5 (dorilar) |
| 3a | `add-medication-effect-and-overuse` — "Yordam berdimi?" savoli bosh ekranda va xuruj tafsilotlarida, overuse ogohlantirishi (D39, D45, D46) | MED-F4, F5, EC-MED-02…06, 11, 12, ATK-F2 |
| 3b | `add-medication-e2e-coverage` — QA: PR #24 retest'i (vaqt zonasi va landscape xabar), zona scenario'lari (gʻarbga uchish, bir sana, fonda va History ochiq turganda zona oʻzgarishi), dorilar uchun E2E flow'lar (roʻyxat, qabul) va qurilma soatini surib tekshiriladigan "Yordam berdimi?" va overuse scenario'lari. Mahsulot kodi oʻzgarmaydi | MED-F1…F5, EC-MED-07…12, EC-TIM-02, D36, D50, D60, D61 |
| 4 | `add-day-markers` — hayz va ogʻriqsiz kun belgilari (History'da tanlangan kun, bosh ekranda "Period" tugmasi), kalendarda va bugungi kartada "ochiq" osmon (D29, D47–D51) | DAY-F1, F2, HIS-F1 va F3 ning belgilar qismi, EC-UX-09, 10, EC-DAY-01…05 |
| 4a | `add-calendar-keyboard-grid` — oylik kalendar klaviatura bilan bitta Tab toʻxtash joyi, kunlar orasida strelkalar (QA #22, D38); landscape xuruj rejimida xabar ostidan matn yarim koʻrinmasligi (QA #26) | EC-UX-19, EC-UX-20, D38, D52 |
| 4b | `add-day-markers-e2e-coverage` — QA: change 4 va 4a retest'i (#22 klaviatura grid'i, #26 landscape xabar), kun belgilari (hayz, ogʻriqsiz kun) va oylik osmon uchun E2E flow'lar, klaviatura yoʻllari haqiqiy key event'lar bilan, accessibility audit'ining bitta Tab toʻxtash joyli grid qoidasi. Mahsulot kodi oʻzgarmaydi | DAY-F*, EC-DAY-01…05, EC-UX-03, EC-UX-04, D38, D47–D52, D62 |
| 5 | `add-basic-stats` — History'dagi oy xulosasi (beshta raqam), surunkali migren indikatori va taraqqiyot xabari, xuruj kunlari qoidasi umumiy modulga (D29, D53–D58) | STA-F*, EC-UX-14, EC-HIS-01 |
| 5a | `add-overuse-indicator` — History oy xulosasida, joriy oy uchun, oxirgi 30 kunning doimiy overuse indikatori (D53, D59) | EC-MED-06, MED-F5 |
| 6 | `add-reminders` — "Reminders" ekrani, kunlik eslatma ("Pain-free day" va "Log a migraine" tugmalari bilan) va "Yordam berdimi?" bildirishnomasi, WorkManager orqali (D65–D68) | REM-F1, REM-F2, EC-REM-* |
| 6b | `add-stats-and-reminders-e2e-coverage` — QA: 5, 5a va 6-change'lar uchun E2E: oy xulosasi, surunkali indikator, taraqqiyot xabari, "Last 30 days" overuse indikatori, eslatmalar ekrani, bildirishnoma ruxsati (rad etish va 30 kunlik qayta soʻrash), kunlik eslatma va "Did it help?" bildirishnomasi (WorkManager ishi majburan ishga tushiriladi), qayta yuklash va zona oʻzgarishi. Change 7 eskirtirgan tekshiruvlar (Settings tugmasi) tuzatiladi. Mahsulot kodi oʻzgarmaydi | STA-F*, EC-UX-14, EC-MED-06, REM-F*, EC-REM-01…06, D53–D59, D65–D69, D63, D64 |
| 7 | `add-data-export-import` — "Settings" ekrani, "Your data": JSON va CSV eksport, ikki rejimli JSON import, Auto Backup qoidalari (D70–D74) | BAK-F*, EC-BAK-01–04 |
| 8 | `add-onboarding` — 4 qadamli onboarding, "Track periods" sozlamasi, onboarding'dagi eslatma taklifi va zaxiradan tiklash havolasi (D76–D80) | ONB-F*, DAY-F1, EC-BAK-05 |
| 9 | `add-subscription` | 7-boʻlim, SUB-F*, EC-BIL-* |
| 10 | `add-doctor-report` — History'dan ochiladigan "Doctor report": davr, xulosa sahifasi, kalendar issiqlik xaritasi, xurujlar va dorilar jadvallari, ixtiyoriy izohlar, FileProvider orqali ulashish (D81–D85) | REP-F*, EC-REP-* |
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
| D29 | Tarix: bosh ekranda bugungi kartaning ostida joriy oyning osmon kalendari turadi (18.3 A). Oy nomini yoki kunni bosish "History" ekranini ochadi: u yerda oyma-oy oʻtiladi, kalendar ostida oyning xurujlari (yangilari tepada) chiqadi, kun tanlansa shu kungi xurujlar. Roʻyxat oyma-oy yuklanadi, bu EC-HIS-02 dagi paging: bir oyda koʻpi bilan bir necha oʻnta xuruj boʻladi. Kun xurujlarning yozilgan zonasi boʻyicha aniqlanadi (EC-TIM-02), yarim tundan oʻtgan xuruj har bir kunni belgilaydi (EC-ATK-06), davom etayotgan xuruj kunlari yomgʻir bilan ajraladi (HIS-F1). Hayz va ogʻriqsiz kun belgilari hamda "ochiq" osmon 4-change'da (`add-day-markers`) keladi, shuning uchun hozircha legend'da "Clear" yoʻq (G6). Taraqqiyot xabari (EC-UX-14) 5-change'da (`add-basic-stats`) qoʻshiladi | PO | 2026-10-24 |
| D30 | Tarixda tanlangan kun uchun "Add a past migraine": bugun tanlansa D26 qiymatlari ishlatiladi. Oʻtgan kun tanlansa, boshlanish shu kunda, odatdagi standart boshlanish (hozirdan 2 soat oldin) soatida boʻladi, tugash esa 2 soat keyin, lekin hech qachon hozirdan keyin emas. Kelajakdagi kunni tanlab boʻlmaydi | PO | 2026-10-24 |
| D31 | Tafsilotlar ekranidan oʻchirilgan xurujning Undo'si foydalanuvchi qaytgan ekranda chiqadi: bosh ekran yoki tarix (EC-HIS-04). Oʻchirilganiga 30 soniya boʻlmay turib bosh ekranga qaytilsa, Undo u yerda ham bir marta taklif qilinadi: bu xavfsiz, chunki faqat shu xuruj qaytariladi va keyinroq xabar takrorlanmaydi | PO | 2026-10-24 |
| D32 | Rejim almashganda ikki marta bosishdan himoya (EC-UX-18): yangi rejimdagi barcha boshqaruvlar 1 soniya, animatsiya boʻlsa u tugaguncha (1,2 soniya), bosilmaydi: eski rejim ham fade tugaguncha bosilishi mumkin. QA ikki bosish orasini 0,15–0,7 soniya deb oʻlchadi, rejimning paydo boʻlish animatsiyasi esa 1,2 soniya. Tugmalarni bir-biridan uzoqlashtirish yetarli emas, chunki 200% shriftda "Severe" ham "Migraine started" oʻrniga tushadi. Ataylab qilingan tez bosish uchun Undo bor. Himoya ilova ochilganda va ekran burilganda yoʻq | PO | 2026-10-24 |
| D33 | 3-change ikkiga boʻlindi: 3 dorilar roʻyxati va dorini yozish, 3a "Yordam berdimi?" va overuse ogohlantirishi. MED-F4 savoli avval ilova ichida beriladi (bildirishnoma ruxsatisiz ishlaydi, EC-MED-05), bildirishnoma esa 6-change'da eslatmalar bilan qoʻshiladi. ATK-F2 dagi "qaysi dori yordam berdi?" savoli har bir dori uchun beriladigan "Yordam berdimi?" bilan almashtiriladi | PO | 2026-10-24 |
| D34 | Dorini yozish uchun bitta "Medication taken" ekrani: xuruj rejimidagi "+ Medication taken" tugmasi, tafsilotlar ekrani va bosh ekrandagi "Log medication" havolasi uni ochadi. Dorini bosish uni koʻrsatilgan vaqtda standart dozasi bilan darhol yozadi, keyin tasdiq, Undo va "Done" chiqadi. Vaqt xuruj paytida va bosh ekranda hozir, tugagan xurujda esa uning boshlanishi boʻladi va oʻzgartirish mumkin. Vaqt kelajakda boʻla olmaydi (EC-ATK-03 tolerantligi). Xuruj vaqti keyin oʻzgarsa, dorilar oʻzgarmaydi | PO | 2026-10-24 |
| D35 | Dorilar roʻyxati: nomi 1–60 belgi, turi (xurujni toʻxtatuvchi yoki profilaktik), toʻxtatuvchi dori uchun toifa (oddiy ogʻriq qoldiruvchi, triptan, kombinatsiyalangan yoki boshqa, "bilmayman"). "Bilmayman" EC-MED-04 boʻyicha hisoblanadi. Standart doza ixtiyoriy erkin matn, koʻpi bilan 20 belgi. Faol dorilar koʻpi bilan 50 ta. Roʻyxat oxirgi ishlatilganidan boshlab, keyin nomi boʻyicha tartiblanadi; dori yozish ekranida shu tartibda avval xurujni toʻxtatuvchi, keyin profilaktik dorilar turadi (D34). Dori oʻchirilmaydi, arxivlanadi (EC-MED-01) | PO | 2026-10-24 |
| D36 | Bugungi karta va "Logged today" roʻyxati xurujni oʻzining kunlari boʻyicha oladi: kunlar xuruj yozilgan zonada aniqlanadi (EC-TIM-02), "bugun" esa qurilmaning hozirgi sanasi. Kalendar va History ham shu qoidaga amal qiladi, shuning uchun bosh ekran bitta xuruj haqida ikki xil javob bermaydi (QA #19). Statistika (5-change) va shifokor hisoboti (10-change) ham shu qoidani ishlatadi. Boshlanish va tugash bir zonada koʻrsatiladigan vaqt oraligʻida shahar faqat bir marta, oxirida yoziladi: "11:15 PM – 11:45 PM (Berlin)". Qurilma zonasi oʻzgarsa (ilova ochiq boʻlsa ham, fonda boʻlsa ham), har bir ekran vaqtlarni darhol qayta chizadi | PO | 2026-10-24 |
| D37 | Landscape'da xuruj rejimi matnni chapga, boshqaruvlarni oʻngga qoʻyadi. Bu holatda xabarlar faqat chap ustun ustida chiqadi (EC-UX-20), shuning uchun boshqaruvlar hech qachon yopilmaydi. Chap ustundagi yoʻl-yoʻriq matni xabar turgan paytda yopilishi mumkin, xuddi portrait'da xabar kartaning tepasini yopgani kabi: bu paytda tasdiqni xabarning oʻzi beradi. Portrait va oddiy rejim oʻzgarmaydi (QA #20) | PO | 2026-10-24 |
| D38 | Klaviatura bilan bosh ekranda (QA #22): oylik kalendar bitta Tab toʻxtash joyi boʻladi. Fokus bugungi yoki tanlangan kunga tushadi, kunlar orasida strelkalar bilan yuriladi, Enter kunni ochadi, keyingi Tab esa "Migraine started"ga oʻtadi. Bu sanalar uchun odatiy grid andozasi (Material date picker). Ekran oʻquvchining tartibi oʻzgarmaydi. Tanlangan kun boʻlmasa, fokus bugunga, bugun bu oyda boʻlmasa, oyning birinchi kuniga tushadi; Shift+Tab ham kalendarni bitta qadamda oʻtadi. Strelkalar bir kun yoki bir hafta suradi (RTL tillarda chap va oʻng almashadi) va tanlab boʻlmaydigan kunga oʻtmaydi, fokus oyda qoladi. Bu alohida 4a-change'da (`add-calendar-keyboard-grid`) qilinadi, chunki 4-change kun belgilari bilan kattalashdi; History ekranidagi kalendar ham shunday boʻladi | PO | 2026-10-24 |
| D39 | "Yordam berdimi?" (MED-F4) ilova ichida: bosh ekranda, oddiy rejimda bugungi karta ostida bitta karta chiqadi. U faqat xurujni toʻxtatuvchi dori uchun, ichilgandan 2 soat oʻtib 24 soatgacha beriladi (EC-MED-05). Savol: "How much did Ibuprofen help?" Javoblar: "Not at all", "A little", "A lot", "Completely" (yoʻq, biroz, yaxshi, toʻliq). Yopish tugmasi shu dori uchun savolni butunlay yopadi. Javob yoki yopishdan keyin savol qayta berilmaydi (EC-MED-11). Javobni xuruj tafsilotlarida keyin ham berish yoki oʻzgartirish mumkin (EC-MED-12, ATK-F2). Bildirishnoma 6-change'da qoʻshiladi | PO | 2026-10-24 |
| D40 | QA jarayoni: QA alohida sessiyada, oʻz worktree'si (`migraine-app-qa`), branch'i va emulatori (`emulator-5580`) bilan ishlaydi va mahsulot kodini oʻzgartirmaydi. Har bir xato GitHub issue boʻladi: `qa` yorligʻi, S1–S4 jiddiylik (S1 va S2 release'ni toʻxtatadi), qadamlar, kutilgan va haqiqiy natija, build, qurilma va oʻlchovlar bilan. Issue xato kamida ikki marta takrorlanib, oʻlchangandan keyingina ochiladi: oʻlchov rad etgan gumon issue boʻlmaydi. Xatoni dev sessiya tuzatadi, QA esa tuzatish PR'ini oʻz emulatorida qayta tekshirib, natijani PR'ga yozadi | QA | 2026-10-24 |
| D41 | E2E testlar Maestro bilan yoziladi, ilovaga test kodi qoʻshilmaydi. Boshqaruv koʻrinadigan matn yoki TalkBack label'i orqali topiladi. Label orqali topilmasa, bu accessibility xatosi hisoblanadi va `testTag` qoʻshilmaydi. Har bir flow toza oʻrnatishdan boshlanadi va qaysi spec scenario'larini tekshirishini `# Covers:` qatorida yozadi. `qa/e2e_coverage.py` qoplashni hisoblaydi va eskirgan havolada yiqiladi. Ochiq xatoni koʻrsatuvchi flow `known-bug` tegi bilan regression'da qoladi, teg tuzatish bilan birga olib tashlanadi | QA | 2026-10-24 |
| D42 | Accessibility real qurilma piksellarida (screencap va accessibility tree) tekshiriladi: WCAG 2.1 AA kontrast, 48 dp, label va klaviatura bilan yetib borish, 100% va 200% shriftda hamda landscape'da. Robolectric ichidagi ATF tekshiruvining ishonchliligi noaniq, shuning uchun u hozircha ishlatilmaydi. Vizual regressiya (Roborazzi, pseudo-locale `en-XA`/`ar-XB`) build oʻzgarishi va golden rasmlar talab qiladi, shuning uchun u 3-change'dan keyin alohida QA change'ida keladi | QA | 2026-10-24 |
| D43 | Soat, vaqt zonasi va tizim ilovani yopishiga bogʻliq xulq qurilmaning oʻzini adb bilan oʻzgartirib tekshiriladi (`cmd alarm set-time`, `set-timezone`, `run-as … kill -9`). Ilovaga test kodi qoʻshilmaydi. Maestro'ning `killApp` buyrugʻi force-stop qiladi va saqlangan holatni tozalaydi, shuning uchun process death uchun ishlatilmaydi. Skript oʻzgartirgan har bir sozlama u tugaganda (xato boʻlsa ham) qaytariladi | QA | 2026-10-24 |
| D44 | Release Go/No-Go roʻyxati: build gate; API 26 va API 36 da smoke; API 36 da toʻliq regression; accessibility audit 0 xato; qurilma scenario'lari oʻtgan; seed bilan 5 000 hodisalik monkey 0 crash va 0 ANR; ruxsatlar auditi (`INTERNET` hech qachon); ochiq S1 yoki S2 qa issue yoʻq. 13-change'dan boshlab R8 release build smoke va Play pre-launch report ham qoʻshiladi. Roʻyxat va buyruqlar `docs/QA.md` da | QA | 2026-10-24 |
| D45 | Overuse (MED-F5, ICHD-3 8.2) sanash qoidasi. Oxirgi 30 kun (bugun va undan oldingi 29 kun, "bugun" qurilma sanasi) ichida dori ichilgan kunlar sanaladi. Har bir qabulning kuni u ichilgan zonada aniqlanadi (EC-TIM-02). Bir kunda bir necha qabul 1 kun hisoblanadi (EC-MED-02). Har bir toifa alohida sanaladi (EC-MED-03): oddiy ogʻriq qoldiruvchi uchun chegara 15 kun, triptan 10, kombinatsiyalangan yoki boshqa (va "Not sure", EC-MED-04) 10. Profilaktik dorilar va oʻchirilayotgan xurujning dorilari sanalmaydi. Ikki yoki undan koʻp toifa ishlatilgan boʻlsa, istalgan xurujni toʻxtatuvchi dori ichilgan kunlar jami ham sanaladi, chegarasi 10 kun (ICHD-3 8.2.6). Bu jami faqat har bir toifadan yuqori darajaga yetgandagina koʻrsatiladi. Yumshoq karta chegaradan 2 kun oldin, "Shifokoringiz bilan gaplashing" kartasi chegarada chiqadi. Karta "Got it" bosilguncha turadi. Shundan keyin shu toifa uchun shu yoki undan past daraja 30 kun chiqmaydi (EC-MED-06), lekin yuqoriroq darajaga oʻtilsa, karta chiqadi. "Got it" faqat oʻz toifasiga taalluqli: qolgan ogohlantirishlardan eng yuqori darajadagisi koʻrsatiladi. Matn maʼlumot beradi, tashxis qoʻymaydi. Statistikadagi doimiy belgi 5a-change'da qoʻshiladi (D59) | PO | 2026-10-24 |
| D46 | Bosh ekranda bir vaqtda koʻpi bilan bitta dori kartasi turadi, tartib: shifokor kartasi, yumshoq karta, "Yordam berdimi?". Xuruj rejimida dori kartalari chiqmaydi (G4, EC-UX-13). Kartalar dorilar feature'iga tegishli. `:app` ularni bosh ekranning kalendar slot'iga oʻxshash slot'iga qoʻyadi (D29 dizayni), shuning uchun home dorilar haqida hech narsa bilmaydi | PO | 2026-10-24 |
| D47 | Kun belgilari: hayz va ogʻriqsiz kun kalendar sanasiga (zonasiz) qoʻyiladi va `day_mark` jadvalida saqlanadi (DB v5). Ikkala belgi ham bugun va oʻtgan kunlar uchun, kelajak uchun emas. Ogʻriqsiz kun xuruj teggan kunga qoʻyilmaydi. Keyin xuruj yozilsa, xuruj ustun keladi va belgi eʼtiborga olinmaydi (EC-DAY-01). Hayz istalgan kunga, xurujli kunga ham qoʻyiladi (EC-DAY-02). Hayz kuzatish hozircha doim yoqiq. Uni oʻchirish onboarding (8-change) va sozlamalarda (12-change) qoʻshiladi, oʻchirilganda hayz UI'si yashiriladi (EC-INS-03) | PO | 2026-10-24 |
| D48 | Belgilar qayerda qoʻyiladi. History'da tanlangan kun ostida ikkita kalit bor: "Period" va "Pain-free day". Bosh ekranda, oddiy rejimda, bugungi karta ostida bitta "Period" tugmasi turadi: bir bosish bugungi hayzni yoqadi yoki oʻchiradi (DAY-F1 "tez tugma"). "Ogʻriqsiz kun" bosh ekranda tugma sifatida berilmaydi, chunki bu kun oxirida beriladigan baho. U kalendardan va 6-change'dagi kunlik eslatma tugmasidan qoʻyiladi (DAY-F2, REM-F1) | PO | 2026-10-24 |
| D49 | "Ochiq" osmon (EC-UX-09). Kalendarda ogʻriqsiz kun yulduzli tiniq osmon boʻladi (shakl: yulduz, EC-UX-15). Bugungi karta ogʻriqsiz deb belgilansa, oʻroq oy oʻrniga toʻlin oy va "Pain-free today" chiqadi. Hayz kuni hujayraning pastki burchagida kichik tomchi shakli bilan, rangdan tashqari shakl bilan ham belgilanadi. Legend'da "Pain-free" va "Period" qoʻshiladi. TalkBack kunni "pain-free" yoki "period" deb oʻqiydi | PO | 2026-10-24 |
| D50 | Zona tufayli qurilmaning hali kelmagan sanasiga tushgan xuruj (masalan, ertalab Toshkentda yozilib, gʻarbga uchilgandan keyin) kalendarda oʻz sanasida, yozilgan zonasi boʻyicha koʻrsatiladi (EC-TIM-02, D36). Bu kun oʻsha sana kelguncha tanlanmaydi va belgilanmaydi (EC-DAY-03). QA #27 | PO | 2026-10-24 |
| D51 | History'dagi eng birinchi oy birinchi xuruj va birinchi kun belgisi (hayz yoki ogʻriqsiz kun) oylaridan oldingisi boʻladi (EC-HIS-03 oʻzgartirildi). Aks holda xurujdan oldin belgilangan oy, masalan migren yozilmasdan belgilangan hayz, oy almashgach History'da koʻrinmay qoladi (review C1) | PO | 2026-10-24 |
| D52 | Landscape xuruj rejimida (QA #26) xabar chap ustun ustida chiqqanda, shu ustundagi yoʻl-yoʻriq xabar ostiga silliq suriladi va xabar ketgach qaytadi. "Reduce motion"da sakrab oʻtadi. Shunday qilib hech qanday matn yopilmaydi va yarim koʻrinmaydi. Oʻng ustundagi boshqaruvlar joyidan qimirlamaydi. Chap ustundagi tugmalar ("2 h earlier", "Details") matn bilan birga suriladi, shuning uchun ustun harakatlanayotganda va undan keyin 0,5 soniya ular bosilmaydi: tez bosilgan ikkinchi tap boshqa tugmaga tushmaydi. Bu D37 dagi "yoʻl-yoʻriq matni xabar ostida qolishi mumkin" degan qoidaning oʻrnini oladi | PO | 2026-10-24 |
| D53 | Statistika (STA-F1) History'da, kalendar ostida, koʻrsatilayotgan oy uchun "oy xulosasi" kartasi boʻladi. Joriy oy uchun sarlavha "October so far", oʻtgan oy uchun "September". Kartada beshta raqam bor. (1) "Migraines": oyda boshlangan xurujlar, boshlangan zonasi boʻyicha (EC-ATK-06, EC-TIM-02). (2) "Migraine days": xuruj tegib oʻtgan kunlar, kalendardagi qoida bilan (D36). (3) "Pain-free days": ogʻriqsiz deb belgilangan va xuruj tegmagan kunlar (EC-DAY-01). (4) "Average peak": D56. (5) "Days with attack medication": hujum uchun dori ichilgan kunlar, kun qabul zonasida aniqlanadi (D45 qoidasi). Joriy oyda raqamlar shu kungacha sanaladi, davom etayotgan xuruj ham kiradi. Hech qachon xuruj yozilmagan boʻlsa, karta oʻrnida History'ning birinchi xuruj kartasi turadi (EC-HIS-01). Xuruj rejimida karta yoʻq (EC-UX-13). EC-MED-06 dagi doimiy overuse indikatori bu kartaga 5a-change'da qoʻshiladi | PO | 2026-10-24 |
| D54 | Surunkali migren indikatori (STA-F2): koʻrsatilayotgan oyda 15 yoki undan koʻp "Migraine days" boʻlsa, xulosa kartasida neytral maʼlumot kartasi chiqadi: "15 or more migraine days in October. That's a lot. Talk to your doctor about it." Rang ogohlantiruvchi emas, ayblov yoʻq (G1). Joriy oy tugamagan boʻlsa ham 15 ga yetishi bilan chiqadi | PO | 2026-10-24 |
| D55 | Taraqqiyot xabari (EC-UX-14) bir xil davrlarni solishtiradi. Toʻliq boʻlmagan oy toʻliq oy bilan solishtirilsa, oy boshida doim "kamaydi" chiqadi va yolgʻon umid beradi (G6). Shuning uchun oʻtgan oyning "Migraine days" soni butun oldingi oy bilan solishtiriladi. Joriy oy esa bugungi sanagacha oldingi oyning shu sanalari bilan solishtiriladi, oldingi oy qisqaroq boʻlsa uning oxirgi kunigacha. Matn: kamaygan boʻlsa iliq rangda "3 fewer migraine days than September" yoki joriy oyda "… than this time last month"; koʻpaygan boʻlsa neytral "2 more migraine days than …"; teng boʻlsa neytral "As many migraine days as …". Xabar faqat ikkala davrda ham kamida 10 ta yozuvli kun boʻlsa chiqadi. Yozuvli kun deganda xuruj tegib oʻtgan, belgi qoʻyilgan yoki hujum uchun dori ichilgan kun tushuniladi. BRD'dagi 10 kunlik chegara ikkala davrga qoʻllanadi, chunki yozilmagan oy "migrensiz oy" emas. Xabar History'dagi xulosa kartasida va bosh ekranda oy kartasining sarlavhasi ostida (faqat joriy oy, D58) koʻrinadi, xuruj rejimida koʻrinmaydi (EC-UX-13) | PO | 2026-10-24 |
| D56 | "Average peak" (STA-F1): oyda boshlangan va eng yuqori ogʻrigʻi yozilgan xurujlar boʻyicha Mild = 1, Moderate = 2, Severe = 3 qiymatlarining oʻrtachasi olinadi va eng yaqin darajaga yaxlitlanadi (yarmi yuqoriga, 1,5 = Moderate). Natija soʻz bilan koʻrsatiladi, chunki 3 bosqichli shkalada "2,3" maʼno bermaydi. Kuchi yozilmagan xurujlar kirmaydi (EC-ATK-08). Bunday xuruj boʻlmasa "—" va "No pain level logged" yoziladi | PO | 2026-10-24 |
| D57 | Xurujning kunlari qoidasi (EC-ATK-06, EC-TIM-02, D36) bitta umumiy domain moduliga, `:features:attackdays:domain` ga oʻtadi. Undan bosh ekran, History va statistika foydalanadi. Bu fix-today-zone-and-landscape-message design D2 dagi vaʼda | PO | 2026-10-24 |
| D58 | Emulator tekshiruvidan keyin ikki aniqlik kiritildi. (1) Xulosadagi birinchi qator "Migraines started" deb ataladi: u oyda boshlangan xurujlarni sanaydi (EC-ATK-06), History roʻyxatining sarlavhasi esa oyga tegib oʻtgan barcha xurujlarni ("13 migraines in October"). Masalan, 30-sentabrdan 1-oktabrgacha boʻlgan xuruj roʻyxatda bor, lekin "boshlangan"da yoʻq, nom shuni tushuntiradi. (2) Bosh ekranda taraqqiyot xabari oy kartasining sarlavhasi ostida, kalendar ustida turadi. Kalendar ostida u legend va pastki tugmalar ortida qolib, aylantirmasdan koʻrinmas edi (D55 oʻzgartirildi) | PO | 2026-10-24 |
| D59 | Doimiy overuse indikatori (EC-MED-06). U History'dagi oy xulosasida, faqat joriy oyda, "Days with attack medication" qatoridan keyin "Last 30 days" boʻlimi sifatida turadi. Oʻtgan oylarda koʻrsatilmaydi, chunki u oy emas, oxirgi 30 kunni oʻlchaydi (D45). Har bir toifa uchun 30 kun ichida dori ichilgan kunlar oʻz chegarasi bilan koʻrsatiladi, masalan "Simple painkillers: 6 of 15 days". Ikki yoki undan koʻp toifa ishlatilgan boʻlsa, "All attack medication: 12 of 10 days" qatori ham chiqadi. Chegaraga yetgan yoki oshgan qator amber rangda (`tertiary`, overuse uchun ajratilgan) boʻladi va ostida bitta xotirjam qator chiqadi: "At this many days, medication can make headaches more frequent. Talk to your doctor about it." Chegaradan past qatorlar neytral. Oxirgi 30 kunda hujum uchun dori ichilmagan boʻlsa, boʻlim koʻrinmaydi. Indikatorni toʻliq dorilar feature'i boshqaradi: use case, ViewModel va UI. `:app` uni History'dagi slot'ga qoʻyadi (D46 andozasi), shuning uchun History dorilar haqida hech narsa bilmaydi. Sanash bosh ekrandagi ogohlantirish bilan bitta qoidada boʻladi (D45). Indikator "Got it" bilan yopilmaydi: u doimiy | PO | 2026-10-24 |
| D60 | Vaqtga bogʻliq dori xulqi ("Yordam berdimi?" 2–24 soat oynasi, 30 kunlik overuse, home'dan yozilgan qabulning xurujga bogʻlanishi) qurilma soatini oldinga va orqaga surib, UI orqali tekshiriladi. Bu black-box usul, ilova ichki tuzilishiga bogʻlanmaydi. Debug build bazasiga `run-as … sqlite3` bilan yozish faqat UI yoʻli amalda imkonsiz boʻlganda ishlatiladi: ilova toʻxtatilgan holda va mos sxema versiyasida | QA | 2026-10-24 |
| D61 | Har bir yangi capability'ning foydalanuvchiga koʻrinadigan scenario'lari uning QA'si yopilishidan oldin flow yoki qurilma scenario'si bilan qoplanadi. Qoplanmagan har bir scenario oʻsha QA change'ining `tasks.md` faylida sababi va keyingi qadami bilan yoziladi (unit test yetarli yoki keyingi QA change'ga qoldirildi) | QA | 2026-10-24 |
| D62 | Klaviatura xulqi (Tab, Shift+Tab, strelkalar, Enter) QA emulator'iga yuborilgan haqiqiy key event'lar bilan end-to-end tekshiriladi, faqat qaysi node focusable ekanini oʻqish bilan emas. Bitta Tab toʻxtash joyi boʻlgan guruhning (masalan, oylik grid) elementlari accessibility service'lar uchun focusable boʻlib qoladi va audit ularni oddiy element kabi tekshiradi; Tab tartibi va strelkalar faqat key event'lar bilan tekshiriladi | QA | 2026-10-24 |
| D63 | QA tekshiruv siyosati: har bir QA change'da yangi va oʻzgargan flow'lar 3 martadan, ta'sir qilingan qurilma scenario'lari 2 martadan va smoke yurgiziladi. Toʻliq regression, barcha qurilma scenario'lari, audit va monkey har bir QA siklining oxirida va har bir release nomzodidan oldin yurgiziladi. Edge case'lar BRD EC-* qatorlaridan toʻliq roʻyxatga olinadi; har biri flow, qurilma scenario'si yoki sababi yozilgan unit test bilan yopiladi | QA | 2026-10-25 |
| D64 | Bildirishnomalar black-box tekshiriladi: soat eslatma vaqtidan oldinroqqa qoʻyiladi, app ishga tushib rejani qayta tuzadi, WorkManager ishi `cmd jobscheduler run -f` bilan majburan ishga tushiriladi, bildirishnoma ochilgan shade'dan uiautomator orqali oʻqiladi va uning tugmalari bosiladi. Ruxsat `pm revoke` va `set-permission-flags` bilan boshqariladi; qayta yuklash `adb reboot` bilan tekshiriladi | QA | 2026-10-25 |
| D65 | 6-change'ning doirasi va kirish nuqtasi. Bu change kunlik eslatmani (REM-F1) va "Yordam berdimi?" bildirishnomasini (REM-F2) qoʻshadi. Trial eslatmasi (REM-F3) obuna bilan birga 9-change'da keladi. Yangi `reminders` feature'ida "Reminders" ekrani boʻladi. Unga bosh ekran sarlavhasining oxiridagi qoʻngʻiroqcha tugmasi ("Reminders") orqali kiriladi. Bu tugma faqat oddiy rejimda turadi, xuruj rejimida qoʻshimcha boshqaruv yoʻq (G4, EC-UX-13). Onboarding (8-change) kunlik eslatmani taklif qiladi, sozlamalar (12-change) shu ekranga havola beradi | PO | 2026-10-24 |
| D66 | Kunlik eslatma (REM-F1). U foydalanuvchi oʻzi yoqmaguncha oʻchiq turadi, bildirishnoma ruxsati ham faqat yoqilayotganda soʻraladi (EC-REM-01). Default vaqt 20:00, uni oʻzgartirish mumkin. WorkManager keyingi vaqtga bitta ish rejalashtiradi (±15 daqiqa, EC-REM-03). Har ishdan keyin, telefon qayta yoqilganda (EC-REM-04), vaqt zonasi yoki soat oʻzgarganda (EC-REM-05) qayta rejalashtiriladi. Bugun ogʻriqsiz deb belgilangan yoki xuruj yozilgan boʻlsa, eslatma chiqmaydi, chunki soʻraydigan narsa yoʻq. Matn: "How was today?", ikki tugma bilan: "Pain-free day" va "Log a migraine". "Pain-free day" bugungi belgini History qoidasi bilan qoʻyadi (EC-DAY-01): bugunga xuruj tegsa, belgi qoʻyilmaydi va bildirishnoma "A migraine is logged today." deb yangilanadi (EC-REM-06). "Log a migraine" ilovani bosh ekranda ochadi | PO | 2026-10-24 |
| D67 | "Yordam berdimi?" bildirishnomasi (REM-F2, MED-F4). Hujum uchun dori ichilgandan 2 soat keyin, savol hali ochiq boʻlsa, "Did <dori> help?" bildirishnomasi chiqadi. Bosilsa, ilova bosh ekranda ochiladi va u yerda karta savolni beradi (D39). Bildirishnomalarga ruxsat berilgan boʻlsa, u default yoqilgan va "Reminders" ekranida kalit bilan oʻchiriladi. Ruxsat boʻlmasa, ilova ichidagi karta qoladi (EC-MED-05). Javob berilsa, savol oʻtkazib yuborilsa yoki qabul bekor qilinsa, bildirishnoma ham bekor boʻladi | PO | 2026-10-24 |
| D68 | Ruxsat rad etilsa (EC-REM-02), kalit oʻchiq qoladi. "Reminders" ekrani buni tushuntiradi va tizim sozlamalariga havola beradi. Ilova qayta soʻrashni 30 kunda koʻpi bilan 1 marta qiladi. Eslatma sozlamalari Room'dagi `reminder_settings` jadvalida saqlanadi (DB v6, additive AutoMigration) | PO | 2026-10-24 |
| D69 | Davom etayotgan xuruj paytida "Yordam berdimi?" bildirishnomasi yuborilmaydi. Migren paytida yorugʻlik va ovoz ogʻriq beradi, "qorongʻi xona" ham ilova ichida kartani koʻrsatmaydi (EC-UX-13, G4). Savol ochiq qoladi, xuruj tugagach bosh ekrandagi karta uni beradi (24 soat ichida). Kunlik eslatma xuruj tegib oʻtgan kunda baribir chiqmaydi (D66). Emulator tekshiruvidan keyin qabul qilindi | PO | 2026-10-24 |
| D70 | "Settings" ekrani. Bosh ekran sarlavhasidagi qoʻngʻiroqcha oʻrniga "Settings" tugmasi turadi (tishli gʻildirak, faqat oddiy rejimda). U ochadigan "Settings" ekranida hozircha "Reminders" va "Your data" bandlari bor. 12-change bu ekranga maxfiylik, xavfsizlik va "About" bandlarini, 9-change obunani qoʻshadi. Har bir funksiya uchun bosh ekranga alohida tugma qoʻshilsa, sarlavha tezda toʻlib ketadi | PO | 2026-10-25 |
| D71 | JSON eksport (BAK-F1). Faylga sogʻliq yozuvlarining hammasi kiradi: xurujlar (ogʻriq nuqtalari, belgilar, funksional taʼsir, izoh bilan), oʻz belgilar, dorilar (arxivdagilar ham), dori qabullari va kun belgilari. Qurilmaga xos holat, yaʼni eslatma sozlamalari va overuse "Got it" belgilari, kirmaydi. Fayl tuzilishi: `format: "migrely-backup"`, `schemaVersion` (maʼlumot bazasi versiyasi), `exportedAt` va yozuvlar. Oʻchirilayotgan xuruj (Undo oynasida) kirmaydi. Fayl nomi `migrely-YYYY-MM-DD.json` | PO | 2026-10-25 |
| D72 | CSV eksport (BAK-F2): har bir xuruj bitta qator boʻladi, Excel'da ochish uchun. Ustunlar: boshlanish va tugash (xuruj zonasidagi mahalliy vaqt), davomiylik (daqiqada), zona, eng yuqori ogʻriq (1–10 va darajasi), ogʻriq joyi, belgilar, triggerlar, ichilgan dorilar, funksional taʼsir va izoh. Ustun nomlari va tayyor belgilar ilovadagi inglizcha nomlar bilan yoziladi; foydalanuvchi oʻzi kiritgan nomlar oʻzgarmaydi. UTF-8 BOM bilan yoziladi, shunda Excel kirill va oʻzbek harflarini toʻgʻri ochadi. Oʻnlik kasrni vergul bilan yozadigan tilda (masalan, oʻzbek va rus) Excel ustunlarni nuqtali vergul bilan ajratadi, shuning uchun ajratgich telefon tiliga qarab "," yoki ";" boʻladi. "=", "+", "-" yoki "@" bilan boshlangan matn formula boʻlib ishga tushmasligi uchun matn sifatida yoziladi. Toʻliq maʼlumot va qayta import faqat JSON orqali boʻladi. Fayl nomi `migrely-attacks-YYYY-MM-DD.csv` | PO | 2026-10-25 |
| D73 | JSON import (BAK-F3, EC-BAK-01–04). Fayl Storage Access Framework orqali tanlanadi, ruxsat soʻralmaydi (BAK-F5). Avval fayl tekshiriladi va uning ichidagi yozuvlar soni koʻrsatiladi. Bazada maʼlumot boʻlsa, ikki tanlov beriladi. "Replace": avval avtomatik zaxira olinadi (ilova ichidagi `files/backups`, oxirgi 3 tasi saqlanadi), soʻng hamma narsa almashtiriladi. "Add": bazada bor ID'li yozuvlar oʻtkazib yuboriladi. Telefondagi xuruj bilan vaqti ustma-ust tushgan xuruj ham oʻtkazib yuboriladi (bir xuruj ikki telefonda yozilgan boʻlishi mumkin), chunki ilovada xurujlar ustma-ust tushmaydi. Bir xil dori bir xil vaqtda ichilgan qabul ham takrorlanmaydi. Nomi mavjud dori bilan bir xil boʻlgan dori qoʻshilmaydi, uning qabullari mavjud doriga bogʻlanadi (EC-MED-07). Import bitta tranzaksiyada bajariladi: yo hammasi, yo hech narsa. Buzilgan yoki boshqa formatdagi fayl hech narsani oʻzgartirmaydi va "This file isn't a Migrely backup." deyiladi. Yangiroq sxema "Update Migrely to open this file." bilan rad etiladi. Eskiroq sxema qabul qilinadi, yetishmayotgan maydonlar default qiymat oladi (migratsiyalar faqat qoʻshuvchi). Eslatma sozlamalari faylga kirmagani va kunlik eslatma kunning yozuvlarini ishga tushganda oʻzi tekshirgani uchun importdan keyin hech narsa qayta rejalashtirilmaydi | PO | 2026-10-25 |
| D74 | Android Auto Backup (BAK-F4) aniq qoidalar bilan yoziladi: maʼlumot bazasi, shared preferences va `files/backups` zaxiraga kiradi, cache kirmaydi (PDF hisobotlar cache'da boʻladi). Cloud backup ham, qurilmadan qurilmaga oʻtkazish ham bir xil qoidalar bilan ishlaydi. Sogʻliq maʼlumoti bulutga faqat end-to-end shifrlangan holda ketadi: telefonda ekran qulfi boʻlmasa (Android 12+), cloud backup oʻchadi va nusxa olishning yoʻli JSON eksport boʻladi. Yangi telefonda Auto Backup tiklanmasa, onboarding'dagi import havolasi 8-change'da qoʻshiladi (EC-BAK-05) | PO | 2026-10-25 |
| D75 | "Replace" oldidan olingan avtomatik zaxirani foydalanuvchi oʻzi tiklay oladigan "Restore the copy saved before your last import" bandi 12-change'da (sozlamalar va xavfsizlik) "Your data" ekraniga qoʻshiladi. Ungacha bu nusxa Android zaxirasi bilan saqlanadi. 7-change'ga kiritilmadi, chunki u alohida tasdiqlash va tekshiruvni talab qiladi | PO | 2026-10-25 |
| D76 | Onboarding qachon chiqadi (ONB-F1, ONB-F2). U faqat birinchi ishga tushirishda, bosh ekrandan oldin chiqadi. Telefonda biror sogʻliq yozuvi boʻlsa (ilova yangilangan, Auto Backup tiklangan yoki fayl import qilingan), onboarding koʻrsatilmaydi va tugagan deb belgilanadi. Ilova xurujni boshlash uchun ochilganda (shortcut) ham koʻrsatilmaydi: xurujni yozish hech qachon toʻsilmaydi. Qaror qabul qilinguncha tizimning splash ekrani turadi, shuning uchun bosh ekran bir lahza koʻrinib qolmaydi. Onboarding tugagach yoki oʻtkazib yuborilgach, qayta chiqmaydi | PO | 2026-10-25 |
| D77 | Onboarding qadamlari (ONB-F1, ONB-F3, EC-BAK-05). 1: "What brings you to Migrely?" ("Keep a record for my doctor", "Find what triggers my migraines", "Take fewer painkillers"); uning ostida bir qatorli disclaimer "Migrely is a headache diary. It doesn't diagnose or treat." va "Restore from a backup file" havolasi ("Your data" ekranini ochadi). 2: "How often do you get migraines?" ("Fewer than 4 a month", "4 to 14 a month", "15 or more a month"). 3: "Track your period too?" ("Yes", "No"). 4: kunlik eslatma taklifi. Har bir qadamda "Skip" bor. Javob tanlansa, keyingi qadamga oʻtiladi. Tizimning "Back" tugmasi oldingi qadamga qaytaradi, 1-qadamda esa ilovadan chiqadi va onboarding keyingi safar yana chiqadi. 4-qadamdan keyin bosh ekran ochiladi; paywall 9-change'da shu joyga qoʻshiladi. "Step 1 of 4" koʻrinadi | PO | 2026-10-25 |
| D78 | Onboarding javoblari va "Track periods" sozlamasi SharedPreferences'da (`migrely_preferences`) saqlanadi. Bular qurilma sozlamalari: Auto Backup ularni saqlaydi (D74), JSON zaxiraga esa kirmaydi (D71). Maʼlumot bazasi oʻzgarmaydi (v6), shuning uchun zaxira fayllari ilovaning avvalgi versiyasi bilan mos qoladi | PO | 2026-10-25 |
| D79 | Javoblar qayerda ishlatiladi. Maqsad va chastota saqlanadi: chastota 11-change'da (Insights) patternlar qachon koʻrinishini aytish uchun va 9-change'da paywall matni uchun ishlatiladi. Hayz kuzatish (DAY-F1): "No" hayz belgisini qoʻshish yoʻllarini yashiradi (bosh ekrandagi "Period today" tugmasi va History'dagi "Period" belgisi). Avval qoʻyilgan belgilar saqlanadi va koʻrinishda qoladi. "Settings" ekraniga "Track periods" kaliti qoʻshiladi. Default holat yoqilgan; "Skip" ham uni yoqilgan qoldiradi | PO | 2026-10-25 |
| D80 | Eslatma qadami Reminders feature'ining "DailyReminderOffer" komponentini ishlatadi. Uni `:app` onboarding'dagi slotga qoʻyadi, xuddi bosh ekran History'ning oy kartasini koʻrsatgani kabi (D46). "Turn on" bildirishnomaga ruxsatni Reminders qoidalari boʻyicha soʻraydi (EC-REM-01, 30 kunlik qoida). Rad etilsa, qadam baribir tugaydi va eslatma oʻchiq qoladi; uni keyin Settings'da yoqish mumkin. "Not now" qadamni oʻtkazib yuboradi. Funnel analitikasi (ADR-09) bu change'ga kirmaydi: u INTERNET ruxsatini va TelemetryDeck ID'ni talab qiladi, bu esa foydalanuvchining qarori | PO | 2026-10-25 |
| D81 | Shifokor hisoboti (REP-F1) History ekranining yuqori panelidagi "Doctor report" tugmasidan ochiladi. "Doctor report" ekranida davr tanlanadi: 1, 3, 6 yoki 12 oy yoki ixtiyoriy oraliq (bugungacha, kelajak sanalarsiz, koʻpi bilan 24 oy); default 3 oy. "Include notes" kaliti bor, default holati oʻchiq, chunki izohlar shaxsiy. Tanlangan davrda xuruj ham, dori ham, ogʻriqsiz kun belgisi ham boʻlmasa (hayz belgisi oʻzi hisobot uchun yetarli emas), "Create report" tugmasi oʻchiq va yonida "Nothing was logged in this period." yoziladi (EC-REP-01). Premium cheklovi obuna bilan birga 9-change'da qoʻshiladi; ungacha hisobot ochiq | PO | 2026-10-25 |
| D82 | Hisobot tarkibi (REP-F2, REP-F3). 1-sahifa (xulosa, 30 soniyada oʻqiladi): davr va yaratilgan sana; oyiga oʻrtacha migren kunlari; xurujlar soni; oyiga oʻrtacha "attack medication" kunlari va overuse holati (bosh ekran kartasining D45 qoidasi: davrning oxirgi kunidan orqaga 30 kunlik oraliqlar, har biri barcha yozuvlardan sanaladi); "Bed rest" xurujlari soni; oʻrtacha eng yuqori ogʻriq; surunkali indikator (History qoidasi: davr tegadigan biror kalendar oyida, butun oy boʻyicha, 15 va undan koʻp migren kuni). Davr 28 kundan qisqa boʻlsa, oyiga oʻrtacha emas, faqat jami kunlar yoziladi. Keyin kalendar issiqlik xaritasi (har oy alohida, kunlar osmon darajasi boʻyicha, oq-qora bosmada ham farqlanadigan toʻqlik bilan va legenda bilan), xurujlar jadvali, dorilar jadvali (har bir dori: qabullar soni, ishlatilgan kunlar, "Did it help?" javoblari) va ixtiyoriy izohlar. Har bir sahifa pastida: "Patient-entered data. Not a diagnosis." va "Page N of M". Hisobot tili ilova tili (inglizcha). Qogʻoz: AQSH va Kanadada Letter, boshqa joyda A4 | PO | 2026-10-25 |
| D83 | Hisob qoidalari ilovaning oʻzidagi bilan bir xil: kunlar xuruj yozilgan zonada aniqlanadi (D57), dori kunlari qabul qilingan zonada (D45), oʻchirilayotgan xuruj hisobga kirmaydi. N oylik davr bugundan orqaga N oy ichidagi kunlar, bugun ham kiradi. Oyiga oʻrtacha qiymatlar davr kunlari soni / 30 ga boʻlib hisoblanadi | PO | 2026-10-25 |
| D84 | Katta davr (EC-REP-02). PDF fonda yaratiladi, ekranda progress koʻrinadi. Hisobot 50 sahifadan oshmaydi: xurujlar jadvali sigʻmasa, eng soʻnggi xurujlar koʻrsatiladi va jadval ostida "Showing the latest N of M migraines. The summary counts all of them." yoziladi; izohlar jadvali ham shunday qisqartiriladi; shunda ham sigʻmasa, oxirgi sahifalar kesiladi | PO | 2026-10-25 |
| D85 | Fayl va ulashish (REP-F4, EC-REP-03). PDF ilovaning cache'idagi `reports/` papkasiga avval `.partial` fayl sifatida yoziladi, tayyor boʻlgach `migrely-report-YYYY-MM-DD.pdf` deb nomlanadi. Yangi hisobot boshlanganda va ilova ishga tushganda qolib ketgan `.partial` fayllar va 1 kundan eski hisobotlar oʻchiriladi. Cache zaxiraga kirmaydi (D74). Hisobot FileProvider orqali Android share sheet bilan ulashiladi, ruxsat soʻralmaydi | PO | 2026-10-25 |
| D86 | Obuna uchun RevenueCat va INTERNET. RevenueCat SDK oʻz serverlari bilan ishlaydi va INTERNET ruxsatini talab qiladi, Migrely esa tarmoqsiz ishlaydi. Shuning uchun 9-change uchun tavsiya: Google Play Billing Library'ni toʻgʻridan-toʻgʻri ishlatish (Play Store bilan IPC orqali ishlaydi, INTERNET kerak emas, uchinchi tomon serveri yoʻq). Bu foydalanuvchining qarori: u tasdiqlaguncha 9-change boshlanmaydi | PO | 2026-10-25 |

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
- Ostida oylik kalendar. Har bir kun kichik osmon: ochiq, tumanli, bulutli yoki boʻron (EC-UX-09). Oy nomi yoki kun bosilsa, "History" ekrani ochiladi (D29).
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
