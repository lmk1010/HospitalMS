package cn.iocoder.yudao.module.hospital.dal.mysql.customform;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.customform.vo.CustomFormPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.customform.CustomFormDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CustomFormMapper extends BaseMapperX<CustomFormDO> {

    default PageResult<CustomFormDO> selectPage(CustomFormPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CustomFormDO>()
                .likeIfPresent(CustomFormDO::getName, reqVO.getName())
                .likeIfPresent(CustomFormDO::getCode, reqVO.getCode())
                .eqIfPresent(CustomFormDO::getDeptId, reqVO.getDeptId())
                .eqIfPresent(CustomFormDO::getBizCategory, reqVO.getBizCategory())
                .eqIfPresent(CustomFormDO::getProcessKey, reqVO.getProcessKey())
                .eqIfPresent(CustomFormDO::getNodeKey, reqVO.getNodeKey())
                .eqIfPresent(CustomFormDO::getPageCode, reqVO.getPageCode())
                .eqIfPresent(CustomFormDO::getStatus, reqVO.getStatus())
                .orderByDesc(CustomFormDO::getUpdateTime)
                .orderByDesc(CustomFormDO::getId));
    }

    default CustomFormDO selectByCode(String code) {
        return selectOne(CustomFormDO::getCode, code);
    }

    default List<CustomFormDO> selectSimpleList() {
        return selectList(new LambdaQueryWrapperX<CustomFormDO>()
                .eq(CustomFormDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByDesc(CustomFormDO::getUpdateTime)
                .orderByDesc(CustomFormDO::getId));
    }

    default List<CustomFormDO> selectListByPage(String pageCode, String processKey, String nodeKey) {
        return selectList(new LambdaQueryWrapperX<CustomFormDO>()
                .eqIfPresent(CustomFormDO::getPageCode, pageCode)
                .eqIfPresent(CustomFormDO::getProcessKey, processKey)
                .eqIfPresent(CustomFormDO::getNodeKey, nodeKey)
                .eq(CustomFormDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByDesc(CustomFormDO::getUpdateTime)
                .orderByDesc(CustomFormDO::getId));
    }

}
