package com.taskflow.taskflow_backend.repository;

import com.taskflow.taskflow_backend.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

    List<TaskAttachment> findByTask_Id(Long taskId);

    int countByTask_Id(Long taskId);  // ✅ for max 5 files check (TC-F08)
}