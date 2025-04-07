package com.springboot.MyTodoList.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.springboot.MyTodoList.model.SprintModel;
import java.util.List;

public interface SprintRepository extends JpaRepository<SprintModel, Integer> {
    SprintModel findByProjectIdAndName(Integer projectId, String name);
    List<SprintModel> findByProjectId(int projectId);
}
