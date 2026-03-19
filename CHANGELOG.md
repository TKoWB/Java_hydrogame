## [0.1] - 08-03-2026

### 🚀 Added
- **Khởi tạo dự án:** `hydrogame`
- **Database:** Tích hợp file `hydrogame.sql`

- **Folder `Database` (Entities):**
    - Thêm `User.java`
    - Thêm `Game.java`
    - Thêm `Genre.java`

- **Folder `feature_admin`:**
    - `AddGame_Service.java`
    - `EditGame_Service.java`

- **Folder `feature_all`:**
    - `Search.java` *(Lưu ý: Chưa thực hiện, đang đợi dữ liệu và form)*

- **Folder `user_service`:**
    - `LoginService.java`
    - `RegisterService.java`

- **Folder `security_service`:**
    - `DecryptionService.java`
    - `EncryptionService.java`

- **Folder `main`:**
    - `App.java` (Sử dụng để Test tính năng, có thể tự do chỉnh sửa)
    - `SystemInfo.java` ( **Không chỉnh sửa**)

- **Folder `hibernate_util`:**
    - `HibernateUtil.java` ( **Không chỉnh sửa**)

---

### 💡 Lưu ý quan trọng (Notes)
- **Chức năng hiện tại:** Hầu hết các lớp trên chỉ đóng vai trò là tầng trung gian (Service/DAO) để giao tiếp với Database (lưu và lấy dữ liệu). 
- **Chưa phải là chương trình hoàn thiện:** Các Service này không tự chạy độc lập. Bạn cần phải viết logic để truyền dữ liệu đầu vào cho chúng. 
- **Trạng thái:** Tên file có thể gây hiểu lầm là đã xong, nhưng thực tế đây mới chỉ là phần khung giao tiếp Database, chưa hoàn thiện toàn bộ tính năng đâu`=v`