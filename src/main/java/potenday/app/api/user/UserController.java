package potenday.app.api.user;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import potenday.app.api.common.ApiResponse;
import potenday.app.api.validation.NicknameValidationSequence;
import potenday.app.domain.user.UserService;

@RestController
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/user/nickname/available-check")
  public ApiResponse<NicknameAvailableResponse> checkDuplicate(
      @Validated(value = NicknameValidationSequence.class)
      @RequestBody UserNicknameRequest userNicknameRequest
  ) {
    boolean isAvailable = userService.checkAvailableNickname(userNicknameRequest.getNickname());
    return ApiResponse.success(new NicknameAvailableResponse(isAvailable));
  }
}
