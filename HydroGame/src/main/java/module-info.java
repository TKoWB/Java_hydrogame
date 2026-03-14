module com.hydrogame {
    // Các module JavaFX bắt buộc
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    

    // Các module liên quan đến Hibernate và JPA
    requires java.persistence; // Đây là nơi chứa javax.persistence
    requires org.hibernate.orm.core;
    requires java.sql;
    requires java.naming;

    // các module liên quan tới argon2
    requires spring.security.crypto;
    requires org.bouncycastle.provider;
    requires commons.logging;
    
    // QUAN TRỌNG: Cho phép Hibernate truy cập vào các Entity bằng Reflection
    opens com.hydrogame.database to org.hibernate.orm.core;
    opens com.hydrogame.main to javafx.fxml;
    
    // Cho phép JavaFX truy cập vào MainApp để khởi chạy
    exports com.hydrogame.main;
}
