"""Combine the authored walkthrough and local controls into the offline guide."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
full = (root / 'docs/full-walkthrough.md').read_text(encoding='utf-8')
short = (root / 'docs/guide-content.md').read_text(encoding='utf-8')
controls = short[short.index('## 触屏操作'):short.index('## 来源与核对边界')]
intro = '# 剑侠情缘二：离线攻略与操作\n\n左上角工具图标可打开攻略或修改器。以下主线流程包含后半程被囚、复仇及天忍教结局；不保证支线全收集，且未在本地移植版逐步通关核验。\n\n'
sources = '\n\n## 在线原始资料\n\nhttps://github.com/lxb007981/JianXiaQingYuan-Walkthrough/tree/master/docs/Walkthrough\nhttps://github.com/lxb007981/JianXiaQingYuan-Walkthrough/blob/master/docs/Copyright/README.md\n'
out = root / 'build-local/assets/engine/guide/guide.txt'
out.write_text(intro + controls + '\n\n' + full + sources, encoding='utf-8')
print('Offline guide characters:', len(out.read_text(encoding='utf-8')))
