package cn.iocoder.yudao.module.hospital.controller.admin.contourtask;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo.ContourTaskPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo.ContourTaskRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo.ContourTaskSaveReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo.ContourTaskSimpleRespVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourtask.ContourTaskDO;
import cn.iocoder.yudao.module.hospital.service.contourtask.ContourTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 靶区勾画任务")
@RestController
@RequestMapping("/hospital/contour-task")
@Validated
public class ContourTaskController {

    @Resource
    private ContourTaskService contourTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建靶区勾画任务")
    @PreAuthorize("@ss.hasPermission('hospital:contour-task:create')")
    public CommonResult<Long> createContourTask(@Valid @RequestBody ContourTaskSaveReqVO createReqVO) {
        return success(contourTaskService.createContourTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改靶区勾画任务")
    @PreAuthorize("@ss.hasPermission('hospital:contour-task:update')")
    public CommonResult<Boolean> updateContourTask(@Valid @RequestBody ContourTaskSaveReqVO updateReqVO) {
        contourTaskService.updateContourTask(updateReqVO);
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交靶区勾画任务")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:contour-task:submit')")
    public CommonResult<Boolean> submitContourTask(@RequestParam("id") Long id) {
        contourTaskService.submitContourTask(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除靶区勾画任务")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:contour-task:delete')")
    public CommonResult<Boolean> deleteContourTask(@RequestParam("id") Long id) {
        contourTaskService.deleteContourTask(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得靶区勾画任务详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:contour-task:query')")
    public CommonResult<ContourTaskRespVO> getContourTask(@RequestParam("id") Long id) {
        ContourTaskDO task = contourTaskService.getContourTask(id);
        return success(BeanUtils.toBean(task, ContourTaskRespVO.class));
    }

    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "获得靶区勾画任务精简列表")
    public CommonResult<List<ContourTaskSimpleRespVO>> getContourTaskSimpleList() {
        return success(contourTaskService.getContourTaskSimpleList().stream().map(this::buildSimpleRespVO).collect(Collectors.toList()));
    }

    @GetMapping("/page")
    @Operation(summary = "获得靶区勾画任务分页")
    @PreAuthorize("@ss.hasPermission('hospital:contour-task:query')")
    public CommonResult<PageResult<ContourTaskRespVO>> getContourTaskPage(@Validated ContourTaskPageReqVO reqVO) {
        PageResult<ContourTaskDO> pageResult = contourTaskService.getContourTaskPage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), ContourTaskRespVO.class), pageResult.getTotal()));
    }

    private ContourTaskSimpleRespVO buildSimpleRespVO(ContourTaskDO task) {
        ContourTaskSimpleRespVO respVO = BeanUtils.toBean(task, ContourTaskSimpleRespVO.class);
        respVO.setDisplayName(task.getBizNo() + " / " + task.getPatientName() + (task.getTreatmentSite() == null || task.getTreatmentSite().isEmpty() ? "" : " / " + task.getTreatmentSite()));
        return respVO;
    }

}
