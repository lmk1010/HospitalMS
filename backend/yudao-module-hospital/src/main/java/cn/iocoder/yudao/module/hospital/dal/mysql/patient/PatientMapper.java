package cn.iocoder.yudao.module.hospital.dal.mysql.patient;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.patient.PatientDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PatientMapper extends BaseMapperX<PatientDO> {

    default PageResult<PatientDO> selectPage(PatientPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PatientDO>()
                .likeIfPresent(PatientDO::getPatientNo, reqVO.getPatientNo())
                .likeIfPresent(PatientDO::getName, reqVO.getName())
                .likeIfPresent(PatientDO::getIdNo, reqVO.getIdNo())
                .likeIfPresent(PatientDO::getPatientPhone, reqVO.getPatientPhone())
                .likeIfPresent(PatientDO::getHospitalizationNo, reqVO.getHospitalizationNo())
                .likeIfPresent(PatientDO::getSocialSecurityNo, reqVO.getSocialSecurityNo())
                .eqIfPresent(PatientDO::getPatientType, reqVO.getPatientType())
                .eqIfPresent(PatientDO::getManagerDoctorId, reqVO.getManagerDoctorId())
                .eqIfPresent(PatientDO::getAttendingDoctorId, reqVO.getAttendingDoctorId())
                .eqIfPresent(PatientDO::getStatus, reqVO.getStatus())
                .orderByDesc(PatientDO::getId));
    }

    default List<PatientDO> selectSimpleList() {
        return selectList(new LambdaQueryWrapperX<PatientDO>()
                .eq(PatientDO::getStatus, 0)
                .orderByDesc(PatientDO::getId));
    }

    default PatientDO selectByPatientNo(String patientNo) {
        return selectOne(PatientDO::getPatientNo, patientNo);
    }

    default PatientDO selectByIdNo(String idNo) {
        return selectOne(PatientDO::getIdNo, idNo);
    }

}
