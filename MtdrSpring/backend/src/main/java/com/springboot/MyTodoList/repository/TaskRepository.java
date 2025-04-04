package com.springboot.MyTodoList.repository;
import com.springboot.MyTodoList.model.TaskModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

import javax.transaction.Transactional;

@Repository
@Transactional
@EnableTransactionManagement
public interface TaskRepository extends JpaRepository<TaskModel,Integer> {
  List<TaskModel> findByProject_ID(Integer projectId);
  List<TaskModel> findByAssignedTo(Integer userId);
  List<TaskModel> findByCreatedBy(Integer userId);
  List<TaskModel> findByStatus(String status);
  List<TaskModel> findBySprint_ID(Integer sprintId);
}
