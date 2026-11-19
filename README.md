# Pixie Clicker (Android)

## Hướng dẫn đưa file ZIP lên GitHub để tự động xuất ra file APK

Dự án đã có đầy đủ:
- **`gradlew`**, **`gradlew.bat`**, **`gradle/wrapper/gradle-wrapper.jar`** và **`gradle/wrapper/gradle-wrapper.properties`** (bộ Gradle Wrapper chuẩn của Android).
- **`.github/workflows/build-apk.yml`** (workflow GitHub Actions tự build APK).
- **`github-apk-workflow/build-apk.yml`** (bản sao **không ẩn** của workflow trên, đảm bảo luôn có mặt và nhìn thấy được trong file ZIP ngay cả khi trình xuất ZIP hoặc hệ điều hành ẩn thư mục có dấu chấm `.github`).
- **`debug.keystore.base64`** và **`app/google-services.json`** (để GitHub Actions ký file APK và giữ đúng cấu hình Firebase).

---

### Cách 1: Tải file ZIP và upload trực tiếp trên web GitHub (Không cần cài Git)

> **Lưu ý quan trọng:** Trình xuất ZIP hoặc chức năng kéo-thả **"Upload files"** trên web GitHub thường **bỏ qua các thư mục bắt đầu bằng dấu chấm** như `.github`. Vì vậy hãy làm 2 bước nhỏ sau:

1. Giải nén file ZIP và upload toàn bộ thư mục/file lên repository GitHub của bạn (`app`, `gradle`, `github-apk-workflow`, `gradlew`, `gradlew.bat`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `debug.keystore.base64`, `.env.example`...).
2. Kiểm tra trên GitHub xem đã có file **`.github/workflows/build-apk.yml`** chưa:
   - Nếu **chưa thấy**: trên trang repository GitHub bấm **Add file** -> **Create new file**.
   - Ở ô đặt tên file, gõ chính xác: `.github/workflows/build-apk.yml` (GitHub sẽ tự tạo thư mục `.github/workflows/`).
   - Mở file **`github-apk-workflow/build-apk.yml`** trong máy của bạn, copy toàn bộ nội dung dán vào rồi bấm **Commit changes**.

---

### Cách 2: Đưa lên bằng Git trên máy tính hoặc GitHub Desktop

1. Giải nén file ZIP.
2. Nếu chưa thấy thư mục `.github`:
   - Trên **Windows**: mở thư mục `github-apk-workflow` và nhấp đúp **`activate-workflow.bat`**.
   - Trên **macOS / Linux / Git Bash**: chạy `bash github-apk-workflow/activate-workflow.sh`.
3. Chạy `git init && git add -A && git commit -m "Pixie Clicker Android"` rồi push lên GitHub.

---

### Cách tải file `.apk` về điện thoại

1. Vào repository của bạn trên GitHub -> mở tab **Actions**.
2. Chọn workflow **Build Android APK** (nếu chưa tự chạy thì bấm **Run workflow**).
3. Đợi vài phút cho tới khi hiện dấu tích xanh.
4. Kéo xuống cuối trang run đó, mục **Artifacts** -> bấm tải **`Pixie-Clicker-APK`**.
5. Giải nén file tải về để lấy **`Pixie-Clicker.apk`**, chép vào điện thoại Android và mở để cài đặt.
