package za.co.invest.notification_service.entity;

import com.google.gson.Gson;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications"   )
@Setter
@Getter
public class EmailList implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String toEmail;
    private String title;
    private String bodyMessage;
    private String status;
    private LocalDateTime createDate;
    private LocalDateTime processDate;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
