package ca.venn.hometask.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;

public interface SpringDataLoadJpaRepository extends JpaRepository<LoadJpaEntity, String> {

    boolean existsByLoadIdAndCustomerId(String loadId, String customerId);

    @Query("select coalesce(sum(l.loadAmount), 0) from LoadJpaEntity l "
            + "where l.customerId = :customerId and l.accepted = true "
            + "and l.time >= :periodStart and l.time < :periodEnd")
    BigDecimal sumAcceptedAmount(@Param("customerId") String customerId,
                                 @Param("periodStart") Instant periodStart,
                                 @Param("periodEnd") Instant periodEnd);

    @Query("select count(l) from LoadJpaEntity l "
            + "where l.customerId = :customerId and l.accepted = true "
            + "and l.time >= :periodStart and l.time < :periodEnd")
    long countAccepted(@Param("customerId") String customerId,
                        @Param("periodStart") Instant periodStart,
                        @Param("periodEnd") Instant periodEnd);
}
