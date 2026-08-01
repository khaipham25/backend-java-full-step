package vn.tayjava.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.tayjava.common.UserStatus;
import vn.tayjava.controller.request.UserCreationRequest;
import vn.tayjava.controller.request.UserPasswordRequest;
import vn.tayjava.controller.request.UserUpdateRequest;
import vn.tayjava.controller.response.UserPageResponse;
import vn.tayjava.controller.response.UserResponse;
import vn.tayjava.exception.InvalidDataException;
import vn.tayjava.exception.ResourceNotFoundException;
import vn.tayjava.model.AddressEntity;
import vn.tayjava.model.UserEntity;
import vn.tayjava.repository.AddressRepository;
import vn.tayjava.repository.UserRepository;
import vn.tayjava.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    // Lấy danh sách user, có tìm kiếm, sắp xếp và phân trang, rồi trả về UserPageResponse.
    // /users?keyword=an&sortBy=firstName:asc&page=0&pageSize=10
    public UserPageResponse findAll(String keyword, String sortBy, int page, int pageSize) {
        log.info("findAll");

        // sap xep Sorting
        // Nếu frontend không truyền sortBy, chương trình mặc định:
        // Sắp xếp theo cột id tăng dần
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "id");

        //Nếu sortBy có dữ liệu thì chương trình sẽ đọc chuỗi sắp xếp.
        if (StringUtils.hasLength(sortBy)) {
            //Dòng này tạo một regex để tách chuỗi có dạng:
            //tênCột:kiểuSắpXếp
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)"); // tencot:asc|desc

            // Dòng này đưa giá trị sortBy vào regex để kiểm tra và tách dữ liệu
            Matcher matcher = pattern.matcher(sortBy);

            //matcher.find() kiểm tra chuỗi có khớp với mẫu:
            if (matcher.find()) {
                // sortBy = "firstName:asc";
                // columnName = "firstName";
                String columnName = matcher.group(1);
                if (matcher.group(3).equalsIgnoreCase("asc")) {

                    order = new Sort.Order(Sort.Direction.ASC, columnName);
                } else {
                    order = new Sort.Order(Sort.Direction.DESC, columnName);
                }
            }
        }

        // Xử lý trường hợp FE muốn bat đầu voi page = 1
        int pageNo = 0;
        if (page > 0 ){
            pageNo = page - 1;
        }

        // phan trang Paging
        // Tạo đối tượng phân trang
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(order));

        // Khai báo biến chứa kết quả truy vấn
        // Biến này sẽ chứa kết quả lấy từ database.
        Page<UserEntity> userEntities;

        //StringUtils.hasLength(keyword) kiểm tra keyword có dữ liệu hay không.
        if (StringUtils.hasLength(keyword)) {
            keyword = "%" + keyword.toLowerCase() + "%";
            // goi search method
            userEntities = userRepository.searchByKeyword(keyword, pageable);
        } else {
            // Dòng này gọi repository để lấy user từ database.
            userEntities = userRepository.findAll(pageable);
        }

        UserPageResponse userPageResponse = getUserPageResponse(page, pageSize, userEntities);
        return userPageResponse;
    }



    @Override
    public UserResponse findById(Long id) {
        log.info("findById {}", id);
        UserEntity userEntity = getUserEntityById(id);
        return UserResponse.builder()
                .id(userEntity.getId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .gender(userEntity.getGender())
                .birthday(userEntity.getBirthday())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .phone(userEntity.getPhone())
                .build();
    }

    @Override
    public UserResponse findByUsername(String username) {
        return null;
    }

    @Override
    public UserResponse findByEmail(String email) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    //Hoặc tất cả thao tác đều thành công, hoặc nếu có lỗi thì hủy toàn bộ những thao tác đã thực hiện.
    public Long save(UserCreationRequest req) {
        log.info("Saving user {}", req);

        UserEntity userByEmail = userRepository.findByEmail(req.getEmail());
        if (userByEmail != null) {
            throw new InvalidDataException("Email already exists");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName(req.getFirstName());
        userEntity.setLastName(req.getLastName());
        userEntity.setGender(req.getGender());
        userEntity.setBirthday(req.getBirthday());
        userEntity.setEmail(req.getEmail());
        userEntity.setPhone(req.getPhone());
        userEntity.setUsername(req.getUsername());
        userEntity.setType(req.getType());
        userEntity.setStatus(UserStatus.NONE);
        // click email thì status mới chuyển thành Active
        userRepository.save(userEntity);
        if (userEntity.getId() != null) {
            List<AddressEntity> addresses = new ArrayList<>();
            req.getAddresses().forEach((address) -> {
                AddressEntity addressEntity = new AddressEntity();
                addressEntity.setApartmentNumber(address.getApartmentNumber());
                addressEntity.setFloor(address.getFloor());
                addressEntity.setBuilding(address.getBuilding());
                addressEntity.setStreetNumber(address.getStreetNumber());
                addressEntity.setStreet(address.getStreet());
                addressEntity.setCity(address.getCity());
                addressEntity.setCountry(address.getCountry());
                addressEntity.setAddressType(address.getAddressType());
                addressEntity.setUserId(userEntity.getId());
                addresses.add(addressEntity);
            });
            addressRepository.saveAll(addresses);
            log.info("Save all addresses {}", addresses);
        }
        return userEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserUpdateRequest req) {
        // Lấy user theo id cho trước
        log.info("Updating user {}", req);
        UserEntity userEntity = getUserEntityById(req.getId());

        // Set data
        userEntity.setFirstName(req.getFirstName());
        userEntity.setLastName(req.getLastName());
        userEntity.setGender(req.getGender());
        userEntity.setBirthday(req.getBirthday());
        userEntity.setEmail(req.getEmail());
        userEntity.setPhone(req.getPhone());
        userEntity.setUsername(req.getUsername());

        userRepository.save(userEntity);
        log.info("Updated user {}", userEntity);

        // save address
        List<AddressEntity> addresses = new ArrayList<>();

        req.getAddresses().forEach((address) -> {
            AddressEntity addressEntity = addressRepository.findByUserIdAndAddressType(userEntity.getId(), address.getAddressType());
            if (addressEntity == null) {
                addressEntity = new AddressEntity();

            }

            addressEntity.setApartmentNumber(address.getApartmentNumber());
            addressEntity.setFloor(address.getFloor());
            addressEntity.setBuilding(address.getBuilding());
            addressEntity.setStreetNumber(address.getStreetNumber());
            addressEntity.setStreet(address.getStreet());
            addressEntity.setCity(address.getCity());
            addressEntity.setCountry(address.getCountry());
            addressEntity.setAddressType(address.getAddressType());
            addressEntity.setUserId(userEntity.getId());

            addresses.add(addressEntity);

        });

        // co id roi thi cap nhat, chua co thi thêm id mới
        // save addresses
        addressRepository.saveAll(addresses);
        log.info("Updated addresses {}", addresses);

        // cập nhật vào database
    }

    @Override
    public void changePassword(UserPasswordRequest req) {
        log.info("Changing user password {}", req);

        // get user by id
        UserEntity userEntity = getUserEntityById(req.getId());
        if (req.getPassword().equals(req.getConfirmPassword())) {
            userEntity.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        userRepository.save(userEntity);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting user {}", id);

        // Get user by Id
        UserEntity userEntity = getUserEntityById(id);
        userEntity.setStatus(UserStatus.INACTIVE);
        userRepository.save(userEntity);
        log.info("Deleted user {}", userEntity);
    }

    /**
     * Get User By Id
     * @param id
     * @return
     */
    private UserEntity getUserEntityById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Convert Page<UserEntity to UserPageResponse
     * @param page
     * @param pageSize
     * @param userEntities
     * @return
     */
    private static UserPageResponse getUserPageResponse(int page, int pageSize, Page<UserEntity> userEntities) {
        log.info("Convert User Entity Page");
        // tra ve page no, page size, list danh sach
        // Chuyển UserEntity thành UserResponse
        // UserEntity dùng để ánh xạ với bảng trong database.
        // Còn UserResponse là DTO dùng để trả dữ liệu cho frontend.
        // ko nên trả thẳng vì userEntity có thể chứa password, refreshToken ...thông tin nội bo
        List<UserResponse> userList = userEntities.stream().map(
                //Biến entity đại diện cho từng UserEntity đang được xử lý.
                //Dòng này dùng Lombok @Builder để tạo đối tượng.
                entity -> UserResponse.builder()
                        .id(entity.getId())
                        .firstName(entity.getFirstName())
                        .lastName(entity.getLastName())
                        .gender(entity.getGender())
                        .birthday(entity.getBirthday())
                        .username(entity.getUsername())
                        .email(entity.getEmail())
                        .phone(entity.getPhone())
                        .build()
        ).toList();
        //Sau khi map từng UserEntity thành UserResponse, .toList() gom tất cả lại thành:
        //List<UserResponse>

        //Tạo đối tượng kết quả phân trang
        UserPageResponse userPageResponse = new UserPageResponse();
        userPageResponse.setPageNumber(page);
        userPageResponse.setPageSize(pageSize);
        userPageResponse.setTotalPages(userEntities.getTotalPages());
        userPageResponse.setTotalElements(userEntities.getTotalElements());
        userPageResponse.setUsers(userList);
        return userPageResponse;
    }
}
