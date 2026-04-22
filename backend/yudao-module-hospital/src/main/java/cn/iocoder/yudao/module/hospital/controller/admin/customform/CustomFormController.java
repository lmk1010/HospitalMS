package cn.iocoder.yudao.module.hospital.controller.admin.customform;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.customform.vo.CustomFormPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.customform.vo.CustomFormRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.customform.vo.CustomFormSaveReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.customform.vo.CustomFormSimpleRespVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.customform.CustomFormDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.department.DepartmentDO;
import cn.iocoder.yudao.module.hospital.service.customform.CustomFormService;
import cn.iocoder.yudao.module.hospital.service.department.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 医院自定义表单")
@RestController
@RequestMapping("/hospital/custom-form")
@Validated
public class CustomFormController {

    @Resource
    private CustomFormService customFormService;
    @Resource
    private DepartmentService departmentService;

    @PostMapping("/create")
    @Operation(summary = "创建医院自定义表单")
    @PreAuthorize("@ss.hasPermission('hospital:custom-form:create')")
    public CommonResult<Long> createCustomForm(@Valid @RequestBody CustomFormSaveReqVO createReqVO) {
        return success(customFormService.createCustomForm(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改医院自定义表单")
    @PreAuthorize("@ss.hasPermission('hospital:custom-form:update')")
    public CommonResult<Boolean> updateCustomForm(@Valid @RequestBody CustomFormSaveReqVO updateReqVO) {
        customFormService.updateCustomForm(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除医院自定义表单")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:custom-form:delete')")
    public CommonResult<Boolean> deleteCustomForm(@RequestParam("id") Long id) {
        customFormService.deleteCustomForm(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得医院自定义表单详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:custom-form:query')")
    public CommonResult<CustomFormRespVO> getCustomForm(@RequestParam("id") Long id) {
        CustomFormDO customForm = customFormService.getCustomForm(id);
        if (customForm == null) {
            return success(null);
        }
        return success(buildCustomFormRespVO(customForm, buildDepartmentMapByDeptId(customForm.getDeptId())));
    }

    @GetMapping("/page")
    @Operation(summary = "获得医院自定义表单分页")
    @PreAuthorize("@ss.hasPermission('hospital:custom-form:query')")
    public CommonResult<PageResult<CustomFormRespVO>> getCustomFormPage(@Validated CustomFormPageReqVO reqVO) {
        PageResult<CustomFormDO> pageResult = customFormService.getCustomFormPage(reqVO);
        Set<Long> deptIds = pageResult.getList().stream()
                .map(CustomFormDO::getDeptId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, DepartmentDO> departmentMap = buildDepartmentMap(deptIds);
        List<CustomFormRespVO> list = pageResult.getList().stream()
                .map(item -> buildCustomFormRespVO(item, departmentMap))
                .collect(Collectors.toList());
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "获得医院自定义表单精简列表")
    public CommonResult<List<CustomFormSimpleRespVO>> getCustomFormSimpleList() {
        return success(BeanUtils.toBean(customFormService.getCustomFormSimpleList(), CustomFormSimpleRespVO.class));
    }

    @GetMapping("/get-by-page")
    @Operation(summary = "按页面获得启用中的医院自定义表单")
    @Parameter(name = "pageCode", required = true)
    public CommonResult<CustomFormRespVO> getCustomFormByPage(@RequestParam("pageCode") String pageCode,
                                                              @RequestParam(value = "processKey", required = false) String processKey,
                                                              @RequestParam(value = "nodeKey", required = false) String nodeKey) {
        CustomFormDO customForm = customFormService.getCustomFormByPage(pageCode, processKey, nodeKey);
        if (customForm == null) {
            return success(null);
        }
        return success(buildCustomFormRespVO(customForm, buildDepartmentMapByDeptId(customForm.getDeptId())));
    }

    private Map<Long, DepartmentDO> buildDepartmentMapByDeptId(Long deptId) {
        if (deptId == null || deptId <= 0) {
            return Collections.emptyMap();
        }
        return buildDepartmentMap(Collections.singleton(deptId));
    }

    private Map<Long, DepartmentDO> buildDepartmentMap(Set<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return departmentService.getDepartmentList(deptIds).stream()
                .collect(Collectors.toMap(DepartmentDO::getId, Function.identity(), (a, b) -> a));
    }

    private CustomFormRespVO buildCustomFormRespVO(CustomFormDO customForm, Map<Long, DepartmentDO> departmentMap) {
        CustomFormRespVO respVO = BeanUtils.toBean(customForm, CustomFormRespVO.class);
        DepartmentDO department = departmentMap.get(customForm.getDeptId());
        respVO.setDeptName(department == null ? "" : department.getName());
        return respVO;
    }

}
