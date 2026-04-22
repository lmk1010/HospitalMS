package cn.iocoder.yudao.module.hospital.service.patient;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientDashboardPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientDashboardRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientDashboardSummaryRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.patient.PatientDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface PatientService {

    Long createPatient(PatientSaveReqVO createReqVO);

    void updatePatient(PatientSaveReqVO updateReqVO);

    void deletePatient(Long id);

    PatientDO getPatient(Long id);

    List<PatientDO> getPatientSimpleList();

    PageResult<PatientDO> getPatientPage(PatientPageReqVO reqVO);

    PageResult<PatientDashboardRespVO> getPatientDashboardPage(PatientDashboardPageReqVO reqVO);

    PatientDashboardSummaryRespVO getPatientDashboardSummary(PatientDashboardPageReqVO reqVO);

    Map<Long, String> getCurrentNodeCodeMap(Collection<Long> patientIds);

    void validatePatientExists(Long id);

}
