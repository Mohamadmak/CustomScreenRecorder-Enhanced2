# دليل رفع الملفات إلى GitHub

## 📁 **قائمة الملفات (18 ملف فقط):**

### 1. **ملفات الجذر** (6 ملفات):
```
✅ build.gradle
✅ settings.gradle
✅ gradle.properties
✅ README.md
✅ proguard-rules.pro
✅ app-build.gradle
```

### 2. **ملف GitHub Actions** (1 ملف):
```
✅ build-apk.yml
```

### 3. **ملفات التطبيق الأساسية** (2 ملف):
```
✅ MainActivity.kt
✅ ScreenRecordService.kt
```

### 4. **ملف AndroidManifest** (1 ملف):
```
✅ AndroidManifest.xml
```

### 5. **ملفات الواجهة** (5 ملفات):
```
✅ activity_main.xml
✅ strings.xml
✅ colors.xml
✅ themes.xml
✅ spinner_background.xml
✅ record_button_background.xml
```

### 6. **ملفات XML إضافية** (2 ملف):
```
✅ backup_rules.xml
✅ data_extraction_rules.xml
```

---

## 🚀 **خطوات الرفع:**

### الخطوة 1: رفع ملفات الجذر
في جذر المستودع، ارفع:
- `build.gradle`
- `settings.gradle`
- `gradle.properties`
- `README.md`

### الخطوة 2: إنشاء مجلد `app`
1. اضغط **"Create new file"**
2. اكتب: `app/build.gradle`
3. انسخ محتوى ملف `app-build.gradle`
4. احفظ

### الخطوة 3: إنشاء مجلد `app/src/main`
1. اضغط **"Create new file"**
2. اكتب: `app/src/main/AndroidManifest.xml`
3. انسخ محتوى ملف `AndroidManifest.xml`
4. احفظ

### الخطوة 4: إنشاء مجلد الكود
1. اضغط **"Create new file"**
2. اكتب: `app/src/main/java/com/customrecorder/app/MainActivity.kt`
3. انسخ محتوى ملف `MainActivity.kt`
4. احفظ
5. كرر للملف `ScreenRecordService.kt`

### الخطوة 5: إنشاء مجلدات الموارد
1. **للتخطيطات**: `app/src/main/res/layout/activity_main.xml`
2. **للنصوص**: `app/src/main/res/values/strings.xml`
3. **للألوان**: `app/src/main/res/values/colors.xml`
4. **للثيمات**: `app/src/main/res/values/themes.xml`
5. **للرسوميات**: `app/src/main/res/drawable/spinner_background.xml`
6. **للرسوميات**: `app/src/main/res/drawable/record_button_background.xml`

### الخطوة 6: إنشاء مجلد XML
1. `app/src/main/res/xml/backup_rules.xml`
2. `app/src/main/res/xml/data_extraction_rules.xml`

### الخطوة 7: إنشاء مجلد GitHub Actions
1. اضغط **"Create new file"**
2. اكتب: `.github/workflows/build-apk.yml`
3. انسخ محتوى ملف `build-apk.yml`
4. احفظ

### الخطوة 8: إضافة proguard-rules
1. اضغط **"Create new file"**
2. اكتب: `app/proguard-rules.pro`
3. انسخ محتوى ملف `proguard-rules.pro`
4. احفظ

---

## ✅ **بعد الانتهاء:**
- ستحصل على 18 ملف فقط بدلاً من مئات الملفات
- GitHub Actions سيبني التطبيق تلقائياً
- ستجد ملف APK في تبويب "Actions" → "Artifacts"

## 🎯 **النتيجة:**
تطبيق كامل الوظائف مع جميع الميزات المطلوبة!

