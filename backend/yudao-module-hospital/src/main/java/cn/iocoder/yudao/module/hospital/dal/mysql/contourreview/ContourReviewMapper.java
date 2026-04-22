package cn.iocoder.yudao.module.hospital.dal.mysql.contourreview;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.contourreview.vo.ContourReviewPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourreview.ContourReviewDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContourReviewMapper extends BaseMapperX<ContourReviewDO> {

    default PageResult<ContourReviewDO> selectPage(ContourReviewPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ContourReviewDO>()
                .eqIfPresent(ContourReviewDO::getId, reqVO.getId())
                .likeIfPresent(ContourReviewDO::getBizNo, reqVO.getBizNo())
                .eqIfPresent(ContourReviewDO::getContourTaskId, reqVO.getContourTaskId())
                .likeIfPresent(ContourReviewDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(ContourReviewDO::getReviewDoctorId, reqVO.getReviewDoctorId())
                .eqIfPresent(ContourReviewDO::getReviewResult, reqVO.getReviewResult())
                .eqIfPresent(ContourReviewDO::getWorkflowStatus, reqVO.getWorkflowStatus())
                .inIfPresent(ContourReviewDO::getWorkflowStatus, reqVO.getWorkflowStatusList())
                .eqIfPresent(ContourReviewDO::getStatus, reqVO.getStatus())
                .orderByDesc(ContourReviewDO::getId));
    }

    default ContourReviewDO selectByBizNo(String bizNo) {
        return selectOne(ContourReviewDO::getBizNo, bizNo);
    }

}
