package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.UserDTO;
import com.springboot.MyTodoList.model.Project;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.repository.ProjectRepository;
import com.springboot.MyTodoList.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> findUsersByProject(int projectId) {
        return userRepository.findByProject_ID(projectId);
    }

    // public List<User> findUsersByProject(int projectId) {
    //     return userRepository.findByProjectId(projectId);
    // }

    // public User findUserByTelegramId(int telegramId) {
    //     return userRepository.findByTelegramId(telegramId);
    // }

    public User createUser(UserDTO userDTO, int projectId) throws RuntimeException {
        // Find project by ID
        Project project = projectRepository.findById(projectId).orElseThrow(
            () -> new RuntimeException("Project not found")
        );
        
        // Create and save user
        User user = new User();
        user.setTelegramId(userDTO.getTelegramId());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setRole(userDTO.getRole());
        user.setProject(project);
        
        return userRepository.save(user);
    }

    public User patchUserOnProject(int id, int projectId, UserDTO newValues) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getProject().getID() == projectId) {
                if (newValues.getTelegramId() != null) {
                    user.setTelegramId(newValues.getTelegramId());
                }
                
                if (newValues.getFirstName() != null) {
                    user.setFirstName(newValues.getFirstName());
                }
                
                if (newValues.getLastName() != null) {
                    user.setLastName(newValues.getLastName());
                }
                
                if (newValues.getRole() != null) {
                    user.setRole(newValues.getRole());
                }
                
                return userRepository.save(user);
            }
        }
        return null;
    }

    public User updateUser(int id, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setTelegramId(userDetails.getTelegramId());
            //Tentativo lol por que no existe project todavia
            existingUser.setProject(userDetails.getProject());
            existingUser.setFirstName(userDetails.getFirstName());
            existingUser.setLastName(userDetails.getLastName());
            existingUser.setRole(userDetails.getRole());
            return userRepository.save(existingUser);
        }
        return null;
    }

    public boolean deleteUser(int id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
