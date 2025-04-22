package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.UserDTO;
import com.springboot.MyTodoList.model.ProjectModel;
import com.springboot.MyTodoList.model.UserModel;
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

    public List<UserModel> findAllUsers() {
        return userRepository.findAll();
    }

    public UserModel findUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    public UserModel findUserByChatId(Long chatId) {
        return userRepository.findByTelegramId(chatId);
    }

    public List<UserModel> findUsersByProject(int projectId) {
        return userRepository.findByProjectId(projectId);
    }

    public UserModel findManagerByProject(int projectId) {
        List<UserModel> users = userRepository.findByProjectId(projectId);
        return users.stream()
                .filter(user -> "manager".equals(user.getRole()))
                .findFirst()
                .orElse(null);
    }

    public UserModel createUser(UserDTO userDTO, int projectId) throws RuntimeException {
        // Find project by ID
        ProjectModel project = projectRepository.findById(projectId).orElseThrow(
            () -> new RuntimeException("Project not found")
        );
        
        // Create and save user
        UserModel user = new UserModel();
        user.setTelegramId(userDTO.getTelegramId());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setRole(userDTO.getRole());
        user.setTitle(userDTO.getTitle());
        user.setProjectId(project.getID());
        
        return userRepository.save(user);
    }

    public UserModel patchUserOnProject(int id, int projectId, UserDTO newValues) {
        Optional<UserModel> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            UserModel user = optionalUser.get();
            if (user.getProjectId() == projectId) {
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

                if (newValues.getTitle() != null) {
                    user.setTitle(newValues.getTitle());
                }
                
                return userRepository.save(user);
            }
        }
        return null;
    }

    public UserModel updateUser(int id, UserModel userDetails) {
        Optional<UserModel> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            UserModel existingUser = optionalUser.get();
            existingUser.setTelegramId(userDetails.getTelegramId());
            //Tentativo lol por que no existe project todavia
            existingUser.setProjectId(userDetails.getProjectId());
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
