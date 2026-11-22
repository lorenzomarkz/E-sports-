export const MODALIDADES = ['Futebol', 'Futsal', 'Basquete', 'Vôlei', 'Handebol', 'E-sports'];
export const JOGOS = ['League of Legends', 'Valorant', 'Counter-Strike 2', 'Rocket League', 'Free Fire'];
export function normalizar(nome: string): string {
 return nome.trim().normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
}
export function rotuloResultado(modalidade: string, jogo?: string | null): string {
 if (jogo === 'Rocket League') return 'Gols';
 if (jogo === 'League of Legends') return 'Vitórias na série';
 if (jogo === 'Valorant' || jogo === 'Counter-Strike 2') return 'Mapas vencidos';
 if (normalizar(modalidade) === 'basquete') return 'Pontos';
 if (normalizar(modalidade) === 'volei') return 'Sets vencidos';
 if (['futebol','futsal','handebol'].includes(normalizar(modalidade))) return 'Gols';
 return 'Placar';
}
export function hoje(): string {
 return new Intl.DateTimeFormat('en-CA', {timeZone:'America/Sao_Paulo',year:'numeric',month:'2-digit',day:'2-digit'}).format(new Date());
}
