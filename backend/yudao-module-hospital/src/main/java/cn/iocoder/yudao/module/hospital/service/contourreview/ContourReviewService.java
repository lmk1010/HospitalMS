package cn.iocoder.yudao.module.hospital.service.contourreview;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.contourreview.vo.ContourReviewAuditReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.contourreview.vo.ContourReviewPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourreview.ContourReviewDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourtask.ContourTaskDO;

public interface ContourReviewService {

    void createContourReviewFromTask(ContourTaskDO contourTask);

    void approveContourReview(ContourReviewAuditReqVO reqVO);

    void rejectContourReview(ContourReviewAuditReqVO reqVO);

    ContourReviewDO getContourReview(Long id);

    PageResult<ContourReviewDO> getContourReviewPage(ContourReviewPageReqVO reqVO);

}
