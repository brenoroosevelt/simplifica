import { reactive } from 'vue'

export function useSnackbar() {
  const snackbar = reactive({
    show: false,
    message: '',
    color: 'success',
  })

  function showSnackbar(message: string, color = 'success'): void {
    snackbar.show = true
    snackbar.message = message
    snackbar.color = color
  }

  return {
    snackbar,
    showSnackbar,
  }
}
