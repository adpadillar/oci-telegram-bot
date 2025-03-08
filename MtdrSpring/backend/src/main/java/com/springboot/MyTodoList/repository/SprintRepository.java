package com.springboot.MyTodoList.repository;


import com.springboot.MyTodoList.model.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

import javax.transaction.Transactional;

@Repository
@Transactional
@EnableTransactionManagement
public interface SprintRepository extends JpaRepository<Sprint, Integer> {
  List<Sprint> findByProject_ID(int projectId);
}
