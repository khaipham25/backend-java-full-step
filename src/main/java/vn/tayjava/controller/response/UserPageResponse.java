package vn.tayjava.controller.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPageResponse extends PageResponseAbstract implements Serializable {
    private List<UserResponse> users;
}
