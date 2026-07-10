package com.itsm.esm.application;

import com.itsm.asset.domain.repository.AssetRepository;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.Department;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.esm.application.dto.ChecklistDetailResponse;
import com.itsm.esm.application.dto.ChecklistTaskStatusRequest;
import com.itsm.esm.application.dto.ChecklistTaskStatusResponse;
import com.itsm.esm.application.dto.MyChecklistTaskResponse;
import com.itsm.esm.domain.ChecklistStatus;
import com.itsm.esm.domain.ChecklistTaskStatus;
import com.itsm.esm.domain.EsmChecklist;
import com.itsm.esm.domain.EsmChecklistTask;
import com.itsm.esm.domain.EsmRequest;
import com.itsm.esm.domain.repository.EsmChecklistRepository;
import com.itsm.esm.domain.repository.EsmChecklistTaskRepository;
import com.itsm.esm.domain.repository.EsmRequestRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 온보딩/오프보딩 체크리스트 유스케이스(API-ESM-014~016). 상세 조회는 연계 요청 접근 권한자 또는
 * 하위 작업 담당 부서 소속만, 내 하위 작업 목록·완료 처리는 DEPT_COORDINATOR 소속 부서 일치 시에만 허용한다.
 */
@Service
public class EsmChecklistService {

    private static final String DEPT_COORDINATOR = "DEPT_COORDINATOR";

    private final EsmChecklistRepository checklistRepository;
    private final EsmChecklistTaskRepository checklistTaskRepository;
    private final EsmRequestRepository requestRepository;
    private final AppUserRepository appUserRepository;
    private final AssetRepository assetRepository;

    public EsmChecklistService(EsmChecklistRepository checklistRepository,
                               EsmChecklistTaskRepository checklistTaskRepository,
                               EsmRequestRepository requestRepository,
                               AppUserRepository appUserRepository,
                               AssetRepository assetRepository) {
        this.checklistRepository = checklistRepository;
        this.checklistTaskRepository = checklistTaskRepository;
        this.requestRepository = requestRepository;
        this.appUserRepository = appUserRepository;
        this.assetRepository = assetRepository;
    }

    // ---------- detail (API-ESM-014) ----------

    @Transactional(readOnly = true)
    public ChecklistDetailResponse detail(Long id) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        EsmChecklist checklist = findChecklist(id);
        List<EsmChecklistTask> tasks = checklistTaskRepository.findByChecklistId(id);
        assertCanView(principal, checklist, tasks);

        List<ChecklistDetailResponse.TaskInfo> taskInfos = tasks.stream()
                .map(t -> new ChecklistDetailResponse.TaskInfo(t.getId(), t.getDepartment(), t.getDescription(),
                        t.getStatus().name(), t.getRelatedAssetId(), relatedAssetKey(t.getRelatedAssetId())))
                .toList();
        return new ChecklistDetailResponse(checklist.getId(), checklist.getType().name(),
                checklist.getTargetUserName(), checklist.getStatus().name(), taskInfos);
    }

    private void assertCanView(AuthPrincipal principal, EsmChecklist checklist, List<EsmChecklistTask> tasks) {
        EsmRequest linkedRequest = requestRepository.findByChecklistId(checklist.getId()).orElse(null);
        if (linkedRequest != null) {
            if (linkedRequest.getRequesterId().equals(principal.userId())) {
                return;
            }
            if (principal.roles().contains(DEPT_COORDINATOR)) {
                Department myDept = myDepartment(principal);
                if (myDept != null && myDept == linkedRequest.getDepartment()) {
                    return;
                }
            }
        }
        Department myDept = myDepartment(principal);
        if (myDept != null && tasks.stream().anyMatch(t -> t.getDepartment() == myDept)) {
            return;
        }
        throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    // ---------- my tasks (API-ESM-015) ----------

    @Transactional(readOnly = true)
    public PageResponse<MyChecklistTaskResponse> myTasks(ChecklistTaskStatus status, Pageable pageable) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        requireDeptCoordinator(principal);
        Department myDept = myDepartment(principal);
        if (myDept == null) {
            return new PageResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0);
        }
        return PageResponse.from(checklistTaskRepository.search(myDept, status, pageable), this::toMyTask);
    }

    // ---------- task status (API-ESM-016) ----------

    @Transactional
    public ChecklistTaskStatusResponse updateTaskStatus(Long taskId, ChecklistTaskStatusRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        requireDeptCoordinator(principal);
        EsmChecklistTask task = findTask(taskId);
        Department myDept = myDepartment(principal);
        if (myDept == null || myDept != task.getDepartment()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        task.markDone();
        checklistTaskRepository.save(task);

        EsmChecklist checklist = checklistRepository.findById(task.getChecklistId()).orElseThrow(
                () -> new BusinessException(ErrorCode.ESM_CHECKLIST_NOT_FOUND));
        long remaining = checklistTaskRepository.countByChecklistIdAndStatusNot(checklist.getId(), ChecklistTaskStatus.DONE);
        if (remaining == 0) {
            checklist.changeStatus(ChecklistStatus.COMPLETED);
            checklistRepository.save(checklist);
        }
        return new ChecklistTaskStatusResponse(task.getId(), request.status().name(), checklist.getStatus().name());
    }

    // ---------- helpers ----------

    private void requireDeptCoordinator(AuthPrincipal principal) {
        if (!principal.roles().contains(DEPT_COORDINATOR)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private Department myDepartment(AuthPrincipal principal) {
        return appUserRepository.findById(principal.userId()).map(AppUser::getDepartment).orElse(null);
    }

    private EsmChecklist findChecklist(Long id) {
        return checklistRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.ESM_CHECKLIST_NOT_FOUND));
    }

    private EsmChecklistTask findTask(Long id) {
        return checklistTaskRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.ESM_CHECKLIST_TASK_NOT_FOUND));
    }

    private String relatedAssetKey(Long assetId) {
        return assetId == null ? null : assetRepository.findById(assetId).map(a -> a.getAssetKey()).orElse(null);
    }

    private MyChecklistTaskResponse toMyTask(EsmChecklistTask task) {
        EsmChecklist checklist = checklistRepository.findById(task.getChecklistId()).orElse(null);
        return new MyChecklistTaskResponse(task.getId(), task.getChecklistId(),
                checklist != null ? checklist.getType().name() : null,
                checklist != null ? checklist.getTargetUserName() : null,
                task.getDescription(), task.getStatus().name());
    }
}
