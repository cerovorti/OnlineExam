export function forceCleanDialogs() {
  document.body.style.overflow = ''
  document.querySelectorAll('.el-overlay').forEach(el => {
    if (el.parentElement === document.body) {
      el.parentElement.removeChild(el)
    }
  })
}
