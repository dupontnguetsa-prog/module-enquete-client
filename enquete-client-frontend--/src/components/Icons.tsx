import type { SVGProps } from 'react'

const paths: Record<string, string> = {
    grid: 'M4 4h6v6H4zM14 4h6v6h-6zM4 14h6v6H4zM14 14h6v6h-6z',
    survey: 'M6 3h12a2 2 0 0 1 2 2v14l-3-2-3 2-3-2-3 2-3-2-3 2V5a2 2 0 0 1 2-2Zm2 5h8M8 12h8M8 16h5',
    plus: 'M12 5v14M5 12h14',
    reply: 'M5 6h14v10H8l-3 3V6Zm4 4h6',
    share: 'M18 8a3 3 0 1 0-2.83-4A3 3 0 0 0 15 5.17L9 8.4a3 3 0 1 0 0 7.2l6 3.23a3 3 0 1 0 .94-1.77l-6-3.23a3 3 0 0 0 0-2.66l6-3.23A3 3 0 0 0 18 8Z',
    mail: 'M3 5h18v14H3zM3 6l9 7 9-7',
    code: 'M8 9 4 12l4 3m8-6 4 3-4 3m-3-9-2 12',
    arrow: 'M5 12h13m-5-5 5 5-5 5',
    play: 'M8 5l11 7-11 7V5Z',
    pause: 'M8 5v14m8-14v14',
    archive: 'M4 7h16v13H4zM3 4h18v3H3zM9 12h6',
    copy: 'M8 8h11v11H8zM5 16H4a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h11a1 1 0 0 1 1 1v1',
    trash: 'M5 7h14M10 11v6m4-6v6M8 7l1-3h6l1 3m-9 0 1 13h10l1-13',
    check: 'm5 12 4 4L19 6',
    chart: 'M5 19V9m7 10V5m7 14v-7',
    settings: 'M12 9a3 3 0 1 0 0 6 3 3 0 0 0-3-3Zm0-6 1 2 2 .5 1.5-1 1.4 1.4-1 1.5.5 2 2 1-.2 2h2v2l-2 .2-1 2 1 1.5-1.4 1.4-1.5-1-2 .5-1 2h-2l-1-2-2-.5-1.5 1L5 19l1-1.5-.5-2-2-.2v-2l2-.2.5-2L5 9l1.4-1.4 1.5 1 2-.5 1-2H12Z',
}

export function Icon({ name, ...props }: { name: string } & SVGProps<SVGSVGElement>) {
    return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" {...props}><path d={paths[name] || paths.grid} /></svg>
}
