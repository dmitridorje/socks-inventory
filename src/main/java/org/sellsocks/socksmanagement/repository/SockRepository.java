package org.sellsocks.socksmanagement.repository;

import org.sellsocks.socksmanagement.model.entity.Sock;
import org.sellsocks.socksmanagement.model.enums.SockColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SockRepository extends JpaRepository<Sock, Long>, JpaSpecificationExecutor<Sock> {

    Optional<Sock> findByColorAndCottonPart(SockColor color, int cottonPart);

    Optional<Sock> findByColorAndCottonPartAndIdNot(SockColor color, int cottonPart, Long id);
}
