<template>
  <div class="page sg-page">
    <section class="sg-hero">
      <div class="hero-eyebrow">Interview Field Notes · Design System v1.0</div>
      <h1>把面试准备，<em>写成一本手记</em></h1>
      <p>
        一套为长时间阅读与每日复习而生的界面语言：暖纸、墨色、朱砂批注，
        衬线标题负责记忆点，无衬线正文负责效率。它不追求“更像一个后台”，
        而是让每一次记录都像在纸上落下一笔。
      </p>
      <div class="sg-hero-meta">
        <span>Direction · Field Notes</span>
        <span>Type · Serif × Sans × Mono</span>
        <span>Motion · 180ms ease-out</span>
      </div>
      <div class="sg-strip">
        <i style="background: #f2ecdf" />
        <i style="background: #d9cdb7" />
        <i style="background: #8a7f6c" />
        <i style="background: #201d16" />
        <i style="background: #b8432a" />
        <i style="background: #3f5b45" />
        <i style="background: #a2762a" />
        <i style="background: #2e5a74" />
      </div>
    </section>

    <section class="card sg-section">
      <div class="sg-section-head">
        <span class="num">01</span>
        <h2>设计原则</h2>
        <p>先有观点，再有组件</p>
      </div>
      <div class="sg-grid-2">
        <div>
          <h3 class="sg-h3">像纸，而不是像屏幕</h3>
          <p class="sg-small">
            暖白纸色、极轻的横向纹理、1px 装订线。信息靠字号、字重和留白分层，
            而不是靠一层层阴影和胶囊。
          </p>
        </div>
        <div>
          <h3 class="sg-h3">朱砂只做批注</h3>
          <p class="sg-small">
            主色只出现在需要行动或需要被记住的位置：主按钮、今日待复习、错误与危险操作。
            其余颜色退到颜料盘里，安静地做分类。
          </p>
        </div>
        <div>
          <h3 class="sg-h3">可扫读，也可久读</h3>
          <p class="sg-small">
            列表用无衬线、短行高密度；详情与复盘用衬线大标题和 1.8 行高。
            扫的时候快，读的时候不累。
          </p>
        </div>
        <div>
          <h3 class="sg-h3">动效是标点</h3>
          <p class="sg-small">
            一次页面入场、一次卡片抬起、一次状态确认，足够。所有移动只使用
            transform 与 opacity，并尊重 reduced-motion。
          </p>
        </div>
      </div>
    </section>

    <section class="card sg-section">
      <div class="sg-section-head">
        <span class="num">02</span>
        <h2>色板</h2>
        <p>Light / Paper · Dark / Lamp</p>
      </div>

      <h4 class="sg-sub">浅色 · 日间纸面</h4>
      <div class="sg-swatches">
        <div v-for="c in lightColors" :key="c.token" class="sg-swatch">
          <div class="chip" :style="{ background: c.hex }" />
          <div class="meta">
            <b>{{ c.name }}</b>
            <code>{{ c.token }} · {{ c.hex }}</code>
          </div>
        </div>
      </div>

      <h4 class="sg-sub">深色 · 夜读灯下</h4>
      <div class="sg-dark-panel">
        <div class="sg-swatches">
          <div v-for="c in darkColors" :key="c.token" class="sg-swatch dark">
            <div class="chip" :style="{ background: c.hex }" />
            <div class="meta">
              <b>{{ c.name }}</b>
              <code>{{ c.token }} · {{ c.hex }}</code>
            </div>
          </div>
        </div>
      </div>

      <h4 class="sg-sub">语义色</h4>
      <div class="sg-row">
        <span class="mini-pill" style="color: var(--accent-2); background: color-mix(in srgb, var(--accent-2) 12%, transparent)">已掌握</span>
        <span class="mini-pill" style="color: var(--accent-4); background: color-mix(in srgb, var(--accent-4) 12%, transparent)">复习中</span>
        <span class="mini-pill" style="color: var(--text-3); background: color-mix(in srgb, var(--text-3) 12%, transparent)">未掌握</span>
        <span class="mini-pill" style="color: var(--accent-3); background: color-mix(in srgb, var(--accent-3) 13%, transparent)">中等</span>
        <span class="mini-pill" style="color: var(--primary); background: color-mix(in srgb, var(--primary) 12%, transparent)">困难</span>
      </div>
      <p class="sg-footnote" style="margin-top: 12px">
        语义色全部使用 CSS 变量与 color-mix，深浅主题切换时无需改组件。
        数据分类色使用低饱和矿物颜料，并自动与当前文字色混合，保证两种主题下的可读性。
      </p>
    </section>

    <section class="card sg-section">
      <div class="sg-section-head">
        <span class="num">03</span>
        <h2>字体</h2>
        <p>Display / Body / Mono</p>
      </div>
      <div class="sg-type">
        <div class="sg-type-row">
          <span class="tag">DISPLAY · 44</span>
          <div class="demo"><div class="sg-display">把答案写下来</div></div>
        </div>
        <div class="sg-type-row">
          <span class="tag">H1 · 30</span>
          <div class="demo"><div class="sg-h1">今天有 11 道题等待复习</div></div>
        </div>
        <div class="sg-type-row">
          <span class="tag">H2 · 22</span>
          <div class="demo"><div class="sg-h2">复习热力图</div></div>
        </div>
        <div class="sg-type-row">
          <span class="tag">H3 · 17</span>
          <div class="demo"><div class="sg-h3">最近添加</div></div>
        </div>
        <div class="sg-type-row">
          <span class="tag">BODY · 14</span>
          <div class="demo">
            <p class="sg-body">
              按遗忘曲线过一遍，比重读十遍更有效。每道题都保留自己的复习轨迹，
              答不上来也没关系，下一次会更牢。
            </p>
          </div>
        </div>
        <div class="sg-type-row">
          <span class="tag">SMALL · 12</span>
          <div class="demo"><p class="sg-small">辅助说明、时间、来源与标签使用 12px / 1.7。</p></div>
        </div>
        <div class="sg-type-row">
          <span class="tag">MONO · 10</span>
          <div class="demo"><span class="sg-mono">FIELD NOTES · 2026.10.28 · 11 DUE</span></div>
        </div>
      </div>
      <p class="sg-footnote" style="margin-top: 14px">
        显示字体：Georgia + 宋体 / Songti SC；正文字体：Segoe UI + 微软雅黑；
        等宽字体：Consolas。全部使用系统字体，离线可用，不依赖网络字体。
      </p>
    </section>

    <section class="card sg-section">
      <div class="sg-section-head">
        <span class="num">04</span>
        <h2>空间、圆角与阴影</h2>
        <p>8px 基准 · 纸片层叠</p>
      </div>
      <div class="sg-grid-2">
        <div>
          <h4 class="sg-sub">间距阶梯</h4>
          <div class="sg-row" style="align-items: flex-end; gap: 12px">
            <div v-for="s in spaces" :key="s.token" style="text-align: center">
              <div :style="{ width: s.px + 'px', height: s.px + 'px', background: 'var(--primary)', opacity: 0.85, borderRadius: '1px' }" />
              <div class="sg-mono" style="margin-top: 6px">{{ s.token }}</div>
            </div>
          </div>
        </div>
        <div>
          <h4 class="sg-sub">圆角与阴影</h4>
          <div class="sg-row">
            <div class="sg-radius" style="border-radius: 4px">4</div>
            <div class="sg-radius" style="border-radius: 6px">6</div>
            <div class="sg-radius" style="border-radius: 8px">8</div>
            <div class="sg-radius" style="border-radius: 12px">12</div>
            <div class="sg-radius" style="border-radius: 999px">pill</div>
          </div>
          <div class="sg-row" style="margin-top: 14px">
            <div class="sg-shadow">shadow</div>
            <div class="sg-shadow hard">shadow-hard</div>
          </div>
        </div>
      </div>
      <div class="sg-table-wrap">
        <table class="sg-table" style="margin-top: 16px">
          <thead>
            <tr><th>Token</th><th>Value</th><th>用途</th></tr>
          </thead>
          <tbody>
            <tr><td><code>--radius-sm</code></td><td>6px</td><td>标签、输入框、徽章</td></tr>
            <tr><td><code>--radius</code></td><td>8px</td><td>卡片、题目、面试记录</td></tr>
            <tr><td><code>--radius-lg</code></td><td>12px</td><td>Hero、弹窗、设计规范面板</td></tr>
            <tr><td><code>--shadow-hard</code></td><td>3px 3px 0 alpha 6%</td><td>纸片层叠、悬停抬起</td></tr>
            <tr><td><code>--shadow</code></td><td>柔和环境阴影</td><td>浮层、粘性侧栏</td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="card sg-section">
      <div class="sg-section-head">
        <span class="num">05</span>
        <h2>组件</h2>
        <p>按钮、表单、标签、卡片</p>
      </div>

      <h4 class="sg-sub">按钮</h4>
      <div class="sg-demo sg-row">
        <el-button type="primary">保存题目</el-button>
        <el-button>取消</el-button>
        <el-button type="primary" plain>次要操作</el-button>
        <el-button type="danger">删除</el-button>
        <el-button text>文字按钮</el-button>
        <el-button type="primary" disabled>禁用</el-button>
      </div>

      <h4 class="sg-sub">表单</h4>
      <div class="sg-demo">
        <div class="form-grid">
          <label class="field">
            <span>题目</span>
            <el-input model-value="HashMap 的底层结构与扩容机制？" />
          </label>
          <label class="field">
            <span>分类</span>
            <el-input model-value="Java 基础" />
          </label>
          <label class="field">
            <span>难度</span>
            <el-input model-value="中等" />
          </label>
          <label class="field field-wide">
            <span>补充说明</span>
            <el-input type="textarea" :rows="2" model-value="用一句话写清楚考点，之后复习会更快。" />
          </label>
        </div>
      </div>

      <h4 class="sg-sub">标签与状态</h4>
      <div class="sg-demo">
        <div class="sg-row">
          <span class="cat-pill" style="color: var(--primary); background: color-mix(in srgb, var(--primary) 12%, transparent)">
            <i class="dot" style="background: var(--primary)" />Java 基础
          </span>
          <span class="mini-pill" style="color: var(--accent-3); background: color-mix(in srgb, var(--accent-3) 13%, transparent)">中等</span>
          <span class="mini-pill" style="color: var(--accent-2); background: color-mix(in srgb, var(--accent-2) 12%, transparent)">已掌握</span>
          <span class="tag-chip">#HashMap</span>
          <span class="tag-chip">#扩容</span>
          <span class="star-flag">重点</span>
          <span class="due-flag"><i />今日到期</span>
        </div>
      </div>

      <h4 class="sg-sub">题目卡片</h4>
      <div class="sg-demo" style="padding: 16px">
        <div class="q-card" style="cursor: default">
          <div class="q-card-top">
            <span class="cat-pill" style="color: var(--primary); background: color-mix(in srgb, var(--primary) 12%, transparent)">
              <i class="dot" style="background: var(--primary)" />Java 基础
            </span>
            <div class="q-card-actions" style="opacity: 1">
              <button class="star-btn on" type="button" aria-label="收藏">★</button>
            </div>
          </div>
          <div class="q-title">HashMap 的底层结构与扩容机制？</div>
          <div class="q-excerpt">数组 + 链表 + 红黑树，负载因子 0.75，扩容时 rehash 的过程与并发问题。</div>
          <div class="q-tags"><span class="tag-chip">#HashMap</span><span class="tag-chip">#扩容</span></div>
          <div class="q-footer">
            <span class="q-meta">3 天前 · 复习 4 次</span>
            <span class="q-company">字节跳动</span>
          </div>
        </div>
      </div>

      <h4 class="sg-sub">数据表</h4>
      <div class="sg-table-wrap">
        <table class="sg-table">
          <thead>
            <tr><th>题目</th><th>分类</th><th>难度</th><th>下次复习</th></tr>
          </thead>
          <tbody>
            <tr><td>HashMap 的底层结构与扩容机制？</td><td>Java 基础</td><td><span class="mini-pill" style="color: var(--accent-3)">中等</span></td><td>明天</td></tr>
            <tr><td>Spring Bean 的生命周期？</td><td>Spring</td><td><span class="mini-pill" style="color: var(--accent-2)">简单</span></td><td>3 天后</td></tr>
            <tr><td>MySQL 索引为什么用 B+ 树？</td><td>数据库</td><td><span class="mini-pill" style="color: var(--primary)">困难</span></td><td>今天</td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="card sg-section">
      <div class="sg-section-head">
        <span class="num">06</span>
        <h2>动效</h2>
        <p>目的先于时长</p>
      </div>
      <div class="sg-grid-2">
        <div>
          <h4 class="sg-sub">编排示例</h4>
          <div class="sg-demo">
            <div class="sg-motion" aria-hidden="true">
              <i /><i /><i /><i /><i /><i />
            </div>
            <p class="sg-footnote" style="margin-top: 10px">悬停整组，子项按 35ms 递进抬起。</p>
          </div>
        </div>
        <div>
          <h4 class="sg-sub">规范</h4>
          <div class="sg-table-wrap">
            <table class="sg-table">
              <tbody>
                <tr><td>悬停 / 按下</td><td><code>120ms</code> · linear</td></tr>
                <tr><td>卡片抬起</td><td><code>180ms</code> · ease-out</td></tr>
                <tr><td>页面入场</td><td><code>260ms</code> · ease-out</td></tr>
                <tr><td>退场</td><td>比入场快 20–30%</td></tr>
                <tr><td>只动</td><td><code>transform / opacity</code></td></tr>
                <tr><td>降级</td><td><code>prefers-reduced-motion</code></td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>

    <section class="card sg-section">
      <div class="sg-section-head">
        <span class="num">07</span>
        <h2>可访问性</h2>
        <p>不是附加项</p>
      </div>
      <div class="sg-grid-2">
        <div>
          <ul class="sg-list">
            <li>正文对比度 ≥ 4.5:1，大字号 ≥ 3:1。</li>
            <li>所有交互元素保留可见的 focus-visible 轮廓。</li>
            <li>颜色不是唯一的信息载体：状态同时有文字。</li>
            <li>深色模式不是简单反色，而是重新调过的暖色夜读主题。</li>
            <li>动效可被系统设置关闭，不阻塞输入与状态更新。</li>
          </ul>
        </div>
        <div>
          <blockquote class="sg-quote">
            “好的界面不是让人赞叹，而是让人第二天还想打开。”
          </blockquote>
          <p class="sg-footnote" style="margin-top: 10px">
            设计语言参考 Anthropic <code>frontend-design</code> 与本地 <code>ui-motion</code> 技能：
            明确的观点、系统字体、纸感层次、克制的朱砂与有目的的动效。
          </p>
        </div>
      </div>
    </section>

    <div class="sg-footer">
      <span>Interview Field Notes · 2026</span>
      <button class="sg-theme-btn" type="button" @click="toggleTheme">
        {{ isDark ? '切换到浅色纸面' : '切换到夜读模式' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { appState, toggleTheme } from '@/store/app'

const isDark = computed(() => appState.theme === 'dark')

const lightColors = [
  { name: '纸 Paper', token: '--paper', hex: '#F2ECDF' },
  { name: '纸面 Surface', token: '--surface', hex: '#FBF6EA' },
  { name: '装订线 Border', token: '--border', hex: '#D9CDB7' },
  { name: '墨 Ink', token: '--text', hex: '#201D16' },
  { name: '次级墨 Text 2', token: '--text-2', hex: '#5B5346' },
  { name: '淡墨 Text 3', token: '--text-3', hex: '#756A58' },
  { name: '朱砂 Primary', token: '--primary', hex: '#B8432A' },
  { name: '苔绿 Moss', token: '--accent-2', hex: '#3F5B45' },
  { name: '赭石 Ochre', token: '--accent-3', hex: '#8A6420' },
  { name: '石青 Petrol', token: '--accent-4', hex: '#2E5A74' }
]

const darkColors = [
  { name: '夜色 Paper', token: '--paper', hex: '#131110' },
  { name: '灯下 Surface', token: '--surface', hex: '#1A1714' },
  { name: '装订线 Border', token: '--border', hex: '#322C24' },
  { name: '暖白 Ink', token: '--text', hex: '#EDE4D3' },
  { name: '次级 Text 2', token: '--text-2', hex: '#B5A992' },
  { name: '淡墨 Text 3', token: '--text-3', hex: '#9A8C76' },
  { name: '朱砂 Primary', token: '--primary', hex: '#E07654' },
  { name: '苔绿 Moss', token: '--accent-2', hex: '#83A98C' },
  { name: '赭石 Ochre', token: '--accent-3', hex: '#D2A75C' },
  { name: '石青 Petrol', token: '--accent-4', hex: '#7FA9C3' }
]

const spaces = [
  { token: '4', px: 4 },
  { token: '8', px: 8 },
  { token: '12', px: 12 },
  { token: '16', px: 16 },
  { token: '24', px: 24 },
  { token: '32', px: 32 },
  { token: '48', px: 48 }
]
</script>

<style scoped>
.sg-list {
  margin: 0;
  padding-left: 1.1em;
  font-size: 12px;
  line-height: 1.9;
  color: var(--text-2);
}

.sg-list li::marker {
  color: var(--primary);
}

.sg-radius {
  width: 52px;
  height: 52px;
  display: grid;
  place-items: center;
  border: 1px solid var(--border-strong);
  background: var(--surface-2);
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-3);
}

.sg-shadow {
  width: 112px;
  height: 56px;
  display: grid;
  place-items: center;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--surface);
  box-shadow: var(--shadow);
  font-family: var(--font-mono);
  font-size: 9.5px;
  color: var(--text-3);
}

.sg-shadow.hard {
  box-shadow: var(--shadow-hard);
}

.sg-dark-panel {
  background: #131110;
  border: 1px solid #322c24;
  border-radius: 8px;
  padding: 16px;
}

.sg-swatch.dark {
  background: #1a1714;
  border-color: #322c24;
  color: #ede4d3;
}

.sg-swatch.dark .meta {
  border-top: 1px solid #322c24;
}

.sg-swatch.dark .meta code {
  color: #857966;
}

.sg-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 22px;
  padding: 14px 2px 0;
  border-top: 1px dashed var(--border);
  font-family: var(--font-mono);
  font-size: 9px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--text-3);
}

.sg-theme-btn {
  border: 1px solid var(--border-strong);
  background: transparent;
  color: var(--text-2);
  border-radius: 5px;
  padding: 6px 12px;
  font-family: var(--font-mono);
  font-size: 9px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  transition: border-color var(--dur-fast) linear, color var(--dur-fast) linear,
    background-color var(--dur-fast) linear;
}

.sg-theme-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: color-mix(in srgb, var(--primary) 7%, transparent);
}

@media (max-width: 640px) {
  .sg-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
