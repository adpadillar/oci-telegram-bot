package com.springboot.MyTodoList.repository;


import com.springboot.MyTodoList.model.SprintModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

import javax.transaction.Transactional;

@Repository
@Transactional
@EnableTransactionManagement
public interface SprintRepository extends JpaRepository<SprintModel, Integer> {
  List<SprintModel> findByProjectId(int projectId);
}
