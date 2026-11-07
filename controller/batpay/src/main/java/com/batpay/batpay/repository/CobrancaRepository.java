package com.batpay.batpay.repository;

import com.batpay.batpay.model.Cobranca;
import com.batpay.batpay.model.StatusCobranca;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CobrancaRepository extends JpaRepository<Cobranca, Long> {
    List<Cobranca> findByStatus(StatusCobranca status);
    List<Cobranca> findByStatusAndDataVencimentoGreaterThanEqual(StatusCobranca status, LocalDate data);
    List<Cobranca> findByStatusOrStatusAndDataVencimentoLessThan(
            StatusCobranca statusRegistrado, StatusCobranca statusPendente, LocalDate data);
    boolean existsByClienteId(Long clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cobranca c where c.id = :id")
    Optional<Cobranca> buscarParaAlteracao(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cobranca c where c.id = (select p.cobranca.id from Pagamento p where p.id = :pagamentoId)")
    Optional<Cobranca> buscarPeloPagamentoParaAlteracao(UUID pagamentoId);
}
