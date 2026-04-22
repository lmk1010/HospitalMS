package cn.iocoder.yudao.module.hospital.service.treatmentappointment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo.TreatmentAppointmentPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo.TreatmentAppointmentScheduleReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planverify.PlanVerifyDO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo.TreatmentAppointmentSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentappointment.TreatmentAppointmentDO;

import java.util.List;

public interface TreatmentAppointmentService {

    Long createTreatmentAppointment(TreatmentAppointmentSaveReqVO createReqVO);

    void updateTreatmentAppointment(TreatmentAppointmentSaveReqVO updateReqVO);

    void createFromPlanVerify(PlanVerifyDO planVerify);

    void submitTreatmentAppointment(Long id);

    void deleteTreatmentAppointment(Long id);

    TreatmentAppointmentDO getTreatmentAppointment(Long id);

    PageResult<TreatmentAppointmentDO> getTreatmentAppointmentPage(TreatmentAppointmentPageReqVO reqVO);

    List<TreatmentAppointmentDO> getTreatmentAppointmentScheduleList(TreatmentAppointmentScheduleReqVO reqVO);

}
