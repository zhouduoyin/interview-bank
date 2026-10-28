<template>
  <div class="page method-page">
    <section class="hero-card method-hero">
      <div class="hero-main">
        <div class="hero-eyebrow">记忆方法 · Memory Protocol</div>
        <h2>记住，不靠重复，靠<em>主动回想</em></h2>
        <p>
          这个助手只做一件事：把“面试时被问到、事后却想不起来”的瞬间，变成下一次可以调用的记忆。
          每一次打开，都沿着同一条闭环走。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/review')">开始记忆训练</el-button>
          <el-button size="large" class="hero-ghost" @click="$router.push('/questions/new')"> 记录一道题 </el-button>
        </div>
      </div>
      <div class="hero-side method-hero-side">
        <div class="method-count">4</div>
        <div class="method-count-label">步记忆闭环</div>
        <p class="method-flow">捕获 → 回想 → 评分 → 安排</p>
      </div>
    </section>

    <section class="method-steps" aria-label="四步记忆闭环">
      <article v-for="(step, index) in steps" :key="step.title" class="card method-step">
        <span class="method-step-index">0{{ index + 1 }}</span>
        <h3>{{ step.title }}</h3>
        <p>{{ step.body }}</p>
        <small>{{ step.hint }}</small>
      </article>
    </section>

    <div class="method-grid">
      <section class="card">
        <div class="card-head">
          <div>
            <h3>间隔回想怎么安排</h3>
            <p>每次评分都会改变下一次出现的时间，目标是“刚好快忘记时再见一次”。</p>
          </div>
        </div>
        <div class="rating-table">
          <div v-for="item in ratings" :key="item.label" class="rating-row">
            <span class="rating-chip" :style="{ color: item.color, background: item.bg }">{{ item.label }}</span>
            <span>{{ item.meaning }}</span>
            <b>{{ item.interval }}</b>
          </div>
        </div>
        <p class="method-footnote">
          连续答对、间隔达到 7 天或累计回想 3 次后，题目会进入“长期记忆”；一旦选择“忘记”，会回到“加固中”并缩短间隔。
        </p>
      </section>

      <section class="card">
        <div class="card-head">
          <div>
            <h3>一条能坚持的日常节奏</h3>
            <p>不追求一次背完，追求每天都能接上。</p>
          </div>
        </div>
        <ol class="method-checklist">
          <li>
            <b>面试当天</b>
            <span>趁热记下 3–5 道题，只写问题也可以，先留住线索。</span>
          </li>
          <li>
            <b>第二天</b>
            <span>先不看答案，用 30 秒口述一遍，再核对并评分。</span>
          </li>
          <li>
            <b>每周一次</b>
            <span>打开成长洞察，看薄弱分类和连续回想天数，不盲目刷新题。</span>
          </li>
          <li>
            <b>每场面试后</b>
            <span>在面试档案里写复盘，把现场追问和题库里的题串起来。</span>
          </li>
        </ol>
      </section>
    </div>

    <section class="card method-note">
      <div>
        <h3>一个反直觉的提醒</h3>
        <p>
          重读答案会让你“看起来会了”，但真正的记忆发生在你合上答案、努力提取的那一刻。
          所以记忆训练默认先藏起答案；想不起来不是失败，而是下一次安排的依据。
        </p>
      </div>
      <router-link to="/review" class="text-link">去练一组 →</router-link>
    </section>
  </div>
</template>

<script setup>
import { RATING } from '@/utils/dict'

const steps = [
  {
    title: '捕获',
    body: '面试结束、通勤路上、刚被问住的那一刻，先把问题记下来，不要急着写完美答案。',
    hint: 'Ctrl + N 随时记题'
  },
  {
    title: '回想',
    body: '下一次打开时，先不看答案，用嘴或笔把思路完整过一遍，逼自己提取。',
    hint: '空格键核对答案'
  },
  {
    title: '评分',
    body: '对照答案后，诚实选择忘记 / 有点难 / 记得 / 很简单，系统据此调整间隔。',
    hint: '1–4 数字键快速评分'
  },
  {
    title: '安排',
    body: '题目按间隔回想自动排进下一次训练；你只需要每天处理“今天到期”的那一批。',
    hint: '明天再来，不突击'
  }
]

