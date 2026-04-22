package cn.iocoder.yudao.module.hospital.controller.admin.ctappointment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo.CtAppointmentPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo.CtAppointmentRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo.CtAppointmentScheduleReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo.CtAppointmentSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctappointment.CtAppointmentDO;
import cn.iocoder.yudao.module.hospital.service.ctappointment.CtAppointmentService;
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

@Tag(name = "管理后台 - CT预约")
@RestController
@RequestMapping("/hospital/ct-appointment")
@Validated
public class CtAppointmentController {

    @Resource
    private CtAppointmentService ctAppointmentService;

    @PostMapping("/create")
    @Operation(summary = "创建CT预约")
    @PreAuthorize("@ss.hasPermission('hospital:ct-appointment:create')")
    public CommonResult<Long> createCtAppointment(@Valid @RequestBody CtAppointmentSaveReqVO createReqVO) {
        return success(ctAppointmentService.createCtAppointment(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改CT预约")
    @PreAuthorize("@ss.hasPermission('hospital:ct-appointment:update')")
    public CommonResult<Boolean> updateCtAppointment(@Valid @RequestBody CtAppointmentSaveReqVO updateReqVO) {
        ctAppointmentService.updateCtAppointment(updateReqVO);
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交CT预约")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:ct-appointment:submit')")
    public CommonResult<Boolean> submitCtAppointment(@RequestParam("id") Long id) {
        ctAppointmentService.submitCtAppointment(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除CT预约")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:ct-appointment:delete')")
    public CommonResult<Boolean> deleteCtAppointment(@RequestParam("id") Long id) {
        ctAppointmentService.deleteCtAppointment(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得CT预约详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:ct-appointment:query')")
    public CommonResult<CtAppointmentRespVO> getCtAppointment(@RequestParam("id") Long id) {
        CtAppointmentDO appointment = ctAppointmentService.getCtAppointment(id);
        return success(BeanUtils.toBean(appointment, CtAppointmentRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得CT预约分页")
    @PreAuthorize("@ss.hasPermission('hospital:ct-appointment:query')")
    public CommonResult<PageResult<CtAppointmentRespVO>> getCtAppointmentPage(@Validated CtAppointmentPageReqVO reqVO) {
        PageResult<CtAppointmentDO> pageResult = ctAppointmentService.getCtAppointmentPage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), CtAppointmentRespVO.class), pageResult.getTotal()));
    }

    @GetMapping("/schedule-list")
    @Operation(summary = "获得CT预约排班列表")
    @PreAuthorize("@ss.hasPermission('hospital:ct-appointment:query')")
    public CommonResult<List<CtAppointmentRespVO>> getCtAppointmentScheduleList(@Validated CtAppointmentScheduleReqVO reqVO) {
        return success(BeanUtils.toBean(ctAppointmentService.getCtAppointmentScheduleList(reqVO), CtAppointmentRespVO.class));
    }

}
