package com.batpay.batpay.repository;

import com.batpay.batpay.model.MetodoPagamento;
import com.batpay.batpay.model.Pagamento;
import com.batpay.batpay.model.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepository extends JpaRepository<Pagamento, UUID> {
    Optional<Pagamento> findByCobrancaIdAndChaveIdempotencia(Long cobrancaId, String chaveIdempotencia);
    boolean existsByCobrancaId(Long cobrancaId);
    @Query("""
            select p from Pagamento p
            where (:cobrancaId is null or p.cobranca.id = :cobrancaId)
              and (:status is null or p.status = :status)
              and (:metodo is null or p.metodo = :metodo)
            order by p.criadoEm desc, p.id desc
            """)
    List<Pagamento> listar(Long cobrancaId, StatusPagamento status, MetodoPagamento metodo);
}
