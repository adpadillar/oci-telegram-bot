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
  List<TaskModel> findByProjectId(Integer projectId);
  List<TaskModel> findByAssignedToId(Integer userId);
  List<TaskModel> findByCreatedById(Integer userId);
  List<TaskModel> findByStatus(String status);
  List<TaskModel> findBySprintId(Integer sprintId);
}
