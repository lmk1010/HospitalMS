package cn.iocoder.yudao.module.hospital.service.plandesign;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.plandesign.vo.PlanDesignPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.plandesign.vo.PlanDesignSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planapply.PlanApplyDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.plandesign.PlanDesignDO;

public interface PlanDesignService {

    Long createPlanDesign(PlanDesignSaveReqVO createReqVO);

    void updatePlanDesign(PlanDesignSaveReqVO updateReqVO);

    void createFromPlanApply(PlanApplyDO planApply);

    void submitPlanDesign(Long id);

    void deletePlanDesign(Long id);

    PlanDesignDO getPlanDesign(Long id);

    PageResult<PlanDesignDO> getPlanDesignPage(PlanDesignPageReqVO reqVO);

}
