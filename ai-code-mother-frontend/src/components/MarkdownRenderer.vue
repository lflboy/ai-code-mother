<template>
  <div class="markdown-content" v-html="renderedMarkdown"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

// 引入代码高亮样式
import 'highlight.js/styles/github.css'

interface Props {
  content: string
}

const props = defineProps<Props>()

// 配置 markdown-it 实例
const md: MarkdownIt = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str: string, lang: string): string {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return (
          '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
        )
      } catch {
        // 忽略错误，使用默认处理
      }
    }

    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  },
})

// 计算渲染后的 Markdown
const renderedMarkdown = computed(() => {
  return md.render(props.content)
})
</script>

<style scoped>
.markdown-content {
  line-height: 1.7;
  color: var(--text-primary);
  word-wrap: break-word;
  font-size: 15px;
}

/* 标题样式 */
.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin: 1.8em 0 0.8em 0;
  font-weight: 600;
  line-height: 1.3;
  color: var(--text-primary);
}

.markdown-content :deep(h1) {
  font-size: 1.6em;
  border-bottom: 2px solid var(--border-light);
  padding-bottom: 0.5em;
  margin-bottom: 1em;
}

.markdown-content :deep(h2) {
  font-size: 1.4em;
  border-bottom: 1px solid var(--border-light);
  padding-bottom: 0.4em;
  margin-bottom: 0.8em;
}

.markdown-content :deep(h3) {
  font-size: 1.2em;
  color: var(--primary-color);
}

.markdown-content :deep(h4) {
  font-size: 1.1em;
}

.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  font-size: 1em;
  color: var(--text-secondary);
}

/* 段落样式 */
.markdown-content :deep(p) {
  margin: 1em 0;
  line-height: 1.7;
}

/* 列表样式 */
.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 1em 0;
  padding-left: 1.8em;
}

.markdown-content :deep(li) {
  margin: 0.5em 0;
  line-height: 1.6;
}

.markdown-content :deep(ul li) {
  list-style-type: disc;
}

.markdown-content :deep(ol li) {
  list-style-type: decimal;
}

/* 引用样式 */
.markdown-content :deep(blockquote) {
  margin: 1.5em 0;
  padding: 1em 1.5em;
  border-left: 4px solid var(--primary-color);
  background: linear-gradient(90deg, var(--primary-light) 0%, transparent 100%);
  border-radius: 0 var(--border-radius-md) var(--border-radius-md) 0;
  color: var(--text-secondary);
  font-style: italic;
  position: relative;
}

.markdown-content :deep(blockquote::before) {
  content: '"';
  font-size: 3em;
  color: var(--primary-color);
  position: absolute;
  top: -0.2em;
  left: 0.3em;
  opacity: 0.3;
}

/* 行内代码样式 */
.markdown-content :deep(code) {
  background: var(--bg-tertiary);
  color: var(--error-color);
  padding: 0.3em 0.5em;
  border-radius: var(--border-radius-sm);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 0.9em;
  border: 1px solid var(--border-light);
  font-weight: 500;
}

/* 代码块样式 */
.markdown-content :deep(pre) {
  background: var(--bg-tertiary);
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius-lg);
  padding: 1.5em;
  overflow-x: auto;
  margin: 1.5em 0;
  box-shadow: var(--shadow-light);
  position: relative;
}

.markdown-content :deep(pre code) {
  background: transparent;
  color: var(--text-primary);
  padding: 0;
  border: none;
  border-radius: 0;
  font-size: 0.9em;
  line-height: 1.5;
  font-weight: normal;
}

/* 表格样式 */
.markdown-content :deep(table) {
  border-collapse: collapse;
  margin: 1.5em 0;
  width: 100%;
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-light);
}

.markdown-content :deep(table th),
.markdown-content :deep(table td) {
  border: 1px solid var(--border-light);
  padding: 0.8em 1em;
  text-align: left;
}

.markdown-content :deep(table th) {
  background: var(--bg-tertiary);
  font-weight: 600;
  color: var(--text-primary);
}

.markdown-content :deep(table tr:nth-child(even)) {
  background: var(--bg-secondary);
}

.markdown-content :deep(table tr:hover) {
  background: var(--primary-light);
}

/* 链接样式 */
.markdown-content :deep(a) {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s;
  border-bottom: 1px solid transparent;
}

.markdown-content :deep(a:hover) {
  color: var(--primary-hover);
  border-bottom-color: var(--primary-hover);
}

/* 图片样式 */
.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: var(--border-radius-lg);
  margin: 1em 0;
  box-shadow: var(--shadow-light);
  transition: transform 0.3s;
}

.markdown-content :deep(img:hover) {
  transform: scale(1.02);
  box-shadow: var(--shadow-medium);
}

/* 分割线样式 */
.markdown-content :deep(hr) {
  border: none;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--border-color), transparent);
  margin: 2em 0;
  border-radius: 1px;
}

/* 代码高亮样式优化 */
.markdown-content :deep(.hljs) {
  background: var(--bg-tertiary) !important;
  border-radius: var(--border-radius-lg);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 0.9em;
  line-height: 1.6;
  color: var(--text-primary) !important;
}

/* 代码高亮颜色 */
.markdown-content :deep(.hljs-keyword) {
  color: var(--primary-color) !important;
  font-weight: 600;
}

.markdown-content :deep(.hljs-string) {
  color: var(--success-color) !important;
}

.markdown-content :deep(.hljs-comment) {
  color: var(--text-tertiary) !important;
  font-style: italic;
}

.markdown-content :deep(.hljs-number) {
  color: var(--warning-color) !important;
}

.markdown-content :deep(.hljs-function) {
  color: #722ed1 !important;
}

.markdown-content :deep(.hljs-tag) {
  color: var(--success-color) !important;
}

.markdown-content :deep(.hljs-attr) {
  color: #722ed1 !important;
}

.markdown-content :deep(.hljs-title) {
  color: var(--primary-color) !important;
  font-weight: 600;
}

.markdown-content :deep(.hljs-variable) {
  color: var(--error-color) !important;
}

.markdown-content :deep(.hljs-built_in) {
  color: var(--info-color) !important;
}

/* 强调样式 */
.markdown-content :deep(strong) {
  font-weight: 600;
  color: var(--text-primary);
}

.markdown-content :deep(em) {
  font-style: italic;
  color: var(--text-secondary);
}

/* 删除线样式 */
.markdown-content :deep(del) {
  text-decoration: line-through;
  color: var(--text-tertiary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .markdown-content {
    font-size: 14px;
    line-height: 1.6;
  }
  
  .markdown-content :deep(h1) {
    font-size: 1.4em;
  }
  
  .markdown-content :deep(h2) {
    font-size: 1.3em;
  }
  
  .markdown-content :deep(h3) {
    font-size: 1.1em;
  }
  
  .markdown-content :deep(pre) {
    padding: 1em;
    font-size: 0.8em;
  }
  
  .markdown-content :deep(table) {
    font-size: 0.9em;
  }
  
  .markdown-content :deep(table th),
  .markdown-content :deep(table td) {
    padding: 0.6em 0.8em;
  }
  
  .markdown-content :deep(blockquote) {
    padding: 0.8em 1.2em;
    margin: 1em 0;
  }
}

@media (max-width: 480px) {
  .markdown-content {
    font-size: 13px;
  }
  
  .markdown-content :deep(ul),
  .markdown-content :deep(ol) {
    padding-left: 1.5em;
  }
  
  .markdown-content :deep(pre) {
    padding: 0.8em;
    overflow-x: scroll;
  }
  
  .markdown-content :deep(table) {
    font-size: 0.8em;
  }
}
</style>
