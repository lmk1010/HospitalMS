package cn.iocoder.yudao.module.hospital.service.ctappointment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo.CtAppointmentPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo.CtAppointmentScheduleReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo.CtAppointmentSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctappointment.CtAppointmentDO;

import java.util.List;

public interface CtAppointmentService {

    Long createCtAppointment(CtAppointmentSaveReqVO createReqVO);

    void updateCtAppointment(CtAppointmentSaveReqVO updateReqVO);

    void submitCtAppointment(Long id);

    void deleteCtAppointment(Long id);

    CtAppointmentDO getCtAppointment(Long id);

    PageResult<CtAppointmentDO> getCtAppointmentPage(CtAppointmentPageReqVO reqVO);

    List<CtAppointmentDO> getCtAppointmentScheduleList(CtAppointmentScheduleReqVO reqVO);

}
