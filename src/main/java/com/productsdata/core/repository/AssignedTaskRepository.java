package com.productsdata.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.productsdata.core.entity.AssignedTask;
import com.productsdata.core.entity.Tasks;


@Repository
public interface AssignedTaskRepository extends JpaRepository<AssignedTask, Long> {

	@Query(value="SELECT t.* "
			+ "FROM tasks t "
			+ "JOIN assignedtasks a ON t.task_id = a.task_id\r\n"
			+ "WHERE a.user_id = :userId",nativeQuery = true)
	public List<Tasks> getTaskAssignedToSpecificUser(Long userId);
	
}