const ratings = [
  { ...RATING.FORGOT, meaning: '完全想不起来，或答错了核心点', interval: '1 天后' },
  { ...RATING.HARD, meaning: '想起来了，但很吃力、遗漏较多', interval: '约 1.2 倍间隔' },
  { ...RATING.GOOD, meaning: '能完整讲清楚，略有停顿', interval: '按熟练度递增' },
  { ...RATING.EASY, meaning: '脱口而出，还能延伸', interval: '约 1.3 倍间隔' }
]
</script>

<style scoped>
.method-hero {
  align-items: center;
}
.method-hero-side {
  text-align: right;
}
.method-count {
  font-family: var(--font-display);
  font-size: 76px;
  line-height: 0.9;
  letter-spacing: -0.04em;
}
.method-count-label {
  margin-top: 10px;
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 0.14em;
  color: rgba(239, 228, 207, 0.62);
}
.method-flow {
  margin: 14px 0 0;
  font-size: 12px;
  color: rgba(239, 228, 207, 0.72);
}

.method-steps {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}
.method-step {
  position: relative;
  min-height: 190px;
  padding-top: 22px;
}
.method-step-index {
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 0.14em;
  color: var(--primary);
}
.method-step h3 {
  margin: 16px 0 8px;
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 500;
}
.method-step p {
  min-height: 72px;
  color: var(--text-2);
  font-size: 12.5px;
  line-height: 1.75;
}
.method-step small {
  display: inline-block;
  margin-top: 12px;
  font-family: var(--font-mono);
  font-size: 9.5px;
  letter-spacing: 0.04em;
  color: var(--text-3);
}

.method-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 0.9fr);
  gap: 16px;
  margin-top: 16px;
}

.rating-table {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.rating-row {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  padding: 9px 10px;
  border: 1px solid var(--border);
  border-radius: 5px;
  background: var(--surface-2);
  font-size: 12.5px;
  color: var(--text-2);
}
.rating-chip {
  display: inline-flex;
  justify-content: center;
  padding: 4px 8px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 600;
}
.rating-row b {
  font-family: var(--font-mono);
  font-size: 10.5px;
  font-weight: 600;
  color: var(--text);
  white-space: nowrap;
}
.method-footnote {
  margin-top: 14px;
  color: var(--text-3);
  font-size: 11.5px;
  line-height: 1.75;
}

.method-checklist {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin: 0;
  padding: 0;
  list-style: none;
  counter-reset: method-step;
}
.method-checklist li {
  position: relative;
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 12px;
  padding-left: 26px;
  counter-increment: method-step;
}
.method-checklist li::before {
  content: counter(method-step, decimal-leading-zero);
  position: absolute;
  left: 0;
  top: 1px;
  font-family: var(--font-mono);
  font-size: 9px;
  color: var(--primary);
}
.method-checklist b {
  font-size: 12.5px;
}
.method-checklist span {
  font-size: 12px;
  line-height: 1.7;
  color: var(--text-3);
}

.method-note {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 28px;
  margin-top: 16px;
}
.method-note p {
  max-width: 820px;
  margin-top: 8px;
  font-size: 12.5px;
  line-height: 1.8;
  color: var(--text-2);
}

@media (max-width: 980px) {
  .method-steps {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .method-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
@media (max-width: 640px) {
  .method-steps {
    grid-template-columns: minmax(0, 1fr);
  }
  .method-step {
    min-height: 0;
  }
  .method-step p {
    min-height: 0;
  }
  .method-note {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }
  .rating-row {
    grid-template-columns: 68px minmax(0, 1fr);
  }
  .rating-row b {
    grid-column: 2;
  }
}
</style>
