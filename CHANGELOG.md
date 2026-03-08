## [0.1] - 08-03-2026
### Added
-Tạo dựu án hydrogame

-Thêm database:
    hydrogame.sql

-folder Database:
    -Thêm User.java
    -Thêm Game.java
    -Thêm Genre.java

-folder feature_admin:
    -AddGame_Service.java
    -EditGame_Service.java

-folder feature_all:
    -Search.java(chưa có làm, chưa đủ dữu liệu, chưa có form để làm)

-folder user_service:
    -LoginService.java
    -RegisterService.java

-folder security_service:
    -DecryptionService.java
    -EncryptionService.java

- folder main:
    -App.java (dang dùng cho thử nghiệm mấy tính năng trên, có thể tự do edit)
    -SystemInfo.java (đừng động vào nó là được)

- folder hibernate_util:
    -HibernateUtil.java(đừng động vào nó là được)



### Lưu ý
- gần như mọi thứ ở trên đều có chức năng là lưu thông tin và lấy thông tin từ database, nó không bao gòm việc hoàn thành tính năng đó, bạn gọi nó ra rồi đưa thông tin cần thiết vào chúng và chúng sẽ trả cho bạn thông tin, chứ không phải là một chương trình, nó chỉ giao tiếp với database, còn thông tin đưa vào chúng nó thì bạn là người phải viết code và đưa thông tin nhập vào
- đừng nhìn chúng nó tên thế tưởng là hoàn thành rồi, chưa đâu =v