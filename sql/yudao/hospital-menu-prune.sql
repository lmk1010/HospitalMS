-- HospitalMS 菜单裁剪：隐藏开源演示与医院无关模块
USE hospital_ms;

UPDATE system_menu
SET visible = b'0', updater = 'admin', update_time = NOW()
WHERE id IN (
  1070, 1117, 1185, 1254, 1281, 2084, 2159, 2160, 2161, 2262, 2362, 2397,
  2472, 2478, 2484, 2490, 2497, 2549, 2550, 2563, 2758, 4000, 5100
);
