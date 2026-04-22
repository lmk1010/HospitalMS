package cn.iocoder.yudao.module.hospital.dal.mysql.department;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.department.vo.DepartmentListReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.department.DepartmentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapperX<DepartmentDO> {

    default List<DepartmentDO> selectList(DepartmentListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<DepartmentDO>()
                .likeIfPresent(DepartmentDO::getName, reqVO.getName())
                .likeIfPresent(DepartmentDO::getCode, reqVO.getCode())
                .eqIfPresent(DepartmentDO::getType, reqVO.getType())
                .eqIfPresent(DepartmentDO::getStatus, reqVO.getStatus())
                .eqIfPresent(DepartmentDO::getParentId, reqVO.getParentId())
                .orderByAsc(DepartmentDO::getSort)
                .orderByDesc(DepartmentDO::getId));
    }

    default List<DepartmentDO> selectSimpleList() {
        return selectList(new LambdaQueryWrapperX<DepartmentDO>()
                .eq(DepartmentDO::getStatus, 0)
                .orderByAsc(DepartmentDO::getSort)
                .orderByDesc(DepartmentDO::getId));
    }

    default DepartmentDO selectByName(String name) {
        return selectOne(DepartmentDO::getName, name);
    }

    default DepartmentDO selectByCode(String code) {
        return selectOne(DepartmentDO::getCode, code);
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(DepartmentDO::getParentId, parentId);
    }

}
