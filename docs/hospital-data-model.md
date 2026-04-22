# 上海XX医院管理平台数据关系设计

## 1. 主线关系
- 一个患者 `hospital_patient` 可以关联多次定位、勾画、计划、治疗、费用记录
- 一个科室 `hospital_department` 下可以有多个医生 `hospital_doctor`
- 医生既是业务执行人，也是 BPM 节点候选人
- 所有审批型业务表都预留 `process_instance_id`，与 BPM 流程实例一一对应

## 2. 核心实体
### 2.1 主数据
- `hospital_department`：科室主数据
- `hospital_doctor`：医生主数据，关联 `dept_id`、`user_id`
- `hospital_patient`：患者建档主表

### 2.2 定位管理
- `hospital_ct_appointment`：CT 预约单
- `hospital_ct_queue`：CT 排队叫号记录

关系：
- 一个患者可有多条 CT 预约
- 一条 CT 预约可生成多条排队/叫号记录

### 2.3 勾画管理
- `hospital_contour_task`：靶区勾画任务
- `hospital_contour_review`：勾画审核记录

关系：
- 一条 CT 预约通常会触发一条勾画任务
- 一条勾画任务可对应多条审核记录（退回重审时保留历史）

### 2.4 计划管理
- `hospital_plan_apply`：计划申请单
- `hospital_plan_design`：计划设计稿
- `hospital_plan_review`：计划审核记录（组长审核 / 医师核准 / 计划复核）
- `hospital_plan_verify`：计划验证记录

关系：
- 一条勾画任务可发起一条或多条计划申请
- 一条计划申请可对应多个设计版本
- 一条设计稿在流转过程中产生多条审核记录
- 最终验证结果单独沉淀为验证记录

### 2.5 治疗管理
- `hospital_treatment_appointment`：治疗预约
- `hospital_treatment_queue`：治疗排队叫号
- `hospital_treatment_record`：单次治疗执行记录
- `hospital_treatment_summary`：疗程小结

关系：
- 一条通过验证的计划可生成多个治疗预约
- 每次预约可进入一次排队流程
- 每次治疗执行都形成执行记录
- 一个疗程结束后汇总生成治疗小结

### 2.6 费用管理
- `hospital_fee_record`：费用登记明细
- `hospital_fee_settlement`：费用结算单

关系：
- 患者维度下可有多条费用明细
- 多条费用明细可归集到一张结算单

## 3. BPM 关联方式
### 3.1 建议进入 BPM 的节点
- 勾画审核
- 计划申请
- 组长审核
- 医师核准
- 计划复核
- 计划验证

### 3.2 表字段约定
审批类业务表统一预留：
- `biz_no`：业务单号
- `workflow_status`：流程状态
- `process_instance_id`：流程实例编号
- `submit_time`：提交流程时间
- `approve_time`：审批完成时间

## 4. 页面与表关系
- 患者登记 -> `hospital_patient`
- 患者列表 -> `hospital_patient`
- 科室管理 -> `hospital_department`
- 医生管理 -> `hospital_doctor`
- CT预约 -> `hospital_ct_appointment`
- CT排队叫号 -> `hospital_ct_queue`
- 靶区勾画 -> `hospital_contour_task`
- 勾画审核 -> `hospital_contour_review`
- 计划申请 -> `hospital_plan_apply`
- 计划设计 -> `hospital_plan_design`
- 组长审核 / 医师核准 / 计划复核 -> `hospital_plan_review`
- 计划验证 -> `hospital_plan_verify`
- 治疗预约 -> `hospital_treatment_appointment`
- 排队叫号 -> `hospital_treatment_queue`
- 治疗执行 -> `hospital_treatment_record`
- 治疗小结 -> `hospital_treatment_summary`
- 费用登记 -> `hospital_fee_record`
- 费用结算 -> `hospital_fee_settlement`
- 费用查询 -> `hospital_fee_record` + `hospital_fee_settlement`

## 5. 当前落地策略
- 先把所有菜单、表结构、占位页一次建齐
- 再从 `科室 -> 医生 -> 患者` 开始生成 CRUD
- 之后逐步接 `CT -> 勾画 -> 计划 -> 治疗 -> 费用`
