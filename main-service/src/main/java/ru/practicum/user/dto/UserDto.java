package ru.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    @Email(message = "Email должен быть корректным адресом электронной почты")
    @NotBlank(message = "Email не должен быть пустым")
    @Size(min = 6, max = 254, message = "Длина эл. почты должна быть от 6 до 254")
    private String email;
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 2, max = 250, message = "Длина имени должна быть от 2 до 250")
    private String name;
}
