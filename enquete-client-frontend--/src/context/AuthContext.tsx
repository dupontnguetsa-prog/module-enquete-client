import { createContext,useCallback,useContext,useEffect,useMemo,useState,type ReactNode } from 'react'
import { api } from '../api'
import type { UserProfile } from '../types'

type AuthValue={user:UserProfile|null;loading:boolean;refresh:()=>Promise<UserProfile|null>;logout:()=>Promise<void>}
const AuthContext=createContext<AuthValue|null>(null)
export function AuthProvider({children}:{children:ReactNode}){
  const [user,setUser]=useState<UserProfile|null>(null); const [loading,setLoading]=useState(true)
  const refresh=useCallback(async()=>{try{const current=await api<UserProfile>('/api/auth/me');setUser(current);return current}catch{setUser(null);return null}},[])
  useEffect(()=>{void refresh().finally(()=>setLoading(false))},[refresh])
  const logout=useCallback(async()=>{await api('/api/auth/logout',{method:'POST'});setUser(null)},[])
  const value=useMemo(()=>({user,loading,refresh,logout}),[user,loading,refresh,logout]);return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
export function useAuth(){const v=useContext(AuthContext);if(!v)throw new Error('AuthProvider manquant');return v}
