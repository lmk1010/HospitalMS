package cn.iocoder.yudao.module.hospital.service.contourtask;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo.ContourTaskPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo.ContourTaskSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourtask.ContourTaskDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctappointment.CtAppointmentDO;

import java.util.List;

public interface ContourTaskService {

    Long createContourTask(ContourTaskSaveReqVO createReqVO);

    void updateContourTask(ContourTaskSaveReqVO updateReqVO);

    void createFromCtAppointment(CtAppointmentDO appointment);

    void submitContourTask(Long id);

    void deleteContourTask(Long id);

    ContourTaskDO getContourTask(Long id);

    List<ContourTaskDO> getContourTaskSimpleList();

    PageResult<ContourTaskDO> getContourTaskPage(ContourTaskPageReqVO reqVO);

}
