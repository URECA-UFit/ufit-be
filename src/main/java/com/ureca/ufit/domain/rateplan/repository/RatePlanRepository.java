package com.ureca.ufit.domain.rateplan.repository;

import static com.ureca.ufit.domain.rateplan.exception.RatePlanErrorCode.*;

import java.util.Optional;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ureca.ufit.entity.RatePlan;
import com.ureca.ufit.global.exception.RestApiException;

@Repository
public interface RatePlanRepository extends MongoRepository<RatePlan, String>, RatePlanQueryRepository {

	Optional<RatePlan> findById(String id);

	default RatePlan getById(String id) {
		return findById(id).orElseThrow(() -> new RestApiException(RATE_PLAN_NOT_FOUND));
	}
  
	List<RatePlan> findAll();
}
