package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.InsightDTO;
import com.springboot.MyTodoList.model.TaskModel;
import com.springboot.MyTodoList.model.UserModel;
import com.springboot.MyTodoList.model.SprintModel;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIService {

    public List<InsightDTO> generateEfficiencyInsights(List<TaskModel> tasks, List<UserModel> users, List<SprintModel> sprints) {
        // Simulación de llamada a un modelo de IA
        // Aquí puedes integrar un modelo real como OpenAI o cualquier otro servicio de IA
        return List.of(
            new InsightDTO("positive", "Buen balance de tareas", "El equipo tiene una distribución equilibrada de tareas."),
            new InsightDTO("warning", "Baja tasa de finalización", "Solo el 50% de las tareas están completadas. Revisa la planificación.")
        );
    }
}