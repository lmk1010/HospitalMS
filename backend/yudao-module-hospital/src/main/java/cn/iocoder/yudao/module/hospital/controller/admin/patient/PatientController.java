package cn.iocoder.yudao.module.hospital.controller.admin.patient;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientDashboardPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientDashboardRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientDashboardSummaryRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientSaveReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientSimpleRespVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.patient.PatientDO;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import cn.iocoder.yudao.module.hospital.service.patient.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 患者档案")
@RestController
@RequestMapping("/hospital/patient")
@Validated
public class PatientController {

    @Resource
    private PatientService patientService;
    @Resource
    private DoctorService doctorService;

    @PostMapping("/create")
    @Operation(summary = "创建患者")
    @PreAuthorize("@ss.hasPermission('hospital:patient:create')")
    public CommonResult<Long> createPatient(@Valid @RequestBody PatientSaveReqVO createReqVO) {
        return success(patientService.createPatient(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改患者")
    @PreAuthorize("@ss.hasPermission('hospital:patient:update')")
    public CommonResult<Boolean> updatePatient(@Valid @RequestBody PatientSaveReqVO updateReqVO) {
        patientService.updatePatient(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除患者")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:patient:delete')")
    public CommonResult<Boolean> deletePatient(@RequestParam("id") Long id) {
        patientService.deletePatient(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得患者详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:patient:query')")
    public CommonResult<PatientRespVO> getPatient(@RequestParam("id") Long id) {
        PatientDO patient = patientService.getPatient(id);
        if (patient == null) {
            return success(null);
        }
        return success(buildPatientRespVO(patient, buildDoctorMap(patient)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得患者分页")
    @PreAuthorize("@ss.hasPermission('hospital:patient:query')")
    public CommonResult<PageResult<PatientRespVO>> getPatientPage(@Validated PatientPageReqVO reqVO) {
        PageResult<PatientDO> pageResult = patientService.getPatientPage(reqVO);
        Set<Long> doctorIds = new HashSet<>();
        pageResult.getList().forEach(item -> {
            if (item.getManagerDoctorId() != null && item.getManagerDoctorId() > 0) {
                doctorIds.add(item.getManagerDoctorId());
            }
            if (item.getAttendingDoctorId() != null && item.getAttendingDoctorId() > 0) {
                doctorIds.add(item.getAttendingDoctorId());
            }
        });
        Map<Long, DoctorDO> doctorMap = buildDoctorMap(doctorIds);
        List<PatientRespVO> list = pageResult.getList().stream()
                .map(item -> buildPatientRespVO(item, doctorMap))
                .collect(Collectors.toList());
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/dashboard-page")
    @Operation(summary = "获得患者列表大屏分页")
    @PreAuthorize("@ss.hasPermission('hospital:patient:query')")
    public CommonResult<PageResult<PatientDashboardRespVO>> getPatientDashboardPage(@Validated PatientDashboardPageReqVO reqVO) {
        return success(patientService.getPatientDashboardPage(reqVO));
    }

    @GetMapping("/dashboard-summary")
    @Operation(summary = "获得患者列表大屏汇总")
    @PreAuthorize("@ss.hasPermission('hospital:patient:query')")
    public CommonResult<PatientDashboardSummaryRespVO> getPatientDashboardSummary(@Validated PatientDashboardPageReqVO reqVO) {
        return success(patientService.getPatientDashboardSummary(reqVO));
    }

    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "获得患者精简列表")
    public CommonResult<List<PatientSimpleRespVO>> getPatientSimpleList() {
        return success(BeanUtils.toBean(patientService.getPatientSimpleList(), PatientSimpleRespVO.class));
    }

    private Map<Long, DoctorDO> buildDoctorMap(PatientDO patient) {
        Set<Long> doctorIds = new HashSet<>();
        if (patient.getManagerDoctorId() != null && patient.getManagerDoctorId() > 0) {
            doctorIds.add(patient.getManagerDoctorId());
        }
        if (patient.getAttendingDoctorId() != null && patient.getAttendingDoctorId() > 0) {
            doctorIds.add(patient.getAttendingDoctorId());
        }
        return buildDoctorMap(doctorIds);
    }

    private Map<Long, DoctorDO> buildDoctorMap(Set<Long> doctorIds) {
        if (doctorIds == null || doctorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return doctorService.getDoctorList(doctorIds).stream()
                .collect(Collectors.toMap(DoctorDO::getId, Function.identity(), (a, b) -> a));
    }

    private PatientRespVO buildPatientRespVO(PatientDO patient, Map<Long, DoctorDO> doctorMap) {
        PatientRespVO respVO = BeanUtils.toBean(patient, PatientRespVO.class);
        DoctorDO managerDoctor = doctorMap.get(patient.getManagerDoctorId());
        DoctorDO attendingDoctor = doctorMap.get(patient.getAttendingDoctorId());
        respVO.setManagerDoctorName(managerDoctor == null ? "" : managerDoctor.getName());
        respVO.setAttendingDoctorName(attendingDoctor == null ? "" : attendingDoctor.getName());
        return respVO;
    }

}
