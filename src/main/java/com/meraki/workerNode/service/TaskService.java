package com.meraki.workerNode.service;

import com.meraki.workerNode.dto.Task;
import com.meraki.workerNode.repository.TaskEventSourcingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    TaskEventSourcingRepository taskEventSourcingRepository;

    public List<Task> getTaskList(String userId) {
        // TODO: Add cursor based pagination
        return taskEventSourcingRepository.getTaskList(userId);
    }

}

