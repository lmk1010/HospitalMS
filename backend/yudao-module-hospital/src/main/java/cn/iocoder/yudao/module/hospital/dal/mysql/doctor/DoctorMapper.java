package cn.iocoder.yudao.module.hospital.dal.mysql.doctor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.doctor.vo.DoctorPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DoctorMapper extends BaseMapperX<DoctorDO> {

    default PageResult<DoctorDO> selectPage(DoctorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DoctorDO>()
                .eqIfPresent(DoctorDO::getDeptId, reqVO.getDeptId())
                .likeIfPresent(DoctorDO::getDoctorCode, reqVO.getDoctorCode())
                .likeIfPresent(DoctorDO::getName, reqVO.getName())
                .likeIfPresent(DoctorDO::getPhone, reqVO.getPhone())
                .eqIfPresent(DoctorDO::getStatus, reqVO.getStatus())
                .orderByAsc(DoctorDO::getSort)
                .orderByDesc(DoctorDO::getId));
    }

    default List<DoctorDO> selectSimpleList() {
        return selectList(new LambdaQueryWrapperX<DoctorDO>()
                .eq(DoctorDO::getStatus, 0)
                .orderByAsc(DoctorDO::getSort)
                .orderByDesc(DoctorDO::getId));
    }

    default DoctorDO selectByDoctorCode(String doctorCode) {
        return selectOne(DoctorDO::getDoctorCode, doctorCode);
    }

    default DoctorDO selectByUserId(Long userId) {
        return selectOne(DoctorDO::getUserId, userId);
    }

}
