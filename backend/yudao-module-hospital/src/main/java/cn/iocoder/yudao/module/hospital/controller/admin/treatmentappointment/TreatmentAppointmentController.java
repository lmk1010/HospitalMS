package cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo.TreatmentAppointmentPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo.TreatmentAppointmentRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo.TreatmentAppointmentScheduleReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo.TreatmentAppointmentSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentappointment.TreatmentAppointmentDO;
import cn.iocoder.yudao.module.hospital.service.treatmentappointment.TreatmentAppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 治疗预约")
@RestController
@RequestMapping("/hospital/treatment-appointment")
@Validated
public class TreatmentAppointmentController {

    @Resource
    private TreatmentAppointmentService treatmentAppointmentService;

    @PostMapping("/create")
    @Operation(summary = "创建治疗预约")
    @PreAuthorize("@ss.hasPermission('hospital:treatment-appointment:create')")
    public CommonResult<Long> createTreatmentAppointment(@Valid @RequestBody TreatmentAppointmentSaveReqVO createReqVO) {
        return success(treatmentAppointmentService.createTreatmentAppointment(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改治疗预约")
    @PreAuthorize("@ss.hasPermission('hospital:treatment-appointment:update')")
    public CommonResult<Boolean> updateTreatmentAppointment(@Valid @RequestBody TreatmentAppointmentSaveReqVO updateReqVO) {
        treatmentAppointmentService.updateTreatmentAppointment(updateReqVO);
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交治疗预约")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:treatment-appointment:submit')")
    public CommonResult<Boolean> submitTreatmentAppointment(@RequestParam("id") Long id) {
        treatmentAppointmentService.submitTreatmentAppointment(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除治疗预约")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:treatment-appointment:delete')")
    public CommonResult<Boolean> deleteTreatmentAppointment(@RequestParam("id") Long id) {
        treatmentAppointmentService.deleteTreatmentAppointment(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得治疗预约详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:treatment-appointment:query')")
    public CommonResult<TreatmentAppointmentRespVO> getTreatmentAppointment(@RequestParam("id") Long id) {
        TreatmentAppointmentDO appointment = treatmentAppointmentService.getTreatmentAppointment(id);
        return success(BeanUtils.toBean(appointment, TreatmentAppointmentRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得治疗预约分页")
    @PreAuthorize("@ss.hasPermission('hospital:treatment-appointment:query')")
    public CommonResult<PageResult<TreatmentAppointmentRespVO>> getTreatmentAppointmentPage(@Validated TreatmentAppointmentPageReqVO reqVO) {
        PageResult<TreatmentAppointmentDO> pageResult = treatmentAppointmentService.getTreatmentAppointmentPage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), TreatmentAppointmentRespVO.class), pageResult.getTotal()));
    }

    @GetMapping("/schedule-list")
    @Operation(summary = "获得治疗预约排班列表")
    @PreAuthorize("@ss.hasPermission('hospital:treatment-appointment:query')")
    public CommonResult<List<TreatmentAppointmentRespVO>> getTreatmentAppointmentScheduleList(@Validated TreatmentAppointmentScheduleReqVO reqVO) {
        return success(BeanUtils.toBean(treatmentAppointmentService.getTreatmentAppointmentScheduleList(reqVO), TreatmentAppointmentRespVO.class));
    }

}
