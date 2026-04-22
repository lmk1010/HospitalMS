package cn.iocoder.yudao.module.hospital.service.customform;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.customform.vo.CustomFormPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.customform.vo.CustomFormSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.customform.CustomFormDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.customform.CustomFormMapper;
import cn.iocoder.yudao.module.hospital.service.department.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CUSTOM_FORM_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CUSTOM_FORM_NOT_EXISTS;

@Service
@Validated
public class CustomFormServiceImpl implements CustomFormService {

    @Resource
    private CustomFormMapper customFormMapper;
    @Resource
    private DepartmentService departmentService;

    @Override
    public Long createCustomForm(CustomFormSaveReqVO createReqVO) {
        validateCustomFormForCreateOrUpdate(null, createReqVO);
        CustomFormDO customForm = BeanUtils.toBean(createReqVO, CustomFormDO.class);
        normalizeCustomForm(customForm);
        customFormMapper.insert(customForm);
        return customForm.getId();
    }

    @Override
    public void updateCustomForm(CustomFormSaveReqVO updateReqVO) {
        validateCustomFormForCreateOrUpdate(updateReqVO.getId(), updateReqVO);
        CustomFormDO customForm = BeanUtils.toBean(updateReqVO, CustomFormDO.class);
        normalizeCustomForm(customForm);
        customFormMapper.updateById(customForm);
    }

    @Override
    public void deleteCustomForm(Long id) {
        validateCustomFormExists(id);
        customFormMapper.deleteById(id);
    }

    @Override
    public CustomFormDO getCustomForm(Long id) {
        return customFormMapper.selectById(id);
    }

    @Override
    public PageResult<CustomFormDO> getCustomFormPage(CustomFormPageReqVO reqVO) {
        return customFormMapper.selectPage(reqVO);
    }

    @Override
    public List<CustomFormDO> getCustomFormSimpleList() {
        return customFormMapper.selectSimpleList();
    }

    @Override
    public CustomFormDO getCustomFormByPage(String pageCode, String processKey, String nodeKey) {
        List<CustomFormDO> forms = customFormMapper.selectListByPage(pageCode, processKey, nodeKey);
        if (CollUtil.isEmpty(forms) && (StrUtil.isNotBlank(processKey) || StrUtil.isNotBlank(nodeKey))) {
            forms = customFormMapper.selectListByPage(pageCode, null, null);
        }
        return CollUtil.getFirst(forms);
    }

    @Override
    public void validateCustomFormExists(Long id) {
        if (id == null || id <= 0) {
            return;
        }
        if (customFormMapper.selectById(id) == null) {
            throw exception(CUSTOM_FORM_NOT_EXISTS);
        }
    }

    private void validateCustomFormForCreateOrUpdate(Long id, CustomFormSaveReqVO reqVO) {
        if (id != null) {
            validateCustomFormExists(id);
        }
        if (reqVO.getDeptId() != null && reqVO.getDeptId() > 0) {
            departmentService.validateDepartmentExists(reqVO.getDeptId());
        }
        validateCustomFormCodeUnique(id, reqVO.getCode());
    }

    private void validateCustomFormCodeUnique(Long id, String code) {
        CustomFormDO customForm = customFormMapper.selectByCode(code);
        if (customForm == null) {
            return;
        }
        if (!Objects.equals(customForm.getId(), id)) {
            throw exception(CUSTOM_FORM_CODE_DUPLICATE);
        }
    }

    private void normalizeCustomForm(CustomFormDO customForm) {
        if (customForm.getStatus() == null) {
            customForm.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (customForm.getFields() == null) {
            customForm.setFields(Collections.emptyList());
        }
        if (StrUtil.isBlank(customForm.getPageCode())) {
            customForm.setPageCode(null);
        }
        if (StrUtil.isBlank(customForm.getPagePath())) {
            customForm.setPagePath(null);
        }
        if (StrUtil.isBlank(customForm.getProcessKey())) {
            customForm.setProcessKey(null);
        }
        if (StrUtil.isBlank(customForm.getProcessName())) {
            customForm.setProcessName(null);
        }
        if (StrUtil.isBlank(customForm.getNodeKey())) {
            customForm.setNodeKey(null);
        }
        if (StrUtil.isBlank(customForm.getNodeName())) {
            customForm.setNodeName(null);
        }
        if (StrUtil.isBlank(customForm.getBizCategory())) {
            customForm.setBizCategory(null);
        }
    }

}
