import { buildSseUrl } from '../services/api';

function normalizeChunk(chunk) {
  if (chunk === null || chunk === undefined) {
    return '';
  }

  return String(chunk);
}

/**
 * 解析 BaseResponse 包装的 SSE 片段。
 * 成功：{ code: 0, data, message: "ok" }
 * 失败：{ code: 非0, data: null, message }
 */
export function parseBaseResponseChunk(raw) {
  const text = normalizeChunk(raw);
  if (!text) {
    return { chunk: '', error: null, done: false };
  }

  if (text === '[DONE]') {
    return { chunk: '', error: null, done: true };
  }

  try {
    const parsed = JSON.parse(text);
    if (parsed && typeof parsed === 'object' && 'code' in parsed) {
      if (parsed.code !== 0) {
        return {
          chunk: '',
          error: new Error(parsed.message || `请求失败（code: ${parsed.code}）`),
          done: false,
        };
      }

      const data = parsed.data;
      if (data && typeof data === 'object') {
        return {
          chunk: '',
          event: data,
          error: null,
          done: false,
        };
      }
      return {
        chunk: data === null || data === undefined ? '' : String(data),
        event: null,
        error: null,
        done: false,
      };
    }
  } catch {
    // 非 JSON 时按原文处理，避免兼容期间整段中断
  }

  return { chunk: text, error: null, done: false };
}

export function openChatStream({ path, params, onChunk, onComplete, onError, parseChunk }) {
  const url = buildSseUrl(path, params);
  const eventSource = new EventSource(url);
  let completed = false;

  const finish = () => {
    if (completed) {
      return;
    }
    completed = true;
    onComplete?.();
  };

  eventSource.onmessage = (event) => {
    const raw = normalizeChunk(event.data);
    const parsed = parseChunk
      ? parseChunk(raw)
      : raw === '[DONE]'
        ? { chunk: '', error: null, done: true }
        : { chunk: raw, error: null, done: false };

    if (parsed.error) {
      eventSource.close();
      onError?.(parsed.error);
      finish();
      return;
    }

    if (parsed.event) {
      onChunk?.(parsed.event);
    } else if (parsed.chunk) {
      onChunk?.(parsed.chunk);
    }

    if (parsed.done) {
      eventSource.close();
      finish();
    }
  };

  eventSource.onerror = (error) => {
    eventSource.close();
    onError?.(error);
    finish();
  };

  return {
    close() {
      eventSource.close();
      finish();
    },
  };
}
