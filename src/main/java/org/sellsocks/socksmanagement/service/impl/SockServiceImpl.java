package org.sellsocks.socksmanagement.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sellsocks.socksmanagement.mapper.SockMapper;
import org.sellsocks.socksmanagement.model.dto.SockDto;
import org.sellsocks.socksmanagement.model.dto.SockUpdateDto;
import org.sellsocks.socksmanagement.model.entity.Sock;
import org.sellsocks.socksmanagement.model.enums.CriteriaOperation;
import org.sellsocks.socksmanagement.model.enums.SockColor;
import org.sellsocks.socksmanagement.repository.SockRepository;
import org.sellsocks.socksmanagement.service.SockService;
import org.sellsocks.socksmanagement.validation.SockParametersValidator;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SockServiceImpl implements SockService {

    private final SockRepository sockRepository;
    private final SockMapper sockMapper;
    private final SockParametersValidator validator;

    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional
    public SockDto addSockIncome(Sock sockIncome) {
        log.info("Adding sock income: color={}, cottonPart={}, quantity={}",
                sockIncome.getColor(), sockIncome.getCottonPart(), sockIncome.getQuantity());
        Sock sock = saveOrUpdateSock(sockIncome.getColor(), sockIncome.getCottonPart(), sockIncome.getQuantity());
        log.info("Sock income added successfully: {}", sock);
        return sockMapper.toSockDto(sock);
    }

    @Override
    @Transactional
    public SockDto subtractSockOutcome(Sock sockOutcome) {
        log.info("Subtracting sock outcome: color={}, cottonPart={}, quantity={}",
                sockOutcome.getColor(), sockOutcome.getCottonPart(), sockOutcome.getQuantity());
        Sock sock = sockRepository.findByColorAndCottonPart(sockOutcome.getColor(), sockOutcome.getCottonPart())
                .orElseThrow(() -> {
                    log.error("No socks found with given parameters: color={}, cottonPart={}",
                            sockOutcome.getColor(), sockOutcome.getCottonPart());
                    return new EntityNotFoundException("No socks found with given parameters: color = " +
                            sockOutcome.getColor() + ", cotton percentage = " + sockOutcome.getCottonPart());
                });

        validateAndSubtractQuantity(sock, sockOutcome.getQuantity());
        log.info("Sock outcome subtracted successfully: {}", sock);
        return sockMapper.toSockDto(sock);
    }

    @Override
    public int getFilteredSocksQuantity(String color, String operation, Integer cottonPart) {
        log.info("Fetching socks quantity: color={}, operation={}, cottonPart={}", color, operation, cottonPart);
        SockColor sockColor = validator.validateAndParseColor(color);
        CriteriaOperation criteriaOperation = validator.validateAndParseOperation(operation);

        List<Sock> socks = sockRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("color"), sockColor));

            switch (criteriaOperation) {
                case MORETHAN -> predicates.add(criteriaBuilder.gt(root.get("cottonPart"), cottonPart));
                case LESSTHAN -> predicates.add(criteriaBuilder.lt(root.get("cottonPart"), cottonPart));
                case EQUAL -> predicates.add(criteriaBuilder.equal(root.get("cottonPart"), cottonPart));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        int quantity = socks.stream().mapToInt(Sock::getQuantity).sum();
        log.info("Total socks quantity found: {}", quantity);
        return quantity;
    }

    @Override
    @Transactional
    public SockDto updateSock(Long id, SockUpdateDto sockUpdate) {
        log.info("Updating sock with id: {}, new data: {}", id, sockUpdate);
        Sock sockToUpdate = sockRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Sock not found with id: {}", id);
                    return new EntityNotFoundException("Sock not found with id: " + id);
                });

        if (sockUpdate.getColor() != null) {
            sockToUpdate.setColor(validator.validateAndParseColor(sockUpdate.getColor()));
        }
        if (sockUpdate.getCottonPart() != null) {
            sockToUpdate.setCottonPart(sockUpdate.getCottonPart());
        }
        if (sockUpdate.getQuantity() != null) {
            sockToUpdate.setQuantity(sockUpdate.getQuantity());
        }

        Sock mergedSock = mergeWithDuplicateIfExists(sockToUpdate, id);
        if (mergedSock != null) {
            return sockMapper.toSockDto(mergedSock);
        }

        sockRepository.save(sockToUpdate);
        log.info("Sock updated successfully: {}", sockToUpdate);
        return sockMapper.toSockDto(sockToUpdate);
    }

    @Override
    public List<Sock> getFilteredAndSortedSocks(String color, Integer cottonPartMin, Integer cottonPartMax, String sortBy, String sortOrder) {
        log.info("Fetching filtered and sorted socks: color={}, cottonPartMin={}, cottonPartMax={}, sortBy={}, sortOrder={}",
                color, cottonPartMin, cottonPartMax, sortBy, sortOrder);
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);

        List<Sock> socks = sockRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildSockFilters(color, cottonPartMin, cottonPartMax, criteriaBuilder, root);
            query.orderBy(direction == Sort.Direction.ASC
                    ? criteriaBuilder.asc(root.get(sortBy))
                    : criteriaBuilder.desc(root.get(sortBy)));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, sort);

        List<Long> sockIds = socks.stream()
                .map(Sock::getId)
                .collect(Collectors.toList());
        log.info("Filtered and sorted socks found: {} items, IDs: {}", socks.size(), sockIds);

        return socks;
    }

    @Override
    @Transactional
    public void processCsvFile(MultipartFile file) {
        log.info("Processing CSV file: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            log.error("Uploaded file is empty");
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            CSVReader csvReader = new CSVReader(reader);
            String[] line;

            csvReader.readNext();
            int lineNumber = 1;

            while ((line = csvReader.readNext()) != null) {
                lineNumber++;
                if (line.length != 3) {
                    log.error("Invalid CSV format: {}", (Object) line);
                    throw new IllegalArgumentException("Invalid CSV format. " +
                            "Each line must have 3 columns: color, cottonPart, quantity");
                }

                Object[] validatedFields = validateCsvFields(line, lineNumber);
                SockColor color = (SockColor) validatedFields[0];
                int cottonPart = (int) validatedFields[1];
                int quantity = (int) validatedFields[2];

                processSock(color, cottonPart, quantity);
            }
        } catch (IOException | CsvValidationException e) {
            log.error("Error processing CSV file: {}", e.getMessage());
            throw new IllegalArgumentException("Error processing the CSV file");
        }

        log.info("CSV file processed successfully: {}", file.getOriginalFilename());
    }

    private Object[] validateCsvFields(String[] line, int lineNumber) {
        SockColor color;
        try {
            color = validator.validateAndParseColor(line[0].trim());
        } catch (IllegalArgumentException e) {
            log.error("Invalid color: {} in csv file line {}", line[0], lineNumber);
            throw new IllegalArgumentException("Invalid color: " + line[0] + ". See CSV file line no. " + lineNumber);
        }

        int cottonPart;
        try {
            cottonPart = validator.validateCottonPart(Integer.parseInt(line[1].trim()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid cottonPart: {} in csv file line {}", line[1], lineNumber);
            throw new IllegalArgumentException("Invalid cotton percentage: " + line[1] + ". See CSV file line no. " + lineNumber);
        }

        int quantity;
        try {
            quantity = validator.validateQuantity(Integer.parseInt(line[2].trim()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid quantity value: {} in csv file line {}", line[2], lineNumber);
            throw new IllegalArgumentException("Invalid quantity: " + line[2] + ". See CSV file line no. " + lineNumber);
        }

        return new Object[]{color, cottonPart, quantity};
    }

    private void processSock(SockColor color, int cottonPart, int quantity) {
        Optional<Sock> existingSock = sockRepository.findByColorAndCottonPart(color, cottonPart);

        if (existingSock.isPresent()) {
            Sock sockToUpdate = existingSock.get();
            sockToUpdate.setQuantity(sockToUpdate.getQuantity() + quantity);
            sockRepository.save(sockToUpdate);
            log.info("Updated sock from CSV: {}", sockToUpdate);
        } else {
            Sock newSock = Sock.builder()
                    .color(color)
                    .cottonPart(cottonPart)
                    .quantity(quantity)
                    .build();
            sockRepository.save(newSock);
            log.info("Added new sock from CSV: {}", newSock);
        }
    }

    private void validateAndSubtractQuantity(Sock sock, int quantity) {
        if (sock.getQuantity() < quantity) {
            log.error("Not enough socks in stock: available={}, requested={}", sock.getQuantity(), quantity);
            throw new IllegalStateException("Not enough socks in stock: available=" + sock.getQuantity() +
                    ", requested=" + quantity);
        }
        sock.setQuantity(sock.getQuantity() - quantity);
    }

    private Sock saveOrUpdateSock(SockColor color, int cottonPart, int quantity) {
        log.info("Saving or updating sock: color={}, cottonPart={}, quantity={}", color, cottonPart, quantity);
        Sock sock = sockRepository.findByColorAndCottonPart(color, cottonPart)
                .orElseGet(() -> new Sock(color, cottonPart, 0));
        sock.setQuantity(sock.getQuantity() + quantity);
        sock = sockRepository.save(sock);
        log.info("Sock saved or updated successfully: {}", sock);
        return sock;
    }

    private List<Predicate> buildSockFilters(
            String color, Integer cottonPartMin, Integer cottonPartMax,
            CriteriaBuilder criteriaBuilder, Root<Sock> root) {
        List<Predicate> predicates = new ArrayList<>();
        if (color != null) {
            predicates.add(criteriaBuilder.equal(root.get("color"), validator.validateAndParseColor(color)));
        }
        if (cottonPartMin != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("cottonPart"), cottonPartMin));
        }
        if (cottonPartMax != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("cottonPart"), cottonPartMax));
        }
        return predicates;
    }

    private Sock mergeWithDuplicateIfExists(Sock sockToUpdate, Long id) {
        entityManager.detach(sockToUpdate);

        Optional<Sock> duplicateSock = sockRepository.findByColorAndCottonPartAndIdNot(
                sockToUpdate.getColor(),
                sockToUpdate.getCottonPart(),
                id
        );

        if (duplicateSock.isPresent() && !duplicateSock.get().getId().equals(id)) {
            Sock existingSock = duplicateSock.get();
            log.info("Merging sock with id: {} into sock with id: {}", id, existingSock.getId());

            existingSock.setQuantity(existingSock.getQuantity() + sockToUpdate.getQuantity());
            sockRepository.save(existingSock);

            sockRepository.delete(sockToUpdate);
            log.info("Sock with id: {} has been merged and deleted", id);
            return existingSock;
        }
        return null;
    }
}
