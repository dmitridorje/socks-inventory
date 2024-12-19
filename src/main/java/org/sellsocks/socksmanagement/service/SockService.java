package org.sellsocks.socksmanagement.service;

import org.sellsocks.socksmanagement.model.dto.SockDto;
import org.sellsocks.socksmanagement.model.dto.SockUpdateDto;
import org.sellsocks.socksmanagement.model.entity.Sock;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SockService {

    SockDto addSockIncome(Sock sockIncome);
    SockDto subtractSockOutcome(Sock sockOutcome);
    int getFilteredSocksQuantity(String color, String operation, Integer cottonPart);
    SockDto updateSock(Long id, SockUpdateDto sockUpdate);
    void processCsvFile(MultipartFile file);
    List<Sock> getFilteredAndSortedSocks(String color, Integer cottonPartMin, Integer cottonPartMax,
                                         String sortBy, String sortOrder);
}