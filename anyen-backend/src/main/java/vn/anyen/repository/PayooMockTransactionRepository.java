package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.anyen.entity.PayooMockTransaction;

import java.util.List;

public interface PayooMockTransactionRepository
        extends JpaRepository<
        PayooMockTransaction,
        String
        > {

    List<PayooMockTransaction>
    findByMaDoiTacOrderByCreatedAtDesc(
            Integer maDoiTac
    );
}