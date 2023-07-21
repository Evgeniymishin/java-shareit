package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.user.UserMapper.toUser;
import static ru.practicum.shareit.user.UserMapper.toUserDto;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAll() {
        List<UserDto> users = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            users.add(toUserDto(user));
        }
        return users;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Не найден пользователь с id: " + id)
        );
        return toUserDto(user);
    }

    public void validateUser(Long id) {
        if (getById(id) == null) {
            throw new NotFoundException("Такого пользователя нет");
        }
    }

    @Transactional
    @Override
    public UserDto create(UserDto userDto) {
        User user = toUser(userDto);
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserDto update(UserDto userDto, Long id) {
        User currentUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id: " + id));
        Optional.ofNullable(userDto.getEmail()).ifPresent(currentUser::setEmail);
        Optional.ofNullable(userDto.getName()).ifPresent(currentUser::setName);

        return toUserDto(userRepository.save(currentUser));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        validateUser(id);
        getById(id);
        userRepository.deleteById(id);
    }


}
