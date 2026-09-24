export function createChatId() {
  const randomPart = Math.random().toString(36).slice(2, 10);
  return `chat_${Date.now()}_${randomPart}`;
}

export function createMessage(role, content = '', extra = {}) {
  return {
    id: `${role}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    role,
    content,
    ...extra,
  };
}
