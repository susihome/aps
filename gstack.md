Let我先深入了解 gstack 的项目结构和各个 skill 文件的内容。



让我再读几个核心 skill 文件，以便给你更完整的流程指南。

让我再看几个关键的 skill 说明。
现在我对 gstack 有了全面的了解。下面是完整的使用指南：

---

# gstack 使用指南：从商业调研到产品上线

## 一句话理解 gstack

**gstack 把 AI 编程助手（主要是 Claude Code）变成一支虚拟工程团队。** 它用 Markdown 斜杠命令（[/office-hours](cci:9://file:///d:/claudework/gstack/office-hours:0:0-0:0)、[/review](cci:9://file:///d:/claudework/gstack/review:0:0-0:0)、[/ship](cci:9://file:///d:/claudework/gstack/ship:0:0-0:0) 等）定义了 23 个"专家角色"，模拟了一家创业公司从想法到上线的完整 sprint 流程。

你不再是面对一个万能但模糊的 AI，而是对着**CEO、工程经理、设计师、QA、安全官、发布工程师**各说各话。

---

## 核心流程：Think → Plan → Build → Review → Test → Ship → Reflect

每个阶段的输出会自动喂给下一个阶段。这是关键——不是一堆独立工具，是一个**流水线**。

---

## 第一步：安装（30 秒）

前提条件：安装好 [Claude Code](https://docs.anthropic.com/en/docs/claude-code)、Git、Bun、Node.js (Windows 必须)。

在 Claude Code 里运行：
```bash
git clone --single-branch --depth 1 https://github.com/garrytan/gstack.git ~/.claude/skills/gstack
cd ~/.claude/skills/gstack && ./setup
```

---

## 第二步：商业调研阶段 — [/office-hours](cci:9://file:///d:/claudework/gstack/office-hours:0:0-0:0)

> 🎯 **角色：YC 合伙人**
> **何时用：你有一个产品想法，还没写代码**

你告诉它你想做什么，它会：
1. **用 6 个追问 拷问你的想法** — 需求是否真实？现有方案为什么不行？谁会急切需要这个？最窄的切入点是什么？
2. **挑战你的定义** — 比如你说"做一个日程简报 App"，它会说"你描述的其实是一个 AI 私人参谋长"
3. **生成 2-3 个实现方案** — 每个带工作量估算（人力 vs AI 辅助时间）
4. **输出设计文档** — 保存到 `~/.gstack/projects/`，后续所有步骤自动读取

**两种模式：**
- **创业模式（Startup）** — 严格拷问，像 YC demo day 前的 office hours
- **构建者模式（Builder）** — 轻松探索，适合黑客松、副业项目

---

## 第三步：产品战略审视 — [/plan-ceo-review](cci:9://file:///d:/claudework/gstack/plan-ceo-review:0:0-0:0)

> 🎯 **角色：CEO/创始人**
> **何时用：设计文档写好后，开始编码前**

它会读取 [/office-hours](cci:9://file:///d:/claudework/gstack/office-hours:0:0-0:0) 产生的设计文档，然后从产品视角挑战：
- **这个需求背后隐藏的"10 星产品"是什么？**
- 你是在做一个功能，还是在解决一个更大的问题？

**四种审视模式：**
| 模式 | 含义 |
|------|------|
| **SCOPE EXPANSION** | 大胆做梦，看看最有野心的版本 |
| **SELECTIVE EXPANSION** | 维持当前范围，逐个挑选值得加的扩展 |
| **HOLD SCOPE** | 不扩展，最大力度审视现有方案 |
| **SCOPE REDUCTION** | 砍到最小可行版本 |

> 💡 **快捷方式：** [/autoplan](cci:9://file:///d:/claudework/gstack/autoplan:0:0-0:0) = 一条命令自动跑完 CEO → 设计 → 工程审查，只在需要你拍板的"品味决策"上停下来问你。

---

## 第四步：技术方案锁定 — [/plan-eng-review](cci:9://file:///d:/claudework/gstack/plan-eng-review:0:0-0:0)

> 🎯 **角色：工程经理**
> **何时用：产品方向确认后，写代码前**

它会产出：
- **架构图**（ASCII 图）—— 数据流、状态机、组件边界
- **边界情况分析** — 如果上传成功但处理失败怎么办？
- **测试矩阵** — 哪些用例必须测试
- **安全问题** — 信任边界在哪里

关键价值：**强迫你把那些"拍脑袋"的假设变成可见的、可讨论的文字**。

---

## 第五步：设计审查（可选但推荐）

| 命令 | 角色 | 用途 |
|------|------|------|
| [/plan-design-review](cci:9://file:///d:/claudework/gstack/plan-design-review:0:0-0:0) | 高级设计师 | 在写代码前审查设计，每个维度 0-10 评分 |
| [/design-consultation](cci:9://file:///d:/claudework/gstack/design-consultation:0:0-0:0) | 设计合伙人 | 从零建立完整设计系统 |
| [/design-shotgun](cci:9://file:///d:/claudework/gstack/design-shotgun:0:0-0:0) | 设计探索者 | 生成 4-6 个 AI 设计稿变体，在浏览器中对比选择 |
| [/design-html](cci:9://file:///d:/claudework/gstack/design-html:0:0-0:0) | 设计工程师 | 把选定的设计稿变成可上线的 HTML/CSS |
| [/plan-devex-review](cci:9://file:///d:/claudework/gstack/plan-devex-review:0:0-0:0) | DX 负责人 | 如果你做的是 API/SDK/CLI，审查开发者体验 |

---

## 第六步：写代码

设计文档和技术方案都确认后，**告诉 Claude 开始实现**。它会读取前面所有阶段的输出，然后按照锁定的架构来编码。

典型场景：一个功能 → 11 个文件、2400 行代码、~8 分钟。

---

## 第七步：代码审查 — [/review](cci:9://file:///d:/claudework/gstack/review:0:0-0:0)

> 🎯 **角色：Staff 工程师**
> **何时用：代码写完后，提 PR 前**

它会分析你的 git diff，寻找：
- SQL 注入风险
- LLM 信任边界违规
- 竞态条件
- 生产环境会爆炸但 CI 能通过的 bug

**两种处理方式：**
- **AUTO-FIXED** — 明显的问题直接修了
- **ASK** — 需要你判断的问题（比如竞态条件），问你再修

> 💡 **额外武器：** [/codex](cci:9://file:///d:/claudework/gstack/codex:0:0-0:0) 可以让 OpenAI Codex 做独立的第二意见审查，跟 Claude 的 [/review](cci:9://file:///d:/claudework/gstack/review:0:0-0:0) 交叉对比。

---

## 第八步：QA 测试 — [/qa](cci:9://file:///d:/claudework/gstack/qa:0:0-0:0)

> 🎯 **角色：QA 负责人**
> **何时用：有可以访问的页面/URL 时**

```
/qa https://staging.myapp.com
```

它会：
1. **打开真正的 Chromium 浏览器**
2. **像真实用户一样点击、填表、导航**
3. **发现 bug → 修复 → 自动生成回归测试 → 验证修复**

> 如果只想要 bug 报告不要修代码，用 [/qa-only](cci:9://file:///d:/claudework/gstack/qa-only:0:0-0:0)。

---

## 第九步：安全审计（推荐） — [/cso](cci:9://file:///d:/claudework/gstack/cso:0:0-0:0)

> 🎯 **角色：首席安全官**

跑 OWASP Top 10 + STRIDE 威胁模型，每个发现都包含具体的攻击场景描述。

---

## 第十步：发布 — [/ship](cci:9://file:///d:/claudework/gstack/ship:0:0-0:0)

> 🎯 **角色：发布工程师**
> **何时用：代码已审查、测试通过**

一条命令完成：
1. 同步 main 分支
2. 跑测试
3. 审计测试覆盖率
4. 推送代码
5. 创建 PR
6. 自动更新文档（调用 [/document-release](cci:9://file:///d:/claudework/gstack/document-release:0:0-0:0)）

如果你的项目还没有测试框架，它甚至会帮你搭建一个。

---

## 第十一步：部署和监控

| 命令 | 作用 |
|------|------|
| [/land-and-deploy](cci:9://file:///d:/claudework/gstack/land-and-deploy:0:0-0:0) | 合并 PR → 等 CI → 部署 → 验证生产环境健康 |
| [/canary](cci:9://file:///d:/claudework/gstack/canary:0:0-0:0) | 部署后持续监控，检测控制台错误和性能回退 |
| [/benchmark](cci:9://file:///d:/claudework/gstack/benchmark:0:0-0:0) | 基线性能测试，对比每次 PR 前后的页面加载速度 |

---

## 第十二步：复盘 — [/retro](cci:9://file:///d:/claudework/gstack/retro:0:0-0:0)

> 🎯 **角色：工程经理**

跑每周回顾，输出：提交量、代码行数、测试健康趋势、每人贡献分析、发货连胜天数。`/retro global` 可以跨所有项目汇总。

---

## 完整流程图（一张图记住）

```
/office-hours          ← 想法调研，输出设计文档
       ↓
/plan-ceo-review       ← 产品战略审视（或 /autoplan 一键全审）
       ↓
/plan-eng-review       ← 技术方案锁定
       ↓
[写代码]               ← Claude 按照方案实现
       ↓
/review                ← 代码审查，自动修 bug
       ↓
/qa https://...        ← 真实浏览器测试
       ↓
/cso                   ← 安全审计
       ↓
/ship                  ← 跑测试 + 推代码 + 建 PR
       ↓
/land-and-deploy       ← 合并 + 部署 + 验证
       ↓
/retro                 ← 复盘
```

---

## 进阶玩法

- **并行 sprint** — 同时跑 10-15 个 Claude Code 会话（用 [Conductor](https://conductor.build)），一个做 office-hours，一个做 review，一个做 QA...
- **安全护栏** — [/careful](cci:9://file:///d:/claudework/gstack/careful:0:0-0:0)（防误删）、[/freeze](cci:9://file:///d:/claudework/gstack/freeze:0:0-0:0)（锁定编辑范围）、[/guard](cci:9://file:///d:/claudework/gstack/guard:0:0-0:0)（两者合一）
- **跨 AI 协作** — [/pair-agent](cci:9://file:///d:/claudework/gstack/pair-agent:0:0-0:0) 让 Claude、Codex、OpenClaw 等不同 AI 共享一个浏览器
- **记忆系统** — [/learn](cci:9://file:///d:/claudework/gstack/learn:0:0-0:0) 管理 gstack 跨 session 学到的项目知识，越用越懂你的代码库
- **语音输入** — 直接说"run a security check"就能触发对应技能

---

## 最简上手（5 步搞定）

如果你只想最快体验，按这个顺序：

1. **安装 gstack**
2. **[/office-hours](cci:9://file:///d:/claudework/gstack/office-hours:0:0-0:0)** — 描述你要做什么
3. **[/plan-ceo-review](cci:9://file:///d:/claudework/gstack/plan-ceo-review:0:0-0:0)** — 让 AI 帮你想更大
4. **写代码 → [/review](cci:9://file:///d:/claudework/gstack/review:0:0-0:0)** — 审查代码
5. **[/ship](cci:9://file:///d:/claudework/gstack/ship:0:0-0:0)** — 发布

走完这 5 步你就知道 gstack 适不适合你了。