import { buildSseUrl } from '../services/api';

function normalizeChunk(chunk) {
  if (chunk === null || chunk === undefined) {
    return '';
  }

  return String(chunk);
}

export function openChatStream({ path, params, onChunk, onComplete, onError }) {
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
    const chunk = normalizeChunk(event.data);
    if (chunk && chunk !== '[DONE]') {
      onChunk?.(chunk);
    } else if (chunk === '[DONE]') {
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
