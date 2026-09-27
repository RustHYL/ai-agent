import { openChatStream, parseBaseResponseChunk } from '../utils/sse';

export const AI_API = {
  loveAppChatSse: '/ai/love_app/chat/sse',
  selfManusChat: '/ai/self_manus/chat',
};

/**
 * AI 恋爱大师 SSE 流式对话
 * 对应后端：GET /ai/love_app/chat/sse?message=&chatId=
 */
export function doChatWithLoveAppSse({ message, chatId, onChunk, onComplete, onError }) {
  return openChatStream({
    path: AI_API.loveAppChatSse,
    params: { message, chatId },
    parseChunk: parseBaseResponseChunk,
    onChunk,
    onComplete,
    onError,
  });
}

/**
 * AI 超级智能体 SSE 流式对话
 * 对应后端：GET /ai/self_manus/chat?message=
 */
export function doChatWithSelfManus({ message, onChunk, onComplete, onError }) {
  return openChatStream({
    path: AI_API.selfManusChat,
    params: { message },
    parseChunk: parseBaseResponseChunk,
    onChunk,
    onComplete,
    onError,
  });
}
