export function subscribeRealtime(url:string,event:string,onMessage:()=>void){
    let source:EventSource|undefined
    let stopped=false
    let retry=1000
    const connect=()=>{if(stopped)return;source=new EventSource(url);source.addEventListener(event,onMessage);source.onopen=()=>{retry=1000};source.onerror=()=>{source?.close();if(!stopped){window.setTimeout(connect,retry);retry=Math.min(retry*2,30000)}}}
    connect()
    return()=>{stopped=true;source?.close()}
}
