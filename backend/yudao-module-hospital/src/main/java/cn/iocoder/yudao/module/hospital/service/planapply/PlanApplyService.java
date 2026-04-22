package cn.iocoder.yudao.module.hospital.service.planapply;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo.PlanApplyPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo.PlanApplySaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourtask.ContourTaskDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planapply.PlanApplyDO;

import java.util.List;

public interface PlanApplyService {

    Long createPlanApply(PlanApplySaveReqVO createReqVO);

    void updatePlanApply(PlanApplySaveReqVO updateReqVO);

    void createFromContourTask(ContourTaskDO contourTask);

    void submitPlanApply(Long id);

    void deletePlanApply(Long id);

    PlanApplyDO getPlanApply(Long id);

    List<PlanApplyDO> getPlanApplySimpleList();

    PageResult<PlanApplyDO> getPlanApplyPage(PlanApplyPageReqVO reqVO);

}
