package vn.anyen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.anyen.entity.PushSubscriptionDoiTac;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionDoiTacRepository
        extends JpaRepository<PushSubscriptionDoiTac, Integer> {

    List<PushSubscriptionDoiTac>
    findByDoiTac_MaDoiTac(Integer maDoiTac);

    Optional<PushSubscriptionDoiTac>
    findByEndpointHash(String endpointHash);

    void deleteByEndpointHashAndDoiTac_MaDoiTac(
            String endpointHash,
            Integer maDoiTac
    );
}
