package com.ureca.ufit.domain.rateplan.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ureca.ufit.entity.RatePlan;

@Repository
public interface RatePlanRepository extends MongoRepository<RatePlan, String>, RatePlanQueryRepository {
	List<RatePlan> findAll();
}
