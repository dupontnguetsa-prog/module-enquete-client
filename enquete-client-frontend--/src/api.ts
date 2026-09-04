const cache=new Map<string,{expires:number;value:unknown}>()
const pending=new Map<string,Promise<unknown>>()
export async function api<T>(path:string, options:RequestInit={}):Promise<T>{
  const method=(options.method||'GET').toUpperCase(), key=`${method}:${path}`
  if(method==='GET'){const hit=cache.get(key);if(hit&&hit.expires>Date.now())return hit.value as T;const existing=pending.get(key);if(existing)return existing as Promise<T>}
  const headers=new Headers(options.headers)
  if(!(options.body instanceof FormData)&&!headers.has('Content-Type'))headers.set('Content-Type','application/json')
  const request=fetch(path,{credentials:'include',cache:'no-store',...options,headers})
    .then(async response=>{
      const contentType=response.headers.get('content-type')||''
      const payload=contentType.includes('application/json')?await response.json():await response.text()

      const currentPath = typeof window !== 'undefined' ? window.location.pathname : ''
  const isPublicRoute =
    currentPath === '/' ||
    currentPath === '/identification' ||
    currentPath === '/inscription' ||
    currentPath === '/mot-de-passe-oublie' ||
    currentPath === '/reinitialiser-mot-de-passe' ||
    currentPath.startsWith('/aide') ||
    currentPath.startsWith('/survey/')

      if(
    (response.status===401||response.status===403) &&
    typeof window!=='undefined' &&
    !isPublicRoute &&
    !path.startsWith('/api/auth/')
      ){
        window.location.replace('/identification?error=session-expired')
        throw new Error('Session expirée. Veuillez vous reconnecter.')
      }

      if(!response.ok){const message=typeof payload==='string'?payload:(payload?.message||'Une erreur est survenue.');throw new Error(message)}
      if(method==='GET')cache.set(key,{expires:Date.now()+15000,value:payload})
      else cache.clear()
      return payload as T
    }).finally(()=>pending.delete(key))
  if(method==='GET')pending.set(key,request)
  return request
}

export function invalidateApiCache(){cache.clear()}
