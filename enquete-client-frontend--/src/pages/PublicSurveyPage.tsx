import {useEffect,useState,type CSSProperties} from "react";
import {useParams,useSearchParams} from "react-router-dom";
import {api} from "../api";
import type {Survey,SurveyQuestion} from "../types";
import "../styles/public-survey.css";
import {IntercomWidget} from "../components/IntercomWidget";
import {getNextQuestionIndex} from "../utils/surveyLogic";

export default function PublicSurveyPage(){
  const {key}=useParams();
  const [params]=useSearchParams();
  const [survey,setSurvey]=useState<Survey|null>(null);
  const [index,setIndex]=useState(0);
  const [answers,setAnswers]=useState<Record<string,unknown>>({});
  const [done,setDone]=useState(false);
  const [actionMessage,setActionMessage]=useState("");
  const [error,setError]=useState("");
  const [validation,setValidation]=useState("");
  const [collapsed,setCollapsed]=useState(false);
  const started=useState(()=>new Date().toISOString())[0];

  useEffect(()=>{
    const publicKey=key||"";
    if(!publicKey)return;
    void api<Survey>(`/api/public/surveys/${publicKey}`)
      .then(s=>{
        setSurvey(s);
        return api(`/api/public/surveys/${publicKey}/view`,{method:"POST",body:JSON.stringify({channel:s.channels[0]||"LINK",pageUrl:window.location.href})});
      })
      .catch((e:unknown)=>setError(e instanceof Error?e.message:"Enquête indisponible."));
  },[key]);

  if(error)return <div className="public-center"><h1>Enquête indisponible</h1><p>{error}</p></div>;
  if(!survey)return <div className="public-center">Chargement…</div>;
  if(done)return <div className="public-center"><div className="public-logo public-thanks-logo"><img src="/logo-afriland.png" alt="Afriland First Bank" /></div><span className="eyebrow">MERCI</span><h1>Merci pour votre réponse.</h1>{actionMessage&&<p>{actionMessage}</p>}<p>Votre retour a bien été enregistré.</p><IntercomWidget compact/></div>;
  const currentSurvey=survey;
  const q=currentSurvey.questions[index];
  if(!q)return <div className="public-center">Aucune question configurée.</div>;

  const answer=answers[String(index)];
  async function next(){
    if(q.required&&(answer===undefined||answer===''||(Array.isArray(answer)&&answer.length===0))){setValidation("Sélectionnez une réponse pour continuer.");return;}
    setValidation("");
    const nextIndex=getNextQuestionIndex(currentSurvey.questions,currentSurvey.logicRules,index,answers);
    if(nextIndex<currentSurvey.questions.length){setIndex(nextIndex);return;}
    try{
      const result=await api<{action?:string}>(`/api/public/surveys/${currentSurvey.publicKey}/responses`,{method:"POST",body:JSON.stringify({answers,anonymous:Boolean(currentSurvey.settings.anonymous),customerId:params.get("customerId")?Number(params.get("customerId")):null,email:params.get("email"),startedAt:started})});
      const redirectAction=result.action?.split('|').find(part=>part.startsWith('REDIRECT:'));
      if(redirectAction){window.location.assign(redirectAction.slice('REDIRECT:'.length)||'/');return;}
      if(result.action?.includes('MESSAGE'))setActionMessage(result.action.split('|').find(part=>part.startsWith('MESSAGE:'))?.slice('MESSAGE:'.length)||"Merci, votre retour a bien été pris en compte.");
      setDone(true);
    }catch(e){setError(e instanceof Error?e.message:"Impossible d’enregistrer la réponse.");}
  }

  const brandName=String(currentSurvey.settings.brandName||'Afriland First Bank');
  return <div className="public-survey" style={{'--survey-brand':String(currentSurvey.settings.primaryColor||'#d20a2e')} as CSSProperties}>
    {collapsed
      ? <button className="survey-launcher" onClick={()=>setCollapsed(false)} aria-label="Ouvrir l’enquête">
          <span className="survey-launcher-icon">✦</span><span className="survey-launcher-label">Votre avis</span>
        </button>
      : <div className="public-card survey-widget">
          <div className="public-head">
            <div className="public-brand"><div className="public-logo"><img src="/logo-afriland.png" alt="" /></div><div><b>{brandName}</b><small>Votre avis compte</small></div></div>
            <button className="survey-close" onClick={()=>setCollapsed(true)} aria-label="Réduire l’enquête">−</button>
          </div>
          <div className="public-progress"><i style={{width:`${((index+1)/currentSurvey.questions.length)*100}%`}}/></div>
          <div className="public-body"><div className="survey-step">QUESTION {index+1} / {currentSurvey.questions.length}</div><h1>{q.title}</h1>{q.description&&<p>{q.description}</p>}<Answer q={q} value={answer} onChange={value=>{setValidation("");setAnswers({...answers,[String(index)]:value})}}/>{validation&&<div className="public-validation" role="alert">{validation}</div>}<div className="public-actions">{index>0&&<button className="public-back" onClick={()=>setIndex(index-1)}>← Retour</button>}<button className="public-button" onClick={()=>void next()}>{index<currentSurvey.questions.length-1?'Continuer':'Envoyer ma réponse'}</button></div><small className="public-privacy">Réponses confidentielles · 1 min</small></div>
        </div>}
    <IntercomWidget compact/>
  </div>;
}

function Answer({q,value,onChange}:{q:SurveyQuestion;value:unknown;onChange:(v:unknown)=>void}){
  if(q.type==='NPS'||q.type==='SCALE')return <><div className="public-scale">{Array.from({length:q.max-q.min+1},(_,i)=>q.min+i).map(n=><button key={n} className={String(value)===String(n)?'selected':''} onClick={()=>onChange(n)}>{n}</button>)}</div><div className="public-labels"><span>{q.minLabel}</span><span>{q.maxLabel}</span></div></>;
  if(q.type==='STARS')return <div className="public-stars">{[1,2,3,4,5].map(n=><button key={n} className={Number(value)===n?'selected':''} onClick={()=>onChange(n)}>★</button>)}</div>;
  if(q.type==='SINGLE_CHOICE'||q.type==='YES_NO'){const opts=q.type==='YES_NO'?['Oui','Non']:q.options;return <div className="public-options">{opts.map(o=><button key={o} className={value===o?'selected':''} onClick={()=>onChange(o)}>{o}</button>)}</div>}
  if(q.type==='MULTIPLE_CHOICE'){const list=Array.isArray(value)?value as string[]:[];return <div className="public-options">{q.options.map(o=><button key={o} className={list.includes(o)?'selected':''} onClick={()=>onChange(list.includes(o)?list.filter(x=>x!==o):[...list,o])}>{o}</button>)}</div>}
  return <textarea className="public-text" rows={5} value={String(value||'')} onChange={e=>onChange(e.target.value)} placeholder="Votre réponse…"/>;
}
