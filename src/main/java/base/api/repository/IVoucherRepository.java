package base.api.repository;

import base.api.entity.VoucherModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IVoucherRepository extends JpaRepository<VoucherModel, Long> {
    Optional<VoucherModel> findByCodeIgnoreCase(String code);
}

