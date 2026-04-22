package cn.iocoder.yudao.module.hospital.service.doctor;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.doctor.vo.DoctorPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.doctor.vo.DoctorSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.doctor.DoctorMapper;
import cn.iocoder.yudao.module.hospital.service.department.DepartmentService;
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
public class DoctorServiceImpl implements DoctorService {

    @Resource
    private DoctorMapper doctorMapper;
    @Resource
    private DepartmentService departmentService;

    @Override
    public Long createDoctor(DoctorSaveReqVO createReqVO) {
        validateDoctorForCreateOrUpdate(null, createReqVO);
        DoctorDO doctor = BeanUtils.toBean(createReqVO, DoctorDO.class);
        normalizeDoctor(doctor);
        doctorMapper.insert(doctor);
        return doctor.getId();
    }

    @Override
    public void updateDoctor(DoctorSaveReqVO updateReqVO) {
        validateDoctorForCreateOrUpdate(updateReqVO.getId(), updateReqVO);
        DoctorDO doctor = BeanUtils.toBean(updateReqVO, DoctorDO.class);
        normalizeDoctor(doctor);
        doctorMapper.updateById(doctor);
    }

    @Override
    public void deleteDoctor(Long id) {
        validateDoctorExists(id);
        doctorMapper.deleteById(id);
    }

    @Override
    public DoctorDO getDoctor(Long id) {
        return doctorMapper.selectById(id);
    }

    @Override
    public List<DoctorDO> getDoctorList(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return doctorMapper.selectByIds(ids);
    }

    @Override
    public List<DoctorDO> getDoctorSimpleList() {
        return doctorMapper.selectSimpleList();
    }

    @Override
    public PageResult<DoctorDO> getDoctorPage(DoctorPageReqVO reqVO) {
        return doctorMapper.selectPage(reqVO);
    }

    @Override
    public void validateDoctorExists(Long id) {
        if (id == null || id <= 0) {
            return;
        }
        if (doctorMapper.selectById(id) == null) {
            throw exception(DOCTOR_NOT_EXISTS);
        }
    }

    private void validateDoctorForCreateOrUpdate(Long id, DoctorSaveReqVO reqVO) {
        if (id != null) {
            validateDoctorExists(id);
        }
        departmentService.validateDepartmentExists(reqVO.getDeptId());
        validateDoctorCodeUnique(id, reqVO.getDoctorCode());
        validateDoctorUserUnique(id, reqVO.getUserId());
    }

    private void validateDoctorCodeUnique(Long id, String doctorCode) {
        DoctorDO doctor = doctorMapper.selectByDoctorCode(doctorCode);
        if (doctor == null) {
            return;
        }
        if (!Objects.equals(doctor.getId(), id)) {
            throw exception(DOCTOR_CODE_DUPLICATE);
        }
    }

    private void validateDoctorUserUnique(Long id, Long userId) {
        if (userId == null || userId <= 0) {
            return;
        }
        DoctorDO doctor = doctorMapper.selectByUserId(userId);
        if (doctor == null) {
            return;
        }
        if (!Objects.equals(doctor.getId(), id)) {
            throw exception(DOCTOR_USER_ID_DUPLICATE);
        }
    }

    private void normalizeDoctor(DoctorDO doctor) {
        if (doctor.getUserId() == null) {
            doctor.setUserId(0L);
        }
        if (doctor.getGender() == null) {
            doctor.setGender(0);
        }
        if (doctor.getSort() == null) {
            doctor.setSort(0);
        }
        if (doctor.getStatus() == null) {
            doctor.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
    }

}
