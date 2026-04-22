package cn.iocoder.yudao.module.hospital.service.doctor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.doctor.vo.DoctorPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.doctor.vo.DoctorSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;

import java.util.Collection;
import java.util.List;

public interface DoctorService {

    Long createDoctor(DoctorSaveReqVO createReqVO);

    void updateDoctor(DoctorSaveReqVO updateReqVO);

    void deleteDoctor(Long id);

    DoctorDO getDoctor(Long id);

    List<DoctorDO> getDoctorList(Collection<Long> ids);

    List<DoctorDO> getDoctorSimpleList();

    PageResult<DoctorDO> getDoctorPage(DoctorPageReqVO reqVO);

    void validateDoctorExists(Long id);

}
