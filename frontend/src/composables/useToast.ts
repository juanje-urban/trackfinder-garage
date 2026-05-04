import { readonly, ref } from 'vue'

type ToastTone = 'success' | 'error' | 'neutral'

type ShowToastOptions = {
  tone?: ToastTone
  durationMs?: number
}

const isOpen = ref(false)
const message = ref('')
const tone = ref<ToastTone>('success')

let hideTimeout: ReturnType<typeof setTimeout> | null = null

// 'ref' crea valores reactivos simples, cuando cambio .value, Vue refresca quien los esté usando.
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

  // Reutilizamos un único toast global en lugar de crear uno nuevo por componente.
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
    // readonly nos permite leer el estado, pero obliga a cambiarlo con las funciones showToast o hideToast.
    isOpen: readonly(isOpen),
    message: readonly(message),
    tone: readonly(tone),
    showToast,
    hideToast,
  }
}
