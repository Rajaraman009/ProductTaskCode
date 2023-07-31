package com.productsdata.core.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.productsdata.core.entity.Tasks;
import com.productsdata.core.enumfolder.TaskStatus;


@Repository
public interface TasksRepository extends JpaRepository<Tasks, Long> {
	
	List<Tasks> findByTaskStatus(TaskStatus taskStatus);
	
	List<Tasks> findByTaskStatusAndCompletedDateBetween(TaskStatus taskStatus, Date startDate, Date endDate);
	
}
