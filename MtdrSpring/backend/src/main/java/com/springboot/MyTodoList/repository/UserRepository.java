package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

import javax.transaction.Transactional;

@Repository
@Transactional
@EnableTransactionManagement
public interface UserRepository extends JpaRepository<UserModel, Integer> {
    List<UserModel> findByProjectId(int projectId);
    UserModel findByTelegramId(Long telegramId);
}
