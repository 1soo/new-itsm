package com.itsm.srm.application;

import com.itsm.srm.application.dto.QueueResponse;
import com.itsm.srm.domain.repository.QueueRepository;
import com.itsm.srm.domain.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 큐 목록·건수 조회(API-SRM-016).
 */
@Service
public class QueueService {

    private final QueueRepository queueRepository;
    private final ServiceRequestRepository requestRepository;

    public QueueService(QueueRepository queueRepository, ServiceRequestRepository requestRepository) {
        this.queueRepository = queueRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional(readOnly = true)
    public List<QueueResponse> list() {
        return queueRepository.findAll().stream()
                .map(q -> new QueueResponse(q.getId(), q.getName(), q.isDefault(),
                        requestRepository.countOpenByQueueId(q.getId())))
                .toList();
    }
}
