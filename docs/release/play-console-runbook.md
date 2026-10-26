# Play Console: Migrely 1.0.0 ni chiqarish yoʻriqnomasi

Bu hujjat Migrely'ni Google Play'ga chiqarish uchun Play Console'da qilinadigan barcha qadamlarni tartib bilan
beradi (BRD 13b, D101–D119). Tugma va boʻlim nomlari Play Console'dagidek inglizcha yozilgan. Play Console vaqti-vaqti
bilan oʻzgaradi: nom boshqacha boʻlsa, eng yaqin maʼnodagi boʻlimni tanlang.

## 0. Tayyor fayllar

| Nima | Qayerda |
|---|---|
| Ilova (AAB) | Desktop: `Migrely-1.0.0-<versionCode>.aab` (har yangi build'da versionCode bittaga oshadi) |
| Ikonka 512×512 | Desktop: `Migrely-Play-Assets/app-icon-512.png` (repo: `docs/release/icon/migrely-play-icon-512.png`) |
| Feature graphic 1024×500 | Desktop: `Migrely-Play-Assets/feature-graphic-1024x500.png` |
| 8 ta skrinshot 1080×1920 | Desktop: `Migrely-Play-Assets/screenshots/01…08` (shu tartibda) |
| Listing matni | `docs/release/listing.md` |
| Data safety javoblari | `docs/release/data-safety.md` |
| Content rating javoblari | `docs/release/content-rating.md` |
| Health apps javoblari | `docs/release/health-declaration.md` |
| Mahsulotlar | `docs/release/play-products.md` |
| Sayt va privacy policy | `develop` branch'dagi `website/` va uning `README.md` yoʻriqnomasi (`https://migrely.health/privacy/`) |

## 1. Mahsulotlar (Monetize with Play → Products)

ID'lar koddagi bilan aynan bir xil boʻlishi shart: Play bir marta ishlatilgan ID'ni qayta bermaydi.

1. **Subscriptions → Create subscription**
   - **Product ID:** `migrely_premium`. **Name:** `Migrely Premium`.
   - **Base plan `yearly`:** auto-renewing, 1 year, $29.99 (US). Grace period va account hold yoqilgan.
     **Activate**.
   - **Base plan `monthly`:** auto-renewing, 1 month, $4.99 (US). **Activate**.
   - **`yearly` ichida offer `free-trial-7d`:** eligibility "New customer acquisition → never had this
     subscription", bitta faza "Free trial, 7 days". **Activate**.
2. **In-app products** (yoki "One-time products") **→ Create product**
   - **Product ID:** `migrely_lifetime`. **Name:** `Migrely Premium, lifetime`. Purchase option `buy` (Buy, **Backwards compatible**), age rating
     All ages, $59.99. **Activate**.
3. **Past xarid qobiliyatli bozorlar** (BRD 7.2): Hindiston, Braziliya, Indoneziya va shunga oʻxshash
   bozorlarda narxni AQSH narxining taxminan 50% iga qoʻlda tushiring (har base plan'ning "Prices" boʻlimida).

## 2. Haqiqiy test xarid (telefonda)

- **Shartlar:**
  - telefondagi Play Store **tester Gmail** bilan ochiq;
  - shu Gmail **License testing** roʻyxatida;
  - ilova internal testing'dan oʻrnatilgan.
- **Test vaqtlari (Play tezlashtiradi):**

  | Haqiqiy | Testda |
  |---|---|
  | 7 kunlik trial | taxminan 3 daqiqa |
  | 1 oylik obuna | 5 daqiqa |
  | 1 yillik obuna | 30 daqiqa |

- **Test kartalari** Play'ning toʻlov oynasida chiqadi:
  - "Test card, always approves": muvaffaqiyatli xarid;
  - "Test card, always declines": rad etilgan toʻlov, grace va hold holatlari uchun;
  - "Slow test card": "Payment pending" holati uchun.
- **Tekshiriladigan holatlar:**
  1. Yillik trial: Premium ochiladi, Settings'da "Free trial" va tugash sanasi koʻrinadi.
  2. Play Console → **Order management**'da buyurtma bor va "acknowledged" (3 kun ichida tasdiqlanmasa, Play
     uni qaytaradi).
  3. Trial tugaydi, keyin yillik obuna yangilanadi.
  4. Play'da bekor qilinadi: Premium davr oxirigacha ishlaydi, keyin yopiladi.
  5. Oylikdan yillikka oʻtish.
  6. Lifetime xaridi: obunani bekor qilish yoʻli koʻrsatiladi.
  7. "Slow test card" bilan pending, keyin tasdiqlanish.
  8. Ilovani oʻchirib qayta oʻrnatish: Premium avtomatik qaytadi.
  9. Order management'dan refund: keyingi tekshiruvda Premium yopiladi, yozuvlar qoladi.

## 3. Yangi build'ni yuklash (Test and release → Testing → Internal testing)

1. **Create new release** → AAB'ni yuklang.
2. **Release notes:**
   ```
   <en-US>
   First release of Migrely: a calm, private migraine diary.
   </en-US>
   ```
3. **Next** → **Save and publish**. Kutiladigan yagona ogohlantirish: "native code ... debug symbols". U zararsiz:
   bu `androidx.graphics.path` kutubxonasidan keladi va u symbolsiz yetkaziladi.

## 4. App content (Policy and programs → App content)

| Boʻlim | Javob |
|---|---|
| Privacy policy | `https://migrely.health/privacy/` (sayt joylangandan keyin) |
| App access | "All functionality is available without special access". Login yoʻq; Premium oddiy xarid |
| Ads | **No**, ilovada reklama yoʻq |
| Content rating | `content-rating.md` boʻyicha: kategoriya "Utility, Productivity, Communication, or Other", hammasiga "No", raqamli xaridlar "Yes" |
| Target audience | Faqat **18 and over** |
| News app | No |
| Data safety | `data-safety.md` boʻyicha: "Does your app collect or share user data?" → **No** |
| Government apps | No |
| Financial features | No |
| Health apps | `health-declaration.md` boʻyicha: Disease and condition management, Medication and treatment management, Period tracking |
| Advertising ID | Ilova reklama ID'sini ishlatmaydi → **No** |

## 5. Store listing (Grow users → Store presence → Main store listing)

1. **App name:** `Migrely: Migraine Tracker`.
2. **Short description:** `Private headache diary & migraine log. Spot triggers, watch painkiller days.`
3. **Full description:** `listing.md` dagi matn, oʻzgartirmasdan (har paragraf bitta qator).
4. **Graphics:** ikonka, feature graphic, 8 ta telefon skrinshoti (01 dan 08 gacha, fayl nomi tartibida:
   birinchi uchtasi qidiruvda koʻrinadi).
5. **Store settings:**
   - kategoriya: **Health & Fitness**;
   - email: `support@migrely.health`;
   - website: `https://migrely.health`.

## 6. Davlatlar (Test and release → Production → Countries / regions)

Birinchi bosqichda **EEA'dan tashqari hamma davlatlar** (BRD qarori: trader maʼlumotlari faqat EEA'da ochiq
koʻrsatiladi). EEA: Yevropa Ittifoqining 27 davlati, Islandiya, Lixtenshteyn va Norvegiya. Uydan boshqa qonuniy manzil
paydo boʻlgach, EEA qoʻshiladi.

## 7. Production release

1. QA'dan **Go** olingach (D44 roʻyxati), release branch'dagi oxirgi AAB'ni **Production** ga yuklang.
2. **Staged rollout:** 20% → 2–3 kundan keyin 50% → yana 2–3 kundan keyin 100%. Har bosqichda **Android vitals**'ni
   tekshiring: crash va ANR darajasi Play chegarasidan past boʻlsin.
3. Google review birinchi chiqishda odatda bir necha soatdan bir necha kungacha davom etadi.

## 8. Launch'dan keyin

- Sharhlarga 24 soat ichida javob bering.
- **Store listing experiments:** ikonkani ("Moon calendar" va "Bold moon") va birinchi skrinshotni A/B test qiling.
- **Financial reports:** Migrely daromadi `com.migrely.app` boʻyicha alohida koʻrinadi.
