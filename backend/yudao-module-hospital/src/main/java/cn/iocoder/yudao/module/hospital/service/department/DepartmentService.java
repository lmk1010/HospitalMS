package cn.iocoder.yudao.module.hospital.service.department;

import cn.iocoder.yudao.module.hospital.controller.admin.department.vo.DepartmentListReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.department.vo.DepartmentSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.department.DepartmentDO;

import java.util.Collection;
import java.util.List;

public interface DepartmentService {

    Long createDepartment(DepartmentSaveReqVO createReqVO);

    void updateDepartment(DepartmentSaveReqVO updateReqVO);

    void deleteDepartment(Long id);

    DepartmentDO getDepartment(Long id);

    List<DepartmentDO> getDepartmentList(DepartmentListReqVO reqVO);

    List<DepartmentDO> getDepartmentList(Collection<Long> ids);

    List<DepartmentDO> getDepartmentSimpleList();

    void validateDepartmentExists(Long id);

}
