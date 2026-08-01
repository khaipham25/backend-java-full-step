package vn.tayjava.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.tayjava.model.UserEntity;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query(value = "select u from UserEntity u where u.status='ACTIVE' and (lower(u.firstName)  like lower(:keyword) " +
            "or lower(u.lastName)  like :keyword " +
            "or lower(u.username) like :keyword " +
            "or lower(u.phone) like :keyword " +
            "or lower(u.email) like :keyword)")
    Page<UserEntity> searchByKeyword(String keyword, Pageable pageable);
    //Spring Data JPA nhận biết Pageable là một tham số đặc biệt dùng để điều khiển truy vấn, chứ không phải dữ liệu đem so sánh trong WHERE.
    // Vì thế không cần viết :pageable trong câu @Query.

    //Spring nhìn vào kiểu Pageable trong tham số method nên tự biết phải phân trang, không cần nhét nó vào nội dung @Query.

    UserEntity findByUsername(String username);
    
    UserEntity findByEmail(String email);
}
