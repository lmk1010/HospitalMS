package cn.iocoder.yudao.module.hospital.controller.admin.doctor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.doctor.vo.DoctorPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.doctor.vo.DoctorRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.doctor.vo.DoctorSaveReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.doctor.vo.DoctorSimpleRespVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.department.DepartmentDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.service.department.DepartmentService;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 医院医生")
@RestController
@RequestMapping("/hospital/doctor")
@Validated
public class DoctorController {

    @Resource
    private DoctorService doctorService;
    @Resource
    private DepartmentService departmentService;

    @PostMapping("/create")
    @Operation(summary = "创建医生")
    @PreAuthorize("@ss.hasPermission('hospital:doctor:create')")
    public CommonResult<Long> createDoctor(@Valid @RequestBody DoctorSaveReqVO createReqVO) {
        return success(doctorService.createDoctor(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改医生")
    @PreAuthorize("@ss.hasPermission('hospital:doctor:update')")
    public CommonResult<Boolean> updateDoctor(@Valid @RequestBody DoctorSaveReqVO updateReqVO) {
        doctorService.updateDoctor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除医生")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:doctor:delete')")
    public CommonResult<Boolean> deleteDoctor(@RequestParam("id") Long id) {
        doctorService.deleteDoctor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得医生详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:doctor:query')")
    public CommonResult<DoctorRespVO> getDoctor(@RequestParam("id") Long id) {
        DoctorDO doctor = doctorService.getDoctor(id);
        if (doctor == null) {
            return success(null);
        }
        return success(buildDoctorRespVO(doctor, buildDepartmentMap(Collections.singleton(doctor.getDeptId()))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得医生分页")
    @PreAuthorize("@ss.hasPermission('hospital:doctor:query')")
    public CommonResult<PageResult<DoctorRespVO>> getDoctorPage(@Validated DoctorPageReqVO reqVO) {
        PageResult<DoctorDO> pageResult = doctorService.getDoctorPage(reqVO);
        Set<Long> deptIds = pageResult.getList().stream().map(DoctorDO::getDeptId).collect(Collectors.toSet());
        Map<Long, DepartmentDO> departmentMap = buildDepartmentMap(deptIds);
        List<DoctorRespVO> list = pageResult.getList().stream()
                .map(item -> buildDoctorRespVO(item, departmentMap))
                .collect(Collectors.toList());
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "获得医生精简列表")
    public CommonResult<List<DoctorSimpleRespVO>> getDoctorSimpleList() {
        return success(BeanUtils.toBean(doctorService.getDoctorSimpleList(), DoctorSimpleRespVO.class));
    }

    private Map<Long, DepartmentDO> buildDepartmentMap(Set<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return departmentService.getDepartmentList(deptIds).stream()
                .collect(Collectors.toMap(DepartmentDO::getId, Function.identity(), (a, b) -> a));
    }

    private DoctorRespVO buildDoctorRespVO(DoctorDO doctor, Map<Long, DepartmentDO> departmentMap) {
        DoctorRespVO respVO = BeanUtils.toBean(doctor, DoctorRespVO.class);
        DepartmentDO department = departmentMap.get(doctor.getDeptId());
        respVO.setDeptName(department == null ? "" : department.getName());
        return respVO;
    }

}
