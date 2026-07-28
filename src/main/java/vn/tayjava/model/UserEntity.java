package vn.tayjava.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import vn.tayjava.common.Gender;
import vn.tayjava.common.UserStatus;
import vn.tayjava.common.UserType;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "tbl_user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@GeneratedValue dùng để nói với JPA/Hibernate rằng:
    //Giá trị của khóa chính id sẽ được tự động sinh ra khi thêm dữ liệu mới.
    @Column(name = "id")
    private Long id;

    // unique: true - bắt buộc phải điền duy nhat
    // nullable: false - không cho phép null
    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender")
    private Gender gender;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_birth")
    private Date birthday;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "username", unique = true, nullable = false, length = 255)
    private String username;

    @Column(name = "password", length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", length = 255)
    private UserType type;

    @Enumerated(EnumType.STRING)
    //Annotation này nói với JPA/Hibernate rằng: Hãy lưu giá trị enum bằng tên chữ, không lưu bằng số thứ tự.
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    //Annotation này là của Hibernate, dùng để nói rằng: Cột trong database là một kiểu ENUM có tên thật của database, đặc biệt thường dùng với PostgreSQL.
    @Column(name = "status", length = 255)
    private UserStatus status;

    @Column(name = "created_at", length = 255)
    //@Temporal dùng để nói cho JPA/Hibernate biết một biến kiểu java.util.Date sẽ được lưu vào CSDL theo kiểu thời gian nào.
    // Timestamp là lưu cả ngày và giờ
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    //Tự ghi thời gian khi bản ghi được tạo lần đầu.
    private Date createdAt;

    @Column(name = "updated_at", length = 255)
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    //Tự ghi lại thời gian mỗi khi bản ghi được cập nhật.
    private Date updatedAt;
}
