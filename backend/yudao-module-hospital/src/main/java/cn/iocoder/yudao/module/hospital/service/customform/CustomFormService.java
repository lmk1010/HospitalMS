package cn.iocoder.yudao.module.hospital.service.customform;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.customform.vo.CustomFormPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.customform.vo.CustomFormSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.customform.CustomFormDO;

import java.util.List;

public interface CustomFormService {

    Long createCustomForm(CustomFormSaveReqVO createReqVO);

    void updateCustomForm(CustomFormSaveReqVO updateReqVO);

    void deleteCustomForm(Long id);

    CustomFormDO getCustomForm(Long id);

    PageResult<CustomFormDO> getCustomFormPage(CustomFormPageReqVO reqVO);

    List<CustomFormDO> getCustomFormSimpleList();

    CustomFormDO getCustomFormByPage(String pageCode, String processKey, String nodeKey);

    void validateCustomFormExists(Long id);

}
