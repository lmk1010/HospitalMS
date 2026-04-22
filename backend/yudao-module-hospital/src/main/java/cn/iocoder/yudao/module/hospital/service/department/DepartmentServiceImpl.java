package cn.iocoder.yudao.module.hospital.service.department;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.department.vo.DepartmentListReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.department.vo.DepartmentSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.department.DepartmentDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.department.DepartmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.*;

@Service
@Validated
public class DepartmentServiceImpl implements DepartmentService {

    @Resource
    private DepartmentMapper departmentMapper;

    @Override
    public Long createDepartment(DepartmentSaveReqVO createReqVO) {
        validateDepartmentForCreateOrUpdate(null, createReqVO);
        DepartmentDO department = BeanUtils.toBean(createReqVO, DepartmentDO.class);
        normalizeDepartment(department);
        departmentMapper.insert(department);
        return department.getId();
    }

    @Override
    public void updateDepartment(DepartmentSaveReqVO updateReqVO) {
        validateDepartmentForCreateOrUpdate(updateReqVO.getId(), updateReqVO);
        DepartmentDO department = BeanUtils.toBean(updateReqVO, DepartmentDO.class);
        normalizeDepartment(department);
        departmentMapper.updateById(department);
    }

    @Override
    public void deleteDepartment(Long id) {
        validateDepartmentExists(id);
        if (departmentMapper.selectCountByParentId(id) > 0) {
            throw exception(DEPARTMENT_EXISTS_CHILDREN);
        }
        departmentMapper.deleteById(id);
    }

    @Override
    public DepartmentDO getDepartment(Long id) {
        return departmentMapper.selectById(id);
    }

    @Override
    public List<DepartmentDO> getDepartmentList(DepartmentListReqVO reqVO) {
        return departmentMapper.selectList(reqVO);
    }

    @Override
    public List<DepartmentDO> getDepartmentList(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return departmentMapper.selectByIds(ids);
    }

    @Override
    public List<DepartmentDO> getDepartmentSimpleList() {
        return departmentMapper.selectSimpleList();
    }

    @Override
    public void validateDepartmentExists(Long id) {
        if (id == null || id <= 0) {
            return;
        }
        if (departmentMapper.selectById(id) == null) {
            throw exception(DEPARTMENT_NOT_EXISTS);
        }
    }

    private void validateDepartmentForCreateOrUpdate(Long id, DepartmentSaveReqVO reqVO) {
        if (id != null) {
            validateDepartmentExists(id);
        }
        if (reqVO.getParentId() != null && reqVO.getParentId() > 0) {
            if (Objects.equals(id, reqVO.getParentId())) {
                throw exception(DEPARTMENT_PARENT_ERROR);
            }
            if (departmentMapper.selectById(reqVO.getParentId()) == null) {
                throw exception(DEPARTMENT_PARENT_NOT_EXISTS);
            }
        }
        validateDepartmentNameUnique(id, reqVO.getName());
        validateDepartmentCodeUnique(id, reqVO.getCode());
    }

    private void validateDepartmentNameUnique(Long id, String name) {
        DepartmentDO department = departmentMapper.selectByName(name);
        if (department == null) {
            return;
        }
        if (!Objects.equals(department.getId(), id)) {
            throw exception(DEPARTMENT_NAME_DUPLICATE);
        }
    }

    private void validateDepartmentCodeUnique(Long id, String code) {
        DepartmentDO department = departmentMapper.selectByCode(code);
        if (department == null) {
            return;
        }
        if (!Objects.equals(department.getId(), id)) {
            throw exception(DEPARTMENT_CODE_DUPLICATE);
        }
    }

    private void normalizeDepartment(DepartmentDO department) {
        if (department.getParentId() == null) {
            department.setParentId(0L);
        }
        if (department.getType() == null) {
            department.setType(1);
        }
        if (department.getSort() == null) {
            department.setSort(0);
        }
        if (department.getStatus() == null) {
            department.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
    }

}
