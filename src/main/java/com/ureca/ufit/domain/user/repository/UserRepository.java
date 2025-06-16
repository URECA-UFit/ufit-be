package com.ureca.ufit.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ureca.ufit.domain.user.exception.UserErrorCode;
import com.ureca.ufit.entity.User;
import com.ureca.ufit.global.exception.RestApiException;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	default User getByEmail(String email) {
		return findByEmail(email).orElseThrow(() -> new RestApiException(UserErrorCode.USER_NOT_FOUND));
	}

	@Query("SELECT u.ratePlanId AS ratePlanId, COUNT(u) AS count " +
			"FROM User u GROUP BY u.ratePlanId")
	List<RatePlanCountProjection> countUsersByRatePlan();

	public interface RatePlanCountProjection {
		String getRatePlanId();
		long getCount();
	}
}
