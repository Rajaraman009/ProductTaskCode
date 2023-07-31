package com.productsdata.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.productsdata.core.entity.ApprovalQueue;


@Repository
public interface ApprovalQueueRepository extends JpaRepository<ApprovalQueue, Long> {

	@Query(value = "select * from approval_queue aq order by aq.request_date ",nativeQuery = true)
	public List<ApprovalQueue> getListByDate();
	
}
