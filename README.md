# 剑侠情缘二 Android 本地改造

基于 [Upwinded/JXQY-all-in-one](https://github.com/Upwinded/JXQY-all-in-one) 的 GPL-3.0 引擎开发，非官方手游。当前本地版本 `1.2.1-local4`，Android 9+，ARM64/x86_64。

## 功能
- 手机横屏触控、摇杆、五技能拖动瞄准及按钮标签。
- 左上角单工具图标，弹出攻略与修改器入口。
- 离线攻略章节、搜索、字号设置；主线流程整理至天忍教结局，未逐步通关验证。
- 游戏线程处理修改器请求，复用原有暂停与作弊面板。
- 用户提供图片的 Android 各密度启动图标适配。

## 目录
- `upstream/`：上游引擎与本地修改后的完整源代码；Lua/minilzo/miniz已展开随附，保留各自许可。
- `tools/`：本地资源准备、构建、验证脚本。
- `docs/项目详情与交付说明.md`：详细架构、构建、测试边界与交付说明。

## 构建
需 Python 3.11+、JDK21、Android SDK36、NDK29.0.13113456、CMake3.31.6和上游预编译Android依赖。工具链路径在 `tools/build_local_android.py` 中配置；脚本默认指向原构建机，请先调整。

本仓库**不含原版游戏素材、预编译平台依赖、完整APK、签名私钥或口令**。自行合法准备资源后，按详情文档执行资源准备与 `assembleFullyRelease`。仅克隆本仓库不能直接构建完整素材版游戏。新生成签名不能覆盖已发行的其他签名应用。

```text
python -I tools/stage_local_resources.py
python -I tools/assemble_offline_guide.py
python -I tools/build_local_android.py
python -I tools/verify_integrated_apk.py <APK路径>
```

## 来源与许可
上游基线：`addd0add52209477850b847c5126f439c4ea55c4`（1.2.1）。本仓库是该基线与本地修改的源码快照，不保留上游Git历史；源文件及GPL许可保留。第三方来源与锁定提交见 `SOURCE_PROVENANCE.md`。

引擎开源不意味着原版美术、音乐、剧情及商标获得再发布许可。含原版资源的集成APK仅作本地交付，不在GitHub公开发布。

已通过构建、签名、资源完整性检查和模拟器安装/标题启动验证；未完成全流程通关、全机型或完整GUI交互验收。详见项目文档。
