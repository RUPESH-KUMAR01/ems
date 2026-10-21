import { useEffect, useState } from 'react'

const KEY = 'ems.theme'

export function useTheme() {
  const [dark, setDark] = useState(() => {
    const stored = localStorage.getItem(KEY)
    if (stored) return stored === 'dark'
    return window.matchMedia('(prefers-color-scheme: dark)').matches
  })

  useEffect(() => {
    document.documentElement.classList.toggle('dark', dark)
    localStorage.setItem(KEY, dark ? 'dark' : 'light')
  }, [dark])

  return { dark, setDark, toggle: () => setDark((d) => !d) }
}
