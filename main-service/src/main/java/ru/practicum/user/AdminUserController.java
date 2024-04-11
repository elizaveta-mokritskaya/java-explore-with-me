package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.UserDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/users")
@Slf4j
public class AdminUserController {
    private final UserService service;

    @GetMapping
    public List<UserDto> getUsersByIds(@RequestParam(defaultValue = "") List<Long> ids,
                                      @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                      @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        return service.getUsersByIds(ids, from / size, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addNewUser(@RequestBody @Valid UserDto dto) {
        return service.addNewUser(dto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable Long userId) {
        service.deleteUserById(userId);
    }
}
