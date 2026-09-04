import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'

type ThemeContextValue={theme:'light'|'dark';toggle:()=>void}
const ThemeContext=createContext<ThemeContextValue|null>(null)
export function ThemeProvider({children}:{children:ReactNode}){const [theme,setTheme]=useState<'light'|'dark'>(()=>localStorage.getItem('afriland-theme')==='dark'?'dark':'light');useEffect(()=>{document.documentElement.dataset.theme=theme;localStorage.setItem('afriland-theme',theme)},[theme]);const value=useMemo(()=>({theme,toggle:()=>setTheme(t=>t==='dark'?'light':'dark')}),[theme]);return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>}
export function useTheme(){const v=useContext(ThemeContext);if(!v)throw new Error('ThemeProvider manquant');return v}
