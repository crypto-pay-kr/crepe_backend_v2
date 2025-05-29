package dev.crepe.global.error.exception;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dev.crepe.global.error.exception.model.ExceptionStatus;
import dev.crepe.global.error.exception.model.ExceptionDb;

@Repository
public interface ExceptionDbRepository extends JpaRepository<ExceptionDb, Long> {

    /**
     * Finds an exception message by its code and status.
     *
     * @param code the exception code
     * @param status the status of the exception
     * @return an Optional containing the ExceptionDb if found, or empty if not found
     */
    Optional<ExceptionDb> findByCodeAndStatus(int code, ExceptionStatus status);
}