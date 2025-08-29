package com.bkap.aispark.service;



import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.ObjectType;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.entity.UserRole;
import com.bkap.aispark.repository.UserRepository;



@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setEmail(updatedUser.getEmail());
            user.setPhone(updatedUser.getPhone());
            user.setRole(updatedUser.getRole());
            user.setStudentCode(updatedUser.getStudentCode());
            user.setIsActive(updatedUser.getIsActive());

            if (updatedUser.getRole() != UserRole.SYSTEM_ADMIN) {
                user.setObjectType(updatedUser.getObjectType());
                user.setObjectId(updatedUser.getObjectId());
            } else {
                user.setObjectType(ObjectType.SYSTEM);
                user.setObjectId(null);
            }

            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }


    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}

