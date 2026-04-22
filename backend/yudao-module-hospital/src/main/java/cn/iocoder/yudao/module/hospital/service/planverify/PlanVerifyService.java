package cn.iocoder.yudao.module.hospital.service.planverify;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.planverify.vo.PlanVerifyAuditReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planverify.vo.PlanVerifyPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.plandesign.PlanDesignDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planverify.PlanVerifyDO;

public interface PlanVerifyService {

    void createPlanVerifyFromDesign(PlanDesignDO planDesign);

    void verifyPlan(PlanVerifyAuditReqVO reqVO);

    void rejectPlanVerify(PlanVerifyAuditReqVO reqVO);

    PlanVerifyDO getPlanVerify(Long id);

    PageResult<PlanVerifyDO> getPlanVerifyPage(PlanVerifyPageReqVO reqVO);

}
