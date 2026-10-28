import { marked } from 'marked'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js/lib/common'

const renderer = new marked.Renderer()

renderer.code = function ({ text, lang }) {
  const language = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
  let highlighted
  try {
    highlighted = hljs.highlight(text, { language, ignoreIllegals: true }).value
  } catch {
    highlighted = text.replace(/[<>&]/g, (c) => ({ '<': '&lt;', '>': '&gt;', '&': '&amp;' })[c])
  }
  return `<pre class="hljs"><div class="code-lang">${language}</div><code class="language-${language}">${highlighted}</code></pre>`
}

renderer.link = function ({ href, title, text }) {
  const isExternal = /^https?:/i.test(href || '')
  const attrs = isExternal ? ' target="_blank" rel="noopener noreferrer"' : ''
  const titleAttr = title ? ` title="${title}"` : ''
  return `<a href="${href}"${titleAttr}${attrs}>${text}</a>`
}

marked.setOptions({
  renderer,
  gfm: true,
  breaks: true
})

export function renderMarkdown(source) {
  if (!source) return ''
  const html = marked.parse(source)
  return DOMPurify.sanitize(html, {
    ADD_ATTR: ['target', 'rel'],
    ADD_TAGS: ['iframe']
  })
}

export function plainText(source) {
  if (!source) return ''
  return source
    .replace(/```[\s\S]*?```/g, ' [代码] ')
    .replace(/[#>*`[\]()!|_-]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}
