package ru.practicum.user;

import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsersByIds(List<Long> ids, int i, int size);

    UserDto addNewUser(UserDto dto);

    void deleteUserById(Long userId);
}
