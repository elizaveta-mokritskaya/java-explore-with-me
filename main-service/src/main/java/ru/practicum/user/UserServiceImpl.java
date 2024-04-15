package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> getUsersByIds(List<Long> ids, int from, int size) {
        if (ids.isEmpty()) {
            return repository.findAll(PageRequest.of(from, size)).stream()
                    .map(UserMapper::toUserDto).collect(Collectors.toList());
        }
        return repository.findAllByIdIn(ids, PageRequest.of(from, size)).stream()
                .map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto addNewUser(UserDto dto) {
        if (repository.existsUserByEmail(dto.getEmail())) {
            throw new AlreadyExistsException("Пользователь с таким email уже существует");
        }
        return UserMapper.toUserDto(repository.save(UserMapper.toUser(dto)));
    }

    @Override
    public void deleteUserById(Long userId) {
        if (!repository.existsUserById(userId)) {
            throw new DataNotFoundException("Пользователь с таким id не найден");
        }
        repository.deleteById(userId);
    }
}
