package com.example.Autovermietung.mapper;

import com.example.Autovermietung.entities.User;
import com.example.Autovermietung.dto.user.CreateUserRequest;
import com.example.Autovermietung.dto.user.UpdateUserRequest;
import com.example.Autovermietung.dto.user.UserResponse;

public final class UserMapper {
    private UserMapper(){

    }

    public static User toEntity(CreateUserRequest request){
        User user = new User();

        user.setName(request.name());
        user.setBirthDate(request.birthDate());
        user.setAddress(request.address());
        user.setPhone(request.phone());
        user.setHasDriverLicense(request.hasDriverLicense());
        user.setEmail(request.email());
        user.setPassword(request.password()); // Achtung: Hier noch Klartext, muss im Service gehasht werden!

        return user;
    }

    public static void updateEntity(User user, UpdateUserRequest request){
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setEmail(request.email());
        user.setName(request.name());
        user.setHasDriverLicense(request.hasDriverLicense());
        user.setBirthDate(request.birthDate());
    }

    public static UserResponse toResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.isHasDriverLicense(),
                user.getBirthDate(),
                user.getRole()
        );
    }
}
