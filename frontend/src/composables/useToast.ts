import { readonly, ref } from 'vue'

export type ToastTone = 'success' | 'error' | 'neutral'

type ShowToastOptions = {
  tone?: ToastTone
  durationMs?: number
}

const isOpen = ref(false)
const message = ref('')
const tone = ref<ToastTone>('success')

let hideTimeout: ReturnType<typeof setTimeout> | null = null

function clearHideTimeout() {
  if (hideTimeout !== null) {
    clearTimeout(hideTimeout)
    hideTimeout = null
  }
}

function hideToast() {
  clearHideTimeout()
  isOpen.value = false
}

function showToast(nextMessage: string, options: ShowToastOptions = {}) {
  clearHideTimeout()

  message.value = nextMessage
  tone.value = options.tone ?? 'success'
  isOpen.value = true

  const durationMs = options.durationMs ?? 3200
  hideTimeout = setTimeout(() => {
    isOpen.value = false
    hideTimeout = null
  }, durationMs)
}

export function useToast() {
  return {
    isOpen: readonly(isOpen),
    message: readonly(message),
    tone: readonly(tone),
    showToast,
    hideToast,
  }
}
